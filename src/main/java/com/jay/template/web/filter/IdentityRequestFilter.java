package com.jay.template.web.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.infra.request.Identity;
import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;
import com.jay.template.web.request.HttpProperties;

/**
 * Binds request identity to the current thread for the duration of a single HTTP request.
 *
 * <p>
 * {@code IdentityRequestFilter} extracts identity metadata from configured inbound headers
 * and stores it in {@link IdentityContextHolder} so downstream code can access a stable,
 * immutable {@link Identity} during request processing.
 * </p>
 *
 * <p>
 * The identity context is cleared in a {@code finally} block to prevent leaking request
 * state across thread reuse.
 * </p>
 */
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
            IdentityContextHolder.setContext(IdentityContextSnapshot.of(identity));
            filterChain.doFilter(request, response);
        } finally {
            IdentityContextHolder.clear();
        }
    }
}
