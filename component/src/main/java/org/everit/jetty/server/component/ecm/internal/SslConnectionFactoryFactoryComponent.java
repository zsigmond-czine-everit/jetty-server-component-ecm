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

import java.net.URL;
import java.security.KeyStore;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.everit.jetty.server.component.ecm.PriorityConstants;
import org.everit.jetty.server.component.ecm.SslConnectionFactoryFactoryConstants;
import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Service;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.PasswordAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.osgi.jetty.server.ConnectionFactoryFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Component that can create {@link SslConnectionFactory} instances.
 */
@Component(componentId = SslConnectionFactoryFactoryConstants.SERVICE_FACTORY_PID,
    configurationPolicy = ConfigurationPolicy.FACTORY,
    label = "Everit Jetty SslConnectionFactory Factory",
    description = "Component to create SslConnectionFactory instances.")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION, optional = true,
        label = "Service description",
        description = "Optional description for the instantiated SslConnectionFactory Factory.") })
@Service
public class SslConnectionFactoryFactoryComponent implements ConnectionFactoryFactory {

  private BundleContext bundleContext;

  private String certAlias;

  private String[] excludeProtocols;

  private String[] includeProtocols;

  private String keyManagerPassword;

  private KeyStore keyStore;

  private String keyStorePassword;

  @Activate
  public void activate(final BundleContext pBundleContext) {
    bundleContext = pBundleContext;

  }

  @Override
  public ConnectionFactory createConnectionFactory(final String nextProtocol) {

    SslContextFactory sslContextFactory = new SslContextFactory();

    if (keyStore != null) {
      sslContextFactory.setKeyStore(keyStore);
      sslContextFactory.setKeyStorePassword(keyStorePassword);
      sslContextFactory.setCertAlias(certAlias);
      sslContextFactory.setKeyManagerPassword(keyManagerPassword);

      if ((includeProtocols != null) && (includeProtocols.length > 0)) {
        sslContextFactory.setIncludeProtocols(includeProtocols);
      }

      if ((excludeProtocols != null) && (excludeProtocols.length > 0)) {
        sslContextFactory.setExcludeProtocols(excludeProtocols);
      }

      // TODO we have much more settings

    } else {
      URL keyStoreUrl = bundleContext.getBundle().getResource("META-INF/development-keystore.jks");
      sslContextFactory.setKeyStorePath(keyStoreUrl.toExternalForm());
      sslContextFactory
          .setKeyStorePassword(SslConnectionFactoryFactoryConstants.DEFAULT_KEYSTORE_PASSWORD);
      sslContextFactory.setKeyStoreType("JKS");
    }

    SslConnectionFactory sslConnectionFactory = new CustomSslConnectionFactory(sslContextFactory,
        nextProtocol);

    return sslConnectionFactory;
  }

  @StringAttribute(attributeId = SslConnectionFactoryFactoryConstants.ATTR_CERT_ALIAS,
      optional = true, priority = PriorityConstants.PRIORITY_03, label = "Certificate alias",
      description = "Alias of SSL certificate for the connector.")
  public void setCertAlias(final String certAlias) {
    this.certAlias = certAlias;
  }

  public void setExcludeProtocols(final String[] excludeProtocols) {
    this.excludeProtocols = excludeProtocols;
  }

  @StringAttribute(attributeId = SslConnectionFactoryFactoryConstants.ATTR_INCLUDE_PROTOCOLS,
      optional = true, priority = PriorityConstants.PRIORITY_05, label = "Include protocols",
      description = "The array of protocol names to include in "
          + "SSLEngine.setEnabledProtocols(String [])")
  public void setIncludeProtocols(final String[] includeProtocols) {
    this.includeProtocols = includeProtocols;
  }

  @PasswordAttribute(attributeId = SslConnectionFactoryFactoryConstants.ATTR_KEY_MANAGER_PASSWORD,
      optional = true, priority = PriorityConstants.PRIORITY_04, label = "Key manager password",
      description = "The password (if any) for the specific key within the key store.")
  public void setKeyManagerPassword(final String keyManagerPassword) {
    this.keyManagerPassword = keyManagerPassword;
  }

  @ServiceRef(referenceId = SslConnectionFactoryFactoryConstants.ATTR_KEYSTORE,
      optional = true, attributePriority = PriorityConstants.PRIORITY_01,
      label = "KeyStore (target)",
      description = "Optional service filter expression of KeyStore OSGi service. If not specified,"
          + " the default developer certificate is used.")
  public void setKeyStore(final KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  @PasswordAttribute(attributeId = SslConnectionFactoryFactoryConstants.ATTR_KEYSTORE_PASSWORD,
      defaultValue = SslConnectionFactoryFactoryConstants.DEFAULT_KEYSTORE_PASSWORD,
      priority = PriorityConstants.PRIORITY_02, label = "KeyStore password",
      description = "The password for the key store.")
  public void setKeyStorePassword(final String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

}
