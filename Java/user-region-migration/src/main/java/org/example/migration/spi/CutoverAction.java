package org.example.migration.spi;

import java.util.List;

/**
 * 切流动作 SPI。业务组件实现，在 migrate 完成且总量对账闸门通过后由框架调用。
 *
 * 典型实现：踢出用户登录（删 Redis token / 调鉴权接口），让用户重登时重定向到新区域。
 * 具体会话机制由业务决定，框架不假设。
 */
public interface CutoverAction {

    /**
     * 切流时框架调用：业务实现踢登录、清理会话等切流动作。
     *
     * @param ctx       迁移上下文
     * @param tenantIds 已迁移完成的全部租户ID
     * @param product   产品标识
     * @param bizLine   业务线标识
     */
    void evict(MigrationContext ctx, List<String> tenantIds, String product, String bizLine);
}
