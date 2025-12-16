package com.jay.template.api.v1.common.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import com.jay.template.api.v1.common.error.ErrorResponse;

/**
 * Declares the standard error responses shared by all v1 API endpoints.
 *
 * <p>This annotation centralizes common HTTP error documentation (currently
 * {@code 400} and {@code 500}) so individual API methods only need to document
 * success responses and endpoint-specific errors (for example {@code 404}).</p>
 *
 * <p>It is intended to be applied to OpenAPI contract interfaces or individual
 * endpoint methods and has no effect on runtime request handling.</p>
 *
 * <p>{@link RetentionPolicy#RUNTIME} is required so OpenAPI tooling can
 * discover and expand the composed {@link ApiResponses} via reflection
 * when generating {@code /v3/api-docs} and Swagger UI.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        )
})
public @interface StandardErrorResponses {}
