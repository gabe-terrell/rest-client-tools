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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.opower.rest.client.generator.extractors.EntityExtractorFactory;

import javax.ws.rs.ext.Providers;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProxyConfig {
    private final ClassLoader loader;
    private final ClientExecutor executor;
    private final Providers providers;
    private final EntityExtractorFactory extractorFactory;
    private final List<ClientErrorInterceptor> clientErrorInterceptors;

    public ProxyConfig(ClassLoader loader, ClientExecutor executor, Providers providers,
                       EntityExtractorFactory extractorFactory, List<ClientErrorInterceptor> clientErrorInterceptors) {
        this.loader = checkNotNull(loader);
        this.executor = checkNotNull(executor);
        this.providers = checkNotNull(providers);
        this.extractorFactory = checkNotNull(extractorFactory);
        this.clientErrorInterceptors = Optional.fromNullable(clientErrorInterceptors).or(Lists.<ClientErrorInterceptor>newArrayList());
    }


    public ClassLoader getLoader() {
        return loader;
    }

    public ClientExecutor getExecutor() {
        return executor;
    }

    public Providers getProviders() {
        return providers;
    }

    public EntityExtractorFactory getExtractorFactory() {
        return extractorFactory;
    }

    public List<ClientErrorInterceptor> getClientErrorInterceptors() {
        return clientErrorInterceptors;
    }

}
