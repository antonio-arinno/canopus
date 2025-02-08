package com.arinno.canopus.entities;

import java.util.Date;



public class ProductResponse {

    private Long id;

	private String name;

	private String description;

	private Date createAt;

    private UserRequest responsible;

    private Integer time;

    public ProductResponse() {
    }
    
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

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public UserRequest getResponsible() {
        return responsible;
    }

    public void setResponsible(UserRequest responsible) {
        this.responsible = responsible;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

}
