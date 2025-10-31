package com.rosetta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "metrics")
public class Metric {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    @Column(unique = true, nullable = false)
    private String key;
    private String unit;

    public Long getId() { return id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
