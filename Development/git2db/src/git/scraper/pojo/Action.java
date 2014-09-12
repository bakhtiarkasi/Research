package git.scraper.pojo;

public class Action {
	String author;
	String field;
	String action;
	String oldValue;
	String newValue;
	String date;
	
	public Action() {
		super();
		this.author = "";
		this.field = "";
		this.action = "";
		this.oldValue = "";
		this.newValue = "";
		this.date = "";
	}
	
	public Action(String author, String field, String action, String oldValue, String newValue, String date) {
		super();
		this.author = author;
		this.field = field;
		this.action = action;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
	
	
}
