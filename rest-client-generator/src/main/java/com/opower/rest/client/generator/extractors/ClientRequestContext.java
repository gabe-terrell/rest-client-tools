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
import com.opower.rest.client.generator.core.ClientRequest;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClientRequestContext {
    private final ClientRequest request;
    private final BaseClientResponse clientResponse;
    private final ClientErrorHandler errorHandler;

    public ClientRequestContext(ClientRequest request, BaseClientResponse clientResponse,
                                ClientErrorHandler errorHandler) {
        this.request = checkNotNull(request);
        this.clientResponse = checkNotNull(clientResponse);
        this.errorHandler = checkNotNull(errorHandler);
    }

    public ClientRequest getRequest() {
        return request;
    }

    public BaseClientResponse getClientResponse() {
        return clientResponse;
    }

    public ClientErrorHandler getErrorHandler() {
        return errorHandler;
    }

}
