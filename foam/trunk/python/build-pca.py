#!/usr/bin/env python

import glob
from vision import *

imagepath = "../no-redist/spacek-large/*.png"
images = glob.glob(imagepath)
w = 20
h = 30
pca = PCA(w*h)

for image in images:
	im = Image(image)
	v = im.Scale(w,h).RGB2GRAY().ToFloatVector()
	pca.AddFeature(v)

pca.Calculate()

f = OpenFile("test.pca", "wb")
pca.Save(f)
CloseFile(f)
