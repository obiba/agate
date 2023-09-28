package org.obiba.agate.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * When a TRACE request is received, returns a Forbidden response.
 */
public class NoTraceFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ("TRACE".equals(httpRequest.getMethod())) {
      httpResponse.reset();
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "TRACE method not allowed");
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }
}
