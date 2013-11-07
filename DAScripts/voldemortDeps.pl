use Understand;

  @filesArray = split ",", $ARGV[0];

  $dbPath = "/Users/bkasi/Documents/Research/DataAnalysis/Voldemort/voldemort.udb";
  $dbPath = "/Users/bkasi/Documents/Research/DataAnalysis/understand/voldemort.udb";
  
  $db = Understand::open($dbPath);

  foreach $fileName (@filesArray){
  	foreach $file ($db->lookup($fileName)) {
    
    	$deps = $file->depends();
    
      if(defined $deps)
      {
        foreach $val ($deps->keys()){
          if (not ($val->longname() =~ /\.class/)) {
    			   print $val->longname(),"\n";
          }
        }
  	  }
  }
} 

$db->close();