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
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.everit.jetty.server.component.ecm.HttpConnectionFactoryFactoryConstants;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.Update;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.IntegerAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.ConnectionFactoryFactory;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * ECM based configurable component that can start one or more {@link HttpConnectionFactory}s and
 * register them as OSGi services.
 */
@Component(componentId = HttpConnectionFactoryFactoryConstants.SERVICE_FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    label = "Everit Jetty HttpConnectionFactory Factory",
    description = "Component to create HTTPConnectionFactory instances.")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION, optional = true,
        label = "Service description",
        description = "Optional description for the instantiated HttpConnectionFactory Factory.") })

@Service
public class HttpConnectionFactoryFactoryComponent implements ConnectionFactoryFactory {

  private final WeakHashMap<CustomHttpConnectionFactory, Boolean> activeConnectionFactories =
      new WeakHashMap<>(); // CS_DISABLE_LINE_LENGTH

  private boolean closeAllEndpointsAfterDynamicUpdate = false;

  private Customizer[] customizers;

  private boolean delayDispatchUntilContent = false;

  private int headerCacheSize;

  private int inputBufferSize;

  private Integer outputAggregationSize;

  private int outputBufferSize;

  private int requestHeaderSize;

  private int responseHeaderSize;

  private int securePort;

  private String secureScheme;

  private boolean sendDateHeader;

  private boolean sendServerVersion;

  private boolean sendXPoweredBy;

  private Set<HttpConnectionFactory> cloneActiveHttpConnectionFactories() {
    Set<CustomHttpConnectionFactory> connectionFactories =
        cloneReferencedConnectionFactories();

    Set<HttpConnectionFactory> result = new HashSet<HttpConnectionFactory>();

    for (CustomHttpConnectionFactory customConnectionFactory : connectionFactories) {
      result.add(customConnectionFactory);
    }

    return result;

  }

  private synchronized Set<CustomHttpConnectionFactory> cloneReferencedConnectionFactories() {
    Set<CustomHttpConnectionFactory> result = null;
    while (result == null) {
      try {
        result = new HashSet<>(activeConnectionFactories.keySet());
      } catch (ConcurrentModificationException e) {
        // TODO probably some warn logging would be nice
      }
    }
    return result;
  }

  @Override
  public synchronized ConnectionFactory createConnectionFactory(final String nextProtocol) {
    HttpConfiguration httpConfiguration = new HttpConfiguration();
    httpConfiguration.setDelayDispatchUntilContent(delayDispatchUntilContent);
    httpConfiguration.setCustomizers(Arrays.asList(customizers));
    httpConfiguration.setHeaderCacheSize(headerCacheSize);

    httpConfiguration.setOutputBufferSize(outputBufferSize);

    if (outputAggregationSize != null) {
      httpConfiguration.setOutputAggregationSize(outputAggregationSize);
    }

    httpConfiguration.setRequestHeaderSize(requestHeaderSize);
    httpConfiguration.setResponseHeaderSize(responseHeaderSize);
    httpConfiguration.setSecurePort(securePort);
    httpConfiguration.setSecureScheme(secureScheme);
    httpConfiguration.setSendDateHeader(sendDateHeader);
    httpConfiguration.setSendServerVersion(sendServerVersion);
    httpConfiguration.setSendXPoweredBy(sendXPoweredBy);

    CustomHttpConnectionFactory httpConnectionFactory = new CustomHttpConnectionFactory(
        httpConfiguration);
    httpConnectionFactory.setInputBufferSize(inputBufferSize);

    activeConnectionFactories.put(httpConnectionFactory, true);
    return httpConnectionFactory;
  }

  /**
   * Updates the customizers on the component and all connection factories dynamically.
   **/
  @ServiceRef(referenceId = HttpConnectionFactoryFactoryConstants.ATTR_CUSTOMIZERS,
      optional = true, dynamic = true, attributePriority = PriorityConstants.PRIORITY_01,
      label = "Customizers (target)",
      description = "Customizers are invoked for every request received. Customizers are often "
          + "used to interpret optional headers (eg ForwardedRequestCustomizer) or optional "
          + "protocol semantics (eg SecureRequestCustomizer).")
  public synchronized void setCustomizers(final Customizer[] customizers) {
    this.customizers = customizers;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setCustomizers(Arrays.asList(customizers));
    }
  }

  /**
   * Updates delayDispatchUntilContent on the component and all active connection factories.
   */
  @BooleanAttribute(
      attributeId = HttpConnectionFactoryFactoryConstants.ATTR_DELAY_DISPATCH_UNTIL_CONTENT,
      defaultValue = false, dynamic = true, priority = PriorityConstants.PRIORITY_13,
      label = "Delay dispatch until content",
      description = "If true, delay the application dispatch until content is available")
  public synchronized void setDelayDispatchUntilContent(final boolean delayDispatchUntilContent) {
    this.delayDispatchUntilContent = delayDispatchUntilContent;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setDelayDispatchUntilContent(
          delayDispatchUntilContent);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Updates header cache size on component and all created connection factory.
   */
  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_HEADER_CACHE_SIZE,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_HEADER_CACHE_SIZE,
      dynamic = true, priority = PriorityConstants.PRIORITY_10, label = "Header cache size",
      description = "The maximum allowed size in bytes for a HTTP header field cache.")
  public void setHeaderCacheSize(final int headerCacheSize) {
    this.headerCacheSize = headerCacheSize;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setHeaderCacheSize(headerCacheSize);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets input buffer size on component and calls
   * {@link HttpConnectionFactory#setInputBufferSize(int)} on every generated
   * {@link HttpConnectionFactory}.
   */
  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_INPUT_BUFFER_SIZE,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_INPUT_BUFFER_SIZE,
      dynamic = true, priority = PriorityConstants.PRIORITY_06, label = "Input buffer size",
      description = "Size of input buffer of the created connections")
  public void setInputBufferSize(final int inputBufferSize) {
    this.inputBufferSize = inputBufferSize;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.setInputBufferSize(inputBufferSize);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  @IntegerAttribute(
      attributeId = HttpConnectionFactoryFactoryConstants.ATTR_OUTPUT_AGGREGATION_SIZE,
      optional = true, dynamic = true, priority = PriorityConstants.PRIORITY_09,
      label = "Output aggregation size",
      description = "The size of the buffer into which response content is aggregated before being "
          + "sent to the client. A larger buffer can improve performance by allowing a content"
          + " producer to run without blocking, however larger buffers consume more memory and may "
          + "induce some latency before a client starts processing the content. The default value "
          + "of this property is \"output buffer size / 4\".")
  public void setOutputAggregationSize(final Integer outputAggregationSize) {
    this.outputAggregationSize = outputAggregationSize;
  }

  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_OUTPUT_BUFFER_SIZE,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_OUTPUT_BUFFER_SIZE,
      dynamic = true, priority = PriorityConstants.PRIORITY_08, label = "Output buffer size",
      description = "The size of the buffer into which response content is aggregated before being "
          + "sent to the client. A larger buffer can improve performance by allowing a content "
          + "producer to run without blocking, however larger buffers consume more memory and may "
          + "induce some latency before a client starts processing the content.")
  public void setOutputBufferSize(final int outputBufferSize) {
    this.outputBufferSize = outputBufferSize;
  }

  /**
   * Sets requestHeaderSize on component and all already created connection factories.
   */
  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_REQUEST_HEADER_SIZE,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_REQUEST_HEADER_SIZE,
      dynamic = true, priority = PriorityConstants.PRIORITY_05, label = "Request header size",
      description = "The maximum size of a request header. Larger headers will allow for more "
          + "and/or larger cookies plus larger form content encoded in a URL. However, larger "
          + "headers consume more memory and can make a server more vulnerable to denial of "
          + "service attacks.")
  public synchronized void setRequestHeaderSize(final int requestHeaderSize) {
    this.requestHeaderSize = requestHeaderSize;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setRequestHeaderSize(requestHeaderSize);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets responseHeaderSize on component and all already created connection factories.
   */
  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_RESPONSE_HEADER_SIZE,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_RESPONSE_HEADER_SIZE,
      dynamic = true, priority = PriorityConstants.PRIORITY_07, label = "Response header size",
      description = "The maximum size of a response header. Larger headers will allow for more "
          + "and/or larger cookies and longer HTTP headers (eg for redirection). However, larger "
          + "headers will also consume more memory.")
  public synchronized void setResponseHeaderSize(final int responseHeaderSize) {
    this.responseHeaderSize = responseHeaderSize;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setResponseHeaderSize(responseHeaderSize);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets securePort an component and all already created connection factories.
   */
  @IntegerAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_SECURE_PORT,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_SECURE_PORT, dynamic = true,
      priority = PriorityConstants.PRIORITY_12, label = "Secure port",
      description = "The TCP/IP port used for CONFIDENTIAL and INTEGRAL redirections.")
  public synchronized void setSecurePort(final int securePort) {
    this.securePort = securePort;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setSecurePort(securePort);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets secureScheme an component and all already created connection factories.
   */
  @StringAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_SECURE_SCHEME,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_SECURE_SCHEME, dynamic = true,
      priority = PriorityConstants.PRIORITY_11, label = "Secure scheme",
      description = "The URI scheme used for CONFIDENTIAL and INTEGRAL redirections.")
  public synchronized void setSecureScheme(final String secureScheme) {
    this.secureScheme = secureScheme;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setSecureScheme(secureScheme);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets sendDateHeader an component and all already created connection factories.
   */
  @BooleanAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_SEND_DATE_HEADER,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_SEND_DATE_HEADER, dynamic = true,
      priority = PriorityConstants.PRIORITY_02, label = "Send date header", description = "")
  public synchronized void setSendDateHeader(final boolean sendDateHeader) {
    this.sendDateHeader = sendDateHeader;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setSendDateHeader(sendDateHeader);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets the sendServerVersion flag on the component and all referenced connections.
   */
  @BooleanAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_SEND_SERVER_VERSION,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_SEND_SERVER_VERSION,
      dynamic = true, priority = PriorityConstants.PRIORITY_03, label = "Send server version",
      description = "")
  public void setSendServerVersion(final boolean sendServerVersion) {
    this.sendServerVersion = sendServerVersion;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setSendServerVersion(sendServerVersion);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Sets the sendServerVersion flag on the component and all referenced connections.
   */
  @BooleanAttribute(attributeId = HttpConnectionFactoryFactoryConstants.ATTR_SEND_X_POWERED_BY,
      defaultValue = HttpConnectionFactoryFactoryConstants.DEFAULT_SEND_X_POWERED_BY,
      dynamic = true, priority = PriorityConstants.PRIORITY_04, label = "Send x-powered-by",
      description = "")
  public void setSendXPoweredBy(final boolean sendXPoweredBy) {
    this.sendXPoweredBy = sendXPoweredBy;
    for (HttpConnectionFactory httpConnectionFactory : cloneActiveHttpConnectionFactories()) {
      httpConnectionFactory.getHttpConfiguration().setSendXPoweredBy(sendXPoweredBy);
    }
    closeAllEndpointsAfterDynamicUpdate = true;
  }

  /**
   * Updates the outputBufferSize and outputAggregationSize on all active connection factories.
   */
  @Update
  public synchronized void update() {
    boolean closeAllEndpoints = closeAllEndpointsAfterDynamicUpdate;
    for (CustomHttpConnectionFactory connectionFactory : cloneReferencedConnectionFactories()) {
      HttpConfiguration httpConfiguration = connectionFactory.getHttpConfiguration();
      if (httpConfiguration.getOutputBufferSize() != outputBufferSize) {
        httpConfiguration.setOutputBufferSize(outputBufferSize);
        closeAllEndpoints = true;
      }
      if ((outputAggregationSize != null)
          && (httpConfiguration.getOutputAggregationSize() != outputAggregationSize.intValue())) {
        httpConfiguration.setOutputAggregationSize(outputAggregationSize);
        closeAllEndpoints = true;
      }
      if (closeAllEndpoints) {
        connectionFactory.closeReferencedEndpoints();
      }
    }

    closeAllEndpointsAfterDynamicUpdate = false;
  }
}
