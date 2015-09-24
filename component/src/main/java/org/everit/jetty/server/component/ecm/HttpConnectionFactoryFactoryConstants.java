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
 * Constants for HttpConnectionFactory component.
 */
public final class HttpConnectionFactoryFactoryConstants {

  public static final String ATTR_CUSTOMIZERS = "customizers";

  public static final String ATTR_DELAY_DISPATCH_UNTIL_CONTENT = "delayDispatchUntilContent";

  public static final String ATTR_HEADER_CACHE_SIZE = "headerCacheSize";

  public static final String ATTR_INPUT_BUFFER_SIZE = "inputBufferSize";

  public static final String ATTR_OUTPUT_AGGREGATION_SIZE = "outputAggregationSize";

  public static final String ATTR_OUTPUT_BUFFER_SIZE = "outputBufferSize";

  public static final String ATTR_REQUEST_HEADER_SIZE = "requestHeaderSize";

  public static final String ATTR_RESPONSE_HEADER_SIZE = "responseHeaderSize";

  public static final String ATTR_SECURE_PORT = "securePort";

  public static final String ATTR_SECURE_SCHEME = "secureScheme";

  public static final String ATTR_SEND_DATE_HEADER = "sendDateHeader";

  public static final String ATTR_SEND_SERVER_VERSION = "sendServerVersion";

  public static final String ATTR_SEND_X_POWERED_BY = "sendXPoweredBy";

  public static final int DEFAULT_HEADER_CACHE_SIZE = 512;

  public static final int DEFAULT_INPUT_BUFFER_SIZE = 8192;

  public static final int DEFAULT_OUTPUT_BUFFER_SIZE = 32 * 1024;

  public static final int DEFAULT_REQUEST_HEADER_SIZE = 8 * 1024;

  public static final int DEFAULT_RESPONSE_HEADER_SIZE = 8 * 1024;

  public static final int DEFAULT_SECURE_PORT = 0;

  public static final String DEFAULT_SECURE_SCHEME = "https";

  public static final boolean DEFAULT_SEND_DATE_HEADER = true;

  public static final boolean DEFAULT_SEND_SERVER_VERSION = true;

  public static final boolean DEFAULT_SEND_X_POWERED_BY = false;

  public static final String SERVICE_FACTORY_PID =
      "org.everit.jetty.server.component.ecm.HttpConnectionFactoryFactory";

  private HttpConnectionFactoryFactoryConstants() {
  }
}
