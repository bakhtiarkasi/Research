import org.neo4j.api.core.*

try {

def beginDate = Date.parse("MMM-dd-yyyy", "Dec-21-2008");
def endDate = Date.parse("MMM-dd-yyyy", "Feb-13-2010");

def mergeHashCode = a1;

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Storm');

def commits =[];
def files=[];

f.idx('vertices')[[hash:mergeHashCode]].out('COMMIT_PARENT').fill(commits);

commits = commits.sort({ a, b -> a.outE('AUTHOR').when.next() <=> b.outE('AUTHOR').when.next() } as Comparator)

//for each author get files count
//pritnln "Server"
println commits[0].out('AUTHOR').out('NAME').name.next();
commits[0].out('CHANGED').token.fill(files);

for(f in files)
	println f;

//date = new Date(commits[0].outE('AUTHOR').when.next()*1000);
//println date;

println "-------------------------------------------";

println commits[1].out('AUTHOR').out('NAME').name.next();

files=[];
commits[1].out('CHANGED').token.fill(files);

for(f in files)
	println f;

//date = new Date(commits[1].outE('AUTHOR').when.next()*1000);
//println date;

}
catch(Exception e) {
  println e;
}
finally
{
f.shutdown()
}
