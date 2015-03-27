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

import com.google.common.base.Optional;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.ClientErrorInterceptor;
import com.opower.rest.client.generator.core.ClientResponseFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles client errors (of course...).
 *
 * @author Solomon.Duskis
 */

// TODO: expand this class for more robust, complicated error handling

public class DefaultClientErrorHandler implements ClientErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultClientErrorHandler.class);
    private final List<ClientErrorInterceptor> interceptors;

    public DefaultClientErrorHandler(List<ClientErrorInterceptor> interceptors) {
        this.interceptors = Optional.fromNullable(interceptors).or(new ArrayList<ClientErrorInterceptor>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clientErrorHandling(Method method, BaseClientResponse clientResponse, RuntimeException e) {
        // Only ClientResponseFailures represent an error response from the server
        // so only such exceptions should be handled by the ClientErrorInterceptors
        if (e instanceof ClientResponseFailure) {
            for (ClientErrorInterceptor handler : interceptors) {
                try {
                    // attempt to reset the stream in order to provide a fresh stream
                    // to each ClientErrorInterceptor -- failing to reset the stream
                    // could mean that an unusable stream will be passed to the
                    // interceptor
                    InputStream stream = clientResponse.getStreamFactory().getInputStream();
                    if (stream != null) {
                        stream.reset();
                    }
                } catch (IOException e1) {
                    LOG.warn("problem while handling errors", e1);
                }
                handler.handle(clientResponse);
            }
        } else {
            String status = clientResponse == null ? "null" : Integer.toString(clientResponse.getStatus());
            LOG.warn("The HTTP request was successful and retuned a status of {}. However, there was a problem processing " +
                     "the response on the client side. Usually this is a deserialization problem, check the configuration " +
                     "of your MessageBodyReader.", status);
        }

        throw e;
    }
}
