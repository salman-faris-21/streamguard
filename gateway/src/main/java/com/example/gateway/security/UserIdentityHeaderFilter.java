package com.example.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class UserIdentityHeaderFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String identity = authenticatedIdentity();
        filterChain.doFilter(new IdentityRequestWrapper(request, identity), response);
    }

    private String authenticatedIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    private static final class IdentityRequestWrapper extends HttpServletRequestWrapper {

        private final String identity;

        private IdentityRequestWrapper(HttpServletRequest request, String identity) {
            super(request);
            this.identity = identity;
        }

        private boolean isIdentityHeader(String name) {
            return USER_ID_HEADER.equalsIgnoreCase(name);
        }

        @Override
        public String getHeader(String name) {
            if (isIdentityHeader(name)) {
                return identity;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isIdentityHeader(name)) {
                if (identity == null) {
                    return Collections.emptyEnumeration();
                }
                return Collections.enumeration(List.of(identity));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = new ArrayList<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                String name = original.nextElement();
                if (!isIdentityHeader(name)) {
                    names.add(name);
                }
            }
            if (identity != null) {
                names.add(USER_ID_HEADER);
            }
            return Collections.enumeration(names);
        }
    }
}
