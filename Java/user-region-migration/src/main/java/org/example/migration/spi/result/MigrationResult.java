package org.example.migration.spi.result;

/**
 * 单批租户迁移结果。由业务插件的 migrate 方法返回，框架据此统计。
 */
public class MigrationResult {

    private final boolean success;
    private final int migratedCount;
    private final String errorMessage;

    private MigrationResult(boolean success, int migratedCount, String errorMessage) {
        this.success = success;
        this.migratedCount = migratedCount;
        this.errorMessage = errorMessage;
    }

    /** 成功结果 */
    public static MigrationResult success(int migratedCount) {
        return new MigrationResult(true, migratedCount, null);
    }

    /** 失败结果（业务可主动返回失败，框架也会捕获异常标记失败） */
    public static MigrationResult failure(String errorMessage) {
        return new MigrationResult(false, 0, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public int getMigratedCount() { return migratedCount; }
    public String getErrorMessage() { return errorMessage; }
}
