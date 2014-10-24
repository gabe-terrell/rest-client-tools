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

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
@SuppressWarnings("rawtypes")
public class ClientResponseFailure extends RuntimeException {
    private static final long serialVersionUID = 7491381058971118249L;
    private ClientResponse response;

    public ClientResponseFailure(ClientResponse response) {
        super("Failed with status: " + response.getStatus());
        this.response = BaseClientResponse.copyFromError(response);
        // release connection just in case it doesn't get garbage collected or manually released
        response.releaseConnection();
    }

    public ClientResponseFailure(String s, ClientResponse response) {
        super(s);
        this.response = BaseClientResponse.copyFromError(response);
        // release the connection because we don't trust users to catch and clean up
        response.releaseConnection();
    }

    public ClientResponseFailure(String s, Throwable throwable, ClientResponse response) {
        super(s, throwable);
        this.response = BaseClientResponse.copyFromError(response);
        // release the connection because we don't trust users to catch and clean up
        response.releaseConnection();
    }

    public ClientResponseFailure(Throwable throwable, ClientResponse response) {
        super(throwable);
        this.response = BaseClientResponse.copyFromError(response);
        // release the connection because we don't trust users to catch and clean up
        response.releaseConnection();
    }

    public ClientResponse getResponse() {
        return response;
    }
}
