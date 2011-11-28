package cbit.vcell.geometry;

import cbit.vcell.render.Vect3d;

public class CSGPseudoPrimitive extends CSGNode {
	private String csgObjectName = null;
	private CSGObject csgObject = null;

	public CSGPseudoPrimitive(String name, String csgObjectName){
		super(name);
		this.csgObjectName = csgObjectName;
	}
	
	public CSGPseudoPrimitive(String name, CSGObject csgObject){
		this(name, csgObject.getName());
	}
	
	public CSGPseudoPrimitive(CSGPseudoPrimitive csgPseudoPrimitive){
		this(csgPseudoPrimitive.getName(), csgPseudoPrimitive.csgObjectName);
	}
	
	@Override
	public CSGNode cloneTree() {
		return new CSGPseudoPrimitive(this);
	}

	@Override
	public boolean isInside(Vect3d point) {
		return csgObject.getRoot().isInside(point);
	}

	public String getCsgObjectName() {
		return csgObjectName;
	}

	public void setCsgObjectName(String csgObjectName) {
		this.csgObjectName = csgObjectName;
	}

	public CSGObject getCsgObject() {
		return csgObject;
	}

	public void setCsgObject(CSGObject csgObject) {
		this.csgObject = csgObject;
	}
	

}
