package com.opower.rest.test;

import javax.ws.rs.core.Response;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used for testing error handling logic with an ExpectedException Rule.
 * @author chris.phillips
 */
public class StatusCodeMatcher extends BaseMatcher<StatusCodeMatcher.ResponseError> {
    private final int expectedCode;

    /**
     * Creates an instance that expects the given status code.
     * @param expectedCode the expected status code.
     */
    public StatusCodeMatcher(int expectedCode) {
        this.expectedCode = expectedCode;
    }

    @Override
    public boolean matches(Object error) {
        return getValue(error) == this.expectedCode;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(this.expectedCode);
    }

    @Override
    public void describeMismatch(Object error, Description description) {
        description.appendText("was ").appendValue(getValue(error));
    }

    private int getValue(Object error) {
        return ((ResponseError) checkNotNull(error)).getClientResponse().getStatus();
    }

    /**
     * A class that wraps up the Response without having to worry about the baggage of BaseClientResponse.
     */
    public static class ResponseError extends RuntimeException {
        private final Response clientResponse;

        /**
         * Create a ResponseError to hold the given Response instance.
         * @param clientResponse the Response to use
         */
        public ResponseError(Response clientResponse) {
            this.clientResponse = checkNotNull(clientResponse);
        }

        public Response getClientResponse() {
            return this.clientResponse;
        }
    }
}
