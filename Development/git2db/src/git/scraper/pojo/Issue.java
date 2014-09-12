package git.scraper.pojo;

import java.util.ArrayList;
import java.util.List;

public class Issue {
	String id;
	
	String title;
	
	String reporter;
	
	String reporterName;
	
	String assignee;
	
	String assigneeName;
	
	String component;
	
	String description;
	
	String dateIssued;
	
	String dateSolved;
	
	String status;
	
	String priority;
	
	String resolution;
	
	List<Comment> comments = new ArrayList<Comment>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDateIssued() {
		return dateIssued;
	}

	public void setDateIssued(String dateIssued) {
		this.dateIssued = dateIssued;
	}

	public String getDateSolved() {
		return dateSolved;
	}

	public void setDateSolved(String dateSolved) {
		this.dateSolved = dateSolved;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	
	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public List<Comment> getComments() {
		return this.comments;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getAssigneeName() {
		return assigneeName;
	}

	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}
	
	
	
}
