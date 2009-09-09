#!/usr/bin/env python

import glob
import os

files = glob.glob("*")
for f in files:
	command = "mv "+f+" "+f+".gif"
	print command
	os.system(command)
