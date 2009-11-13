# faces.py
# bunch of scripts for doing things with faces

import glob,string
import numpy as np
import scipy.misc.pilutil as smp
from magicsquares import *

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

def filename_from_path(path):
	return path[string.rfind(path,"/")+1:]

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# returns a vector with the extra parameter appended

def add_parameter(v,p):
	vp = FloatVector(v.Size()+1)
	for i in range(0,v.Size()):
		vp[i]=v[i]
	vp[v.Size()]=p
	return vp

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# builds a eigenmatrix from all the images and saves the pca file

def build_pca(imagepath,w,h,filename):
	images = glob.glob(imagepath)
	pca = PCA(w*h)
	
	for image in images:
		im = Image(image)
		v = im.Scale(w,h).RGB2GRAY().ToFloatVector()
		pca.AddFeature(v)
	
	pca.Calculate()
	
	f = OpenFile(filename, "wb")
	pca.Save(f)
	CloseFile(f)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# do facefinding on all the images and save the cropped faces

def get_faces(imagepath,outpath):
	images = glob.glob(imagepath)
	finder = FaceFinder()
	for image in images:
		i = Image(image)
		rects = finder.Find(i,False)
		for rect in rects:
			face = i.SubImage(rect)			
			face.Save(outpath+filename_from_path(image))

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# attempts to build an expression eigenspace with emotion 
# parameter tacked on (based on the image filename)

def build_expression_pca(imagepath,w,h,filename):
	images = glob.glob(imagepath)
	
	pca = PCA(w*h+1)
	
	for imagename in images:
		emotion=0
		if "happy" in imagename:
			emotion=1
		if "sad" in imagename:
			emotion=-1
		print(imagename+" is "+str(emotion))
	
		im = Image(imagename)
		v = im.Scale(w,h).RGB2GRAY().ToFloatVector()
		pca.AddFeature(add_parameter(v,emotion))
	
	pca.Calculate()
	
	f = OpenFile(filename, "wb")
	pca.Save(f)
	CloseFile(f)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# takes an image and attempts to calculate it's extra parameter

def emotion_from_expression(filename,w,h,pca):
	i = Image(filename)
	v = i.Scale(w,h).RGB2GRAY().ToFloatVector()
	p = pca.Project(add_parameter(v,0))
	s = pca.Synth(p)
	return s[w*h]>0;
	
def load_pca(filename):
	pca = PCA(1)	
	f = OpenFile(filename, "rb")
	pca.Load(f)
	CloseFile(f)
	return pca
	
def vec2npimg(w,h,v):
	c=0
	image = np.zeros( (h,w,3), dtype=np.uint8 )
	for iy in range(0,h):
		for ix in range(0,w):
			i = int(max(min(256*v[c],256),0))
			image[iy,ix] = [i,i,i] 
			c=c+1
	return image
	
def plot_eigenface(image,x,y,w,h,pca,row,gain):
	eigenface = pca.GetEigenTransform().GetRowVector(row)*gain+pca.GetMean()
	c=0
	for iy in range(0,h):
		for ix in range(0,w):
			v = int(max(min(256*eigenface[c],256),0))
			image[iy+y,ix+x] = [v,v,v] 
			c=c+1

def make_eigenfaces_image(w,h,pca,start,end,gain):
	num_imagesx = end-start;	
	image = np.zeros( (h,w*num_imagesx,3), dtype=np.uint8 )
	for i in range(start, end):
		c=i-start
		plot_eigenface(image,w*c,0,w,h,pca,i,gain)
	return image
