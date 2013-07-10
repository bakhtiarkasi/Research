
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
s = getSolverforOpt()

##Developers Vectors
D1 = IntVector('D1', 6) 
D2 = IntVector('D2', 6) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 6,1 <= D1[1], D1[1] <= 6,1 <= D1[2], D1[2] <= 6,1 <= D1[3], D1[3] <= 6,1 <= D1[4], D1[4] <= 6,1 <= D1[5], D1[5] <= 6,\
		1 <= D2[0], D2[0] <= 6,1 <= D2[1], D2[1] <= 6,1 <= D2[2], D2[2] <= 6,1 <= D2[3], D2[3] <= 6,1 <= D2[4], D2[4] <= 6,1 <= D2[5], D2[5] <= 6]

distConst = [Distinct(D1), Distinct(D2)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 0)
IC = BoolVector('IC', 5)

##Adding soft constraints as assertions
##Adding Direct Conflicts

##Adding InDirect Conflicts
s.add(Implies(IC[0], D1[3] > D2[2]))
s.add(Implies(IC[1], D1[3] > D2[0]))
s.add(Implies(IC[2], D1[5] > D2[4]))
s.add(Implies(IC[3], D1[2] > D2[4]))
s.add(Implies(IC[4], D1[0] > D2[2]))

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
