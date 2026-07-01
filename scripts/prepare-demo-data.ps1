param(
    [string]$GatewayBaseUrl = "http://localhost:9000",
    [string]$MysqlHost = "localhost",
    [int]$MysqlPort = 3307,
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "root",
    [string]$Database = "youxuan_mall",
    [string]$AdminUsername = "admin",
    [string]$AdminPassword = "Admin@123456",
    [string]$DocPath = "docs/06-演示数据准备.md"
)

$ErrorActionPreference = "Stop"

function Invoke-MysqlSql {
    param([string]$Sql)
    $mysql = (Get-Command mysql -ErrorAction SilentlyContinue)
    if (-not $mysql) { throw "未找到 mysql 命令，请先把 MySQL client 加入 PATH。" }
    $oldMysqlPwd = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $MysqlPassword
        & $mysql.Source "--host=$MysqlHost" "--port=$MysqlPort" "--user=$MysqlUser" "--default-character-set=utf8mb4" "--database=$Database" "--batch" "--raw" "--execute=$Sql" 2>&1 | Out-String
    } finally {
        $env:MYSQL_PWD = $oldMysqlPwd
    }
}

function Escape-Sql {
    param([string]$Value)
    if ($null -eq $Value) { return "" }
    return $Value.Replace("\", "\\").Replace("'", "''")
}

function Get-AdminToken {
    try {
        $body = @{ username = $AdminUsername; password = $AdminPassword } | ConvertTo-Json -Compress
        $res = Invoke-RestMethod -Method Post -Uri "$GatewayBaseUrl/api/user/login" -ContentType "application/json" -Body $body -TimeoutSec 8
        return $res.data.token
    } catch {
        return $null
    }
}

$categories = @(
    @{ Key = "fruit"; Name = "新鲜水果"; Sort = 1 },
    @{ Key = "vegetable"; Name = "时令蔬菜"; Sort = 2 },
    @{ Key = "meat"; Name = "肉禽蛋品"; Sort = 3 },
    @{ Key = "bakery"; Name = "乳品烘焙"; Sort = 4 },
    @{ Key = "grain"; Name = "粮油调味"; Sort = 5 },
    @{ Key = "snack"; Name = "休闲零食"; Sort = 6 },
    @{ Key = "drink"; Name = "酒水饮料"; Sort = 7 },
    @{ Key = "daily"; Name = "日用百货"; Sort = 8 }
)

$products = @(
    @{ Category = "fruit"; Name = "优选苹果"; Subtitle = "脆甜多汁，产地直供"; Price = "19.90"; Original = "29.90"; Sales = 128; Stock = 300 },
    @{ Category = "fruit"; Name = "阳光玫瑰葡萄"; Subtitle = "果香浓郁，清甜爽口"; Price = "39.90"; Original = "49.90"; Sales = 96; Stock = 180 },
    @{ Category = "fruit"; Name = "海南香蕉"; Subtitle = "软糯香甜，早餐常备"; Price = "12.90"; Original = "16.90"; Sales = 152; Stock = 260 },
    @{ Category = "fruit"; Name = "四川爱媛橙"; Subtitle = "果肉细嫩，汁水充盈"; Price = "29.90"; Original = "39.90"; Sales = 118; Stock = 200 },
    @{ Category = "fruit"; Name = "精品蓝莓"; Subtitle = "酸甜适口，小盒鲜享"; Price = "25.90"; Original = "32.90"; Sales = 86; Stock = 160 },
    @{ Category = "vegetable"; Name = "有机西红柿"; Subtitle = "自然成熟，酸甜适口"; Price = "12.90"; Original = "16.90"; Sales = 88; Stock = 220 },
    @{ Category = "vegetable"; Name = "新鲜黄瓜"; Subtitle = "清脆爽口，凉拌优选"; Price = "8.90"; Original = "11.90"; Sales = 106; Stock = 240 },
    @{ Category = "vegetable"; Name = "高山娃娃菜"; Subtitle = "鲜嫩清甜，火锅常备"; Price = "9.90"; Original = "13.90"; Sales = 74; Stock = 200 },
    @{ Category = "vegetable"; Name = "云南生菜"; Subtitle = "叶片鲜嫩，轻食优选"; Price = "7.90"; Original = "10.90"; Sales = 69; Stock = 190 },
    @{ Category = "vegetable"; Name = "精品土豆"; Subtitle = "粉糯绵密，家常百搭"; Price = "6.90"; Original = "9.90"; Sales = 143; Stock = 320 },
    @{ Category = "meat"; Name = "鲜鸡蛋"; Subtitle = "营养新鲜，早餐常备"; Price = "18.80"; Original = "22.80"; Sales = 140; Stock = 260 },
    @{ Category = "meat"; Name = "冷鲜鸡胸肉"; Subtitle = "低脂高蛋白，健身优选"; Price = "29.90"; Original = "39.90"; Sales = 84; Stock = 150 },
    @{ Category = "meat"; Name = "精选猪里脊"; Subtitle = "肉质细嫩，炒菜更香"; Price = "36.90"; Original = "45.90"; Sales = 91; Stock = 150 },
    @{ Category = "meat"; Name = "牛肉卷"; Subtitle = "肥瘦相间，火锅优选"; Price = "49.90"; Original = "65.90"; Sales = 112; Stock = 120 },
    @{ Category = "meat"; Name = "鸡翅中"; Subtitle = "肉厚鲜嫩，煎烤皆宜"; Price = "32.90"; Original = "42.90"; Sales = 97; Stock = 180 },
    @{ Category = "bakery"; Name = "纯牛奶"; Subtitle = "醇香顺滑，整箱优选"; Price = "49.90"; Original = "59.90"; Sales = 118; Stock = 160 },
    @{ Category = "bakery"; Name = "原味酸奶"; Subtitle = "浓稠顺滑，低温鲜享"; Price = "22.90"; Original = "29.90"; Sales = 83; Stock = 150 },
    @{ Category = "bakery"; Name = "全麦吐司"; Subtitle = "松软麦香，低脂轻食"; Price = "15.90"; Original = "19.90"; Sales = 72; Stock = 130 },
    @{ Category = "bakery"; Name = "奶香小餐包"; Subtitle = "柔软香甜，早餐搭配"; Price = "18.90"; Original = "24.90"; Sales = 63; Stock = 130 },
    @{ Category = "bakery"; Name = "芝士蛋糕"; Subtitle = "浓郁奶香，下午茶优选"; Price = "39.90"; Original = "49.90"; Sales = 58; Stock = 90 },
    @{ Category = "grain"; Name = "五常大米"; Subtitle = "米香浓郁，软糯适口"; Price = "69.90"; Original = "89.90"; Sales = 109; Stock = 120 },
    @{ Category = "grain"; Name = "东北珍珠米"; Subtitle = "颗粒饱满，家庭囤货"; Price = "59.90"; Original = "75.90"; Sales = 92; Stock = 140 },
    @{ Category = "grain"; Name = "花生油"; Subtitle = "浓香压榨，厨房常备"; Price = "79.90"; Original = "99.90"; Sales = 81; Stock = 120 },
    @{ Category = "grain"; Name = "生抽酱油"; Subtitle = "鲜香调味，炒菜凉拌"; Price = "13.90"; Original = "18.90"; Sales = 121; Stock = 260 },
    @{ Category = "grain"; Name = "香醋"; Subtitle = "酸香柔和，蘸拌皆宜"; Price = "12.90"; Original = "16.90"; Sales = 79; Stock = 220 },
    @{ Category = "snack"; Name = "每日坚果"; Subtitle = "坚果果干科学搭配"; Price = "45.90"; Original = "59.90"; Sales = 110; Stock = 180 },
    @{ Category = "snack"; Name = "海苔脆片"; Subtitle = "咸香酥脆，轻负担零食"; Price = "16.90"; Original = "22.90"; Sales = 87; Stock = 200 },
    @{ Category = "snack"; Name = "薯片"; Subtitle = "经典原味，聚会分享"; Price = "9.90"; Original = "13.90"; Sales = 136; Stock = 260 },
    @{ Category = "snack"; Name = "饼干礼盒"; Subtitle = "多口味组合，送礼自用"; Price = "35.90"; Original = "45.90"; Sales = 77; Stock = 150 },
    @{ Category = "snack"; Name = "山楂条"; Subtitle = "酸甜开胃，独立小包"; Price = "12.90"; Original = "17.90"; Sales = 93; Stock = 190 },
    @{ Category = "drink"; Name = "矿泉水"; Subtitle = "清冽甘甜，整箱装"; Price = "24.90"; Original = "32.90"; Sales = 160; Stock = 300 },
    @{ Category = "drink"; Name = "橙汁"; Subtitle = "果香清新，早餐搭配"; Price = "18.90"; Original = "25.90"; Sales = 103; Stock = 180 },
    @{ Category = "drink"; Name = "无糖气泡水"; Subtitle = "清爽无负担，餐后解腻"; Price = "29.90"; Original = "39.90"; Sales = 99; Stock = 180 },
    @{ Category = "drink"; Name = "椰子水"; Subtitle = "自然清甜，运动补水"; Price = "35.90"; Original = "45.90"; Sales = 88; Stock = 150 },
    @{ Category = "drink"; Name = "绿茶饮料"; Subtitle = "茶香清爽，整箱优惠"; Price = "28.90"; Original = "36.90"; Sales = 101; Stock = 190 },
    @{ Category = "daily"; Name = "抽纸"; Subtitle = "柔韧亲肤，家庭必备"; Price = "29.90"; Original = "39.90"; Sales = 148; Stock = 260 },
    @{ Category = "daily"; Name = "洗衣液"; Subtitle = "洁净留香，温和去渍"; Price = "39.90"; Original = "52.90"; Sales = 116; Stock = 180 },
    @{ Category = "daily"; Name = "洗手液"; Subtitle = "细腻泡沫，清洁护手"; Price = "16.90"; Original = "22.90"; Sales = 94; Stock = 220 },
    @{ Category = "daily"; Name = "垃圾袋"; Subtitle = "加厚耐用，不易破漏"; Price = "12.90"; Original = "18.90"; Sales = 130; Stock = 300 },
    @{ Category = "daily"; Name = "厨房湿巾"; Subtitle = "去油除污，厨房清洁"; Price = "19.90"; Original = "26.90"; Sales = 87; Stock = 180 }
)

$repoRoot = (Resolve-Path ".").Path
$docFullPath = Join-Path $repoRoot $DocPath
$docDir = Split-Path $docFullPath -Parent
if (-not (Test-Path $docDir)) { New-Item -ItemType Directory -Path $docDir | Out-Null }

$snapshotSql = @"
SET NAMES utf8mb4;
SELECT 'product_category' AS table_name, COUNT(*) AS total FROM product_category;
SELECT 'product' AS table_name, COUNT(*) AS total FROM product;
SELECT 'product_stock' AS table_name, COUNT(*) AS total FROM product_stock;
SELECT 'cart_item' AS table_name, COUNT(*) AS total FROM cart_item;
SELECT 'user_address' AS table_name, COUNT(*) AS total FROM user_address;
SELECT 'mall_order' AS table_name, COUNT(*) AS total FROM mall_order;
SELECT 'mall_order_item' AS table_name, COUNT(*) AS total FROM mall_order_item;
SELECT 'home_banner' AS table_name, COUNT(*) AS total FROM home_banner;
SELECT 'home_recommend' AS table_name, COUNT(*) AS total FROM home_recommend;
SELECT 'coupon' AS table_name, COUNT(*) AS total FROM coupon;
SELECT 'user_coupon' AS table_name, COUNT(*) AS total FROM user_coupon;
SELECT id, name FROM product_category ORDER BY id LIMIT 80;
SELECT id, name, category_id, main_image, price, sales, status FROM product ORDER BY id LIMIT 120;
"@
$preSnapshot = Invoke-MysqlSql -Sql $snapshotSql

$cleanupSql = @"
SET NAMES utf8mb4;
DELETE FROM cart_item; SELECT 'cart_item' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM mall_order_item; SELECT 'mall_order_item' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM mall_order; SELECT 'mall_order' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM user_address; SELECT 'user_address' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM user_coupon; SELECT 'user_coupon' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM coupon; SELECT 'coupon' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM home_recommend; SELECT 'home_recommend' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM home_banner; SELECT 'home_banner' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM product_stock; SELECT 'product_stock' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM product; SELECT 'product' AS table_name, ROW_COUNT() AS deleted_rows;
DELETE FROM product_category; SELECT 'product_category' AS table_name, ROW_COUNT() AS deleted_rows;
"@
$deleteResult = Invoke-MysqlSql -Sql $cleanupSql

$sql = "SET NAMES utf8mb4;`n"
foreach ($category in $categories) {
    $sql += "INSERT INTO product_category (name, parent_id, sort, status, deleted) VALUES ('$((Escape-Sql $category.Name))', 0, $($category.Sort), 1, 0);`n"
    $sql += "SET @cat_$($category.Key) = LAST_INSERT_ID();`n"
}
$index = 1
foreach ($product in $products) {
    $detail = "$($product.Name)：$($product.Subtitle)，优选商城演示商品，适合前台展示、购物车和下单测试。"
    $sql += "INSERT INTO product (category_id, name, subtitle, main_image, detail, price, original_price, sales, status, deleted) VALUES (@cat_$($product.Category), '$((Escape-Sql $product.Name))', '$((Escape-Sql $product.Subtitle))', '', '$((Escape-Sql $detail))', $($product.Price), $($product.Original), $($product.Sales), 1, 0);`n"
    $sql += "SET @p$index = LAST_INSERT_ID(); INSERT INTO product_stock (product_id, stock, locked_stock) VALUES (@p$index, $($product.Stock), 0);`n"
    $index++
}
$sql += @"
INSERT INTO home_banner (title, image_url, link_type, link_value, sort, status, deleted) VALUES
('新鲜水果 优选好物', '', 'CATEGORY', CAST(@cat_fruit AS CHAR), 1, 1, 0),
('早餐鲜享 乳品烘焙', '', 'CATEGORY', CAST(@cat_bakery AS CHAR), 2, 1, 0),
('家庭囤货 粮油百货', '', 'CATEGORY', CAST(@cat_grain AS CHAR), 3, 1, 0);

INSERT INTO home_recommend (title, product_id, sort, status, deleted) VALUES
('今日鲜果', @p1, 1, 1, 0),
('当季爆款', @p2, 2, 1, 0),
('早餐组合', @p11, 3, 1, 0),
('低脂优选', @p12, 4, 1, 0),
('家庭囤货', @p21, 5, 1, 0),
('休闲零食', @p26, 6, 1, 0),
('整箱饮品', @p31, 7, 1, 0),
('日用刚需', @p36, 8, 1, 0);

INSERT INTO coupon (name, amount, min_amount, total_stock, available_stock, start_time, end_time, status, scope_type, category_id, deleted) VALUES
('新人满50减5', 5.00, 50.00, 200, 200, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'ALL', NULL, 0),
('满99减10', 10.00, 99.00, 200, 200, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'ALL', NULL, 0),
('满199减25', 25.00, 199.00, 150, 150, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'ALL', NULL, 0),
('生鲜专区满129减15', 15.00, 129.00, 150, 150, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'CATEGORY', @cat_fruit, 0),
('乳品烘焙满79减8', 8.00, 79.00, 120, 120, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'CATEGORY', @cat_bakery, 0),
('全场满299减40', 40.00, 299.00, 100, 100, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 'ALL', NULL, 0);

INSERT INTO user_address (user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_flag, deleted)
SELECT id, '演示用户', '13800000000', '北京市', '北京市', '朝阳区', '优选商城演示地址 1001 室', 1, 0
FROM sys_user WHERE username = '$AdminUsername';
"@
$insertResult = Invoke-MysqlSql -Sql $sql

$redisResult = ""
try {
    $detailKeys = & docker exec youxuan-redis redis-cli --scan --pattern "youxuan:product:detail:*" 2>&1
    foreach ($key in $detailKeys) { if ($key) { & docker exec youxuan-redis redis-cli DEL $key | Out-Null } }
    $homeRedisResult = (& docker exec youxuan-redis redis-cli DEL youxuan:home:index youxuan:home:banners youxuan:home:recommend-products youxuan:home:hot-products 2>&1 | Out-String).Trim()
    $redisResult = "product detail keys cleared; home keys deleted=$homeRedisResult"
} catch {
    $redisResult = "未清理 Redis：$($_.Exception.Message)"
}

$token = Get-AdminToken
$preheatResult = ""
if ($token) {
    try {
        $couponPage = Invoke-RestMethod -Method Get -Uri "$GatewayBaseUrl/api/coupon/page?pageNum=1&pageSize=50" -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 10
        $preheated = @()
        foreach ($coupon in @($couponPage.data.records)) {
            Invoke-RestMethod -Method Post -Uri "$GatewayBaseUrl/api/coupon/$($coupon.id)/preheat" -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 10 | Out-Null
            $preheated += "$($coupon.id):$($coupon.name)"
        }
        $preheatResult = "已预热优惠券：" + ($preheated -join "、")
    } catch {
        $preheatResult = "优惠券预热调用失败：$($_.Exception.Message)"
    }
} else {
    $preheatResult = "未登录 Gateway，跳过优惠券预热。"
}

$importResult = ""
if ($token) {
    try {
        $import = Invoke-RestMethod -Method Post -Uri "$GatewayBaseUrl/api/search/product/importAll" -Headers @{ Authorization = "Bearer $token" } -TimeoutSec 20
        $importResult = ($import | ConvertTo-Json -Depth 8 -Compress)
    } catch {
        $importResult = "ES 导入调用失败：$($_.Exception.Message)"
    }
} else {
    $importResult = "未登录 Gateway，跳过 ES 导入。"
}

$postCounts = Invoke-MysqlSql -Sql @"
SET NAMES utf8mb4;
SELECT 'product_category' AS table_name, COUNT(*) AS total FROM product_category;
SELECT 'product' AS table_name, COUNT(*) AS total FROM product;
SELECT 'product_stock' AS table_name, COUNT(*) AS total FROM product_stock;
SELECT 'home_banner' AS table_name, COUNT(*) AS total FROM home_banner;
SELECT 'home_recommend' AS table_name, COUNT(*) AS total FROM home_recommend;
SELECT 'coupon' AS table_name, COUNT(*) AS total FROM coupon;
SELECT 'user_address' AS table_name, COUNT(*) AS total FROM user_address;
"@

$now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$doc = @"
# 06-演示数据准备

更新时间：$now

## 清理范围

本脚本只处理本地开发库 $Database 中用于演示的业务数据表，保留 sys_user 用户表，不删除登录账号。

清理顺序：cart_item、mall_order_item、mall_order、user_address、user_coupon、coupon、home_recommend、home_banner、product_stock、product、product_category。

## 清理前 SELECT 快照

~~~text
$preSnapshot
~~~

## 实际删除行数

~~~text
$deleteResult
~~~

## 演示数据

重建 8 个分类，每个分类 5 个商品，共 40 个商品。商品 main_image 默认留空，前端显示本地占位图；后续在后台商品管理替换 mainImage 后，首页、商品列表、商品详情、购物车和订单确认页都会优先显示真实图片。

分类：新鲜水果、时令蔬菜、肉禽蛋品、乳品烘焙、粮油调味、休闲零食、酒水饮料、日用百货。

同时创建：首页 Banner 3 条、首页推荐位 8 条、优惠券 6 张（含全场券和分类券）、管理员账号默认收货地址 1 条。

首页推荐位绑定的 product_id 均来自本脚本刚插入的真实上架商品；不会插入“未命名商品”、乱码商品、网络图片 URL 或本地图片路径。

插入结果：

~~~text
$insertResult
~~~

插入后数量：

~~~text
$postCounts
~~~

## 图片说明

本阶段不再自动从 D:\图片\Pictures 匹配或上传商品图片，也不使用网络图片。

如果后台替换商品图片后首页仍显示旧图，通常是 Redis 首页缓存或商品详情缓存尚未刷新。可执行本脚本，或手动删除：

~~~text
youxuan:home:index
youxuan:home:hot-products
youxuan:home:recommend-products
youxuan:product:detail:{productId}
~~~

## 缓存与索引

Redis 清理结果：

~~~text
$redisResult
~~~

优惠券预热结果：

~~~text
$preheatResult
~~~

Elasticsearch 重新导入结果：

~~~text
$importResult
~~~

## 重新执行方式

在仓库根目录执行：

~~~powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-demo-data.ps1
~~~

要求：MySQL localhost:3307 可访问，账号 root/root；如需同步搜索结果，search-service、product-service、Gateway 需要已启动；本脚本不会修改 docker/docker-compose.yml。
"@
Set-Content -LiteralPath $docFullPath -Value $doc -Encoding UTF8

Write-Output "DEMO_DATA_DONE"
Write-Output "DOC=$docFullPath"
Write-Output "DELETE_RESULT:"
Write-Output $deleteResult
Write-Output "POST_COUNTS:"
Write-Output $postCounts
Write-Output "REDIS=$redisResult"
Write-Output "ES_IMPORT=$importResult"
