from z3 import *

def fib(m):    # write Fibonacci series up to n
	total = 0;
	total = distance(D1, m)
	total = total + distance(D2, m)
	return total 


def goalDef():
	count = 0
	for i in range(0,len(D1)-1):
		if D1[i] != i+1:
			count = count + 1
	for i in range(0,len(D2)-1):
		if D2[i] != i+1:
			count = count + 1
	print 'count';
	print count;
	return count
	
	
	
	#j=1
	#k =0
	#for d in m.decls():
		#if d.name().find('D') == 0:
			#print 'Success'
			#a=d.name();
			#print m.evaluate(D1[0]);
			#count = distance(D1)
			#count = 
			
			#print a
			#print 'D' in a

def distance(D,m):
	count = 0
	for i in range(0,len(D)-1):
		if m.evaluate(D[i]) != i+1:
			count = count + 1
	return count

D1 = IntVector('D1', 2)
D2 = IntVector('D2', 3)

p1, p2, p3, p4 = Bools('p1 p2 p3 p4')



#initialize the solver add constraints
s = Solver()



# developer to tasks mapping (variable no of tasks ?, hashmap)
#s.add(And( 1 <= D1[0], D1[0] <= 2, 1 <= D1[1], D1[1] <= 2, D2[0] >= 1, D2[0] <= 2, D2[1] >= 1, D2[1] <= 2) )

dc = Implies(p2, D1[0] != D1[1])
ic = Implies(p3, D2[0] != D2[1])
#s.push();
#s.add(dc)
#s.push();
#s.add(ic);


# if satisfiabl3
if s.check(p1,p2,p3,p4) == sat:

     
     #get model for the solution
    m = s.model()
    print m
    
    total = fib(m);
    #s.push()
    
    dc1 = (1 <= D1[0], D1[0] <= 2, 1 <= D1[1], D1[1] <= 2, 1 <= D2[0], D2[0] <= 2, 1 <= D2[1], D2[1] <= 2, D1[0] != D1[1], D2[0] != D2[1], D1[1] > D2[2])
    
    s.add(dc1)
     
    print goalDef();
    
    print s.check();
    print s.unsat_core()
    
    if s.check() == sat:
		m = s.model()
		for d in m.decls():
			print "%s = %s" % (d.name(), m[d])
	
	

#else:
#	print s.unsat_core()

#s.pop()
#print s
#print s.check(p1,p2,p3)

#print s.check(p1,p2,p3)
#print s.model()


# Now call the function we just defined:
#print fib(2000)
