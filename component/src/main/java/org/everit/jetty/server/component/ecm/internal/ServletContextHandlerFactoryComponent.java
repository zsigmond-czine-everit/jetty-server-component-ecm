/*
 * Copyright (C) 2015 Everit Kft. (http://www.everit.org)
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

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.jetty.server.component.ecm.ServletContextHandlerFactoryConstants;
import org.everit.jetty.server.component.ecm.internal.servletcontext.FilterHolderManager;
import org.everit.jetty.server.component.ecm.internal.servletcontext.FilterMappingKey;
import org.everit.jetty.server.component.ecm.internal.servletcontext.FilterMappingManager;
import org.everit.jetty.server.component.ecm.internal.servletcontext.HolderKey;
import org.everit.jetty.server.component.ecm.internal.servletcontext.ServletHolderManager;
import org.everit.jetty.server.component.ecm.internal.servletcontext.ServletMappingKey;
import org.everit.jetty.server.component.ecm.internal.servletcontext.ServletMappingManager;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.ReferenceConfigurationType;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.ThreeStateBoolean;
import org.everit.osgi.ecm.annotation.Update;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.IntegerAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.component.ServiceHolder;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.ErrorHandlerFactory;
import org.everit.osgi.jetty.server.SecurityHandlerFactory;
import org.everit.osgi.jetty.server.ServletContextHandlerFactory;
import org.everit.osgi.jetty.server.SessionHandlerFactory;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * ECM based configurable component that registers one or more instantiated
 * {@link ServletContextHandler} OSGi services. The component handles filter and servlet reference
 * changes dynamically.
 */
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@Component(componentId = ServletContextHandlerFactoryConstants.SERVICE_FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    label = "Everit Jetty ServletContextHandler Factory",
    description = "ECM based component that can register one or more ServletContextHandlerFactory "
        + "instances.")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION, optional = true,
        label = "Service description",
        description = "Optional description for ServletContextHandlerFactory service.") })
@Service(ServletContextHandlerFactory.class)
public class ServletContextHandlerFactoryComponent implements ServletContextHandlerFactory {

  private final WeakHashMap<ServletContextHandler, Boolean> activeServletContextHandlers =
      new WeakHashMap<>();

  private ServletContextAttributeListener[] contextAttributeListeners;

  private ServletContextListener[] contextListeners;

  private ErrorHandlerFactory errorHandlerFactory;

  private final FilterHolderManager filterHolderManager = new FilterHolderManager();

  private HolderKey<Filter>[] filterKeys;

  private FilterMappingKey[] filterMappingKeys;

  private final FilterMappingManager filterMappingManager = new FilterMappingManager();

  private int maxFormContentSize;

  private int maxFormKeys;

  private MimeTypes mimeTypes;

  private ServletRequestAttributeListener[] requestAttributeListeners;

  private ServletRequestListener[] requestListeners;

  private boolean security = false;

  private SecurityHandlerFactory securityHandlerFactory;

  private final ServletHolderManager servletHolderManager = new ServletHolderManager();

  private HolderKey<Servlet>[] servletKeys;

  private ServletMappingKey[] servletMappingKeys;

  private final ServletMappingManager servletMappingManager = new ServletMappingManager();

  private SessionHandlerFactory sessionHandlerFactory;

  private boolean sessions = true;

  private String[] virtualHosts;

  @Activate
  public void activate() {
    servletHolderManager.updatePrviousKeys(servletKeys);
    filterHolderManager.updatePrviousKeys(filterKeys);
  }

  private void addListenersToHandler(final ServletContextHandler servletContextHandler) {
    for (ServletContextListener contextListener : contextListeners) {
      servletContextHandler.addEventListener(contextListener);
    }

    for (ServletContextAttributeListener contextAttributeListener : contextAttributeListeners) {
      servletContextHandler.addEventListener(contextAttributeListener);
    }

    for (ServletRequestListener requestListener : requestListeners) {
      servletContextHandler.addEventListener(requestListener);
    }

    for (ServletRequestAttributeListener requestAttributeListener : requestAttributeListeners) {
      servletContextHandler.addEventListener(requestAttributeListener);
    }

  }

  private Set<ServletContextHandler> cloneActiveServletContextHandlerSet() {
    Set<ServletContextHandler> result = null;
    while (result == null) {
      Set<ServletContextHandler> keySet = activeServletContextHandlers.keySet();
      try {
        result = new HashSet<ServletContextHandler>(keySet);
      } catch (ConcurrentModificationException e) {
        // Do nothing
      }
    }
    return result;
  }

  @Override
  public synchronized ServletContextHandler createHandler(final HandlerContainer parent,
      final String contextPath) {
    CustomServletHandler servletHandler = new CustomServletHandler();

    servletHandler.setEnsureDefaultServlet(false);

    SessionHandler sessionHandler = resolveSessionHandler();
    SecurityHandler securityHandler = resolveSecurityHandler();
    ErrorHandler errorHandler = resolveErrorHandler();

    int sessionsFlag = (sessions) ? ServletContextHandler.SESSIONS
        : ServletContextHandler.NO_SESSIONS;

    int securityFlag = (security) ? ServletContextHandler.SECURITY
        : ServletContextHandler.NO_SECURITY;

    int options = securityFlag | sessionsFlag;

    ServletContextHandler servletContextHandler = new ServletContextHandler(parent,
        contextPath, sessionHandler, securityHandler, servletHandler, errorHandler, options);

    updateServletHandlerWithDynamicSettings(servletHandler);

    servletContextHandler.setMaxFormContentSize(maxFormContentSize);
    servletContextHandler.setMaxFormKeys(maxFormKeys);
    setMimeTypesOnServletContextHandler(servletContextHandler);
    servletContextHandler.setVirtualHosts(virtualHosts);

    addListenersToHandler(servletContextHandler);

    activeServletContextHandlers.put(servletContextHandler, Boolean.TRUE);

    return servletContextHandler;
  }

  private ErrorHandler resolveErrorHandler() {
    ErrorHandler errorHandler = null;
    if (errorHandlerFactory != null) {
      errorHandler = errorHandlerFactory.createErrorHandler();
    }
    return errorHandler;
  }

  private FilterMappingKey[] resolveFilterMappingKeys(final ServiceHolder<Filter>[] filters) {
    FilterMappingKey[] result = new FilterMappingKey[filters.length];
    for (int i = 0; i < filters.length; i++) {
      ServiceHolder<Filter> serviceHolder = filters[i];
      result[i] = new FilterMappingKey(serviceHolder);
    }
    return result;
  }

  private <E> HolderKey<E>[] resolveHolderKeys(final ServiceHolder<E>[] serviceHolders) {
    @SuppressWarnings("unchecked")
    HolderKey<E>[] result = new HolderKey[serviceHolders.length];

    for (int i = 0; i < serviceHolders.length; i++) {
      ServiceHolder<E> serviceHolder = serviceHolders[i];
      result[i] = new HolderKey<E>(serviceHolder);
    }
    return result;
  }

  private SecurityHandler resolveSecurityHandler() {
    SecurityHandler securityHandler = null;
    if (securityHandlerFactory != null) {
      securityHandler = securityHandlerFactory.createSecurityHandler();
    }
    return securityHandler;
  }

  private ServletMappingKey[] resolveServletMappingKeys(final ServiceHolder<Servlet>[] servlets) {
    ServletMappingKey[] result = new ServletMappingKey[servlets.length];
    for (int i = 0; i < servlets.length; i++) {
      ServiceHolder<Servlet> serviceHolder = servlets[i];
      result[i] = new ServletMappingKey(serviceHolder);
    }
    return result;
  }

  private SessionHandler resolveSessionHandler() {
    SessionHandler sessionHandler = null;
    if (sessionHandlerFactory != null) {
      sessionHandler = sessionHandlerFactory.createSessionHandler();
    }
    return sessionHandler;
  }

  @ServiceRef(
      referenceId = ServletContextHandlerFactoryConstants.ATTR_CONTEXT_ATTRIBUTE_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_05,
      label = "ServletContextAttributeListeners (target)",
      description = "OSGi filter of zero or more ServletContextAttributeListener service that will "
          + "be registered in the ServletContext.")
  public void setContextAttributeListeners(
      final ServletContextAttributeListener[] contextAttributeListeners) {
    this.contextAttributeListeners = contextAttributeListeners;
  }

  @ServiceRef(referenceId = ServletContextHandlerFactoryConstants.ATTR_CONTEXT_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_04,
      label = "ServletContextListeners (target)",
      description = "OSGi filter of zero or more ServletContextListener service that will be "
          + "registered in the ServletContext.")
  public void setContextListeners(final ServletContextListener[] contextListeners) {
    this.contextListeners = contextListeners;
  }

  @ServiceRef(
      referenceId = ServletContextHandlerFactoryConstants.ATTR_ERROR_HANDLER_FACTORY,
      optional = true, attributePriority = PriorityConstants.PRIORITY_12,
      label = "ErrorHandler factory (target)",
      description = "OSGi service filter that selects the factory of the ErrorHandler that should "
          + "be used in the servlet context. This attribute is optional.")
  public void setErrorHandlerFactory(final ErrorHandlerFactory errorHandlerFactory) {
    this.errorHandlerFactory = errorHandlerFactory;
  }

  @ServiceRef(referenceId = ServletContextHandlerFactoryConstants.ATTR_FILTERS,
      configurationType = ReferenceConfigurationType.CLAUSE, optional = true, dynamic = true,
      attributePriority = PriorityConstants.PRIORITY_03, label = "Filters (clause)",
      description = "Zero or more clauses to register filters within the Servlet Context. Possible "
          + "attributes are: url-pattern, servlet-name, dispatcher, init-*. E.g.: myFilter;"
          + "url-pattern=\"/a,/b/c\";servlet-name=\"myServlet,myOtherServlet\","
          + "dispatcher=\"REQUEST,ASYNC\";filter:=(service.pid=...)")
  public synchronized void setFilters(final ServiceHolder<Filter>[] filters) {
    filterKeys = resolveHolderKeys(filters);
    filterMappingKeys = resolveFilterMappingKeys(filters);
  }

  @IntegerAttribute(attributeId = ServletContextHandlerFactoryConstants.ATTR_MAX_FORM_CONTENT_SIZE,
      defaultValue = -1, priority = PriorityConstants.PRIORITY_14, label = "Max. form content size",
      description = "The maximum size of a form post, to protect against DOS attacks from large "
          + "forms.")
  public void setMaxFormContentSize(final int maxFormContentSize) {
    updateMaxFormContentSize(maxFormContentSize);
  }

  @IntegerAttribute(attributeId = ServletContextHandlerFactoryConstants.ATTR_MAX_FORM_KEYS,
      defaultValue = -1, priority = PriorityConstants.PRIORITY_15, label = "Max. form keys",
      description = "The maximum number of form Keys to protect against DOS attack from crafted "
          + "hash keys.")
  public void setMaxFormKeys(final int maxFormKeys) {
    updateMaxFormKeys(maxFormKeys);
  }

  @ServiceRef(referenceId = ServletContextHandlerFactoryConstants.ATTR_MIMETYPES,
      optional = true, attributePriority = PriorityConstants.PRIORITY_13,
      label = "Mime types (target)",
      description = "The mime type object that can be used to resolve the mime type of a file.")
  public void setMimeTypes(final MimeTypes mimeTypes) {
    updateMimeTypes(mimeTypes);
  }

  private void setMimeTypesOnServletContextHandler(
      final ServletContextHandler servletContextHandler) {

    if (mimeTypes == null) {
      servletContextHandler.setMimeTypes(new MimeTypes());
    } else {
      servletContextHandler.setMimeTypes(mimeTypes);
    }

  }

  @ServiceRef(
      referenceId = ServletContextHandlerFactoryConstants.ATTR_REQUEST_ATTRIBUTE_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_07,
      label = "ServletRequestAttributeListeners (target)",
      description = "OSGi filter of zero or more ServletRequestAttributeListener service that will "
          + "be registered in the ServletContext.")
  public void setRequestAttributeListeners(
      final ServletRequestAttributeListener[] requestAttributeListeners) {
    this.requestAttributeListeners = requestAttributeListeners;
  }

  @ServiceRef(referenceId = ServletContextHandlerFactoryConstants.ATTR_REQUEST_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_06,
      label = "ServletRequestListeners (target)",
      description = "OSGi filter of zero or more ServletRequestListener service that will be "
          + "registered in the ServletContext.")
  public void setRequestListeners(final ServletRequestListener[] requestListeners) {
    this.requestListeners = requestListeners;
  }

  @BooleanAttribute(attributeId = ServletContextHandlerFactoryConstants.ATTR_SECURITY,
      defaultValue = false, priority = PriorityConstants.PRIORITY_10, label = "Security",
      description = "In case the security property is set to true and no securityHandlerFactory "
          + "reference is defined, a constraint based security manager with default values will "
          + "be used.")
  public void setSecurity(final boolean security) {
    this.security = security;
  }

  @ServiceRef(
      referenceId = ServletContextHandlerFactoryConstants.ATTR_SECURITY_HANDLER_FACTORY,
      optional = true, attributePriority = PriorityConstants.PRIORITY_11,
      label = "SecurityHandler factory (target)",
      description = "OSGi service filter that selects the factory of the SecurityHandler that "
          + "should be used in the servlet context. This attribute is optional. In case it is not "
          + "defined, a constraint based security handler will be started if the security property "
          + "is set to true.")
  public void setSecurityHandlerFactory(final SecurityHandlerFactory securityHandlerFactory) {
    this.securityHandlerFactory = securityHandlerFactory;
  }

  @ServiceRef(referenceId = ServletContextHandlerFactoryConstants.ATTR_SERVLETS,
      configurationType = ReferenceConfigurationType.CLAUSE, optional = true, dynamic = true,
      attributePriority = PriorityConstants.PRIORITY_02, label = "Servlets (clause)",
      description = "Zero or more clauses to register servlets within the Servlet Context. "
          + "Possible attributes are: url-pattern, init-*. E.g.: myFilter;url-pattern=\"/a,/b/c\";"
          + "filter:=(service.pid=...). The OSGi service that implements the Servlet interface can "
          + "contain the \"async-supported\" service property.")
  public void setServlets(final ServiceHolder<Servlet>[] servlets) {
    servletKeys = resolveHolderKeys(servlets);
    servletMappingKeys = resolveServletMappingKeys(servlets);
  }

  @ServiceRef(
      referenceId = ServletContextHandlerFactoryConstants.ATTR_SESSION_HANDLER_FACTORY,
      optional = true, attributePriority = PriorityConstants.PRIORITY_09,
      label = "SessionHandler Factory (target)",
      description = "OSGi service filter that selects the factory of the session handler that "
          + "should be used in the servlet context. This attribute is optional. In case it is not "
          + "defined, a hash based session handler will be started if the sessions property is set "
          + "to true.")
  public void setSessionHandlerFactory(final SessionHandlerFactory sessionHandlerFactory) {
    this.sessionHandlerFactory = sessionHandlerFactory;
  }

  @BooleanAttribute(attributeId = ServletContextHandlerFactoryConstants.ATTR_SESSIONS,
      defaultValue = true, priority = PriorityConstants.PRIORITY_08, label = "Sessions",
      description = "In case the sessions property is set to true and no sessionManagerFactory "
          + "reference is defined, a hash based session manager will be used with default "
          + "settings.")
  public void setSessions(final boolean sessions) {
    this.sessions = sessions;
  }

  @StringAttribute(attributeId = ServletContextHandlerFactoryConstants.ATTR_VIRTUAL_HOSTS,
      optional = true, multiple = ThreeStateBoolean.TRUE, priority = PriorityConstants.PRIORITY_01,
      label = "Virtual hosts",
      description = "Array of virtual hosts that this context responds to. A null host name or "
          + "null/empty array means any hostname is acceptable. Host names may be String "
          + "representation of IP addresses. Host names may start with '*.' to wildcard one level "
          + "of names.  Hosts may start with '@', in which case they will match the "
          + "Connector#getName()} for the request.")
  public void setVirtualHosts(final String[] virtualHosts) {
    updateVirtualHosts(virtualHosts);
  }

  /**
   * Updates the dynamic references (filters and servlets with their mappings) on all of the created
   * and still used (the instances are referenced) {@link ServletContextHandler} instances.
   */
  @Update
  public synchronized void update() {

    Set<ServletContextHandler> servletContextHandlers = cloneActiveServletContextHandlerSet();
    for (ServletContextHandler servletContextHandler : servletContextHandlers) {
      ServletHandler servletHandler = servletContextHandler.getServletHandler();
      if (servletHandler instanceof CustomServletHandler) {
        updateServletHandlerWithDynamicSettings((CustomServletHandler) servletHandler);
      }
    }
    servletHolderManager.updatePrviousKeys(servletKeys);
    servletMappingManager.updatePrviousKeys(servletMappingKeys);
    filterHolderManager.updatePrviousKeys(filterKeys);
    filterMappingManager.updatePrviousKeys(filterMappingKeys);
  }

  private synchronized void updateMaxFormContentSize(final int pMaxFormContentSize) {
    maxFormContentSize = pMaxFormContentSize;
    Set<ServletContextHandler> servletContextHandlers = cloneActiveServletContextHandlerSet();
    for (ServletContextHandler servletContextHandler : servletContextHandlers) {
      servletContextHandler.setMaxFormContentSize(pMaxFormContentSize);
    }

  }

  private synchronized void updateMaxFormKeys(final int pMaxFormKeys) {
    maxFormKeys = pMaxFormKeys;
    Set<ServletContextHandler> servletContextHandlers = cloneActiveServletContextHandlerSet();
    for (ServletContextHandler servletContextHandler : servletContextHandlers) {
      servletContextHandler.setMaxFormKeys(pMaxFormKeys);
    }

  }

  private synchronized void updateMimeTypes(final MimeTypes pMimeTypes) {
    mimeTypes = pMimeTypes;
    Set<ServletContextHandler> servletContextHandlers = cloneActiveServletContextHandlerSet();
    for (ServletContextHandler servletContextHandler : servletContextHandlers) {
      setMimeTypesOnServletContextHandler(servletContextHandler);
    }
  }

  private synchronized void updateServletHandlerWithDynamicSettings(
      final CustomServletHandler servletHandler) {

    ServletHolder[] servletHolders = servletHolderManager.generateUpgradedElementArray(servletKeys,
        servletHandler.getServlets());

    ServletMapping[] servletMappings = servletMappingManager.generateUpgradedElementArray(
        servletMappingKeys, servletHandler.getServletMappings());

    FilterHolder[] filterHolders = filterHolderManager.generateUpgradedElementArray(filterKeys,
        servletHandler.getFilters());

    FilterMapping[] filterMappings = filterMappingManager.generateUpgradedElementArray(
        filterMappingKeys, servletHandler.getFilterMappings());

    servletHandler.updateServletsAndFilters(servletHolders, servletMappings, filterHolders,
        filterMappings);
  }

  private synchronized void updateVirtualHosts(final String[] pVirtualHosts) {
    virtualHosts = pVirtualHosts;
    Set<ServletContextHandler> servletContextHandlers = cloneActiveServletContextHandlerSet();
    for (ServletContextHandler servletContextHandler : servletContextHandlers) {
      servletContextHandler.setVirtualHosts(virtualHosts);
    }
  }
}
