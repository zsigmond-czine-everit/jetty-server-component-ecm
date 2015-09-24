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

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.everit.jetty.server.component.ecm.HashSessionHandlerFactoryConstants;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.jetty.server.component.ecm.SessionHandlerConstants;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.IntegerAttribute;
import org.everit.osgi.ecm.annotation.attribute.LongAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.component.ConfigurationException;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.SessionHandlerFactory;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Configurable component that creates a {@link SessionHandler} based on {@link HashSessionManager}
 * implementation.
 */
@Component(componentId = HashSessionHandlerFactoryConstants.SERVICE_FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    label = "Everit Jetty Hash SessionHandler Factory")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION, optional = true,
        label = "Service description",
        description = "Optional description for SessionHandler Factory service.") })
@Service
public class HashSessionHandlerFactoryComponent implements SessionHandlerFactory {

  private boolean checkingRemoteSessionIdEncoding;

  private String cookieName;

  private boolean deleteUnrestorableSessions;

  private boolean httpOnly;

  private int idleSavePeriod;

  private boolean lazyLoad;

  private int maxInactiveInterval;

  private boolean nodeIdInSessionId;

  private Random random;

  private final WeakHashMap<HashSessionManager, Boolean> referencedSessionManagers =
      new WeakHashMap<>();

  private int refreshCookieAge;

  private long reseed;

  private int savePeriod;

  private int scavengePeriod;

  private boolean secureRequestOnly;

  private HttpSessionAttributeListener[] sessionAttributeListeners;

  private HttpSessionIdListener[] sessionIdListeners;

  private String sessionIdParameterName;

  private HttpSessionListener[] sessionListeners;

  private String storeDirectory;

  private boolean usingCookies;

  private boolean usingURLs;

  private String workerName;

  private void addListeners(final HashSessionManager sessionManager) {
    if (sessionListeners != null) {
      for (HttpSessionListener sessionListener : sessionListeners) {
        sessionManager.addEventListener(sessionListener);
      }
    }

    if (sessionAttributeListeners != null) {
      for (HttpSessionAttributeListener sessionAttributeListener : sessionAttributeListeners) {
        sessionManager.addEventListener(sessionAttributeListener);
      }
    }

    if (sessionIdListeners != null) {
      for (HttpSessionIdListener sessionIdListener : sessionIdListeners) {
        sessionManager.addEventListener(sessionIdListener);
      }
    }
  }

  private synchronized Set<HashSessionManager> cloneReferencedConnectionFactories() {
    Set<HashSessionManager> result = null;
    while (result == null) {
      try {
        result = new HashSet<>(referencedSessionManagers.keySet());
      } catch (ConcurrentModificationException e) {
        // TODO probably some warn logging would be nice
      }
    }
    return result;
  }

  @Override
  public synchronized SessionHandler createSessionHandler() {
    HashSessionManager sessionManager = new HashSessionManager();

    sessionManager.setMaxInactiveInterval(maxInactiveInterval);
    File storeDirFile = resolveStoreDirectory();
    if (storeDirFile != null) {
      try {
        sessionManager.setStoreDirectory(storeDirFile);
      } catch (IOException e) {
        throw new ConfigurationException("Could not set store directory: " + storeDirFile, e);
      }
    }
    sessionManager.setSavePeriod(savePeriod);
    sessionManager.setCheckingRemoteSessionIdEncoding(checkingRemoteSessionIdEncoding);
    sessionManager.setDeleteUnrestorableSessions(deleteUnrestorableSessions);
    sessionManager.setHttpOnly(httpOnly);
    sessionManager.setIdleSavePeriod(idleSavePeriod);
    sessionManager.setLazyLoad(lazyLoad);
    sessionManager.setNodeIdInSessionId(nodeIdInSessionId);
    sessionManager.setRefreshCookieAge(refreshCookieAge);
    sessionManager.setScavengePeriod(scavengePeriod);
    sessionManager.setSecureRequestOnly(secureRequestOnly);
    sessionManager.setSessionCookie(cookieName);
    sessionManager.setSessionIdPathParameterName(sessionIdParameterName);
    sessionManager.setSessionTrackingModes(resolveSessionTrackingModes());

    HashSessionIdManager hashSessionIdManager = new HashSessionIdManager();

    if (workerName != null) {
      hashSessionIdManager.setWorkerName(workerName);
    }
    hashSessionIdManager.setReseed(reseed);
    if (random != null) {
      hashSessionIdManager.setRandom(random);
    }

    sessionManager.setSessionIdManager(hashSessionIdManager);

    addListeners(sessionManager);

    // TODO add more configuration possibilities (also for the id manager)

    SessionHandler sessionHandler = new SessionHandler(sessionManager);

    return sessionHandler;
  }

  private Set<SessionTrackingMode> resolveSessionTrackingModes() {
    Set<SessionTrackingMode> result = new HashSet<SessionTrackingMode>();
    if (usingCookies) {
      result.add(SessionTrackingMode.COOKIE);
    }
    if (usingURLs) {
      result.add(SessionTrackingMode.URL);
    }
    return result;
  }

  private File resolveStoreDirectory() {
    if (storeDirectory == null) {
      return null;
    }
    return new File(storeDirectory);
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_CHECKING_REMOTE_SESSION_ID_ENCODING,
      defaultValue = SessionHandlerConstants.DEFAULT_CHECKING_REMOTE_SESSION_ID_ENCODING,
      priority = PriorityConstants.PRIORITY_11, label = "Checking remote session id encoding",
      description = "True if absolute URLs are check for remoteness before being session encoded.")
  public void setCheckingRemoteSessionIdEncoding(final boolean checkingRemoteSessionIdEncoding) {
    this.checkingRemoteSessionIdEncoding = checkingRemoteSessionIdEncoding;
  }

  @StringAttribute(attributeId = SessionHandlerConstants.ATTR_COOKIE_NAME,
      defaultValue = SessionManager.__DefaultSessionCookie,
      priority = PriorityConstants.PRIORITY_07, label = "Cookie name",
      description = "The name of the cookie.")
  public void setCookieName(final String cookieName) {
    this.cookieName = cookieName;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_DELETE_UNRESTORABLE_SESSIONS,
      defaultValue = SessionHandlerConstants.DEFAULT_DELETE_UNRESTORABLE_SESSIONS,
      priority = PriorityConstants.PRIORITY_14, label = "Delete unrestorable sessions",
      description = "")
  public void setDeleteUnrestorableSessions(final boolean deleteUnrestorableSessions) {
    this.deleteUnrestorableSessions = deleteUnrestorableSessions;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_HTTP_ONLY,
      defaultValue = SessionHandlerConstants.DEFAULT_HTTP_ONLY,
      priority = PriorityConstants.PRIORITY_15, label = "HTTP only", description = "")
  public void setHttpOnly(final boolean httpOnly) {
    this.httpOnly = httpOnly;
  }

  @IntegerAttribute(attributeId = SessionHandlerConstants.ATTR_IDLE_SAVE_PERIOD,
      defaultValue = SessionHandlerConstants.DEFAULT_IDLE_SAVE_PERIOD,
      priority = PriorityConstants.PRIORITY_16, label = "Idle save period",
      description = "Configures the period in seconds after which a session is deemed idle and "
          + "saved to save on session memory. The session is persisted, the values attribute map "
          + "is cleared and the session set to idled.")
  public void setIdleSavePeriod(final int idleSavePeriod) {
    this.idleSavePeriod = idleSavePeriod;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_LAZY_LOAD,
      defaultValue = SessionHandlerConstants.DEFAULT_LAZY_LOAD, label = "Lazy load",
      description = "Restore sessions lazily.")
  public void setLazyLoad(final boolean lazyLoad) {
    this.lazyLoad = lazyLoad;
  }

  /**
   * Sets the session-timeout on the component and on all referenced session managers.
   */
  @IntegerAttribute(attributeId = SessionHandlerConstants.ATTR_MAX_INACTIVE_INTERVAL,
      defaultValue = SessionHandlerConstants.DEFAULT_MAX_INACTIVE_INTERVAL, dynamic = true,
      priority = PriorityConstants.PRIORITY_01, label = "Max inactive interval",
      description = "The max period of inactivity, after which the session is invalidated, "
          + "in seconds.")
  public synchronized void setMaxInactiveInterval(final int maxInactiveInterval) {
    this.maxInactiveInterval = maxInactiveInterval;
    for (HashSessionManager sessionManager : cloneReferencedConnectionFactories()) {
      sessionManager.setMaxInactiveInterval(maxInactiveInterval);
    }
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_NODE_IN_SESSION_ID,
      defaultValue = SessionHandlerConstants.DEFAULT_NODE_IN_SESSION_ID,
      priority = PriorityConstants.PRIORITY_17, label = "Node in session id",
      description = "Wether the cluster node id (worker id) will be returned as part of the "
          + "session id by HttpSession.getId() or not.")
  public void setNodeIdInSessionId(final boolean nodeIdInSessionId) {
    this.nodeIdInSessionId = nodeIdInSessionId;
  }

  @ServiceRef(referenceId = SessionHandlerConstants.ATTR_RANDOM, optional = true,
      attributePriority = PriorityConstants.PRIORITY_21, label = "Random (target)",
      description = "The random number generator for generating Session Ids.")
  public void setRandom(final Random random) {
    this.random = random;
  }

  @IntegerAttribute(attributeId = SessionHandlerConstants.ATTR_REFRESH_COOKIE_AGE,
      defaultValue = SessionHandlerConstants.DEFAULT_REFRESH_COOKIE_AGE,
      priority = PriorityConstants.PRIORITY_18, label = "Refresh cookie age",
      description = "Time before a session cookie is re-set in seconds.")
  public void setRefreshCookieAge(final int refreshCookieAge) {
    this.refreshCookieAge = refreshCookieAge;
  }

  @LongAttribute(attributeId = SessionHandlerConstants.ATTR_RESEED,
      defaultValue = SessionHandlerConstants.DEFAULT_RESEED,
      priority = PriorityConstants.PRIORITY_20, label = "Reseed",
      description = "If non zero then when a random long modulo the reseed value == 1, "
          + "the SecureRandom will be reseeded.")
  public void setReseed(final long reseed) {
    this.reseed = reseed;
  }

  @IntegerAttribute(attributeId = SessionHandlerConstants.ATTR_SAVE_PERIOD,
      defaultValue = SessionHandlerConstants.DEFAULT_SAVE_PERIOD,
      priority = PriorityConstants.PRIORITY_12, label = "Save period",
      description = "The period is seconds at which sessions are periodically saved to disk. "
          + "Zero means never save.")
  public void setSavePeriod(final int savePeriod) {
    this.savePeriod = savePeriod;
  }

  @IntegerAttribute(attributeId = SessionHandlerConstants.ATTR_SCAVENGE_PERIOD,
      defaultValue = SessionHandlerConstants.DEFAULT_SCAVENGE_PERIOD,
      priority = PriorityConstants.PRIORITY_19, label = "Scavenge period",
      description = "The period in seconds at which a check is made for sessions to be "
          + "invalidated.")
  public void setScavengePeriod(final int scavengePeriod) {
    this.scavengePeriod = scavengePeriod;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_SECURE_REQUEST_ONLY,
      defaultValue = SessionHandlerConstants.DEFAULT_SECURE_REQUEST_ONLY,
      priority = PriorityConstants.PRIORITY_05, label = "Secure request only",
      description = "HTTPS request. Can be overridden by setting "
          + "SessionCookieConfig.setSecure(true), in which case the session cookie will be marked "
          + "as secure on both HTTPS and HTTP.")
  public void setSecureRequestOnly(final boolean secureRequestOnly) {
    this.secureRequestOnly = secureRequestOnly;
  }

  @ServiceRef(referenceId = SessionHandlerConstants.ATTR_SESSION_ATTRIBUTE_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_03,
      label = "Session attribute listeners (target)",
      description = "Zero or more filter expression for HttpSessionAttributeListener services")
  public void setSessionAttributeListeners(
      final HttpSessionAttributeListener[] sessionAttributeListeners) {
    this.sessionAttributeListeners = sessionAttributeListeners;
  }

  @ServiceRef(referenceId = SessionHandlerConstants.ATTR_SESSION_ID_LISTENERS,
      optional = true, attributePriority = PriorityConstants.PRIORITY_04,
      label = "Session Id listeners (target)",
      description = "Zero or more filter expression for HttpSessionIdListener services")
  public void setSessionIdListeners(final HttpSessionIdListener[] sessionIdListeners) {
    this.sessionIdListeners = sessionIdListeners;
  }

  @StringAttribute(attributeId = SessionHandlerConstants.SESSION_ID_PARAMETER_NAME,
      defaultValue = SessionManager.__DefaultSessionIdPathParameterName,
      priority = PriorityConstants.PRIORITY_09, label = "Session id parameter name",
      description = "The URL path parameter name for session id URL rewriting "
          + "(\"none\" for no rewriting).")
  public void setSessionIdParameterName(final String sessionIdParameterName) {
    this.sessionIdParameterName = sessionIdParameterName;
  }

  @ServiceRef(referenceId = SessionHandlerConstants.ATTR_SESSION_LISTENERS, optional = true,
      attributePriority = PriorityConstants.PRIORITY_02, label = "Session listeners (target)",
      description = "Zero or more filter expression for HttpSessionListener services.")
  public void setSessionListeners(final HttpSessionListener[] sessionListeners) {
    this.sessionListeners = sessionListeners;
  }

  @StringAttribute(attributeId = SessionHandlerConstants.ATTR_STORE_DIRECTORY, optional = true,
      priority = PriorityConstants.PRIORITY_13, label = "Store directory",
      description = "Path to the directory where sessions should be stored.")
  public void setStoreDirectory(final String storeDirectory) {
    this.storeDirectory = storeDirectory;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_USING_COOKIES,
      defaultValue = SessionHandlerConstants.DEFAULT_USING_COOKIES,
      priority = PriorityConstants.PRIORITY_06, label = "Using cookies")
  public void setUsingCookies(final boolean usingCookies) {
    this.usingCookies = usingCookies;
  }

  @BooleanAttribute(attributeId = SessionHandlerConstants.ATTR_USING_URLS,
      defaultValue = SessionHandlerConstants.DEFAULT_USING_URLS,
      priority = PriorityConstants.PRIORITY_08, label = "Using URLs", description = "")
  public void setUsingURLs(final boolean usingURLs) {
    this.usingURLs = usingURLs;
  }

  @StringAttribute(attributeId = SessionHandlerConstants.ATTR_WORKER_NAME, optional = true,
      priority = PriorityConstants.PRIORITY_10, label = "Worker name",
      description = "If set, the workername is dot appended to the session ID and can be used to "
          + "assist session affinity in a load balancer. A worker name starting with $ is used as "
          + "a request attribute name to lookup the worker name that can be dynamically set by a "
          + "request customiser.")
  public void setWorkerName(final String workerName) {
    this.workerName = workerName;
  }

}
