import org.neo4j.api.core.*

try {

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

    //match files that ends in .java or .clj
    f.idx('vertices')[[type:'NAME']].filter{it.getProperty('name').matches(j)}.in('NAME').in('AUTHOR').filter{it.isMerge.equals(false)}.out('CHANGED').filter{it.token.matches('.*[java|clj]$')}.token.groupCount(files).iterate();
    
    //sort file in descending order of count
    files = files.sort{a,b -> b.value <=> a.value};

    println j;
    println "-----------------------------------------";
    
    for(i in files)
      println i.getKey() + " : " + i.getValue();

    print "=========================================\n";
  }

}
catch(Exception e) {
  println e;
}
finally
{
f.shutdown()
}
