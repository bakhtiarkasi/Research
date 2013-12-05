#!/usr/bin/ruby
require 'date'

#mergeid:[list of all files]
$mergeFilesMap = Hash.new

$workDir = "/Users/bkasi/Documents/Research/DAScripts";
$gremlinPath = "../gremlin-2.0/gremlin-groovy.sh";
$voldemortReleasePath = "/Users/bkasi/Documents/Research/DataAnalysis/Voldemort/";
$understandPath = "/Applications/scitools/bin/macosx";
$unsertandDBPath = "/Users/bkasi/Documents/Research/DataAnalysis/understand";

#$workDir = "/work/esquared/bkasi/DataAnalysis/Voldemort";
#$gremlinPath = "../../Tools/Gremlin1/bin/gremlin.sh";
#$voldemortReleasePath = "/work/esquared/bkasi/GitRepos/Voldemort/";
#$understandPath = "/work/esquared/bkasi/Tools/Understand/bin/linux64"
#$unsertandDBPath = $workDir;

$xmlstring = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Merges>"

class ProcessSummary

	def processHash(fromDate, toDate)	
		Dir.chdir($workDir) do
			filesInfo = %x[#{$gremlinPath} -e VoldemortICSEWeekly.groovy #{fromDate} #{toDate}];

			filesInfo = filesInfo.split("-------------------------------------------\n");
			master = filesInfo[0];
			masterInfo = master.split("\n");
			
			$xmlstring += "<Commit MergeId=\"#{$hashCode}\"><Master DevName=\"#{masterInfo[1]}\">";

			#file count here
			#filesInfo = [];
			for ss in 2...masterInfo.count()
				masterInfo[ss] = masterInfo[ss].gsub("voldemort--", "");
				$xmlstring += "<File FileName=\"#{masterInfo[ss]}\">";

				#add dependecies
				fileDependencies = getDependentFiles(masterInfo[ss]);
				
				if ! fileDependencies.nil? 
					fileDependencies = fileDependencies.split(",");

					fileDependencies.each do |tempFN|
						$xmlstring += "<Dependency FileName=\"#{tempFN}\"></Dependency>";
					end
				end

				$xmlstring +="</File>"		
			end

			$xmlstring +="</Master>";

			for merge in filesInfo[1..filesInfo.size()]

				remoteInfo = merge.split("\n")
				$xmlstring +="<Remote DevName=\"#{remoteInfo[1]}\">";

				for ss in 2...remoteInfo.count()
					remoteInfo[ss] = remoteInfo[ss].gsub("voldemort--", "");
					$xmlstring += "<File FileName=\"#{remoteInfo[ss]}\">";

					#add dependecies
					fileDependencies = getDependentFiles(remoteInfo[ss]);

					if ! fileDependencies.nil? 
						fileDependencies = fileDependencies.split(",");

						fileDependencies.each do |tempFN|
							$xmlstring += "<Dependency FileName=\"#{tempFN}\"></Dependency>";
						end
					end
					$xmlstring +="</File>"		
				end

				$xmlstring +="</Remote>";
			end

			$xmlstring +="</Commit>";
		end	
	end

	
	def getDependentFiles(filesids)

		if ! filesids.include?(".java")
			if ! filesids.include?(".cpp")
				if ! filesids.include?(".h") 
					return "";
				end
			end
		end

		#puts "/Applications/scitools/bin/macosx/uperl /Users/bkasi/Documents/Research/DAScripts/voldemortDeps.pl #{filesids}";
		#puts "#{$understandPath}/uperl #{$workDir}/voldemortDeps.pl #{filesids}";
		filesids = filesids.split(",")
		tempFileName = "";
		filesids.each do |filename|
			tempFileName += $voldemortReleasePath + filename + ",";
		end

		if tempFileName != ""
			filesids = tempFileName[0..-2];
		end

		taskList =  %x[#{$understandPath}/uperl #{$workDir}/voldemortDeps.pl #{filesids}]
		
		taskList = taskList.split("\n");
		
		@ss = 0;
		@allFileNames = "";

		while @ss < taskList.count()
			@allFileNames += taskList[@ss].gsub($voldemortReleasePath,"") + ",";
			@ss = @ss+1;	
		end
			
		#putting files names in DB;
		if @allFileNames != ""
			@allFilesSelect = @allFileNames[0..-2];
		end

		if @allFilesSelect.nil?
			return "";
		end

	#puts @allFilesSelect;
		
	return @allFilesSelect;
	end
end

$hashCode =  ARGV[0];

allFiles =  ARGV[1];

gitSummary = ProcessSummary.new

if allFiles.nil?

	mergeConfs = File.new("VoldemortWeekly.ids", "r")

	mergeConfs.each do |hashCode|
		hashCode = hashCode.chomp;
		hashId = hashCode.split(":");
		$hashCode = hashId[0]	
		d =  Date.parse(hashId[1]);
		startDate = d - (d.cwday % 7);
		endDate =  d + 7 - (d.cwday % 7) - 1;

		Dir.chdir($voldemortReleasePath) do
			 	%x[git reset --hard #{$hashCode}]
			 	%x[git clean -f -x -d]
			 	%x[git checkout master]
			 	%x[#{$understandPath}/und -db #{$unsertandDBPath}/voldemort.udb analyze -rescan -changed]
		end

		gitSummary.processHash(startDate, endDate);		
	end

	$xmlstring += "</Merges>";
	puts $xmlstring;
else
	allFiles = allFiles.split(",");
	output = "";
	
	Dir.chdir($voldemortReleasePath) do
	 	%x[git reset --hard #{$hashCode}]
	 	%x[git clean -f -x -d]
	 	%x[git checkout master]
	 	%x[#{$understandPath}/und -db #{$unsertandDBPath}/voldemort.udb analyze -rescan -changed]
	end
	
	allFiles.each do |tempFN|
		fileDependencies = gitSummary.getDependentFiles(tempFN)
		
		if !fileDependencies.nil?
			output += fileDependencies + ","; 
		end
	end
	
	if output != ""
			output = output[0..-2];
	end
	puts output;
end

#gitSummary.processESEMData($FilePath);
#gitSummary.connectSocket();
#gitSummary.writeOutput("/Users/bkasi/Documents/workspace/Temp/tasklist.xml");

#puts "done here";
#puts $hashCode;
 


