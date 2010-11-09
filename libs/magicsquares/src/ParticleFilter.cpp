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
#include <cstdlib>

using namespace std;

////////////////////////////////////////////////////////////////
// The particle filter

ParticleFilter::ParticleFilter(unsigned int NumParticles)
{
	m_Particles.resize(NumParticles);
}

ParticleFilter::~ParticleFilter()
{
	// Clean up
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
        delete i->m_State;
	}
}

void ParticleFilter::Initialise()
{
	// Start the particles off in random positions
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
        i->m_State = NewState();
		i->m_State->Randomise();
	}
}

void ParticleFilter::SetNoiseLevels(float Prediction)
{
	m_PredictionNoiseLevel=Prediction;
}

void ParticleFilter::Predict()
{
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{
        i->m_State->Predict(m_PredictionNoiseLevel);
	}
}
	
ParticleFilter::State *ParticleFilter::Update(const Observation *Obs)
{
    // Resample first, which leaves the resampled particles
    // with incorrect weights
	Resample();
	
	float TotalWeight = 0;

    // Now resample
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		// Assign the weight to each particle, based on the difference 
		// between the one observation from the outside world, and the 
		// observation of the particle's state
		//Observation *PObs=i->m_State->Observe();
        //i->m_Weight = PObs->Weight(Obs);
        i->m_Weight = Obs->Weight(i->m_State);
        //delete PObs;
        
		TotalWeight+=i->m_Weight;
	}
	
    // Normalise the weights
	for (vector<Particle>::iterator i=m_Particles.begin(); 
		i!=m_Particles.end(); ++i)
	{	
		i->m_Weight/=TotalWeight;
	}

    // Return the estimate
    return WeightedAverage();
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
					i->m_State->Randomise();
				}
				else
				{
					// Copy settings from the 'best' current particle
					(*i->m_State)=(*Highest->m_State);
					// And jitter them a little - this stops the particles 
					// converging too much on one state
					i->m_State->Jitter();
                }
			}	
		}
	}
}

