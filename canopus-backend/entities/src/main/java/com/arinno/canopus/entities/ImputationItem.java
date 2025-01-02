package com.arinno.canopus.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="imputations_items")
public class ImputationItem {
   	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	
		
	@JoinColumn(nullable = false)	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })	
	@ManyToOne(fetch = FetchType.LAZY)
	private Project project;
	
	private Integer time;	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "ImputationItem [id=" + id + ", project=" + project + ", time=" + time + "]";
	}


}
