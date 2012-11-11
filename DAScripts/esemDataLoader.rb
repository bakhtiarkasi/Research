#!/usr/bin/ruby
require 'date'

#mergeid:[list of all files]
$mergeFilesMap = Hash.new

$rowCount = 0

$workDir = "/Users/bkasi/Documents/Research/DAScripts/voldemortESEM.txt";
$filureDir = "/Users/bkasi/Documents/Research/DAScripts/voldemortFailure.txt";

$fileClassMap = Hash.new

class FileClass
def initialize()
	@otherFilesMap = Hash.new;
	@othePackagesMap = Hash.new;
end
  def otherFilesMap
	return @otherFilesMap
  end
  def otherFilesMap=(myargs)
	@otherFilesMap = myargs
  end
  def othePackagesMap
	return @othePackagesMap
  end
  def othePackagesMap=(myargs)
	@othePackagesMap = myargs
  end
  def fileName
	return @fileName
  end
  def fileName=(myargs)
	@fileName = myargs 
  end
end


class MergeData
def initialize()
  	files = [];
  	hashCode ="";
        developerName="";
	@buildF=0;
	@mergeF=0;
	@testF=0;
	
  end
 
  def developer
	return @developerName
  end
  def developer=(myargs)
	@developerName = myargs
  end
  def files
	return @files
  end
  def files=(myargs)
	@files=myargs
  end
  def hash
	return @hashCode
  end
  def hash=(myargs)
	@hashCode=myargs
  end

  def saveFiles(myargs)
	myargs.gsub!("[", "");
	myargs.gsub!("]", "");
        myargs.gsub!(", ", ",");
        @files = myargs.split(",")
	@files = @files.find_all{|item| item =~ /\.(sh|java|py|xml|groovy|h|cpp|rb|scala)$/ }
	
  end

  def countPackages()
	@allPackages = Hash.new;
	for ff in 0...files.length
		fileName = files[ff];
		names = fileName.split("/")[-1]
		packageName = fileName.gsub(names,"");

		
		if packageName == ""
			
		else
			@allPackages[packageName] = 1
		end 
		
	end
	return @allPackages.keys.length
  end

  def getPackageCount()
	return @allPackages.keys.length
  end

  def buildF=(myargs)
	@buildF = myargs
  end

  def buildF
	return @buildF
  end

  def mergeF=(myargs)
	@mergeF = myargs
  end

  def mergeF
	return @mergeF
  end

  def testF=(myargs)
	@testF = myargs
  end

  def testF
	return @testF
  end 
	
end

class ProcessSummary

	def processESEMData(logFile)
		#Create a read only object of the file
		mergeConfs = File.new(logFile, "r")

		@fileContents= '';
		mergeConfs.each do |logResult|
			@fileContents.concat(logResult);	
		end
					
		# split results on a newline char
		gitSummary = @fileContents.split("--------------------------------------------------------------");
		mergeDeveloperText = gitSummary[0]
		mergeFilesTest = gitSummary[1]
		$rowCount = gitSummary[2]
		
		
			# split results on section seperator
			gitSummary = mergeDeveloperText.split("\n");
			
			#saving git summary into objects
			gitSummary.each do |dateInfo|
            			dateInfo.strip!
	    			mergeLogInfo = dateInfo.split(":")
                                mergeHashCode = mergeLogInfo[0]
				mergeDataObj = MergeData.new
				mergeDataObj.hash = mergeHashCode
                                mergeDataObj.developer = mergeLogInfo[1]
				

				$mergeFilesMap[mergeHashCode] = mergeDataObj; 
			end

			
			# split results on section seperator
			gitSummary = mergeFilesTest.split("\n");
			
			#saving git summary into objects
			gitSummary.each do |dateInfo|
            			dateInfo.strip!
	    			mergeLogInfo = dateInfo.split(":")  
	    			
				mergeHashCode = mergeLogInfo[0] 
				allContext = mergeLogInfo[1]

				if $mergeFilesMap.has_key?(mergeHashCode)
					mergeDataObj = $mergeFilesMap[mergeHashCode];
					mergeDataObj.saveFiles(allContext);
				end 
			end
			
			for ss in 0...$mergeFilesMap.keys.length
				mergeHashCode = $mergeFilesMap.keys[ss]
				mergeDataObj = $mergeFilesMap[mergeHashCode];
				
				mergeDataObj.countPackages();
				
				for ff in 0...mergeDataObj.files.length
					fileName = mergeDataObj.files[ff];

					if $fileClassMap.has_key?(fileName)
						fileObj = $fileClassMap[fileName];
					else
						fileObj = FileClass.new;
						fileObj.fileName = fileName;
						$fileClassMap[fileName] = fileObj;
					end					  
				end
		      end

		      for ss in 0...$mergeFilesMap.keys.length
				mergeHashCode = $mergeFilesMap.keys[ss]
				mergeDataObj = $mergeFilesMap[mergeHashCode];
	
				for ff in 0...mergeDataObj.files.length
					fileName = mergeDataObj.files[ff];

					fileObj = $fileClassMap[fileName];
					
					for fc in 0...mergeDataObj.files.length
						fileName2 = mergeDataObj.files[fc];
						
						if fileName != fileName2
							if fileObj.otherFilesMap.has_key?(fileName2)
							
							else
								fileObj.otherFilesMap[fileName2] = 1; 
							end

							names = fileName2.split("/")[-1]
							packageName = fileName2.gsub(names,"");

							if fileObj.othePackagesMap.has_key?(packageName)
							
							else
								fileObj.othePackagesMap[packageName] = 1; 
							end
							
						end
					end
				
									  
				end
		      end

	end

	def printResults
		for ss in 0...$mergeFilesMap.keys.length
				mergeHashCode = $mergeFilesMap.keys[ss]
				mergeDataObj = $mergeFilesMap[mergeHashCode];
				
				allFiles = mergeDataObj.files;
				for jj in 0...allFiles.length
					fileName = allFiles[jj];
					puts "#{mergeHashCode} #{fileName} #{mergeDataObj.developer.split("<")[1].split(">")[0]} #{mergeDataObj.files.length - 1} #{mergeDataObj.getPackageCount()-1} #{0} #{mergeDataObj.mergeF} #{mergeDataObj.buildF} #{mergeDataObj.testF}"
				end	

		      end
		for ss in 0...$fileClassMap.keys.length
			puts "#{$fileClassMap[$fileClassMap.keys[ss]].fileName} #{$fileClassMap[$fileClassMap.keys[ss]].otherFilesMap.length} #{$fileClassMap[$fileClassMap.keys[ss]].othePackagesMap.length}"
		end
		
	end

	def processFailuersData(logFile)
		#Create a read only object of the file
		mergeConfs = File.new(logFile, "r")

		@fileContents= '';
		mergeConfs.each do |logResult|
			@fileContents.concat(logResult);	
		end
					
		# split results on a newline char
		gitSummary = @fileContents.split("--------------------------------------------------------------");
		mergeText = gitSummary[0]
		buildText = gitSummary[1]
		testText = gitSummary[2]
		
		
		# split results on section seperator
		gitSummary = mergeText.split("\n");
			
		#saving git summary into objects
		gitSummary.each do |dateInfo|
            		dateInfo.strip!
			if $mergeFilesMap.has_key?(dateInfo)
				mergeDataObj = $mergeFilesMap[dateInfo];
				mergeDataObj.mergeF = 1;
			end
		end
		# split results on section seperator
		gitSummary = buildText.split("\n");
				
	
		#saving git summary into objects
		gitSummary.each do |dateInfo|
		dateInfo.strip!
            		if $mergeFilesMap.has_key?(dateInfo)
				mergeDataObj = $mergeFilesMap[dateInfo];
				mergeDataObj.buildF = 1;
			end
		end

		# split results on section seperator
		gitSummary = testText.split("\n");
			
		#saving git summary into objects
            	gitSummary.each do |dateInfo|
            		dateInfo.strip!
			if $mergeFilesMap.has_key?(dateInfo)
				mergeDataObj = $mergeFilesMap[dateInfo];
				mergeDataObj.testF = 1;
			end
		end
	end

end


gitSummary = ProcessSummary.new
gitSummary.processESEMData($workDir);
gitSummary.processFailuersData($filureDir);
gitSummary.printResults();



