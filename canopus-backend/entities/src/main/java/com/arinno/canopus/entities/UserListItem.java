package com.arinno.canopus.entities;

public record UserListItem(Long id, String username, String name, String lastname, Long countProducts, Long time) {
}