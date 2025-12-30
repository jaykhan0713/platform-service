package com.jay.template.infra.outbound.http.client.rest.error;

import java.util.function.Supplier;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.jay.template.core.error.dependency.DependencyCallException;
import com.jay.template.core.error.dependency.Reason;

public final class RestClientExceptionTranslator {

    private RestClientExceptionTranslator() {}

    public static <T> T execute(
            Supplier<T> supplier,
            String clientName
    ) {
        try {
            return supplier.get();
        } catch (ResourceAccessException ex) {
            /*TODO: handle wrapped IO exceptions for other http client adapters if used in the future
             * ResourceAccessException is Spring's IOException contract for RestClient
             */
            //IO Exceptions like ConnectException, SocketException, DNS/handshake/connection refused etc.
            throw new DependencyCallException(clientName, Reason.IO_ERROR, ex);
        } catch (BulkheadFullException ex) {
            throw new DependencyCallException(clientName, Reason.CAPACITY_REJECTED, ex);
        } catch (CallNotPermittedException ex) {
            throw new DependencyCallException(clientName, Reason.SHORT_CIRCUITED, ex);
        }
    }

    public static RestClient.ResponseSpec applyDefaultOnStatusHandlers(
            RestClient.ResponseSpec spec,
            String clientName
    ) {
        return spec
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (_, _) -> {
                            throw new DependencyCallException(clientName, Reason.RESPONSE_CLIENT_ERROR);
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (_, _) -> {
                            throw new DependencyCallException(clientName, Reason.RESPONSE_SERVER_ERROR);
                        }
                );
    }
}
