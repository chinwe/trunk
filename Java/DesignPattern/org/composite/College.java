package org.composite;

import java.util.ArrayList;
import java.util.List;

public class College extends OrganizationComponent {

	List<OrganizationComponent> organizationComponents = new ArrayList<OrganizationComponent>();

	public College(String name, String desc) {
		super(name, desc);
	}
	
	@Override
	protected void print() {
		System.out.println("----" + getName() + "----");
		for (OrganizationComponent organizationComponent : organizationComponents) {
			organizationComponent.print();
		}
	}

	@Override
	protected void add(OrganizationComponent organizationComponent) {
		organizationComponents.add(organizationComponent);
	}
	
	@Override
	protected void remove(OrganizationComponent organizationComponent) {
		organizationComponents.remove(organizationComponent);
	}
}
