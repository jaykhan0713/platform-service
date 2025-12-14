package com.jay.template.web.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.jay.template.infra.request.Identity;
import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;
import com.jay.template.web.request.HttpProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class IdentityRequestFilter extends OncePerRequestFilter {

    private final HttpProperties httpProps;

    public IdentityRequestFilter(HttpProperties httpProps) {
        this.httpProps = httpProps;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(httpProps.headers().userId());
        String requestId = request.getHeader(httpProps.headers().requestId());

        Identity identity = new Identity(userId, requestId);

        try {
            IdentityContextHolder.setContext(new IdentityContextSnapshot(identity));
        } finally {
            IdentityContextHolder.clear();
        }

    }
}
