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

import com.opower.rest.client.generator.extractors.ClientRequestContext;
import com.opower.rest.client.generator.extractors.EntityExtractor;
import com.opower.rest.client.generator.extractors.EntityExtractorFactory;
import com.opower.rest.client.generator.marshallers.ClientMarshallerFactory;
import com.opower.rest.client.generator.marshallers.Marshaller;
import com.opower.rest.client.generator.specimpl.UriBuilderImpl;
import com.opower.rest.client.generator.util.MediaTypeHelper;

import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
@SuppressWarnings("unchecked")
public class ClientInvoker implements MethodInvoker {
    protected String httpMethod;
    protected Method method;
    protected Class declaring;
    protected MediaType accepts;
    protected Marshaller[] marshallers;
    protected ClientExecutor executor;
    protected boolean followRedirects;
    protected EntityExtractor extractor;
    protected EntityExtractorFactory extractorFactory;
    protected UriProvider baseUriProvider;
    private final ProxyConfig proxyConfig;


    public ClientInvoker(UriProvider baseUriProvider, Class declaring, Method method, ProxyConfig config) {
        this.proxyConfig = config;
        this.declaring = declaring;
        this.method = method;
        this.marshallers = ClientMarshallerFactory.createMarshallers(declaring, method);
        this.executor = config.getExecutor();
        this.accepts = MediaTypeHelper.getProduces(declaring, method);
        this.baseUriProvider = checkNotNull(baseUriProvider);
        this.extractorFactory = config.getExtractorFactory();
        this.extractor = extractorFactory.createExtractor(method);
    }

    public Method getMethod() {
        return this.method;
    }

    public Object invoke(Object[] args) {

        ClientRequest request = createRequest(args);

        BaseClientResponse clientResponse = null;
        try {
            clientResponse = (BaseClientResponse) request.execute(this.httpMethod);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        clientResponse.setAttributeExceptionsTo(this.method.toString());
        clientResponse.setAnnotations(this.method.getAnnotations());
        ClientRequestContext clientRequestContext = new ClientRequestContext(request, clientResponse, this.proxyConfig.getClientErrorHandler());
        return this.extractor.extractEntity(clientRequestContext);
    }

    protected ClientRequest createRequest(Object[] args) {
        UriBuilderImpl uri = new UriBuilderImpl();
        uri.uri(this.baseUriProvider.getUri());
        if (this.declaring.isAnnotationPresent(Path.class)) uri.path(this.declaring);
        if (this.method.isAnnotationPresent(Path.class)) uri.path(this.method);
        ClientRequest request = new ClientRequest(uri, this.executor, this.proxyConfig, this.method);
        if (this.accepts != null) request.header(HttpHeaders.ACCEPT, this.accepts.toString());

        boolean isClientResponseResult = ClientResponse.class.isAssignableFrom(this.method.getReturnType());
        request.followRedirects(!isClientResponseResult || this.followRedirects);

        for (int i = 0; i < this.marshallers.length; i++) {
            this.marshallers[i].build(request, args[i]);
        }
        return request;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

}
