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

import javax.annotation.Generated;
import javax.servlet.Servlet;

import org.everit.osgi.ecm.component.ServiceHolder;

/**
 * Mapping keys for servlets.
 */
public class ServletMappingKey extends MappingKey<Servlet> {

  public final String servletName;

  public ServletMappingKey(final ServiceHolder<Servlet> serviceHolder) {
    super(serviceHolder);
    servletName = serviceHolder.getReferenceId();
  }

  @Override
  @Generated("eclipse")
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ServletMappingKey other = (ServletMappingKey) obj;
    if (servletName == null) {
      if (other.servletName != null) {
        return false;
      }
    } else if (!servletName.equals(other.servletName)) {
      return false;
    }
    return true;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + ((servletName == null) ? 0 : servletName.hashCode());
    return result;
  }

}
