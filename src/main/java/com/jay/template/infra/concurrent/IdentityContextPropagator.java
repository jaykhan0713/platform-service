package com.jay.template.infra.concurrent;

import java.util.concurrent.Callable;

import com.jay.template.infra.request.IdentityContextHolder;
import com.jay.template.infra.request.IdentityContextSnapshot;

final class IdentityContextPropagator implements ContextPropagator {

    @Override
    public Runnable propagate(Runnable task) {
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.getContext();

            try {
                apply(captured);
                task.run();
            } finally {
                apply(previous);
            }
        };
    }

    @Override
    public <T> Callable<T> propagate(Callable<T> task) {
        IdentityContextSnapshot captured = IdentityContextHolder.getContext();
        return () -> {
            IdentityContextSnapshot previous = IdentityContextHolder.getContext();

            try {
                apply(captured);
                return task.call();
            } finally {
                apply(previous);
            }
        };
    }

    private void apply(IdentityContextSnapshot snapshot) {
        if (snapshot == IdentityContextSnapshot.EMPTY) { //sentinel instance check
            IdentityContextHolder.clear();
        } else {
            IdentityContextHolder.setContext(snapshot);
        }
    }
}
