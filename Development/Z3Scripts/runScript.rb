#!/usr/bin/ruby
require 'date'

class ProcessSummary	

	
	
	def runAllScripts(workDirectory)
		%x[cp ./cassMain.py .]
		Dir.chdir(workDirectory) do
	
			path = "*.py"
			
				
			Dir[path].each do |fileName|
				if ! fileName.include?("cassMain.py")
				    print "Session: "
				    print fileName.sub("outputFile","").sub(".py","");
				    puts"";
					puts %x[python -m cProfile #{fileName}]
					#puts %x[python  #{fileName}]
					puts "\n\n==========================================================================\n\n"
				end
			end
		end
 	 end
end

gitSumm = ProcessSummary.new
gitSumm.runAllScripts("/Users/bkasi/Documents/Research/Development/Z3 Scripts/Z3Input/");


