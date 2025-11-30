package org.obiba.agate.web.rest.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.obiba.agate.upgrade.RuntimeVersionProvider;

import java.io.IOException;

@Provider
public class VersionFilter implements ContainerResponseFilter {

  private final RuntimeVersionProvider runtimeVersionProvider;

  @Inject
  public VersionFilter(RuntimeVersionProvider versionProvider) {
    this.runtimeVersionProvider = versionProvider;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    responseContext.getHeaders().add("X-Agate-Version", runtimeVersionProvider.getVersion());
  }
}
