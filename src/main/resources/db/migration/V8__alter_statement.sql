DROP TABLE order_items;

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT,
    product_id BIGINT,
    quantity INTEGER,
    price NUMERIC
);