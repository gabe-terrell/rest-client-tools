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
package com.opower.rest.client.generator.executors;

import com.opower.rest.client.generator.core.ClientExecutor;
import com.opower.rest.client.generator.core.ClientRequest;
import com.opower.rest.client.generator.core.ClientRequestFilter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractClientExecutor implements ClientExecutor {

    private final List<ClientRequestFilter> requestFilters;


    protected AbstractClientExecutor(List<ClientRequestFilter> requestFilters) {
        this.requestFilters = checkNotNull(requestFilters);
    }

    @Override
    public void processFilters(ClientRequest request) {
        for(ClientRequestFilter filter : requestFilters) {
            filter.filter(request);
        }
    }

}
