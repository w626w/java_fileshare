CREATE DATABASE IF NOT EXISTS file_manager;

USE file_manager;



CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50),
    is_public BOOLEAN DEFAULT TRUE,
    uploader_id BIGINT NOT NULL,
    upload_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (uploader_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS file_shares (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    share_time DATETIME NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    operation_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE files MODIFY COLUMN file_type VARCHAR(100);

-- 检查并添加 role 列
SET @dbname = 'file_manager';
SET @tablename = 'users';
SET @columnname = 'role';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " VARCHAR(20) DEFAULT 'ROLE_USER'")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查用户表数据
SELECT * FROM users;

-- 检查文件表数据
SELECT * FROM files;

-- 检查关联查询结果
SELECT f.*, u.username as uploaderName
FROM files f
         LEFT JOIN users u ON f.uploader_id = u.id;

-- 假设你已经注册了用户名为 'admin' 的账号
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'admin';