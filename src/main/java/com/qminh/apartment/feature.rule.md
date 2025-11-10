## Quy trình chuẩn để viết API mới (Đầu vào: Yêu cầu + Entity)

> Ghi chú: Khi cần viết API mới, chỉ cần đọc file này và áp dụng đúng quy tắc bên dưới. Không cần ghi lại hay mô tả thêm các bước ở nơi khác.

Mục tiêu: thêm 1 hoặc nhiều API nhất quán, dễ bảo trì, có test đầy đủ.

### 1) Repository
- Tạo `Repository` extends `JpaRepository<Entity, IdType>`.
- Đặt tên method theo Spring Data (findBy..., existsBy..., countBy...).
- Với truy vấn đặc thù, cân nhắc `@Query` JPQL/SQL.
- Ví dụ:
```java
public interface PropertyAreaRepository extends JpaRepository<PropertyArea, Integer> {
  Optional<PropertyArea> findByAreaLink(String areaLink);
  boolean existsByAreaName(String areaName);
}
```

### 2) DTO
- Chia rõ: Request DTO (create/update/filter) và Response DTO.
- Dùng validation `javax.validation` trên Request DTO: `@NotBlank`, `@Size`, ...
- Không lộ toàn bộ entity ra ngoài; chỉ map trường cần.
- Ví dụ:
```java
public class PropertyAreaCreateReq {
  @NotBlank private String areaName;
  @NotBlank private String areaLink;
  // getters/setters
}

public class PropertyAreaRes {
  private Integer areaId;
  private String areaName;
  private String areaLink;
  // getters/setters
}
```

### 3) Service
- Đặt business logic tại đây; controller chỉ điều phối.
- Sử dụng transaction: `@Transactional` (readOnly cho query).
- Validate nghiệp vụ (duy nhất, trạng thái, …) và ném exception chuẩn.
- Map entity ↔ DTO trong service hoặc mapper riêng.
- Chuẩn hóa interface/implementation:
  - Tạo interface `I{Domain}Service` khai báo contract public.
  - Implementation đặt tên `{Domain}Service implements I{Domain}Service` và gắn `@Service`.
  - Controller tiêm qua interface `I{Domain}Service` để dễ test/mock và mở rộng.
- Ví dụ:
```java
public interface IPropertyAreaService {
  PropertyAreaRes create(PropertyAreaCreateReq req);
  Optional<PropertyAreaRes> getById(Integer id);
}

@Service
public class PropertyAreaService implements IPropertyAreaService {
  private final PropertyAreaRepository repo;
  public PropertyAreaService(PropertyAreaRepository repo) { this.repo = repo; }

  @Transactional
  public PropertyAreaRes create(PropertyAreaCreateReq req) {
    if (repo.existsByAreaName(req.getAreaName())) {
      throw new IllegalArgumentException("Tên khu vực đã tồn tại");
    }
    PropertyArea e = new PropertyArea();
    e.setAreaName(req.getAreaName());
    e.setAreaLink(req.getAreaLink());
    PropertyArea saved = repo.save(e);
    return toRes(saved);
  }

  @Transactional(readOnly = true)
  public Optional<PropertyAreaRes> getById(Integer id) {
    return repo.findById(id).map(this::toRes);
  }

  private PropertyAreaRes toRes(PropertyArea e) {
    PropertyAreaRes r = new PropertyAreaRes();
    r.setAreaId(e.getAreaId());
    r.setAreaName(e.getAreaName());
    r.setAreaLink(e.getAreaLink());
    return r;
  }
}
```

### 4) Controller
- Tiếp nhận/validate request, gọi service, trả `ApiResponse` thống nhất.
- Đặt route theo resource, dùng chuẩn REST: GET/POST/PUT/PATCH/DELETE.
- Dùng `@Valid` cho DTO, `@PathVariable`/`@RequestParam` cho id/filter.
- Ví dụ:
```java
@RestController
@RequestMapping("/api/areas")
public class PropertyAreaController {
  private final IPropertyAreaService service;
  public PropertyAreaController(IPropertyAreaService service) { this.service = service; }

  @PostMapping
  public ApiResponse<PropertyAreaRes> create(@Valid @RequestBody PropertyAreaCreateReq req) {
    PropertyAreaRes res = service.create(req);
    return ApiResponse.ok("Tạo khu vực thành công", res);
  }

  @GetMapping("/{id}")
  public ApiResponse<PropertyAreaRes> detail(@PathVariable Integer id) {
    return service.getById(id)
      .map(r -> ApiResponse.ok("Chi tiết khu vực", r))
      .orElseGet(() -> ApiResponse.notFound("Không tìm thấy khu vực"));
  }
}
```

### 5) Test
- Controller/IT:
  - `@SpringBootTest` + `@AutoConfigureMockMvc`
  - Kế thừa base Testcontainers (Postgres) khi cần DB thật.
  - JSON: `contentType(MediaType.APPLICATION_JSON_VALUE)` và body qua `ObjectMapper`.
  - Assert: `jsonPath(...).value(...)`/`.exists()`.
- Service (theo cấu trúc hiện tại của dự án):
  - `@SpringBootTest` + kế thừa base Testcontainers.
  - `@AutoConfigureTestDatabase(replace = NONE)` để dùng Postgres từ container.
  - Có thể dùng `@Transactional` trong test method để rollback.
- Repository:
  - `@DataJpaTest` + kế thừa base Testcontainers.
  - `@AutoConfigureTestDatabase(replace = NONE)` để không thay DataSource bằng embedded DB.
- Lỗi thường gặp & fix nhanh:
  - Null-safety: dùng `Objects.requireNonNull(...)` cho tham số hợp đồng non-null.
  - Hamcrest generic: dùng `jsonPath("$.x").value("...")` thay vì `is(...)`.
  - Testcontainers lifecycle: dùng `@Testcontainers` + `@Container` (không tự start/stop thủ công).

Ví dụ Controller IT snippet:
```java
mockMvc.perform(post("/api/areas")
    .contentType(MediaType.APPLICATION_JSON_VALUE)
    .content(Objects.requireNonNull(mapper.writeValueAsString(req))))
  .andExpect(status().isOk())
  .andExpect(jsonPath("$.message").value("Tạo khu vực thành công"))
  .andExpect(jsonPath("$.data.areaId").exists());
```

### 6) HTTP (Tài liệu/Thử nghiệm)
- Tạo file `.http` hoặc Postman/Insomnia collection.
- Ghi rõ method, URL, header, body, ví dụ:
```http
### Tạo khu vực
POST http://localhost:8080/api/areas
Content-Type: application/json

{
  "areaName": "District 1",
  "areaLink": "district-1"
}

### Chi tiết khu vực
GET http://localhost:8080/api/areas/1
```

### Checklist khi thêm API
- Repository: method cần thiết, tên rõ ràng.
- DTO: tách request/response, có validation.
- Service: logic + transaction, map entity–DTO.
- Controller: route chuẩn REST, trả `ApiResponse` nhất quán.
- Test: Controller/IT + Unit + Repository, cover path chính/lỗi.
- HTTP: ví dụ request đầy đủ để QA/dev verify nhanh.

---

## Bổ sung quy ước “zero-ambiguity” (để code không cần suy nghĩ thêm)

### A) Chuẩn ApiResponse (theo code hiện tại)
- Cấu trúc:
```json
{
  "message": "Create property area successfully",
  "data": { },
  "meta": { "page": 0, "size": 10, "total": 100 },
  "error": null
}
```
- Khi lỗi:
```json
{
  "message": "error",
  "error": { "code": "NOT_FOUND", "message": "Area not found: 1", "details": null }
}
```
- Mapping mặc định:
  - Thành công: 200 OK (hoặc 201 Created khi tạo mới nếu muốn).
  - Lỗi client: 400 VALIDATION_ERROR, 404 NOT_FOUND, 409 SQL_CONSTRAINT/CONFLICT, 422 BUSINESS_ERROR.
  - Lỗi server: 500 INTERNAL_ERROR.

### B) Chuẩn hóa lỗi/exception
- Dùng `@ControllerAdvice` + `@ExceptionHandler` để map exception → ApiResponse.error(code, message, details).
- Validation error: trả `VALIDATION_ERROR` (400) với map field → message.
- Quy ước code: `VALIDATION_ERROR` (400), `NOT_FOUND` (404), `SQL_CONSTRAINT`/`CONFLICT` (409), `BUSINESS_ERROR` (422), `INTERNAL_ERROR` (500).

### C) REST + Versioning
- Base path: `/api/v1/...`
- Quy tắc:
  - POST `/resources` → tạo
  - GET `/resources/{id}` → chi tiết
  - GET `/resources` → danh sách (có paging/sort/filter)
  - PUT `/resources/{id}` → cập nhật toàn bộ
  - PATCH `/resources/{id}` → cập nhật một phần
  - DELETE `/resources/{id}` → xóa
- Idempotency: PUT/PATCH/DELETE là idempotent, POST không.

### D) Paging/Sorting/Filtering
- Tham số:
  - `page` (0-based), `size` (mặc định 20, tối đa 200), `sort=field,asc|desc`
  - Filter tự do: `q` hoặc theo trường rõ ràng (ví dụ `name`, `status`, …)
- Response danh sách:
```json
{
  "success": true,
  "code": "OK",
  "message": "Danh sách",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 123,
    "totalPages": 7,
    "sort": "name,asc"
  },
  "timestamp": "...",
  "traceId": "..."
}
```

### E) Validation chi tiết
- Dùng `@NotBlank`, `@Size`, `@Pattern`, `@Email`, ...
- Bản địa hóa message (i18n key) nếu cần; quy ước message ngắn gọn, không nhúng field name (đã có trong cấu trúc `errors[]`).
- Khác biệt create/update: dùng validation groups hoặc tách DTO.

### F) Mapping DTO
- Ưu tiên `MapStruct` cho dự án lớn; nếu nhỏ, mapping thủ công trong Service.
- Quy ước đặt tên DTO: `XxxCreateReq`, `XxxUpdateReq`, `XxxFilterReq`, `XxxRes`.
- Mapper (nếu dùng MapStruct):
```java
@Mapper(componentModel = "spring")
public interface PropertyAreaMapper {
  PropertyArea toEntity(PropertyAreaCreateReq req);
  PropertyAreaRes toRes(PropertyArea e);
}
```

### G) Security
- Chuẩn header: `Authorization: Bearer <token>`
- Quy tắc quyền:
  - Ví dụ: `@PreAuthorize("hasRole('ADMIN')")` cho tạo/cập nhật/xóa
  - `@PreAuthorize("permitAll()")` cho GET danh sách/chi tiết (tùy yêu cầu)
- Khi yêu cầu auth bắt buộc: trả 401 nếu thiếu/invalid, 403 nếu thiếu quyền.

### H) Transaction & đồng bộ dữ liệu
- Mặc định:
  - Query: `@Transactional(readOnly = true)`
  - Ghi: `@Transactional`
- Lỗi nghiệp vụ → ném `IllegalArgumentException`/`BusinessException` (rollback).
- Khuyến nghị optimistic locking (`@Version`) cho entity quan trọng.

### I) DateTime & Enum
- Timezone: UTC, định dạng ISO-8601: `yyyy-MM-dd'T'HH:mm:ss'Z'`.
- Enum serialize dạng string (không dùng ordinal).

### J) Ràng buộc DB & tên cột
- Quy ước migration (Flyway): snake_case, index/unique rõ ràng.
- Entity mapping: `@Table(name="...")`, `@Column(name="...")` để nhất quán.
- Unique constraint nên có index tương ứng.

### K) Observability
- Log theo mức: INFO cho flow chính, WARN cho tình huống bất thường, ERROR cho lỗi nghiêm trọng.
- Nhúng `traceId` (MDC) trong log/response nếu có.

### L) Tài liệu API
- `springdoc-openapi` + `@Operation`, `@Parameter`, `@ApiResponses`.
```java
@Operation(summary = "Tạo khu vực", description = "Tạo mới khu vực với tên và link duy nhất")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Thành công"),
  @ApiResponse(responseCode = "409", description = "Trùng tên/link")
})
```

### M) HTTP examples phong phú
```http
### Tạo mới
POST http://localhost:8080/api/v1/areas
Content-Type: application/json
Authorization: Bearer <token>

{
  "areaName": "District 1",
  "areaLink": "district-1"
}

### Cập nhật (PUT)
PUT http://localhost:8080/api/v1/areas/1
Content-Type: application/json
Authorization: Bearer <token>

{
  "areaName": "District 1 Updated",
  "areaLink": "district-1"
}

### Cập nhật một phần (PATCH)
PATCH http://localhost:8080/api/v1/areas/1
Content-Type: application/json
Authorization: Bearer <token>

{
  "areaName": "District 1 Partial"
}

### Danh sách có paging/sort/filter
GET http://localhost:8080/api/v1/areas?page=0&size=20&sort=areaName,asc&q=district
Authorization: Bearer <token>

### Xóa
DELETE http://localhost:8080/api/v1/areas/1
Authorization: Bearer <token>
```

### N) Checklist mở rộng khi thêm API
- ApiResponse: đúng schema, có `timestamp`, `traceId`.
- Exception: map đầy đủ qua `@ControllerAdvice`.
- REST: đường dẫn `/api/v1/...`, đúng method semantics.
- Paging/sort/filter: tham số chuẩn, response có metadata.
- Validation: đủ ràng buộc, message rõ ràng.
- DTO/Mapper: không lộ entity, mapping nhất quán.
- Security: xác định rõ ai được làm gì; test quyền.
- Transaction/locking: đúng annotation; xem xét @Version với entity quan trọng.
- Date/time/enum: UTC + ISO-8601, enum as string.
- DB: index/unique/constraint rõ ràng, migrate với Flyway.
- Observability: log phù hợp, traceId nhất quán.
- Tài liệu: có annotation OpenAPI, ví dụ HTTP cập nhật kèm.

