# API Property Search

## Tổng quan

- **Endpoint**: `POST /api/properties/search`
- **Content-Type**: `application/json`
- **Mục đích**: API thống nhất để tìm kiếm và lấy danh sách bất động sản. Hỗ trợ 2 chế độ response:
  - **`mode=list`** (mặc định): Response nhẹ, phù hợp cho listing/search UI.
  - **`mode=select`**: Response đầy đủ thông tin liên quan (type, area, saleInfo, galleries, details), phù hợp khi cần hiển thị chi tiết ngay mà không cần gọi thêm API detail.

---

## Request

### Query Parameters

| Parameter | Type   | Required | Default      | Mô tả                                                                                                                                                              |
|-----------|--------|----------|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `page`    | int    | No       | `0`          | Trang hiện tại, bắt đầu từ `0`.                                                                                                                                    |
| `size`    | int    | No       | `10`         | Số phần tử trên một trang.                                                                                                                                         |
| `sort`    | string | No       | `createdAt,desc` | Thông tin sắp xếp, format: `field,direction`. Ví dụ: `price,asc`, `createdAt,desc`. Nếu không truyền hoặc sai format thì mặc định `createdAt DESC`. |
| `mode`    | string | No       | `list`       | Chế độ response: `list` (response nhẹ) hoặc `select` (response đầy đủ thông tin liên quan).                                                                        |

**Ví dụ URL:**
```
POST /api/properties/search?page=0&size=10&sort=price,asc&mode=list
POST /api/properties/search?page=0&size=10&mode=select
```

### Request Body

Body là object `PropertySearchReq` với **tất cả các field đều optional**. Nếu không truyền field nào thì không filter theo điều kiện đó. Body rỗng `{}` sẽ trả về tất cả property (có phân trang).

```json
{
  "q": "vinhome",
  "title": "căn hộ",
  "description": "luxury",
  "typeIds": [1, 2],
  "areaIds": [1, 2, 3],
  "saleUserIds": [10, 20],
  "isPublic": true,
  "isForRent": true,
  "minPrice": 1000000000,
  "maxPrice": 5000000000,
  "createdFrom": "2025-01-01T00:00:00",
  "createdTo": "2025-12-31T23:59:59",
  "updatedFrom": "2025-01-01T00:00:00",
  "updatedTo": "2025-12-31T23:59:59",
  "details": [
    {
      "detailId": 1,
      "minNumber": 50,
      "maxNumber": 100
    },
    {
      "detailId": 2,
      "number": 2
    },
    {
      "detailId": 3,
      "text": "Đông Nam"
    }
  ]
}
```

### Mô tả chi tiết các field trong Request Body

| Field         | Type                           | Mô tả                                                                                                                                                                                                                   |
|---------------|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `q`           | string                         | Từ khóa tìm kiếm chung. Tìm trong `title` và `description` (case-insensitive, partial match).                                                                                                                           |
| `title`       | string                         | Lọc theo tiêu đề bất động sản (case-insensitive, partial match).                                                                                                                                                        |
| `description` | string                         | Lọc theo mô tả (case-insensitive, partial match).                                                                                                                                                                       |
| `typeIds`     | List&lt;Integer&gt;            | Danh sách ID loại bất động sản (`property_type.type_id`). Lọc property có `type_id` nằm trong danh sách này.                                                                                                            |
| `areaIds`     | List&lt;Integer&gt;            | Danh sách ID khu vực (`property_area.area_id`). Lọc property có `area_id` nằm trong danh sách này.                                                                                                                      |
| `saleUserIds` | List&lt;Long&gt;               | Danh sách ID nhân viên sale (`property_sale_info.user_id`). Lọc property có `sale_id` nằm trong danh sách này.                                                                                                          |
| `isPublic`    | Boolean                        | `true`: chỉ lấy property public. `false`: chỉ lấy property không public. `null`/không truyền: không lọc.                                                                                                                |
| `isForRent`   | Boolean                        | `true`: chỉ lấy property cho thuê. `false`: chỉ lấy property bán. `null`/không truyền: không lọc.                                                                                                                       |
| `minPrice`    | BigDecimal                     | Giá tối thiểu (>=). Đơn vị: VND.                                                                                                                                                                                        |
| `maxPrice`    | BigDecimal                     | Giá tối đa (<=). Đơn vị: VND.                                                                                                                                                                                           |
| `createdFrom` | LocalDateTime (ISO-8601)       | Ngày tạo từ (>=). Format: `yyyy-MM-ddTHH:mm:ss`.                                                                                                                                                                        |
| `createdTo`   | LocalDateTime (ISO-8601)       | Ngày tạo đến (<=). Format: `yyyy-MM-ddTHH:mm:ss`.                                                                                                                                                                       |
| `updatedFrom` | LocalDateTime (ISO-8601)       | Ngày cập nhật từ (>=). Format: `yyyy-MM-ddTHH:mm:ss`.                                                                                                                                                                   |
| `updatedTo`   | LocalDateTime (ISO-8601)       | Ngày cập nhật đến (<=). Format: `yyyy-MM-ddTHH:mm:ss`.                                                                                                                                                                  |
| `details`     | List&lt;PropertyDetailFilterReq&gt; | Danh sách filter theo thuộc tính chi tiết của property (diện tích, số phòng, hướng nhà...). Xem chi tiết bên dưới.                                                                                                      |

### Cấu trúc `PropertyDetailFilterReq`

Dùng để filter theo các thuộc tính chi tiết của property (bảng `property_details` + `property_define_details`).

```json
{
  "detailId": 1,
  "number": 2,
  "minNumber": 50,
  "maxNumber": 100,
  "text": "Đông Nam"
}
```

| Field       | Type       | Mô tả                                                                                                                                                                                                                                     |
|-------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `detailId`  | Integer    | **Bắt buộc**. ID của thuộc tính chi tiết (`property_define_details.detail_id`).                                                                                                                                                           |
| `number`    | BigDecimal | Giá trị số chính xác. Dùng khi muốn filter thuộc tính số với giá trị cụ thể (ví dụ: số phòng ngủ = 2).                                                                                                                                    |
| `minNumber` | BigDecimal | Giá trị số tối thiểu (>=). Dùng khi muốn filter thuộc tính số theo khoảng (ví dụ: diện tích >= 50m²).                                                                                                                                     |
| `maxNumber` | BigDecimal | Giá trị số tối đa (<=). Dùng khi muốn filter thuộc tính số theo khoảng (ví dụ: diện tích <= 100m²).                                                                                                                                       |
| `text`      | string     | Giá trị text (partial match, case-insensitive). Dùng khi muốn filter thuộc tính text (ví dụ: hướng nhà chứa "Đông").                                                                                                                      |

**Lưu ý logic filter details:**
- Nếu truyền `number`: filter chính xác `value = number`.
- Nếu truyền `minNumber` và/hoặc `maxNumber`: filter theo khoảng `minNumber <= value <= maxNumber`.
- Nếu truyền `text`: filter `value LIKE '%text%'` (case-insensitive).
- Các điều kiện trong cùng một `PropertyDetailFilterReq` được kết hợp bằng AND.
- Các `PropertyDetailFilterReq` khác nhau trong `details` được kết hợp bằng AND.

---

## Response

### Cấu trúc chung `ApiResponse`

```json
{
  "message": "string",
  "error": null,
  "data": {
    "content": [ /* danh sách property */ ],
    "pageable": { /* thông tin pageable */ },
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false,
    "empty": false
  },
  "meta": {
    "page": 0,
    "size": 10,
    "total": 100
  }
}
```

| Field     | Type          | Mô tả                                                                                                                    |
|-----------|---------------|--------------------------------------------------------------------------------------------------------------------------|
| `message` | string        | Thông điệp kết quả. `"Property search result"` cho mode=list, `"Property search result (select)"` cho mode=select.       |
| `error`   | object / null | Thông tin lỗi nếu có. Request thành công sẽ là `null`.                                                                   |
| `data`    | Page object   | Kết quả phân trang. `content` chứa danh sách property theo mode đã chọn.                                                 |
| `meta`    | object        | Thông tin phân trang tóm tắt: `page` (trang hiện tại), `size` (số phần tử/trang), `total` (tổng số phần tử match filter). |

---

## Response theo Mode

### Mode `list` (mặc định)

Response nhẹ, phù hợp cho listing/search UI. Mỗi item trong `data.content` có cấu trúc `PropertyRes`:

```json
{
  "propertyId": 1,
  "title": "Căn hộ Vinhome Central Park",
  "price": 2500000000,
  "description": "Căn hộ cao cấp 2 phòng ngủ...",
  "isPublic": true,
  "isForRent": false,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-02-01T14:20:00",

  "typeId": 1,
  "typeName": "Căn hộ",

  "areaId": 5,
  "areaName": "Quận Bình Thạnh",

  "saleUserId": 10,
  "saleDisplayName": "Nguyễn Văn A",
  "salePhone": "0903123456",

  "mainImageRelativePath": "public/properties/abc123.jpg"
}
```

| Field                   | Type          | Mô tả                                                                                      |
|-------------------------|---------------|--------------------------------------------------------------------------------------------|
| `propertyId`            | Long          | ID của property.                                                                           |
| `title`                 | string        | Tiêu đề property.                                                                          |
| `price`                 | BigDecimal    | Giá (VND).                                                                                 |
| `description`           | string        | Mô tả chi tiết.                                                                            |
| `isPublic`              | Boolean       | Property có public không.                                                                  |
| `isForRent`             | Boolean       | `true`: cho thuê, `false`: bán.                                                            |
| `createdAt`             | LocalDateTime | Ngày tạo.                                                                                  |
| `updatedAt`             | LocalDateTime | Ngày cập nhật gần nhất.                                                                    |
| `typeId`                | Integer       | ID loại property.                                                                          |
| `typeName`              | string        | Tên loại property (ví dụ: "Căn hộ", "Nhà phố", "Biệt thự").                                |
| `areaId`                | Integer       | ID khu vực.                                                                                |
| `areaName`              | string        | Tên khu vực (ví dụ: "Quận 1", "Quận Bình Thạnh").                                          |
| `saleUserId`            | Long          | ID nhân viên sale phụ trách.                                                               |
| `saleDisplayName`       | string        | Tên hiển thị của nhân viên sale (từ `users.display_name`).                                 |
| `salePhone`             | string        | Số điện thoại nhân viên sale (từ `property_sale_info.phone`).                              |
| `mainImageRelativePath` | string / null | Đường dẫn tương đối của ảnh đại diện (ảnh đầu tiên trong gallery). `null` nếu chưa có ảnh. |

---

### Mode `select` (đầy đủ thông tin)

Response đầy đủ tất cả thông tin liên quan, phù hợp khi cần hiển thị chi tiết ngay mà không cần gọi thêm API. Mỗi item trong `data.content` có cấu trúc `PropertySelectRes`:

```json
{
  "propertyId": 1,
  "title": "Căn hộ Vinhome Central Park",
  "price": 2500000000,
  "description": "Căn hộ cao cấp 2 phòng ngủ, view sông Sài Gòn...",
  "isPublic": true,
  "isForRent": false,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-02-01T14:20:00",

  "type": {
    "typeId": 1,
    "typeName": "Căn hộ"
  },

  "area": {
    "areaId": 5,
    "areaName": "Quận Bình Thạnh",
    "areaLink": "quan-binh-thanh"
  },

  "saleInfo": {
    "userId": 10,
    "displayName": "Nguyễn Văn A",
    "phone": "0903123456"
  },

  "galleries": [
    {
      "fileId": 100,
      "originalName": "phong-khach.jpg",
      "relativePath": "public/properties/abc123.jpg",
      "mimeType": "image/jpeg",
      "altText": "Phòng khách",
      "title": "Phòng khách căn hộ"
    },
    {
      "fileId": 101,
      "originalName": "phong-ngu.jpg",
      "relativePath": "public/properties/def456.jpg",
      "mimeType": "image/jpeg",
      "altText": "Phòng ngủ",
      "title": "Phòng ngủ master"
    }
  ],

  "details": [
    {
      "detailId": 1,
      "detailName": "Diện tích",
      "isNumber": true,
      "unit": "m²",
      "showInHomePage": true,
      "value": "75"
    },
    {
      "detailId": 2,
      "detailName": "Số phòng ngủ",
      "isNumber": true,
      "unit": "phòng",
      "showInHomePage": true,
      "value": "2"
    },
    {
      "detailId": 3,
      "detailName": "Hướng nhà",
      "isNumber": false,
      "unit": null,
      "showInHomePage": false,
      "value": "Đông Nam"
    }
  ]
}
```

#### Cấu trúc các object con

**`type` (PropertyTypeInfo)**

| Field      | Type    | Mô tả                                           |
|------------|---------|-------------------------------------------------|
| `typeId`   | Integer | ID loại property (`property_type.type_id`).     |
| `typeName` | string  | Tên loại property (`property_type.type_name`).  |

**`area` (PropertyAreaInfo)**

| Field      | Type    | Mô tả                                                                |
|------------|---------|----------------------------------------------------------------------|
| `areaId`   | Integer | ID khu vực (`property_area.area_id`).                                |
| `areaName` | string  | Tên khu vực (`property_area.area_name`).                             |
| `areaLink` | string  | Slug/link khu vực (`property_area.area_link`), dùng cho URL thân thiện. |

**`saleInfo` (PropertySaleInfoRes)**

| Field         | Type   | Mô tả                                                                            |
|---------------|--------|----------------------------------------------------------------------------------|
| `userId`      | Long   | ID user của nhân viên sale (`property_sale_info.user_id`).                       |
| `displayName` | string | Tên hiển thị của nhân viên sale (từ `users.display_name`).                       |
| `phone`       | string | Số điện thoại liên hệ của nhân viên sale (từ `property_sale_info.phone`).        |

**`galleries` (List&lt;PropertyGalleryRes&gt;)**

Danh sách ảnh của property (từ bảng `property_gallery` + `stored_file`).

| Field          | Type   | Mô tả                                                           |
|----------------|--------|-----------------------------------------------------------------|
| `fileId`       | Long   | ID file (`stored_file.file_id`).                                |
| `originalName` | string | Tên file gốc khi upload (`stored_file.original_name`).          |
| `relativePath` | string | Đường dẫn tương đối để truy cập file (`stored_file.relative_path`). |
| `mimeType`     | string | MIME type của file (ví dụ: `image/jpeg`, `image/png`).          |
| `altText`      | string | Alt text cho ảnh (SEO, accessibility).                          |
| `title`        | string | Tiêu đề/caption của ảnh.                                        |

**`details` (List&lt;PropertyDetailRes&gt;)**

Danh sách thuộc tính chi tiết của property (từ bảng `property_details` + `property_define_details`).

| Field            | Type    | Mô tả                                                                                              |
|------------------|---------|----------------------------------------------------------------------------------------------------|
| `detailId`       | Integer | ID định nghĩa thuộc tính (`property_define_details.detail_id`).                                    |
| `detailName`     | string  | Tên thuộc tính (ví dụ: "Diện tích", "Số phòng ngủ", "Hướng nhà").                                  |
| `isNumber`       | Boolean | `true` nếu thuộc tính là số, `false` nếu là text.                                                  |
| `unit`           | string  | Đơn vị (ví dụ: "m²", "phòng"). `null` nếu không có đơn vị.                                         |
| `showInHomePage` | Boolean | `true` nếu thuộc tính này nên hiển thị ở trang chủ/listing.                                        |
| `value`          | string  | Giá trị của thuộc tính cho property này (từ `property_details.value`). Luôn là string, FE tự parse nếu cần. |

---

## Ví dụ Request/Response

### Ví dụ 1: Tìm kiếm cơ bản với mode=list

**Request**

```http
POST /api/properties/search?page=0&size=10&sort=createdAt,desc&mode=list
Content-Type: application/json

{
  "q": "vinhome",
  "isForRent": false,
  "minPrice": 2000000000,
  "maxPrice": 5000000000
}
```

**Response 200 OK**

```json
{
  "message": "Property search result",
  "error": null,
  "data": {
    "content": [
      {
        "propertyId": 1,
        "title": "Căn hộ Vinhome Central Park 2PN",
        "price": 2500000000,
        "description": "Căn hộ cao cấp view sông...",
        "isPublic": true,
        "isForRent": false,
        "createdAt": "2025-01-15T10:30:00",
        "updatedAt": "2025-02-01T14:20:00",
        "typeId": 1,
        "typeName": "Căn hộ",
        "areaId": 5,
        "areaName": "Quận Bình Thạnh",
        "saleUserId": 10,
        "saleDisplayName": "Nguyễn Văn A",
        "salePhone": "0903123456",
        "mainImageRelativePath": "public/properties/abc123.jpg"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "first": true,
    "last": true,
    "empty": false
  },
  "meta": {
    "page": 0,
    "size": 10,
    "total": 1
  }
}
```

### Ví dụ 2: Tìm kiếm với mode=select (đầy đủ thông tin)

**Request**

```http
POST /api/properties/search?page=0&size=10&mode=select
Content-Type: application/json

{
  "typeIds": [1],
  "areaIds": [5],
  "isPublic": true
}
```

**Response 200 OK**

```json
{
  "message": "Property search result (select)",
  "error": null,
  "data": {
    "content": [
      {
        "propertyId": 1,
        "title": "Căn hộ Vinhome Central Park 2PN",
        "price": 2500000000,
        "description": "Căn hộ cao cấp view sông...",
        "isPublic": true,
        "isForRent": false,
        "createdAt": "2025-01-15T10:30:00",
        "updatedAt": "2025-02-01T14:20:00",
        "type": {
          "typeId": 1,
          "typeName": "Căn hộ"
        },
        "area": {
          "areaId": 5,
          "areaName": "Quận Bình Thạnh",
          "areaLink": "quan-binh-thanh"
        },
        "saleInfo": {
          "userId": 10,
          "displayName": "Nguyễn Văn A",
          "phone": "0903123456"
        },
        "galleries": [
          {
            "fileId": 100,
            "originalName": "phong-khach.jpg",
            "relativePath": "public/properties/abc123.jpg",
            "mimeType": "image/jpeg",
            "altText": "Phòng khách",
            "title": "Phòng khách căn hộ"
          }
        ],
        "details": [
          {
            "detailId": 1,
            "detailName": "Diện tích",
            "isNumber": true,
            "unit": "m²",
            "showInHomePage": true,
            "value": "75"
          },
          {
            "detailId": 2,
            "detailName": "Số phòng ngủ",
            "isNumber": true,
            "unit": "phòng",
            "showInHomePage": true,
            "value": "2"
          }
        ]
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0,
    "first": true,
    "last": true,
    "empty": false
  },
  "meta": {
    "page": 0,
    "size": 10,
    "total": 1
  }
}
```

### Ví dụ 3: Tìm kiếm với filter details (diện tích, số phòng)

**Request**

```http
POST /api/properties/search?page=0&size=10&sort=price,asc&mode=list
Content-Type: application/json

{
  "isForRent": true,
  "details": [
    {
      "detailId": 1,
      "minNumber": 50,
      "maxNumber": 100
    },
    {
      "detailId": 2,
      "number": 2
    }
  ]
}
```

**Giải thích**: Tìm property cho thuê, có diện tích từ 50-100m² và có 2 phòng ngủ, sắp xếp theo giá tăng dần.

### Ví dụ 4: Lấy tất cả property (không filter)

**Request**

```http
POST /api/properties/search?page=0&size=20
Content-Type: application/json

{}
```

**Giải thích**: Body rỗng `{}` sẽ trả về tất cả property, phân trang 20 items/page, sắp xếp mặc định theo `createdAt DESC`.

---

## Lưu ý kỹ thuật

1. **Performance**: 
   - `mode=list` sử dụng query nhẹ, chỉ join các bảng cần thiết.
   - `mode=select` sử dụng EntityGraph để fetch tất cả relations trong 1 query, tránh N+1 problem.

2. **Pagination với collections**: Khi dùng `mode=select`, do fetch nhiều collections (galleries, details), Hibernate sẽ apply pagination in-memory. Với dataset lớn, nên cân nhắc giới hạn `size` hợp lý.

3. **Null handling**: Các field optional trong response có thể là `null`. FE cần handle case này.

4. **Date format**: Tất cả datetime sử dụng ISO-8601 format: `yyyy-MM-ddTHH:mm:ss`.

---

## Files liên quan

- **Controller**: `PropertyController.java`
- **Service**: `IPropertyService.java`, `PropertyService.java`
- **Repository**: `PropertyRepository.java`, `PropertyRepositoryCustom.java`, `PropertyRepositoryCustomImpl.java`
- **DTO Request**: `PropertySearchReq.java`, `PropertyDetailFilterReq.java`
- **DTO Response (list)**: `PropertyRes.java`
- **DTO Response (select)**: `PropertySelectRes.java`, `PropertyTypeInfo.java`, `PropertyAreaInfo.java`, `PropertySaleInfoRes.java`, `PropertyGalleryRes.java`, `PropertyDetailRes.java`
- **Mapper**: `PropertyMapper.java`
- **Specifications**: `PropertySpecifications.java`

---

*Cập nhật lần cuối: 2025-12-09*
