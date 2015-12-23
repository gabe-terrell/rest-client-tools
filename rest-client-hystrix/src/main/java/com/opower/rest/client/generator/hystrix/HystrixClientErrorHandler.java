package com.opower.rest.client.generator.hystrix;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.extractors.ClientErrorHandler;

import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opower.rest.client.generator.util.HttpResponseCodes.SC_BAD_REQUEST;

/**
 * Certain responses should be treated as HystrixBadRequestExceptions. By default only requests with a response
 * code of 400 will be handled this way. To change that behavior, just specify new criteria for any method
 * in your resource interface.
 *
 * @author chris.phillips
 */
public class HystrixClientErrorHandler implements ClientErrorHandler {

    static final BadRequestCriteria DEFAULT_BAD_REQUEST_CRITERIA = new BadRequestCriteria() {
        @Override
        public boolean apply(BaseClientResponse response, Exception exception) {
            return response != null && response.getStatus() == SC_BAD_REQUEST;
        }
    };

    private final ClientErrorHandler clientErrorHandler;
    private final Map<Method, ? extends BadRequestCriteria> badRequestCriteriaMap;

    /**
     * Creates an instance based on the provided ClientErrorHandler and bad request criteria.
     * @param badRequestCriteriaMap the bad request criteria to apply for each method
     * @param clientErrorHandler the ClientErrorHandler to wrap
     */
    public HystrixClientErrorHandler(Map<Method, ? extends BadRequestCriteria> badRequestCriteriaMap,
                                     ClientErrorHandler clientErrorHandler) {
        this.clientErrorHandler = checkNotNull(clientErrorHandler);
        this.badRequestCriteriaMap = checkNotNull(badRequestCriteriaMap);
    }

    @Override
    public void clientErrorHandling(Method method, BaseClientResponse clientResponse, RuntimeException e) {
        try {
            this.clientErrorHandler.clientErrorHandling(method, clientResponse, e);
            checkForBadRequest(method, clientResponse, e);
        } 
        catch (Exception ex) {
            checkForBadRequest(method, clientResponse, ex);
        }
    }

    private void checkForBadRequest(Method method, BaseClientResponse clientResponse, Exception ex) {
        BadRequestCriteria criteria;
        if (method != null) {
            criteria = Optional.fromNullable(this.badRequestCriteriaMap.get(method)).or(DEFAULT_BAD_REQUEST_CRITERIA);
        } 
        else {
            criteria = DEFAULT_BAD_REQUEST_CRITERIA;
        }

        if (criteria.apply(clientResponse, ex) || (clientResponse != null && clientResponse.isSuccessful())) {
            throw new HystrixBadRequestException("Bad Request", ex);
        } 
        else {
            Throwables.propagate(ex);
        }
    }

    /**
     * This method is for testing.
     * @return the map of method -> BadRequestCriteria
     */
    Map<Method, ? extends BadRequestCriteria> getCriteriaMap() {
        return this.badRequestCriteriaMap;
    }

    /**
     * Defines which responses should be NOT trigger the hystrix circuit breaker.
     */
    public interface BadRequestCriteria {
        /**
         * Check the response and Exception to determine whether or not to wrap the Exception in a
         * HystrixBadRequestException. When this method returns true, the Exception will be wrapped.
         * @param response the response to check
         * @param exception the exception to check
         * @return true if the Exception should be wrapped in a HystrixBadRequestException
         */
        boolean apply(BaseClientResponse response, Exception exception);
    }
}
