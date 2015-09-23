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

import org.eclipse.jetty.servlet.FilterMapping;

/**
 * Manager for FilterMapping ServletContext elements.
 */
public class FilterMappingManager extends
    AbstractServletContextElementManager<FilterMappingKey, FilterMapping> {

  @Override
  protected FilterMapping createNewElement(final FilterMappingKey key) {
    FilterMapping filterMapping = new FilterMapping();

    filterMapping.setFilterName(key.filterName);
    filterMapping.setPathSpecs(key.urlPatterns);
    filterMapping.setServletNames(key.servletNames);
    filterMapping.setDispatcherTypes(key.dispatcher);

    return filterMapping;
  }

  @Override
  protected FilterMapping[] createNewElementArray(final int length) {
    return new FilterMapping[length];
  }

}
