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
- Dùng validation `jakarta.validation` trên Request DTO: `@NotBlank`, `@Size`, ...
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
- TÁCH RIÊNG MAPPING (Entity ↔ DTO) — BẮT BUỘC:
  - Không mapping trong Controller hay Repository.
  - BẮT BUỘC dùng mapper chuyên biệt (MapStruct). CẤM mapping thủ công trong Service/Controller.
  - Service chỉ gọi mapper để chuyển đổi dữ liệu vào/ra.
- Chuẩn hóa interface/implementation:
  - Tạo interface `I{Domain}Service` khai báo contract public.
  - Implementation đặt tên `{Domain}Service implements I{Domain}Service` và gắn `@Service`.
  - Controller tiêm qua interface `I{Domain}Service` để dễ test/mock và mở rộng.
- Ví dụ:
```java
public interface IPropertyAreaService {
  PropertyAreaRes create(PropertyAreaCreateReq req);
  PropertyAreaRes get(int id);
  Page<PropertyAreaRes> list(Pageable pageable);
  PropertyAreaRes update(int id, PropertyAreaUpdateReq req);
  void delete(int id);
}

@Service
public class PropertyAreaService implements IPropertyAreaService {
  private final PropertyAreaRepository repo;
  private final PropertyAreaMapper mapper;
  public PropertyAreaService(PropertyAreaRepository repo, PropertyAreaMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Transactional
  public PropertyAreaRes create(PropertyAreaCreateReq req) {
    PropertyArea entity = mapper.toEntity(req);
    PropertyArea saved = repo.save(entity);
    return mapper.toRes(saved);
  }

  @Transactional(readOnly = true)
  public PropertyAreaRes get(int id) {
    PropertyArea area = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Area not found: " + id));
    return mapper.toRes(area);
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
  public ResponseEntity<ApiResponse<PropertyAreaRes>> create(@Valid @RequestBody PropertyAreaCreateReq req) {
    PropertyAreaRes res = service.create(req);
    return ResponseEntity.ok(ApiResponse.ok("Create property area successfully", res));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PropertyAreaRes>> detail(@PathVariable Integer id) {
    PropertyAreaRes res = service.get(id);
    return ResponseEntity.ok(ApiResponse.ok("Property area detail", res));
  }
}
```

### 5) Test
xem tài liệu src/test/java/com/qminh/apartment/test.rule.md để viết nhé

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
- Base path hiện tại: `/api/...` (chưa dùng version prefix).
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
  "message": "Property area list",
  "data": [ ... ],
  "meta": { "page": 0, "size": 20, "total": 123 },
  "error": null
}
```

### E) Validation chi tiết
- Dùng `@NotBlank`, `@Size`, `@Pattern`, `@Email`, ... từ `jakarta.validation`.
- Bản địa hóa message (i18n key) nếu cần; quy ước message ngắn gọn, không nhúng field name (đã có trong cấu trúc `errors[]`).
- Khác biệt create/update: dùng validation groups hoặc tách DTO.

### F) Mapping DTO
- BẮT BUỘC dùng `MapStruct` làm mapper chuyên biệt; CẤM mapping thủ công trong Service/Controller.
- Quy ước đặt tên DTO: `XxxCreateReq`, `XxxUpdateReq`, `XxxFilterReq`, `XxxRes`.
- Mapper (nếu dùng MapStruct):
```java
@Mapper(
  componentModel = "spring",
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyAreaMapper {
  PropertyArea toEntity(PropertyAreaCreateReq req);
  void updateEntityFromReq(PropertyAreaUpdateReq req, @MappingTarget PropertyArea target);
  PropertyAreaRes toRes(PropertyArea e);
}
```

#### Chuẩn MapStruct của repo này (làm theo đúng các file hiện tại)
- `@Mapper` bắt buộc:
  - `componentModel = "spring"`
  - `nullValueCheckStrategy = ALWAYS`
  - `nullValuePropertyMappingStrategy = IGNORE`
- Phương thức cho Create:
  - `toEntity(CreateReq)`: luôn `@Mapping(..., ignore = true)` cho khóa chính và cột audit.
  - Tên trường khóa chính theo chuẩn entity: ví dụ `typeId`, `areaId`.
  - Cột audit: `createdAt`, `updatedAt` luôn bị bỏ qua (DB hoặc audit layer set).
- Phương thức cho Update:
  - `updateEntityFromReq(UpdateReq, @MappingTarget Entity)` với
    `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` để không overwrite bằng null.
  - Tiếp tục `ignore = true` cho khóa chính và cột audit.
- Phương thức phản hồi:
  - `toRes(Entity)` ánh xạ 1–1 các trường cần expose.
- Không viết logic nghiệp vụ trong mapper. Mapper chỉ chuyển đổi dữ liệu.
- Không dùng mapping thủ công trong Service/Controller (chỉ gọi mapper).

Ví dụ bám sát code hiện tại — `PropertyTypeMapper`:
```java
@Mapper(
  componentModel = "spring",
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyTypeMapper {
  @Mapping(target = "typeId",    ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  PropertyType toEntity(PropertyTypeCreateReq req);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "typeId",    ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromReq(PropertyTypeUpdateReq req, @MappingTarget PropertyType target);

  PropertyTypeRes toRes(PropertyType entity);
}
```

Gợi ý bổ sung khi mở rộng:
- Nếu tên trường khác nhau: dùng `@Mapping(source = "reqField", target = "entityField")`.
- Với quan hệ (relation) phức tạp: không map trực tiếp ID → entity trong mapper; hãy load entity ở Service rồi set vào entity đích trước/hoặc sau khi dùng mapper.
- Mapping danh sách/trang:
  - Dùng `page.map(mapper::toRes)` tại Service cho `Page<Entity>`.
  - Có thể thêm `List<Res> toResList(List<Entity> e)` nếu cần, nhưng ưu tiên dùng stream/page map để tránh dư thừa.

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
POST http://localhost:8080/api/areas
Content-Type: application/json

{
  "areaName": "District 1",
  "areaLink": "district-1"
}

### Cập nhật (PUT)
PUT http://localhost:8080/api/areas/1
Content-Type: application/json

{
  "areaName": "District 1 Updated",
  "areaLink": "district-1"
}

### Cập nhật một phần (PATCH)
PATCH http://localhost:8080/api/areas/1
Content-Type: application/json

{
  "areaName": "District 1 Partial"
}

### Danh sách có paging/sort/filter
GET http://localhost:8080/api/areas?page=0&size=20&sort=areaName,asc&q=district

### Xóa
DELETE http://localhost:8080/api/areas/1
```

### N) Checklist mở rộng khi thêm API
- ApiResponse: đúng schema hiện tại (`message`, `data`, `meta`, `error`).
- Exception: map đầy đủ qua `@ControllerAdvice`.
- REST: đường dẫn `/api/...`, đúng method semantics.
- Paging/sort/filter: tham số chuẩn, response có metadata.
- Validation: đủ ràng buộc, message rõ ràng.
- DTO/Mapper: không lộ entity, mapping nhất quán.
- Security: xác định rõ ai được làm gì; test quyền.
- Transaction/locking: đúng annotation; xem xét @Version với entity quan trọng.
- Date/time/enum: UTC + ISO-8601, enum as string.
- DB: index/unique/constraint rõ ràng, migrate với Flyway.
- Observability: log phù hợp, traceId nhất quán.
- Tài liệu: có annotation OpenAPI, ví dụ HTTP cập nhật kèm.

