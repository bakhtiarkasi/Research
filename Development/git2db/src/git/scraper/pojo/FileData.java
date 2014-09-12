package git.scraper.pojo;

public class FileData {
	int commitId;
	String commitHash;
	
	int issueId = 0;
	String issueKey = "NONE";
	
	String filename;
	String author;
	String committer;
	String date;
	
	int authors = 0;
	int committers = 0;
	int loc = 0;
	int code = 0;
	int comment = 0;
	int frequency = 0;
	
	float commentRatio = 0;
	public int getCommitId() {
		return commitId;
	}
	public void setCommitId(int commitId) {
		this.commitId = commitId;
	}
	public String getCommitHash() {
		return commitHash;
	}
	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}
	public int getIssueId() {
		return issueId;
	}
	public void setIssueId(int issueId) {
		this.issueId = issueId;
	}
	public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCommitter() {
		return committer;
	}
	public void setCommitter(String committer) {
		this.committer = committer;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getAuthors() {
		return authors;
	}
	public void setAuthors(int authors) {
		this.authors = authors;
	}
	public int getCommitters() {
		return committers;
	}
	public void setCommitters(int committers) {
		this.committers = committers;
	}
	public int getLoc() {
		return loc;
	}
	public void setLoc(int loc) {
		this.loc = loc;
	}
	public float getCommentRatio() {
		return commentRatio;
	}
	public void setCommentRatio(float commentRatio) {
		this.commentRatio = commentRatio;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public int getComment() {
		return comment;
	}
	public void setComment(int comment) {
		this.comment = comment;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	
	
	
	
}
