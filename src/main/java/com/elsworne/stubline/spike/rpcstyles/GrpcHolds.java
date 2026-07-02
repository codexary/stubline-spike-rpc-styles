package com.elsworne.stubline.spike.rpcstyles;

import com.elsworne.stubline.spike.rpcstyles.proto.Empty;
import com.elsworne.stubline.spike.rpcstyles.proto.HoldList;
import com.elsworne.stubline.spike.rpcstyles.proto.HoldReply;
import com.elsworne.stubline.spike.rpcstyles.proto.HoldsGrpc;
import com.elsworne.stubline.spike.rpcstyles.proto.SeatRequest;
import io.grpc.stub.StreamObserver;

/** Generated gRPC contract adapter. */
public final class GrpcHolds extends HoldsGrpc.HoldsImplBase {
  private final HoldStore store;

  public GrpcHolds(HoldStore store) {
    this.store = store;
  }

  @Override
  public void hold(SeatRequest request, StreamObserver<HoldReply> observer) {
    observer.onNext(HoldReply.newBuilder().setChanged(store.hold(request.getSeat())).build());
    observer.onCompleted();
  }

  @Override
  public void release(SeatRequest request, StreamObserver<HoldReply> observer) {
    observer.onNext(HoldReply.newBuilder().setChanged(store.release(request.getSeat())).build());
    observer.onCompleted();
  }

  @Override
  public void list(Empty request, StreamObserver<HoldList> observer) {
    observer.onNext(HoldList.newBuilder().addAllSeats(store.list()).build());
    observer.onCompleted();
  }
}
