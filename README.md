English | [简体中文](README.zh-CN.md)

# Three remote invocation styles

The same synchronized seat-hold store supports `hold`, `release`, and `list` over RMI, gRPC, and REST. Build with `./mvnw -q verify`. In one terminal run `./mvnw -q exec:java -Dexec.mainClass=com.elsworne.stubline.spike.rpcstyles.RpcStylesMain -Dexec.args='server rmi 1099'`; in a second run the same command with `-Dexec.args='client rmi 1099 A1'`. Replace `rmi 1099` with `grpc 9090` or `rest 8080` for the other styles. The server prints `RMI ready 1099` (or its equivalent); the client prints a successful hold, a list containing `A1`, and a successful release. A subsequent client can hold `A1` again. Stop the server with Ctrl-C.

| Concern | RMI | gRPC | REST/JSON |
|---|---|---|---|
| Contract | Java `Remote` interface | `.proto` service and messages | HTTP methods, paths, JSON convention or OpenAPI |
| Language coupling | Java to Java | Cross-language generated code | Cross-language HTTP and JSON |
| Error model | `RemoteException`, registry lookup failure | Typed gRPC status codes | HTTP status and response body |
| Timeouts/deadlines | Socket options, no per-call deadline in interface | Per-call deadline on generated stub | `HttpRequest.timeout` |
| Streaming | Remote calls, no built-in stream | Client, server, bidirectional streams | Response streaming/SSE, usually separate design |
| Tooling | JDK registry and stubs | `protoc`, plugin, generated stub, grpcurl | JDK HTTP client/server, curl, OpenAPI tools |
| Choose today | Java-only legacy integration | Internal typed service APIs and streaming | Public or browser-facing APIs and easy inspection |

RMI requires a registry and exported remote object; its client handles both `RemoteException` and `NotBoundException`. gRPC always sets a two-second deadline. REST uses the JDK `HttpServer` and `HttpClient` with JSON bodies. All three transport tests run in the default verification.

## License

This repository is licensed under the [MIT License](LICENSE).

## About this repository

This repository contains a focused Stubline experiment. The README documents its commands and observed results.

The code and documents in this repository were written with the help of AI tools; every recorded output comes from actually running them.

## Findings

The experiment results and their effect on the backend decision are recorded in [FINDINGS.md](FINDINGS.md).
