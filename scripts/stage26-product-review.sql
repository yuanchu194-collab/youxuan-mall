USE youxuan_mall;

CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名快照',
    rating TINYINT NOT NULL COMMENT '评分 1-5',
    content VARCHAR(500) NOT NULL COMMENT '评价内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    UNIQUE KEY uk_user_product_review (user_id, product_id),
    KEY idx_product_time (product_id, deleted, create_time),
    KEY idx_product_rating (product_id, rating)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';
