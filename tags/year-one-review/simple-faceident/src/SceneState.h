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

#include <iostream>
#include <map>
#include <yarp/os/all.h>

#ifndef SCENE_STATE
#define SCENE_STATE

/////////////////////////////////////////////////////////////////////////////////
// This class records who is present in the scene and keeps a record 
// from frame to frame in order to send changes over yarp

class SceneState
{
public:
	SceneState();
	~SceneState();

	// User information. Not much here yet, maybe more one day
	class User
	{
	public:
		User() : m_Confidence(0) {}
		User(float c) : m_Confidence(c) {}
		~User() {}
		
		float m_Confidence;
	};
	
	// Call this each frame with each detected user
	void AddPresent(unsigned int id, const User &u);
	
	// Call this once per frame to process and dispatch yarp bottles
	void Update();
	
private:

	void SendAppeared(unsigned int ID, const User &u);
	void SendDisappeared(unsigned int ID);

	yarp::os::Network m_YarpNetwork;
	yarp::os::Port m_YarpPort;

	std::map<unsigned int, User> m_CurrentFrame;
	std::map<unsigned int, User> m_LastFrame;
};

#endif
