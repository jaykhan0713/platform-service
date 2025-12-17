package com.jay.template.smoke;

import com.jay.template.app.smoke.ping.contract.inbound.PingResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PingSmokeTest {

    private static MockWebServer mockWebServer;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopMockServer() throws Exception {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void overrideOutboundBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.profiles.active", () -> "smoke");
        registry.add("app.outbound.http.clients.ping.base-url",
                () -> mockWebServer.url("/").toString());
    }

    @Test
    void smokePingCallsDownstreamAndPropagatesTraceHeaders() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"msg\":\"pong\"}"));

        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        PingResponse response = client.get()
                .uri("/smoke/ping")
                .header("x-user-id", "smoke-user-001")
                .header("x-request-id", "smoke-request-001")
                .retrieve()
                .body(PingResponse.class);

        assertNotNull(response);
        assertTrue(response.ok());
        assertEquals("pong", response.msg());

        RecordedRequest req = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(req, "Downstream did not receive request");

        assertEquals("GET", req.getMethod());
        assertEquals("/outbound/ping", req.getPath());

        String traceparent = req.getHeader("traceparent");
        assertNotNull(traceparent, "Missing traceparent header on outbound request");

        // If you expect identity propagation for ping client, assert these too:
        // assertNotNull(req.getHeader("x-user-id"));
        // assertNotNull(req.getHeader("x-request-id"));
    }
}
