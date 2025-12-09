package com.qminh.apartment.dto.property;

public class PropertyTypeInfo {
	private Integer typeId;
	private String typeName;

	public PropertyTypeInfo() {}

	public PropertyTypeInfo(Integer typeId, String typeName) {
		this.typeId = typeId;
		this.typeName = typeName;
	}

	public Integer getTypeId() { return typeId; }
	public void setTypeId(Integer typeId) { this.typeId = typeId; }
	public String getTypeName() { return typeName; }
	public void setTypeName(String typeName) { this.typeName = typeName; }
}
