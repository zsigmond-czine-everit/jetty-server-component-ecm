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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.everit.jetty.server.component.ecm.JettyServerConstants;
import org.everit.jetty.server.component.ecm.JettyServerException;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Deactivate;
import org.everit.osgi.ecm.annotation.ReferenceConfigurationType;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.component.ComponentContext;
import org.everit.osgi.ecm.component.ConfigurationException;
import org.everit.osgi.ecm.component.ServiceHolder;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.NetworkConnectorFactory;
import org.everit.osgi.jetty.server.ServletContextHandlerFactory;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * ECM based configurable component that can start one or more Jetty {@link Server}s.
 */
@Component(componentId = JettyServerConstants.SERVICE_FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    label = "Everit Jetty Server",
    description = "Configurable component that can start Jetty server instances. In case a Jetty "
        + "server is started, the server instance is registered as an OSGi service.")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION, optional = true,
        label = "Service description",
        description = "Optional description for the instantiated Jetty server.") })
public class JettyServerComponent {

  /**
   * Helper class to identify connector references with their settings in hash based collections.
   */
  private static class ConnectorFactoryKey {

    public final String connectorId;

    public final String host;

    public final int port;

    public final ServiceReference<NetworkConnectorFactory> serviceReference;

    public ConnectorFactoryKey(final ServiceHolder<NetworkConnectorFactory> serviceHolder,
        final String host, final int port) {
      serviceReference = serviceHolder.getReference();
      connectorId = serviceHolder.getReferenceId();
      this.host = host;
      this.port = port;
    }

    @Override
    @Generated("eclipse")
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ConnectorFactoryKey other = (ConnectorFactoryKey) obj;
      if (connectorId == null) {
        if (other.connectorId != null) {
          return false;
        }
      } else if (!connectorId.equals(other.connectorId)) {
        return false;
      }
      if (host == null) {
        if (other.host != null) {
          return false;
        }
      } else if (!host.equals(other.host)) {
        return false;
      }
      if (port != other.port) {
        return false;
      }
      if (serviceReference == null) {
        if (other.serviceReference != null) {
          return false;
        }
      } else if (!serviceReference.equals(other.serviceReference)) {
        return false;
      }
      return true;
    }

    @Override
    @Generated("eclipse")
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + ((connectorId == null) ? 0 : connectorId.hashCode());
      result = (prime * result) + ((host == null) ? 0 : host.hashCode());
      result = (prime * result) + port;
      result = (prime * result) + ((serviceReference == null) ? 0 : serviceReference.hashCode());
      return result;
    }

  }

  /**
   * Container class that holds a context handler and the original context path that was used to
   * create or update the handler.
   *
   */
  private static class ContextWithPath {

    public final String contextPath;

    public final ServletContextHandler handler;

    public ContextWithPath(final ServletContextHandler handler,
        final String contextPath) {
      this.handler = handler;
      this.contextPath = contextPath;
    }

  }

  /**
   * Helper class to identify contexts with their settings in hash based collections.
   */
  private static class ServletContextFactoryKey {

    public final String contextId;

    public final ServiceReference<ServletContextHandlerFactory> serviceReference;

    public ServletContextFactoryKey(
        final ServiceHolder<ServletContextHandlerFactory> serviceHolder) {
      serviceReference = serviceHolder.getReference();
      contextId = serviceHolder.getReferenceId();
    }

    @Override
    @Generated("eclipse")
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ServletContextFactoryKey other = (ServletContextFactoryKey) obj;
      if (contextId == null) {
        if (other.contextId != null) {
          return false;
        }
      } else if (!contextId.equals(other.contextId)) {
        return false;
      }
      if (serviceReference == null) {
        if (other.serviceReference != null) {
          return false;
        }
      } else if (!serviceReference.equals(other.serviceReference)) {
        return false;
      }
      return true;
    }

    @Override
    @Generated("eclipse")
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + ((contextId == null) ? 0 : contextId.hashCode());
      result = (prime * result) + ((serviceReference == null) ? 0 : serviceReference.hashCode());
      return result;
    }

  }

  private CustomContextHandlerCollection contextHandlerCollection;

  private ServiceHolder<NetworkConnectorFactory>[] networkConnectorFactories;

  private final HashMap<ConnectorFactoryKey, NetworkConnector> registeredConnectors =
      new HashMap<>();

  private final HashMap<ServletContextFactoryKey, ContextWithPath> registeredServletContexts =
      new HashMap<>();

  private Server server;

  private ServiceRegistration<Server> serviceRegistration;

  private ServiceHolder<ServletContextHandlerFactory>[] servletContextHandlerFactories;

  /**
   * Activate method of the component that sets up and starts a server.
   */
  @Activate
  public void activate(final ComponentContext<JettyServerComponent> componentContext) {
    server = new Server();
    contextHandlerCollection = new CustomContextHandlerCollection();

    server.setHandler(contextHandlerCollection);

    updateConnectorFactoriesOnServer();

    updateServletContextHandlerFactoriesOnServer();

    Dictionary<String, Object> serviceProps = new Hashtable<String, Object>(
        componentContext.getProperties());

    try {
      server.start();
    } catch (Exception e) {
      fail(e);
      return;
    }
    serviceRegistration = componentContext.registerService(Server.class, server, serviceProps);
  }

  private void addNewConnectors(
      final Map<ConnectorFactoryKey, NetworkConnectorFactory> newConnectors) {
    Set<Entry<ConnectorFactoryKey, NetworkConnectorFactory>> entrySet = newConnectors.entrySet();
    for (Entry<ConnectorFactoryKey, NetworkConnectorFactory> entry : entrySet) {
      NetworkConnectorFactory factory = entry.getValue();
      ConnectorFactoryKey factoryParams = entry.getKey();

      NetworkConnector connector = factory.createNetworkConnector(server, factoryParams.host,
          factoryParams.port);

      server.addConnector(connector);
      if (server.isStarted() && !connector.isStarted()) {
        try {
          connector.start();
          server.manage(connector);
        } catch (Exception e) {
          fail(e);
          return;
        }
      }
      registeredConnectors.put(factoryParams, connector);
    }
  }

  /**
   * Deactivate method that stops the server if it is running.
   */
  @Deactivate
  public void deactivate() {
    if (serviceRegistration != null) {
      serviceRegistration.unregister();
    }

    if ((server != null) && !server.isStopped()) {
      try {
        server.stop();
        server.destroy();
      } catch (Exception e) {
        throw new JettyServerException(e);
      }
    }

  }

  private void deleteConnectors(
      final HashMap<ConnectorFactoryKey, NetworkConnector> connectorsToDelete) {
    Set<Entry<ConnectorFactoryKey, NetworkConnector>> connectorToDeleteSet = connectorsToDelete
        .entrySet();
    for (Entry<ConnectorFactoryKey, NetworkConnector> connectorToDelete : connectorToDeleteSet) {
      registeredConnectors.remove(connectorToDelete.getKey());
      server.removeConnector(connectorToDelete.getValue());
    }
  }

  private void fail(final Throwable e) {
    try {
      deactivate();
    } catch (RuntimeException re) {
      e.addSuppressed(re);
    }

    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    }
    throw new JettyServerException(e);
  }

  private String resolveContextPath(final ServiceHolder<ServletContextHandlerFactory> holder) {
    Map<String, Object> attributes = holder.getAttributes();
    Object contextPath = attributes.get(JettyServerConstants.CONTEXT_CLAUSE_ATTR_CONTEXTPATH);

    if (contextPath == null) {
      throw new ConfigurationException("'" + JettyServerConstants.CONTEXT_CLAUSE_ATTR_CONTEXTPATH
          + "' attribute must be provided in clause of ServletContextHandlerFactory");
    }
    return String.valueOf(contextPath);
  }

  private String resolveHostFromAttributes(final Map<String, Object> attributes) {
    Object hostValue = attributes.get(JettyServerConstants.CONNECTOR_REF_CLAUSE_ATTR_HOST);
    if (hostValue == null) {
      return null;
    }
    return String.valueOf(hostValue);
  }

  private int resolvePortFromAttributes(final String referenceId,
      final Map<String, Object> attributes) {

    Object portValue = attributes.get(JettyServerConstants.CONNECTOR_REF_CLAUSE_ATTR_PORT);
    if (portValue == null) {
      return 0;
    }

    try {
      return Integer.parseInt(String.valueOf(portValue));
    } catch (NumberFormatException e) {
      throw new ConfigurationException("Invalid value for connector port of reference: "
          + referenceId, e);
    }
  }

  private void setAndManageNewHandlers(final ServletContextHandler[] newHandlers) {
    contextHandlerCollection.setHandlers(newHandlers);
    for (ServletContextHandler newHandler : newHandlers) {
      if (!newHandler.isStarted() && server.isStarted()) {
        try {
          newHandler.start();
          contextHandlerCollection.manage(newHandler);
        } catch (Exception e) {
          throw new JettyServerException(e);
        }
      }
    }
  }

  @ServiceRef(referenceId = JettyServerConstants.ATTR_NETWORK_CONNECTOR_FACTORIES,
      configurationType = ReferenceConfigurationType.CLAUSE, optional = false, dynamic = true,
      attributePriority = PriorityConstants.PRIORITY_01,
      label = "NetworkConnector Factories (clause)",
      description = "Zero or more clauses to install Network Connectors based on their factory "
          + "services. Supported attributes: host, port.")
  public void setNetworkConnectorFactories(
      final ServiceHolder<NetworkConnectorFactory>[] networkConnectorFactories) {
    updateConnectorFactories(networkConnectorFactories);
  }

  @ServiceRef(referenceId = JettyServerConstants.ATTR_SERVLET_CONTEXT_HANDLER_FACTORIES,
      configurationType = ReferenceConfigurationType.CLAUSE, dynamic = true,
      attributePriority = PriorityConstants.PRIORITY_02,
      label = "ServletContextHandler Factories (clause)",
      description = "Zero or more clauses to install Servlet Contexts based on their factory "
          + "services. Supported attributes: contextPath")
  public void setServletContextHandlerFactories(
      final ServiceHolder<ServletContextHandlerFactory>[] servletContextHandlerFactories) {
    updateServletContextAndHandleFailure(servletContextHandlerFactories);
  }

  private synchronized void updateConnectorFactories(
      final ServiceHolder<NetworkConnectorFactory>[] pNetworkConnectorFactories) {
    networkConnectorFactories = pNetworkConnectorFactories;
    if (server != null) {
      updateConnectorFactoriesOnServer();
    }
  }

  private void updateConnectorFactoriesOnServer() {
    @SuppressWarnings("unchecked")
    HashMap<ConnectorFactoryKey, NetworkConnector> connectorsToDelete =
        (HashMap<ConnectorFactoryKey, NetworkConnector>) registeredConnectors.clone();

    Map<ConnectorFactoryKey, NetworkConnectorFactory> newConnectors = new HashMap<>();

    for (ServiceHolder<NetworkConnectorFactory> serviceHolder : networkConnectorFactories) {
      NetworkConnectorFactory connectorFactory = serviceHolder.getService();
      Map<String, Object> attributes = serviceHolder.getAttributes();
      String host = resolveHostFromAttributes(attributes);
      int port = resolvePortFromAttributes(serviceHolder.getReferenceId(), attributes);

      ConnectorFactoryKey factoryKey = new ConnectorFactoryKey(serviceHolder, host, port);

      if (connectorsToDelete.containsKey(factoryKey)) {
        connectorsToDelete.remove(factoryKey);
      } else {
        newConnectors.put(factoryKey, connectorFactory);
      }

    }

    deleteConnectors(connectorsToDelete);

    addNewConnectors(newConnectors);

  }

  private void updateServletContextAndHandleFailure(
      final ServiceHolder<ServletContextHandlerFactory>[] pServletContextHandlerFactories) {

    servletContextHandlerFactories = pServletContextHandlerFactories;
    if (server != null) {
      updateServletContextHandlerFactoriesOnServer();
    }
  }

  private void updateServletContextHandlerFactoriesOnServer() {
    Set<ServletContextFactoryKey> unregisteredContexts = new HashSet<>(
        registeredServletContexts.keySet());

    ServletContextHandler[] newHandlers =
        new ServletContextHandler[servletContextHandlerFactories.length];

    contextHandlerCollection.setMapContextsCallIgnored(true);
    for (int i = 0; i < servletContextHandlerFactories.length; i++) {
      ServiceHolder<ServletContextHandlerFactory> holder = servletContextHandlerFactories[i];
      String contextPath = resolveContextPath(holder);
      ServletContextFactoryKey factoryKey = new ServletContextFactoryKey(holder);

      ContextWithPath contextWithPath = registeredServletContexts.get(factoryKey);
      if (contextWithPath != null) {
        if (!contextPath.equals(contextWithPath.contextPath)) {
          contextWithPath.handler.setContextPath(contextPath);
          registeredServletContexts.put(factoryKey, new ContextWithPath(contextWithPath.handler,
              contextPath));
        }
        unregisteredContexts.remove(factoryKey);
        newHandlers[i] = contextWithPath.handler;
      } else {
        ServletContextHandler handler = holder.getService().createHandler(contextHandlerCollection,
            contextPath);

        newHandlers[i] = handler;
        registeredServletContexts.put(factoryKey, new ContextWithPath(handler, contextPath));
      }
    }

    // Remove handlers that are not in the new configuration
    for (ServletContextFactoryKey key : unregisteredContexts) {
      registeredServletContexts.remove(key);
    }

    // Set mapContext function back as setHandlers will call it
    contextHandlerCollection.setMapContextsCallIgnored(false);
    setAndManageNewHandlers(newHandlers);
  }
}
