
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
	result += 'D3 -> ['
	result += getResultforDev(D3,m)
	result +=  ']\n'
	result += 'D4 -> ['
	result += getResultforDev(D4,m)
	result +=  ']\n'
	result += 'D5 -> ['
	result += getResultforDev(D5,m)
	result +=  ']\n'
	result += 'D6 -> ['
	result += getResultforDev(D6,m)
	result +=  ']\n'
	return result

def getGoal():
	return simplify( getGoalFor(D1) + getGoalFor(D2) + getGoalFor(D3) + getGoalFor(D4) + getGoalFor(D5) + getGoalFor(D6) )

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	total = total + distance(D3, m)
	total = total + distance(D4, m)
	total = total + distance(D5, m)
	total = total + distance(D6, m)
	return total

###end method sections

#initialize the solver
s = getSolver()

##Developers Vectors
D1 = IntVector('D1', 8) 
D2 = IntVector('D2', 9) 
D3 = IntVector('D3', 8) 
D4 = IntVector('D4', 8) 
D5 = IntVector('D5', 8) 
D6 = IntVector('D6', 9) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 8,1 <= D1[1], D1[1] <= 8,1 <= D1[2], D1[2] <= 8,1 <= D1[3], D1[3] <= 8,1 <= D1[4], D1[4] <= 8,1 <= D1[5], D1[5] <= 8,1 <= D1[6], D1[6] <= 8,1 <= D1[7], D1[7] <= 8,\
		1 <= D2[0], D2[0] <= 9,1 <= D2[1], D2[1] <= 9,1 <= D2[2], D2[2] <= 9,1 <= D2[3], D2[3] <= 9,1 <= D2[4], D2[4] <= 9,1 <= D2[5], D2[5] <= 9,1 <= D2[6], D2[6] <= 9,1 <= D2[7], D2[7] <= 9,1 <= D2[8], D2[8] <= 9,\
		1 <= D3[0], D3[0] <= 8,1 <= D3[1], D3[1] <= 8,1 <= D3[2], D3[2] <= 8,1 <= D3[3], D3[3] <= 8,1 <= D3[4], D3[4] <= 8,1 <= D3[5], D3[5] <= 8,1 <= D3[6], D3[6] <= 8,1 <= D3[7], D3[7] <= 8,\
		1 <= D4[0], D4[0] <= 8,1 <= D4[1], D4[1] <= 8,1 <= D4[2], D4[2] <= 8,1 <= D4[3], D4[3] <= 8,1 <= D4[4], D4[4] <= 8,1 <= D4[5], D4[5] <= 8,1 <= D4[6], D4[6] <= 8,1 <= D4[7], D4[7] <= 8,\
		1 <= D5[0], D5[0] <= 8,1 <= D5[1], D5[1] <= 8,1 <= D5[2], D5[2] <= 8,1 <= D5[3], D5[3] <= 8,1 <= D5[4], D5[4] <= 8,1 <= D5[5], D5[5] <= 8,1 <= D5[6], D5[6] <= 8,1 <= D5[7], D5[7] <= 8,\
		1 <= D6[0], D6[0] <= 9,1 <= D6[1], D6[1] <= 9,1 <= D6[2], D6[2] <= 9,1 <= D6[3], D6[3] <= 9,1 <= D6[4], D6[4] <= 9,1 <= D6[5], D6[5] <= 9,1 <= D6[6], D6[6] <= 9,1 <= D6[7], D6[7] <= 9,1 <= D6[8], D6[8] <= 9]

distConst = [Distinct(D1), Distinct(D2), Distinct(D3), Distinct(D4), Distinct(D5), Distinct(D6)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 8)
IC = BoolVector('IC', 35)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D1[4] != D2[8]))
s.add(Implies(DC[1], D5[0] != D6[4]))
s.add(Implies(DC[2], D4[4] != D5[2]))
s.add(Implies(DC[3], D2[2] != D4[2]))
s.add(Implies(DC[4], D4[0] != D5[3]))
s.add(Implies(DC[5], D4[5] != D6[4]))
s.add(Implies(DC[6], D1[4] != D6[7]))
s.add(Implies(DC[7], D3[7] != D4[7]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D5[3] > D6[1]))
s.add(Implies(IC[1], D2[6] > D6[3]))
s.add(Implies(IC[2], D1[5] > D5[0]))
s.add(Implies(IC[3], D1[4] > D5[7]))
s.add(Implies(IC[4], D1[3] > D6[1]))
s.add(Implies(IC[5], D3[3] > D6[1]))
s.add(Implies(IC[6], D1[5] > D3[5]))
s.add(Implies(IC[7], D4[5] > D6[3]))
s.add(Implies(IC[8], D1[4] > D5[0]))
s.add(Implies(IC[9], D2[2] > D6[5]))
s.add(Implies(IC[10], D2[6] > D5[0]))
s.add(Implies(IC[11], D4[5] > D6[4]))
s.add(Implies(IC[12], D3[1] > D6[7]))
s.add(Implies(IC[13], D3[1] > D4[7]))
s.add(Implies(IC[14], D1[6] > D6[5]))
s.add(Implies(IC[15], D2[2] > D6[1]))
s.add(Implies(IC[16], D2[8] > D3[7]))
s.add(Implies(IC[17], D5[0] > D6[3]))
s.add(Implies(IC[18], D1[6] > D2[1]))
s.add(Implies(IC[19], D1[1] > D2[6]))
s.add(Implies(IC[20], D3[4] > D6[4]))
s.add(Implies(IC[21], D1[4] > D3[2]))
s.add(Implies(IC[22], D2[8] > D5[0]))
s.add(Implies(IC[23], D3[0] > D4[1]))
s.add(Implies(IC[24], D5[2] > D6[4]))
s.add(Implies(IC[25], D2[1] > D5[4]))
s.add(Implies(IC[26], D2[3] > D4[3]))
s.add(Implies(IC[27], D3[6] > D6[6]))
s.add(Implies(IC[28], D1[3] > D3[7]))
s.add(Implies(IC[29], D1[7] > D4[4]))
s.add(Implies(IC[30], D1[6] > D3[3]))
s.add(Implies(IC[31], D3[7] > D6[8]))
s.add(Implies(IC[32], D3[3] > D5[3]))
s.add(Implies(IC[33], D2[8] > D3[7]))
s.add(Implies(IC[34], D3[4] > D5[5]))

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
