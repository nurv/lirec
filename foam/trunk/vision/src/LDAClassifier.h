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

#ifndef FOAM_LDA_CLASSIFIER
#define FOAM_LDA_CLASSIFIER

// A linear discriminant analysis classifier for arbitrary data sets

class LDAClassifier : public Classifier
{
public:
	LDAClassifier(unsigned int FeatureSize);
	~LDAClassifier();

	virtual int Classify(const Vector<float> &f);

private:

	void CalcGroupMeans();
	void CalcMeanCorrected();
	void CalcGroupCovariance();
	void CalcPooledCovariance();
	void CalcPriorProbablity();
	
	std::map<int,Vector<float> > m_GroupMean;
	std::map<int,Matrix<float> > m_MeanCorrectedGroups;
	std::map<int,Matrix<float> > m_GroupCovariance;

	//Matrix<T> m_PooledCovariance;
	//Vector<T> m_PriorProbability;

};

#endif
