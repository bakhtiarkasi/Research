import org.neo4j.api.core.*

try {

def beginDate = Date.parse("MMM-dd-yyyy", "Dec-21-2008");
def endDate = Date.parse("MMM-dd-yyyy", "Feb-13-2010");

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Storm');

def authors =[];

//get all authors
f.idx('vertices')[[type:'NAME']].name.fill(authors);
authors = authors.sort({ a, b -> a <=> b } as Comparator)

def files =[:]; 

//for each author get files count
for(j in authors)
  {
    files=[:]
    f.idx('vertices')[[type:'NAME']].filter{it.getProperty('name').matches(j)}.in('NAME').in('AUTHOR').filter{it.isMerge.equals(false)}.out('CHANGED').filter{it.token.matches('.*java$')}.token.groupCount(files).iterate();
    println j;
    
    for(i in files)
      println i.getKey() + " : " + i.getValue();

    println "****************************************\n\n";
  }

}
catch(Exception e) {
  println e;
}
finally
{
f.shutdown()
}
