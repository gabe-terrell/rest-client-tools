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

import com.opower.rest.client.generator.util.StringConverters;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * This class is only for situations where NO JAX-RS implementation is found on the classpath. The client can still work
 * by using this little class. All you have to do is set a system property on the jvm:
 *
 * -Djavax.ws.rs.ext.RuntimeDelegate=com.opower.rest.client.generator.core.BasicRuntimeDelegate
 */
public class BasicRuntimeDelegate extends RuntimeDelegate {

    @Override
    public UriBuilder createUriBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Variant.VariantListBuilder createVariantListBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> tClass) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(final Class<T> tClass) {
        return new HeaderDelegate<T>() {
            @Override
            public T fromString(String s) throws IllegalArgumentException {
                return StringConverters.getStringConverter(tClass).fromString(s);
            }

            @Override
            public String toString(T t) {
                return StringConverters.getStringConverter(tClass).toString(t);
            }
        };
    }
}
