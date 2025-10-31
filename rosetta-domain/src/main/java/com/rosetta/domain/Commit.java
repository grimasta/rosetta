package com.rosetta.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "commits")
public class Commit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    private String commitId;
    private Instant commitDate;
    private String author;
    private String message;

    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileChange> fileChanges = new ArrayList<>();

    public Long getId() { return id; }
    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
    public Instant getCommitDate() { return commitDate; }
    public void setCommitDate(Instant commitDate) { this.commitDate = commitDate; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<FileChange> getFileChanges() { return fileChanges; }
}
