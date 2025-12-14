package com.jay.template.infra.concurrent;

import com.jay.template.infra.request.IdentityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityContextPropagatorTest {

    private final IdentityContextPropagator propagator = new IdentityContextPropagator();

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void runnablePropagateAppliesCurrentToCaptured() {
        //new Identity
        //IdentityContextHolder.setContext();
    }


}