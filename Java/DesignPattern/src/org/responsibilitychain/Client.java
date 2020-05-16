package org.responsibilitychain;

public class Client {
    public static void main(String[] args) {

        SchoolMasterApprover schoolMasterApprover = new SchoolMasterApprover("School Master");

        ViceSchoolMasterAppover viceSchoolMasterAppover = new ViceSchoolMasterAppover("Vice School Master");
        viceSchoolMasterAppover.setApprover(schoolMasterApprover);

        CollegeApprover collegeApprover = new CollegeApprover("College");
        collegeApprover.setApprover(viceSchoolMasterAppover);

        DepartmentApprover departmentApprover = new DepartmentApprover("Department");
        departmentApprover.setApprover(collegeApprover);


        PurchaseRequest purchaseRequest = new PurchaseRequest(0, 12000, 1);
        departmentApprover.handleRequest(purchaseRequest);

        PurchaseRequest purchaseRequest2 = new PurchaseRequest(0, 200, 2);
        departmentApprover.handleRequest(purchaseRequest2);

        PurchaseRequest purchaseRequest3 = new PurchaseRequest(0, 50000, 3
        );
        departmentApprover.handleRequest(purchaseRequest3);
    }
}
