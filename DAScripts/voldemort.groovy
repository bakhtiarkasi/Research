import org.neo4j.api.core.*

def beginDate = Date.parse("MMM-dd-yyyy", "Dec-21-2008");
def endDate = Date.parse("MMM-dd-yyyy", "Feb-13-2010");

f=new Neo4jGraph('/Users/bkasi/Documents/Research/gremlin-2.0/data/db/Voldemort');

def m =[:];
f.idx('edges')[[label:'COMMIT_PARENT']].outV.groupCount(m).iterate();
def n = m.sort{a,b -> a.value <=> b.value};   

for( i in m)          
  if( i.getValue() == 1)
    n.remove(i.getKey());


n = n.sort({ a, b -> a.outE('COMMITTER').when.next() <=> b.outE('COMMITTER').when.next()} as Comparator)
m = n.clone();

for(i in m)
{
    def d = new Date(0);
    d = new Date(i.getKey().outE('COMMITTER').when.next());

    //if(d.minus(endDate) > 0)
       //n.remove(i.getKey());
}

j = 0;  
for(i in n[0..n.size()])
{
  def d = new Date(0);
  d = new Date(i.getKey().outE('COMMITTER').when.next());

  if(d.minus(beginDate) < 0)
       j++;

println i.getKey().hash;
}

println j;

f.shutdown()
