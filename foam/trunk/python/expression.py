#!/usr/bin/env python

import glob,string
from vision import *
from faces import *
	
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#build_expression_pca("../no-redist/yalefaces/processed/*.png",20,30,"expr.pca")

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

pca = PCA(1)	
f = OpenFile("expr.pca", "rb")
pca.Load(f)
CloseFile(f)

emotion_from_expression("../data/images/faces/dave/expression/normal.png",20,30,pca)
