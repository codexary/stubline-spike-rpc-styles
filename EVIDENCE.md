# Verification record / 验证记录

On 2026-10-31 at the tested commit with Temurin JDK 21.0.11, run `./mvnw -q verify` from the repository root. The command completed with `EXIT_STATUS=0`. The default suite exercises successful RMI, gRPC and REST flows. It does not establish the outcome of a hold after a lost reply. / 在测试过的提交中使用 Temurin JDK 21.0.11 执行上述命令，退出码为 0；默认测试覆盖三种协议的正常流程，但不证明回复丢失后 hold 的结果。
