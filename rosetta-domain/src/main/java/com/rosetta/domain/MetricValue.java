package com.rosetta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "metric_values")
public class MetricValue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @ManyToOne(optional = false) private Metric metric;
    @ManyToOne(optional = false) private FileEntity file;
    @ManyToOne(optional = false) private Commit commit;

    private double value;

    public Long getId() { return id; }
    public Metric getMetric() { return metric; }
    public void setMetric(Metric metric) { this.metric = metric; }
    public FileEntity getFile() { return file; }
    public void setFile(FileEntity file) { this.file = file; }
    public Commit getCommit() { return commit; }
    public void setCommit(Commit commit) { this.commit = commit; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
