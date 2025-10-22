### Curl 入门指南：掌握 HTTP 请求的核心技巧

Curl 是命令行中强大的 HTTP 工具，支持多种协议。本文按最佳实践详解核心功能，适用于**curl 7.87.0+**（推荐更新到最新版）。

---

#### 1. **请求方法**

**最佳实践：** 用`-X`明确方法，GET 参数用`--url-query`避免转义问题（替代旧版`-G`+`-d`）。

```bash
# GET（默认方法，无需-X）
curl --url-query "name=John" --url-query "age=30" https://api.example.com/users

# POST
curl -X POST https://api.example.com/users

# PUT/DELETE
curl -X PUT https://api.example.com/users/123
curl -X DELETE https://api.example.com/users/123
```

---

#### 2. **请求头**

**最佳实践：** 用`-H`添加头部，JSON 类型优先指定`Content-Type`。

```bash
curl -H "Authorization: Bearer TOKEN" \
     -H "X-Custom-Header: value" \
     -H "Content-Type: application/json" \
     https://api.example.com/data
```

---

#### 3. **Cookie 管理**

**最佳实践：** 用`-b`发送 Cookie，`-c`保存响应 Cookie 到文件。

```bash
# 发送Cookie
curl -b "sessionid=abc123" https://example.com

# 保存响应Cookie并复用
curl -c cookies.txt https://example.com/login
curl -b cookies.txt https://example.com/dashboard
```

---

#### 4. **请求体**

**最佳实践：** 根据内容类型选择参数，文件上传用`-F`。

##### 4.1 普通数据（JSON/表单）

```bash
# JSON数据
curl -X POST -d '{"name":"John"}' \
     -H "Content-Type: application/json" \
     https://api.example.com/users

# URL编码表单
curl -X POST -d "username=john" -d "password=123" \
     https://api.example.com/login
```

##### 4.2 multipart/form-data（文件上传）

```bash
# 上传文件+文本字段（自动设置Content-Type）
curl -X POST -F "avatar=@photo.jpg" \
     -F "description=Profile image" \
     https://api.example.com/upload

# 指定文件名和类型
curl -X POST -F "file=@report.pdf;filename=custom.pdf;type=application/pdf" \
     https://api.example.com/docs
```

---

#### 5. **请求过程追踪**

**最佳实践：** 用`-v`调试头部，`--trace`记录完整通信。

```bash
# 查看请求/响应头
curl -v https://example.com

# 输出完整二进制通信过程
curl --trace trace.bin https://example.com

# 仅查看SSL握手信息
curl --trace-ssl ssl.log https://example.com
```

---

#### 6. **处理响应**

**最佳实践：** 用管道组合工具处理响应，分离头与体。

##### 6.1 响应体处理

```bash
# 格式化JSON响应（需jq）
curl https://api.example.com/data | jq .

# 仅显示HTTP状态码
curl -o /dev/null -s -w "%{http_code}\n" https://example.com
```

##### 6.2 响应头处理

```bash
# 将响应头保存到文件
curl -D headers.txt https://example.com

# 仅显示响应头
curl -I https://example.com
```

---

#### 7. **输出控制**

**最佳实践：** 用`-sS`静默模式，`-o`重定向输出。

```bash
# 静默模式（隐藏进度，显示错误）
curl -sS https://example.com

# 输出到终端+保存到文件
curl https://example.com | tee output.html

# 丢弃响应体（只关注头/状态码）
curl -o /dev/null https://example.com
```

---

#### 8. **文件操作**

**最佳实践：** 用`-O`保留远程文件名，`-J`配合重定向。

```bash
# 保留服务器文件名
curl -O https://example.com/files/report.pdf

# 自动从Content-Disposition获取文件名
curl -O -J https://example.com/generate-report

# 分段下载（恢复下载）
curl -C - -O https://example.com/largefile.zip
```

---

### 完整工作流示例

#### 1. 带认证的文件上传

```bash
curl -X POST \
  -H "Authorization: Bearer xyz" \
  -F "metadata=@data.json;type=application/json" \
  -F "image=@photo.jpg" \
  -o response.json \
  -c session.cookie \
  -sS \
  https://api.example.com/upload
```

#### 2. 复杂 API 调用（JSON 体+查询参数）

```bash
curl -X PUT \
  --url-query "version=2" \
  -H "Content-Type: application/json" \
  -d '{"status":"active"}' \
  https://api.example.com/users/123?debug=true
```

> **关键参数说明：**
>
> - `-F`：multipart/form-data 上传（自动生成 boundary）
> - `-sS`：静默模式但显示错误
> - `-o response.json`：保存响应到文件
> - `--url-query`：安全添加 URL 参数（避免手动编码）

掌握这些实践能提升命令行 HTTP 操作效率！调试时使用：

```bash
curl --version  # 确认版本（推荐7.87.0+）
curl --help all # 查看完整参数
```

## 参考链接：

- [curl - 如何使用 --- curl - How To Use](https://curl.se/docs/manpage.html)
- [curl 的用法指南 - 阮一峰的网络日志](https://www.ruanyifeng.com/blog/2019/09/curl-reference.html)
