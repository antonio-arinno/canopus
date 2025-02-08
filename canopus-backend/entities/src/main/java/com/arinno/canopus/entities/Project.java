package com.arinno.canopus.entities;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "projects", uniqueConstraints = {
@UniqueConstraint(columnNames = {"company_id" ,"product_id", "name"})})
public class Project {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)	
	private String name;

	private String description;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "create_at")
	@Temporal(TemporalType.DATE)
	private Date createAt;	
		
	@JoinColumn(nullable = false)	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "projects" })
    @ManyToOne(fetch = FetchType.LAZY)	
	private Product product;
	
	@JoinColumn(nullable = false)	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)		
	private User responsible;	
	
	@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler"},allowSetters = true)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
        joinColumns = {@JoinColumn(name="project_id")},
        inverseJoinColumns = @JoinColumn(name="contribuitor_id")
    )
	private List<User> contributors;

 
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")	
	private List<ImputationItem> imputationItems;	

	@JoinColumn(nullable = false)	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)	
    private Company company;	

    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}		
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	public User getResponsible() {
		return responsible;
	}

	public void setResponsible(User responsible) {
		this.responsible = responsible;
	}		

	public List<User> getContributors() {
		return contributors;
	}

	public void setContributors(List<User> contributors) {
		this.contributors = contributors;
	}

	public Company getCompany() {
		return company;
	}
 
	public void setCompany(Company company) {
		this.company = company;
	}
		
	public Integer getTime() {
		Integer time = 0;
		if (imputationItems != null){
			for(ImputationItem imputationItem: imputationItems) {
				time += imputationItem.getTime();
			}
		}
		return time;
	}	
 
}
