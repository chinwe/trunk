package org.example.migration.spi;

import java.util.List;

/**
 * 切流动作 SPI。业务组件实现，在 migrate 完成且总量对账闸门通过后由框架调用。
 *
 * 典型实现：踢出用户登录（删 Redis token / 调鉴权接口），让用户重登时重定向到新区域。
 * 具体会话机制由业务决定，框架不假设。
 *
 * <p><b>幂等契约（必须遵守，ADR-0005 Q4）</b>：本方法必须幂等——对同一批租户调用 N 次
 * 与 1 次的结果等价。两阶段迁移下，CORE 切流与 SECONDARY 全程迁移之间可达数小时，
 * 崩溃恢复时框架会重做 evict（"先 evict 后写状态"的顺序使然）。非幂等的踢登录语义
 * （如"触发推送通知用户被踢"）会破坏此契约，业务不得使用。典型幂等实现：删 Redis token、
 * 调"删除会话"语义的鉴权接口。
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
