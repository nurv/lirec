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

#ifndef FOAM_LDA_CLASSIFIER
#define FOAM_LDA_CLASSIFIER

// A linear discriminant analysis classifier for arbitrary data sets

template<class T>
class LDAClassifier
{
public:
	LDAClassifier(unsigned int FeatureSize);
	~LDAClassifier();

	class Feature
	{
	public:
		Vector<T> m_Data;
		int m_Class;
	};

	void AddFeature(const Feature &f);
	int Classify(const Feature &f);

private:

	void CalcClasses();
	void CalcGlobalMean();
	void CalcClassMeans();
	void CalcMeanCorrected();
	void CalcCovariance();
	void CalcPooledCovariance();
	void CalcPriorProbablity();

	unsigned int m_FeatureSize;
	std::vector<Feature > m_Features;
	
	std::set<int> m_Classes;
	Vector<T> m_GlobalMean;
	std::map<int,Vector<T> > m_ClassMeans;
	std::map<int,Matrix<T> > m_MeanCorrected;
	std::map<int,Matrix<T> > m_Covariance;
	Matrix<T> m_PooledCovariance;
	Vector<T> m_PriorProbability;

};

template<class T>
LDAClassifier<T>::LDAClassifier(unsigned int FeatureSize)
{
}

template<class T>
LDAClassifier<T>::~LDAClassifier()
{
}

template<class T>
void LDAClassifier<T>::AddFeature(const Feature &f) 
{ 
	m_Features.push_back(f); 
}

template<class T>
int LDAClassifier<T>::Classify(const Feature &f)
{
	return 0;
}

template<class T>
void LDAClassifier<T>::CalcClasses()
{
	m_Classes.clear();
	for (typename std::vector<Feature >::iterator i=m_Features.begin();
		i!=m_Features.end(); ++i)
	{
		m_Classes.insert(i->m_Class);
	}
}

template<class T>
void LDAClassifier<T>::CalcGlobalMean()
{

}

template<class T>
void LDAClassifier<T>::CalcClassMeans()
{
}

template<class T>
void LDAClassifier<T>::CalcMeanCorrected()
{
}

template<class T>
void LDAClassifier<T>::CalcCovariance()
{
}

template<class T>
void LDAClassifier<T>::CalcPooledCovariance()
{
}

template<class T>
void LDAClassifier<T>::CalcPriorProbablity()
{
}

#endif
