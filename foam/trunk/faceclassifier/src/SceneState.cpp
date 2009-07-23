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

#include "SceneState.h"

using namespace std;
using namespace yarp::os;

SceneState::SceneState()
{
	cerr<<"connecting to yarp..."<<endl;
	m_YarpPort.open("/faceident");
}

SceneState::~SceneState()
{
}
	
void SceneState::Update()
{
	// for each person present this frame
	for (map<unsigned int, User>::iterator i=m_CurrentFrame.begin(); 
		i!=m_CurrentFrame.end(); i++)
	{
		// look for in the last frame
		map<unsigned int, User>::iterator l=m_LastFrame.find(i->first);
		if (l==m_LastFrame.end())
		{
			// new person!
			SendAppeared(i->first, i->second);
		}
	}
	
	// for each person present last frame
	for (map<unsigned int, User>::iterator i=m_LastFrame.begin(); 
		i!=m_LastFrame.end(); i++)
	{
		// look for in the current frame
		map<unsigned int, User>::iterator l=m_CurrentFrame.find(i->first);
		if (l==m_CurrentFrame.end())
		{
			// disappeared!
			SendDisappeared(i->first);
		}
	}
	
	m_LastFrame=m_CurrentFrame;
	m_CurrentFrame.clear();
}

void SceneState::SendAppeared(unsigned int ID, const User &u)
{
cerr<<"SendAppeared "<<ID<<endl;
	Bottle b;  
	b.clear();
	b.add("user appeared");
	b.add((int)ID);
	b.add(u.m_Confidence);
	m_YarpPort.write(b);
}

void SceneState::SendDisappeared(unsigned int ID)
{
cerr<<"SendDisappeared "<<ID<<endl;
	Bottle b;  
	b.clear();
	b.add("user disappeared");
	b.add((int)ID);
	m_YarpPort.write(b);
}

void SceneState::AddPresent(unsigned int id, const User &u)
{
	m_CurrentFrame[id]=u;
}
