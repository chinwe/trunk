package org.example.migration.shell;

import org.example.migration.spi.CutoverAction;
import org.example.migration.spi.TenantMigrationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Shell 层自动装配：构造 TaskRegistry 并自动收集业务插件 Bean。
 *
 * 业务插件只需实现 TenantMigrationTask / CutoverAction 并标注为 Spring Bean，
 * 即被自动注册到 TaskRegistry，无需手动声明。
 */
@Configuration
public class ShellAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ShellAutoConfiguration.class);

    @Bean
    public TaskRegistry taskRegistry(List<TenantMigrationTask> tasks,
                                     Map<String, CutoverAction> cutoverActions) {
        TaskRegistry registry = new TaskRegistry();
        for (TenantMigrationTask task : tasks) {
            registry.register(task);
            log.info("registered migration task: {}", task.taskName());
        }
        // CutoverAction 的 bean 名默认与实现类名相关，业务可通过 @Component("user-migration") 对齐 taskName
        cutoverActions.forEach((beanName, action) -> {
            registry.registerCutover(beanName, action);
            log.info("registered cutover action for task: {}", beanName);
        });
        return registry;
    }
}
