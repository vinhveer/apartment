## Quy tắc viết test (Controller, Integration, Repository, Service) và cách xử lý lỗi thường gặp

### Mục tiêu

* Test phải rõ ràng, ổn định, dễ bảo trì.
* Tránh cảnh báo liên quan đến null-safety, generic variance, hoặc resource leak.

---

## Quy ước chung

* **Tên test**: dùng `@DisplayName("...")` để mô tả hành vi (behavior), không mô tả kỹ thuật.
* **Bố cục**: tuân theo cấu trúc “Arrange – Act – Assert”, cách nhau bằng dòng trống để dễ đọc.
* **Cách assert**:

  * Với Controller: ưu tiên `jsonPath(...).value(...)` hoặc `.exists()` để tránh cảnh báo generic.
  * Với Service và Repository: ưu tiên AssertJ (`assertThat(...)`) thay cho Hamcrest.
* **Null-safety**: luôn kiểm tra và chuẩn hóa dữ liệu đầu vào; tránh ép kiểu hoặc unchecked conversion.
* **Testcontainers**: khởi tạo qua factory method; JUnit sẽ tự quản lý lifecycle khi dùng `@Testcontainers` và `@Container`.

---

## Controller Test

* **Annotation khuyến nghị**:

  * Dùng `@WebMvcTest` khi chỉ test web layer (nhanh hơn, cô lập).
  * Dùng `@SpringBootTest` + `@AutoConfigureMockMvc` khi cần toàn bộ context (integration).
* **Gửi request JSON**:

  * Dùng `MediaType.APPLICATION_JSON_VALUE` khi API yêu cầu chuỗi MIME.
  * Dùng `ObjectMapper` để serialize body, đảm bảo request không null.
* **Kiểm tra kết quả JSON**:

  * Dùng `jsonPath(...).value(...)` để so sánh giá trị.
  * Dùng `jsonPath(...).exists()` để xác nhận có trường dữ liệu, thay cho `notNullValue()`.

---

## Integration Test (IT)

* Mục tiêu là kiểm thử logic tích hợp giữa các module trong Spring context.
* Chạy với DB thật thông qua Testcontainers.
* Dùng `@DynamicPropertySource` để map các thông số datasource từ container vào Spring context.
* Không cần tự `start()` hoặc `stop()` container; JUnit quản lý tự động.
* Không đồng nhất với E2E test — IT chỉ kiểm tra mức ứng dụng, không build image của app.

---

## Repository Test

* Dùng `@DataJpaTest` để test repository với transaction rollback tự động.
* Về DataSource:
  * Nếu classpath có embedded DB (ví dụ H2), thêm `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` để dùng Postgres từ Testcontainers.
  * Nếu không có embedded DB trong classpath, chỉ cần kế thừa base Testcontainers là đủ (giống các test hiện tại).
* Dùng `saveAndFlush` khi cần sinh ID ngay để assert.
* Kiểm tra ràng buộc unique hoặc ngoại lệ bằng `assertThatThrownBy(...)`.
* Luôn xác nhận entity đã có ID trước khi sử dụng trong test khác.

---

## Service Test

* Chọn loại test phù hợp:

  * **Unit test (Mockito)** khi service chỉ có logic thuần, không cần Spring context.
  * **Integration test (SpringBootTest + Testcontainers)** khi service có tương tác DB, transaction, hoặc dependency thật.
* Có thể dùng `@Transactional` để rollback sau mỗi test.

---

## Lỗi thường gặp và cách xử lý

* **Null type safety**: luôn kiểm tra đầu vào và dữ liệu trả về; không dùng ép kiểu.
* **Generic variance (Hamcrest)**: tránh dùng `is(...)` hoặc `notNullValue()` với `jsonPath`.
* **Resource leak (Testcontainers)**: không khởi tạo container trong constructor hay mỗi test case; dùng static field với `@Container`.
* **Import thừa hoặc wildcard**: tránh `import *` để không gây nhập nhằng overload.
* **Transaction trong @DataJpaTest**: mặc định rollback; dùng `saveAndFlush` nếu cần persist thật.
* **Flaky test theo múi giờ**: inject `Clock` vào service, mock hoặc fix thời gian trong test.

---

## Checklist nhanh

* **Controller**:

  * Dùng `APPLICATION_JSON_VALUE` khi gửi request.
  * Serialize body bằng `ObjectMapper`.
  * Assert qua `jsonPath(...).value()` hoặc `.exists()`.

* **Integration**:

  * Base Testcontainers có factory method riêng.
  * `@DynamicPropertySource` map datasource vào Spring context.

* **Repository**:

  * Dùng `saveAndFlush` để lấy ID.
  * Xác nhận ID tồn tại trước khi assert hoặc truyền tiếp.

* **Service**:

  * Dùng Mockito cho unit test.
  * Dùng Testcontainers + SpringBootTest cho integration test.