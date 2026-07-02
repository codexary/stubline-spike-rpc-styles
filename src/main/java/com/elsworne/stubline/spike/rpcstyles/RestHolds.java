package com.elsworne.stubline.spike.rpcstyles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/** JSON HTTP adapter for the hold store. */
public final class RestHolds implements AutoCloseable {
  private final HttpServer server;

  public RestHolds(int port, HoldStore store) throws IOException {
    var json = new ObjectMapper();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
    server.createContext(
        "/holds",
        exchange -> {
          int status = 200;
          byte[] response;
          try {
            if (exchange.getRequestMethod().equals("GET"))
              response = json.writeValueAsBytes(store.list());
            else {
              String seat = json.readTree(exchange.getRequestBody()).path("seat").asText();
              boolean changed =
                  switch (exchange.getRequestMethod()) {
                    case "POST" -> store.hold(seat);
                    case "DELETE" -> store.release(seat);
                    default -> throw new IllegalArgumentException("method");
                  };
              if (!changed) status = 409;
              response = json.writeValueAsBytes(Map.of("changed", changed));
            }
          } catch (IllegalArgumentException e) {
            status = 400;
            response = "{\"error\":\"bad request\"}".getBytes();
          }
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, response.length);
          try (var out = exchange.getResponseBody()) {
            out.write(response);
          }
        });
  }

  public void start() {
    server.start();
  }

  public int port() {
    return server.getAddress().getPort();
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
