[English](FINDINGS.md) | 简体中文

# 实验结论

## 观察

同一个同步座位占用存储通过 RMI、gRPC 和 REST 提供 `hold`、`release`、`list`。默认测试覆盖了三种传输方式的正常流程。gRPC 设置两秒截止时间；RMI 处理远程调用和注册表错误；REST 使用 JDK HTTP 客户端与服务器。[EVIDENCE.md](EVIDENCE.md) 记录了使用 Temurin JDK 21.0.11 成功运行 `./mvnw -q verify`，并明确说明没有验证回复丢失后的结果。

## 对后端的影响

这些比较结果支持用 gRPC 定义内部库存服务的强类型接口，并为每次调用设置截止时间。但它们没有证明回复丢失后占座结果如何确认；幂等或核对策略仍需单独决定。参见[后端 ADR 0009：Extract inventory with gRPC](https://github.com/codexary/stubline-backend/blob/main/docs/adr/0009-extract-inventory-with-grpc.md)。
