package com.arinno.canopus.entities;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "imputations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date" , "user_id"})})
public class Imputation {

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	
	
	@JoinColumn(nullable = false)	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @ManyToOne(fetch = FetchType.LAZY)	
    private User user;		
	
	@Column(nullable = false)	
	@Temporal(TemporalType.DATE)
	private Date date;	
	
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "imputation_id")	
	private List<ImputationItem> items;
    	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}	
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<ImputationItem> getItems() {
		return items;
	}

	public void setItems(List<ImputationItem> items) {
		this.items = items;
	}
	
	public Integer getTotal() {
		Integer total = 0;
		for(ImputationItem item: items) {
			total += item.getTime();
		}
		return total;
	}

	@Override
	public String toString() {
		return "Imputation [id=" + id + ", user=" + user + ", date=" + date + ", items=" + items + "]";
	}

	

}
