class rnd_gen:
	def __init__(self):
            self.state=0
	
	def seed(self,s):
            self.state=s
            warm_up()

        def get_seed(self):
            return self.state
	
        def warm_up(self):
            for i in range(0,10):
                self.rnd_int()

	def rnd_int(self):
            self.state=int(self.state*214013+2531011)
            return int(abs(self.state))

        def rnd_range(self,lo, hi):
            return lo+self.rnd_int()%(hi-lo)
	
        def rnd_flt(self):
            return self.rnd_int()/pow(2,32)*2

        def rnd_centred_flt(self):
            return (self.rnd_flt()-0.5)*2;

        def choose(self,arr):
            return arr[self.rnd_int()%arr.length()]

