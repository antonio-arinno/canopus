package com.arinno.canopus.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ImputationRequest {

    @NotNull
    private LocalDate date;

    @Valid
    @NotEmpty
    private List<ImputationItemRequest> items = new ArrayList<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<ImputationItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ImputationItemRequest> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}
