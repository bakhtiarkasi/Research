
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
	result += 'D14 -> ['
	result += getResultforDev(D14,m)
	result +=  ']\n'
	result += 'D15 -> ['
	result += getResultforDev(D15,m)
	result +=  ']\n'
	result += 'D16 -> ['
	result += getResultforDev(D16,m)
	result +=  ']\n'
	result += 'D17 -> ['
	result += getResultforDev(D17,m)
	result +=  ']\n'
	result += 'D18 -> ['
	result += getResultforDev(D18,m)
	result +=  ']\n'
	result += 'D19 -> ['
	result += getResultforDev(D19,m)
	result +=  ']\n'
	result += 'D20 -> ['
	result += getResultforDev(D20,m)
	result +=  ']\n'
	result += 'D21 -> ['
	result += getResultforDev(D21,m)
	result +=  ']\n'
	result += 'D22 -> ['
	result += getResultforDev(D22,m)
	result +=  ']\n'
	result += 'D23 -> ['
	result += getResultforDev(D23,m)
	result +=  ']\n'
	result += 'D24 -> ['
	result += getResultforDev(D24,m)
	result +=  ']\n'
	result += 'D25 -> ['
	result += getResultforDev(D25,m)
	result +=  ']\n'
	result += 'D26 -> ['
	result += getResultforDev(D26,m)
	result +=  ']\n'
	result += 'D27 -> ['
	result += getResultforDev(D27,m)
	result +=  ']\n'
	result += 'D28 -> ['
	result += getResultforDev(D28,m)
	result +=  ']\n'
	result += 'D29 -> ['
	result += getResultforDev(D29,m)
	result +=  ']\n'
	result += 'D30 -> ['
	result += getResultforDev(D30,m)
	result +=  ']\n'
	result += 'D31 -> ['
	result += getResultforDev(D31,m)
	result +=  ']\n'
	result += 'D32 -> ['
	result += getResultforDev(D32,m)
	result +=  ']\n'
	result += 'D33 -> ['
	result += getResultforDev(D33,m)
	result +=  ']\n'
	result += 'D34 -> ['
	result += getResultforDev(D34,m)
	result +=  ']\n'
	result += 'D35 -> ['
	result += getResultforDev(D35,m)
	result +=  ']\n'
	result += 'D36 -> ['
	result += getResultforDev(D36,m)
	result +=  ']\n'
	result += 'D37 -> ['
	result += getResultforDev(D37,m)
	result +=  ']\n'
	return result

def getGoal():
	return simplify( getGoalFor(D1) + getGoalFor(D2) + getGoalFor(D3) + getGoalFor(D4) + getGoalFor(D5) + getGoalFor(D6) + getGoalFor(D7) + getGoalFor(D8) + getGoalFor(D9) + getGoalFor(D10) + getGoalFor(D11) + getGoalFor(D12) + getGoalFor(D13) + getGoalFor(D14) + getGoalFor(D15) + getGoalFor(D16) + getGoalFor(D17) + getGoalFor(D18) + getGoalFor(D19) + getGoalFor(D20) + getGoalFor(D21) + getGoalFor(D22) + getGoalFor(D23) + getGoalFor(D24) + getGoalFor(D25) + getGoalFor(D26) + getGoalFor(D27) + getGoalFor(D28) + getGoalFor(D29) + getGoalFor(D30) + getGoalFor(D31) + getGoalFor(D32) + getGoalFor(D33) + getGoalFor(D34) + getGoalFor(D35) + getGoalFor(D36) + getGoalFor(D37) )

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
	total = total + distance(D14, m)
	total = total + distance(D15, m)
	total = total + distance(D16, m)
	total = total + distance(D17, m)
	total = total + distance(D18, m)
	total = total + distance(D19, m)
	total = total + distance(D20, m)
	total = total + distance(D21, m)
	total = total + distance(D22, m)
	total = total + distance(D23, m)
	total = total + distance(D24, m)
	total = total + distance(D25, m)
	total = total + distance(D26, m)
	total = total + distance(D27, m)
	total = total + distance(D28, m)
	total = total + distance(D29, m)
	total = total + distance(D30, m)
	total = total + distance(D31, m)
	total = total + distance(D32, m)
	total = total + distance(D33, m)
	total = total + distance(D34, m)
	total = total + distance(D35, m)
	total = total + distance(D36, m)
	total = total + distance(D37, m)
	return total

###end method sections

#initialize the solver
s = Solver()

##Developers Vectors
D1 = IntVector('D1', 1) 
D2 = IntVector('D2', 1) 
D3 = IntVector('D3', 1) 
D4 = IntVector('D4', 4) 
D5 = IntVector('D5', 6) 
D6 = IntVector('D6', 1) 
D7 = IntVector('D7', 1) 
D8 = IntVector('D8', 1) 
D9 = IntVector('D9', 1) 
D10 = IntVector('D10', 3) 
D11 = IntVector('D11', 1) 
D12 = IntVector('D12', 1) 
D13 = IntVector('D13', 5) 
D14 = IntVector('D14', 1) 
D15 = IntVector('D15', 1) 
D16 = IntVector('D16', 1) 
D17 = IntVector('D17', 1) 
D18 = IntVector('D18', 2) 
D19 = IntVector('D19', 1) 
D20 = IntVector('D20', 27) 
D21 = IntVector('D21', 75) 
D22 = IntVector('D22', 4) 
D23 = IntVector('D23', 2) 
D24 = IntVector('D24', 2) 
D25 = IntVector('D25', 4) 
D26 = IntVector('D26', 3) 
D27 = IntVector('D27', 10) 
D28 = IntVector('D28', 6) 
D29 = IntVector('D29', 1) 
D30 = IntVector('D30', 10) 
D31 = IntVector('D31', 3) 
D32 = IntVector('D32', 1) 
D33 = IntVector('D33', 1) 
D34 = IntVector('D34', 1) 
D35 = IntVector('D35', 4) 
D36 = IntVector('D36', 2) 
D37 = IntVector('D37', 4) 

##Adding hard constraints
valueConst = [1 <= D1[0], D1[0] <= 1,\
		1 <= D2[0], D2[0] <= 1,\
		1 <= D3[0], D3[0] <= 1,\
		1 <= D4[0], D4[0] <= 4,1 <= D4[1], D4[1] <= 4,1 <= D4[2], D4[2] <= 4,1 <= D4[3], D4[3] <= 4,\
		1 <= D5[0], D5[0] <= 6,1 <= D5[1], D5[1] <= 6,1 <= D5[2], D5[2] <= 6,1 <= D5[3], D5[3] <= 6,1 <= D5[4], D5[4] <= 6,1 <= D5[5], D5[5] <= 6,\
		1 <= D6[0], D6[0] <= 1,\
		1 <= D7[0], D7[0] <= 1,\
		1 <= D8[0], D8[0] <= 1,\
		1 <= D9[0], D9[0] <= 1,\
		1 <= D10[0], D10[0] <= 3,1 <= D10[1], D10[1] <= 3,1 <= D10[2], D10[2] <= 3,\
		1 <= D11[0], D11[0] <= 1,\
		1 <= D12[0], D12[0] <= 1,\
		1 <= D13[0], D13[0] <= 5,1 <= D13[1], D13[1] <= 5,1 <= D13[2], D13[2] <= 5,1 <= D13[3], D13[3] <= 5,1 <= D13[4], D13[4] <= 5,\
		1 <= D14[0], D14[0] <= 1,\
		1 <= D15[0], D15[0] <= 1,\
		1 <= D16[0], D16[0] <= 1,\
		1 <= D17[0], D17[0] <= 1,\
		1 <= D18[0], D18[0] <= 2,1 <= D18[1], D18[1] <= 2,\
		1 <= D19[0], D19[0] <= 1,\
		1 <= D20[0], D20[0] <= 27,1 <= D20[1], D20[1] <= 27,1 <= D20[2], D20[2] <= 27,1 <= D20[3], D20[3] <= 27,1 <= D20[4], D20[4] <= 27,1 <= D20[5], D20[5] <= 27,1 <= D20[6], D20[6] <= 27,1 <= D20[7], D20[7] <= 27,1 <= D20[8], D20[8] <= 27,1 <= D20[9], D20[9] <= 27,1 <= D20[10], D20[10] <= 27,1 <= D20[11], D20[11] <= 27,1 <= D20[12], D20[12] <= 27,1 <= D20[13], D20[13] <= 27,1 <= D20[14], D20[14] <= 27,1 <= D20[15], D20[15] <= 27,1 <= D20[16], D20[16] <= 27,1 <= D20[17], D20[17] <= 27,1 <= D20[18], D20[18] <= 27,1 <= D20[19], D20[19] <= 27,1 <= D20[20], D20[20] <= 27,1 <= D20[21], D20[21] <= 27,1 <= D20[22], D20[22] <= 27,1 <= D20[23], D20[23] <= 27,1 <= D20[24], D20[24] <= 27,1 <= D20[25], D20[25] <= 27,1 <= D20[26], D20[26] <= 27,\
		1 <= D21[0], D21[0] <= 75,1 <= D21[1], D21[1] <= 75,1 <= D21[2], D21[2] <= 75,1 <= D21[3], D21[3] <= 75,1 <= D21[4], D21[4] <= 75,1 <= D21[5], D21[5] <= 75,1 <= D21[6], D21[6] <= 75,1 <= D21[7], D21[7] <= 75,1 <= D21[8], D21[8] <= 75,1 <= D21[9], D21[9] <= 75,1 <= D21[10], D21[10] <= 75,1 <= D21[11], D21[11] <= 75,1 <= D21[12], D21[12] <= 75,1 <= D21[13], D21[13] <= 75,1 <= D21[14], D21[14] <= 75,1 <= D21[15], D21[15] <= 75,1 <= D21[16], D21[16] <= 75,1 <= D21[17], D21[17] <= 75,1 <= D21[18], D21[18] <= 75,1 <= D21[19], D21[19] <= 75,1 <= D21[20], D21[20] <= 75,1 <= D21[21], D21[21] <= 75,1 <= D21[22], D21[22] <= 75,1 <= D21[23], D21[23] <= 75,1 <= D21[24], D21[24] <= 75,1 <= D21[25], D21[25] <= 75,1 <= D21[26], D21[26] <= 75,1 <= D21[27], D21[27] <= 75,1 <= D21[28], D21[28] <= 75,1 <= D21[29], D21[29] <= 75,1 <= D21[30], D21[30] <= 75,1 <= D21[31], D21[31] <= 75,1 <= D21[32], D21[32] <= 75,1 <= D21[33], D21[33] <= 75,1 <= D21[34], D21[34] <= 75,1 <= D21[35], D21[35] <= 75,1 <= D21[36], D21[36] <= 75,1 <= D21[37], D21[37] <= 75,1 <= D21[38], D21[38] <= 75,1 <= D21[39], D21[39] <= 75,1 <= D21[40], D21[40] <= 75,1 <= D21[41], D21[41] <= 75,1 <= D21[42], D21[42] <= 75,1 <= D21[43], D21[43] <= 75,1 <= D21[44], D21[44] <= 75,1 <= D21[45], D21[45] <= 75,1 <= D21[46], D21[46] <= 75,1 <= D21[47], D21[47] <= 75,1 <= D21[48], D21[48] <= 75,1 <= D21[49], D21[49] <= 75,1 <= D21[50], D21[50] <= 75,1 <= D21[51], D21[51] <= 75,1 <= D21[52], D21[52] <= 75,1 <= D21[53], D21[53] <= 75,1 <= D21[54], D21[54] <= 75,1 <= D21[55], D21[55] <= 75,1 <= D21[56], D21[56] <= 75,1 <= D21[57], D21[57] <= 75,1 <= D21[58], D21[58] <= 75,1 <= D21[59], D21[59] <= 75,1 <= D21[60], D21[60] <= 75,1 <= D21[61], D21[61] <= 75,1 <= D21[62], D21[62] <= 75,1 <= D21[63], D21[63] <= 75,1 <= D21[64], D21[64] <= 75,1 <= D21[65], D21[65] <= 75,1 <= D21[66], D21[66] <= 75,1 <= D21[67], D21[67] <= 75,1 <= D21[68], D21[68] <= 75,1 <= D21[69], D21[69] <= 75,1 <= D21[70], D21[70] <= 75,1 <= D21[71], D21[71] <= 75,1 <= D21[72], D21[72] <= 75,1 <= D21[73], D21[73] <= 75,1 <= D21[74], D21[74] <= 75,\
		1 <= D22[0], D22[0] <= 4,1 <= D22[1], D22[1] <= 4,1 <= D22[2], D22[2] <= 4,1 <= D22[3], D22[3] <= 4,\
		1 <= D23[0], D23[0] <= 2,1 <= D23[1], D23[1] <= 2,\
		1 <= D24[0], D24[0] <= 2,1 <= D24[1], D24[1] <= 2,\
		1 <= D25[0], D25[0] <= 4,1 <= D25[1], D25[1] <= 4,1 <= D25[2], D25[2] <= 4,1 <= D25[3], D25[3] <= 4,\
		1 <= D26[0], D26[0] <= 3,1 <= D26[1], D26[1] <= 3,1 <= D26[2], D26[2] <= 3,\
		1 <= D27[0], D27[0] <= 10,1 <= D27[1], D27[1] <= 10,1 <= D27[2], D27[2] <= 10,1 <= D27[3], D27[3] <= 10,1 <= D27[4], D27[4] <= 10,1 <= D27[5], D27[5] <= 10,1 <= D27[6], D27[6] <= 10,1 <= D27[7], D27[7] <= 10,1 <= D27[8], D27[8] <= 10,1 <= D27[9], D27[9] <= 10,\
		1 <= D28[0], D28[0] <= 6,1 <= D28[1], D28[1] <= 6,1 <= D28[2], D28[2] <= 6,1 <= D28[3], D28[3] <= 6,1 <= D28[4], D28[4] <= 6,1 <= D28[5], D28[5] <= 6,\
		1 <= D29[0], D29[0] <= 1,\
		1 <= D30[0], D30[0] <= 10,1 <= D30[1], D30[1] <= 10,1 <= D30[2], D30[2] <= 10,1 <= D30[3], D30[3] <= 10,1 <= D30[4], D30[4] <= 10,1 <= D30[5], D30[5] <= 10,1 <= D30[6], D30[6] <= 10,1 <= D30[7], D30[7] <= 10,1 <= D30[8], D30[8] <= 10,1 <= D30[9], D30[9] <= 10,\
		1 <= D31[0], D31[0] <= 3,1 <= D31[1], D31[1] <= 3,1 <= D31[2], D31[2] <= 3,\
		1 <= D32[0], D32[0] <= 1,\
		1 <= D33[0], D33[0] <= 1,\
		1 <= D34[0], D34[0] <= 1,\
		1 <= D35[0], D35[0] <= 4,1 <= D35[1], D35[1] <= 4,1 <= D35[2], D35[2] <= 4,1 <= D35[3], D35[3] <= 4,\
		1 <= D36[0], D36[0] <= 2,1 <= D36[1], D36[1] <= 2,\
		1 <= D37[0], D37[0] <= 4,1 <= D37[1], D37[1] <= 4,1 <= D37[2], D37[2] <= 4,1 <= D37[3], D37[3] <= 4]

distConst = [Distinct(D1), Distinct(D2), Distinct(D3), Distinct(D4), Distinct(D5), Distinct(D6), Distinct(D7), Distinct(D8), Distinct(D9), Distinct(D10), Distinct(D11), Distinct(D12), Distinct(D13), Distinct(D14), Distinct(D15), Distinct(D16), Distinct(D17), Distinct(D18), Distinct(D19), Distinct(D20), Distinct(D21), Distinct(D22), Distinct(D23), Distinct(D24), Distinct(D25), Distinct(D26), Distinct(D27), Distinct(D28), Distinct(D29), Distinct(D30), Distinct(D31), Distinct(D32), Distinct(D33), Distinct(D34), Distinct(D35), Distinct(D36), Distinct(D37)]

##Add constraints to solver s
s.add(valueConst)
s.add(distConst)

##Adding soft constraints
##Adding boolean vector for DC
DC = BoolVector('DC', 27)
IC = BoolVector('IC', 0)

##Adding soft constraints as assertions
##Adding Direct Conflicts
s.add(Implies(DC[0], D21[64] != D20[18]))
s.add(Implies(DC[1], D30[0] != D22[0]))
s.add(Implies(DC[2], D21[70] != D20[20]))
s.add(Implies(DC[3], D26[1] != D20[24]))
s.add(Implies(DC[4], D21[28] != D20[2]))
s.add(Implies(DC[5], D21[22] != D22[3]))
s.add(Implies(DC[6], D32[0] != D21[51]))
s.add(Implies(DC[7], D21[45] != D20[21]))
s.add(Implies(DC[8], D25[3] != D21[9]))
s.add(Implies(DC[9], D21[69] != D20[14]))
s.add(Implies(DC[10], D21[2] != D20[1]))
s.add(Implies(DC[11], D21[5] != D22[1]))
s.add(Implies(DC[12], D21[35] != D20[12]))
s.add(Implies(DC[13], D21[19] != D20[4]))
s.add(Implies(DC[14], D21[44] != D37[1]))
s.add(Implies(DC[15], D21[55] != D26[0]))
s.add(Implies(DC[16], D27[6] != D22[2]))
s.add(Implies(DC[17], D21[15] != D20[6]))
s.add(Implies(DC[18], D21[4] != D20[19]))
s.add(Implies(DC[19], D21[9] != D20[13]))
s.add(Implies(DC[20], D21[8] != D20[23]))
s.add(Implies(DC[21], D21[62] != D20[8]))
s.add(Implies(DC[22], D21[26] != D20[24]))
s.add(Implies(DC[23], D21[20] != D20[4]))
s.add(Implies(DC[24], D21[42] != D4[2]))
s.add(Implies(DC[25], D21[73] != D28[5]))
s.add(Implies(DC[26], D28[1] != D21[10]))

##Adding InDirect Conflicts

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
	relaxConstraints(s,DC,IC)
	m = s.model()
	print printResult(m)
else:
	print 'unknown'		
