package com.zywl.app.manager.filter;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName = "CrossOriginFilter",urlPatterns = "/*",asyncSupported = true)
public class CrossOrignFilter  implements Filter {

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
