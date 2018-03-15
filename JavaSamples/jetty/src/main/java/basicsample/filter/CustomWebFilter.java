package basicsample.filter;

import javax.servlet.*;
import java.io.IOException;

public class CustomWebFilter implements Filter {
    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("FILTER");
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
