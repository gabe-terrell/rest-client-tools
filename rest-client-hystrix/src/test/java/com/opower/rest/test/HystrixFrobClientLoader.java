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
package com.opower.rest.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.HystrixCommandProperties;
import com.opower.rest.client.ConfigurationCallback;
import com.opower.rest.client.generator.core.ResourceInterface;
import com.opower.rest.client.generator.core.SimpleUriProvider;
import com.opower.rest.client.generator.executors.ApacheHttpClient4Executor;
import com.opower.rest.client.generator.hystrix.HystrixClient;
import com.opower.rest.client.generator.hystrix.TestHystrixGroupKeys;
import com.opower.rest.test.resource.FrobClientLoader;
import com.opower.rest.test.resource.FrobResource;

import java.util.Map;


/**
 * Assembles sample clients using the HystrixClientBuilder.
 */
public class HystrixFrobClientLoader implements FrobClientLoader {

    private static final int TEN_SECONDS = 10000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setDateFormat(new ISO8601DateFormat())
            .registerModule(new GuavaModule())
            .registerModule(new JodaModule());
    private static final JacksonJsonProvider JACKSON_JSON_PROVIDER = new JacksonJsonProvider(OBJECT_MAPPER)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Map<String, FrobResource> clientsToTest(final int port, String type) {
        try {
            HystrixClient.Builder<FrobResource> clientBuilder = new HystrixClient.Builder<FrobResource>(
                    new ResourceInterface<>(FrobResource.class),
                    new SimpleUriProvider(String.format("http://localhost:%s", port)),
                    TestHystrixGroupKeys.TEST_GROUP)
                    .executor(new ApacheHttpClient4Executor())
                    .commandProperties(new ConfigurationCallback<HystrixCommandProperties.Setter>() {
                        @Override
                        public void configure(HystrixCommandProperties.Setter setter) {
                            setter.withExecutionIsolationThreadTimeoutInMilliseconds(TEN_SECONDS);
                        }
                    })
                    .registerProviderInstance(JACKSON_JSON_PROVIDER);

            return ImmutableMap.of("default", clientBuilder.build());
        }
        catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }
}
