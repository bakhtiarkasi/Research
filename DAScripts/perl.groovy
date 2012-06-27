import org.neo4j.api.core.*

def beginDate = Date.parse("MMM-dd-yyyy", "Dec-21-2008");
def endDate = Date.parse("MMM-dd-yyyy", "Feb-13-2010");

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-1.3/data/db/perl');

def m =[:];
f.idx(T.e)[[label:'PARENT']].outV.groupCount(m).iterate();
def n = m.sort{a,b -> a.value <=> b.value};   

for( i in m)          
  if( i.getValue() == 1)
    n.remove(i.getKey());

n = n.sort({ a, b -> a.outE[[label:'COMMITTER']].when.next() <=> b.outE[[label:'COMMITTER']].when.next()} as Comparator)
m = n.clone();

for(i in m)
{
    def d = new Date(0);
    d = Date.parse("yyyy-MM-dd'T'HH:mm:ssZ",i.getKey().outE[[label:'COMMITTER']].when.next());

    //if(d.minus(endDate) > 0)
       //n.remove(i.getKey());
}

j = 0;  
for(i in n[0..n.size()])
{
  def d = new Date(0);
  d = Date.parse("yyyy-MM-dd'T'HH:mm:ssZ",i.getKey().outE[[label:'COMMITTER']].when.next());

  if(d.minus(beginDate) < 0)
       j++;

println i.getKey().hash;
}

//println j;

f.shutdown()
