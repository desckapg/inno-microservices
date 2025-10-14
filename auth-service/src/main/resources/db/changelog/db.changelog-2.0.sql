-- liquibase formatted sql

-- changeset desckapg:1
INSERT INTO users (login, password_hash) VALUES
('order-service', '$2y$12$3qEpDTkUS5sLvb7HLqXu5OB8xeqO0X67sb5YtNCxtUzh6J2uQj35y')

-- rollback DELETE FROM users WHERE login = 'order-service'

-- changeset desckapg:3
INSERT INTO user_roles(user_id, role) VALUES
((SELECT id FROM users WHERE login = 'order-service'), 'SUPER_ADMIN')

-- rollback DELETE FROM users WHERE user_id = (SELECT id FROM users WHERE login = 'order-service')