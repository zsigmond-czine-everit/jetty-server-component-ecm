/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jetty.server.component.ecm.internal;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.everit.osgi.jetty.server.ReferencedEndPointsCloseable;

/**
 * {@link SslConnectionFactory} that implements {@link ReferencedEndPointsCloseable}.
 */
public class CustomSslConnectionFactory extends SslConnectionFactory implements
    ReferencedEndPointsCloseable {

  private final WeakHashMap<EndPoint, Boolean> referencedEndPoints = new WeakHashMap<>();

  public CustomSslConnectionFactory(final SslContextFactory factory, final String nextProtocol) {
    super(factory, nextProtocol);
  }

  private synchronized Set<EndPoint> cloneReferencedEndPoints() {
    Set<EndPoint> result = null;
    while (result == null) {
      try {
        result = new HashSet<EndPoint>(referencedEndPoints.keySet());
      } catch (ConcurrentModificationException e) {
        // TODO probably some warn logging would be nice
      }
    }
    return result;
  }

  @Override
  public void closeReferencedEndpoints() {
    Set<EndPoint> endPoints = cloneReferencedEndPoints();
    for (EndPoint endPoint : endPoints) {
      endPoint.close();
    }
  }

  @Override
  public Connection newConnection(final Connector connector, final EndPoint endPoint) {
    referencedEndPoints.put(endPoint, Boolean.TRUE);
    return super.newConnection(connector, endPoint);
  }
}
