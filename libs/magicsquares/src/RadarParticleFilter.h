
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

#include "ParticleFilter.h"

#ifndef FOAM_RADAR_PARTICLE_FILTER
#define FOAM_RADAR_PARTICLE_FILTER

// Util functions for this pf
float FloatNoise();
float GaussianNoise();
float GetAngle(float x, float y);
void GetPos(float a, float d, float &x, float &y);
float Distance(float ax, float ay, float bx, float by);

// A radar particle filter example
class RadarParticleFilter : public ParticleFilter
{
public:
    
    RadarParticleFilter(int NumParticles) :
        ParticleFilter(NumParticles)
    {
    }

    ~RadarParticleFilter() {}
    
    class RadarState;

    // Our observation class
    class RadarObservation : public ParticleFilter::Observation
    {
    public:        
        // Return a weight compared against the target
        virtual float Weight(const State *state) const;

        float Dist;
        float Angle;
    };
        
    // The state of an object in the radar
    class RadarState : public ParticleFilter::State
    {
    public:
        RadarState() {}

        virtual Observation *Observe();
        virtual void Predict(float Noise);
        virtual void Randomise();
        virtual void Jitter();
        virtual State &operator=(const State &other);

        float x;
        float y;
        float dx;
        float dy;
    };
        
    // These functions need to be defined by derived particle filters
    virtual State *WeightedAverage();
    virtual State *NewState();

};

#endif
