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

#include "PCA.h"
#include "SVD.h"
#include <iostream>

using namespace std;

PCA::PCA(unsigned int FeatureSize) :
m_FeatureSize(FeatureSize),
m_Mean(FeatureSize)
{
}

PCA::~PCA() 
{
}


void PCA::Calculate()
{
	// calculate the mean
	Vector<float> Mean(m_FeatureSize);
	Mean.Zero();
	for (FeatureVec::iterator vi = m_Features.begin(); vi!=m_Features.end(); ++vi)
	{
		Mean+=*vi;
	}
	
	Mean/=m_Features.size();
		
	// subtract the mean
	FeatureVec SubMean;
	for (FeatureVec::iterator vi = m_Features.begin(); vi!=m_Features.end(); ++vi)
	{
		SubMean.push_back(*vi-Mean);
	}
	
	// allocate the transform matrix (this is where it'll run out of memory)
	cerr<<"Allocating "<<m_FeatureSize*m_FeatureSize*sizeof(float)/1024/1024.0<<" megs for covariance matrix"<<endl;
	m_EigenTransform = Matrix<float>(m_FeatureSize,m_FeatureSize);
	m_EigenTransform.Zero();
	
	// start by calculating the covariance matrix 
	for (unsigned int i=0; i<m_FeatureSize; i++)
	{
		for (unsigned int j=0; j<m_FeatureSize; j++)
		{
			for (FeatureVec::iterator f = SubMean.begin(); f!=SubMean.end(); ++f)
			{
				m_EigenTransform[i][j]+=(*f)[i]*(*f)[j];
			}
			
			m_EigenTransform[i][j]/=(float)(SubMean.size()-1);
		}
	}
	m_EigenValues = SVD(m_EigenTransform);
}

void PCA::RunTests()
{
	PCA pca(2);
	
	Vector<float> in(2);
	in[0]=2.5; in[1]=2.4;
	pca.AddFeature(in);
	in[0]=0.5; in[1]=0.7;
	pca.AddFeature(in);
	in[0]=2.2; in[1]=2.9;
	pca.AddFeature(in);
	in[0]=1.9; in[1]=2.2;
	pca.AddFeature(in);
	in[0]=3.1; in[1]=3.0;
	pca.AddFeature(in);
	in[0]=2.3; in[1]=2.7;
	pca.AddFeature(in);
	in[0]=2; in[1]=1.6;
	pca.AddFeature(in);
	in[0]=1; in[1]=1.1;
	pca.AddFeature(in);
	in[0]=1.5; in[1]=1.6;
	pca.AddFeature(in);
	in[0]=1.1; in[1]=0.9;
	pca.AddFeature(in);
	
	pca.Calculate();
}



