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
package org.everit.jetty.server.component.ecm;

/**
 * Common constants that are available in session manager implementations.
 */
public final class SessionHandlerConstants {

  public static final String ATTR_CHECKING_REMOTE_SESSION_ID_ENCODING =
      "checkingRemoteSessionIdEncoding";

  public static final String ATTR_COOKIE_NAME = "cookieName";

  public static final String ATTR_DELETE_UNRESTORABLE_SESSIONS = "deleteUnrestorableSessions";

  public static final String ATTR_HTTP_ONLY = "httpOnly";

  public static final String ATTR_IDLE_SAVE_PERIOD = "idleSavePeriod";

  public static final String ATTR_LAZY_LOAD = "lazyLoad";

  public static final String ATTR_MAX_INACTIVE_INTERVAL = "maxInactiveInterval";

  public static final String ATTR_NODE_IN_SESSION_ID = "nodeIdInSessionId";

  public static final String ATTR_RANDOM = "random";

  public static final String ATTR_REFRESH_COOKIE_AGE = "refreshCookieAge";

  public static final String ATTR_RESEED = "reseed";

  public static final String ATTR_SAVE_PERIOD = "savePeriod";

  public static final String ATTR_SCAVENGE_PERIOD = "scavengePeriod";

  public static final String ATTR_SECURE_REQUEST_ONLY = "secureRequestOnly";

  public static final String ATTR_SESSION_ATTRIBUTE_LISTENERS = "sessionAttributeListeners";

  public static final String ATTR_SESSION_ID_LISTENERS = "sessionIdListeners";

  public static final String ATTR_SESSION_LISTENERS = "sessionListeners";

  public static final String ATTR_STORE_DIRECTORY = "storeDirectory";

  public static final String ATTR_USING_COOKIES = "usingCookies";

  public static final String ATTR_USING_URLS = "usingURLs";

  public static final String ATTR_WORKER_NAME = "workerName";

  public static final boolean DEFAULT_CHECKING_REMOTE_SESSION_ID_ENCODING = false;

  public static final boolean DEFAULT_DELETE_UNRESTORABLE_SESSIONS = false;

  public static final boolean DEFAULT_HTTP_ONLY = false;

  public static final int DEFAULT_IDLE_SAVE_PERIOD = 0;

  public static final boolean DEFAULT_LAZY_LOAD = false;

  public static final int DEFAULT_MAX_INACTIVE_INTERVAL = 30;

  public static final boolean DEFAULT_NODE_IN_SESSION_ID = false;

  public static final int DEFAULT_REFRESH_COOKIE_AGE = 0;

  public static final long DEFAULT_RESEED = 100000L;

  public static final int DEFAULT_SAVE_PERIOD = 0;

  public static final int DEFAULT_SCAVENGE_PERIOD = 30;

  public static final boolean DEFAULT_SECURE_REQUEST_ONLY = true;

  public static final boolean DEFAULT_USING_COOKIES = true;

  public static final boolean DEFAULT_USING_URLS = false;

  public static final String SESSION_ID_PARAMETER_NAME = "sessionIdParameterName";

  private SessionHandlerConstants() {
  }

}
