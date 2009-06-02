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
	 
	void Print() const;
	void SetAll(T s);
	void Zero() { SetAll(0); }
	bool IsInf();
	Matrix Transposed();

	Matrix &operator=(const Matrix &other);
	Matrix operator+(const Matrix &other) const;
	Matrix operator-(const Matrix &other) const;
	Matrix operator*(const Matrix &other) const;
	Matrix &operator+=(const Matrix &other);
	Matrix &operator-=(const Matrix &other);
	Matrix &operator*=(const Matrix &other);
	
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
Matrix<T> Matrix<T>::Transposed()
{
	Matrix<T> copy(*this);
	for (unsigned int i=0; i<m_Rows; i++)
	{
		for (unsigned int j=0; j<m_Cols; j++)
		{
			copy[i][j]=(*this)[j][i];
		}
	}
	return copy;
}


template<class T>
Matrix<T> Matrix<T>::operator+(const Matrix &other) const
{
	assert(m_Rows=other.m_Rows);
	assert(m_Cols=other.m_Cols);
	
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
	assert(m_Rows=other.m_Rows);
	assert(m_Cols=other.m_Cols);
	
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
void Matrix<T>::RunTests()
{
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
}

#endif
