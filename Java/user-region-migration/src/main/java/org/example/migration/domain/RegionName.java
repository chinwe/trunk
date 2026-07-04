package org.example.migration.domain;

/**
 * 区域名称。框架对具体 region 不做枚举限制——{@link #of(String)} 可创建任意 region，
 * 长期复用其他区域拆分。下方常量仅为当前场景（新加坡→缅甸）的便利引用，
 * 不构成约束：新区域直接用 {@code RegionName.of("new-region")}。
 */
public record RegionName(String value) {

    /** 便利常量：当前场景的源区域。新区域用 of() 创建。 */
    public static final RegionName SINGAPORE = new RegionName("singapore");
    /** 便利常量：当前场景的目标区域。新区域用 of() 创建。 */
    public static final RegionName MYANMAR = new RegionName("myanmar");

    public static RegionName of(String name) {
        return new RegionName(name);
    }

    @Override
    public String toString() {
        return value;
    }
}
