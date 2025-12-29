package com.jay.template.app.dependency;

import java.util.function.Supplier;

import com.jay.template.core.error.api.ApiException;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.core.error.dependency.DependencyCallException;

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