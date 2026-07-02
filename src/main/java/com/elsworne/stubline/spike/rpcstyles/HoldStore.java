package com.elsworne.stubline.spike.rpcstyles;

import java.util.Set;
import java.util.TreeSet;

/** Shared seat-hold behavior for all three transports. */
public final class HoldStore {
  private final Set<String> held = new TreeSet<>();

  public synchronized boolean hold(String seat) {
    if (seat == null || seat.isBlank()) throw new IllegalArgumentException("seat required");
    return held.add(seat);
  }

  public synchronized boolean release(String seat) {
    return held.remove(seat);
  }

  public synchronized Set<String> list() {
    return Set.copyOf(held);
  }
}
