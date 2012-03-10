#!/usr/bin/ruby
require 'date'

$commitsMap = Hash.new
$dateTemp = DateTime.now();
$workDir = "/Users/bkasi/Documents/Research/DataAnalysis/Perl5/";

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

  def startMergeProcess()
  	Dir.chdir($workDir) do
	    
	    parentHash = parents.at(0)[0]
	    puts "\nHard reset Git repository to rev: " + parentHash;
            #%x[git reset --hard #{parentHash}]

	    parents.each do |x|
           
		remoteCode = x[0]
                if parentHash != remoteCode
	   		puts "\nChecking out 'new' branch for " + remoteCode;
			#%x[git checkout -b new #{remoteCode}]
	   
	   		puts "\nChecking out Git blead repository";
           		#%x[git checkout blead]    
           
           		puts"\n\n Merge new into blead"
           		#puts %x[git merge new]
           		puts"\n\n";

			puts "\nDeleting the new branch"; 
	    		#%x[git branch -D new]

	   	end
			
           end
	    
       end
  end
end 

class ProcessSummary
	
	def processDate(hCode)

		puts "Processing Date Information"
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
		
		puts "Processing File Information"
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

	   puts "Set remote hash code"
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


	def getMergeFixEstimates(masterCode, remoteCode)
         
        	conflict = true;
	        

	        Dir.chdir($workDir) do
                       while conflict == true
	                 	puts "\nHard reset Git repository to rev: " + masterCode;
                         	%x[git reset --hard #{masterCode}]

				puts "\nDeleting the new branch"; 
				%x[git branch -D new]    
           
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
					parentHash = getParentHash(parentHash);
		   	    	    end
                          	end
				puts conflict;
				puts"\n\n";
                         end	
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

end


gitSummary = ProcessSummary.new


	        
puts "\nGetting Git repository for Perl5";
#%x[git clone git://perl5.git.perl.org/perl.git #{$workDir}]

#Create a read only object of the file
mergeConfs = File.new("/Users/bkasi/Documents/Research/merge.ids", "r")

Dir.chdir($workDir)

#iterate contents of the file
mergeConfs.each do |hashCode|		    
                   gitSummary.processDate(hashCode);
		   gitSummary.processFiles(hashCode);
		   gitSummary.setRemoteHash(hashCode);
                end


$commitsMap.keys.each do |hashCode|
                #$commitsMap[hashCode].printParentChildFiles();              
end

$commitsMap.keys.each do |hashCode|
                    $commitsMap[hashCode].startMergeProcess();
		    puts"\n=============================================================================\n\n"
                end

#conflictsHash = ["3b8a5fb","6606016","216e7de","d4fb0a1","edb80b8","65d043b","7ce0928"]
#remoteHash = ["85d7fce","e3f38af","f1bef09","2547c83","454155d","d96f3ac","4e2ac26"]

conflictsHash = ["6606016"]
remoteHash = ["e3f38af"]

for ss in 0...conflictsHash.length
#	gitSummary.getMergeFixEstimates(conflictsHash[ss], remoteHash[ss]);
end

#$commitsMap[$commitsMap.keys[1]].startMergeProcess();



