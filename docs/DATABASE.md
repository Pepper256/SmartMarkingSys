# SQLite 数据库（可验收版本）

本项目已将原先的 `data.json` 文件存储替换为 **SQLite 真数据库**。

## 你能“监督/验收”的点（不需要看代码）

### 1) 必须出现的数据库文件

默认数据库文件路径：

- `./database/smartmark.db`

第一次运行/第一次写入时会自动创建该 `.db` 文件。

### 2) 必须有可见的表结构

- 建表脚本：`src/main/resources/database/schema.sql`

### 3) 数据必须能持久化

验收方法：

1. 运行一次“上传试卷+答案”（或调用 DAO 存储）
2. 退出程序
3. 再次运行
4. **数据库文件仍然存在**，并且数据仍在（用 DB Browser for SQLite 打开查看）

## 如何查看数据库内容（推荐）

使用 **DB Browser for SQLite**：

1. 打开 `./database/smartmark.db`
2. 查看 Tables：`exam_paper`、`answer_paper`

## 说明

为什么数据库不放在 `src/main/resources`？

因为 resources 在打包后通常是只读的，放那里经常导致“写不进去/写了但找不到”的问题。
