import org.neo4j.api.core.*

try {

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Voldemort');

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

    def median = 0.0;
    if(files.size() > 0)
    {
      allValues = files.values().toList();
      midNumber = (int)(files.size()/2)  
      median = files.size() % 2 != 0 ? allValues[midNumber] : (allValues[midNumber] + allValues[midNumber-1])/2;  
    }
    
    println j;
    println "-----------------------------------------";
    println "Median : " + median;
   
    for(i in files)
      //println j+ ",D"  + k.toString()  + "," + i.getValue() + " : " + median
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
