package com.qminh.apartment.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final String uploadBasePath;

	public WebMvcConfig(@Value("${app.upload.base-path:./uploads}") String uploadBasePath) {
		this.uploadBasePath = uploadBasePath;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Map /public/** URL to uploads/public/ folder
		registry.addResourceHandler("/public/**")
			.addResourceLocations("file:" + uploadBasePath + "/public/");
	}
}
