
from z3 import *
from cassMain import *

#begin methods
	
set_option(max_depth=1000, max_args=9000, max_lines = 1000, max_width=1000, max_indent=1000)

def binarySrch(imin, imax):
	if imin == imax:
		fb = FreshBool()
		wt = Implies(fb,getGoal() == imin)
		s.check(DC+IC+[fb])
		s.add(wt)
		m = s.model()
		print imin
		print printResult(m)
		return;
	else:
		imid = (imin + imax) / 2
		
		fb = FreshBool()
		wt = Implies(fb,getGoal() <= imid)
		s.add(wt)
		
	if s.check(DC+IC+[fb]) == sat:
		return binarySrch(imin, imid)
	else:
		return binarySrch(imid+1, imax)
	
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
		
		if s.check(DC+IC+[fb]) == sat:
			binarySrch(0,total);
		else:
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
	return simplify( getGoalFor(D1) + getGoalFor(D2) )

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	return total

###end method sections

#initialize the solver
s = Then(With('simplify', blast_distinct=True, arith_lhs=True), 
 'normalize-bounds',  'lia2pb', 'pb2bv', 'bit-blast', 'smt').solver()

##Developers Vectors
D1 = IntVector('D1', 2) 
D2 = IntVector('D2', 2) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 2,1 <= D1[1], D1[1] <= 2,\
		1 <= D2[0], D2[0] <= 2,1 <= D2[1], D2[1] <= 2]

distConst = [Distinct(D1), Distinct(D2)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 1)
IC = BoolVector('IC', 1)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D1[0] != D2[0]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D1[1] > D2[0]))

##End Java insert


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
	relaxConstraints2(s,DC,IC)
	m = s.model()
	print printResult(m)
else:
	print 'unknown'		
