#!/usr/bin/env python

import glob,string
from magicsquares import *
from faces import *
	
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

def get_params(filename,pca,w,h):
	i = Image(filename)
	v = i.Scale(w,h).RGB2GRAY().ToFloatVector()
	p = pca.Project(v)
	return str(p[0])+" "+str(p[1])+" "+str(p[2])

def plot_all(path, w, h):
	images = glob.glob(path)
	ret = ""
	for image in images:
		ret=ret+get_params(image,pca,w,h)+"\n"
	return ret
	
def plot_individual(n):
	f = open("individual"+str(n),"w")
	f.write(plot_all("../data/benchmark/yale/training/"+str(n)+"/*.jpg",w,h))
	f.close()

def plot_test_individual(n):
	f = open("individual"+str(n),"w")
	f.write(plot_all("../data/benchmark/yale/test/yaleB0"+str(n)+"*.jpg",w,h))
	f.close()

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

pca = load_pca("../data/eigenspaces/spacek-20x30.pca")
w = 20
h = 30
pca.Compress(36,40)

plot_test_individual(0)
plot_test_individual(1)
plot_test_individual(2)
plot_test_individual(3)
plot_test_individual(4)
plot_test_individual(5)

