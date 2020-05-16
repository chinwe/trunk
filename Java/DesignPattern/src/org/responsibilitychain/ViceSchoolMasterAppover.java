package org.responsibilitychain;

public class ViceSchoolMasterAppover extends Approver {

    public ViceSchoolMasterAppover(String name) {
        super(name);
    }

    @Override
    public void handleRequest(PurchaseRequest purchaseRequest) {
        if (purchaseRequest.getPrice() > 10000 && purchaseRequest.getPrice() <= 30000) {
            System.out.println(this.name + " 处理了请求. id = " + purchaseRequest.getId());
        } else {
            approver.handleRequest(purchaseRequest);
        }
    }
}
