s = Solver() # initialize Z3 solver with appropriate config's

A = IntVector('A', 3) # 3 tasks assigned to Alice
B = IntVector('B', 3) # 3 tasks assigned to Bob

# Task's are assigned within valid range e.g. (T1 to T3)
s.add([1 <= A[0], A[0] <= 3,1 <= A[1], A[1] <= 3,1 <= A[2], A[2] <= 3,\
		1 <= B[0], B[0] <= 3,1 <= B[1], B[1] <= 3,1 <= B[2], B[2] <= 3])

s.add([Distinct(A), Distinct(B)]) # Assignment must be unique for each task

DC = BoolVector('DC', 1) # 1 Direct Conflicts
IC = BoolVector('IC', 3) # 3 Indirect Conflicts

# Adding conflicts as assertions
s.add(Implies(DC[0], A[0] != B[0])) # TA1:Rectangle.java <-> TB1:Rectangle.java 

s.add(Implies(IC[0], A[0] != B[0])) # TA1:Shape.java <-> TB1:Square.java
s.add(Implies(IC[1], A[0] != B[1])) # TA1:Shape.java <-> TB2:Traiangle.java
s.add(Implies(IC[2], A[1] < A[2]))  # TA2:Canvas.java <-> TA3:Panel.java

isSat = s.check(DC+IC) # Check if all constraints are Satisfiable
if isSat == sat:
	print getOptimizedSolution() # function for optimizing solution 
else: 
	relaxConstraints(s,DC,IC) # function for relaxing constraints 
	m = s.model()
	print printResult(m)
	
	


	
	