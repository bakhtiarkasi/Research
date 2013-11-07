#!/usr/bin/ruby

mergeConfs = File.new("reps", "r")

$repsArray = [];
i = 0;

mergeConfs.each do |hashCode|
	$repsArray[i] = hashCode;
	i += 1;
end

i=0;
mergeConfs = "";
mergeConfs = File.new("outs", "r")
mergeConfs.each do |hashCode|
	for ss in 0...$repsArray[i].to_i
		puts hashCode
	end
	i += 1;
	for ss in 0...$repsArray[i].to_i
		puts hashCode
	end
	puts "";
	puts "";
	i += 1;
end
