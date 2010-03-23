// AUTHOR: Ginevra Castellano
// Queen Mary University of London
// DATE: 03/2010
// VERSION: 1.0

// Copyright (C) 2009 Ginevra Castellano
// Queen Mary University of London

// This file is part of the MotionDirectionDetection program

// MotionDirectionDetection is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// MotionDirectionDetection uses the OpenCV library
// Copyright (C) 2000-2006, Intel Corporation, all rights reserved.
// Third party copyrights are property of their respective owners.
// See OpenCV_license.txt, in the program folder, for details.



#include "MessageStorage.h"

#include <stdio.h>


MessageStorage::MessageStorage(int maxListSize)
{
	this->maxListSize = maxListSize;
}

MessageStorage::~MessageStorage(void)
{
	for (int i = 0; i < this->messageList.size(); i++)
	{
		delete this->messageList[i];
	}
}


void MessageStorage::addMessage(int messageCode, double timestamp)
{
	MessageInfo *mi = new MessageInfo;
	mi->messageCode = messageCode;
	mi->timeStamp = timestamp;
	messageList.push_back(mi);  // add a new element (message code and timestamp) to the list

	if (this->messageList.size() >= this->maxListSize) // list only stores N (maxListSize) elements
	{
		delete this->messageList[0];	//delete the MessageInfo instance in messageList[0]		
		this->messageList.erase(this->messageList.begin());	//delete the container at the beginning of the list
	}
}

MessageInfo* MessageStorage::GetMessage(void)

{
	return this->messageList[this->messageList.size()-1]; // get latest message
}
