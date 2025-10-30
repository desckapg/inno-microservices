-- liquibase formatted sql

-- changeset desckapg:1
CREATE INDEX idx_orders_user_id ON orders (user_id);
-- rollback DROP INDEX idx_orders_user_id

-- changeset desckapg:2
CREATE INDEX idx_orders_status ON orders (status);
-- rollback DROP INDEX idx_orders_status

-- changeset desckapg:3
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
-- rollback DROP INDEX idx_order_items_order_id

-- changeset desckapg:4
CREATE INDEX idx_order_items_item_id ON order_items (item_id);
-- rollback DROP INDEX idx_order_items_item_id