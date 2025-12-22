package com.jay.template.web.servlet.filter;

import java.io.IOException;
import jakarta.servlet.ServletException;

import io.github.resilience4j.bulkhead.Bulkhead;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.jay.template.web.servlet.support.ErrorResponseWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BulkheadFilterTest {

    @Test
    void acquiresPermitThenReleasesPermit() throws ServletException, IOException {
        Bulkhead bulkhead = Mockito.mock(Bulkhead.class);
        when(bulkhead.tryAcquirePermission()).thenReturn(true);

        ErrorResponseWriter errorResponseWriter = Mockito.mock(ErrorResponseWriter.class);

        BulkheadFilter filter = new BulkheadFilter(bulkhead, errorResponseWriter);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);
    }

}