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


ParticleFilter::ParticleFilter(unsigned int NumParticles)
{
	m_Particles.resize(NumParticles);
	
	// Start the particles off in random positions
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
		i->m_State.x = FloatNoise()*100;
		i->m_State.y = FloatNoise()*100;
	}
}

float ParticleFilter::FloatNoise()
{
	return rand()%INT_MAX/float(INT_MAX)*2-1;
}

float ParticleFilter::GaussianNoise()
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

ParticleFilter::Observation ParticleFilter::State::Observe()
{
	Observation NewObs;
	NewObs.Angle=atan(y/x);
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
		// As we don't have a model we can use for prediction
		// we just add some noise to the state here.
		// It would be quite simple to add velocity into our state,
		// in which case we'd add the velocity to the position here 
		// as well.
		i->m_State.x+=GaussianNoise()*m_PredictionNoiseLevel;
		i->m_State.y+=GaussianNoise()*m_PredictionNoiseLevel;
	}
}
	
void ParticleFilter::Update(const Observation &Obs)
{
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		// assign the weight to each particle, based on the 
		Observation PObs=i->m_State.Observe();
		
		float AngErr = Obs.Angle-PObs.Angle+(m_ObsAngleNoiseLevel*GaussianNoise());
		float DistErr = Obs.Dist-PObs.Dist+(m_ObsDistNoiseLevel*GaussianNoise());
		
		i->m_Weight =1/(AngErr*AngErr*100 + DistErr*DistErr);
	}
	
	Resample();
}

void ParticleFilter::Resample()
{
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
		if (i->m_Weight<m_ResampleWeight)
		{
			// cast to a random position
			i->m_State.x = FloatNoise()*100;
			i->m_State.y = FloatNoise()*100;
		}	
	}
}
