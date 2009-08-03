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
	m_Mean.Zero();
	for (FeatureVec::iterator vi = m_Features.begin(); vi!=m_Features.end(); ++vi)
	{
		m_Mean+=*vi;
	}
	
	m_Mean/=m_Features.size();
		
	// subtract the mean
	FeatureVec SubMean;
	for (FeatureVec::iterator vi = m_Features.begin(); vi!=m_Features.end(); ++vi)
	{
		SubMean.push_back(*vi-m_Mean);
	}
	
	// allocate the transform matrix (this is where it'll run out of memory)
	float size= m_FeatureSize*m_FeatureSize*sizeof(float)/1024/1024.0;
	if (size>1)
	{
		cerr<<"Allocating "<<size<<" megs for covariance matrix"<<endl;
	}
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
	m_EigenTransform=m_EigenTransform.Transposed();
}

void PCA::Compress(unsigned int s, unsigned int e)
{
	m_EigenTransform=m_EigenTransform.CropRows(s,e);
}

Vector<float> PCA::Project(Vector<float> v) const
{
	return m_EigenTransform*v;
}
	
Vector<float> PCA::Synth(Vector<float> v) const
{
	return m_Mean+m_EigenTransform.VecMulTransposed(v);
}
	
void PCA::Mult(const PCA &other)
{
	m_EigenTransform *= other.GetEigenTransform().Inverted();
}

void PCA::Save(FILE *f) const
{
	int version = 2;
	fwrite(&version,sizeof(version),1,f);
	m_EigenTransform.Save(f);
	m_EigenValues.Save(f);
	m_Mean.Save(f);
	fwrite(&m_FeatureSize,sizeof(m_FeatureSize),1,f);
	for (unsigned int i=0; i<m_Features.size(); i++)
	{
		m_Features[i].Save(f);
	}
	
}

void PCA::Load(FILE *f)
{
	int version;	
	fread(&version,sizeof(version),1,f);
	m_EigenTransform.Load(f);
	m_EigenValues.Load(f);
	
	if (version == 1)
	{
		m_EigenTransform=m_EigenTransform.Transposed();
	}
	
	if (version>1) 
	{
		m_Mean.Load(f);	
		fread(&m_FeatureSize,sizeof(m_FeatureSize),1,f);
		for (unsigned int i=0; i<m_Features.size(); i++)
		{
			m_Features[i].Load(f);
		}
	}
}

void PCA::RunTests()
{
	Matrix<float>::RunTests();
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
	
	
	in[0]=.69; in[1]=.49;
	Vector<float> out = pca.Project(in);
	assert(feq(out[0],-0.82797f) && feq(out[1],-0.175115f));

	PCA pcasub(2);
	
	in[0]=-0.677; in[1]=-0.735;
	pcasub.AddFeature(in);
	in[0]=0; in[1]=0;
	pcasub.AddFeature(in);
	in[0]=0.677; in[1]=0.735;
	pcasub.AddFeature(in);
	
	pcasub.Calculate();
	pca.Mult(pca);	
	
	
}



