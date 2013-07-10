
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
	result += 'D7 -> ['
	result += getResultforDev(D7,m)
	result +=  ']\n'
	return result

def getGoal():
	return simplify( getGoalFor(D1) + getGoalFor(D2) + getGoalFor(D3) + getGoalFor(D4) + getGoalFor(D5) + getGoalFor(D6) + getGoalFor(D7) )

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	total = total + distance(D3, m)
	total = total + distance(D4, m)
	total = total + distance(D5, m)
	total = total + distance(D6, m)
	total = total + distance(D7, m)
	return total

###end method sections

#initialize the solver
s = getSolver()

##Developers Vectors
D1 = IntVector('D1', 8) 
D2 = IntVector('D2', 8) 
D3 = IntVector('D3', 8) 
D4 = IntVector('D4', 8) 
D5 = IntVector('D5', 9) 
D6 = IntVector('D6', 8) 
D7 = IntVector('D7', 8) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 8,1 <= D1[1], D1[1] <= 8,1 <= D1[2], D1[2] <= 8,1 <= D1[3], D1[3] <= 8,1 <= D1[4], D1[4] <= 8,1 <= D1[5], D1[5] <= 8,1 <= D1[6], D1[6] <= 8,1 <= D1[7], D1[7] <= 8,\
		1 <= D2[0], D2[0] <= 8,1 <= D2[1], D2[1] <= 8,1 <= D2[2], D2[2] <= 8,1 <= D2[3], D2[3] <= 8,1 <= D2[4], D2[4] <= 8,1 <= D2[5], D2[5] <= 8,1 <= D2[6], D2[6] <= 8,1 <= D2[7], D2[7] <= 8,\
		1 <= D3[0], D3[0] <= 8,1 <= D3[1], D3[1] <= 8,1 <= D3[2], D3[2] <= 8,1 <= D3[3], D3[3] <= 8,1 <= D3[4], D3[4] <= 8,1 <= D3[5], D3[5] <= 8,1 <= D3[6], D3[6] <= 8,1 <= D3[7], D3[7] <= 8,\
		1 <= D4[0], D4[0] <= 8,1 <= D4[1], D4[1] <= 8,1 <= D4[2], D4[2] <= 8,1 <= D4[3], D4[3] <= 8,1 <= D4[4], D4[4] <= 8,1 <= D4[5], D4[5] <= 8,1 <= D4[6], D4[6] <= 8,1 <= D4[7], D4[7] <= 8,\
		1 <= D5[0], D5[0] <= 9,1 <= D5[1], D5[1] <= 9,1 <= D5[2], D5[2] <= 9,1 <= D5[3], D5[3] <= 9,1 <= D5[4], D5[4] <= 9,1 <= D5[5], D5[5] <= 9,1 <= D5[6], D5[6] <= 9,1 <= D5[7], D5[7] <= 9,1 <= D5[8], D5[8] <= 9,\
		1 <= D6[0], D6[0] <= 8,1 <= D6[1], D6[1] <= 8,1 <= D6[2], D6[2] <= 8,1 <= D6[3], D6[3] <= 8,1 <= D6[4], D6[4] <= 8,1 <= D6[5], D6[5] <= 8,1 <= D6[6], D6[6] <= 8,1 <= D6[7], D6[7] <= 8,\
		1 <= D7[0], D7[0] <= 8,1 <= D7[1], D7[1] <= 8,1 <= D7[2], D7[2] <= 8,1 <= D7[3], D7[3] <= 8,1 <= D7[4], D7[4] <= 8,1 <= D7[5], D7[5] <= 8,1 <= D7[6], D7[6] <= 8,1 <= D7[7], D7[7] <= 8]

distConst = [Distinct(D1), Distinct(D2), Distinct(D3), Distinct(D4), Distinct(D5), Distinct(D6), Distinct(D7)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 16)
IC = BoolVector('IC', 110)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D5[8] != D6[7]))
s.add(Implies(DC[1], D1[1] != D6[3]))
s.add(Implies(DC[2], D1[1] != D3[1]))
s.add(Implies(DC[3], D4[1] != D6[0]))
s.add(Implies(DC[4], D3[6] != D5[7]))
s.add(Implies(DC[5], D3[2] != D5[6]))
s.add(Implies(DC[6], D5[4] != D6[1]))
s.add(Implies(DC[7], D1[4] != D7[4]))
s.add(Implies(DC[8], D3[4] != D4[7]))
s.add(Implies(DC[9], D1[7] != D5[4]))
s.add(Implies(DC[10], D1[3] != D6[0]))
s.add(Implies(DC[11], D6[6] != D7[0]))
s.add(Implies(DC[12], D1[0] != D2[7]))
s.add(Implies(DC[13], D2[3] != D3[1]))
s.add(Implies(DC[14], D1[5] != D5[0]))
s.add(Implies(DC[15], D1[5] != D4[2]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D1[7] > D5[1]))
s.add(Implies(IC[1], D1[5] > D6[1]))
s.add(Implies(IC[2], D6[5] > D7[6]))
s.add(Implies(IC[3], D1[0] > D3[5]))
s.add(Implies(IC[4], D4[7] > D6[2]))
s.add(Implies(IC[5], D4[1] > D7[6]))
s.add(Implies(IC[6], D5[7] > D6[5]))
s.add(Implies(IC[7], D5[4] > D6[5]))
s.add(Implies(IC[8], D2[7] > D5[2]))
s.add(Implies(IC[9], D3[0] > D7[5]))
s.add(Implies(IC[10], D1[6] > D3[6]))
s.add(Implies(IC[11], D5[1] > D7[2]))
s.add(Implies(IC[12], D2[6] > D6[6]))
s.add(Implies(IC[13], D1[0] > D6[0]))
s.add(Implies(IC[14], D6[5] > D7[2]))
s.add(Implies(IC[15], D1[1] > D2[1]))
s.add(Implies(IC[16], D1[7] > D3[1]))
s.add(Implies(IC[17], D3[7] > D7[5]))
s.add(Implies(IC[18], D2[4] > D5[1]))
s.add(Implies(IC[19], D3[1] > D7[7]))
s.add(Implies(IC[20], D1[2] > D4[7]))
s.add(Implies(IC[21], D1[7] > D4[7]))
s.add(Implies(IC[22], D4[3] > D5[5]))
s.add(Implies(IC[23], D3[1] > D6[5]))
s.add(Implies(IC[24], D5[3] > D6[0]))
s.add(Implies(IC[25], D1[5] > D7[1]))
s.add(Implies(IC[26], D2[7] > D5[6]))
s.add(Implies(IC[27], D1[0] > D5[5]))
s.add(Implies(IC[28], D1[2] > D6[4]))
s.add(Implies(IC[29], D2[2] > D6[4]))
s.add(Implies(IC[30], D4[4] > D5[5]))
s.add(Implies(IC[31], D1[2] > D6[0]))
s.add(Implies(IC[32], D3[5] > D5[4]))
s.add(Implies(IC[33], D2[4] > D6[7]))
s.add(Implies(IC[34], D4[0] > D6[0]))
s.add(Implies(IC[35], D2[2] > D4[5]))
s.add(Implies(IC[36], D2[2] > D3[6]))
s.add(Implies(IC[37], D5[5] > D7[0]))
s.add(Implies(IC[38], D1[5] > D6[2]))
s.add(Implies(IC[39], D1[6] > D7[7]))
s.add(Implies(IC[40], D4[0] > D7[5]))
s.add(Implies(IC[41], D2[3] > D3[7]))
s.add(Implies(IC[42], D3[6] > D4[2]))
s.add(Implies(IC[43], D1[4] > D6[2]))
s.add(Implies(IC[44], D4[0] > D6[5]))
s.add(Implies(IC[45], D5[0] > D7[5]))
s.add(Implies(IC[46], D6[0] > D7[0]))
s.add(Implies(IC[47], D3[5] > D5[3]))
s.add(Implies(IC[48], D3[7] > D6[6]))
s.add(Implies(IC[49], D2[3] > D7[5]))
s.add(Implies(IC[50], D2[2] > D5[4]))
s.add(Implies(IC[51], D6[1] > D7[0]))
s.add(Implies(IC[52], D1[3] > D5[2]))
s.add(Implies(IC[53], D3[5] > D4[7]))
s.add(Implies(IC[54], D5[5] > D7[3]))
s.add(Implies(IC[55], D1[4] > D2[2]))
s.add(Implies(IC[56], D2[1] > D4[0]))
s.add(Implies(IC[57], D2[4] > D3[7]))
s.add(Implies(IC[58], D3[6] > D5[6]))
s.add(Implies(IC[59], D4[6] > D5[2]))
s.add(Implies(IC[60], D1[4] > D4[0]))
s.add(Implies(IC[61], D4[1] > D5[7]))
s.add(Implies(IC[62], D3[1] > D7[1]))
s.add(Implies(IC[63], D4[6] > D7[3]))
s.add(Implies(IC[64], D3[1] > D4[2]))
s.add(Implies(IC[65], D2[4] > D5[5]))
s.add(Implies(IC[66], D3[7] > D5[7]))
s.add(Implies(IC[67], D5[2] > D6[3]))
s.add(Implies(IC[68], D1[1] > D7[7]))
s.add(Implies(IC[69], D1[6] > D6[1]))
s.add(Implies(IC[70], D5[7] > D6[6]))
s.add(Implies(IC[71], D2[0] > D7[0]))
s.add(Implies(IC[72], D4[2] > D5[4]))
s.add(Implies(IC[73], D5[1] > D6[0]))
s.add(Implies(IC[74], D4[4] > D7[5]))
s.add(Implies(IC[75], D2[5] > D6[4]))
s.add(Implies(IC[76], D6[3] > D7[0]))
s.add(Implies(IC[77], D1[4] > D4[1]))
s.add(Implies(IC[78], D3[5] > D4[0]))
s.add(Implies(IC[79], D3[4] > D6[2]))
s.add(Implies(IC[80], D5[2] > D6[7]))
s.add(Implies(IC[81], D3[5] > D7[6]))
s.add(Implies(IC[82], D2[4] > D6[5]))
s.add(Implies(IC[83], D2[6] > D7[1]))
s.add(Implies(IC[84], D2[4] > D3[1]))
s.add(Implies(IC[85], D2[0] > D3[7]))
s.add(Implies(IC[86], D3[0] > D7[0]))
s.add(Implies(IC[87], D1[3] > D2[4]))
s.add(Implies(IC[88], D1[6] > D2[6]))
s.add(Implies(IC[89], D2[7] > D3[1]))
s.add(Implies(IC[90], D3[2] > D7[5]))
s.add(Implies(IC[91], D2[5] > D7[4]))
s.add(Implies(IC[92], D5[3] > D6[7]))
s.add(Implies(IC[93], D6[4] > D7[7]))
s.add(Implies(IC[94], D3[5] > D5[4]))
s.add(Implies(IC[95], D4[1] > D5[8]))
s.add(Implies(IC[96], D4[7] > D5[7]))
s.add(Implies(IC[97], D2[1] > D3[6]))
s.add(Implies(IC[98], D2[5] > D5[0]))
s.add(Implies(IC[99], D3[1] > D4[7]))
s.add(Implies(IC[100], D1[2] > D5[1]))
s.add(Implies(IC[101], D4[3] > D7[5]))
s.add(Implies(IC[102], D5[1] > D7[4]))
s.add(Implies(IC[103], D1[3] > D6[7]))
s.add(Implies(IC[104], D1[0] > D2[7]))
s.add(Implies(IC[105], D1[6] > D4[7]))
s.add(Implies(IC[106], D4[1] > D5[0]))
s.add(Implies(IC[107], D3[7] > D7[6]))
s.add(Implies(IC[108], D1[1] > D5[6]))
s.add(Implies(IC[109], D2[7] > D3[0]))

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
