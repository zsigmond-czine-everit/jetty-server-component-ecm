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
package org.everit.jetty.server.component.ecm.internal.servletcontext;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Manager for servlet ServletContext component elements.
 */
public class ServletHolderManager extends
    AbstractServletContextElementManager<HolderKey<Servlet>, ServletHolder> {

  @Override
  protected ServletHolder createNewElement(final HolderKey<Servlet> newKey) {
    ServletHolder servletHolder = new ServletHolder(newKey.name, newKey.heldValue);
    servletHolder.setAsyncSupported(newKey.asyncSupported);
    servletHolder.setInitParameters(newKey.initParameters);

    return servletHolder;
  }

  @Override
  protected ServletHolder[] createNewElementArray(final int length) {
    return new ServletHolder[length];
  }

}
