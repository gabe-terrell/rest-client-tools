package com.opower.rest.client.generator.hystrix;

import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.Client;
import com.opower.rest.client.generator.core.ClientResponseFailure;
import com.opower.rest.client.generator.extractors.ClientErrorHandler;
import com.opower.rest.client.generator.hystrix.HystrixClientErrorHandler.BadRequestCriteria;
import com.opower.rest.client.generator.util.HttpResponseCodes;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for the HystrixClientErrorHandler.
 * @author chris.phillips
 */
public class TestHystrixClientErrorHandler {

    private static final String CAUSE = "cause";

    private ClientErrorHandler clientErrorHandler = createMock(ClientErrorHandler.class);

    @SuppressWarnings("unchecked")
    private HystrixClientErrorHandler errorHandler = new HystrixClientErrorHandler(EMPTY_MAP, this.clientErrorHandler);

    private final Method method = TestHystrixClientErrorHandler.class.getMethods()[0];
    private final Map<Method, ? extends BadRequestCriteria> criteriaMap 
            = ImmutableMap.of(this.method, new BadRequestCriteria() {
        @Override
        public boolean apply(BaseClientResponse response, Exception exception) {
            return response.getStatus() == HttpResponseCodes.SC_NOT_FOUND;
        }
    });

    private void prepareHandler(BaseClientResponse response, RuntimeException ex, RuntimeException toThrow) {
        this.clientErrorHandler.clientErrorHandling(this.method, response, ex);
        expectLastCall().andThrow(toThrow);
        replay(this.clientErrorHandler);
    }

    @Test
    public void defaultBadRequestNoInterceptors() {
        RuntimeException cause = new RuntimeException(CAUSE);
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_BAD_REQUEST);
        prepareHandler(response, cause, cause);
        ensureBadRequestException(this.errorHandler, response, cause);
    }

    @Test(expected = ClientResponseFailure.class)
    public void defaultFailedRequestNoInterceptors() {
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_INTERNAL_SERVER_ERROR);
        ClientResponseFailure cause = new ClientResponseFailure(response);
        prepareHandler(response, cause, cause);
        ensureFailure(this.errorHandler, response, cause);
    }

    @Test(expected = IllegalStateException.class)
    public void defaultNullResponse() {
        IllegalStateException cause = new IllegalStateException(CAUSE);
        prepareHandler(null, cause, cause);
        ensureFailure(this.errorHandler, null, cause);
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultFailedRequestInterceptorThrows() {
        @SuppressWarnings("unchecked")
        HystrixClientErrorHandler hystrixClientErrorHandler = new HystrixClientErrorHandler(EMPTY_MAP, this.clientErrorHandler);
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_INTERNAL_SERVER_ERROR);
        ClientResponseFailure cause = new ClientResponseFailure(response);
        prepareHandler(response, cause, new IllegalArgumentException());
        ensureFailure(hystrixClientErrorHandler, response, cause);
    }

    @Test(expected = ClientResponseFailure.class)
    public void customFailedRequestNoInterceptor() {
        @SuppressWarnings("unchecked")
        HystrixClientErrorHandler hystrixClientErrorHandler = new HystrixClientErrorHandler(this.criteriaMap,
                                                                                            this.clientErrorHandler);
        // response that doesn't match the configured criteria
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_INTERNAL_SERVER_ERROR);
        ClientResponseFailure cause = new ClientResponseFailure(response);
        prepareHandler(response, cause, cause);
        ensureFailure(hystrixClientErrorHandler, response, cause);
    }

    @Test(expected = IllegalArgumentException.class)
    public void customFailedRequestInterceptorThrows() {
        HystrixClientErrorHandler hystrixClientErrorHandler = new HystrixClientErrorHandler(this.criteriaMap,
                                                                                            this.clientErrorHandler);
        // response that doesn't match the configured criteria
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_INTERNAL_SERVER_ERROR);
        ClientResponseFailure cause = new ClientResponseFailure(response);
        prepareHandler(response, cause, new IllegalArgumentException());
        ensureFailure(hystrixClientErrorHandler, response, cause);
    }

    @Test
    public void customBadRequestInterceptorThrows() {
        HystrixClientErrorHandler hystrixClientErrorHandler = new HystrixClientErrorHandler(this.criteriaMap,
                                                                                            this.clientErrorHandler);
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_NOT_FOUND);
        ClientResponseFailure cause = new ClientResponseFailure(CAUSE, response);
        prepareHandler(response, cause, new IllegalArgumentException(CAUSE));
        ensureBadRequestException(hystrixClientErrorHandler, response, cause);
    }

    @Test
    public void customBadRequestNoInterceptors() {
        @SuppressWarnings("unchecked")
        HystrixClientErrorHandler hystrixClientErrorHandler = new HystrixClientErrorHandler(this.criteriaMap,
                                                                                            this.clientErrorHandler);
        BaseClientResponse response = dummyResponse(HttpResponseCodes.SC_NOT_FOUND);
        ClientResponseFailure cause = new ClientResponseFailure(CAUSE, response);
        prepareHandler(response, cause, cause);
        ensureBadRequestException(hystrixClientErrorHandler, response, cause);
    }

    private void ensureBadRequestException(HystrixClientErrorHandler hystrixClientErrorHandler, BaseClientResponse response,
                                           RuntimeException cause) {
        try {
            hystrixClientErrorHandler.clientErrorHandling(this.method, response, cause);
            fail();
        } 
        catch (HystrixBadRequestException ex) {
            assertThat(ex.getCause().getMessage(), is(CAUSE));
        }
    }

    private void ensureFailure(HystrixClientErrorHandler hystrixClientErrorHandler, BaseClientResponse response,
                               RuntimeException cause) {
        try {
            hystrixClientErrorHandler.clientErrorHandling(this.method, response, cause);
            fail();
        } 
        catch (HystrixBadRequestException ex) {
            fail();
        }
    }

    private BaseClientResponse dummyResponse(int status) {
        BaseClientResponse clientResponse = new BaseClientResponse(new BaseClientResponse.BaseClientResponseStreamFactory() {
            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void performReleaseConnection() {

            }
        }, Client.DEFAULT_ERROR_STATUS_CRITERIA);
        clientResponse.setStatus(status);
        return clientResponse;
    }
}
