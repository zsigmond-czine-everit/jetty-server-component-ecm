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
 * Constants that help the usage of ServerConnectorFactory component.
 */
public final class ServerConnectorFactoryConstants {

  public static final String ATTR_ACCEPT_QUEUE_SIZE = "acceptQueueSize";

  public static final String ATTR_ACCEPTOR_PRIORITY_DELTA = "acceptorPriorityDelta";

  public static final String ATTR_CONNECTION_FACTORY_FACTORIES =
      "connectionFactoryFactories";

  public static final String ATTR_IDLE_TIMEOUT = "idleTimeout";

  public static final String ATTR_INHERIT_CHANNEL = "inheritChannel";

  public static final String ATTR_LINGER_TIME = "lingerTime";

  public static final String ATTR_NAME = "name";

  public static final String ATTR_REUSE_ADDRESS = "reuseAddress";

  public static final String ATTR_SELECTOR_PRIORITY_DELTA = "selectorPriorityDelta";

  public static final long DEFAULT_IDLE_TIMEOUT = 30000;

  public static final boolean DEFAULT_INHERIT_CHANNEL = false;

  public static final int DEFAULT_LINGER_TIME = -1;

  public static final boolean DEFAULT_REUSE_ADDRESS = true;

  public static final int DEFAULT_SELECTOR_PRIORITY_DELTA = 0;

  public static final String SERVICE_FACTORY_PID =
      "org.everit.jetty.server.component.ecm.ServerConnectorFactory";

  private ServerConnectorFactoryConstants() {
  }
}
