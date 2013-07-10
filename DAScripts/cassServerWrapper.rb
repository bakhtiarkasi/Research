#!/usr/bin/ruby
require 'date'
require 'rubygems'
require 'mysql'
require 'drb/drb'
require 'thread'

$myServer = "localhost";
$myUser = "root";
$myPass = "arsonae";
$myDB = "CassandraConflict";

$FilePath ='';

# The URI for the server to connect to
URI="druby://localhost:8787"


  class ServerApp
    def initialize
      @session = 0
      @mutex = Mutex.new
      @string = "";
    end
    def session(elem)
      @mutex.synchronize do
        @session = elem
        @string += "here for #{elem}"
        callDbfor()
      end
    end
    def getStatus
        return @session
    end
    def getoutput
      return @string;
    end
    
    def callDbfor()
      @FileName = %x[java -jar /Users/bkasi/Documents/workspace/Temp/Cassandra.jar #{@session} false]
      @outPut =  %x[python #{@FileName}]
      return @outPut;
    end

  end

  serverApp = ServerApp.new


#$SAFE = 1   # disable eval() and friends

# Wait for the drb server thread to finish before exiting.
DRb.start_service(URI, serverApp)
DRb.thread.join
