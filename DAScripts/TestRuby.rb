#!/usr/bin/ruby

@i=0;



def istrue()
	Thread.new
		@i = @i + 1;
		puts @i;
		if @i >= 5
			return false;
		end
	return true;
	end
thread = istrue();
thread.join;
puts  thread
