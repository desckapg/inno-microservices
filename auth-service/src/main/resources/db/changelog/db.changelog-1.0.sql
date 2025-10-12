-- liquibase formatted sql

-- changeset desckapg:1
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    login         VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_id       BIGINT UNIQUE,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- rollback DROP TABLE users;

-- changeset desckapg:2
CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
-- rollback DROP TABLE user_roles;

-- changeset desckapg:3
CREATE UNIQUE INDEX idx_unique_user_login ON users (login);
-- rollback DROP INDEX idx_unique_user_login;