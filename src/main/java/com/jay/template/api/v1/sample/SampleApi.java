package com.jay.template.api.v1.sample;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import com.jay.template.api.v1.common.openapi.StandardErrorResponses;
import com.jay.template.api.v1.sample.model.SampleResponse;

@Tag(
        name = "Sample",
        description = "Endpoints used to demonstrate the template structure and conventions."
)
public interface SampleApi {

    @Operation(
            summary = "Sample endpoint",
            description = "Demonstrates API structure, request identity extraction, and OpenAPI documentation."
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SampleResponse.class)
                    )
            )
    })
    @GetMapping("/v1/sample")
    SampleResponse get();
}