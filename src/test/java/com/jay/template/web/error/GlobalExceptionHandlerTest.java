package com.jay.template.web.error;

import static org.junit.jupiter.api.Assertions.*;

import com.jay.template.logging.mdc.MdcRetriever;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleGenericException() {
        String message = "generic error";
        Exception ex = new Exception(message);
        String traceId = "trace-001";

        MdcRetriever mdcRetriever = Mockito.mock(MdcRetriever.class);
        Mockito.when(mdcRetriever.getGatewayTraceId()).thenReturn(traceId);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mdcRetriever);
        ResponseEntity<ErrorResponse> entity = handler.handleGenericException(ex);

        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getStatus(), entity.getStatusCode());
        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(traceId, body.gatewayTraceId());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getDefaultMessage(), body.message());
        assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getCode(), body.code());
    }

    @Test
    public void testHandleApiException() {
        String message = "api error";
        ApiException ex = new ApiException(ErrorType.BAD_REQUEST, message);
        String traceId = "trace-001";

        MdcRetriever mdcRetriever = Mockito.mock(MdcRetriever.class);
        Mockito.when(mdcRetriever.getGatewayTraceId()).thenReturn(traceId);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mdcRetriever);
        ResponseEntity<ErrorResponse> entity = handler.handleApiException(ex);

        assertEquals(ErrorType.BAD_REQUEST.getStatus(), entity.getStatusCode());
        ErrorResponse body = entity.getBody();
        assertNotNull(body);
        assertEquals(traceId, body.gatewayTraceId());
        assertEquals(message, body.message());
        assertEquals(ErrorType.BAD_REQUEST.getCode(), body.code());
    }
}