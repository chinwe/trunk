package org.composite;

public class Client {

	public static void main(String[] args) {
		OrganizationComponent university = new University("CS大学", "中国顶尖学校");
		
		OrganizationComponent csCollege = new College("计算机学院", "计算机学院");
		csCollege.add(new Department("软件工程", "软件工程"));
		csCollege.add(new Department("计算机科学与技术", "计算机科学与技术"));

		OrganizationComponent ieCollege = new College("信息工程学院", "信息工程学院");
		ieCollege.add(new Department("通信工程", "通信工程"));
		ieCollege.add(new Department("信息工程", "信息工程"));
		
		university.add(csCollege);
		university.add(ieCollege);
		university.print();
	}

}
