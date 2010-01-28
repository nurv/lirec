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

#include <assert.h>
#include <iostream>
#include "Vector.h"

#ifndef FOAM_MATRIX
#define FOAM_MATRIX

template<class T> 
class Matrix
{
public:
	Matrix();
	Matrix(unsigned int r, unsigned int c);
	~Matrix();
	Matrix(const Matrix &other);
	Matrix(unsigned int r, unsigned int c, float *data);

	// Row proxy classes to allow matrix[r][c] notation
	class Row
	{
	public:
		Row(Matrix *owner, unsigned int r)
		{
			m_Data=&owner->GetRawData()[r*owner->GetCols()];
			m_Cols=owner->GetCols();
		}
		
		T &operator[](unsigned int c)
		{
			assert(c<m_Cols);
			return m_Data[c];
		}
		
	private:
		T *m_Data;
		unsigned int m_Cols;
	};

	class ConstRow
	{
	public:
		ConstRow(const Matrix *owner, unsigned int r)
		{
			m_Data=&owner->GetRawDataConst()[r*owner->GetCols()];
			m_Cols=owner->GetCols();
		}
		
		const T &operator[](unsigned int c) const
		{
			assert(c<m_Cols);
			return m_Data[c];
		}
		
	private:
		const T *m_Data;
		unsigned int m_Cols;
	};

	
	Row operator[](unsigned int r) 
	{
		assert(r<m_Rows);
		return Row(this,r);
	}

	ConstRow operator[](unsigned int r) const
	{
		assert(r<m_Rows);
		return ConstRow(this,r);
	}
 
	unsigned int GetRows() const { return m_Rows; }
	unsigned int GetCols() const { return m_Cols; }
	T *GetRawData() { return m_Data; }
	const T *GetRawDataConst() const { return m_Data; }
	Vector<T> GetRowVector(unsigned int r) const; 
	Vector<T> GetColVector(unsigned int c) const; 
	void SetRowVector(unsigned int r, const Vector<T> &row);
	void SetColVector(unsigned int c, const Vector<T> &col);
	void NormaliseRows();
	void NormaliseCols();

	void Print() const;
	void SetAll(T s);
	void Zero() { SetAll(0); }
	bool IsInf();
	Matrix Transposed() const;
	Matrix Inverted() const;

	Matrix &operator=(const Matrix &other);
	Matrix operator+(const Matrix &other) const;
	Matrix operator-(const Matrix &other) const;
	Matrix operator*(const Matrix &other) const;
	Vector<T> operator*(const Vector<T> &other) const;
	Vector<T> VecMulTransposed(const Vector<T> &other) const;
	Matrix &operator+=(const Matrix &other);
	Matrix &operator-=(const Matrix &other);
	Matrix &operator*=(const Matrix &other);
	bool operator==(const Matrix &other) const;

	void SortRows(Vector<T> &v);
	void SortCols(Vector<T> &v);
	
	Matrix CropRows(unsigned int s, unsigned int e);
	Matrix CropCols(unsigned int s, unsigned int e);

	void Save(FILE *f) const;
	void Load(FILE *f);

	static void RunTests();
	
private:

	unsigned int m_Rows;
	unsigned int m_Cols;
	
	T *m_Data;
	
};

template<class T>
Matrix<T>::Matrix(unsigned int r, unsigned int c) :
m_Rows(r),
m_Cols(c)
{
	m_Data=new T[r*c];
}

template<class T>
Matrix<T>::Matrix() :
m_Rows(0),
m_Cols(0),
m_Data(NULL)
{
}

template<class T>
Matrix<T>::Matrix(unsigned int r, unsigned int c, float *data) :
m_Rows(r),
m_Cols(c),
m_Data(data)
{
}

template<class T>
Matrix<T>::~Matrix()
{
	delete[] m_Data;
}

template<class T>
Matrix<T>::Matrix(const Matrix &other)
{
	m_Rows = other.m_Rows;
	m_Cols = other.m_Cols;
	m_Data=new T[m_Rows*m_Cols];
	memcpy(m_Data,other.m_Data,m_Rows*m_Cols*sizeof(T));
}

template<class T>
Matrix<T> &Matrix<T>::operator=(const Matrix &other)
{
	if (m_Data!=NULL)
	{
		delete[] m_Data;
	}
	
	m_Rows = other.m_Rows;
	m_Cols = other.m_Cols;
	m_Data=new T[m_Rows*m_Cols];
	memcpy(m_Data,other.m_Data,m_Rows*m_Cols*sizeof(T));
	
	return *this;
}

template<class T>
void Matrix<T>::Print() const
{
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			std::cerr<<(*this)[i][j]<<" ";
		}
		std::cerr<<std::endl;
	}
}

template<class T>
void Matrix<T>::SetAll(T s)
{
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			(*this)[i][j]=s;
		}
	}
}

template<class T>
bool Matrix<T>::IsInf()
{
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			if (isinf((*this)[i][j])) return true;
			if (isnan((*this)[i][j])) return true;
		}
	}
	return false;
}

template<class T>
Matrix<T> Matrix<T>::Transposed() const
{
	Matrix<T> copy(m_Cols,m_Rows);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			copy[j][i]=(*this)[i][j];
		}
	}
	return copy;
}

void matrix_inverse(const float *Min, float *Mout, int actualsize);

template<class T>
Matrix<T> Matrix<T>::Inverted() const
{
	assert(m_Rows==m_Cols); // only works for square matrices
	Matrix<T> ret(m_Rows,m_Cols);
	matrix_inverse(GetRawDataConst(),ret.GetRawData(),m_Rows);
	return ret;
}

template<class T>
Matrix<T> Matrix<T>::operator+(const Matrix &other) const
{
	assert(m_Rows==other.m_Rows);
	assert(m_Cols==other.m_Cols);
	
	Matrix<T> ret(m_Rows,m_Cols);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			ret[i][j]=(*this)[i][j]+other[i][j];
		}
	}
	return ret;
}

template<class T>
Matrix<T> Matrix<T>::operator-(const Matrix &other) const
{
	assert(m_Rows==other.m_Rows);
	assert(m_Cols==other.m_Cols);
	
	Matrix<T> ret(m_Rows,m_Cols);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			ret[i][j]=(*this)[i][j]-other[i][j];
		}
	}
	return ret;
}

template<class T>
Matrix<T> Matrix<T>::operator*(const Matrix &other) const
{
	assert(m_Cols==other.m_Rows);
	
	Matrix<T> ret(m_Rows,other.m_Cols);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<other.m_Cols; j++)
		{
            ret[i][j]=0;
			for (unsigned int k=0; k<m_Cols; k++)
			{
				ret[i][j]+=(*this)[i][k]*other[k][j];
			}
		}
	}
	return ret;
}

template<class T>
Vector<T> Matrix<T>::operator*(const Vector<T> &other) const
{
	assert(m_Cols==other.Size());
	
	Vector<T> ret(m_Rows);
	
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<other.Size(); j++)
		{
			ret[i]=0;
			for (unsigned int k=0; k<m_Cols; k++)
			{
				ret[i]+=(*this)[i][k]*other[k];
			}
		}
	}
	return ret;
}

template<class T>
Vector<T> Matrix<T>::VecMulTransposed(const Vector<T> &other) const
{
	assert(m_Rows==other.Size());
	
	Vector<T> ret(m_Cols);
	
	for (unsigned int i=0; i<m_Cols; i++)
	{
		for (unsigned int j=0; j<other.Size(); j++)
		{
			ret[i]=0;
			for (unsigned int k=0; k<m_Rows; k++)
			{
				ret[i]+=(*this)[k][i]*other[k];
			}
		}
	}
	return ret;
}

template<class T>
Matrix<T> &Matrix<T>::operator+=(const Matrix &other)
{
	(*this)=(*this)+other;
	return *this;
}

template<class T>
Matrix<T> &Matrix<T>::operator-=(const Matrix &other)
{
	(*this)=(*this)-other;
	return *this;
}

template<class T>
Matrix<T> &Matrix<T>::operator*=(const Matrix &other)
{
	(*this)=(*this)*other;
	return *this;
}

template<class T>
bool Matrix<T>::operator==(const Matrix &other) const
{
	if (m_Rows != other.m_Rows || 
		m_Cols != other.m_Cols)
	{
		return false;
	}
	
	for (unsigned int i=0; i<m_Cols; i++)
	{
		for (unsigned int j=0; j<m_Rows; j++)
		{
			if (!feq((*this)[i][j],other[i][j])) return false;
		}
	}

	return true;
}

//todo: use memcpy for these 4 functions
template<class T>
Vector<T> Matrix<T>::GetRowVector(unsigned int r) const
{
	assert(r<m_Rows);
	Vector<T> ret(m_Cols);
	for (unsigned int j=0; j<m_Cols; j++)
	{
		ret[j]=(*this)[r][j];
	}
	return ret;
}

template<class T>
Vector<T> Matrix<T>::GetColVector(unsigned int c) const
{
	assert(c<m_Cols);
	Vector<T> ret(m_Rows);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		ret[i]=(*this)[i][c];
	}
	return ret;
}

template<class T>
void Matrix<T>::SetRowVector(unsigned int r, const Vector<T> &row)
{
	assert(r<m_Rows);
	assert(row.Size()==m_Cols);
	for (unsigned int j=0; j<m_Cols; j++)
	{
		(*this)[r][j]=row[j];
	}
}

template<class T>
void Matrix<T>::SetColVector(unsigned int c, const Vector<T> &col)
{
	assert(c<m_Cols);
	assert(col.Size()==m_Rows);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		(*this)[i][c]=col[i];
	}
}

// sort rows by v
template<class T>
void Matrix<T>::SortRows(Vector<T> &v)
{
	assert(v.Size()==m_Rows);
	
	bool sorted=false;
	while(!sorted)
	{
		sorted=true;
		
		for (unsigned int i=0; i<v.Size()-1; i++)
		{
			if (v[i]<v[i+1])
			{
				sorted=false;
				float vtmp = v[i];
				v[i]=v[i+1];
				v[i+1]=vtmp;
				
				Vector<float> rtmp = GetRowVector(i);
				SetRowVector(i,GetRowVector(i+1));
				SetRowVector(i+1,rtmp);
			}
		}
	}
}

// sort cols by v
template<class T>
void Matrix<T>::SortCols(Vector<T> &v)
{
	assert(v.Size()==m_Cols);
	
	bool sorted=false;
	while(!sorted)
	{
		sorted=true;
		
		for (unsigned int i=0; i<v.Size()-1; i++)
		{
			if (v[i]<v[i+1])
			{
				sorted=false;
				float vtmp = v[i];
				v[i]=v[i+1];
				v[i+1]=vtmp;
				
				Vector<float> rtmp = GetColVector(i);
				SetColVector(i,GetColVector(i+1));
				SetColVector(i+1,rtmp);
			}
		}
	}
}
	
template<class T>
Matrix<T> Matrix<T>::CropRows(unsigned int s, unsigned int e)
{
	assert(s<e);
	assert(s<m_Rows);
	assert(e<=m_Rows);
	
	Matrix r(e-s,m_Cols);
	unsigned int c=0;
	for(unsigned int i=s; i<e; i++)
	{
		r.SetRowVector(c,GetRowVector(i));
		c++;
	}
	
	return r;
}

template<class T>
Matrix<T> Matrix<T>::CropCols(unsigned int s, unsigned int e)
{
	assert(s<e);
	assert(s<m_Cols);
	assert(e<=m_Cols);
	
	Matrix r(m_Rows,e-s);
	unsigned int c=0;
	for(unsigned int i=s; i<e; i++)
	{
		r.SetColVector(c,GetColVector(i));
		c++;
	}
	
	return r;
}

template<class T>
void Matrix<T>::NormaliseRows()
{
	for(unsigned int i=0; i<m_Rows; i++)
	{
		SetRowVector(i,GetRowVector(i).Normalised());
	}
}

template<class T>
void Matrix<T>::NormaliseCols()
{
	for(unsigned int i=0; i<m_Cols; i++)
	{
		SetColVector(i,GetColVector(i).Normalised());
	}
}

template<class T>
void Matrix<T>::Save(FILE* f) const
{
	int version = 1;	
	fwrite(&version,1,sizeof(version),f);
	fwrite(&m_Rows,1,sizeof(m_Rows),f);
	fwrite(&m_Cols,1,sizeof(m_Cols),f);
	fwrite(m_Data,1,sizeof(T)*m_Rows*m_Cols,f);
}

template<class T>
void Matrix<T>::Load(FILE* f)
{
	int version;	
	fread(&version,sizeof(version),1,f);
	fread(&m_Rows,sizeof(m_Rows),1,f);
	fread(&m_Cols,sizeof(m_Cols),1,f);
	m_Data=new T[m_Rows*m_Cols];
	fread(m_Data,sizeof(T)*m_Rows*m_Cols,1,f);
}

template<class T>
void Matrix<T>::RunTests()
{
	Vector<T>::RunTests();

    std::cerr<<"running matrix tests"<<std::endl;

	Matrix<T> m(10,10);
	m.SetAll(0);
	assert(m[0][0]==0);
	m[5][2]=0.5;
	assert(m[5][2]==0.5);
	Matrix<T> om(m);
	assert(om[5][2]==0.5);
	Matrix<T> a(2,3);
	a[0][0]=1; a[0][1]=2; a[0][2]=3;
	a[1][0]=4; a[1][1]=5; a[1][2]=6;
	Matrix<T> b(3,1);
	b[0][0]=3;
	b[1][0]=1;
	b[2][0]=2;
	Matrix<T> c=a*b;
	assert(c[0][0]==11 && c[1][0]==29);
	
	// test matrix * vector
	Vector<T> d(3);
	d[0]=3;
	d[1]=1;
	d[2]=2;
	Vector<T> e=a*d;
	assert(e[0]==11 && e[1]==29);
	
	Matrix<T> f=a.CropCols(1,3);
	assert(f.GetRows()==2 && f.GetCols()==2 && f[0][0]==2);
	Matrix<T> g=a.CropRows(0,1);
	assert(g.GetRows()==1 && g.GetCols()==3 && g[0][0]==1);
	
	// test matrix invert
	Matrix<T> h(3,3);
	h.Zero();
	h[0][0]=1;
	h[1][1]=1;
	h[2][2]=1;
	Matrix<T> i=h.Inverted();
	i==h;
	
	// some transforms from fluxus
	Matrix<T> j(4,4);	
	j[0][0]=1.0;			
	j[0][1]=0.0 ;				
	j[0][2]=0.0; 				
	j[0][3]=0.0; 				
	
	j[1][0]=0.0 			;	
	j[1][1]=0.7071067690849304 ; 
	j[1][2]=0.7071067690849304 ; 
	j[1][3]=0.0 				;
	
	j[2][0]=0.0 				;
	j[2][1]=-0.7071067690849304 ;
	j[2][2]=0.7071067690849304  ;
	j[2][3]=0.0 				;
	
	j[3][0]=1.0 				;
	j[3][1]=2.0 				;
	j[3][2]=3.0 				;
	j[3][3]=1.0 				;

	Matrix<T> k(4,4);
	k[0][0]=1.0 				 ;
	k[0][1]=0.0 				 ;
	k[0][2]=0.0 				 ;
	k[0][3]=0.0 				 ;

	k[1][0]=0.0 				 ;
	k[1][1]=0.7071068286895752   ;
	k[1][2]=-0.7071068286895752  ;
	k[1][3]=0.0 				 ;

	k[2][0]=0.0 				 ;
	k[2][1]=0.7071068286895752   ;
	k[2][2]=0.7071068286895752   ;
	k[2][3]=0.0 				 ;

	k[3][0]=-0.9999999403953552  ;
	k[3][1]=-3.535533905029297   ;
	k[3][2]=-0.7071067690849304  ;
	k[3][3]=0.9999999403953552	 ;
	
	assert(j.Inverted()==k);
		
	Matrix<float> l(2,2);
	l[0][0]=3;
	l[0][1]=3;
	l[1][0]=0;
	l[1][1]=0;
	
	Matrix<float> n(2,2);
	n[0][0]=2;
	n[0][1]=2;
	n[1][0]=0;
	n[1][1]=0;
	
	n*=l;
	
	Matrix<float> o(4,4);
	o.Zero();
	o[0][0]=1;
	o[1][1]=1;
	o[2][2]=1;
	o[3][3]=1;
	
	j*=k;
	assert(j==o);

	{
		Matrix<float> a(2,3);
		Matrix<float> b(3,2);
		
		a[0][0]=1; a[0][1]=2; a[0][2]=3;
		a[1][0]=4; a[1][1]=5; a[1][2]=6;

		b[0][0]=2; b[0][1]=3;
		b[1][0]=-1; b[1][1]=1;
		b[2][0]=1; b[2][1]=2;
		
		Matrix<float> result(2,2);
		result[0][0]=3; result[0][1]=11;
		result[1][0]=9; result[1][1]=29;
		
		assert(a*b==result);
	}
	
}

#endif
