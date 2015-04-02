package com.opower.rest.client.generator.hystrix;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.opower.rest.client.ConfigurationCallback;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.ResourceInterface;
import com.opower.rest.client.generator.core.SimpleUriProvider;
import com.opower.rest.client.generator.core.UriProvider;
import com.opower.rest.client.generator.hystrix.HystrixClientErrorHandler.BadRequestCriteria;
import com.opower.rest.test.resource.FrobResource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the HystrixClient.Builder.
 * @author chris.phillips
 */
public class TestHystrixClientBuilder {

    private static final UriProvider URI_PROVIDER = new SimpleUriProvider("http://localhost");
    static final HystrixCommandGroupKey GROUP_KEY = HystrixCommandGroupKey.Factory.asKey("test");
    private static final ResourceInterface<FrobResource> RESOURCE_INTERFACE = new ResourceInterface<>(FrobResource.class);
    private static final Method FROB_METHOD = FrobResource.class.getMethods()[0];
    private HystrixClient.Builder<FrobResource> builder = new HystrixClient.Builder<>(RESOURCE_INTERFACE, URI_PROVIDER, GROUP_KEY);
    /**
     * Initializes the system property to ensure the RuntimeDelegate gets properly loaded.
     */
    @BeforeClass
    public static void init() {
        System.setProperty("javax.ws.rs.ext.RuntimeDelegate","com.opower.rest.client.generator.core.BasicRuntimeDelegate");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullMethod() {
        builder.methodBadRequestCriteria(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMethod() {
        builder.methodBadRequestCriteria(TestHystrixClientBuilder.class.getMethods()[0], null);
    }

    @Test(expected = NullPointerException.class)
    public void nullCriteria() {
        builder.methodBadRequestCriteria(FROB_METHOD, null);
    }

    @Test
    public void subInterfaceIsValid() {
        HystrixClient.Builder<FrobResource> builder =
                new HystrixClient.Builder<>(new ResourceInterface<>(SpecialFrobResource.class), URI_PROVIDER, GROUP_KEY);
        builder.methodFallback(FROB_METHOD, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
    }

    @Test
    public void setMethodCriteria() {
        builder.methodBadRequestCriteria(FROB_METHOD, new BadRequestCriteria() {
            @Override
            public boolean apply(BaseClientResponse response, Exception exception) {
                return false;
            }
        });
        Map<Method, ? extends BadRequestCriteria> criteriaMap = ((HystrixClientErrorHandler)builder.getClientErrorHandler()).getCriteriaMap();
        assertTrue(criteriaMap.containsKey(FROB_METHOD));
        assertThat(criteriaMap.size(), is(1));
    }

    @Test
    public void setResourceCriteria() {
        builder.badRequestCriteria(new BadRequestCriteria() {
            @Override
            public boolean apply(BaseClientResponse response, Exception exception) {
                return false;
            }
        });
        Map<Method, ? extends BadRequestCriteria> criteriaMap = ((HystrixClientErrorHandler)builder.getClientErrorHandler()).getCriteriaMap();
        Method[] methods = FrobResource.class.getMethods();
        for (Method method : methods) {
            assertTrue(criteriaMap.containsKey(method));
        }
        assertThat(criteriaMap.size(), is(methods.length));
    }

    /**
     * To make sure that sub-interfaces can be used.
     */
    private interface SpecialFrobResource extends FrobResource{

    }
}
