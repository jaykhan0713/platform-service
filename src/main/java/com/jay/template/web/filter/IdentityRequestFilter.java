package com.jay.template.web.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.jay.template.infra.identity.IdentityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jay.template.infra.identity.Identity;
import com.jay.template.infra.identity.IdentityContextHolder;
import com.jay.template.infra.identity.IdentityContextSnapshot;

/**
 * Binds identity identity to the current thread for the duration of a single HTTP identity.
 *
 * <p>
 * {@code IdentityRequestFilter} extracts identity metadata from configured inbound headers
 * and stores it in {@link IdentityContextHolder} so downstream code can access a stable,
 * immutable {@link Identity} during identity processing.
 * </p>
 *
 * <p>
 * The identity context is cleared in a {@code finally} block to prevent leaking identity
 * state across thread reuse.
 * </p>
 */
@Component
public class IdentityRequestFilter extends OncePerRequestFilter {

    private final IdentityProperties.Http.Headers headerKeys;

    public IdentityRequestFilter(IdentityProperties props) {
        this.headerKeys = props.http().headers();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(headerKeys.userId());
        String requestId = request.getHeader(headerKeys.requestId());

        Identity identity = new Identity(userId, requestId);

        try {
            IdentityContextHolder.setContext(IdentityContextSnapshot.of(identity));
            filterChain.doFilter(request, response);
        } finally {
            IdentityContextHolder.clear();
        }
    }
}
