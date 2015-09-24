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
 * Constants of SslConnectionFactoryFactory component.
 */
public final class SslConnectionFactoryFactoryConstants {

  public static final String ATTR_CERT_ALIAS = "certAlias";

  public static final String ATTR_INCLUDE_PROTOCOLS = "includeProtocols";

  public static final String ATTR_KEY_MANAGER_PASSWORD = "keyManagerPassword";

  public static final String ATTR_KEYSTORE = "keyStore";

  public static final String ATTR_KEYSTORE_PASSWORD = "keyStorePassword";

  public static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";

  public static final String SERVICE_FACTORY_PID =
      "org.everit.jetty.server.component.ecm.SslConnectionFactoryFactory";

  private SslConnectionFactoryFactoryConstants() {
  }
}
