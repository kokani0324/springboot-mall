# 🛒 Spring Boot Mall 架構筆記（以商品 CRUD 為例）

> Spring Boot 三層式架構（Controller → Service → DAO）完整學習筆記
> 範例：**商品 Product** —— 已實作完整 CRUD（查單筆 / 查列表 / 新增 / 修改 / 刪除）
> 技術棧：Spring Boot 4.0.6 / Java 17 / MySQL / JdbcTemplate / Jackson 3

---

## 📌 一句話總覽

> **每一層只做自己份內的事，層與層之間只透過「介面」溝通。**

```
前端 → Controller → Service → DAO → 資料庫
        (收請求)    (商業邏輯)  (寫SQL)
                                  ↑
                            RowMapper 把查詢結果轉成物件
```

| 層 | 角色 | 商品對應的類別 |
| --- | --- | --- |
| Controller | API 入口，收請求 / 回結果 | `ProductController` |
| Service | 商業邏輯 | `ProductService` + `ProductServiceImpl` |
| DAO | 寫 SQL、碰資料庫 | `ProductDao` + `ProductDaoImpl` |
| RowMapper | DB 一列 → Java 物件 | `ProductRowMapper` |
| Model | 對應資料表 | `Product` |
| DTO | API 進出格式 | `ProductRequest` / `ProductQueryParms` |
| util | 共用工具 | `Page<T>` |
| constant | 常數 / 列舉 | `ProductCategory` |

---

## 🔢 商品 CRUD 對照表

| 操作 | HTTP 方法 | 網址 | Controller 方法 | 回傳狀態 |
| --- | --- | --- | --- | --- |
| 查單筆 | GET | `/products/{id}` | `getProduct` | 200 / 404 |
| 查列表 | GET | `/products` | `getProducts` | 200 |
| 新增 | POST | `/products` | `createProduct` | 201 |
| 修改 | PUT | `/products/{id}` | `updateProduct` | 200 / 404 |
| 刪除 | DELETE | `/products/{id}` | `deleteProduct` | 204 |

---

# 1️⃣ 查單筆 Read — `GET /products/{productId}`

## Controller

```java
@GetMapping("/products/{productId}")
public ResponseEntity<Product> getProduct(@PathVariable Integer productId) {
    Product product = productService.getProductById(productId);   // 叫 service 去查

    if (product != null) {
        return ResponseEntity.status(HttpStatus.OK).body(product);      // 查到 → 200
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();     // 沒查到 → 404
    }
}
```

**逐行重點：**
- `@PathVariable Integer productId` → 抓網址上的值，如 `/products/6` 就拿到 `6`
- 查詢的重點：**可能查不到**，所以一定要 `if (product != null)` 判斷
  - 查到 → 回 **200** + 商品 JSON
  - 查不到 → 回 **404**（`.build()` 表示沒有 body）

## Service（只是轉呼叫）

```java
@Override
public Product getProductById(Integer productId) {
    return productDao.getProductById(productId);
}
```

## DAO — 查詢核心

```java
@Override
public Product getProductById(Integer productId) {
    // 1. SQL 用具名參數 :productId（防 SQL injection）
    String sql = "select product_id, product_name, category, image_url, price, stock, " +
                 "description, created_date, last_modified_date " +
                 "FROM product WHERE product_id = :productId";

    // 2. 把參數值放進 map（key 要跟 :productId 一致）
    Map<String, Object> map = new HashMap<>();
    map.put("productId", productId);

    // 3. 執行查詢，結果用 ProductRowMapper 轉成 List<Product>
    List<Product> productList = namedParameterJdbcTemplate.query(sql, map, new ProductRowMapper());

    // 4. query() 永遠回傳 List，要自己判斷有沒有查到
    if (productList.size() > 0) {
        return productList.get(0);   // 有 → 回第一筆
    } else {
        return null;                 // 沒有 → 回 null（Controller 靠這個回 404）
    }
}
```

> ⚠️ `query()` **永遠回傳 List**，就算只查一筆。查不到時是「空 List」不是 null，所以用 `size() > 0` 判斷。

### `query()` 的三個參數

| 參數 | 意思 |
| --- | --- |
| `sql` | 要執行的 SQL |
| `map` | SQL 裡 `:參數` 的值 |
| `new ProductRowMapper()` | 把每一列轉成 Product 物件的工具 |

---

# 2️⃣ 查列表 Read List — `GET /products`（含篩選 / 排序 / 分頁）

## Controller

```java
@GetMapping("/products")
public ResponseEntity<Page<Product>> getProducts(
        // 篩選 Filtering（required=false → 可不傳）
        @RequestParam(required = false) ProductCategory category,
        @RequestParam(required = false) String search,
        // 排序 Sorting（有預設值）
        @RequestParam(defaultValue = "created_date") String orderBy,
        @RequestParam(defaultValue = "desc") String sort,
        // 分頁 Pagination（限制 limit 範圍）
        @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit,
        @RequestParam(defaultValue = "0") @Min(0) Integer offset
) {
    // 1. 把一堆參數打包成一個查詢條件物件
    ProductQueryParms params = new ProductQueryParms();
    params.setCategory(category);
    params.setSearch(search);
    params.setOrderBy(orderBy);
    params.setSort(sort);
    params.setLimit(limit);
    params.setOffset(offset);

    // 2. 取資料、取總筆數
    List<Product> productList = productService.getProducts(params);
    Integer total = productService.countProducts(params);

    // 3. 用 Page<T> 包裝分頁結果回傳
    Page<Product> page = new Page<>();
    page.setLimit(limit);
    page.setOffset(offset);
    page.setTotal(total);
    page.setResults(productList);

    return ResponseEntity.status(HttpStatus.OK).body(page);
}
```

**重點：**
- `@RequestParam` 抓 query string，如 `?search=B&category=CAR&limit=2&offset=0`
- `required = false` → 該參數可不傳；`defaultValue` → 沒傳時的預設值
- `@Max` / `@Min` → 限制數值範圍（搭配 class 上的 `@Validated` 生效）
- 把零散參數**打包成 `ProductQueryParms`** 往下傳，避免方法參數爆炸
- 回傳 `Page<Product>`，前端拿到 `{ limit, offset, total, results: [...] }`

## DAO — 動態拼接 SQL（這段最精華）

```java
@Override
public List<Product> getProducts(ProductQueryParms params) {
    String sql = "select ... FROM product where 1=1";   // 用 1=1 起頭，方便接 AND
    Map<String, Object> map = new HashMap<>();

    sql = addFilteringSql(sql, map, params);             // 接篩選條件

    // 排序
    sql = sql + " ORDER BY " + params.getOrderBy() + " " + params.getSort();
    // 分頁
    sql = sql + " LIMIT :limit OFFSET :offset";
    map.put("limit", params.getLimit());
    map.put("offset", params.getOffset());

    return namedParameterJdbcTemplate.query(sql, map, new ProductRowMapper());
}

// 把篩選條件抽出來共用（countProducts 和 getProducts 都用）
private String addFilteringSql(String sql, Map<String,Object> map, ProductQueryParms p) {
    if (p.getCategory() != null) {
        sql += " AND category = :category";
        map.put("category", p.getCategory().name());
    }
    if (p.getSearch() != null) {
        sql += " AND product_name LIKE :search";   // LIKE = 模糊查詢
        map.put("search", "%" + p.getSearch() + "%");
    }
    return sql;
}
```

> 💡 **`WHERE 1=1` 技巧**：永遠成立的條件，後面所有篩選都用 `AND ...` 接，即使一個篩選條件都沒有也不會語法錯誤。

---

# 3️⃣ 新增 Create — `POST /products`

## Controller

```java
@PostMapping("/products")
public ResponseEntity<Product> createProduct(@RequestBody @Valid ProductRequest productRequest) {
    Integer productId = productService.createProduct(productRequest);   // 新增，拿回自動產生的 id
    Product product = productService.getProductById(productId);         // 再用 id 撈完整資料
    return ResponseEntity.status(HttpStatus.CREATED).body(product);     // 回 201 + 完整商品
}
```

**逐行重點：**
- `@RequestBody` → 把前端 JSON 自動轉成 `ProductRequest` 物件
- `@Valid` → 進方法前先檢查 `ProductRequest` 的 `@NotNull` 規則，漏填必填欄位**自動回 400，根本不進方法**
- **為什麼要查兩次？** 新增時前端只送商品內容，但 `product_id`、`created_date` 是**資料庫自己產生的**，前端不知道，所以新增完要**用拿回的 id 再撈一次完整資料**回傳。

## DAO — INSERT + 拿回自動產生的 id

```java
@Override
public Integer createProduct(ProductRequest req) {
    String sql = "INSERT INTO product(product_name, category, ..., created_date, last_modified_date) " +
                 "VALUES(:productName, :category, ..., :createdDate, :lastModifiedDate)";

    Map<String, Object> map = new HashMap<>();
    map.put("productName", req.getProductName());
    map.put("category", req.getCategory().toString());
    // ... 其餘欄位

    Date now = new Date();                  // 由後端產生時間
    map.put("createdDate", now);
    map.put("lastModifiedDate", now);

    // KeyHolder 用來接「資料庫自動產生的主鍵 id」
    KeyHolder keyHolder = new GeneratedKeyHolder();
    namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map), keyHolder);

    int productId = keyHolder.getKey().intValue();   // 取出剛產生的 id
    return productId;
}
```

> 🔑 **`KeyHolder`** 是新增操作的關鍵：INSERT 後資料庫會自動編號，用它把那個 id 拿回來，Controller 才能再撈完整資料。
> 注意：新增/修改/刪除用的是 **`update()`**，查詢才用 `query()`。

---

# 4️⃣ 修改 Update — `PUT /products/{productId}`

## Controller

```java
@PutMapping("/products/{productId}")
public ResponseEntity<Product> updateProduct(@PathVariable Integer productId,
                                             @RequestBody @Valid ProductRequest productRequest) {
    // 1. 先檢查商品存不存在
    Product product = productService.getProductById(productId);
    if (product == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();   // 不存在 → 404
    }
    // 2. 執行修改
    productService.updateProduct(productId, productRequest);
    // 3. 撈出修改後的資料回傳
    Product updatedProduct = productService.getProductById(productId);
    return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);  // 200
}
```

**重點：** 修改 = 「查單筆」+「新增」的綜合
- 同時有 `@PathVariable`（哪一筆）和 `@RequestBody @Valid`（改成什麼）
- **先查存不存在** → 不存在回 404
- 改完再撈一次回傳最新資料

## DAO — UPDATE

```java
@Override
public void updateProduct(Integer productId, ProductRequest req) {
    String sql = "UPDATE product SET product_name = :productName, ..., " +
                 "last_modified_date = :lastModifiedDate WHERE product_id = :productId";
    Map<String, Object> map = new HashMap<>();
    map.put("productId", productId);
    // ... 其餘欄位
    map.put("lastModifiedDate", new Date());   // 更新「最後修改時間」

    namedParameterJdbcTemplate.update(sql, map);   // 沒有回傳值（void）
}
```

> 修改通常只更新 `last_modified_date`，**不動 `created_date`**（建立時間不該被改）。

---

# 5️⃣ 刪除 Delete — `DELETE /products/{productId}`

## Controller

```java
@DeleteMapping("/products/{productId}")
public ResponseEntity<?> deleteProduct(@PathVariable Integer productId) {
    productService.deleteProductById(productId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();   // 204
}
```

**重點：**
- 回 **204 No Content**（成功但沒有內容要回）
- **不用先檢查存不存在**：刪一個不存在的東西，結果一樣是「它不在了」，所以直接回 204（這叫**冪等性**，刪幾次結果都一樣）

## DAO — DELETE

```java
@Override
public void deleteProductById(Integer productId) {
    String sql = "DELETE FROM product WHERE product_id = :productId";
    Map<String, Object> map = new HashMap<>();
    map.put("productId", productId);
    namedParameterJdbcTemplate.update(sql, map);
}
```

---

# 🧩 配角們

## RowMapper — DB 一列 → Java 物件

```java
public class ProductRowMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));            // DB 欄位 → 物件屬性
        product.setProductName(rs.getString("product_name"));
        product.setCategory(ProductCategory.valueOf(rs.getString("category"))); // 字串 → enum
        product.setCreatedDate(rs.getTimestamp("created_date"));
        // ...
        return product;
    }
}
```

> DB 欄位是 `product_id`（底線），Java 是 `productId`（駝峰），對不起來，**要手動一一對應**。每查到一列，`mapRow` 就被呼叫一次。

## Model — 對應資料表

```java
public class Product {
    private Integer productId;        // ≈ product 表的 product_id
    private String productName;
    private ProductCategory category;
    private Integer price;
    private Date createdDate;
    // 只有欄位 + getter/setter，沒有任何邏輯
}
```

## DTO — API 進出格式

| DTO | 用途 |
| --- | --- |
| `ProductRequest` | 前端「送進來」的商品資料（含 `@NotNull` 驗證） |
| `ProductQueryParms` | 查列表的條件（category / search / sort / orderBy / limit / offset） |

```java
public class ProductRequest {
    @NotNull private String productName;     // 不能 null，否則回 400
    @NotNull private ProductCategory category;
    private String description;               // 沒加 @NotNull → 可不填
}
```

> **為什麼不直接用 Product model？** 前端不該送 `productId`、`createdDate`（那是 DB 產生的）。用 DTO 只開放該開放的欄位，較安全乾淨。

## util — `Page<T>` 分頁包裝

```java
public class Page<T> {           // 泛型，商品/使用者都能套
    private Integer limit;       // 一頁幾筆
    private Integer offset;      // 跳過幾筆
    private Integer total;       // 總筆數
    private List<T> results;     // 這一頁的資料
}
```

## constant — `ProductCategory` 列舉

```java
public enum ProductCategory { FOOD, CAR, BOOK }
```

> 用 enum 限制分類只能是這三種，打錯字編譯就擋下來。

---

# 🔄 完整資料流（以查單筆為例）

```
前端 GET /products/6
   │
   ▼
ProductController.getProduct()          解析網址、決定 HTTP 狀態碼
   │  productService.getProductById(6)
   ▼
ProductServiceImpl                      商業邏輯（目前只是轉呼叫）
   │  productDao.getProductById(6)
   ▼
ProductDaoImpl                          執行 SQL: SELECT ... WHERE product_id=6
   │  query(sql, map, new ProductRowMapper())
   ▼
ProductRowMapper                        把 DB 一列組裝成 Product
   │
   ▲  (Product 一路往上回傳)
   │
Controller 把 Product 轉成 JSON → 回前端 HTTP 200（null 則回 404）
```

---

# ✅ CRUD 重點速記

| 操作 | 關鍵字 / 重點 |
| --- | --- |
| 查單筆 | `query()` 回 List → 判斷 `size() > 0` → null 則 404 |
| 查列表 | `WHERE 1=1` + 動態拼 SQL；用 `Page<T>` + `total` 包裝 |
| 新增 | `@Valid` 驗證；`update()` + `KeyHolder` 拿回 id；查兩次 |
| 修改 | 先查存不存在（404）；只改 `last_modified_date` |
| 刪除 | `update()` 執行 DELETE；回 204；不用先查（冪等） |

### 共通觀念
1. **三層架構**：Controller（收/回）→ Service（邏輯）→ DAO（SQL）
2. **介面 / 實作分離**：依賴介面，方便替換與測試
3. **DTO ≠ Model**：DTO 管 API 格式，Model 對應資料表
4. **查詢用 `query()` + RowMapper；增改刪用 `update()`**
5. **具名參數 SQL**（`:param` + map）防 injection
6. **`@PathVariable`** 抓網址值、**`@RequestParam`** 抓 query string、**`@RequestBody`** 抓 JSON body
