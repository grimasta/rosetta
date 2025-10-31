package com.rosetta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "file_changes")
public class FileChange {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @ManyToOne(optional = false) private FileEntity file;
    @ManyToOne(optional = false) private Commit commit;

    private int additions;
    private int deletions;
    private int churn;

    public Long getId() { return id; }
    public FileEntity getFile() { return file; }
    public void setFile(FileEntity file) { this.file = file; }
    public Commit getCommit() { return commit; }
    public void setCommit(Commit commit) { this.commit = commit; }
    public int getAdditions() { return additions; }
    public void setAdditions(int additions) { this.additions = additions; }
    public int getDeletions() { return deletions; }
    public void setDeletions(int deletions) { this.deletions = deletions; }
    public int getChurn() { return churn; }
    public void setChurn(int churn) { this.churn = churn; }
}
