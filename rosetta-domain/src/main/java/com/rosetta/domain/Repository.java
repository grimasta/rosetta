package com.rosetta.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "repositories")
public class Repository {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @Column(unique = true)
    private String url;
    private String defaultBranch;

    @ManyToOne(optional = false)
    private Project project;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files = new ArrayList<>();

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public List<FileEntity> getFiles() { return files; }
}
