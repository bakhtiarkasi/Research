import org.neo4j.api.core.*

try {
  


//f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Voldemort');

def allCommits = [8,3,2,1,4,7];
allCommits = allCommits.sort({ a, b -> b <=> a} as Comparator)

for(i in allCommits)
{
  println i;
}
}
catch(Exception e) {
  //f.stopTransaction(TransactionalGraph.Conclusion.FAILURE)
  println e;
}
finally
{
//f.shutdown()
}


