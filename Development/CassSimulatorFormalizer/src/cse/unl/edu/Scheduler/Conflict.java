package cse.unl.edu.Scheduler;

public class Conflict {

	final int sessionId;
	final int task1Id;
	final int task2Id;
	final Type conflictType;
	final Direction conflictDirection;
	private float conflictWeight;

	public enum Type {
		MC, BC, TC
	};

	public enum Direction {
		Right, Left, None
	};

	public Conflict(int sessionId, int task1Id, int task2Id,
			String conflictType, String conflictDir) {
		super();
		this.sessionId = sessionId;
		this.task1Id = task1Id;
		this.task2Id = task2Id;
		this.conflictType = Type.valueOf(conflictType);
		if (conflictDir.equals(">"))
			this.conflictDirection = Direction.Right;
		else if (conflictDir.equals("<"))
			this.conflictDirection = Direction.Left;
		else
			this.conflictDirection = Direction.None;
	}

	public float getConflictWeight() {
		return conflictWeight;
	}

	public void setConflictWeight(float conflictWeight) {
		this.conflictWeight = conflictWeight;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getTask1Id() {
		return task1Id;
	}

	public int getTask2Id() {
		return task2Id;
	}

	public Type getConflictType() {
		return conflictType;
	}

	public Direction getConflictDirection() {
		return conflictDirection;
	}

}
