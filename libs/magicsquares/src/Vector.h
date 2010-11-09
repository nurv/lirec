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
#include <stdio.h>
#include <memory.h>

#ifndef FOAM_VECTOR
#define FOAM_VECTOR

template<class T>
bool feq(T a, T b, T t=0.001)
{
	return fabs(a-b)<t;
}

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
	T Mean();
	T DistanceFrom(const Vector &other) const;
	T Magnitude() const;
	T Dot(const Vector<T> &other);
	Vector Normalised() const;

	Vector &operator=(const Vector &other);
	Vector operator+(const Vector &other) const;
	Vector operator-(const Vector &other) const;
	Vector operator+(T v) const;
	Vector operator-(T v) const;
	Vector operator*(T v) const;
	Vector operator/(T v) const;
	Vector &operator+=(const Vector &other);
	Vector &operator-=(const Vector &other);
	Vector &operator+=(T v);
	Vector &operator-=(T v);
	Vector &operator*=(T v);
	Vector &operator/=(T v);
	
	void Save(FILE *f) const;
	void Load(FILE *f);
	
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
T Vector<T>::DistanceFrom(const Vector &other) const
{
	assert(m_Size==other.m_Size);
	
	float acc=0;
	for (unsigned int i=0; i<m_Size; i++)
	{
		acc+=(other[i]-(*this)[i]) * (other[i]-(*this)[i]);
	}
	
	return sqrt(acc);
}

template<class T>
T Vector<T>::Magnitude() const
{
	float acc=0;
	for (unsigned int i=0; i<m_Size; i++)
	{
		acc+=(*this)[i] * (*this)[i];
	}
	
	return sqrt(acc);
}

template<class T>
T Vector<T>::Dot(const Vector<T> &other)
{
	assert(m_Size==other.m_Size);
	T acc=0;
	for (unsigned int i=0; i<m_Size; i++)
	{
		acc+=(*this)[i]*other[i];
	}
	return acc;
}

template<class T>
Vector<T> Vector<T>::Normalised() const
{
	Vector<T> ret(*this);
	ret/=ret.Magnitude();
	return ret;
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
Vector<T> Vector<T>::operator+(T v) const
{	
	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]+v;
	}
	return ret;
}

template<class T>
Vector<T> Vector<T>::operator-(T v) const
{
	Vector<T> ret(m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		ret[i]=(*this)[i]-v;
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
	assert(m_Size==other.m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]+=other[i];
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator-=(const Vector &other)
{
	assert(m_Size==other.m_Size);
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]-=other[i];
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator+=(T v)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]+=v;
	}
	return *this;
}

template<class T>
Vector<T> &Vector<T>::operator-=(T v)
{
	for (unsigned int i=0; i<m_Size; i++)
	{
		(*this)[i]-=v;
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
T Vector<T>::Mean()
{
	T acc=0;
	for (unsigned int i=0; i<m_Size; i++)
	{
		acc+=(*this)[i];
	}
	return acc/(float)m_Size;
}

template<class T>
void Vector<T>::Save(FILE* f) const
{
	int version = 1;	
	fwrite(&version,sizeof(version),1,f);
	fwrite(&m_Size,sizeof(m_Size),1,f);
	fwrite(m_Data,sizeof(T)*m_Size,1,f);
}

template<class T>
void Vector<T>::Load(FILE* f)
{
	int version;	
	fread(&version,sizeof(version),1,f);
	fread(&m_Size,sizeof(m_Size),1,f);
	m_Data=new T[m_Size];
	fread(m_Data,sizeof(T)*m_Size,1,f);	
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
	
	assert(feq(m.Magnitude(),0.5f));
	Vector<T> a(10);
	a.Zero();
	a[5]=-10;
	assert(feq(a.DistanceFrom(m),10.5f));
}

#endif
