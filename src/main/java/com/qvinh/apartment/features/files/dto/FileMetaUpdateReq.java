package com.qvinh.apartment.features.files.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileMetaUpdateReq {

	private String altText;
	private String title;
	private String description;
	private List<String> tags;
}
