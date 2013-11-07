import org.neo4j.api.core.*

try {

voldemortDB = "/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Voldemort";
//voldemortDB = "/work/esquared/bkasi/Tools/Gremlin/data/Voldemort";

def mergeHashCode = a1;

f=new Neo4jGraph(voldemortDB);

def commits =[];
def files=[];

f.idx('vertices')[[hash:mergeHashCode]].out('COMMIT_PARENT').fill(commits);
commits = commits.sort({ a, b -> a.outE('AUTHOR').when.next() <=> b.outE('AUTHOR').when.next() } as Comparator)

println commits[0].out('AUTHOR').out('NAME').name.next();
files=[];

filesName = getFilesForBranch(commits[0]);

files = f.idx('vertices')[[type:'COMMIT']].filter{it.hash.matches(filesName)}.out('CHANGED').token.unique();

for(f in files)
	println f;

println "-------------------------------------------";

println commits[1].out('AUTHOR').out('NAME').name.next();

files=[];
filesName = getFilesForBranch(commits[1]);

files = f.idx('vertices')[[type:'COMMIT']].filter{it.hash.matches(filesName)}.out('CHANGED').token.unique();

for(f in files)
	println f;
}
catch(Exception e) {
  println e;
}
finally
{
f.shutdown()
}


def getFilesForBranch (v) 
{
 files=v.hash;
  
  if(v.isMerge == true)
  	return files;

  v = v.out('COMMIT_PARENT').next();
  while(v.isMerge == false && v.out('COMMIT_PARENT').count() <= 1)
  {
  	files += '|' + v.hash;

    if(v.out('COMMIT_PARENT').count() == 0)
      return files;  

    v = v.out('COMMIT_PARENT').next();
  }
  return files;
}
