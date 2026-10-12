package com.arinno.canopus.organization.user.contract;

public record UserListItem(Long id, String username, String name, String lastname, Long countProducts, Long time) {
}