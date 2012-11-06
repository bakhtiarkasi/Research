#!/usr/bin/ruby
require 'date'

$commitsMap = Hash.new
$dateTemp = DateTime.now();
$workDir = "/Users/bkasi/Documents/Research/DataAnalysis/Voldemort";
$sortedList = Hash.new;

#defining the class object for git summary
class Commit
  def initialize()
  	parents = Hash.new;
  	files = [];
  	commitDate = DateTime.now;
	hashCode ="";
        remoteCode="";
  end
 
  def parents
	return @parents
  end
  def parents=(myargs)
	@parents = myargs
  end
  def files
	return @files
  end
  def files=(myargs)
	@files=myargs
  end
  def commitDate
	return @commitDate
  end
  def commitDate=(myargs)
	@commitDate=myargs
  end
  def hashCode
	return @hashCode
  end
  def hashCode=(myargs)
	@hashCode=myargs
  end
  def remoteCode
	return @remoteCode
  end
  def remoteCode=(myargs)
	@remoteCode=myargs
  end
  def prettyPrint
  	print "\nHash: "
	puts hashCode;
	print "Date: "
	puts commitDate;
	
	print "Parents: "
	parents.each do |x|
		          print x[0];
		          print " ";
	             end 
        
	print "\nFiles("
	print files.length
	print "): "
 	files.each do |x|
		          print x;
		          print " ";
	             end
	print "\n";
  end

  def printParentChildFiles
  	print "\nHash: "
	puts hashCode;
	
	print "Files("
	print files.length
	print "): "
 	files.each do |x|
		          print x;
		          print " ";
	             end
	
	print "\nParents: \n"
	parents.each do |x|
		          puts x[0];
			  print "files: ";

			  gitSummary = %x[git show --pretty="format:" --name-only #{x[0]}]
          
		          # split results on a newline char
		          gitSummary = gitSummary.split("\n");

		          #saving git summary into objects
		          gitSummary.each do |fileInfo|
                                        fileInfo.strip!

		   			if fileInfo.length() > 0
		   				print fileInfo;
						print " ";
		                        end
		          end
			  
		          print "\n";
	             end 
	print "\n";
  end

  def starBuildTest()
  	Dir.chdir($workDir) do
	    
	    parentHash = hashCode
	    puts "\nHard reset Git repository to rev: " + parentHash;
            %x[git reset --hard #{parentHash}]

	    puts "\nChecking out Git master repository";
            %x[git checkout -f master]

	    puts "\nClean local git folder";
	    %x[git clean -f -x -d]

	    
	    	

	    #%x[cp -f /Users/bkasi/Documents/Research/DataAnalysis/makedepend #{$workDir}]
	    #%x[cp -f /Users/bkasi/Documents/Research/DataAnalysis/makedepend.SH #{$workDir}]    
           
        puts "\nCopying SQL Library \n\n";
        %x[cp -f ../mysql-connector-java-5.1.17-bin.jar ./lib/]

		puts "\nMaking build\n\n";
        puts %x[ant jar]

	    #puts"\n\n Building Code"
            #puts %x[ant -f build.xml]
            #puts"\n\n";

	
	   puts"--------------------------------------------------------------------------------";
            puts"\n\n";	

	   puts "Running tests \n\n";
	   puts %x[ant junit];

	  end
	end
end 

class ProcessSummary
	
	def processDate(hCode)

		# declare a new commits object
		changeSet = Commit.new
		changeSet.hashCode = hCode

		#execute a git show command on the given hash code
		gitSummary = %x[git show --name-only #{hCode}]
          
		# split results on a newline char
		gitSummary = gitSummary.split("\n");

		
		#saving git summary into objects
		gitSummary.each do |dateInfo|
                   dateInfo.strip!
		   
		   if dateInfo.start_with?("Merge: ")
			changeSet.parents = Hash.new
			dateInfo = dateInfo.sub("Merge: ", "").strip
			merges = dateInfo.split(" ")
                        
			merges.each do |hashId|
			        changeSet.parents[hashId] = ""; 
                        end

		   elsif dateInfo.start_with?("Date: ")
			dateInfo = dateInfo.sub("Date: ", "").strip
			changeSet.commitDate = DateTime.parse(dateInfo); 
		   end
		end
		
		$commitsMap[hCode] = changeSet
	end
	
	def processFiles(hCode)
		
	        changeSet = $commitsMap[hCode]
		fileNames = []
		
		gitSummary = %x[git show --pretty="format:" --name-only #{hCode}]
          
		# split results on a newline char
		gitSummary = gitSummary.split("\n");

		#saving git summary into objects
		gitSummary.each do |fileInfo|
                   fileInfo.strip!
		   
		   if fileInfo.length() > 0
		   	fileNames.push(fileInfo);
		   end
		end
		changeSet.files = fileNames;
	end

	def setRemoteHash(hCode)

	   $dateTemp = nil
	   changeSet = $commitsMap[hCode]
	   changeSet.parents.keys.each do |x|
       			gitSummary = %x[git show --name-only #{x}]
			gitSummary.each do |dateInfo|
                   		            dateInfo.strip!
		   
		   			    if dateInfo.start_with?("Date: ")
			                       dateInfo = dateInfo.sub("Date: ", "").strip
			                       changeSet.parents[x] = DateTime.parse(dateInfo);
		                            end
		                          end
			end
	   # parents hash has been automatically converted to array at this point(array of hash)
           changeSet.parents = changeSet.parents.sort{ |x, y| x[1] <=> y[1] }
         end


	def getMergeFixEstimates(masterCode, remoteCode, numConflicts)
         
        	conflict = true;
	        parentHash = getParentHash(remoteCode);
                @prevCommit = remoteCode;
	        Dir.chdir($workDir) do
                       while conflict == true
	                 	puts "\nHard reset Git repository to rev: " + masterCode;
                         	%x[git reset --hard #{masterCode}]
 
           
	   			 puts "\nChecking out 'new' branch for " + parentHash;
	   			 %x[git checkout -b new #{parentHash}]
	   
		                 puts "\nChecking out Git blead repository";
        	   		 %x[git checkout blead]    
           
                	         puts"\n\n Merge new into blead"
                        	 gitSummary = %x[git merge new]
			
				 # split results on a newline char
				 gitSummary = gitSummary.split("\n");
		
				 conflict = false;

				 #saving git summary into objects
				 gitSummary.each do |dateInfo|
				    dateInfo.strip!
			            puts dateInfo;
		   		    if dateInfo.start_with?("CONFLICT (content): ")
					conflict = true;
					@prevCommit = parentHash; 
					parentHash = getParentHash(parentHash);
		   	    	    end
                          	end
				puts "\nDeleting the new branch"; 
				%x[git branch -D new]   

				puts conflict;
				puts"\n\n";
                         end
			 
			 mergeCode = getChildtHash(masterCode)
			 d1 = getCommitDate(parentHash);
			 d2 = getCommitDate(@prevCommit);
			 d3 = getCommitDate(mergeCode);
			 print "Number of conflicts: "
                         puts numConflicts;
			 puts "Last clean commit: " + parentHash;
			 print "Last clean commit date D1: "
                         puts d1;
                         puts "Last conflicting commit: "+ @prevCommit;
			 print "Last conflicting date D2: "
                         puts d2;
                         puts "Current Merge Id: "+ mergeCode;
			 print "Current Merge Date D3: "
                         puts d3;
			 print "D3 - D1 (Merge - L.Clean): "
                         puts (d3 - d1).to_i
			 print "D3 - D2 (Merge - L.Conf): "
			 puts (d3 - d2).to_i
			 puts"\n=============================================================================\n\n" 
			 	
                end 
        end

        def getParentHash(remoteCode)
		gitSummary = %x[git show -s --pretty=raw #{remoteCode}]
          
		# split results on a newline char
		gitSummary = gitSummary.split("\n");
		
		#saving git summary into objects
		gitSummary.each do |dateInfo|
                   dateInfo.strip!
		   
		   if dateInfo.start_with?("parent ")
			dateInfo = dateInfo.sub("parent ", "").strip;
                        return dateInfo;
		   end
               end
		
        end


	def processMergeLogs(logFile)
		#Create a read only object of the file
		mergeConfs = File.new(logFile, "r")
		
		conflictsHash = Hash.new;
        	conflictsCount = Hash.new;
			
		#iterate contents of the file
		mergeConfs.each do |logResult|

			# split results on section seperator
			gitSummary = logResult.split("=============================================================================");
	
			
			#saving git summary into objects
			gitSummary.each do |dateInfo|
            			dateInfo.strip!
	    			mergeLogInfo = dateInfo.split("\n")  
	    			
	    			mergeLogInfo.each do |conflictInfo|    
	       				conflictInfo.strip!
					if conflictInfo.start_with?("Hard reset Git repository to rev: ")
						@parentFile = dateInfo.sub("Hard reset Git repository to rev: ", "").strip;
                
                			elsif conflictInfo.start_with?("Checking out 'new' branch for ")
						@remoteFile = dateInfo.sub("Checking out 'new' branch for ", "").strip;
					elsif conflictInfo.start_with?("CONFLICT (content): ")
						if conflictsHash.has_key?(@parentFile)
							conflictsCount[@parentFile] = conflictsCount[@parentFile] + 1
						else
							conflictsHash[@parentFile] = @remoteFile;
							conflictsCount[@parentFile] = 1;
                                                end 
					end
            			end
	 		end
     		end
		for ss in 1...2 #0ÉconflictsHash.keys.length
			getMergeFixEstimates(conflictsHash.keys[ss], conflictsHash[conflictsHash.keys[ss]], conflictsCount[conflictsHash.keys[ss]]);
		end
        end
	

	def getCommitDate(remoteCode)
		gitSummary = %x[git show --name-only #{remoteCode}]
          
		# split results on a newline char
		gitSummary = gitSummary.split("\n");
		
		#saving git summary into objects
		gitSummary.each do |dateInfo|
                   		   dateInfo.strip!
	   			    if dateInfo.start_with?("Date: ")
		                       dateInfo = dateInfo.sub("Date: ", "").strip
		                       return DateTime.parse(dateInfo);
	                            end
                          end
        end

	def getChildtHash(masterCode)
		
		$commitsMap.keys.each do |hashCode|
                	if $commitsMap[hashCode].parents.at(0)[0] == masterCode
				return hashCode
                        end  
		end
		
			
	end
end


gitSummary = ProcessSummary.new


puts "\nGetting Git repository for Perl5";
#%x[git clone git://perl5.git.perl.org/perl.git #{$workDir}]

#Create a read only object of the file
mergeConfs = File.new("voldemort1.ids", "r")

Dir.chdir($workDir)

#iterate contents of the file
puts "Processing Date Information"
puts "Processing File Information"  
puts "Set remote hash code"
mergeConfs.each do |hashCode|		 
		    
                   gitSummary.processDate(hashCode);
		   gitSummary.processFiles(hashCode);
		   gitSummary.setRemoteHash(hashCode);
                end


$commitsMap.keys.each do |hashCode|
                #$commitsMap[hashCode].printParentChildFiles();    

		$sortedList[hashCode] =  $commitsMap[hashCode].commitDate;        
end

$continue = false;

$sortedList = $sortedList.sort{ |x, y| y[1] <=> x[1] }
$sortedList.each do |x|
		print "Processing for "
		print x[0]
                print " Date: "
                puts x[1] 
		$commitsMap[x[0]].starBuildTest();
		puts"\n=============================================================================\n\n"
end




