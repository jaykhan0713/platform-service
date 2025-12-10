package com.jay.template.infra.logging;

/**
 * Marker class used only to define the logger category for
 * structured, per-request metadata logs.
 * <p>
 * All "one log per request" entries should log using this category
 * so they are easy to find, filter, and route.
 */
public final class MetaDataLogger {
    private MetaDataLogger() {};
}
