package com.elsworne.stubline.spike.rpcstyles;

import static org.junit.jupiter.api.Assertions.*;

import com.elsworne.stubline.spike.rpcstyles.proto.Empty;
import com.elsworne.stubline.spike.rpcstyles.proto.HoldsGrpc;
import com.elsworne.stubline.spike.rpcstyles.proto.SeatRequest;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RpcStylesTest {
  @Test
  void rmi() throws Exception {
    var registry = LocateRegistry.createRegistry(0);
    var service = new RmiHolds(new HoldStore());
    try {
      registry.rebind("Holds", service);
      var remote = (RemoteHolds) registry.lookup("Holds");
      assertTrue(remote.hold("A1"));
      assertFalse(remote.hold("A1"));
      assertEquals(1, remote.list().size());
      assertTrue(remote.release("A1"));
    } finally {
      UnicastRemoteObject.unexportObject(service, true);
      UnicastRemoteObject.unexportObject(registry, true);
    }
  }

  @Test
  void grpc() throws Exception {
    var server =
        NettyServerBuilder.forPort(0).addService(new GrpcHolds(new HoldStore())).build().start();
    var channel =
        NettyChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext().build();
    try {
      var stub = HoldsGrpc.newBlockingStub(channel).withDeadlineAfter(2, TimeUnit.SECONDS);
      var seat = SeatRequest.newBuilder().setSeat("A1").build();
      assertTrue(stub.hold(seat).getChanged());
      assertFalse(stub.hold(seat).getChanged());
      assertEquals(1, stub.list(Empty.getDefaultInstance()).getSeatsCount());
      assertTrue(stub.release(seat).getChanged());
    } finally {
      channel.shutdownNow();
      server.shutdownNow();
    }
  }

  @Test
  void rest() throws Exception {
    try (var server = new RestHolds(0, new HoldStore());
        var client = HttpClient.newHttpClient()) {
      server.start();
      var uri = URI.create("http://localhost:" + server.port() + "/holds");
      var hold =
          HttpRequest.newBuilder(uri)
              .POST(HttpRequest.BodyPublishers.ofString("{\"seat\":\"A1\"}"))
              .build();
      assertEquals(200, client.send(hold, HttpResponse.BodyHandlers.ofString()).statusCode());
      assertEquals(409, client.send(hold, HttpResponse.BodyHandlers.ofString()).statusCode());
      assertTrue(
          client
              .send(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofString())
              .body()
              .contains("A1"));
      assertEquals(
          200,
          client
              .send(
                  HttpRequest.newBuilder(uri)
                      .method("DELETE", HttpRequest.BodyPublishers.ofString("{\"seat\":\"A1\"}"))
                      .build(),
                  HttpResponse.BodyHandlers.ofString())
              .statusCode());
    }
  }
}
