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

#include "RadarParticleFilter.h"
#include <math.h>
#include <iostream>
#include <cstdlib>

using namespace std;

/////////////////////////////////////////////////////////
// Util functions

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

/////////////////////////////////////////////////////////
// Observation functions

float RadarParticleFilter::RadarObservation::Weight(const State *state) const
{
    /*
    const RadarObservation *RadarOther = static_cast<const RadarObservation *>(Other);
    
    // todo: angle error will be wrong around 360 -> 0 boundary
    float AngErr = (Angle-RadarOther->Angle)+(0.01*GaussianNoise());
    float DistErr = (Dist-RadarOther->Dist)+(0.01*GaussianNoise());
		
    // Here we can use information about our sensor to modify the error from
    // different readings. We'll make it so the angle observation is less 'trustworthy'
    // than the distance readings. This has the effect of making the pdf into a cresent 
    // shape.
    return 1/(fabs(AngErr)+fabs(DistErr));
    */
    return 1;
}

/////////////////////////////////////////////////////////////
// State functions

ParticleFilter::Observation *RadarParticleFilter::RadarState::Observe()
{
	RadarObservation *NewObs = new RadarObservation;
	NewObs->Angle=GetAngle(x,y);
	NewObs->Dist=sqrt(x*x+y*y);
	return NewObs;
}

void RadarParticleFilter::RadarState::Predict(float Noise)
{
    // Apply the velocity to the particle position
    x+=dx;
    y+=dy;
    // Add some noise to the position
    x+=GaussianNoise()*Noise;
    y+=GaussianNoise()*Noise;
}

void RadarParticleFilter::RadarState::Randomise()
{
    x = FloatNoise()*100;
    y = FloatNoise()*100;
    dx = GaussianNoise();
    dy = GaussianNoise();
}
		
// Add a small random amount to the position and velocity
void RadarParticleFilter::RadarState::Jitter()
{
    x += GaussianNoise()*5;
    y += GaussianNoise()*5;
    dx += GaussianNoise()*0.05;
    dy += GaussianNoise()*0.05;
}

ParticleFilter::State &RadarParticleFilter::RadarState::operator=(const ParticleFilter::State &other)
{
    const RadarState rs = static_cast<const RadarState&>(other);
    x=rs.x; y=rs.y;
    dx=rs.dx; dy=rs.dy;
    return *this;
}

/////////////////////////////////////////////////////////////////
// Radar particle filter functions

ParticleFilter::State *RadarParticleFilter::WeightedAverage()
{
	// Find the weighted average to get an estimate
	RadarState *ret = new RadarState;
    ret->x=0;
    ret->y=0;

	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
        RadarParticleFilter::RadarState* RState = static_cast<RadarParticleFilter::RadarState*>(i->m_State);
		ret->x += RState->x * i->m_Weight;
        ret->y += RState->y * i->m_Weight;	   
	}
	
	return ret;
}

ParticleFilter::State *RadarParticleFilter::NewState()
{
    return new RadarState();
}
