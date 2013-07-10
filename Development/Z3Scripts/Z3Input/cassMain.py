from z3 import *

def distance(D,m):
	dis = Int('dis')
	dis = 0
	for i in range(0,len(D)):
		if is_true(simplify(m.evaluate(D[i]) != (i+1))):
                    dis += 1
	return dis


def getResultforDev(D,m):
    result = ''
    for i in range(0,len(D)):
	result += "%s" % m.evaluate(D[i])
	result += ' '
    return result.rstrip()
	
def abs(a):
	return If(a >= 0, a, -a) 

def getGoalFor(D):
	return simplify(sum(If(D[i] == (i+1), 0, 1) for i in range(0,len(D))))


#delete DC's from unsat core see if sat?
#if unsat delete the IC's and check if sat
def relaxConstraints(s,DC,IC):
	indxDC = []
	indxIC = []
	print 'uncore list'
	print s.unsat_core()
	
	for i in range(0,len(DC)):
		if is_true(simplify(If(DC[i] in s.unsat_core(), True,False))):
			indxDC.append(i)
	
	for i in range(0,len(IC)):
		if is_true(simplify(If(IC[i] in s.unsat_core(), True,False))):
			indxIC.append(i)
		
	if len(indxDC) > 0:
		deleteIndices(DC, indxDC)
		while s.check(DC+IC) == unsat:
			relaxConstraints(s, DC,IC)
	
	elif len(indxIC) > 0:
		deleteIndices(IC, indxIC)
		while s.check(DC+IC) == unsat:
			relaxConstraints(s,DC,IC)
				

#Approach 2:
# delete contraints based on their weights
def relaxConstraints2(s,DC,IC):
	indxDC = []
	indxIC = []
	print 'uncore list'
	print s.unsat_core()
	
	for i in range(0,len(DC)):
		if is_true(simplify(If(DC[i] in s.unsat_core(), True,False))):
			indxDC.append(i)
	
	for i in range(0,len(IC)):
		if is_true(simplify(If(IC[i] in s.unsat_core(), True,False))):
			indxIC.append(i)
		
	if len(indxDC) > 0:
		deleteIndices2(DC, indxDC)
		while s.check(DC+IC) == unsat:
			relaxConstraints2(s, DC,IC)
	
	elif len(indxIC) > 0:
		deleteIndices2(IC, indxIC)
		while s.check(DC+IC) == unsat:
			relaxConstraints2(s,DC,IC)


#make sure to delete indices in descending order
def deleteIndices(lst, indices):
	indices.sort()
	indices.reverse()
	
	for i in indices:
		del lst[i]
	
	del indices
	
#for approach 2
def deleteIndices2(lst, indices):
	indices.sort()
	indices.reverse()
	x = len(indices) / 2;
	
	if x == 0:
		x = 1
	
	for i in indices[0:x]:
		del lst[i]
	
	del indices

def getSolver():
	return Then(With('simplify', blast_distinct=True, arith_lhs=True), 
  	'normalize-bounds',
         'lia2pb',
         'pb2bv',
         'bit-blast',
         'smt').solver()

def getSolverforOpt():
	return Then(With('simplify', blast_distinct=True, arith_lhs=True), 
  	'normalize-bounds',
    'lia2pb',
    'bit-blast',
    'smt').solver()  	