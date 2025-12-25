package com.jay.template.web.servlet.filter;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.identity.IdentityProperties;
import com.jay.template.infra.logging.MdcProperties;
import com.jay.template.web.servlet.support.ErrorResponseWriter;

@Configuration
class FilterConfig {

    private static final String API_WILDCARD = "/api/*";

    @Bean
    public FilterRegistrationBean<IdentityFilter> identityFilter(IdentityProperties identityProps) {
        FilterRegistrationBean<IdentityFilter> registration = new FilterRegistrationBean<>();
        IdentityFilter identityFilter = new IdentityFilter(identityProps);

        registration.setFilter(identityFilter);
        registration.setOrder(FilterOrders.IDENTITY.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcFilter> mdcFilter(MdcProperties mdcProps) {
        FilterRegistrationBean<MdcFilter> registration = new FilterRegistrationBean<>();
        MdcFilter mdcFilter = new MdcFilter(mdcProps);

        registration.setFilter(mdcFilter);
        registration.setOrder(FilterOrders.MDC.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<BulkheadFilter> bulkheadFilter(
        BulkheadRegistry bulkheadRegistry,
        ErrorResponseWriter errorResponseWriter
    ) {
        FilterRegistrationBean<BulkheadFilter> registration = new FilterRegistrationBean<>();
        String instanceName = BulkheadFilter.BULKHEAD_INSTANCE_NAME;
        BulkheadFilter bulkheadFilter = new BulkheadFilter(
                bulkheadRegistry.bulkhead(instanceName),
                errorResponseWriter
        );

        registration.setFilter(bulkheadFilter);
        registration.setOrder(FilterOrders.BULKHEAD.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    private enum FilterOrders {
        IDENTITY(0),
        MDC(1),
        BULKHEAD(2);

        private final int order;

        FilterOrders(int order) {
            this.order = order;
        }

        public int order() {
            return order;
        }
    }
}
