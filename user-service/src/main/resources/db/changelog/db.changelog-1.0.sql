-- liquibase formatted sql

-- changeset desckapg:1
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  name VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,
  birth_date DATE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL
);

-- changeset desckapg:2
CREATE TABLE cards_info (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  number VARCHAR(255) UNIQUE NOT NULL,
  holder VARCHAR(255) NOT NULL,
  expiration_date DATE NOT NULL
);

-- changeset desckapg:3
CREATE UNIQUE INDEX idx_unique_user_email ON users(email);
