import org.neo4j.api.core.*

def beginDate = Date.parse("yyyy-MM-dd", a1);
def endDate = Date.parse("yyyy-MM-dd", a2);

stormDb = "/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Storm";
stormDb = "/work/esquared/bkasi/Tools/Gremlin/data/Storm";



try {

      f=new Neo4jGraph(stormDb);

      def allCommits = [];
      f.idx('vertices')[[type:'COMMIT']].filter{endDate.minus(new Date(it.outE('COMMITTER').when.next()*1000)) >= 0 }.filter{beginDate.minus(new Date(it.outE('COMMITTER').when.next()*1000)) <= 0 }.fill(allCommits);
      allCommits = allCommits.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)

      for(i in allCommits)
      {
        println i.hash;
        //println new Date(i.outE('COMMITTER').when.next()*1000);
        println i.out('AUTHOR').out('NAME').name.next();
        
        files=[];
        files = i.out('CHANGED').token;

        for(f in files)
          println f;

        println "-------------------------------------------";

      }
    }
catch(Exception e) {
  println e;
}
finally
{
f.shutdown()
}
