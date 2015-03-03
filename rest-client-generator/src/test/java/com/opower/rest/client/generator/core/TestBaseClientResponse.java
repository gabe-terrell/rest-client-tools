package com.opower.rest.client.generator.core;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.Test;

import static com.opower.rest.client.generator.core.Client.DEFAULT_ERROR_STATUS_CRITERIA;
import static com.opower.rest.client.generator.core.Client.BAD_REQUEST;
import static com.opower.rest.client.generator.core.Client.NETWORK_CONNECT_TIMEOUT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author chris.phillips
 */
public class TestBaseClientResponse {

    private static final int SC_OK = 200;
    private static final int END_OF_REDIRECTION_RANGE = 399;

    /**
     * Verifies that the Client.DEFAULT_ERROR_STATUS_CRITERIA works as expected.
     */
    @Test
    public void defaultErrorStatusCriteria() {

        Predicate<Integer> t = new Predicate<Integer>() {
            @Override
            public boolean apply(Integer status) {
                return 404 != status && status >= BAD_REQUEST && status <= NETWORK_CONNECT_TIMEOUT;
            }
        };

        BaseClientResponse blah = new BaseClientResponse(null, t);
        blah.setStatus(404);
        blah.checkFailureStatus();

        BaseClientResponse response = new BaseClientResponse(null, DEFAULT_ERROR_STATUS_CRITERIA);
        for (int i = BAD_REQUEST; i <= NETWORK_CONNECT_TIMEOUT; i++) {
            try {
                response.setStatus(i);
                response.checkFailureStatus();
                fail();
            } catch(ClientResponseFailure clientResponseFailure) {}
        }

        for (int i = SC_OK; i <= END_OF_REDIRECTION_RANGE; i++) {
            try {
                response.setStatus(i);
                response.checkFailureStatus();
            } catch(ClientResponseFailure clientResponseFailure) {
                fail();
            }
        }
    }



    /**
     * When resetting the stream if an exception is thrown, it should be caught and resetStream should return false.
     */
    @Test
    public void exceptionOnResetStreamReturnsFalse() {

        BaseClientResponse response = new BaseClientResponse(new BaseClientResponse.BaseClientResponseStreamFactory() {
            @Override
            public InputStream getInputStream() throws IOException {
                // InputStream by default throws an exception when reset is called
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
            }

            @Override
            public void performReleaseConnection() {

            }
        }, DEFAULT_ERROR_STATUS_CRITERIA);

        assertThat(response.resetStream(), is(false));
    }

    /**
     * If the underlying stream is successfully reset, then resetStream should return true.
     */
    @Test
    public void successfulStreamResetReturnsTrue() {
        BaseClientResponse response = new BaseClientResponse(new BaseClientResponse.BaseClientResponseStreamFactory() {
            @Override
            public InputStream getInputStream() throws IOException {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }

                    @Override
                    public synchronized void reset() throws IOException {
                        // making sure this doesn't throw
                    }
                };
            }

            @Override
            public void performReleaseConnection() {

            }
        }, DEFAULT_ERROR_STATUS_CRITERIA);

        assertThat(response.resetStream(), is(true));
    }
}
