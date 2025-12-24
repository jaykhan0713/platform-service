package com.jay.template.smoke;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.app.smoke.outbound.ping.contract.DownstreamPingResponse;
import com.jay.template.app.smoke.api.v1.smoke.model.SmokeResponse;
import com.jay.template.common.FunctionalTestBase;
import com.jay.template.common.SpringBootTestShared;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.MediaType.APPLICATION_JSON;

@SpringBootTestShared
public class PingSmokeTest extends FunctionalTestBase {

    private final TestRestTemplate restTemplate;

    public PingSmokeTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate; //automatically configured to talk to spring instance url
    }


    @Test
    void smokePingCallsDownstreamAndPropagatesTraceAndIdentityHeaders() throws Exception {

        //the mocked response that the downstream mock server responds with
        DownstreamPingResponse downstreamResponse = new DownstreamPingResponse("pong");
        ObjectMapper mapper = new ObjectMapper();
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .setBody(mapper.writeValueAsString(downstreamResponse)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-user-id", "smoke-user-001");
        headers.set("x-request-id", "smoke-request-001");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        //request the app server
        ResponseEntity<SmokeResponse> response =
                restTemplate.exchange(
                        "/api/smoke",
                        HttpMethod.GET,
                        entity,
                        SmokeResponse.class
                );

        SmokeResponse body = response.getBody();

        assertNotNull(body);
        assertTrue(body.ok());
        assertEquals("pong", body.msg());

        //the request that went to downstream mock server from spring app
        RecordedRequest req = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(req, "Downstream did not receive request");

        assertEquals("GET", req.getMethod());
        assertEquals("/api/v1/ping", req.getPath());

        String traceparent = req.getHeader("traceparent");
        assertNotNull(traceparent, "Missing traceparent header on ping request");

        String userId = req.getHeader("x-user-id");
        assertNotNull(userId, "Missing x-user-id header on ping request");

        String requestId = req.getHeader("x-request-id");
        assertNotNull(requestId, "Missing x-request-id header on ping request");
    }
}
