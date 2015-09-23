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

import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * A customized context handler collection where the functionality of {@link #mapContexts()} can be
 * switched off temporary, until a full update is done.
 */
public class CustomContextHandlerCollection extends ContextHandlerCollection {

  private boolean mapContextsCallIgnored = false;

  @Override
  public void mapContexts() {
    if (mapContextsCallIgnored) {
      return;
    }
    super.mapContexts();
  }

  public void setMapContextsCallIgnored(final boolean mapContextsCallIgnored) {
    this.mapContextsCallIgnored = mapContextsCallIgnored;
  }
}
