#!/usr/bin/ruby
require 'date'
require 'rubygems'

#mergeid:[list of all files]
$mergeFilesMap = Hash.new

$workDir = "/Users/bkasi/Documents/Research/DAScripts";

$xmlstring = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Merges>"

class ProcessSummary

	def processHash(hashCode)
		Dir.chdir($workDir) do
			filesInfo = %x[../gremlin-2.0/gremlin-groovy.sh -e stormGetFiles.groovy #{hashCode}];

			filesInfo = filesInfo.split("-------------------------------------------\n");
			master = filesInfo[0];
			remote = filesInfo[1];

			filesInfo = master.split("\n");
			$xmlstring += "<Commit MergeId=\"#{hashCode}\"><Master DevName=\"#{filesInfo[0]}\">";

			for ss in 1...filesInfo.count()
				$xmlstring += "<File FileName=\"#{filesInfo[ss].gsub("storm--src/jvm", "classes").gsub("storm--src/clj", "classes")}\">";

				#add dependecies
				fileDependencies = getDependentFiles(filesInfo[ss]);
				
				if ! fileDependencies.nil? 
					fileDependencies = fileDependencies.split(",");

					fileDependencies.each do |tempFN|
						$xmlstring += "<Dependency FileName=\"#{tempFN}\"></Dependency>";
					end
				end

				$xmlstring +="</File>"					
			end

			filesInfo = remote.split("\n")
			$xmlstring +="</Master><Remote DevName=\"#{filesInfo[0]}\">";

			for ss in 1...filesInfo.count()
				$xmlstring += "<File FileName=\"#{filesInfo[ss].gsub("storm--src/jvm", "classes").gsub("storm--src/clj", "classes")}\">";

				#add dependecies
				fileDependencies = getDependentFiles(filesInfo[ss]);
				
				if ! fileDependencies.nil? 
					fileDependencies = fileDependencies.split(",");

					fileDependencies.each do |tempFN|
						$xmlstring += "<Dependency FileName=\"#{tempFN}\"></Dependency>";
					end
				end

				$xmlstring +="</File>"					
			end

			$xmlstring +="</Remote></Commit>";

		end
	end

	def processESEMData(logFile)

	end

	def getDependentFiles(filesids)

		filetempid=filesids;
        
		if ! filesids.include?(".java")
			if ! filesids.include?(".clj") 
				return "";
			end
		end

		absFilePath = "/Users/bkasi/Documents/Research/DataAnalysis/StormRelease/"+$hashCode+"/";

		filesids = filesids.gsub("storm--src/jvm", "classes").gsub("storm--src/clj", "classes").gsub(".java", ".class").gsub(".clj", ".class");

		filePath = absFilePath + filesids;

		if ! File.exist?(filePath)
			return;
		end;

		ENV['JAVA_HOME']="/Library/Java/Home"; 
		%x[/Users/bkasi/Library/Cassandra/Server/DependencyFinder-1.2.1-beta4/bin/DependencyExtractor -xml -minimize -out test.xml  #{filePath}]

		#-scope-includes /^backtype/
		taskList =  %x[/Users/bkasi/Library/Cassandra/Server/DependencyFinder-1.2.1-beta4/bin/DependencyReporter -c2c -show-inbounds -minimize -class-scope -scope-includes /^backtype/  -indent-text + test.xml]
		

		taskList = taskList.split("\n");
		@ss = 0;
		@allFileNames = "";

		while @ss < taskList.count()

			if !taskList[@ss].start_with? "+"
				@parentName = taskList[@ss].gsub("*","").gsub(" ","");

				if @parentName.include? "."
					@parentName =  "classes/" + @parentName.gsub(".","/") + "/";
				else
					@parentName =  "classes/";
				end						

				@fileName ="";
				@ss = @ss+1;

				while taskList[@ss].start_with? "+"
					if taskList[@ss].start_with? "++"
						@ss = @ss+1;
					elsif taskList[@ss].start_with? "+"
						temp = @parentName + taskList[@ss].gsub("*","").gsub(" ","").gsub("+","") + ".class";
							
						if (temp =~ /\$[0-9]*/).nil? 
							if ! filesids.include? temp
								@fileName += "#{temp},";
							end 
						end
						@ss = @ss+1;
					end
					if @ss == taskList.count()
						break;
					end
				end

				if @fileName != ""
					@allFileNames += @fileName;
				end
				@fileName ="";
			end
		end
			
		#putting files names in DB;
		if @allFileNames != ""
			@allFilesSelect = @allFileNames[0..-2];
		end

		if @allFilesSelect.nil?
			return "";
		end

		tempFileName = @allFilesSelect.split(",");

		tempFileName.each do |tempFN|
			
			if ! File.exist?(absFilePath+tempFN)
				puts "Not exist! :: " + tempFN;
			end

		end

	#puts @allFilesSelect;
		
	return @allFilesSelect;
	end
	
end

$hashCode =  ARGV[0];

allFiles =  ARGV[1];

gitSummary = ProcessSummary.new

if allFiles.nil?

	mergeConfs = File.new("storm.ids", "r")

	mergeConfs.each do |hashCode|
			$hashCode = hashCode;
			gitSummary.processHash(hashCode);
		end

	$xmlstring += "</Merges>";
	puts $xmlstring;
else
	allFiles = allFiles.split(",");
	output = "";
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
 


