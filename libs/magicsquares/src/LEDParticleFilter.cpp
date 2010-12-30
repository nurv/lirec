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

#include "LEDParticleFilter.h"
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

float Distance(float ax, float ay, float bx, float by)
{
	float x=ax-bx;
	float y=ay-by;
	return sqrt(x*x+y*y);
}

/////////////////////////////////////////////////////////
// Observation functions

float LEDParticleFilter::LEDObservation::Weight(const State *state) const
{
    const LEDState *rs = static_cast<const LEDState*>(state);

    float v=m_Image->Get(rs->x,rs->y,0);
    printf("%f\n", v);
    return v;
}

/////////////////////////////////////////////////////////////
// State functions

void LEDParticleFilter::LEDState::Predict(float Noise)
{
    // Apply the velocity to the particle position
    x+=dx;
    y+=dy;
    // Add some noise to the position
    x+=GaussianNoise()*Noise;
    y+=GaussianNoise()*Noise;
}

void LEDParticleFilter::LEDState::Randomise()
{
    x = FloatNoise()*100;
    y = FloatNoise()*100;
    dx = GaussianNoise();
    dy = GaussianNoise();
}
		
// Add a small random amount to the position and velocity
void LEDParticleFilter::LEDState::Jitter()
{
    x += GaussianNoise()*5;
    y += GaussianNoise()*5;
    dx += GaussianNoise()*0.05;
    dy += GaussianNoise()*0.05;
}

ParticleFilter::State &LEDParticleFilter::LEDState::operator=(const ParticleFilter::State &other)
{
    const LEDState rs = static_cast<const LEDState&>(other);
    x=rs.x; y=rs.y;
    dx=rs.dx; dy=rs.dy;
    return *this;
}

/////////////////////////////////////////////////////////////////
// LED particle filter functions

ParticleFilter::State *LEDParticleFilter::WeightedAverage()
{
	// Find the weighted average to get an estimate
	LEDState *ret = new LEDState;
    ret->x=0;
    ret->y=0;

	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
        LEDParticleFilter::LEDState* state = static_cast<LEDParticleFilter::LEDState*>(i->m_State);
		ret->x += state->x * i->m_Weight;
        ret->y += state->y * i->m_Weight;	   
	}
	
	return ret;
}

ParticleFilter::State *LEDParticleFilter::NewState()
{
    return new LEDState();
}

