English | [简体中文](FINDINGS.zh-CN.md)

# Findings

## What we observed

The same synchronized seat-hold store exposes `hold`, `release` and `list` through RMI, gRPC and REST. The default tests cover successful flows for all three transports. gRPC uses a two-second deadline; RMI handles remote and registry errors; REST uses the JDK HTTP client and server. [EVIDENCE.md](EVIDENCE.md) records a successful `./mvnw -q verify` run with Temurin JDK 21.0.11 and explicitly excludes lost-reply outcomes.

## Backend decision

The comparison supports gRPC for an internal typed inventory contract with per-call deadlines. It does not show how any transport resolves an ambiguous hold after a lost reply; that requires a separate idempotency or reconciliation decision. See [backend ADR 0009 — Extract inventory with gRPC](https://github.com/codexary/stubline-backend/blob/main/docs/adr/0009-extract-inventory-with-grpc.md).
