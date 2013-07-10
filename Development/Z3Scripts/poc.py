from z3 import *

IC = BoolVector('IC', 3)
x, y = Ints('x y')
s = Solver()
s.add(Implies(IC[0], x > 10),
      Implies(IC[0], y > x),
      Implies(IC[1], y < 5),
      Implies(IC[2], y > 0))
print s.check(IC)    
print s.unsat_core()
print IC

#for uc in s.unsat_core():
	#print simplify(uc == 'IC__1')
#	print simplify(uc == IC[1])
	#print is_true(uc == 'IC__1')
#	print is_true(uc == IC[1])
	#print is_true(simplify(uc == 'IC__1'))

##	print is_true(simplify(uc == IC[1]))
	

#	print IC.index(uc)

#	if is_true(uc == IC[0]):
#		print 1
#	else:
#		print 0
idx = []
for i in range(0,len(IC)):
	if is_true(simplify(If(IC[i] in s.unsat_core(), True,False))):
		idx.append(i)


print idx