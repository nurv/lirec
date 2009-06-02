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

#include <vector>
#include <map>
#include <set>
#include "Vector.h"
#include "Matrix.h"

#ifndef FOAM_CLASSIFIER
#define FOAM_CLASSIFIER

// A base class classifier

class Classifier
{
public:
	Classifier(unsigned int FeatureSize);
	~Classifier();
	
	void AddFeature(int group, const Vector<float> &f);
	virtual int Classify(const Vector<float> &f) = 0;
	
protected:
	void CalcMean();
	
	typedef std::vector<Vector<float> > FeatureVec;
	typedef std::map<int,FeatureVec> FeatureMap;
	
	FeatureMap m_Features;
	unsigned int m_FeatureSize;

	Vector<float> m_Mean;
	
};

#endif
