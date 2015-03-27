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
package com.opower.rest.client.generator.extractors;


import com.opower.rest.client.generator.core.BaseClientResponse;

import java.io.InputStream;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opower.rest.client.generator.extractors.DefaultEntityExtractorFactory.handleResponseErrors;

/**
 * BodyEntityExtractor extract body objects from responses. This ends up calling
 * the appropriate MessageBodyReader through a series of calls
 *
 * @author <a href="mailto:sduskis@gmail.com">Solomon Duskis</a>
 *
 * @see EntityExtractorFactory
 * @see javax.ws.rs.ext.MessageBodyReader
 */
@SuppressWarnings("unchecked")
public class BodyEntityExtractor implements EntityExtractor {
    private final Method method;

    public BodyEntityExtractor(Method method) {
        this.method = checkNotNull(method);
    }

    public Object extractEntity(ClientRequestContext context, Object... args) {
        handleResponseErrors(this.method, context);
        final BaseClientResponse response = context.getClientResponse();
        // only release connection if it is not an instance of an
        // InputStream
        boolean releaseConnectionAfter = true;
        try {
            // void methods should be handled before this method gets called, but it's worth being defensive
            if (this.method.getReturnType() == null) {
                throw new RuntimeException(
                        "No type information to extract entity with.  You use other getEntity() methods");
            }
            Object obj = response.getEntity(this.method.getReturnType(), this.method.getGenericReturnType());
            if (obj instanceof InputStream)
                releaseConnectionAfter = false;
            return obj;
        } catch (RuntimeException e) {
            context.getErrorHandler().clientErrorHandling(this.method, response, e);
        } finally {
            if (releaseConnectionAfter)
                response.releaseConnection();
        }
        throw new RuntimeException("Should be unreachable");
    }
}
