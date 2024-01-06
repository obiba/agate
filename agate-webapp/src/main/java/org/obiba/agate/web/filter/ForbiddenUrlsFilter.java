package org.obiba.agate.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters all forbidden URLs registered above (/.htaccess, /.htacces/)
 */
public class ForbiddenUrlsFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    httpResponse.reset();
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, String.format("%s not allowed", httpRequest.getRequestURI()));
  }

  @Override
  public void destroy() {
  }
}
