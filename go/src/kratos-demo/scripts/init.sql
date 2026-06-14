-- kratos-demo 数据库初始化脚本
-- 表结构由 ent 在服务启动时自动迁移(client.Schema.Create),无需手动建表。
-- 此脚本仅创建数据库本身(MySQL 要求库先存在)。
--
-- 用法: mysql -u root -p < configs/init.sql
CREATE DATABASE IF NOT EXISTS kratos_demo
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
