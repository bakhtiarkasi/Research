#!/usr/bin/ruby
require 'date'
require 'rubygems'
require 'mysql'
require 'drb/drb'
require File.expand_path(File.dirname(__FILE__) + '/configurations')

#mergeid:[list of all files]
$mergeFilesMap = Hash.new

$FilePath ='';
$SessionId = 3;
$allSequence="";
$allSequence = "7,4,5,3,2,1,5";


SERVER_URI="druby://localhost:8787"


class ProcessSummary

	def processESEMData(logFile)

		begin
			$dbhandle = Mysql.connect($myServer, $myUser, $myPass,$myDB)
			#Create a read only object of the file
			mergeConfs = File.new(logFile, "r")

			@fileContents= '';
			mergeConfs.each do |logResult|
				@fileContents.concat(logResult);	
			end
						
			# split results on a newline char
			taskList = @fileContents.split("-----------------------------------------------\n");
			
			$dbhandle.query("INSERT INTO Session(UserName,UID) VALUES('#{$developeName}',#{$developeID})");
			$SessionId = $dbhandle.insert_id;

			taskList.each do |taskLine|
				taskData = taskLine.split("\n");
				@filesValues = 'values';
				@files ='';
				@fileDesc='';
				@taskId = 0;
				for ss in 0...taskData.count()
					if taskData.count() == 0
						@fileDesc = taskData;
					else
						if ss == 0
							@fileDesc = taskData[ss];
						else
							@filesValues += "('#{taskData[ss]}')" + ",";
							@files += "'#{taskData[ss]}'" + ",";
						end
					end
				end
				if @filesValues != 'values'
					@filesValues = @filesValues[0, @filesValues.length - 1]
					$dbhandle.query "INSERT IGNORE INTO Files(Path) #{@filesValues}";
					rs = $dbhandle.query "Select group_concat(pkFileID) from files where Path in (#{@files[0, @files.length - 1]})";
	    			@fileIds = rs.fetch_row;
	    			$dbhandle.query "INSERT INTO Tasks(fkSessionID, Description, Preference) VALUES(#{$SessionId},'#{@fileDesc.split(':')[1]}', #{@fileDesc.split(':')[0]})";
	    			@taskId = $dbhandle.insert_id;

	    			#@fileIds = @fileIds.split(',');
	    			@fileIds = @fileIds[0].split(',');
	    			@fileIds.each do |fileId|
	    				$dbhandle.query "INSERT INTO Tasks2File(fkFileID,fkTaskID,SelectionType) VALUES(#{fileId},#{@taskId},'E')";
	    			end
	    		else
	    			$dbhandle.query "INSERT INTO Tasks(fkSessionID, Description, Preference) VALUES(#{$SessionId},'#{@fileDesc.split(':')[1]}', #{@fileDesc.split(':')[0]})";
				end
			end
			$dbhandle.query("CALL SP_CassandraIndentifyConflicts(#{$developeID}, #{$SessionId})")

		rescue Mysql::Error => e
		    puts e.errno
		    puts e.error
		ensure
		    $dbhandle.close if $dbhandle
		end
	end

	def processZ3Results(results)
		begin
			$dbhandle = Mysql.connect($myServer, $myUser, $myPass,$myDB)
			#Create a read only object of the file
						
			# split results on a newline char
			taskList = results.split("\n");
			
			$dbhandle.query("Update Session set Processed = 1 where pkSessionId = #{$SessionId}");

			taskList.each do |taskLine|
				if taskLine.include? "->"
					@allResults = taskLine.split("->")[1]
					
					if @allResults.split("[")[0].to_i == $developeID
						$allSequence = @allResults.split("[")[1].gsub("]","").gub(" ", ",")
						@allResults = @allResults.split("[")[1].gsub("]","").split(" ");
						
						for ss in 0...@allResults.count()
							$dbhandle.query("Update tasks set RecOrder = #{@allResults[ss]} where fkSessionId = #{$SessionId} and Preference = #{ss+1}");
						end
						break;
					end
				end
			end
		rescue Mysql::Error => e
		    puts e.errno
		    puts e.error
		ensure
		    $dbhandle.close if $dbhandle
		end
	end

	def connectSocket()
		begin
			# The URI to connect to
			serverApp = DRbObject.new_with_uri(SERVER_URI)
			z3output = serverApp.session(1);

			puts z3output;
			processZ3Results(z3output)
			
			#serverApp2 = DRbObject.new_with_uri(SERVER_URI)
			#z3output = serverApp2.session(2);

			puts z3output;
			processZ3Results(z3output)
			
		end
	end


	def writeOutput(fileName)
		begin
			$dbhandle = Mysql.connect($myServer, $myUser, $myPass,$myDB)
			@xmlstring = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Conflicts>"
			@xmlstring += "<Sequence>#{$allSequence}</Sequence><Summary>"

			rs = $dbhandle.query("Select (Select UID from session st where st.pkSessionId = #{$SessionId}) UID, 
									s.UserName, t.description, c.Type 
							from conflicts c 
			    			inner join tasks t on t.pkTaskid = c.fkTaskid2
			    			inner join session s on t.fkSessionId = s.pkSessionId
			    			where c.fkSessionid = #{$SessionId}");

			@conflictMap =Hash.new;
			@confXml = "<AllConflicts>"			
			rs.each_hash do |row|

	   		
	   			@confXml += "<conf Id=\"#{row['UID']}\" DevName=\"#{row['UserName']}\" TaskName=\"#{row['description']}\"  Type=\"#{row['Type']}\"/>";

	   			if !@conflictMap[row['UID']]
	   				@conflictMap[row['UID']] = "";
	   			end



	   			confType = "DC"
	   			if row['Type'] == "I"
	   				confType = "IC"
	   			end	
	   			@conflictMap[row['UID']] += confType + "[#{row['UserName']}] : #{row['description']}\\n";  
			end
			@confXml += "</AllConflicts>"

			for ss in 0...@conflictMap.keys.length
				str = @conflictMap[@conflictMap.keys[ss]];
				str = str[0,str.length-2]
				@xmlstring += "<Task Id=\"#{@conflictMap.keys[ss]}\">#{str}</Task>";
			end

			@xmlstring += "</Summary>#{@confXml}</Conflicts>";

			puts @xmlstring;
			
			f = File.new(fileName, "w")
			f.puts @xmlstring;
			f.close();

		rescue Mysql::Error => e
		    puts e.errno
		    puts e.error
		ensure
		    $dbhandle.close if $dbhandle
		end
	end
	
end

$FilePath =  ARGV[0];

$FilePath ="/Users/bkasi/Documents/workspace/Temp/tasklistconflicts.txt";

gitSummary = ProcessSummary.new
#gitSummary.processESEMData($FilePath);
#gitSummary.connectSocket();
gitSummary.writeOutput("/Users/bkasi/Documents/workspace/Temp/tasklist.xml");

puts "done here";



