[English](README.md) | 简体中文

# 三种远程调用方式

同一个同步座位占用存储通过 RMI、gRPC、REST 提供 `hold`、`release`、`list`。用 `./mvnw -q verify` 构建。在一个终端运行 `./mvnw -q exec:java -Dexec.mainClass=com.elsworne.stubline.spike.rpcstyles.RpcStylesMain -Dexec.args='server rmi 1099'`；在另一终端运行相同命令，改为 `-Dexec.args='client rmi 1099 A1'`。其他方式把 `rmi 1099` 换成 `grpc 9090` 或 `rest 8080`。服务端打印 `RMI ready 1099`（或对应文字）；客户端打印成功占用、含 `A1` 的列表、成功释放。再次运行客户端仍可占用 `A1`。用 Ctrl-C 停止服务端。

| 对比项 | RMI | gRPC | REST/JSON |
|---|---|---|---|
| 契约 | Java `Remote` 接口 | `.proto` 服务与消息 | HTTP 方法、路径、JSON 约定或 OpenAPI |
| 语言耦合 | Java 对 Java | 跨语言生成代码 | 跨语言 HTTP 和 JSON |
| 错误模型 | `RemoteException`、registry 查询失败 | 类型化 gRPC 状态码 | HTTP 状态与响应体 |
| 超时与 deadline | socket 配置，接口无逐调用 deadline | 生成的 stub 逐调用 deadline | `HttpRequest.timeout` |
| 流式处理 | 远程调用，无内建流 | 客户端、服务端、双向流 | 响应流/SSE，通常另行设计 |
| 工具 | JDK registry 和 stub | `protoc`、插件、生成 stub、grpcurl | JDK HTTP client/server、curl、OpenAPI 工具 |
| 今日选择 | 仅 Java 的旧系统集成 | 内部强类型服务 API 与流式处理 | 公共或浏览器 API，便于检查 |

RMI 需要 registry 和导出的远程对象，客户端处理 `RemoteException` 和 `NotBoundException`。gRPC 总是设置两秒 deadline。REST 使用 JDK `HttpServer` 和 `HttpClient`，消息为 JSON。默认验证运行三种传输的测试。

## 许可证

本仓库采用 [MIT License](LICENSE)。

## 关于本仓库

本仓库包含一项针对 Stubline 的独立实验。README 说明运行命令与观察结果。

本仓库的代码与文档借助 AI 工具编写；记录的输出都来自实际运行。
