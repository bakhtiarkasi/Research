
from z3 import *
from cassMain import *

#begin methods
	
#returns an optimized solution
def getOptimizedSolution(s, total):
	m = s.model()
	
	if is_true(total == 0):
		print printResult(m)
	else:
		print 'total = %s' % total
		
		fb = FreshBool()
		wt = Implies(fb,getGoal() < total)
		s.add(wt)
		
		while s.check(DC+IC+[fb]) == sat:
			total = simplify(total - 1)
			fb = FreshBool()
			wt = Implies(fb,getGoal() < total)
			s.add(wt)
			print s
			m = s.model()
		
		print printResult(m)

#Start java insert

def printResult(m):
	result = 'D1 -> ['
	result += getResultforDev(D1,m)
	result +=  ']\n'
	result += 'D2 -> ['
	result += getResultforDev(D2,m)
	result +=  ']\n'
	return result	

def getGoal():
	return getGoalFor(D1) +\
		   getGoalFor(D2)

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	return simplify(total)


###end method sections

#initialize the solver add constraints
s = Solver()		

#Developers Vectors
D1 = IntVector('D1', 2)
D2 = IntVector('D2', 3)

hardCons = (1 <= D1[0], D1[0] <= 2,\
			1 <= D1[1], D1[1] <= 2,\
			1 <= D2[0], D2[0] <= 2,\
			1 <= D2[1], D2[1] <= 2,\
                        0 <= D2[2])
			
s.add(hardCons)

#Boolean vectors for DC/IC
DC = BoolVector('DC', 2)
IC = BoolVector('IC', 1)


#Adding soft cosntraints as implies
s.add(Implies(DC[0], D1[0] != D1[1]))
s.add(Implies(DC[1], D2[0] != D2[1]))
s.add(Implies(IC[0], And(D2[2] >0, D2[2] < 1) ))

#end java insert

# Check if a solution exisits to this problem
isSat = s.check(DC+IC)
if isSat == sat:
	print 'Solution is SAT'
	m = s.model()
	total = getDistance(m)
	print 'Optimizing Solution'
	getOptimizedSolution(s,total)
elif isSat == unsat:
	print 'UnSAT Scenario: relaxing constraints'
	relaxConstraints(s,DC,IC)
	m = s.model()
	print printResult(m)
else:
	print 'unknown'		