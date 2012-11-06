
from z3 import *
from cassMain import *

#begin methods

set_option(max_depth=1000, max_args=9000, max_lines = 1000, max_width=1000, max_indent=1000)
	
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
			total = total - 1
			print 'total = %s' % total
			fb = FreshBool()
			wt = Implies(fb,getGoal() < total)
			s.add(wt)
			print s
			m = s.model()
		
		print printResult(m)

def printResult(m):
	result = 'Results:\n'
	result += 'D1 -> ['
	result += getResultforDev(D1,m)
	result +=  ']\n'
	result += 'D2 -> ['
	result += getResultforDev(D2,m)
	result +=  ']\n'
	return result

def getGoal():
	return simplify(getGoalFor(D1) + getGoalFor(D2)) 

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	return total

###end method sections

#initialize the solver
s = Solver()

##Developers Vectors
D1 = IntVector('D1', 14) 
D2 = IntVector('D2', 14) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 14,1 <= D1[1], D1[1] <= 14,1 <= D1[2], D1[2] <= 14,1 <= D1[3], D1[3] <= 14,1 <= D1[4], D1[4] <= 14,1 <= D1[5], D1[5] <= 14,1 <= D1[6], D1[6] <= 14,1 <= D1[7], D1[7] <= 14,1 <= D1[8], D1[8] <= 14,1 <= D1[9], D1[9] <= 14,1 <= D1[10], D1[10] <= 14,1 <= D1[11], D1[11] <= 14,1 <= D1[12], D1[12] <= 14,1 <= D1[13], D1[13] <= 14,\
		1 <= D2[0], D2[0] <= 14,1 <= D2[1], D2[1] <= 14,1 <= D2[2], D2[2] <= 14,1 <= D2[3], D2[3] <= 14,1 <= D2[4], D2[4] <= 14,1 <= D2[5], D2[5] <= 14,1 <= D2[6], D2[6] <= 14,1 <= D2[7], D2[7] <= 14,1 <= D2[8], D2[8] <= 14,1 <= D2[9], D2[9] <= 14,1 <= D2[10], D2[10] <= 14,1 <= D2[11], D2[11] <= 14,1 <= D2[12], D2[12] <= 14,1 <= D2[13], D2[13] <= 14]

distConst = [Distinct(D1), Distinct(D2)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 15)
IC = BoolVector('IC', 10)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D1[9] != D2[5]))
s.add(Implies(DC[1], D1[2] != D2[10]))
s.add(Implies(DC[2], D1[13] != D2[5]))
s.add(Implies(DC[3], D1[6] != D2[3]))
s.add(Implies(DC[4], D1[7] != D2[7]))
s.add(Implies(DC[5], D1[9] != D2[10]))
s.add(Implies(DC[6], D1[0] != D2[10]))
s.add(Implies(DC[7], D1[5] != D2[13]))
s.add(Implies(DC[8], D1[1] != D2[10]))
s.add(Implies(DC[9], D1[1] != D2[0]))
s.add(Implies(DC[10], D1[13] != D2[2]))
s.add(Implies(DC[11], D1[4] != D2[9]))
s.add(Implies(DC[12], D1[10] != D2[3]))
s.add(Implies(DC[13], D1[13] != D2[9]))
s.add(Implies(DC[14], D1[8] != D2[8]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D1[7] < D2[8]))
s.add(Implies(IC[1], D1[13] < D2[5]))
s.add(Implies(IC[2], D1[4] > D2[12]))
s.add(Implies(IC[3], D1[7] < D2[5]))
s.add(Implies(IC[4], D1[12] > D2[4]))
s.add(Implies(IC[5], D1[1] < D2[13]))
s.add(Implies(IC[6], D1[0] > D2[6]))
s.add(Implies(IC[7], D1[6] > D2[9]))
s.add(Implies(IC[8], D1[9] < D2[2]))
s.add(Implies(IC[9], D1[5] < D2[11]))

##End Java insert


# Check if a solution exisits to this problem
isSat = s.check(DC+IC)
if isSat == sat:
	print 'Solution is SAT'
	m = s.model()
	total = getDistance(m)
	print 'Optimizing Solution'
	print printResult(m)
	getOptimizedSolution(s,total)
elif isSat == unsat:
	print 'UnSAT Scenario: relaxing constraints'
	relaxConstraints(s,DC,IC)
	m = s.model()
	print printResult(m)
else:
	print 'unknown'		
