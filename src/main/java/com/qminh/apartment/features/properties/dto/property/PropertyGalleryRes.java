package com.qminh.apartment.features.properties.dto.property;

public class PropertyGalleryRes {
	private Long fileId;
	private String originalName;
	private String relativePath;
	private String mimeType;
	private String altText;
	private String title;

	public PropertyGalleryRes() {}

	public Long getFileId() { return fileId; }
	public void setFileId(Long fileId) { this.fileId = fileId; }
	public String getOriginalName() { return originalName; }
	public void setOriginalName(String originalName) { this.originalName = originalName; }
	public String getRelativePath() { return relativePath; }
	public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
	public String getMimeType() { return mimeType; }
	public void setMimeType(String mimeType) { this.mimeType = mimeType; }
	public String getAltText() { return altText; }
	public void setAltText(String altText) { this.altText = altText; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
}
