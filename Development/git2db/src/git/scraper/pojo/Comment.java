package git.scraper.pojo;

public class Comment {
	String author;
	String comment;
	String date;
	
	public Comment(String author, String comment, String date) {
		super();
		this.author = author;
		this.comment = comment;
		this.date = date;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
	
	
}
