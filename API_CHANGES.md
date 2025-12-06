# API Changes - Property API

## Tổng quan

API Property đã được cập nhật để trả về thêm thông tin về ngày tạo (`createdAt`) và ngày cập nhật (`updatedAt`) trong response.

---

## Thay đổi chi tiết

### Property Response (`PropertyRes`)

**Endpoint bị ảnh hưởng:**
- `GET /api/properties` - Danh sách properties
- `GET /api/properties/{id}` - Chi tiết property
- `POST /api/properties` - Tạo property mới
- `PUT /api/properties/{id}` - Cập nhật property

**Thay đổi:**
Thêm 2 trường mới vào response:

| Trường | Kiểu dữ liệu | Mô tả | Bắt buộc |
|--------|--------------|-------|----------|
| `createdAt` | `string` (ISO 8601 DateTime) | Ngày giờ tạo property | Có |
| `updatedAt` | `string` (ISO 8601 DateTime) | Ngày giờ cập nhật property lần cuối | Có |

---

## Ví dụ Response

### Trước khi thay đổi:
```json
{
  "message": "Property detail",
  "data": {
    "propertyId": 1,
    "title": "Căn hộ cao cấp",
    "price": 5000000000.00,
    "description": "Mô tả căn hộ",
    "typeId": 1,
    "saleUserId": 1,
    "areaId": 1,
    "isPublic": true,
    "isForRent": false
  }
}
```

### Sau khi thay đổi:
```json
{
  "message": "Property detail",
  "data": {
    "propertyId": 1,
    "title": "Căn hộ cao cấp",
    "price": 5000000000.00,
    "description": "Mô tả căn hộ",
    "typeId": 1,
    "saleUserId": 1,
    "areaId": 1,
    "isPublic": true,
    "isForRent": false,
    "createdAt": "2025-11-28T10:30:00",
    "updatedAt": "2025-11-28T15:45:00"
  }
}
```

---

## Format DateTime

- **Format**: ISO 8601 (`yyyy-MM-ddTHH:mm:ss`)
- **Ví dụ**: `2025-11-28T10:30:00`
- **Timezone**: UTC (hoặc theo server timezone)

---

## Hành vi

1. **Khi tạo mới property** (`POST /api/properties`):
   - `createdAt` và `updatedAt` được set tự động bằng thời điểm hiện tại
   - Cả hai giá trị sẽ giống nhau

2. **Khi cập nhật property** (`PUT /api/properties/{id}`):
   - `createdAt` giữ nguyên giá trị ban đầu
   - `updatedAt` được cập nhật tự động bằng thời điểm hiện tại

3. **Khi lấy danh sách** (`GET /api/properties`):
   - Tất cả items trong danh sách đều có `createdAt` và `updatedAt`

---

## Migration Guide cho Frontend

### 1. Cập nhật TypeScript Interface

```typescript
interface PropertyRes {
  propertyId: number;
  title: string;
  price: number;
  description: string;
  typeId: number;
  saleUserId: number;
  areaId: number;
  isPublic: boolean;
  isForRent: boolean;
  createdAt: string;  // Thêm mới
  updatedAt: string;  // Thêm mới
}
```

### 2. Xử lý DateTime

```typescript
// Parse ISO 8601 string thành Date object
const createdAt = new Date(property.createdAt);
const updatedAt = new Date(property.updatedAt);

// Format để hiển thị
const formattedDate = createdAt.toLocaleString('vi-VN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
});
```

### 3. Hiển thị thông tin

```typescript
// Hiển thị ngày tạo
<p>Ngày tạo: {formatDate(property.createdAt)}</p>

// Hiển thị ngày cập nhật
<p>Cập nhật lần cuối: {formatDate(property.updatedAt)}</p>

// So sánh để hiển thị "Mới cập nhật"
{isRecentlyUpdated(property.updatedAt) && (
  <Badge>Mới cập nhật</Badge>
)}
```

### 4. Sắp xếp theo ngày

```typescript
// Sắp xếp theo ngày tạo (mới nhất trước)
properties.sort((a, b) => 
  new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
);

// Sắp xếp theo ngày cập nhật (mới nhất trước)
properties.sort((a, b) => 
  new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
);
```

---

## Breaking Changes

**Không có breaking changes** - Đây là thay đổi additive (chỉ thêm trường mới), không ảnh hưởng đến code hiện tại của Frontend.

Tuy nhiên, nếu Frontend code có validation strict về số lượng fields, cần cập nhật để chấp nhận 2 trường mới này.

---

## Testing

Tất cả endpoints đã được test và verify:
- ✅ `GET /api/properties` - List response có `createdAt` và `updatedAt`
- ✅ `GET /api/properties/{id}` - Detail response có `createdAt` và `updatedAt`
- ✅ `POST /api/properties` - Create response có `createdAt` và `updatedAt`
- ✅ `PUT /api/properties/{id}` - Update response có `createdAt` và `updatedAt` (updatedAt được cập nhật)

---

## Ngày áp dụng

**Ngày release**: 2025-11-28

**Version**: Backend đã deploy với thay đổi này.

---

## Liên hệ

Nếu có thắc mắc hoặc vấn đề, vui lòng liên hệ Backend team.

