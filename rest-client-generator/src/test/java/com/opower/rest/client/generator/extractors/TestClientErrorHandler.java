package com.opower.rest.client.generator.extractors;

import com.google.common.collect.ImmutableList;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.ClientErrorInterceptor;
import com.opower.rest.client.generator.core.ClientResponse;
import com.opower.rest.client.generator.core.ClientResponseFailure;
import com.opower.rest.client.generator.util.HttpResponseCodes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import java.lang.reflect.Method;

import static org.easymock.EasyMock.*;

/**
 * @author chris.phillips
 */
public class TestClientErrorHandler {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final Method method = this.getClass().getMethods()[0];
    /**
     * ClientResponseFailures should be passed on to the ClientErrorInterceptor chain. The chain in this test
     * will translate the ClientResponseFailure into an ExceptionFromHandler instance.
     */
    @Test
    public void clientResponseFailuresAreIntercepted() {
        thrown.expect(ExceptionFromHandler.class);

        ClientErrorInterceptor mockInterceptor = createMock(ClientErrorInterceptor.class);
        mockInterceptor.handle(anyObject(ClientResponse.class));
        expectLastCall().andThrow(new ExceptionFromHandler());

        BaseClientResponse.BaseClientResponseStreamFactory streamFactory =
                createMock(BaseClientResponse.BaseClientResponseStreamFactory.class);
        ClientErrorHandler errorHandler = new DefaultClientErrorHandler(ImmutableList.of(mockInterceptor));
        BaseClientResponse mockResponse = createMock(BaseClientResponse.class);
        expect(mockResponse.getStreamFactory()).andReturn(streamFactory);
        replay(mockInterceptor, mockResponse);
        errorHandler.clientErrorHandling(method, mockResponse, createMock(ClientResponseFailure.class));
    }

    /**
     * Exceptions that are not of type ClientResponseFailure should just be rethrown. They don't indicate an
     * error response from the server, but an error trying to parse the response from the server (error response or otherwise)
     *
     */
    @Test
    public void nonClientResponseFailuresAreReThrown() {
        thrown.expect(UnhandledException.class);

        ClientErrorHandler errorHandler = new DefaultClientErrorHandler(ImmutableList.<ClientErrorInterceptor>of());
        BaseClientResponse mockResponse = createMock(BaseClientResponse.class);
        expect(mockResponse.getStatus()).andReturn(HttpResponseCodes.SC_OK);
        replay(mockResponse);
        errorHandler.clientErrorHandling(method, mockResponse, new UnhandledException());
    }

    private class ExceptionFromHandler extends RuntimeException {}

    private class UnhandledException extends RuntimeException {}
}
