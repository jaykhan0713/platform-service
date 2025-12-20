package com.jay.template.web.filter;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.infra.identity.IdentityProperties;
import com.jay.template.infra.logging.MdcProperties;
import com.jay.template.web.error.ErrorResponseFactory;

@Configuration
class FilterConfig {

    private static final String API_WILDCARD = "/api/*";

    @Bean
    public FilterRegistrationBean<IdentityFilter> identityFilter(
            IdentityProperties identityProps
    ) {
        FilterRegistrationBean<IdentityFilter> registration = new FilterRegistrationBean<>();
        IdentityFilter identityFilter = new IdentityFilter(identityProps);

        registration.setFilter(identityFilter);
        registration.setOrder(FilterOrders.IDENTITY.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcFilter>  mdcFilter(
            IdentityProperties identityProps,
            MdcProperties mdcProps
    ) {
        FilterRegistrationBean<MdcFilter> registration = new FilterRegistrationBean<>();
        MdcFilter mdcFilter = new MdcFilter(identityProps, mdcProps);

        registration.setFilter(mdcFilter);
        registration.setOrder(FilterOrders.MDC.order());
        registration.addUrlPatterns(API_WILDCARD);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<BulkheadFilter> bulkheadFilter(
        BulkheadRegistry bulkheadRegistry,
        ErrorResponseFactory errorResponseFactory,
        ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<BulkheadFilter> registration = new FilterRegistrationBean<>();
        BulkheadFilter bulkheadFilter = new BulkheadFilter(
                bulkheadRegistry.bulkhead("webInboundFilter"), errorResponseFactory, objectMapper
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
