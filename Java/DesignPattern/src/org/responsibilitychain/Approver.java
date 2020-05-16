package org.responsibilitychain;

public abstract class Approver {

    // 下一个处理者
    Approver approver;

    String name;

    public Approver(String name) {
        this.name = name;
    }

    public void setApprover(Approver approver) {
        this.approver = approver;
    }

    public abstract void handleRequest(PurchaseRequest purchaseRequest);

}
