#!/usr/bin/env python

import glob,string
from vision import *
from faces import *

if sys.argv[1] == "-h":
	print("build_pca inpath/*.png width height outfile")

build_pca(sys.argv[1],int(sys.argv[2]),int(sys.argv[3]),sys.argv[4])
