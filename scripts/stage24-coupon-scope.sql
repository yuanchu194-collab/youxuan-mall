USE youxuan_mall;

ALTER TABLE coupon
    ADD COLUMN IF NOT EXISTS scope_type VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '适用范围 ALL全部商品 CATEGORY指定分类' AFTER status;

ALTER TABLE coupon
    ADD COLUMN IF NOT EXISTS category_id BIGINT DEFAULT NULL COMMENT '适用分类ID' AFTER scope_type;

UPDATE coupon
SET scope_type = 'ALL'
WHERE scope_type IS NULL OR scope_type = '';

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'coupon'
      AND index_name = 'idx_scope_category'
);
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX idx_scope_category ON coupon (scope_type, category_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
