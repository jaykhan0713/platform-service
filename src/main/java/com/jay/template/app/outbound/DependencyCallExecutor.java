package com.jay.template.app.outbound;

import java.util.function.Supplier;

import com.jay.template.app.error.ApiException;
import com.jay.template.app.error.ErrorType;
import com.jay.template.infra.outbound.error.DependencyCallException;

final class DependencyCallExecutor {

    static <T> T execute(Supplier<T> call, ErrorType errorType) {
        try {
            return call.get();
        } catch (DependencyCallException ex) {
            throw new ApiException(errorType, ex);
        }
    }

    private DependencyCallExecutor() {}
}