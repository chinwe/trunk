package org.example.migration.domain;

/**
 * 区域名称。框架对具体 region 不做枚举限制（长期复用其他区域拆分），
 * 但提供常用区域常量便于代码引用。
 *
 * 通过 of(name) 可创建任意 region，不限于预定义常量。
 */
public record RegionName(String value) {

    public static final RegionName SINGAPORE = new RegionName("singapore");
    public static final RegionName MYANMAR = new RegionName("myanmar");

    public static RegionName of(String name) {
        return new RegionName(name);
    }

    @Override
    public String toString() {
        return value;
    }
}
