import org.neo4j.api.core.*

try {
  


f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Voldemort');

def branchPrefinx = 'b';
def branchNumber = 2;

//getting all commits
def allCommits = [];
f.idx('vertices')[[type:'COMMIT']].fill(allCommits);
allCommits = allCommits.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)
  println allCommits.size();

//getting all branches
def temp = [:];
f.idx('edges')[[label:'COMMIT_PARENT']].inV.groupCount(temp).iterate();
def branches = temp.sort{a,b -> a.value <=> b.value};

for( i in temp)          
  if( i.getValue() == 1)
    branches.remove(i.getKey());

f.startTransaction();

branches = branches.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)
temp = branches.clone();


//starting from the first commit label all commits that is not a branch or merge.
for(i in allCommits)
{
  println i.hash;
  if(i.out('COMMIT_PARENT').count() == 0)
  {
    firstCommit = i;
    break;
  }
}
labelUntilMergeorBranch(firstCommit, "b1");



//label every commit that starts with a branch and ends in a branch or merge
for(i in temp)
{
    def children = [];
    i.getKey().in('COMMIT_PARENT').fill(children);
    children = children.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)
    
    /// for(j in i.getKey().in('COMMIT_PARENT')) it works!!

    def branchName = branchPrefinx + branchNumber;  
    if(i.getKey().isMerge == false)
    {
       branchName = 'd' + branchNumber;
       i.getKey().setProperty('branch', branchName);
       branchNumber++;
    }

    //* println i.getKey().hash + " " + i.getKey().branch;
    
    for(j in children)
    {
      //d = new Date(j.outE('COMMITTER').when.next() * 1000);
      //*println "children: "
      //*println j.hash + " " + j.branch;
      branchName = branchPrefinx + branchNumber;

      //continue to label until you see a merge
      bLabel = labelUntilMergeorBranch(j, branchName);
      
      if(bLabel)
        branchNumber++;
    }
    //*println "";
}



//getting all merges
def m =[:];
f.idx('edges')[[label:'COMMIT_PARENT']].outV.groupCount(m).iterate();
def merges = m.sort{a,b -> a.value <=> b.value};

for( i in m)          
  if( i.getValue() == 1)
    merges.remove(i.getKey());   

merges = merges.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)


//for each merge get its first child, see if not labelled and label this as a seperate branch.
// done for all those commit who lie between 2 merges (parent = merge, child=merge)
branchNumber = 1;
for(i in merges)
{
  branchName = 'c' + branchNumber; 

  if(i.getKey().in('COMMIT_PARENT').count() == 1)
  {
      branchName = 'c' + branchNumber;
      childSP = i.getKey().in('COMMIT_PARENT').next();
      bLaballed = labelUntilMergeorBranch(childSP, branchName);
      
      if(bLaballed)
        branchNumber++; 
  }
  //println i.getKey().hash + " " + branchId;
}

//label all merges
for(i in merges)
{
  branchId = 'm';
  bContinue = true;

  def children = [];
  i.getKey().out('COMMIT_PARENT').fill(children);
  children = children.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)

  for(j in children)
  {
    branchId += j.branch;
    
    if(j.branch == null)
    {
        bContinue = false;
    }
  }

  if(bContinue)
    i.getKey().setProperty('branch', branchId);
  
  //println i.getKey().hash + " " + branchId;
}



println "Failuresssss";
//for all those committs that we were not able to label
for(i in allCommits)
{
  def d = new Date(f.idx('vertices')[[hash:'8e088b98d72838606656cd543dfe74f157fe0d7b']].outE('COMMITTER').when.next()*1000);
  def d2 = new Date(i.outE('COMMITTER').when.next()*1000);
  if(d.minus(d2) <= 3 && d.minus(d2) >= 0)
     println i.branch + " " + d2;


  //if(i.branch != null)
    //println i.hash + " " + i.branch + " " + i.in('COMMIT_PARENT').count();
}
println "";

/*
for(i in f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches('d.*')}.branch.unique())
{
  nnn = '';
  nnn = i;

  def children = [];
  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(nnn)}.fill(children);
  children = children.sort({ a, b -> a.outE('').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator);

  brnhStart = children[0];
  brnhEnd = children[children.size()-1];
  //in hours
  lenghtTime = (brnhEnd.outE('AUTHOR').when.next()-brnhStart.outE('AUTHOR').when.next()) /3600; 
  lengthCommits = children.size();
  noAuthors = f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(nnn)}.out('AUTHOR').string.unique().count();
  noFiles = f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(nnn)}.out('CHANGED').token.unique().count();

  println brnhStart.hash + " " + nnn + " " + lengthCommits + " " + noFiles  + " " + noAuthors + " " + lenghtTime;
  
}
*/

 //nnn = 'b51';
 //println f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(nnn)}.map.next();

j = 0;

for(i in allCommits[0..allCommits.size()-1])
{
  d = new Date(i.outE('COMMITTER').when.next() * 1000);
}

for(i in branches[0..branches.size()])
{
    //println i.getKey().hash + ":" + i.getKey().outE('COMMITTER').inV.string.next();
}

println "--------------------------------------------------------------\r";

for(i in branches[0..branches.size()])
{
  //def d = new Date(0);
  //d = new Date(i.getKey().outE('COMMITTER').when.next());

  //if(d.minus(beginDate) < 0)
    //   j++;

//println i.getKey().hash + ":" + i.getKey().outE('CHANGED').inV.token.toList(); 

}
println "--------------------------------------------------------------\r";
//println branches.size();



revMerges = merges.sort({ a, b -> b.outE('COMMITTER').when.next() <=> a.outE('COMMITTER').when.next()} as Comparator)

//for 32 merges only

for(i in revMerges[0..60])
{
  def parentBranch = [];
  i.getKey().out('COMMIT_PARENT').branch.fill(parentBranch);
  
  def minDate = getMinDateforBranch(parentBranch[0]);
  def maxDate = i.getKey().outE('COMMITTER').when.next();
  
  //min date is the very first commit on either of the branches
  if(getMinDateforBranch(parentBranch[1]) < minDate)
      minDate = getMinDateforBranch(parentBranch[1]);

  def d1 = new Date(minDate*1000);
  def d2 = new Date(maxDate*1000);


  println "Merge: " + i.getKey().branch + " hash: " + i.getKey().hash; //+ " authors: " +  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(parentBranch[0]+'|'+parentBranch[1])}.out('AUTHOR').string.unique().count() + " committs " + f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(parentBranch[0]+'|'+parentBranch[1])}.count() + " files " + f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(parentBranch[0]+'|'+parentBranch[1])}.out('CHANGED').token.unique().count() + " days " + d2.minus(d1);
  
  def otherBranches = [];
  otherBranches = f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches('[b|c].*') && !(it.branch.matches(parentBranch[0]+'|'+parentBranch[1]))  && ( maxDate >= it.outE('COMMITTER').when.next()) && (it.outE('COMMITTER').when.next() >= minDate )}.branch.unique().toList();
  //println otherBranches.size();

  tempBranches = otherBranches.clone();
  for(j in otherBranches)
  {
    def maxActiveBranch = getMaxDateforBranch(j);
    
    //println maxActiveBranch +" : "+ maxDate;
    if(maxActiveBranch > maxDate)
      tempBranches.remove(j);   
  }
  
  otherBranches = tempBranches.clone();
  for(j in tempBranches)
  {
    firstHash = getFirstHashforBranch(j);
    def dateRef = f.idx('vertices')[[hash:firstHash]].outE('COMMITTER').when.next();
    
    noOfPaths = f.idx('vertices')[[hash:i.getKey().hash]].out('COMMIT_PARENT').loop(1){it.object.outE('AUTHOR').when.next() > dateRef }.filter{it.outE('AUTHOR').when.next()==dateRef}.path.count();
    //println f.idx('vertices')[[hash:'32aa7d58d3fd7c2132034b9cbd3d80ebf4b7d3e2']].out('COMMIT_PARENT').loop(1){it.object.outE('AUTHOR').when.next() > 1339464159 }.filter{it.outE('AUTHOR').when.next()==1339464159}.path{it.outE('AUTHOR').when.next()>1339464159}.size();
    if(noOfPaths == 0)
      otherBranches.remove(j); 
  }

  def k = '';
  for(j in otherBranches)
  {
    k = k + j;
    if(j != otherBranches[otherBranches.size()-1])
      k = k + '|';
  }

  if(otherBranches.size() > 0)
  {
    //println k;
  println "branches: " + otherBranches.size() ;//+ " authors: " +  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(k)}.out('AUTHOR').string.unique().count() + " committs " + f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(k)}.count() + " files " + f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(k)}.out('CHANGED').token.unique().count();

  }
 
}



f.stopTransaction(TransactionalGraph.Conclusion.FAILURE)

}
catch(Exception e) {
  f.stopTransaction(TransactionalGraph.Conclusion.FAILURE)

  println e;
}
finally
{
f.shutdown()
}


def labelUntilMergeorBranch (v, branchName) 
{
  bLaballed = false;
  //*println "sub children:"
  while(v.isMerge == false && v.in('COMMIT_PARENT').count() <= 1)
  {
    v.setProperty('branch', branchName);
    //*println v.hash + " " + v.branch;
    if(v.in('COMMIT_PARENT').count() == 0)
      return false;  
    v = v.in('COMMIT_PARENT').next();
    bLaballed = true;
  }

  return bLaballed;
}


def getMinDateforBranch (branchName) 
{

  def branchDate = [];
  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(branchName)}.outE('COMMITTER').when.fill(branchDate);
  branchDate = branchDate.sort({ a, b -> a <=> b} as Comparator)
  return branchDate[0];
}


def getMaxDateforBranch (branchName) 
{

  def branchDate = [];
  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(branchName)}.outE('COMMITTER').when.fill(branchDate);
  branchDate = branchDate.sort({ a, b -> b <=> a} as Comparator)
  return branchDate[0];
}

def getFirstHashforBranch(branchName)
{
  def children = [];
  f.idx('vertices')[[type:'COMMIT']].filter{it.branch.matches(branchName)}.fill(children);
  children = children.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator);
  return children[0].hash;
}