package com.zywl.app.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "CrossOriginFilter",urlPatterns = "/*",asyncSupported = true)
public class CrossOrignFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        HttpServletResponse response1 = (HttpServletResponse) response;
        response1.setHeader("Access-Control-Allow-Origin", "*");
        response1.setHeader("Access-Control-Allow-Methods", "*");
        response1.setHeader("Access-Control-Max-Age", "3600");
        response1.setHeader("Access-Control-Allow-Headers", "*");
        response1.setHeader("Access-Control-Allow-Credentials", "true");
        chain.doFilter(request, response1);

    }

    @Override
    public void destroy() {

    }
}
