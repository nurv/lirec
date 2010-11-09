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

#ifndef FOAM_PARTICLE_FILTER
#define FOAM_PARTICLE_FILTER

#include <vector>
#include <iostream>
#include <assert.h>

using namespace std;

/////////////////////////////////////////////////////////
// A general purpose abstract base class particle filter
class ParticleFilter
{
public:
	ParticleFilter(unsigned int NumParticles);
	virtual ~ParticleFilter();

    // Needs an initialise as we have to call virtual funcs
    void Initialise();

    class State;

	// An observation of the state
	class Observation
	{
	public:
        //virtual float Weight(const Observation *Target)=0;
        virtual float Weight(const State *State) const =0;
	};

	// The hidden state we want to estimate
	class State
	{
	public:
		// Gets the observation we would expect from this state
		//virtual Observation *Observe()=0;
		// Put the state into a random position and velocity
		virtual void Randomise()=0;
		// Add a small random amount to the position and velocity
		virtual void Jitter()=0;
        // Run the internal prediction for this state, given the state noise level
        virtual void Predict(float Noise)=0;
        // Assign from given state
        virtual State &operator=(const State &other)=0;
	};

	class Particle
	{
	public:
		// A particle is a state with a cooresponding probabilistic 
		// weighting
		State *m_State;
		float m_Weight;
	};

    // Needs to be done by the derived classes,
    // finds a weighted average of the states given the 
    // particle weights.
    virtual State *WeightedAverage()=0;
    virtual State *NewState()=0;
       
	// Use the model to predict the next state of each particle, 
	// not forgetting to add some prediction noise. This should 'spread'
	// the particles out. 
	void Predict();

	// Set the particle weights according to the current real observation,
	// and resample particles with low weights, which 'tightens' the particles in.
	State *Update(const Observation *Obs);

	void SetNoiseLevels(float Prediction);
	void SetResampleWeight(float Weight) { m_ResampleWeight=Weight; }

	// For debug rendering
	const vector<Particle> &GetParticles() { return m_Particles; }

	// Returns the particle with the highest weight
	Particle* GetMostLikely();
	
protected:

	
	// Reset particles with low weight
	void Resample();
	
	float m_PredictionNoiseLevel;
	float m_ResampleWeight;
	
	vector<Particle> m_Particles;
};

#endif
