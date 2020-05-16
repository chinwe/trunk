package org.responsibilitychain;

public class CollegeApprover extends Approver {

    public CollegeApprover(String name) {
        super(name);
    }

    @Override
    public void handleRequest(PurchaseRequest purchaseRequest) {
        if (purchaseRequest.getPrice() > 5000 && purchaseRequest.getPrice() <= 10000) {
            System.out.println(this.name + " 处理了请求. id = " + purchaseRequest.getId());
        } else {
            approver.handleRequest(purchaseRequest);
        }
    }
}
