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
#include <math.h>
#include <iostream>

#ifndef FOAM_VECTOR
#define FOAM_VECTOR

template<class T>
class Vector
{
public:
	Vector();
	Vector(unsigned int s);
	~Vector();
	Vector(const Vector &other);
	
	T &operator[](unsigned int i) 
	{
		assert(i<m_Size);
		return m_Data[i];
	}

	const T &operator[](unsigned int i) const
	{
		assert(i<m_Size);
		return m_Data[i];
	}
 
	unsigned int Size() const { return m_Size; }
	T *GetRawData() { return m_Data; }
	const T *GetRawDataConst() const { return m_Data; }
	 
	void Print() const;
	void SetAll(T s);
	void Zero() { SetAll(0); }
	bool IsInf();

	Vector &operator=(const Vector &other);
	Vector operator+(const Vector &other) const;
	Vector operator-(const Vector &other) const;
	Vector operator*(T v) const;
	Vector operator/(T v) const;
	Vector &operator+=(const Vector &other);
	Vector &operator-=(const Vector &other);
	Vector &operator*=(T v);
	Vector &operator/=(T v);
	
	static void RunTests();
	
private:

	unsigned int m_Size;
	T *m_Data;
};

template<class T>
Vector<T>::Vector() :
m_Size(0)
{
	m_Data=NULL;
}

template<class T>
Vector<T>::Vector(unsigned int s) :
m_Size(s)
{
	m_Data=new T[s];
}

template<class T>
Vector<T>::~Vector()
{
	delete[] m_Data;
}

template<class T>
Vector<T>::Vector(const Vector &other)
{
	m_Size = other.m_Size;
	m_Data=new T[m_Size];
	memcpy(m_Data,other.m_Data,m_Size*sizeof(T));
}

template<class T>
Vector<T> &Vector<T>::operator=(const Vector &other)
{
	if (m_Data!=NULL)
	{
		delete[] m_Data;
	}
	
	m_Size = other.m_Size;
	m_Data=new T[m_Size];
	memcpy(m_Data,other.m_Data,m_Size*sizeof(T));
	
	return *this;
}

template<class T>
void Vector<T>::Print() const
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		std::cerr<<(*this)[i]<<" ";
	}
	std::cerr<<std::endl;
}

template<class T>
void Vector<T>::SetAll(T s)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]=s;
	}
}

template<class T>
bool Vector<T>::IsInf()
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		if (isinf((*this)[i])) return true;
		if (isnan((*this)[i])) return true;
	}
	return false;
}

template<class T>
Vector<T> Vector<T>::operator+(const Vector &other) const
{
	assert(m_Size==other.m_Size);
	
	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]+other[i];
	}
	return ret;
}

template<class T>
Vector<T> Vector<T>::operator-(const Vector &other) const
{
	assert(m_Size==other.m_Size);

	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]-other[i];
	}
	return ret;
}

template<class T>
Vector<T> Vector<T>::operator*(T v) const
{	
	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]*v;
	}
	return ret;
}

template<class T>
Vector<T> Vector<T>::operator/(T v) const
{	
	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]/v;
	}
	return ret;
}

template<class T>
Vector<T> &Vector<T>::operator+=(const Vector &other)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]+=other[i];
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator-=(const Vector &other)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]-=other[i];
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator*=(T v)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]*=v;
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator/=(T v)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]/=v;
	}
	return *this;
}


template<class T>
void Vector<T>::RunTests()
{
	Vector<T> m(10);
	m.SetAll(0);
	assert(m[0]==0);
	m[5]=0.5;
	assert(m[5]==0.5);
	Vector<T> om(m);
	assert(om[5]==0.5);
}

#endif
