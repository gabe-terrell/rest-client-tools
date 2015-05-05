/**
 *    Copyright 2014 Opower, Inc.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 **/
package com.opower.rest.client.generator.core;

import com.google.common.base.Predicate;
import com.opower.rest.client.generator.extractors.ClientErrorHandler;
import com.opower.rest.client.generator.extractors.DefaultClientErrorHandler;
import com.opower.rest.client.generator.extractors.DefaultEntityExtractorFactory;
import com.opower.rest.client.generator.util.IsHttpMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Providers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.opower.rest.client.generator.util.HttpResponseCodes.SC_BAD_REQUEST;

/**
 * Base class to make the return types of the inherited builders work correctly.
 * @param <T> the type of the client to be created
 * @param <B> the type of the concrete builder
 */
public abstract class Client<T, B extends Client<T, B>> {

    public static final Predicate<Integer> DEFAULT_ERROR_STATUS_CRITERIA = new Predicate<Integer>() {
        @Override
        public boolean apply(Integer status) {
            checkNotNull(status);
            return status >= SC_BAD_REQUEST && status <= NETWORK_CONNECT_TIMEOUT;
        }
    };

    protected static final int NETWORK_CONNECT_TIMEOUT = 599;

    private final ConcurrentMap<Method, Predicate<Integer>> errorStatusCriteria = new ConcurrentHashMap<>();

    protected ClientExecutor executor;
    protected ClientProviders clientProviders = new ClientProviders();
    protected List<ClientErrorInterceptor> clientErrorInterceptors;
    protected final ResourceInterface<T> resourceInterface;
    protected final UriProvider uriProvider;
    protected final ClassLoader loader;

    protected Client(ResourceInterface<T> resourceInterface, UriProvider uriProvider) {
        this.resourceInterface = checkNotNull(resourceInterface);
        this.uriProvider = checkNotNull(uriProvider);
        this.loader = checkNotNull(resourceInterface.getInterface().getClassLoader());
        for (Method method : resourceInterface.getInterface().getMethods()) {
            this.errorStatusCriteria.put(method, DEFAULT_ERROR_STATUS_CRITERIA);
        }
    }

    /**
     * Configures a custom {@link com.google.common.base.Predicate<Integer>} that defines which Http response codes should
     * be treated as errors and cause the client proxy to throw an Exception. The specified {@link com.google.common.base.Predicate<Integer>}
     * will be used for all methods on the resource interface.
     * @param errorStatusCriteria The Predicate to use.
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public B errorStatusCriteria(Predicate<Integer> errorStatusCriteria) {
        checkNotNull(errorStatusCriteria);
        for (Method method : this.resourceInterface.getInterface().getDeclaredMethods()) {
            errorStatusCriteriaForMethod(method, errorStatusCriteria);
        }
        return (B) this;
    }

    /**
     *
     * Configures a custom {@link com.google.common.base.Predicate<Integer>} that defines which Http response codes should
     * be treated as errors and cause the client proxy to throw an Exception. The specified {@link com.google.common.base.Predicate<Integer>}
     * will be used ONLY for the specified method.
     *
     * @param method the method on the resource interface
     * @param errorStatusCriteria the Predicate to use.
     * @return the builder
     */
    @SuppressWarnings("unchecked")
    public B errorStatusCriteriaForMethod(Method method, Predicate<Integer> errorStatusCriteria) {
        checkArgument(method != null && method.getDeclaringClass().equals(this.resourceInterface.getInterface()));
        this.errorStatusCriteria.put(method, checkNotNull(errorStatusCriteria));
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B clientErrorInterceptors(List<ClientErrorInterceptor> clientErrorInterceptors) {
        this.clientErrorInterceptors = checkNotNull(clientErrorInterceptors);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B executor(ClientExecutor exec) {
        this.executor = exec;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B registerProviderInstance(Object provider) {
        this.clientProviders.registerProviderInstance(provider);
        return (B) this;
    }

    /**
     * Get holder of registered providers. This is mutable structure, so some instances may be added after finalizing client
     * builder work. Useful when you need to register provider (message body reader or writer) that relies on other
     * providers (e.g. MultipartFormAnnotationWriter).
     * 
     * @return holder of registered providers
     */
    public Providers getClientProviders() {
        return this.clientProviders;
    }

    public T build() {
        if (this.executor == null)
            throw new IllegalArgumentException("You must provide a ClientExecutor");
        if (this.clientProviders == null)
            throw new IllegalArgumentException("you must specify a MessageBodyWriter and a MessageBodyReader for serialization");

        final ProxyConfig config = new ProxyConfig(this.loader, this.executor, this.clientProviders, new DefaultEntityExtractorFactory(),
                                                   this.errorStatusCriteria, getClientErrorHandler());
        return createProxy(this.resourceInterface.getInterface(), this.uriProvider, config);
    }

    protected ClientErrorHandler getClientErrorHandler() {
        return new DefaultClientErrorHandler(this.clientErrorInterceptors);
    }

    @SuppressWarnings("unchecked")
    static <S> S createProxy(final Class<S> iface, UriProvider uriProvider, final ProxyConfig config) {
        HashMap<Method, MethodInvoker> methodMap = new HashMap<Method, MethodInvoker>();
        for (Method method : iface.getMethods()) {
            MethodInvoker invoker;
            Set<String> httpMethods = IsHttpMethod.getHttpMethods(method);
            if ((httpMethods == null || httpMethods.size() == 0) && method.isAnnotationPresent(Path.class) && method.getReturnType().isInterface()) {
                invoker = new SubResourceInvoker(uriProvider, method, config);
            } else {
                invoker = createClientInvoker(iface, method, uriProvider, config);
            }
            methodMap.put(method, invoker);
        }

        Class<?>[] intfs = { iface };

        ClientProxy clientProxy = new ClientProxy(methodMap, config);
        // this is done so that equals and hashCode work ok. Adding the rest to a
        // Collection will cause equals and hashCode to be invoked. The Spring
        // infrastructure had some problems without this.
        clientProxy.setClazz(iface);

        return (S) Proxy.newProxyInstance(config.getLoader(), intfs, clientProxy);
    }

    private static ClientInvoker createClientInvoker(Class<?> clazz, Method method, UriProvider uriProvider, ProxyConfig config) {
        Set<String> httpMethods = IsHttpMethod.getHttpMethods(method);
        if (httpMethods == null || httpMethods.size() != 1) {
            throw new RuntimeException("You must use at least one, but no more than one http method annotation on: " + method.toString());
        }
        ClientInvoker invoker = new ClientInvoker(uriProvider, clazz, method, config);
        invoker.setHttpMethod(httpMethods.iterator().next());
        return invoker;
    }

    /**
     * Basic JAX-RS client proxy builder.
     * @param <T> the type of the ResourceInterface to build a client for
     */
    public static final class Builder<T> extends Client<T, Builder<T>> {
        public Builder(ResourceInterface<T> resourceInterface, UriProvider uriProvider) {
            super(resourceInterface, uriProvider);
        }
    }
}
