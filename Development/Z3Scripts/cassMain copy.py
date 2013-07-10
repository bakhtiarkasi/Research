from z3 import *

def distance(D,m):
	dis = Int('dis')
	val = Int('val')
	dis = 0
	val = 0
	for i in range(0,len(D)):
		val = simplify(m.evaluate(D[i]) -(i+1) )
		if is_true(simplify(val < 0)):
			val = -val	
		dis = dis + val
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
	return (sum( (abs(D[i] - (i +1))) for i in range(0,len(D))))


#delete DC's from unsat core see if sat?
#if unsat delete the IC's and check if sat
def relaxConstraints(s,DC,IC):
	indxDC = []
	indxIC = []
	print s.unsat_core()
	for const in s.	unsat_core():
		name = "%s"% const
		if(name.find('DC__') == 0):
			if(len(DC) > 0):
				intVal = int(name[len('DC__'):]) 
				indxDC.append(intVal)
		
		elif(name.find('IC__') == 0):
			if(len(IC) > 0):
				intVal = int(name[len('IC__'):]) 
				indxIC.append(intVal)
		
	if len(indxDC) > 0:
		deleteIndices(DC, indxDC)
		while s.check(DC+IC) == unsat:
			relaxConstraints(s, DC,IC)
	
	elif len(indxIC) > 0:
		deleteIndices(IC, indxIC)
		while s.check(DC+IC) == unsat:
			relaxConstraints(s,DC,IC)
				

#make sure to delete indices in descending order
def deleteIndices(lst, indices):
	indices.sort()
	indices.reverse()
	
	for i in indices:
		del lst[i]
	
	del lst
		