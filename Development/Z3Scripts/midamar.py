from z3 import *


#s = Then(With('simplify', blast_distinct=True, arith_lhs=True), 
# 'normalize-bounds',  'lia2pb', 'bit-blast', 'smt').solver()

s = Solver();

A = IntVector('A', 2) # Alice Task's
B = RealVector('B', 2) # Bob Task's

# Task's must be assigned within range (1:3)
s.add([7 <= A[0], 1 <= A[1], 5.45 == B[0], 8 == B[1]])
s.add([(A[0]*B[0] + A[1]*B[1]) >= 100 ])
s.add([A[0] + A[1] <= 15 ])



isSat = s.check() # Check if all constraints are Satisfiable
if isSat == sat:
	print 'Solution is SAT'
	print s.model()
elif isSat == unsat:
	print 'UnSAT Scenario: relaxing constraints';
else:
	print 'unknown'		
