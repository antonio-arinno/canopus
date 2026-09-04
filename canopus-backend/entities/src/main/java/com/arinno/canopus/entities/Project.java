package com.arinno.canopus.entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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

	private String reference1;

	private String reference2;

	@Column(name = "create_at")
	@Temporal(TemporalType.DATE)
	private LocalDate createAt;	

	@Temporal(TemporalType.DATE)
	private LocalDate  dateDev;	

	@Temporal(TemporalType.DATE)
	private LocalDate  datePre;	

	@Temporal(TemporalType.DATE)
	private LocalDate  datePro;	
		
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getReference1() {
		return reference1;
	}

	public void setReference1(String reference1) {
		this.reference1 = reference1;
	}

	public String getReference2() {
		return reference2;
	}

	public void setReference2(String reference2) {
		this.reference2 = reference2;
	}

	public LocalDate  getCreateAt() {
		return createAt;
	}

	public void setCreateAt(LocalDate createAt) {
		this.createAt = createAt;
	}	
	
	public LocalDate getDateDev() {
		return dateDev;
	}

	public void setDateDev(LocalDate dateDev) {
		this.dateDev = dateDev;
	}

	public LocalDate getDatePre() {
		return datePre;
	}

	public void setDatePre(LocalDate datePre) {
		this.datePre = datePre;
	}

	public LocalDate getDatePro() {
		return datePro;
	}

	public void setDatePro(LocalDate datePro) {
		this.datePro = datePro;
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
}
