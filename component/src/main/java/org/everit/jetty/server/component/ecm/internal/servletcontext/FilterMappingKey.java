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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

import javax.annotation.Generated;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.everit.jetty.server.component.ecm.ServletContextHandlerFactoryConstants;
import org.everit.osgi.ecm.component.ConfigurationException;
import org.everit.osgi.ecm.component.ServiceHolder;

/**
 * Key for FilterMappings.
 */
public class FilterMappingKey extends MappingKey<Filter> {

  public final EnumSet<DispatcherType> dispatcher;

  public final String filterName;

  public final String[] servletNames;

  /**
   * Constructor that sets all necessary attributes based on the content of the
   * {@link ServiceHolder}.
   *
   * @param serviceHolder
   *          The ServiceHolder that contains the relevant information for this key.
   */
  public FilterMappingKey(final ServiceHolder<Filter> serviceHolder) {
    super(serviceHolder);
    filterName = serviceHolder.getReferenceId();
    Map<String, Object> attributes = serviceHolder.getAttributes();
    servletNames = resolveClausePotentialListAttribute(
        ServletContextHandlerFactoryConstants.FILTER_CLAUSE_ATTR_SERVLET_NAME, attributes);

    dispatcher = resolveDispatcherTypes(attributes);
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
    FilterMappingKey other = (FilterMappingKey) obj;
    if (dispatcher == null) {
      if (other.dispatcher != null) {
        return false;
      }
    } else if (!dispatcher.equals(other.dispatcher)) {
      return false;
    }
    if (filterName == null) {
      if (other.filterName != null) {
        return false;
      }
    } else if (!filterName.equals(other.filterName)) {
      return false;
    }
    if (!Arrays.equals(servletNames, other.servletNames)) {
      return false;
    }
    return true;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + ((dispatcher == null) ? 0 : dispatcher.hashCode());
    result = (prime * result) + ((filterName == null) ? 0 : filterName.hashCode());
    result = (prime * result) + Arrays.hashCode(servletNames);
    return result;
  }

  private EnumSet<DispatcherType> resolveDispatcherTypes(
      final Map<String, Object> clauseAttributes) {

    EnumSet<DispatcherType> dispatcherTypes;
    Object dispatcherAttr = clauseAttributes
        .get(ServletContextHandlerFactoryConstants.FILTER_CLAUSE_ATTR_DISPATCHER);

    if (dispatcherAttr == null) {
      dispatcherTypes = EnumSet.noneOf(DispatcherType.class);
    } else {
      dispatcherTypes = EnumSet.noneOf(DispatcherType.class);

      String[] dispatcherTypeStringArray = String.valueOf(dispatcherAttr).split(",");
      for (String dispatcherTypeString : dispatcherTypeStringArray) {
        try {
          DispatcherType dispatcherType = DispatcherType.valueOf(dispatcherTypeString);
          dispatcherTypes.add(dispatcherType);
        } catch (IllegalArgumentException e) {
          throw new ConfigurationException(
              "Invalid dispatcherType in '"
                  + ServletContextHandlerFactoryConstants.ATTR_FILTERS + "[" + filterName
                  + "]' configuration: "
                  + dispatcherTypeString,
              e);
        }
      }
    }
    return dispatcherTypes;
  }

}
