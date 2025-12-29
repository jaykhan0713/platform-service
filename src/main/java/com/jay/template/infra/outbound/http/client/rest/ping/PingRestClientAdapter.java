package com.jay.template.infra.outbound.http.client.rest.ping;

import com.jay.template.core.domain.dependency.ping.PingResult;
import com.jay.template.core.port.dependency.ping.PingDependency;
import org.springframework.web.client.RestClient;

import com.jay.template.infra.outbound.http.client.rest.ping.contract.DownstreamPingResponse;

public class PingRestClientAdapter implements PingDependency {

    private final RestClient restClient;
    private final String uri;
    private final DownstreamPingResponseMapper dtoMapper;

    public PingRestClientAdapter(
            RestClient restClient,
            String uri,
            DownstreamPingResponseMapper dtoMapper
    ) {
        this.restClient = restClient;
        this.uri = uri;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public PingResult ping() {
        DownstreamPingResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(DownstreamPingResponse.class);

        /*TODO: handle when response has 4xx/5xx via onStatus()
         *
         * currently no error DTO for this path, so spring just throws RestClientResponseException
         * which is surfaced up to a GlobalExceptionHandler as a generic exception.
         */

        return dtoMapper.map(response);
    }
}
