package com.qvinh.apartment.shared.api;

public class ApiResponse<T> {

	 private String message;
	 private ErrorInfo error;
	 private T data;
	 private Meta meta;

	 public ApiResponse() {
		 // Intentionally empty: required for frameworks (Jackson/serialization) and factory methods
	 }

	 public static <T> ApiResponse<T> ok(String msg, T data) {
		 ApiResponse<T> res = new ApiResponse<>();
		 res.message = msg;
		 res.data = data;
		 return res;
	 }

	 public static <T> ApiResponse<T> ok(String msg, T data, Meta meta) {
		 ApiResponse<T> res = ok(msg, data);
		 res.meta = meta;
		 return res;
	 }

	 public static <T> ApiResponse<T> error(String code, String msg, Object details) {
		 ApiResponse<T> res = new ApiResponse<>();
		 res.message = "error";
		 res.error = new ErrorInfo(code, msg, details);
		 return res;
	 }

	 public String getMessage() { return message; }
	 public ErrorInfo getError() { return error; }
	 public T getData() { return data; }
	 public Meta getMeta() { return meta; }

	 public static record Meta(int page, int size, long total) {}
	 public static record ErrorInfo(String code, String message, Object details) {}
}

