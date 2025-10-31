package com.rosetta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "violations")
public class Violation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @ManyToOne(optional = false) private Rule rule;
    @ManyToOne(optional = false) private FileEntity file;
    @ManyToOne(optional = false) private Commit commit;
    private String details;

    public Long getId() { return id; }
    public Rule getRule() { return rule; }
    public void setRule(Rule rule) { this.rule = rule; }
    public FileEntity getFile() { return file; }
    public void setFile(FileEntity file) { this.file = file; }
    public Commit getCommit() { return commit; }
    public void setCommit(Commit commit) { this.commit = commit; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
