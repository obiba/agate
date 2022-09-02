package org.obiba.agate.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
