-- V3__add_email_unique_constraint.sql
-- Añadimos la restricción de unicidad para el correo electrónico a nivel global
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
