
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
	result += 'D8 -> ['
	result += getResultforDev(D8,m)
	result +=  ']\n'
	result += 'D9 -> ['
	result += getResultforDev(D9,m)
	result +=  ']\n'
	result += 'D10 -> ['
	result += getResultforDev(D10,m)
	result +=  ']\n'
	result += 'D11 -> ['
	result += getResultforDev(D11,m)
	result +=  ']\n'
	result += 'D12 -> ['
	result += getResultforDev(D12,m)
	result +=  ']\n'
	result += 'D13 -> ['
	result += getResultforDev(D13,m)
	result +=  ']\n'
	return result

def getGoal():
	return simplify( getGoalFor(D1) + getGoalFor(D2) + getGoalFor(D3) + getGoalFor(D4) + getGoalFor(D5) + getGoalFor(D6) + getGoalFor(D7) + getGoalFor(D8) + getGoalFor(D9) + getGoalFor(D10) + getGoalFor(D11) + getGoalFor(D12) + getGoalFor(D13) )

def getDistance(m):
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	total = total + distance(D3, m)
	total = total + distance(D4, m)
	total = total + distance(D5, m)
	total = total + distance(D6, m)
	total = total + distance(D7, m)
	total = total + distance(D8, m)
	total = total + distance(D9, m)
	total = total + distance(D10, m)
	total = total + distance(D11, m)
	total = total + distance(D12, m)
	total = total + distance(D13, m)
	return total

###end method sections

#initialize the solver
s = getSolver()

##Developers Vectors
D1 = IntVector('D1', 8) 
D2 = IntVector('D2', 8) 
D3 = IntVector('D3', 8) 
D4 = IntVector('D4', 8) 
D5 = IntVector('D5', 8) 
D6 = IntVector('D6', 8) 
D7 = IntVector('D7', 9) 
D8 = IntVector('D8', 8) 
D9 = IntVector('D9', 8) 
D10 = IntVector('D10', 8) 
D11 = IntVector('D11', 8) 
D12 = IntVector('D12', 8) 
D13 = IntVector('D13', 8) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 8,1 <= D1[1], D1[1] <= 8,1 <= D1[2], D1[2] <= 8,1 <= D1[3], D1[3] <= 8,1 <= D1[4], D1[4] <= 8,1 <= D1[5], D1[5] <= 8,1 <= D1[6], D1[6] <= 8,1 <= D1[7], D1[7] <= 8,\
		1 <= D2[0], D2[0] <= 8,1 <= D2[1], D2[1] <= 8,1 <= D2[2], D2[2] <= 8,1 <= D2[3], D2[3] <= 8,1 <= D2[4], D2[4] <= 8,1 <= D2[5], D2[5] <= 8,1 <= D2[6], D2[6] <= 8,1 <= D2[7], D2[7] <= 8,\
		1 <= D3[0], D3[0] <= 8,1 <= D3[1], D3[1] <= 8,1 <= D3[2], D3[2] <= 8,1 <= D3[3], D3[3] <= 8,1 <= D3[4], D3[4] <= 8,1 <= D3[5], D3[5] <= 8,1 <= D3[6], D3[6] <= 8,1 <= D3[7], D3[7] <= 8,\
		1 <= D4[0], D4[0] <= 8,1 <= D4[1], D4[1] <= 8,1 <= D4[2], D4[2] <= 8,1 <= D4[3], D4[3] <= 8,1 <= D4[4], D4[4] <= 8,1 <= D4[5], D4[5] <= 8,1 <= D4[6], D4[6] <= 8,1 <= D4[7], D4[7] <= 8,\
		1 <= D5[0], D5[0] <= 8,1 <= D5[1], D5[1] <= 8,1 <= D5[2], D5[2] <= 8,1 <= D5[3], D5[3] <= 8,1 <= D5[4], D5[4] <= 8,1 <= D5[5], D5[5] <= 8,1 <= D5[6], D5[6] <= 8,1 <= D5[7], D5[7] <= 8,\
		1 <= D6[0], D6[0] <= 8,1 <= D6[1], D6[1] <= 8,1 <= D6[2], D6[2] <= 8,1 <= D6[3], D6[3] <= 8,1 <= D6[4], D6[4] <= 8,1 <= D6[5], D6[5] <= 8,1 <= D6[6], D6[6] <= 8,1 <= D6[7], D6[7] <= 8,\
		1 <= D7[0], D7[0] <= 9,1 <= D7[1], D7[1] <= 9,1 <= D7[2], D7[2] <= 9,1 <= D7[3], D7[3] <= 9,1 <= D7[4], D7[4] <= 9,1 <= D7[5], D7[5] <= 9,1 <= D7[6], D7[6] <= 9,1 <= D7[7], D7[7] <= 9,1 <= D7[8], D7[8] <= 9,\
		1 <= D8[0], D8[0] <= 8,1 <= D8[1], D8[1] <= 8,1 <= D8[2], D8[2] <= 8,1 <= D8[3], D8[3] <= 8,1 <= D8[4], D8[4] <= 8,1 <= D8[5], D8[5] <= 8,1 <= D8[6], D8[6] <= 8,1 <= D8[7], D8[7] <= 8,\
		1 <= D9[0], D9[0] <= 8,1 <= D9[1], D9[1] <= 8,1 <= D9[2], D9[2] <= 8,1 <= D9[3], D9[3] <= 8,1 <= D9[4], D9[4] <= 8,1 <= D9[5], D9[5] <= 8,1 <= D9[6], D9[6] <= 8,1 <= D9[7], D9[7] <= 8,\
		1 <= D10[0], D10[0] <= 8,1 <= D10[1], D10[1] <= 8,1 <= D10[2], D10[2] <= 8,1 <= D10[3], D10[3] <= 8,1 <= D10[4], D10[4] <= 8,1 <= D10[5], D10[5] <= 8,1 <= D10[6], D10[6] <= 8,1 <= D10[7], D10[7] <= 8,\
		1 <= D11[0], D11[0] <= 8,1 <= D11[1], D11[1] <= 8,1 <= D11[2], D11[2] <= 8,1 <= D11[3], D11[3] <= 8,1 <= D11[4], D11[4] <= 8,1 <= D11[5], D11[5] <= 8,1 <= D11[6], D11[6] <= 8,1 <= D11[7], D11[7] <= 8,\
		1 <= D12[0], D12[0] <= 8,1 <= D12[1], D12[1] <= 8,1 <= D12[2], D12[2] <= 8,1 <= D12[3], D12[3] <= 8,1 <= D12[4], D12[4] <= 8,1 <= D12[5], D12[5] <= 8,1 <= D12[6], D12[6] <= 8,1 <= D12[7], D12[7] <= 8,\
		1 <= D13[0], D13[0] <= 8,1 <= D13[1], D13[1] <= 8,1 <= D13[2], D13[2] <= 8,1 <= D13[3], D13[3] <= 8,1 <= D13[4], D13[4] <= 8,1 <= D13[5], D13[5] <= 8,1 <= D13[6], D13[6] <= 8,1 <= D13[7], D13[7] <= 8]

distConst = [Distinct(D1), Distinct(D2), Distinct(D3), Distinct(D4), Distinct(D5), Distinct(D6), Distinct(D7), Distinct(D8), Distinct(D9), Distinct(D10), Distinct(D11), Distinct(D12), Distinct(D13)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 79)
IC = BoolVector('IC', 156)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D4[7] != D7[8]))
s.add(Implies(DC[1], D7[3] != D10[6]))
s.add(Implies(DC[2], D2[0] != D13[2]))
s.add(Implies(DC[3], D5[1] != D8[4]))
s.add(Implies(DC[4], D1[0] != D5[0]))
s.add(Implies(DC[5], D2[6] != D12[0]))
s.add(Implies(DC[6], D3[7] != D12[2]))
s.add(Implies(DC[7], D4[0] != D6[6]))
s.add(Implies(DC[8], D7[6] != D9[5]))
s.add(Implies(DC[9], D11[1] != D12[4]))
s.add(Implies(DC[10], D5[6] != D6[5]))
s.add(Implies(DC[11], D3[5] != D8[6]))
s.add(Implies(DC[12], D9[7] != D10[5]))
s.add(Implies(DC[13], D3[3] != D6[3]))
s.add(Implies(DC[14], D3[5] != D8[5]))
s.add(Implies(DC[15], D1[5] != D11[2]))
s.add(Implies(DC[16], D7[7] != D13[5]))
s.add(Implies(DC[17], D8[6] != D11[5]))
s.add(Implies(DC[18], D1[3] != D9[2]))
s.add(Implies(DC[19], D4[7] != D8[0]))
s.add(Implies(DC[20], D3[1] != D11[1]))
s.add(Implies(DC[21], D8[1] != D9[6]))
s.add(Implies(DC[22], D2[1] != D5[2]))
s.add(Implies(DC[23], D2[7] != D5[5]))
s.add(Implies(DC[24], D5[7] != D10[3]))
s.add(Implies(DC[25], D8[5] != D13[4]))
s.add(Implies(DC[26], D1[4] != D7[5]))
s.add(Implies(DC[27], D5[5] != D10[7]))
s.add(Implies(DC[28], D3[4] != D11[2]))
s.add(Implies(DC[29], D4[3] != D11[4]))
s.add(Implies(DC[30], D8[0] != D12[6]))
s.add(Implies(DC[31], D8[5] != D10[5]))
s.add(Implies(DC[32], D3[0] != D5[4]))
s.add(Implies(DC[33], D8[3] != D12[6]))
s.add(Implies(DC[34], D4[3] != D9[0]))
s.add(Implies(DC[35], D6[7] != D7[5]))
s.add(Implies(DC[36], D7[0] != D12[1]))
s.add(Implies(DC[37], D7[3] != D11[1]))
s.add(Implies(DC[38], D5[3] != D12[0]))
s.add(Implies(DC[39], D5[3] != D9[3]))
s.add(Implies(DC[40], D1[2] != D9[1]))
s.add(Implies(DC[41], D6[2] != D11[2]))
s.add(Implies(DC[42], D3[4] != D12[4]))
s.add(Implies(DC[43], D7[1] != D9[6]))
s.add(Implies(DC[44], D6[5] != D12[6]))
s.add(Implies(DC[45], D10[2] != D11[7]))
s.add(Implies(DC[46], D9[1] != D10[7]))
s.add(Implies(DC[47], D4[2] != D13[0]))
s.add(Implies(DC[48], D2[1] != D12[7]))
s.add(Implies(DC[49], D5[3] != D13[4]))
s.add(Implies(DC[50], D3[1] != D10[6]))
s.add(Implies(DC[51], D2[0] != D7[1]))
s.add(Implies(DC[52], D4[2] != D10[3]))
s.add(Implies(DC[53], D3[1] != D11[4]))
s.add(Implies(DC[54], D5[1] != D12[4]))
s.add(Implies(DC[55], D4[6] != D8[5]))
s.add(Implies(DC[56], D5[3] != D8[7]))
s.add(Implies(DC[57], D4[1] != D8[6]))
s.add(Implies(DC[58], D6[4] != D13[4]))
s.add(Implies(DC[59], D8[2] != D12[7]))
s.add(Implies(DC[60], D11[0] != D12[4]))
s.add(Implies(DC[61], D1[6] != D11[5]))
s.add(Implies(DC[62], D1[6] != D4[7]))
s.add(Implies(DC[63], D9[5] != D12[5]))
s.add(Implies(DC[64], D8[5] != D9[7]))
s.add(Implies(DC[65], D2[4] != D5[2]))
s.add(Implies(DC[66], D3[1] != D9[3]))
s.add(Implies(DC[67], D2[6] != D11[1]))
s.add(Implies(DC[68], D5[0] != D11[3]))
s.add(Implies(DC[69], D2[7] != D10[2]))
s.add(Implies(DC[70], D7[0] != D12[3]))
s.add(Implies(DC[71], D3[1] != D11[2]))
s.add(Implies(DC[72], D10[5] != D11[1]))
s.add(Implies(DC[73], D3[4] != D9[6]))
s.add(Implies(DC[74], D1[2] != D9[6]))
s.add(Implies(DC[75], D5[2] != D8[0]))
s.add(Implies(DC[76], D6[6] != D12[7]))
s.add(Implies(DC[77], D6[5] != D9[7]))
s.add(Implies(DC[78], D10[1] != D13[5]))

##Adding InDirect Conflicts
s.add(Implies(IC[0], D2[3] > D13[0]))
s.add(Implies(IC[1], D6[7] > D10[1]))
s.add(Implies(IC[2], D3[2] > D10[5]))
s.add(Implies(IC[3], D3[6] > D7[8]))
s.add(Implies(IC[4], D3[7] > D6[0]))
s.add(Implies(IC[5], D4[5] > D7[2]))
s.add(Implies(IC[6], D1[2] > D7[1]))
s.add(Implies(IC[7], D1[0] > D4[7]))
s.add(Implies(IC[8], D8[6] > D13[1]))
s.add(Implies(IC[9], D4[6] > D6[6]))
s.add(Implies(IC[10], D3[6] > D10[7]))
s.add(Implies(IC[11], D4[2] > D10[4]))
s.add(Implies(IC[12], D3[4] > D10[6]))
s.add(Implies(IC[13], D5[4] > D9[2]))
s.add(Implies(IC[14], D9[2] > D13[5]))
s.add(Implies(IC[15], D1[3] > D13[6]))
s.add(Implies(IC[16], D2[4] > D4[3]))
s.add(Implies(IC[17], D7[1] > D12[4]))
s.add(Implies(IC[18], D10[1] > D13[4]))
s.add(Implies(IC[19], D7[5] > D12[7]))
s.add(Implies(IC[20], D6[7] > D12[1]))
s.add(Implies(IC[21], D3[1] > D8[7]))
s.add(Implies(IC[22], D7[5] > D11[4]))
s.add(Implies(IC[23], D1[3] > D13[4]))
s.add(Implies(IC[24], D4[1] > D10[7]))
s.add(Implies(IC[25], D6[7] > D9[3]))
s.add(Implies(IC[26], D1[1] > D8[6]))
s.add(Implies(IC[27], D6[1] > D9[6]))
s.add(Implies(IC[28], D2[0] > D4[2]))
s.add(Implies(IC[29], D3[3] > D9[2]))
s.add(Implies(IC[30], D7[3] > D11[7]))
s.add(Implies(IC[31], D9[3] > D13[7]))
s.add(Implies(IC[32], D3[6] > D7[2]))
s.add(Implies(IC[33], D1[3] > D7[8]))
s.add(Implies(IC[34], D4[1] > D5[1]))
s.add(Implies(IC[35], D7[7] > D13[7]))
s.add(Implies(IC[36], D7[3] > D11[3]))
s.add(Implies(IC[37], D7[0] > D12[7]))
s.add(Implies(IC[38], D2[0] > D7[7]))
s.add(Implies(IC[39], D7[6] > D10[7]))
s.add(Implies(IC[40], D5[6] > D13[4]))
s.add(Implies(IC[41], D5[4] > D12[5]))
s.add(Implies(IC[42], D7[3] > D10[3]))
s.add(Implies(IC[43], D4[1] > D7[0]))
s.add(Implies(IC[44], D7[0] > D12[2]))
s.add(Implies(IC[45], D5[7] > D10[3]))
s.add(Implies(IC[46], D3[3] > D5[2]))
s.add(Implies(IC[47], D2[4] > D12[1]))
s.add(Implies(IC[48], D2[1] > D12[0]))
s.add(Implies(IC[49], D4[6] > D13[5]))
s.add(Implies(IC[50], D6[0] > D11[5]))
s.add(Implies(IC[51], D6[4] > D7[0]))
s.add(Implies(IC[52], D4[0] > D7[6]))
s.add(Implies(IC[53], D2[1] > D11[5]))
s.add(Implies(IC[54], D1[0] > D9[3]))
s.add(Implies(IC[55], D6[7] > D11[6]))
s.add(Implies(IC[56], D2[4] > D8[0]))
s.add(Implies(IC[57], D8[4] > D10[5]))
s.add(Implies(IC[58], D3[0] > D11[1]))
s.add(Implies(IC[59], D7[2] > D13[3]))
s.add(Implies(IC[60], D4[4] > D8[0]))
s.add(Implies(IC[61], D1[7] > D6[4]))
s.add(Implies(IC[62], D2[4] > D3[0]))
s.add(Implies(IC[63], D1[5] > D4[1]))
s.add(Implies(IC[64], D3[4] > D9[7]))
s.add(Implies(IC[65], D3[4] > D11[5]))
s.add(Implies(IC[66], D4[1] > D10[2]))
s.add(Implies(IC[67], D4[4] > D7[3]))
s.add(Implies(IC[68], D8[0] > D11[7]))
s.add(Implies(IC[69], D7[8] > D11[1]))
s.add(Implies(IC[70], D5[3] > D6[7]))
s.add(Implies(IC[71], D8[2] > D10[1]))
s.add(Implies(IC[72], D2[5] > D13[6]))
s.add(Implies(IC[73], D1[5] > D7[4]))
s.add(Implies(IC[74], D3[6] > D11[6]))
s.add(Implies(IC[75], D5[5] > D12[3]))
s.add(Implies(IC[76], D6[0] > D9[4]))
s.add(Implies(IC[77], D7[0] > D13[7]))
s.add(Implies(IC[78], D3[7] > D9[1]))
s.add(Implies(IC[79], D9[2] > D10[2]))
s.add(Implies(IC[80], D9[2] > D13[2]))
s.add(Implies(IC[81], D3[3] > D7[7]))
s.add(Implies(IC[82], D1[5] > D10[4]))
s.add(Implies(IC[83], D7[2] > D13[5]))
s.add(Implies(IC[84], D3[3] > D7[8]))
s.add(Implies(IC[85], D9[5] > D12[2]))
s.add(Implies(IC[86], D5[1] > D10[6]))
s.add(Implies(IC[87], D8[5] > D13[3]))
s.add(Implies(IC[88], D4[7] > D9[7]))
s.add(Implies(IC[89], D1[6] > D9[1]))
s.add(Implies(IC[90], D2[0] > D6[0]))
s.add(Implies(IC[91], D2[0] > D13[0]))
s.add(Implies(IC[92], D4[4] > D9[2]))
s.add(Implies(IC[93], D8[7] > D11[6]))
s.add(Implies(IC[94], D6[2] > D10[2]))
s.add(Implies(IC[95], D3[6] > D11[0]))
s.add(Implies(IC[96], D9[0] > D11[6]))
s.add(Implies(IC[97], D6[1] > D11[2]))
s.add(Implies(IC[98], D4[0] > D9[7]))
s.add(Implies(IC[99], D3[3] > D4[6]))
s.add(Implies(IC[100], D11[4] > D12[1]))
s.add(Implies(IC[101], D3[5] > D8[1]))
s.add(Implies(IC[102], D2[1] > D5[5]))
s.add(Implies(IC[103], D2[3] > D8[5]))
s.add(Implies(IC[104], D3[0] > D8[2]))
s.add(Implies(IC[105], D8[2] > D12[4]))
s.add(Implies(IC[106], D4[6] > D9[1]))
s.add(Implies(IC[107], D9[2] > D13[3]))
s.add(Implies(IC[108], D6[1] > D11[5]))
s.add(Implies(IC[109], D2[0] > D13[4]))
s.add(Implies(IC[110], D3[2] > D9[5]))
s.add(Implies(IC[111], D1[2] > D3[6]))
s.add(Implies(IC[112], D2[5] > D13[5]))
s.add(Implies(IC[113], D4[2] > D12[5]))
s.add(Implies(IC[114], D4[4] > D10[5]))
s.add(Implies(IC[115], D2[4] > D10[6]))
s.add(Implies(IC[116], D3[3] > D11[7]))
s.add(Implies(IC[117], D3[5] > D6[2]))
s.add(Implies(IC[118], D1[4] > D3[5]))
s.add(Implies(IC[119], D6[0] > D12[5]))
s.add(Implies(IC[120], D4[7] > D7[3]))
s.add(Implies(IC[121], D2[2] > D7[1]))
s.add(Implies(IC[122], D10[0] > D12[0]))
s.add(Implies(IC[123], D1[4] > D2[6]))
s.add(Implies(IC[124], D7[0] > D8[5]))
s.add(Implies(IC[125], D7[7] > D11[3]))
s.add(Implies(IC[126], D2[5] > D11[6]))
s.add(Implies(IC[127], D4[1] > D10[3]))
s.add(Implies(IC[128], D2[3] > D6[7]))
s.add(Implies(IC[129], D1[6] > D6[5]))
s.add(Implies(IC[130], D9[7] > D12[5]))
s.add(Implies(IC[131], D6[4] > D13[6]))
s.add(Implies(IC[132], D7[5] > D12[1]))
s.add(Implies(IC[133], D4[2] > D9[5]))
s.add(Implies(IC[134], D9[5] > D11[7]))
s.add(Implies(IC[135], D5[4] > D12[3]))
s.add(Implies(IC[136], D5[7] > D7[3]))
s.add(Implies(IC[137], D9[6] > D13[7]))
s.add(Implies(IC[138], D5[0] > D9[7]))
s.add(Implies(IC[139], D1[6] > D10[4]))
s.add(Implies(IC[140], D7[2] > D12[6]))
s.add(Implies(IC[141], D2[6] > D10[5]))
s.add(Implies(IC[142], D2[5] > D11[5]))
s.add(Implies(IC[143], D5[2] > D6[7]))
s.add(Implies(IC[144], D4[3] > D7[1]))
s.add(Implies(IC[145], D2[3] > D12[1]))
s.add(Implies(IC[146], D8[7] > D12[7]))
s.add(Implies(IC[147], D1[7] > D9[1]))
s.add(Implies(IC[148], D9[0] > D13[2]))
s.add(Implies(IC[149], D8[5] > D10[0]))
s.add(Implies(IC[150], D6[4] > D11[2]))
s.add(Implies(IC[151], D1[1] > D4[5]))
s.add(Implies(IC[152], D3[6] > D10[5]))
s.add(Implies(IC[153], D2[6] > D9[1]))
s.add(Implies(IC[154], D2[5] > D5[7]))
s.add(Implies(IC[155], D5[5] > D12[7]))

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
