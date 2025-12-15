package com.jay.template.infra.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityContextHolderTest {

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void setContextNullClearsAndGetReturnsEmpty() {
        IdentityContextHolder.setContext(null);
        IdentityContextSnapshot context = IdentityContextHolder.getContext();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void setContextEmptyAndGetReturnsEmpty() {
        IdentityContextHolder.setContext(IdentityContextSnapshot.EMPTY);
        IdentityContextSnapshot context = IdentityContextHolder.getContext();
        assertSame(IdentityContextSnapshot.EMPTY, context);
    }

    @Test
    void setContextAndGetReturnsCopyOfContext() {
        Identity identity = new Identity("user-001", "req-001");
        IdentityContextSnapshot snapshot = IdentityContextSnapshot.of(identity);
        IdentityContextHolder.setContext(snapshot);

        IdentityContextSnapshot context = IdentityContextHolder.getContext();
        assertNotSame(IdentityContextSnapshot.EMPTY, context);
        assertNotSame(snapshot, context); //getContext returns a copy of snapshot
        assertEquals(identity, context.identity());
    }
}