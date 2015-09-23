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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract manager class for ServletContext component.
 *
 * @param <KEY>
 *          The type of the key of the ServletContext element. E.g.: ServletKey
 * @param <ELEMENT>
 *          The type of the ServletContext element. E.g.: Servlet
 */
public abstract class AbstractServletContextElementManager<KEY, ELEMENT> {

  private Map<KEY, Integer> previousKeysWithPosition = Collections.emptyMap();

  protected abstract ELEMENT createNewElement(KEY newKey);

  protected abstract ELEMENT[] createNewElementArray(int length);

  /**
   * Generates a new Array for the new keys in the way that if there are keys from the old array,
   * those elements are re-used.
   *
   * @param newKeys
   *          The new key array.
   * @param previousElements
   *          The previous element array that was used by the ServletContext component.
   * @return The new element array.
   */
  public ELEMENT[] generateUpgradedElementArray(final KEY[] newKeys,
      final ELEMENT[] previousElements) {

    ELEMENT[] result = createNewElementArray(newKeys.length);

    for (int i = 0; i < newKeys.length; i++) {
      KEY newKey = newKeys[i];
      if ((previousElements == null) || (previousElements.length == 0)) {
        // The length of previousElements can be zero if the ServletHandler is freshly created
        result[i] = createNewElement(newKey);
      } else {
        Integer position = previousKeysWithPosition.get(newKey);
        if (position != null) {
          result[i] = previousElements[position];
        } else {
          result[i] = createNewElement(newKey);
        }
      }
    }

    return result;
  }

  /**
   * Updates the previous key array in the manager.
   *
   * @param keys
   *          The keys that are getting that are currently used in the ServletContext component (so
   *          next time they will be previous).
   */
  public void updatePrviousKeys(final KEY[] keys) {
    Map<KEY, Integer> updated = new HashMap<>(keys.length);
    for (int i = 0; i < keys.length; i++) {
      updated.put(keys[i], i);
    }
    previousKeysWithPosition = updated;
  }
}
