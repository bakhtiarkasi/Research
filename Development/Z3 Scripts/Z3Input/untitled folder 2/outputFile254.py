
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
	return result

def getGoal():
	return simplify( getGoalFor(D1) + getGoalFor(D2) + getGoalFor(D3) + getGoalFor(D4) + getGoalFor(D5) )

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	total = total + distance(D3, m)
	total = total + distance(D4, m)
	total = total + distance(D5, m)
	return total

###end method sections

#initialize the solver
s = getSolver()

##Developers Vectors
D1 = IntVector('D1', 8) 
D2 = IntVector('D2', 8) 
D3 = IntVector('D3', 9) 
D4 = IntVector('D4', 9) 
D5 = IntVector('D5', 8) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 8,1 <= D1[1], D1[1] <= 8,1 <= D1[2], D1[2] <= 8,1 <= D1[3], D1[3] <= 8,1 <= D1[4], D1[4] <= 8,1 <= D1[5], D1[5] <= 8,1 <= D1[6], D1[6] <= 8,1 <= D1[7], D1[7] <= 8,\
		1 <= D2[0], D2[0] <= 8,1 <= D2[1], D2[1] <= 8,1 <= D2[2], D2[2] <= 8,1 <= D2[3], D2[3] <= 8,1 <= D2[4], D2[4] <= 8,1 <= D2[5], D2[5] <= 8,1 <= D2[6], D2[6] <= 8,1 <= D2[7], D2[7] <= 8,\
		1 <= D3[0], D3[0] <= 9,1 <= D3[1], D3[1] <= 9,1 <= D3[2], D3[2] <= 9,1 <= D3[3], D3[3] <= 9,1 <= D3[4], D3[4] <= 9,1 <= D3[5], D3[5] <= 9,1 <= D3[6], D3[6] <= 9,1 <= D3[7], D3[7] <= 9,1 <= D3[8], D3[8] <= 9,\
		1 <= D4[0], D4[0] <= 9,1 <= D4[1], D4[1] <= 9,1 <= D4[2], D4[2] <= 9,1 <= D4[3], D4[3] <= 9,1 <= D4[4], D4[4] <= 9,1 <= D4[5], D4[5] <= 9,1 <= D4[6], D4[6] <= 9,1 <= D4[7], D4[7] <= 9,1 <= D4[8], D4[8] <= 9,\
		1 <= D5[0], D5[0] <= 8,1 <= D5[1], D5[1] <= 8,1 <= D5[2], D5[2] <= 8,1 <= D5[3], D5[3] <= 8,1 <= D5[4], D5[4] <= 8,1 <= D5[5], D5[5] <= 8,1 <= D5[6], D5[6] <= 8,1 <= D5[7], D5[7] <= 8]

distConst = [Distinct(D1), Distinct(D2), Distinct(D3), Distinct(D4), Distinct(D5)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 7)
IC = BoolVector('IC', 66)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D1[1] != D5[3]))
s.add(Implies(DC[1], D2[1] != D5[6]))
s.add(Implies(DC[2], D2[6] != D3[4]))
s.add(Implies(DC[3], D2[6] != D3[2]))
s.add(Implies(DC[4], D1[7] != D2[5]))
s.add(Implies(DC[5], D3[8] != D5[1]))
s.add(Implies(DC[6], D4[7] != D5[2]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D3[0] > D5[7]))
s.add(Implies(IC[1], D1[1] > D2[0]))
s.add(Implies(IC[2], D1[4] > D4[4]))
s.add(Implies(IC[3], D3[1] > D5[3]))
s.add(Implies(IC[4], D3[1] > D4[8]))
s.add(Implies(IC[5], D2[3] > D4[0]))
s.add(Implies(IC[6], D2[5] > D3[8]))
s.add(Implies(IC[7], D1[4] > D5[1]))
s.add(Implies(IC[8], D1[0] > D3[2]))
s.add(Implies(IC[9], D2[6] > D4[7]))
s.add(Implies(IC[10], D3[4] > D5[0]))
s.add(Implies(IC[11], D2[4] > D4[6]))
s.add(Implies(IC[12], D2[4] > D5[5]))
s.add(Implies(IC[13], D2[0] > D3[4]))
s.add(Implies(IC[14], D1[7] > D5[7]))
s.add(Implies(IC[15], D2[3] > D4[7]))
s.add(Implies(IC[16], D1[2] > D3[3]))
s.add(Implies(IC[17], D3[3] > D5[7]))
s.add(Implies(IC[18], D2[5] > D4[8]))
s.add(Implies(IC[19], D2[1] > D5[1]))
s.add(Implies(IC[20], D2[1] > D4[4]))
s.add(Implies(IC[21], D1[3] > D3[5]))
s.add(Implies(IC[22], D2[7] > D3[1]))
s.add(Implies(IC[23], D1[2] > D5[1]))
s.add(Implies(IC[24], D4[1] > D5[1]))
s.add(Implies(IC[25], D1[0] > D4[7]))
s.add(Implies(IC[26], D4[3] > D5[6]))
s.add(Implies(IC[27], D3[6] > D5[2]))
s.add(Implies(IC[28], D3[0] > D5[1]))
s.add(Implies(IC[29], D3[3] > D4[3]))
s.add(Implies(IC[30], D3[8] > D5[0]))
s.add(Implies(IC[31], D2[3] > D4[1]))
s.add(Implies(IC[32], D2[0] > D5[0]))
s.add(Implies(IC[33], D1[2] > D2[1]))
s.add(Implies(IC[34], D1[4] > D2[3]))
s.add(Implies(IC[35], D1[1] > D3[0]))
s.add(Implies(IC[36], D3[3] > D5[3]))
s.add(Implies(IC[37], D2[3] > D4[3]))
s.add(Implies(IC[38], D1[1] > D5[0]))
s.add(Implies(IC[39], D1[5] > D2[5]))
s.add(Implies(IC[40], D2[2] > D3[7]))
s.add(Implies(IC[41], D3[3] > D5[1]))
s.add(Implies(IC[42], D1[5] > D4[6]))
s.add(Implies(IC[43], D2[0] > D3[6]))
s.add(Implies(IC[44], D2[2] > D3[2]))
s.add(Implies(IC[45], D4[3] > D5[7]))
s.add(Implies(IC[46], D1[7] > D3[8]))
s.add(Implies(IC[47], D1[3] > D2[2]))
s.add(Implies(IC[48], D1[0] > D4[1]))
s.add(Implies(IC[49], D3[0] > D4[8]))
s.add(Implies(IC[50], D4[3] > D5[3]))
s.add(Implies(IC[51], D1[1] > D5[5]))
s.add(Implies(IC[52], D1[1] > D5[1]))
s.add(Implies(IC[53], D1[1] > D5[3]))
s.add(Implies(IC[54], D1[7] > D5[4]))
s.add(Implies(IC[55], D2[5] > D3[2]))
s.add(Implies(IC[56], D2[1] > D3[6]))
s.add(Implies(IC[57], D3[5] > D4[5]))
s.add(Implies(IC[58], D3[1] > D4[4]))
s.add(Implies(IC[59], D1[3] > D3[0]))
s.add(Implies(IC[60], D1[4] > D5[0]))
s.add(Implies(IC[61], D4[4] > D5[0]))
s.add(Implies(IC[62], D1[3] > D2[6]))
s.add(Implies(IC[63], D4[3] > D5[6]))
s.add(Implies(IC[64], D2[7] > D4[3]))
s.add(Implies(IC[65], D1[4] > D4[2]))

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
