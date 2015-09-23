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

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.jetty.server.component.ecm.ServerConnectorFactoryConstants;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.Update;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.IntegerAttribute;
import org.everit.osgi.ecm.annotation.attribute.LongAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.ConnectionFactoryFactory;
import org.everit.osgi.jetty.server.NetworkConnectorFactory;
import org.everit.osgi.jetty.server.ReferencedEndPointsCloseable;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * ECM based configurable component that can set up and register {@link NetworkConnectorFactory}s.
 *
 */
@Component(componentId = ServerConnectorFactoryConstants.FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    localizationBase = "OSGI-INF/metatype/serverConnectorFactory")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({ @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION,
    optional = true) })
@Service
public class ServerConnectorFactoryComponent implements NetworkConnectorFactory {

  private int acceptorPriorityDelta;

  private int acceptQueueSize;

  private boolean closeEndpointsAfterDynamicUpdate;

  private ConnectionFactoryFactory[] connectionFactoryFactories;

  private long idleTimeout = ServerConnectorFactoryConstants.DEFAULT_IDLE_TIMEOUT;

  private boolean inheritChannel;

  private int lingerTime;

  private String name;

  private final WeakHashMap<ServerConnector, Boolean> providedConnectors =
      new WeakHashMap<ServerConnector, Boolean>();

  private boolean reuseAddress;

  private int selectorPriorityDelta;

  private boolean updateConnectionFactories = false;

  @Activate
  public void activate() {
    closeEndpointsAfterDynamicUpdate = false;
    updateConnectionFactories = false;
  }

  private synchronized Set<ServerConnector> activeServerConnectors() {
    Set<ServerConnector> result = null;
    while (result == null) {
      try {
        result = new HashSet<ServerConnector>(providedConnectors.keySet());
      } catch (ConcurrentModificationException e) {
        // TODO probably some warn logging would be nice
      }
    }
    return result;
  }

  @Override
  public ServerConnector createNetworkConnector(final Server server, final String host,
      final int port) {

    ServerConnector result = new ServerConnector(server);

    Collection<ConnectionFactory> connectionFactories = generateConnectionFactories();
    result.setConnectionFactories(connectionFactories);
    result.setDefaultProtocol(connectionFactories.iterator().next().getProtocol());
    result.setAcceptorPriorityDelta(acceptorPriorityDelta);
    result.setAcceptQueueSize(acceptQueueSize);
    result.setIdleTimeout(idleTimeout);
    result.setInheritChannel(inheritChannel);
    result.setName(name);
    result.setReuseAddress(reuseAddress);
    result.setSelectorPriorityDelta(selectorPriorityDelta);
    result.setSoLingerTime(lingerTime);
    result.setHost(host);
    result.setPort(port);
    putIntoProvidedConnectors(result);
    return result;
  }

  private Collection<ConnectionFactory> generateConnectionFactories() {
    int n = connectionFactoryFactories.length;
    ConnectionFactory[] result = new ConnectionFactory[n];
    String nextProtocol = null;
    for (int i = n - 1; i >= 0; i--) {
      ConnectionFactoryFactory connectionFactoryFactory = connectionFactoryFactories[i];

      ConnectionFactory connectionFactory = connectionFactoryFactory
          .createConnectionFactory(nextProtocol);

      result[i] = connectionFactory;
      nextProtocol = connectionFactory.getProtocol();
    }
    return Arrays.asList(result);
  }

  private synchronized void putIntoProvidedConnectors(final ServerConnector result) {
    providedConnectors.put(result, Boolean.TRUE);
  }

  /**
   * Setter that also updates the property on the connector without restarting it.
   */
  @IntegerAttribute(attributeId = ServerConnectorFactoryConstants.PROP_ACCEPTOR_PRIORITY_DELTA,
      defaultValue = 0, dynamic = true, priority = PriorityConstants.PRIORITY_08)
  public synchronized void setAcceptorPriorityDelta(final int acceptorPriorityDelta) {
    this.acceptorPriorityDelta = acceptorPriorityDelta;
    for (ServerConnector serverConnector : activeServerConnectors()) {
      serverConnector.setAcceptorPriorityDelta(acceptorPriorityDelta);
    }

  }

  /**
   * Setter that also updates the property on the connector without restarting it.
   */
  @IntegerAttribute(attributeId = ServerConnectorFactoryConstants.PROP_ACCEPT_QUEUE_SIZE,
      defaultValue = 0, priority = PriorityConstants.PRIORITY_05)
  public synchronized void setAcceptQueueSize(final int acceptQueueSize) {
    this.acceptQueueSize = acceptQueueSize;
  }

  /**
   * Setter that also updates the property on the connector without restarting it.
   */
  @ServiceRef(
      referenceId = ServerConnectorFactoryConstants.SERVICE_REF_CONNECTION_FACTORY_FACTORIES,
      dynamic = true, optional = true, attributePriority = PriorityConstants.PRIORITY_01)
  public synchronized void setConnectionFactoryFactories(
      final ConnectionFactoryFactory[] connectionFactoryFactories) {

    if ((connectionFactoryFactories == null) || (connectionFactoryFactories.length == 0)) {
      this.connectionFactoryFactories =
          new ConnectionFactoryFactory[] { new HttpConnectionFactoryFactoryComponent() };
    } else {
      this.connectionFactoryFactories = connectionFactoryFactories.clone();
    }

    updateConnectionFactories = true;
    closeEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Setter that also updates the property on the connector without restarting it.
   */
  @LongAttribute(attributeId = ServerConnectorFactoryConstants.PROP_IDLE_TIMEOUT,
      defaultValue = ServerConnectorFactoryConstants.DEFAULT_IDLE_TIMEOUT, dynamic = true,
      priority = PriorityConstants.PRIORITY_02)
  public synchronized void setIdleTimeout(final long idleTimeout) {
    this.idleTimeout = idleTimeout;
    for (ServerConnector serverConnector : activeServerConnectors()) {
      serverConnector.setIdleTimeout(idleTimeout);
    }
    closeEndpointsAfterDynamicUpdate = true;

  }

  @BooleanAttribute(attributeId = ServerConnectorFactoryConstants.PROP_INHERIT_CHANNEL,
      defaultValue = ServerConnectorFactoryConstants.DEFAULT_INHERIT_CHANNEL,
      priority = PriorityConstants.PRIORITY_06)
  public void setInheritChannel(final boolean inheritChannel) {
    this.inheritChannel = inheritChannel;
  }

  @IntegerAttribute(attributeId = ServerConnectorFactoryConstants.PROP_LINGER_TIME,
      defaultValue = ServerConnectorFactoryConstants.DEFAULT_LINGER_TIME,
      priority = PriorityConstants.PRIORITY_07)
  public void setLingerTime(final int lingerTime) {
    this.lingerTime = lingerTime;
  }

  @StringAttribute(attributeId = ServerConnectorFactoryConstants.PROP_NAME, optional = true,
      priority = PriorityConstants.PRIORITY_03)
  public void setName(final String name) {
    this.name = name;
  }

  @BooleanAttribute(attributeId = ServerConnectorFactoryConstants.PROP_REUSE_ADDRESS,
      defaultValue = ServerConnectorFactoryConstants.DEFAULT_REUSE_ADDRESS,
      priority = PriorityConstants.PRIORITY_04)
  public void setReuseAddress(final boolean reuseAddress) {
    this.reuseAddress = reuseAddress;
  }

  /**
   * Sets the selectorPriorityDelta on the component and every active connector.
   */
  @IntegerAttribute(attributeId = ServerConnectorFactoryConstants.PROP_SELECTOR_PRIORITY_DELTA,
      defaultValue = ServerConnectorFactoryConstants.DEFAULT_SELECTOR_PRIORITY_DELTA,
      dynamic = true, priority = PriorityConstants.PRIORITY_09)
  public synchronized void setSelectorPriorityDelta(final int selectorPriorityDelta) {
    this.selectorPriorityDelta = selectorPriorityDelta;
    for (ServerConnector serverConnector : activeServerConnectors()) {
      serverConnector.setSelectorPriorityDelta(selectorPriorityDelta);
    }
  }

  /**
   * Updates all connection factories if necessary and closes all endpoints if necessary.
   */
  @Update
  public void update() {
    if (closeEndpointsAfterDynamicUpdate) {
      for (ServerConnector serverConnector : activeServerConnectors()) {
        Collection<ConnectionFactory> previousConnectionFactories = new HashSet<ConnectionFactory>(
            serverConnector.getConnectionFactories());

        if (updateConnectionFactories) {
          Collection<ConnectionFactory> connectionFactories = generateConnectionFactories();
          serverConnector.setConnectionFactories(connectionFactories);
          serverConnector.setDefaultProtocol(connectionFactories.iterator().next().getProtocol());
        }

        // Closing all endpoints
        for (ConnectionFactory connectionFactory : previousConnectionFactories) {

          if (connectionFactory instanceof ReferencedEndPointsCloseable) {
            ((ReferencedEndPointsCloseable) connectionFactory).closeReferencedEndpoints();
          }
        }
      }
    }

    closeEndpointsAfterDynamicUpdate = false;
    updateConnectionFactories = false;
  }
}
