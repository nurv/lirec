#!/usr/bin/env python

import glob,string
from magicsquares import *
from faces import *
	
# just try projecting onto vectors that seem 'happy'

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def print_weight(filename,param,pca,w,h):
	i = Image(filename)
	v = i.Scale(w,h).RGB2GRAY().ToFloatVector()
	p = pca.Project(v)
	print(p[param])

pca = load_pca("../data/eigenspaces/yalefaces-30x50.pca")
w = 30
h = 50
filename = "../data/images/faces/dave/expression/normal.png"

print("normal=")
print_weight("../data/images/faces/dave/expression/normal.png",13,pca,w,h)
print("happy=")
print_weight("../data/images/faces/dave/expression/happy.png",13,pca,w,h)
print("angry=")
print_weight("../data/images/faces/dave/expression/angry.png",13,pca,w,h)
print("grimace=")
print_weight("../data/images/faces/dave/expression/grimace.png",13,pca,w,h)
