%module magicsquares
%{
#include "PCA.h"
#include "Classifier.h"
#include "LDAClassifier.h"
#include "Matrix.h"       
#include "Vector.h"
#include "Image.h"
#include "PCAClassifier.h"
#include "SVD.h"
#include "FileTools.h"
#include "Geometry.h"
#include "FaceFinder.h"
%}

%include "stl.i"
%include "typemaps.i"
%include "std_vector.i"
%include "PCA.h"
%include "Classifier.h"
%include "LDAClassifier.h"
%include "Matrix.h"  	 
%include "Vector.h"
%include "Image.h"
%include "PCAClassifier.h"
%include "SVD.h"
%include "FileTools.h"
%include "Geometry.h"
%include "FaceFinder.h"

%template(FloatVector) Vector<float>;
%template(FloatMatrix) Matrix<float>;

namespace std
{
  %template(RectVector) vector<Rect>;
}

%extend Vector<float> {
	float __getitem__(long int i)
	{
		return (*self)[i];
	}
	void __setitem__(long int i, float s)
	{
		(*self)[i]=s;
	}
}

%extend Matrix<float> {
	float Get(long int r, long int c)
	{
		return (*self)[r][c];
	}
	void Set(long int r, long int c, float s)
	{
		(*self)[r][c]=s;
	}
}


