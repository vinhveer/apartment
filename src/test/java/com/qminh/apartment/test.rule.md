## Quy tắc viết test (Controller, Integration, Repository, Service) và cách xử lý lỗi thường gặp

### Mục tiêu

* Test phải rõ ràng, ổn định, dễ bảo trì.
* Tránh cảnh báo liên quan đến null-safety, generic variance, hoặc resource leak.
* Kết quả test phải độc lập môi trường: chạy được với Docker (Testcontainers) hoặc DB cục bộ (qua biến môi trường).

---

## Quy ước chung

* **Tên test**: dùng `@DisplayName("...")` để mô tả hành vi (behavior), không mô tả kỹ thuật.
* **Bố cục**: tuân theo cấu trúc “Arrange – Act – Assert”, cách nhau bằng dòng trống để dễ đọc.
* **Cách assert**:

  * Với Controller: ưu tiên `jsonPath(...).value(...)` hoặc `.exists()` để tránh cảnh báo generic.
  * Với Service và Repository: ưu tiên AssertJ (`assertThat(...)`) thay cho Hamcrest.
* **Null-safety**: luôn kiểm tra và chuẩn hóa dữ liệu đầu vào; tránh ép kiểu hoặc unchecked conversion.
* **Quy ước API envelope**: mọi response dạng `ApiResponse{ message, data, meta? }`. Luôn assert `message` và trường trọng yếu của `data`.
* **Migration**: tên file theo `V{version}__{desc}.sql`, đặt tại `classpath:db/migration`. Test rely vào Flyway để chuẩn hóa schema.
* **Random test data**: tránh va chạm unique bằng hậu tố ngẫu nhiên khi cần (ví dụ thêm `System.nanoTime()`), trừ khi chủ đích test unique.

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
* **Ví dụ kiểm thử CRUD**:

  ```java
  @SpringBootTest
  @AutoConfigureMockMvc
  class PropertyTypeControllerIT extends PostgresTestContainer {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    @Test
    @DisplayName("POST/GET/PUT/DELETE /api/property-types works and returns ApiResponse")
    void crud_flow() throws Exception {
      var req = new PropertyTypeCreateReq();
      req.setTypeName("Apartment");

      var createRes = mockMvc.perform(post("/api/property-types")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Create property type successfully"))
        .andExpect(jsonPath("$.data.typeId").exists())
        .andReturn().getResponse().getContentAsString();

      int id = new ObjectMapper().readTree(createRes).path("data").path("typeId").asInt();

      mockMvc.perform(get("/api/property-types/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Property type detail"))
        .andExpect(jsonPath("$.data.typeName").value("Apartment"));
    }
  }
  ```

---

## Integration Test (IT)

* Mục tiêu là kiểm thử logic tích hợp giữa các module trong Spring context.
* Ưu tiên chạy với DB thật thông qua Testcontainers, nhưng có thể fallback dùng DB cục bộ khi set biến môi trường.
* Dùng `@DynamicPropertySource` để map các thông số datasource vào Spring context.
* Không đồng nhất với E2E test — IT chỉ kiểm tra mức ứng dụng, không build image của app.
* **Chạy test**:
  * Với Docker (đề xuất): `./gradlew test`
  * Không có Docker (dùng DB cục bộ):
    ```bash
    DB_URL=jdbc:postgresql://localhost:5432/apartment \
    DB_USERNAME=postgres \
    DB_PASSWORD=postgres \
    ./gradlew test
    ```

---

## Repository Test

* Dùng `@DataJpaTest` để test repository với transaction rollback tự động.
* Về DataSource:
  * Nếu classpath có embedded DB (ví dụ H2), thêm `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` để dùng Postgres từ Testcontainers.
  * Nếu không có embedded DB trong classpath, chỉ cần kế thừa base Testcontainers là đủ (giống các test hiện tại).
* Dùng `saveAndFlush` khi cần sinh ID ngay để assert.
* Kiểm tra ràng buộc unique hoặc ngoại lệ bằng `assertThatThrownBy(...)`.
* Luôn xác nhận entity đã có ID trước khi sử dụng trong test khác.
* **Ví dụ**:

  ```java
  @DataJpaTest
  class PropertyTypeRepositoryTest extends PostgresTestContainer {
    @Autowired private PropertyTypeRepository repository;

    @Test
    @DisplayName("unique constraint on type_name")
    void unique_type_name() {
      var t1 = new PropertyType(); t1.setTypeName("DupType");
      repository.saveAndFlush(t1);
      var t2 = new PropertyType(); t2.setTypeName("DupType");
      assertThatThrownBy(() -> repository.saveAndFlush(t2))
        .isInstanceOf(DataIntegrityViolationException.class);
    }
  }
  ```

---

## Service Test

* Chọn loại test phù hợp:

  * **Unit test (Mockito)** khi service chỉ có logic thuần, không cần Spring context.
  * **Integration test (SpringBootTest + Testcontainers)** khi service có tương tác DB, transaction, hoặc dependency thật.
* Có thể dùng `@Transactional` để rollback sau mỗi test.
* **Ví dụ**:

  ```java
  @SpringBootTest
  @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
  class PropertyTypeServiceTest extends PostgresTestContainer {
    @Autowired private PropertyTypeService service;
    @Test @Transactional
    @DisplayName("create duplicate name -> DataIntegrityViolationException")
    void duplicates_throw() {
      var r1 = new PropertyTypeCreateReq(); r1.setTypeName("DupType");
      service.create(r1);
      var r2 = new PropertyTypeCreateReq(); r2.setTypeName("DupType");
      assertThatThrownBy(() -> service.create(r2))
        .isInstanceOf(DataIntegrityViolationException.class);
    }
  }
  ```

---

## Quy tắc Testcontainers và DataSource (áp dụng cho repo này)

* Mọi test kế thừa `PostgresTestContainer` (abstract class).
* Chiến lược DataSource:
  * Nếu có biến `DB_URL` (hoặc system property `DB_URL`) → dùng DB cục bộ với `DB_USERNAME`, `DB_PASSWORD`.
  * Ngược lại → khởi tạo PostgreSQL bằng Testcontainers.
* Lifecycle container được quản lý thủ công trong factory method (khởi động một lần, shutdown hook trên JVM):

  ```java
  // trích yếu
  private static final boolean USE_LOCAL_DB =
      System.getenv("DB_URL") != null || System.getProperty("DB_URL") != null;
  private static final PostgreSQLContainer<?> CONTAINER = initContainer();
  @SuppressWarnings("resource")
  private static PostgreSQLContainer<?> initContainer() {
    if (USE_LOCAL_DB) return null;
    var c = new PostgreSQLContainer<>("postgres:17-alpine")
      .withDatabaseName("testdb").withUsername("test").withPassword("test");
    c.start(); Runtime.getRuntime().addShutdownHook(new Thread(c::stop)); return c;
  }
  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    if (USE_LOCAL_DB) {
      r.add("spring.datasource.url", () -> System.getenv().getOrDefault("DB_URL",
        System.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/apartment")));
      r.add("spring.datasource.username", () -> System.getenv().getOrDefault("DB_USERNAME",
        System.getProperty("DB_USERNAME", "postgres")));
      r.add("spring.datasource.password", () -> System.getenv().getOrDefault("DB_PASSWORD",
        System.getProperty("DB_PASSWORD", "postgres")));
    } else {
      r.add("spring.datasource.url", CONTAINER::getJdbcUrl);
      r.add("spring.datasource.username", CONTAINER::getUsername);
      r.add("spring.datasource.password", CONTAINER::getPassword);
    }
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.locations", () -> "classpath:db/migration");
  }
  ```

* Lý do `@SuppressWarnings("resource")`: IDE cảnh báo leak giả (container được JVM shutdown hook đóng). Không dùng try-with-resources ở đây.
* Không tạo container trong constructor hoặc mỗi test case.

---

## Lỗi thường gặp và cách xử lý

* **Null type safety**: luôn kiểm tra đầu vào và dữ liệu trả về; không dùng ép kiểu.
* **Generic variance (Hamcrest)**: tránh dùng `is(...)` hoặc `notNullValue()` với `jsonPath`.
* **Resource leak (Testcontainers)**: khởi tạo container qua static factory, đóng bằng JVM shutdown hook; chấp nhận `@SuppressWarnings("resource")` ở factory method.
* **Import thừa hoặc wildcard**: tránh `import *` để không gây nhập nhằng overload.
* **Transaction trong @DataJpaTest**: mặc định rollback; dùng `saveAndFlush` nếu cần persist thật.
* **Flaky test theo múi giờ**: inject `Clock` vào service, mock hoặc fix thời gian trong test.
* **Kết nối DB bị từ chối**: kiểm tra Docker đang chạy; hoặc dùng DB cục bộ qua `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

---

## Checklist nhanh

* **Controller**:

  * Dùng `APPLICATION_JSON_VALUE` khi gửi request.
  * Serialize body bằng `ObjectMapper`.
  * Assert qua `jsonPath(...).value()` hoặc `.exists()`.
  * Luôn assert `$.message` đúng thông điệp nghiệp vụ.

* **Integration**:

  * Kế thừa base `PostgresTestContainer`.
  * `@DynamicPropertySource` map datasource vào Spring context (container hoặc DB cục bộ).
  * Flyway bật để đảm bảo schema nhất quán cho mọi test run.

* **Repository**:

  * Dùng `saveAndFlush` để lấy ID.
  * Xác nhận ID tồn tại trước khi assert hoặc truyền tiếp.

* **Service**:

  * Dùng Mockito cho unit test.
  * Dùng Testcontainers + SpringBootTest cho integration test.
  * Dùng `@Transactional` để rollback và đơn giản hóa thiết lập dữ liệu.