package com.elsworne.stubline.spike.rpcstyles;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

/** RMI adapter exported by UnicastRemoteObject. */
public final class RmiHolds extends UnicastRemoteObject implements RemoteHolds {
  private final HoldStore store;

  public RmiHolds(HoldStore store) throws RemoteException {
    this.store = store;
  }

  public boolean hold(String seat) throws RemoteException {
    return store.hold(seat);
  }

  public boolean release(String seat) throws RemoteException {
    return store.release(seat);
  }

  public Set<String> list() throws RemoteException {
    return store.list();
  }
}
