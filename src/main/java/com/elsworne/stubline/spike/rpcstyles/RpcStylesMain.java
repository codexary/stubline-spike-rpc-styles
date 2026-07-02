package com.elsworne.stubline.spike.rpcstyles;

import com.elsworne.stubline.spike.rpcstyles.proto.Empty;
import com.elsworne.stubline.spike.rpcstyles.proto.HoldsGrpc;
import com.elsworne.stubline.spike.rpcstyles.proto.SeatRequest;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Runs servers and sample clients for all three styles. */
public final class RpcStylesMain {
  private RpcStylesMain() {}

  public static void main(String[] args) throws Exception {
    if (args.length < 3)
      throw new IllegalArgumentException("server|client rmi|grpc|rest port [seat]");
    String action = args[0], style = args[1];
    int port = Integer.parseInt(args[2]);
    String seat = args.length > 3 ? args[3] : "A1";
    if (action.equals("server")) serve(style, port);
    else if (action.equals("client")) client(style, port, seat);
    else throw new IllegalArgumentException("server or client required");
  }

  private static void serve(String style, int port) throws Exception {
    var store = new HoldStore();
    switch (style) {
      case "rmi" -> {
        var registry = LocateRegistry.createRegistry(port);
        registry.rebind("Holds", new RmiHolds(store));
        System.out.println("RMI ready " + port);
        new CountDownLatch(1).await();
      }
      case "grpc" -> {
        var server =
            NettyServerBuilder.forPort(port).addService(new GrpcHolds(store)).build().start();
        System.out.println("gRPC ready " + port);
        server.awaitTermination();
      }
      case "rest" -> {
        try (var server = new RestHolds(port, store)) {
          server.start();
          System.out.println("REST ready " + port);
          new CountDownLatch(1).await();
        }
      }
      default -> throw new IllegalArgumentException("unknown style");
    }
  }

  private static void client(String style, int port, String seat) throws Exception {
    switch (style) {
      case "rmi" -> {
        try {
          var remote = (RemoteHolds) LocateRegistry.getRegistry("127.0.0.1", port).lookup("Holds");
          System.out.println("hold=" + remote.hold(seat));
          System.out.println("holds=" + remote.list());
          System.out.println("release=" + remote.release(seat));
        } catch (RemoteException | NotBoundException e) {
          System.err.println("RMI failed: " + e);
          throw e;
        }
      }
      case "grpc" -> {
        var channel = NettyChannelBuilder.forAddress("127.0.0.1", port).usePlaintext().build();
        try {
          var stub = HoldsGrpc.newBlockingStub(channel).withDeadlineAfter(2, TimeUnit.SECONDS);
          var request = SeatRequest.newBuilder().setSeat(seat).build();
          System.out.println("hold=" + stub.hold(request).getChanged());
          System.out.println("holds=" + stub.list(Empty.getDefaultInstance()).getSeatsList());
          System.out.println("release=" + stub.release(request).getChanged());
        } finally {
          channel.shutdownNow();
        }
      }
      case "rest" -> {
        try (var http = HttpClient.newHttpClient()) {
          var uri = URI.create("http://127.0.0.1:" + port + "/holds");
          var body = "{\"seat\":\"" + seat + "\"}";
          System.out.println(
              "hold="
                  + http.send(
                          HttpRequest.newBuilder(uri)
                              .timeout(Duration.ofSeconds(2))
                              .POST(HttpRequest.BodyPublishers.ofString(body))
                              .build(),
                          HttpResponse.BodyHandlers.ofString())
                      .body());
          System.out.println(
              "holds="
                  + http.send(
                          HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(2)).GET().build(),
                          HttpResponse.BodyHandlers.ofString())
                      .body());
          System.out.println(
              "release="
                  + http.send(
                          HttpRequest.newBuilder(uri)
                              .timeout(Duration.ofSeconds(2))
                              .method("DELETE", HttpRequest.BodyPublishers.ofString(body))
                              .build(),
                          HttpResponse.BodyHandlers.ofString())
                      .body());
        }
      }
      default -> throw new IllegalArgumentException("unknown style");
    }
  }
}
