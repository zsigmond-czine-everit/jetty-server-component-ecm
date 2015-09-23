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

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

/**
 * Customized implementation of {@link ServletHandler} to support dynamic updates of servlets and
 * filters.
 */
public class CustomServletHandler extends ServletHandler {

  private boolean ignoreUpdateMapping = false;

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  @Override
  public void doHandle(final String target, final Request baseRequest,
      final HttpServletRequest request,
      final HttpServletResponse response) throws IOException, ServletException {
    ReadLock readLock = readWriteLock.readLock();
    readLock.lock();
    try {
      super.doHandle(target, baseRequest, request, response);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void doScope(final String target, final Request baseRequest,
      final HttpServletRequest request,
      final HttpServletResponse response) throws IOException, ServletException {
    ReadLock readLock = readWriteLock.readLock();
    readLock.lock();
    try {
      super.doScope(target, baseRequest, request, response);
    } finally {
      readLock.unlock();
    }
  }

  @Override
  protected synchronized void updateMappings() {
    if (!ignoreUpdateMapping) {
      super.updateMappings();
    }
  }

  @Override
  protected synchronized void updateNameMappings() {
    if (!ignoreUpdateMapping) {
      super.updateNameMappings();
    }
  }

  /**
   * Updates servlets and filters with their mappings atomically. No requests are processed during
   * the update process so no request will fail due to inconsistent state.
   */
  public void updateServletsAndFilters(final ServletHolder[] servletHolders,
      final ServletMapping[] servletMappings, final FilterHolder[] filterHolders,
      final FilterMapping[] filterMappings) {
    WriteLock writeLock = readWriteLock.writeLock();
    writeLock.lock();
    try {
      ignoreUpdateMapping = true;
      setServlets(servletHolders);
      setServletMappings(servletMappings);
      setFilters(filterHolders);

      if (isStarted()) {
        for (ServletHolder servletHolder : servletHolders) {
          manage(servletHolder);
        }
        for (FilterHolder filterHolder : filterHolders) {
          manage(filterHolder);
        }
      }
      setFilterMappings(filterMappings);
      ignoreUpdateMapping = false;
      if (isStarted()) {
        updateNameMappings();
        updateMappings();
      }
    } finally {
      try {
        ignoreUpdateMapping = false;
      } finally {
        writeLock.unlock();
      }
    }
  }
}
