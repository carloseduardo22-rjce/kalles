ALTER TABLE sale_item ADD COLUMN IF NOT EXISTS items_order INTEGER;

UPDATE sale_item
SET items_order = subq.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY sale_id ORDER BY id) - 1 AS rn
    FROM sale_item
) subq
WHERE sale_item.id = subq.id;

ALTER TABLE payment ADD COLUMN IF NOT EXISTS payments_order INTEGER;

UPDATE payment
SET payments_order = subq.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY sale_id ORDER BY created_at, id) - 1 AS rn
    FROM payment
) subq
WHERE payment.id = subq.id;
