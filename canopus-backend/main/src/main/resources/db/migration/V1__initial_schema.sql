-- Initial schema migration for Canopus application

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    create_at DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(12) NOT NULL,
    name VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uk_users_roles UNIQUE (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS technologies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    create_at DATE,
    responsible_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_technologies_responsible FOREIGN KEY (responsible_id) REFERENCES users (id),
    CONSTRAINT fk_technologies_company FOREIGN KEY (company_id) REFERENCES companies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users_technologies (
    user_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    CONSTRAINT uk_users_technologies UNIQUE (user_id, technology_id),
    CONSTRAINT fk_users_technologies_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_technologies_technology FOREIGN KEY (technology_id) REFERENCES technologies (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    create_at DATE,
    technology_id BIGINT NOT NULL,
    responsible_id BIGINT NOT NULL,
    backup_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT uk_products_company_name UNIQUE (company_id, name),
    CONSTRAINT fk_products_technology FOREIGN KEY (technology_id) REFERENCES technologies (id),
    CONSTRAINT fk_products_responsible FOREIGN KEY (responsible_id) REFERENCES users (id),
    CONSTRAINT fk_products_backup FOREIGN KEY (backup_id) REFERENCES users (id),
    CONSTRAINT fk_products_company FOREIGN KEY (company_id) REFERENCES companies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    reference1 VARCHAR(255),
    reference2 VARCHAR(255),
    create_at DATE,
    date_dev DATE,
    date_pre DATE,
    date_pro DATE,
    product_id BIGINT NOT NULL,
    responsible_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    CONSTRAINT uk_projects_company_product_name UNIQUE (company_id, product_id, name),
    CONSTRAINT fk_projects_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_projects_responsible FOREIGN KEY (responsible_id) REFERENCES users (id),
    CONSTRAINT fk_projects_company FOREIGN KEY (company_id) REFERENCES companies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects_contributors (
    project_id BIGINT NOT NULL,
    contribuitor_id BIGINT NOT NULL,
    CONSTRAINT fk_projects_contributors_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_projects_contributors_user FOREIGN KEY (contribuitor_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS imputations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    CONSTRAINT uk_imputations_date_user UNIQUE (date, user_id),
    CONSTRAINT fk_imputations_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS imputations_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    imputation_id BIGINT,
    project_id BIGINT NOT NULL,
    time INT,
    CONSTRAINT fk_imputations_items_imputation FOREIGN KEY (imputation_id) REFERENCES imputations (id) ON DELETE CASCADE,
    CONSTRAINT fk_imputations_items_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER'), (2, 'ROLE_ADMIN');
