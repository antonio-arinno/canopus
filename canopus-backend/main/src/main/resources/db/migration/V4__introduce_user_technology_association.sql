SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'uk_users_id_company'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD CONSTRAINT uk_users_id_company UNIQUE (id, company_id)'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'technologies' AND index_name = 'uk_technologies_id_company'
    ),
    'SELECT 1',
    'ALTER TABLE technologies ADD CONSTRAINT uk_technologies_id_company UNIQUE (id, company_id)'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users_technologies' AND column_name = 'company_id'
    ),
    'SELECT 1',
    'ALTER TABLE users_technologies ADD COLUMN company_id BIGINT NULL AFTER technology_id'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

UPDATE users_technologies ut
JOIN users u ON u.id = ut.user_id
SET ut.company_id = u.company_id;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users_technologies'
          AND column_name = 'company_id' AND is_nullable = 'NO'
    ),
    'SELECT 1',
    'ALTER TABLE users_technologies MODIFY company_id BIGINT NOT NULL'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @foreign_key_name = (
    SELECT k.constraint_name
    FROM information_schema.key_column_usage k
    WHERE k.constraint_schema = DATABASE()
      AND k.table_name = 'users_technologies'
      AND k.column_name = 'user_id'
      AND k.referenced_table_name = 'users'
      AND (SELECT COUNT(*) FROM information_schema.key_column_usage k2
           WHERE k2.constraint_schema = k.constraint_schema
             AND k2.table_name = k.table_name
             AND k2.constraint_name = k.constraint_name) = 1
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE users_technologies DROP FOREIGN KEY `', @foreign_key_name, '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @foreign_key_name = (
    SELECT k.constraint_name
    FROM information_schema.key_column_usage k
    WHERE k.constraint_schema = DATABASE()
      AND k.table_name = 'users_technologies'
      AND k.column_name = 'technology_id'
      AND k.referenced_table_name = 'technologies'
      AND (SELECT COUNT(*) FROM information_schema.key_column_usage k2
           WHERE k2.constraint_schema = k.constraint_schema
             AND k2.table_name = k.table_name
             AND k2.constraint_name = k.constraint_name) = 1
    LIMIT 1
);
SET @migration_sql = IF(
    @foreign_key_name IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE users_technologies DROP FOREIGN KEY `', @foreign_key_name, '`')
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.key_column_usage
        WHERE constraint_schema = DATABASE() AND table_name = 'users_technologies'
          AND constraint_name = 'fk_users_technologies_user_company'
    ),
    'SELECT 1',
    'ALTER TABLE users_technologies ADD CONSTRAINT fk_users_technologies_user_company FOREIGN KEY (user_id, company_id) REFERENCES users (id, company_id) ON DELETE CASCADE'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @migration_sql = IF(
    EXISTS (
        SELECT 1 FROM information_schema.key_column_usage
        WHERE constraint_schema = DATABASE() AND table_name = 'users_technologies'
          AND constraint_name = 'fk_users_technologies_technology_company'
    ),
    'SELECT 1',
    'ALTER TABLE users_technologies ADD CONSTRAINT fk_users_technologies_technology_company FOREIGN KEY (technology_id, company_id) REFERENCES technologies (id, company_id) ON DELETE CASCADE'
);
PREPARE migration_statement FROM @migration_sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;