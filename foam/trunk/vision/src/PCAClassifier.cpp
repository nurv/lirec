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

#include <cfloat>
#include "PCAClassifier.h"

using namespace std;

PCAClassifier::PCAClassifier(const PCA &pca) :
Classifier(pca.GetParamsSize()),
m_PCA(pca)
{
}

PCAClassifier::~PCAClassifier()
{
}

void PCAClassifier::AddFeature(int group, const Vector<float> &f)
{
	Vector<float> p=m_PCA.Project(f);
	AddFeatureToGroup(group, p);
}

int PCAClassifier::Classify(const Vector<float> &f, float &error)
{
	Vector<float> params=m_PCA.Project(f);

	// find the closest point in all the group means
	error = FLT_MAX;
	int ret=-1;
	for (map<int,Vector<float> >::iterator i=m_GroupMeans.begin(); 
		i!=m_GroupMeans.end(); ++i)
	{
		float d=params.DistanceFrom(i->second);
		if (d<error)
		{
			ret=i->first;
			error=d;
		}
	}
	
	return ret;
}

void PCAClassifier::Refresh()
{
	
}

