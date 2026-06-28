-- ====================================================================
-- springboot-mall 資料庫建置腳本
-- 資料庫：MySQL 8.x
-- 用途：建立 mall 資料庫與所需資料表（product / user / order / order_item）
--
-- 使用方式：
--   mysql -u root -p < sql/mall.sql
-- 或在 MySQL Workbench 開啟此檔案後直接執行。
-- ====================================================================

-- 建立資料庫（若不存在），並使用 utf8mb4 以支援中文與表情符號
CREATE DATABASE IF NOT EXISTS mall
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE mall;

-- --------------------------------------------------------------------
-- 商品資料表 product
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product (
    product_id          INT             NOT NULL AUTO_INCREMENT,
    product_name        VARCHAR(128)    NOT NULL,
    category            VARCHAR(32)      NOT NULL,            -- 對應 enum：FOOD / CAR / BOOK
    image_url           VARCHAR(256)    NOT NULL,
    price               INT             NOT NULL,
    stock               INT             NOT NULL,
    description         VARCHAR(1024)   NULL,
    created_date        TIMESTAMP       NOT NULL,
    last_modified_date  TIMESTAMP       NOT NULL,
    PRIMARY KEY (product_id)
);

-- --------------------------------------------------------------------
-- 會員資料表 user
-- email 設為唯一鍵，避免重複註冊
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user (
    user_id             INT             NOT NULL AUTO_INCREMENT,
    email               VARCHAR(256)    NOT NULL,
    password            VARCHAR(256)    NOT NULL,             -- 以 MD5 雜湊後儲存
    created_date        TIMESTAMP       NOT NULL,
    last_modified_date  TIMESTAMP       NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY email (email)
);

-- --------------------------------------------------------------------
-- 訂單資料表 order
-- 注意：order 為 MySQL 保留字，使用時需以反引號 `order` 包住
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order` (
    order_id            INT             NOT NULL AUTO_INCREMENT,
    user_id             INT             NOT NULL,
    total_amount        INT             NOT NULL,
    created_date        TIMESTAMP       NOT NULL,
    last_modified_date  TIMESTAMP       NOT NULL,
    PRIMARY KEY (order_id)
);

-- --------------------------------------------------------------------
-- 訂單明細資料表 order_item
-- 一筆訂單可包含多個商品項目
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id       INT             NOT NULL AUTO_INCREMENT,
    order_id            INT             NOT NULL,
    product_id          INT             NOT NULL,
    quantity            INT             NOT NULL,
    amount              INT             NOT NULL,
    PRIMARY KEY (order_item_id)
);

-- ====================================================================
-- （選用）測試用初始資料
-- 若不需要可自行刪除以下區塊
-- ====================================================================
INSERT INTO product (product_name, category, image_url, price, stock, description, created_date, last_modified_date)
VALUES
    ('蘋果（一斤）', 'FOOD', 'http://example.com/apple.png', 80,  10, '進口富士蘋果', NOW(), NOW()),
    ('蛋糕',         'FOOD', 'http://example.com/cake.png',  120, 5,  '巧克力生日蛋糕', NOW(), NOW()),
    ('Toyota 模型車','CAR',  'http://example.com/car.png',   500, 8,  '1:18 合金模型車', NOW(), NOW()),
    ('Java 入門書',  'BOOK', 'http://example.com/book.png',  450, 20, 'Spring Boot 教學書', NOW(), NOW());
