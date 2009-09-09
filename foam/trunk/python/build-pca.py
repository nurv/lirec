#!/usr/bin/env python

import glob,string
from vision import *
from faces import *

#get_faces("../no-redist/yalefaces/orig/*.png", "../no-redist/yalefaces/processed/")
# calculate("../no-redist/spacek-large/*.png",20,30,"test.pca")

build_pca("../no-redist/yalefaces/processed/*.png",20,30,"yalefaces-expression-20x30.pca")
