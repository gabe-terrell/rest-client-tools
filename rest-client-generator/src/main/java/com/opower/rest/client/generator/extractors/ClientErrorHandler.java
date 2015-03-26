package com.opower.rest.client.generator.extractors;

import com.opower.rest.client.generator.core.BaseClientResponse;

import java.lang.reflect.Method;

/**
 * Implementations of this class handle the various errors both client and server side that can occur as a result of
 * making REST calls.
 * @author chris.phillips
 */
public interface ClientErrorHandler {
    void clientErrorHandling(Method method, BaseClientResponse clientResponse, RuntimeException e);
}
