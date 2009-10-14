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

#ifndef FOAM_GEOMETRY
#define FOAM_GEOMETRY

class Rect
{
public:
	Rect(float X, float Y, float W, float H): x(X), y(Y), w(W), h(H) {}
	Rect(): x(0), y(0), w(0), h(0) {}
	~Rect() {}
	
	float x;
	float y;
	float w;
	float h;
};

#endif
