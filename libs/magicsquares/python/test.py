#!/usr/bin/env python

from magicsquares import *

v2 = Image(10,10,8,3)

v = FloatVector(100)
v[10]=99
print(v[10])

m = FloatMatrix(10,10)
m.Set(4,4,999)
print(m.Get(4,4))
