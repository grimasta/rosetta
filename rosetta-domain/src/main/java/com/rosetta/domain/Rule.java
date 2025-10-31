package com.rosetta.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    private String name;
    private String description;
    private String expression;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
}
