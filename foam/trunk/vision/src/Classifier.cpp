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

#include "Classifier.h"

using namespace std;

Classifier::Classifier(unsigned int FeatureSize) :
m_FeatureSize(FeatureSize),
m_Mean(FeatureSize)
{
}

Classifier::~Classifier()
{
}

void Classifier::AddFeatureToGroup(int group, const Vector<float> &v) 
{ 
	assert(v.Size()==m_FeatureSize);
	m_Features[group].push_back(v); 
	// possibly overkill to do this each time we add a new feature...
	CalcGroupMeans();
	//m_GroupMeans[group]=v;
}

void Classifier::CalcMean()
{
	m_Mean.Zero();
	
	for (FeatureMap::iterator i=m_Features.begin();
		i!=m_Features.end(); ++i)
	{
		for (FeatureVec::iterator vi = i->second.begin(); vi!=i->second.end(); ++vi)
		{
			m_Mean+=*vi;
		}
	}
	
	m_Mean/=m_FeatureSize;
}

void Classifier::CalcGroupMeans()
{
	for (FeatureMap::iterator i=m_Features.begin();
		i!=m_Features.end(); ++i)
	{
		Vector<float> mean(m_FeatureSize);
		mean.Zero();
		for (FeatureVec::iterator vi = i->second.begin(); vi!=i->second.end(); ++vi)
		{
			mean+=*vi;
		}
		mean/=i->second.size();		
		m_GroupMeans[i->first]=mean;
	}
}

