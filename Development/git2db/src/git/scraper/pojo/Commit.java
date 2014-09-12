package git.scraper.pojo;

import java.util.Arrays;
import java.util.List;

import rleano.util.GitUtil;


public class Commit {

	String hash;
	String author;
	String authorMail;
	String authorDate;
	String committer;
	String committerMail;
	String committerDate;
	
	String comment;
	List<String> parents;
	List<String> files;
	
	public Commit(String hash, String gitFolder) {
		List<String> result = GitUtil.getCommit(gitFolder, hash);
		this.hash = result.get(0);
		this.author = result.get(2);
		this.authorMail = result.get(3);
		this.authorDate = result.get(4);
		this.committer = result.get(5);
		this.committerMail = result.get(6);
		this.committerDate = result.get(7);
		this.comment = GitUtil.getCommitComment(gitFolder, hash);
		this.parents = Arrays.asList(result.get(1).split(" "));
	}
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getAuthorMail() {
		return authorMail;
	}
	public void setAuthorMail(String authorMail) {
		this.authorMail = authorMail;
	}
	public String getAuthorDate() {
		return authorDate;
	}
	public void setAuthorDate(String authorDate) {
		this.authorDate = authorDate;
	}
	public String getCommitter() {
		return committer;
	}
	public void setCommitter(String committer) {
		this.committer = committer;
	}
	public String getCommitterMail() {
		return committerMail;
	}
	public void setCommitterMail(String committerMail) {
		this.committerMail = committerMail;
	}
	public String getCommitterDate() {
		return committerDate;
	}
	public void setCommitterDate(String committerDate) {
		this.committerDate = committerDate;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<String> getParents() {
		return parents;
	}
	public void setParents(List<String> parents) {
		this.parents = parents;
	}

	public String getParent(int index) {
		return this.parents.get(index);
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}


}
