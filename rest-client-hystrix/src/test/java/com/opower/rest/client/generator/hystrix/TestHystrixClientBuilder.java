package com.opower.rest.client.generator.hystrix;

import com.google.auto.value.AutoValue;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
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

    private static final String TEST_KEY_1 = "test-key-1";
    private static final String TEST_KEY_2 = "test-key-2";
    private static final UriProvider URI_PROVIDER = new SimpleUriProvider("http://localhost");
    static final HystrixCommandGroupKey GROUP_KEY = HystrixCommandGroupKey.Factory.asKey("test");
    private static final ResourceInterface<FrobResource> RESOURCE_INTERFACE = new ResourceInterface<>(FrobResource.class);
    private static final Method FROB_METHOD = FrobResource.class.getMethods()[0];
    private HystrixClient.Builder<FrobResource> builder = new HystrixClient.Builder<>(RESOURCE_INTERFACE,
                                                                                      URI_PROVIDER,
                                                                                      GROUP_KEY);
    /**
     * Initializes the system property to ensure the RuntimeDelegate gets properly loaded.
     */
    @BeforeClass
    public static void init() {
        System.setProperty("javax.ws.rs.ext.RuntimeDelegate",
                           "com.opower.rest.client.generator.core.BasicRuntimeDelegate");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullMethod() {
        this.builder.methodBadRequestCriteria(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongMethod() {
        this.builder.methodBadRequestCriteria(TestHystrixClientBuilder.class.getMethods()[0], null);
    }

    @Test(expected = NullPointerException.class)
    public void nullCriteria() {
        this.builder.methodBadRequestCriteria(FROB_METHOD, null);
    }

    @Test
    public void subInterfaceIsValid() {
        HystrixClient.Builder<FrobResource> frobResourceBuilder =
                new HystrixClient.Builder<>(new ResourceInterface<>(SpecialFrobResource.class), URI_PROVIDER, GROUP_KEY);
        frobResourceBuilder.methodFallback(FROB_METHOD, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
    }

    @Test
    public void setMethodCriteria() {
        this.builder.methodBadRequestCriteria(FROB_METHOD, new BadRequestCriteria() {
            @Override
            public boolean apply(BaseClientResponse response, Exception exception) {
                return false;
            }
        });
        Map<Method, ? extends BadRequestCriteria> criteriaMap
                = ((HystrixClientErrorHandler) this.builder.getClientErrorHandler()).getCriteriaMap();
        assertTrue(criteriaMap.containsKey(FROB_METHOD));
        assertThat(criteriaMap.size(), is(1));
    }

    @Test
    public void setResourceCriteria() {
        this.builder.badRequestCriteria(new BadRequestCriteria() {
            @Override
            public boolean apply(BaseClientResponse response, Exception exception) {
                return false;
            }
        });
        Map<Method, ? extends BadRequestCriteria> criteriaMap
                = ((HystrixClientErrorHandler) this.builder.getClientErrorHandler()).getCriteriaMap();
        Method[] methods = FrobResource.class.getMethods();
        for (Method method : methods) {
            assertTrue(criteriaMap.containsKey(method));
        }
        assertThat(criteriaMap.size(), is(methods.length));
    }

    /**
     * Multiple calls to set the same criteria should just let the last one win.
     */
    @Test
    public void replaceBadRequestCriteria() {
        SimpleCriteria criteria1 = SimpleCriteria.create("1");
        SimpleCriteria criteria2 = SimpleCriteria.create("2");

        this.builder.methodBadRequestCriteria(FROB_METHOD, criteria1);
        this.builder.methodBadRequestCriteria(FROB_METHOD, criteria2);

        assertThat(this.builder.badRequestCriteriaMap.get(FROB_METHOD), is((BadRequestCriteria) criteria2));
    }

    /**
     * Setting the CommandKey multiple times should just let the last one win.
     */
    @Test
    public void replaceCommandKey() {
        this.builder.methodCommandKey(FROB_METHOD, HystrixCommandKey.Factory.asKey(TEST_KEY_1));
        this.builder.methodCommandKey(FROB_METHOD, HystrixCommandKey.Factory.asKey(TEST_KEY_2));

        assertThat(this.builder.commandKeyMap.get(FROB_METHOD).name(), is(TEST_KEY_2));
    }

    @Test
    public void replaceFallback() {
        SimpleFallback fallback1 = SimpleFallback.create(TEST_KEY_1);
        SimpleFallback fallback2 = SimpleFallback.create(TEST_KEY_2);

        this.builder.methodFallback(FROB_METHOD, fallback1);
        this.builder.methodFallback(FROB_METHOD, fallback2);

        assertThat(this.builder.fallbackMap.get(FROB_METHOD), is((Callable) fallback2));
    }

    @AutoValue
    abstract static class SimpleFallback implements Callable<Object> {
        static SimpleFallback create(String name) {
            return new AutoValue_TestHystrixClientBuilder_SimpleFallback(name);
        }

        @Override
        public Object call() {
            return new Object();
        }

        abstract String name();
    }

    @AutoValue
    abstract static class SimpleCriteria implements BadRequestCriteria {
        static SimpleCriteria create(String name) {
            return new AutoValue_TestHystrixClientBuilder_SimpleCriteria(name);
        }

        @Override
        public boolean apply(BaseClientResponse response, Exception exception) {
            return false;
        }

        abstract String name();
    }
    /**
     * To make sure that sub-interfaces can be used.
     */
    private interface SpecialFrobResource extends FrobResource{

    }
}
