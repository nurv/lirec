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

#include "LDAClassifier.h"

LDAClassifier::LDAClassifier(unsigned int FeatureSize) :
Classifier(FeatureSize)
{
}

LDAClassifier::~LDAClassifier()
{
}

int LDAClassifier::Classify(const Vector<float> &v)
{
	return 0;
}

void LDAClassifier::CalcGroupMeans()
{	
	for (FeatureMap::iterator i=m_Features.begin();
		i!=m_Features.end(); ++i)
	{
		m_GroupMean[i->first]=Vector<float>(m_FeatureSize);
		m_GroupMean[i->first].Zero();		
		for (FeatureVec::iterator vi = i->second.begin(); vi!=i->second.end(); ++vi)
		{
			m_GroupMean[i->first]+=*vi;
		}		
		m_GroupMean[i->first]/=i->second.size();
	}
}

void LDAClassifier::CalcMeanCorrected()
{
}

void LDAClassifier::CalcCovariance()
{
}

void LDAClassifier::CalcPooledCovariance()
{
}

void LDAClassifier::CalcPriorProbablity()
{
}

