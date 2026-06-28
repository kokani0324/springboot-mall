# springboot-mall

一個使用 **Spring Boot** 打造的電商後端 RESTful API，包含 **商品（Product）**、**會員（User）**、**訂單（Order）** 三大功能模組，採用經典的分層架構（Controller → Service → DAO），並使用 Spring JDBC 操作 MySQL 資料庫。

> 本專案為學習用途，實作了電商系統最核心的後端流程：商品的 CRUD 與篩選分頁、會員註冊登入、下單與庫存扣除。

---

## 目錄

- [技術棧](#技術棧)
- [主要功能](#主要功能)
- [專案結構](#專案結構)
- [環境需求](#環境需求)
- [快速開始](#快速開始)
- [API 文件](#api-文件)
  - [商品 Product](#商品-product)
  - [會員 User](#會員-user)
  - [訂單 Order](#訂單-order)
- [資料庫結構](#資料庫結構)

---

## 技術棧

| 類別 | 技術 |
|------|------|
| 語言 | Java 17 |
| 框架 | Spring Boot 4.0.6、Spring MVC |
| 資料存取 | Spring JDBC（`NamedParameterJdbcTemplate`） |
| 資料驗證 | Spring Boot Validation（Bean Validation） |
| 資料庫 | MySQL 8.x（主要）、H2（內建） |
| 建置工具 | Maven |

---

## 主要功能

- **商品模組**
  - 商品查詢，支援 **類別篩選**、**關鍵字搜尋**、**排序**、**分頁**
  - 查詢單一商品
  - 新增、修改、刪除商品
- **會員模組**
  - 會員註冊（密碼以 **MD5** 雜湊後儲存、Email 唯一性檢查）
  - 會員登入
- **訂單模組**
  - 建立訂單（檢查使用者與商品是否存在、檢查庫存、自動 **扣除庫存**、計算總金額）
  - 查詢使用者的訂單列表（含訂單明細、分頁）

---

## 專案結構

```
src/main/java/com/kuanyu/springbootmall
├── SpringbootMallApplication.java   # 程式進入點
├── controller/                      # 接收 HTTP 請求、回傳 JSON
├── service/                         # 商業邏輯（含 impl 實作）
├── dao/                             # 資料存取層（含 impl 實作）
├── rowmapper/                       # 將資料庫 ResultSet 轉成 Java 物件
├── model/                           # 資料模型（Product、User、Order、OrderItem）
├── dto/                             # 請求/查詢參數物件
├── constant/                        # 常數與列舉（如 ProductCategory）
└── util/                            # 工具類（如分頁 Page）
```

分層職責：

```
Controller  →  Service  →  DAO  →  MySQL
 接收請求      商業邏輯     SQL 操作    資料庫
```

---

## 環境需求

- JDK 17 以上
- Maven 3.6 以上（或使用專案內附的 `mvnw`）
- MySQL 8.x

---

## 快速開始

### 1. 建立資料庫

使用專案內附的 SQL 腳本 [`sql/mall.sql`](sql/mall.sql) 建立 `mall` 資料庫與資料表（包含選用的測試資料）：

```bash
mysql -u root -p < sql/mall.sql
```

> 也可以直接在 MySQL Workbench 開啟 `sql/mall.sql` 後執行。

### 2. 設定資料庫連線

修改 `src/main/resources/application.properties` 中的連線設定，改成你自己的 MySQL 帳號密碼：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mall?serverTimezone=Asia/Taipei&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的密碼
```

### 3. 啟動專案

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

啟動後預設服務位置為 `http://localhost:8080`。

---

## API 文件

Base URL：`http://localhost:8080`

所有請求與回應的 Content-Type 皆為 `application/json`。

### 商品 Product

#### 查詢商品列表

```
GET /products
```

**Query 參數**

| 參數 | 型別 | 必填 | 預設值 | 說明 |
|------|------|:----:|--------|------|
| `category` | string | 否 | - | 商品類別：`FOOD` / `CAR` / `BOOK` |
| `search` | string | 否 | - | 商品名稱關鍵字（模糊查詢） |
| `orderBy` | string | 否 | `created_date` | 排序欄位 |
| `sort` | string | 否 | `desc` | 排序方式：`asc` / `desc` |
| `limit` | int | 否 | `5` | 每頁筆數（0–1000） |
| `offset` | int | 否 | `0` | 略過筆數 |

**範例**：`GET /products?category=FOOD&search=蘋果&limit=5&offset=0`

**回應 `200 OK`**

```json
{
    "limit": 5,
    "offset": 0,
    "total": 1,
    "results": [
        {
            "productId": 1,
            "productName": "蘋果（一斤）",
            "category": "FOOD",
            "imageUrl": "http://example.com/apple.png",
            "price": 80,
            "stock": 10,
            "description": "進口富士蘋果",
            "createdDate": "2026-06-28 14:00:00",
            "lastModifiedDate": "2026-06-28 14:00:00"
        }
    ]
}
```

#### 查詢單一商品

```
GET /products/{productId}
```

**回應**：`200 OK`（回傳商品物件）／ `404 Not Found`（商品不存在）

#### 新增商品

```
POST /products
```

**Request Body**

```json
{
    "productName": "蘋果（一斤）",
    "category": "FOOD",
    "imageUrl": "http://example.com/apple.png",
    "price": 80,
    "stock": 10,
    "description": "進口富士蘋果"
}
```

> `productName`、`category`、`imageUrl`、`price`、`stock` 為必填；`description` 可省略。

**回應 `201 Created`**：回傳新建立的商品物件。

#### 修改商品

```
PUT /products/{productId}
```

Request Body 同「新增商品」。

**回應**：`200 OK`（回傳更新後商品）／ `404 Not Found`（商品不存在）

#### 刪除商品

```
DELETE /products/{productId}
```

**回應 `204 No Content`**

---

### 會員 User

#### 註冊

```
POST /users/register
```

**Request Body**

```json
{
    "email": "test@example.com",
    "password": "123456"
}
```

> `email` 需符合 Email 格式且不可重複；`password` 不可為空，儲存時會以 MD5 雜湊。

**回應 `201 Created`**

```json
{
    "userId": 1,
    "email": "test@example.com",
    "createDate": "2026-06-28 14:00:00",
    "lastModifiedDate": "2026-06-28 14:00:00"
}
```

> 基於安全考量，回應中不會包含 `password` 欄位。

#### 登入

```
POST /users/login
```

**Request Body**

```json
{
    "email": "test@example.com",
    "password": "123456"
}
```

**回應 `200 OK`**：回傳會員物件（不含密碼）。

---

### 訂單 Order

#### 建立訂單

```
POST /users/{userId}/orders
```

**Request Body**

```json
{
    "buyItemsList": [
        { "productId": 1, "quantity": 2 },
        { "productId": 3, "quantity": 1 }
    ]
}
```

> 系統會檢查使用者與商品是否存在、商品庫存是否足夠，並於下單成功後自動扣除庫存、計算訂單總金額。

**回應 `201 Created`**

```json
{
    "orderId": 1,
    "userId": 1,
    "totalAmount": 660,
    "createdDate": "2026-06-28 14:00:00",
    "lastModifiedDate": "2026-06-28 14:00:00",
    "orderItemList": [
        {
            "orderItemId": 1,
            "orderId": 1,
            "productId": 1,
            "quantity": 2,
            "amount": 160,
            "productName": "蘋果（一斤）",
            "imageUrl": "http://example.com/apple.png"
        },
        {
            "orderItemId": 2,
            "orderId": 1,
            "productId": 3,
            "quantity": 1,
            "amount": 500,
            "productName": "Toyota 模型車",
            "imageUrl": "http://example.com/car.png"
        }
    ]
}
```

#### 查詢訂單列表

```
GET /users/{userId}/orders
```

**Query 參數**

| 參數 | 型別 | 必填 | 預設值 | 說明 |
|------|------|:----:|--------|------|
| `limit` | int | 否 | `10` | 每頁筆數（0–100） |
| `offset` | int | 否 | `0` | 略過筆數 |

**回應 `200 OK`**：回傳分頁結構（`limit` / `offset` / `total` / `results`），`results` 為訂單陣列，每筆訂單包含其 `orderItemList`。

---

## 資料庫結構

| 資料表 | 說明 |
|--------|------|
| `product` | 商品 |
| `user` | 會員（`email` 為唯一鍵） |
| `order` | 訂單（`order` 為 MySQL 保留字，查詢時需以反引號 `` `order` `` 包住） |
| `order_item` | 訂單明細（一筆訂單對多個商品項目） |

完整的建表語法請見 [`sql/mall.sql`](sql/mall.sql)。

### product

| 欄位 | 型別 | 說明 |
|------|------|------|
| `product_id` | INT, PK, AI | 商品編號 |
| `product_name` | VARCHAR(128) | 商品名稱 |
| `category` | VARCHAR(32) | 類別（FOOD / CAR / BOOK） |
| `image_url` | VARCHAR(256) | 商品圖片網址 |
| `price` | INT | 價格 |
| `stock` | INT | 庫存 |
| `description` | VARCHAR(1024), NULL | 商品描述 |
| `created_date` | TIMESTAMP | 建立時間 |
| `last_modified_date` | TIMESTAMP | 最後修改時間 |

### user

| 欄位 | 型別 | 說明 |
|------|------|------|
| `user_id` | INT, PK, AI | 會員編號 |
| `email` | VARCHAR(256), UNIQUE | 電子信箱 |
| `password` | VARCHAR(256) | 密碼（MD5 雜湊） |
| `created_date` | TIMESTAMP | 建立時間 |
| `last_modified_date` | TIMESTAMP | 最後修改時間 |

### order

| 欄位 | 型別 | 說明 |
|------|------|------|
| `order_id` | INT, PK, AI | 訂單編號 |
| `user_id` | INT | 會員編號 |
| `total_amount` | INT | 訂單總金額 |
| `created_date` | TIMESTAMP | 建立時間 |
| `last_modified_date` | TIMESTAMP | 最後修改時間 |

### order_item

| 欄位 | 型別 | 說明 |
|------|------|------|
| `order_item_id` | INT, PK, AI | 明細編號 |
| `order_id` | INT | 所屬訂單編號 |
| `product_id` | INT | 商品編號 |
| `quantity` | INT | 購買數量 |
| `amount` | INT | 小計金額 |
