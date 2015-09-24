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
 * Constants that help the usage of ServletContextHandlerFactory component.
 */
public final class ServletContextHandlerFactoryConstants {

  /**
   * Constant values a that are used as or as part of multiple constants in
   * {@link ServletContextHandlerFactoryConstants} class.
   */
  public static final class CommonConstants {

    public static final String CLAUSE_ATTR_URL_PATTERN = "url-pattern";

    private CommonConstants() {
    }
  }

  public static final String ATTR_CONTEXT_ATTRIBUTE_LISTENERS = "contextAttributeListeners";

  public static final String ATTR_CONTEXT_LISTENERS = "contextListeners";

  public static final String ATTR_ERROR_HANDLER_FACTORY = "errorHandlerFactory";

  public static final String ATTR_FILTERS = "filters";

  public static final String ATTR_MAX_FORM_CONTENT_SIZE = "maxFormContentSize";

  public static final String ATTR_MAX_FORM_KEYS = "maxFormKeys";

  public static final String ATTR_MIMETYPES = "mimeTypes";

  public static final String ATTR_REQUEST_ATTRIBUTE_LISTENERS = "requestAttributeListeners";

  public static final String ATTR_REQUEST_LISTENERS = "requestListeners";

  public static final String ATTR_SECURITY = "security";

  public static final String ATTR_SECURITY_HANDLER_FACTORY = "securityHandlerFactory";

  public static final String ATTR_SERVLETS = "servlets";

  public static final String ATTR_SESSION_HANDLER_FACTORY = "sessionHandlerFactory";

  public static final String ATTR_SESSIONS = "sessions";

  public static final String ATTR_VIRTUAL_HOSTS = "virtualHosts";

  public static final String FILTER_CLAUSE_ATTR_DISPATCHER = "dispatcher";

  public static final String FILTER_CLAUSE_ATTR_SERVLET_NAME = "servlet-name";

  public static final String FILTER_CLAUSE_ATTR_URL_PATTERN =
      CommonConstants.CLAUSE_ATTR_URL_PATTERN;

  public static final String SERVICE_FACTORY_PID =
      "org.everit.jetty.server.component.ecm.ServletContextHandlerFactory";

  public static final String SERVLET_CLAUSE_ATTR_URL_PATTERN =
      CommonConstants.CLAUSE_ATTR_URL_PATTERN;

  private ServletContextHandlerFactoryConstants() {
  }
}
