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

#include "cv.h"
#include "highgui.h"
#include <yarp/os/all.h>
#include <string>

#include "FaceBank.h"
#include "SceneState.h"

using namespace yarp::os;
using namespace std;

class App
{
public:
	App(const string &filename);
	~App();
	
	void Update();
	
private:
	
	CvCapture* m_Capture;
	CvHaarClassifierCascade* m_Cascade;
	CvMemStorage* m_Storage;
	
	Classifier *m_Classifier;
	FaceBank *m_FaceBank;
	SceneState m_SceneState;
	BufferedPort<Bottle> m_CtrlPort; 
	
	int m_FaceNum;
	bool m_Learn; 
	CvFont m_Font; 
	
	IplImage *frame;
	IplImage *frame_copy;
	
	int m_FrameNum;
};
