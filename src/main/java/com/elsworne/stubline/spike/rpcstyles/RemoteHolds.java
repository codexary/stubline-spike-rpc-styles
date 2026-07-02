package com.elsworne.stubline.spike.rpcstyles;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/** RMI interface shared by registry clients and servers. */
public interface RemoteHolds extends Remote {
  boolean hold(String seat) throws RemoteException;

  boolean release(String seat) throws RemoteException;

  Set<String> list() throws RemoteException;
}
