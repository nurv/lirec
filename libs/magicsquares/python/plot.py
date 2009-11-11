import numpy as np
import scipy.misc.pilutil as smp

# Create a 1024x1024x3 array of 8 bit unsigned integers
data = np.zeros( (1024,1024,3), dtype=np.uint8 )

data[512,512] = [254,0,0]       # Makes the middle pixel red
data[512,513] = [0,0,255]       # Makes the next pixel blue

img = smp.toimage( data )       # Create a PIL image
img.show()                      # View in default viewer
