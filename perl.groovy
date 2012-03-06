import org.neo4j.api.core.*

def beginDate = Date.parse("MMM-dd-yyyy", "Dec-12-2008");
def endDate = Date.parse("MMM-dd-yyyy", "Aug-12-2011");

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-1.3/data/db/perl');

def m =[:];
f.idx(T.e)[[label:'PARENT']].outV.groupCount(m).iterate();
def n = m.sort{a,b -> a.value <=> b.value};   

for( i in m)          
  if( i.getValue() == 1)
    n.remove(i.getKey());

m = n.clone();

for(i in m)
{
    def d = new Date(0);
    d = Date.parse("yyyy-MM-dd'T'HH:mm:ssZ",i.getKey().outE[[label:'COMMITTER']].when.next());

    if(d.minus(beginDate) < 0)
       n.remove(i.getKey());
}

for(i in n)
  println i.getKey().hash;

f.shutdown()
