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
import com.opower.rest.client.generator.core.ClientResponseFailure;

import java.io.InputStream;
import java.lang.reflect.Method;

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
        this.method = method;
    }

    public Object extractEntity(ClientRequestContext context, Object... args) {
        final BaseClientResponse response = context.getClientResponse();
        try {
            response.checkFailureStatus();
        } catch (ClientResponseFailure ce) {
            // If ClientResponseFailure do a copy of the response and then release the connection,
            // we need to use the copy here and not the original response
            context.getErrorHandler().clientErrorHandling((BaseClientResponse) ce.getResponse(), ce);
        } catch (RuntimeException e) {
            context.getErrorHandler().clientErrorHandling(response, e);
        }

        // only release connection if it is not an instance of an
        // InputStream
        boolean releaseConnectionAfter = true;
        try {
            // void methods should be handled before this method gets called, but it's worth being defensive
            if (method.getReturnType() == null) {
                throw new RuntimeException(
                        "No type information to extract entity with.  You use other getEntity() methods");
            }
            Object obj = response.getEntity(method.getReturnType(), method.getGenericReturnType());
            if (obj instanceof InputStream)
                releaseConnectionAfter = false;
            return obj;
        } catch (RuntimeException e) {
            context.getErrorHandler().clientErrorHandling(response, e);
        } finally {
            if (releaseConnectionAfter)
                response.releaseConnection();
        }
        throw new RuntimeException("Should be unreachable");
    }
}
