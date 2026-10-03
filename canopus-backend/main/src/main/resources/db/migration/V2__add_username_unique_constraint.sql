-- V2__add_username_unique_constraint.sql
-- Añadimos la restricción de unicidad para el nombre de usuario a nivel global
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
