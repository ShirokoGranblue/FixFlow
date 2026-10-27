ALTER TABLE ticket
    DROP FOREIGN KEY fk_ticket_category;

ALTER TABLE ticket
    MODIFY COLUMN category_id BIGINT NOT NULL;

ALTER TABLE ticket
    ADD CONSTRAINT fk_ticket_category
    FOREIGN KEY (category_id) REFERENCES category (id);