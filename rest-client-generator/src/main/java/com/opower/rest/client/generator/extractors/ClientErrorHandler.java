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

import com.google.common.collect.Lists;
import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.ClientErrorInterceptor;
import com.opower.rest.client.generator.core.ClientResponseFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class handles client errors (of course...).
 *
 * @author Solomon.Duskis
 */

// TODO: expand this class for more robust, complicated error handling

public class ClientErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ClientErrorHandler.class);
    private List<ClientErrorInterceptor> interceptors = Lists.newArrayList();

    public ClientErrorHandler(List<ClientErrorInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @SuppressWarnings("unchecked")
    public void clientErrorHandling(BaseClientResponse clientResponse, RuntimeException e) {

        // Only ClientResponseFailures represent an error response from the server
        // so only such exceptions should be handled by the ClientErrorInterceptors
        if(e instanceof ClientResponseFailure) {
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
        }

        throw e;
    }
}
