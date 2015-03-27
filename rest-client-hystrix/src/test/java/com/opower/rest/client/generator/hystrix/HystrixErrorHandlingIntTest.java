package com.opower.rest.client.generator.hystrix;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.ImmutableList;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;
import com.opower.rest.client.ConfigurationCallback;
import com.opower.rest.client.generator.core.ClientErrorInterceptor;
import com.opower.rest.client.generator.core.ClientResponse;
import com.opower.rest.client.generator.core.ResourceInterface;
import com.opower.rest.client.generator.core.SimpleUriProvider;
import com.opower.rest.client.generator.core.UriProvider;
import com.opower.rest.client.generator.executors.ApacheHttpClient4Executor;
import com.opower.rest.client.generator.util.HttpResponseCodes;
import com.opower.rest.test.StatusCodeMatcher;
import com.opower.rest.test.jetty.JettyRule;
import com.opower.rest.test.resource.FrobResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.opower.rest.client.generator.hystrix.TestHystrixClientBuilder.GROUP_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author chris.phillips
 */
public class HystrixErrorHandlingIntTest {

    private static int PORT = 7999;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setDateFormat(new ISO8601DateFormat())
            .registerModule(new GuavaModule())
            .registerModule(new JodaModule());
    private static final JacksonJsonProvider JACKSON_JSON_PROVIDER = new JacksonJsonProvider(OBJECT_MAPPER)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // enabling this feature to make deserialization fail
            .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

    @ClassRule
    public static final JettyRule JETTY_RULE =
            new JettyRule(PORT, HystrixErrorHandlingIntTest.class.getResource("/jersey/1/web.xml").toString());

    private static final UriProvider URI_PROVIDER = new SimpleUriProvider(String.format("http://localhost:%s/", PORT));
    private static final ResourceInterface<FrobResource> RESOURCE_INTERFACE = new ResourceInterface<>(FrobResource.class);

    private FrobResource frobResource = new HystrixClient.Builder<FrobResource>(RESOURCE_INTERFACE, URI_PROVIDER, GROUP_KEY)
                                            .clientErrorInterceptors(ImmutableList
                                                    .<ClientErrorInterceptor>of(new TestClientErrorInterceptor()))
            .commandProperties(new ConfigurationCallback<HystrixCommandProperties.Setter>() {
                @Override
                public void configure(HystrixCommandProperties.Setter setter) {
                    setter.withExecutionIsolationThreadTimeoutInMilliseconds(10000);
                }
            })
                                            .executor(new ApacheHttpClient4Executor())
                                            .registerProviderInstance(JACKSON_JSON_PROVIDER).build();

    /**
     * Exceptions on the client side, even despite a successful http response, should always be propagated
     * and shouldn't count towards circuit-breaker
     * @throws Exception
     */
    @Test
    public void nonClientResponseFailureThrowsOriginalException() throws Exception {
        Method frobJsonErrorMethod = FrobResource.class.getMethod("frobJsonError");
        try {
            frobResource.frobJsonError();
            fail();
        } catch(RuntimeException ex) {
            assertTrue(ex.getCause() instanceof JsonMappingException);
            HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixClient.keyForMethod(frobJsonErrorMethod));
            assertThat(metrics.getCumulativeCount(HystrixRollingNumberEvent.FAILURE), is(0L));
            assertThat(metrics.getCumulativeCount(HystrixRollingNumberEvent.EXCEPTION_THROWN), is(1L));
        }
    }

    /**
     * Ensures that the HystrixClientErrorHandler is properly wrapping exceptions in HystrixBadRequestExceptions. This is
     * verified by checking the failure stats for the HystrixCommand.
     * @throws Exception
     */
    @Test
    public void serverResponseErrors() throws Exception {
        Method frobErrorResponseMethod = FrobResource.class.getMethod("frobErrorResponse", int.class);
        // this is an error that should trip the circuit-breaker so should up the FAILURE count
        metricsTest(frobErrorResponseMethod, HttpResponseCodes.SC_INTERNAL_SERVER_ERROR, 1L, 1L);
        // this is a bad request that should NOT trip the circuit break or up the FAILURE count
        metricsTest(frobErrorResponseMethod, HttpResponseCodes.SC_BAD_REQUEST, 1L, 2L);
    }

    private void metricsTest(Method method, int statusCode, long expectedFailureCount, long expectedExceptionThrownCount) throws Exception {
        try {
            frobResource.frobErrorResponse(statusCode);
            fail();
        } catch(StatusCodeMatcher.ResponseError ex) {
            HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixClient.keyForMethod(method));
            assertThat(ex.getClientResponse().getStatus(), is(statusCode));
            assertThat(metrics.getCumulativeCount(HystrixRollingNumberEvent.FAILURE), is(expectedFailureCount));
            assertThat(metrics.getCumulativeCount(HystrixRollingNumberEvent.EXCEPTION_THROWN), is(expectedExceptionThrownCount));
        }
    }

    private class TestClientErrorInterceptor implements ClientErrorInterceptor {
        @Override
        public void handle(ClientResponse response) throws RuntimeException {
            throw new StatusCodeMatcher.ResponseError(response);
        }
    }
}
