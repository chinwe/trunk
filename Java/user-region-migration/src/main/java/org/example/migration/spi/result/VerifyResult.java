package org.example.migration.spi.result;

/**
 * 单批租户对账结果。由业务插件的 verify 方法返回。
 */
public class VerifyResult {

    private final boolean passed;
    private final int checkedCount;
    private final int mismatchCount;
    private final String detail;

    private VerifyResult(boolean passed, int checkedCount, int mismatchCount, String detail) {
        this.passed = passed;
        this.checkedCount = checkedCount;
        this.mismatchCount = mismatchCount;
        this.detail = detail;
    }

    /** 对账通过 */
    public static VerifyResult passed(int checkedCount) {
        return new VerifyResult(true, checkedCount, 0, null);
    }

    /** 对账不通过，记录不一致数量与详情 */
    public static VerifyResult failed(int checkedCount, int mismatchCount, String detail) {
        return new VerifyResult(false, checkedCount, mismatchCount, detail);
    }

    /** 业务未实现 verify 时的默认返回 */
    public static VerifyResult unimplemented() {
        return new VerifyResult(true, 0, 0, "verify not implemented by task");
    }

    public boolean isPassed() { return passed; }
    public int getCheckedCount() { return checkedCount; }
    public int getMismatchCount() { return mismatchCount; }
    public String getDetail() { return detail; }
}
