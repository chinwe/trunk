package org.example.migration.domain;

/**
 * 中间件客户端类型。框架支持的六类中间件。
 */
public enum ClientType {
    MYSQL,
    REDIS,
    ES,
    S3,
    DYNAMODB,
    KAFKA
}
