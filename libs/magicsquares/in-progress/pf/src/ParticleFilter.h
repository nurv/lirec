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

using namespace std;

class ParticleFilter
{
public:
	ParticleFilter(unsigned int NumParticles);
	~ParticleFilter() {}

	// An observation of the state
	class Observation
	{
	public:
		float Angle,Dist;
	};

	// The hidden state we want to estimate
	class State
	{
	public:
		float x,y;
		// Gets the observation we would expect from this state
		// includes some noise, based on our expectation of the sensor
		// used to make the measurement.
		Observation Observe();
	};

	class Particle
	{
	public:
		// A particle is a state with a cooresponding probabilistic 
		// weighting
		State m_State;
		float m_Weight;
	};
	
	// Use the model to predict the next state of each particle, 
	// not forgetting to add some prediction noise. This should 'spread'
	// the particles out. 
	void Predict();
	
	// Set the particle weights according to the current real observation,
	// and resample particles with low weights, which 'tightens' the particles in.
	void Update(const Observation &Obs);

	void SetNoiseLevels(float Prediction, float ObsAngle, float ObsDist);
	void SetResampleWeight(float Weight) { m_ResampleWeight=Weight; }

	// For debug rendering
	const vector<Particle> &GetParticles() { return m_Particles; }

private:
	float FloatNoise();
	float GaussianNoise();
	void Resample();
	
	float m_PredictionNoiseLevel;
	float m_ObsAngleNoiseLevel;
	float m_ObsDistNoiseLevel;
	float m_ResampleWeight;
	
	vector<Particle> m_Particles;
};

#endif
