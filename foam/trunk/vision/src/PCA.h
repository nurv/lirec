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
#include "Classifier.h"

#ifndef FOAM_PCA
#define FOAM_PCA

// Principle component analysis

class PCA 
{
public:
	PCA(unsigned int FeatureSize);
	~PCA();
	
	typedef std::vector<Vector<float> > FeatureVec;

	void AddFeature(Vector<float> v) { m_Features.push_back(v); }
	void Calculate();
	
	// remove eigenvectors from the transform
	void Compress(unsigned int s, unsigned int e);

	void Mult(const PCA &other);
	
	Vector<float> Project(Vector<float> v) const;
	Vector<float> Synth(Vector<float> v) const;

	static void RunTests();

	const Vector<float> &GetEigenValues() const { return m_EigenValues; }
	const Matrix<float> &GetEigenTransform() const { return m_EigenTransform; }
	Matrix<float> &EigenTransform() { return m_EigenTransform; }
	const FeatureVec &GetFeatures() const { return m_Features; }
	const Vector<float> &GetMean() const { return m_Mean; }
	unsigned int GetFeatureSize() const { return m_FeatureSize; }
	unsigned int GetParamsSize() const { return m_EigenTransform.GetRows(); }
	
	
	void Load(FILE *f);
	void Save(FILE *f) const;
	
private:	

	void CalcMean();


	FeatureVec m_Features;
	unsigned int m_FeatureSize;
	Vector<float> m_Mean;
	
	Vector<float> m_EigenValues;
	Matrix<float> m_EigenTransform;
	
};

#endif
