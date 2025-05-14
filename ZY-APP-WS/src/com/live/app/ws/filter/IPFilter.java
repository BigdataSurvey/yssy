package com.live.app.ws.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@WebFilter(asyncSupported=true, filterName="IPFilter", urlPatterns="/*")
public class IPFilter implements Filter{
	
	@Override
	public void init(FilterConfig fc) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		String ip = getIpAddress(request);
		request.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		request.getSession().setAttribute("ip", ip);
		filterChain.doFilter(req, res);
	}
	
	public void destroy() {
	}
	
	private final String getIpAddress(HttpServletRequest request)
			throws IOException {
		String ip = request.getHeader("X-Real-Ip");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("X-Forwarded-For");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}
		return ip;
	}
}