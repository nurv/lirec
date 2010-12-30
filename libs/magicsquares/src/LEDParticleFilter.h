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
#include "Image.h"

#ifndef FOAM_LED_PARTICLE_FILTER
#define FOAM_LED_PARTICLE_FILTER

// Util functions for this pf
float FloatNoise();
float GaussianNoise();

// A LED particle filter example
class LEDParticleFilter : public ParticleFilter
{
public:
    
    LEDParticleFilter(int NumParticles) :
        ParticleFilter(NumParticles)
    {
    }

    ~LEDParticleFilter() {}
    
    // Our observation class
    class LEDObservation : public ParticleFilter::Observation
    {
    public:        
        // Return a weight compared against the target
        virtual float Weight(const State *state) const;
        Image *m_Image;
    };
        
    // The state of the LED
    class LEDState : public ParticleFilter::State
    {
    public:
        LEDState() {}

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
