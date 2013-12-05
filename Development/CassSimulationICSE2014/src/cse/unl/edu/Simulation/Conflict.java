package cse.unl.edu.Simulation;

import java.util.ArrayList;

public class Conflict {

	int fromTask = 0;
	int toTask = 0;
	String fromFile;
	String toFile;
	
	
	@Override
	public Conflict clone() {
		Conflict c = new Conflict();
		c.fromFile = this.fromFile;
		c.toFile = this.toFile;
		c.fromTask = this.fromTask;
		c.toTask = this.toTask;
		return c;
	}
	
	@Override
	public String toString()
	{
		return "FromFile: " + fromFile + " ToFile: " + toFile + " FromTask: " + fromTask + " ToTask: " + toTask; 
	}
	
	@Override
	public boolean equals(Object object)
	{
	    boolean isEqual= false;

	    if (object != null && object instanceof Conflict)
	    {
	        isEqual = this.fromFile.equals(((Conflict) object).fromFile) && (this.toFile.equals(((Conflict) object).toFile))
	        		&& this.fromTask  == ((Conflict) object).fromTask && this.toTask  == ((Conflict) object).toTask ;
	    }

	    return isEqual;
	}

	@Override
	public int hashCode() {
	    return this.fromFile.hashCode() + this.toFile.hashCode() + this.fromTask + this.toTask;
	}
}
