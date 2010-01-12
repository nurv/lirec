// Copyright (C) 2009 foam
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

#include <limits.h>
#include <math.h>
#include "ParticleFilter.h"
#include <iostream>

using namespace std;

///////////////////////////////////////////////////////////////
// Some util functions

float FloatNoise()
{
	return rand()%99999/float(99999)*2-1;
}

float GaussianNoise()
{
	float l=0;
	float x=0;
	float y=0;
	while (l>=1 || l==0)
	{
		x=FloatNoise();
		y=FloatNoise();
		l=x*x+y*y;
	}
	return sqrt(-2*log(l)/l)*x;
}

// gets the angle of a vector from 0 to 360 degrees
float GetAngle(float x, float y)
{
	if(y==0) // prevent division by 0
	{
		if(x>=0) return 0;
		else return 180;
	}
	else 
	{
		if (y>0) return 90-atan(x/y)*180/M_PI;
		else return 270-atan(x/y)*180/M_PI;
	} 
}

// the inverse of above
void GetPos(float a, float d, float &x, float &y)
{
	a/=180/M_PI;
	x = cos(a)*d;
	y = sin(a)*d;
}

float Distance(float ax, float ay, float bx, float by)
{
	float x=ax-bx;
	float y=ay-by;
	return sqrt(x*x+y*y);
}

////////////////////////////////////////////////////////////////
// The particle filter

ParticleFilter::ParticleFilter(unsigned int NumParticles)
{
	m_Particles.resize(NumParticles);
	
	// Start the particles off in random positions
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
		i->m_State.Randomise();
	}
}


ParticleFilter::Observation ParticleFilter::State::Observe()
{
	Observation NewObs;
	NewObs.Angle=GetAngle(x,y);
	NewObs.Dist=sqrt(x*x+y*y);
	return NewObs;
}

void ParticleFilter::SetNoiseLevels(float Prediction, float ObsAngle, float ObsDist)
{
	m_PredictionNoiseLevel=Prediction;
	m_ObsAngleNoiseLevel=ObsAngle;
	m_ObsDistNoiseLevel=ObsDist;
}

void ParticleFilter::Predict()
{
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
		// Apply the velocity to the particle position
		i->m_State.x+=i->m_State.dx;
		i->m_State.y+=i->m_State.dy;
		// Add some noise to the position
		i->m_State.x+=GaussianNoise()*m_PredictionNoiseLevel;
		i->m_State.y+=GaussianNoise()*m_PredictionNoiseLevel;
	}
}
	
ParticleFilter::State ParticleFilter::Update(const Observation &Obs)
{
	Resample();
	
	float TotalWeight = 0;

	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		// Assign the weight to each particle, based on the difference 
		// between the one observation from the outside world, and the 
		// observation of the particle's state
		Observation PObs=i->m_State.Observe();
		
		// todo: angle error will be wrong around 360 -> 0 boundary
		float AngErr = (Obs.Angle-PObs.Angle)+(m_ObsAngleNoiseLevel*GaussianNoise());
		float DistErr = (Obs.Dist-PObs.Dist)+(m_ObsDistNoiseLevel*GaussianNoise());
		
		// Here we can use information about our sensor to modify the error from
		// different readings. We'll make it so the angle observation is less 'trustworthy'
		// than the distance readings. This has the effect of making the pdf into a cresent 
		// shape.
		i->m_Weight = 1/(fabs(AngErr)+fabs(DistErr));
		TotalWeight+=i->m_Weight;
	}
	
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		i->m_Weight/=TotalWeight;
	}
	
	// Find the weighted average to get an estimate
	State ret;
	ret.x=0; ret.y=0;
	
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		ret.x += i->m_State.x * i->m_Weight;
		ret.y += i->m_State.y * i->m_Weight;
	}
	
	return ret;
}

ParticleFilter::Particle* ParticleFilter::GetMostLikely()
{
	Particle *ret=NULL;
	float HighestWeight=0;
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
		if (i->m_Weight>HighestWeight)
		{
			ret = &(*i);
			HighestWeight = i->m_Weight;
		}
	}
	return ret;
}

void ParticleFilter::Resample()
{
	Particle *Highest = GetMostLikely();
	
	if (Highest!=NULL)
	{
		for (vector<Particle>::iterator i=m_Particles.begin(); 
			i!=m_Particles.end(); ++i)
		{
			// If this particle has a low weight
			if (i->m_Weight<m_ResampleWeight)
			{
				// Randomly choose between either...
				if (rand()%2==0)
				{
					// Cast to a new completely random position/velocity
					i->m_State.Randomise();
				}
				else
				{
					// Copy settings from the 'best' current particle
					i->m_State=Highest->m_State;
					// And jitter them a little - this stops the particles 
					// converging too much on one state
					i->m_State.Jitter();
				}
			}	
		}
	}
}

