package com.xunkutech.base.app.filter;

import com.xunkutech.base.app.context.AppContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContextAwareFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ContextAwareFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Enter ContextAwareFilter");
        AppContextHolder.resetAppContext();
        filterChain.doFilter(request, response);
        logger.debug("Exit ContextAwareFilter");
        AppContextHolder.resetAppContext();
    }
}
