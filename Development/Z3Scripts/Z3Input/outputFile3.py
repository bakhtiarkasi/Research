
from z3 import *
from cassMain import *

#begin methods
	
set_option(max_depth=1000, max_args=9000, max_lines = 1000, max_width=1000, max_indent=1000)

def binarySrch(imin, imax):
	if imin == imax:
		fb = FreshBool()
		wt = Implies(fb,getGoal() == imin)
		s.add(wt)
		s.check(DC+IC+[fb])
		m = s.model()
		print "Otimized to:%s "%imin;
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
			print "Cannot further Optimize";
			print printResult(m)
def printResult(m):
	result = 'Results:\n'
	result += 'D1 ->1['
	result += getResultforDev(D1,m)
	result +=  ']\n'
	result += 'D2 ->2['
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
s = getSolver()

##Developers Vectors
D1 = IntVector('D1', 3) 
D2 = IntVector('D2', 3) 

##Adding hard constraints
valueConst = [3 <= D1[2], D1[2] <= 3,2 <= D1[1], D1[1] <= 2,1 <= D1[0], D1[0] <= 1,\
		1 <= D2[0], D2[0] <= 3,1 <= D2[1], D2[1] <= 3,1 <= D2[2], D2[2] <= 3]

distConst = [Distinct(D1), Distinct(D2)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 1)
IC = BoolVector('IC', 0)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D2[0] != D1[0]))
##Adding INDirect Conflicts

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
	print 'Initial DC: %s'% len(DC);
	print 'Initial IC: %s'% len(IC);
	relaxConstraints(s,DC,IC)
	print 'Final DC: %s'% len(DC);
	print 'Final IC: %s'% len(IC);
	
	m = s.model()
	total = getDistance(m)
	getOptimizedSolution(s,total)
else:
	print 'unknown'		
