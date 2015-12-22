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
package com.opower.rest.test.resource;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Meaningless implementation of FrobResource. The point of the tests is to show that the client
 * can talk correctly to the JAX-RS resource so this just makes dumb responses.
 */
public class FrobServerResource implements FrobResource {

    private static final LoadingCache<String, Frob> FROBS = CacheBuilder.newBuilder().build(new CacheLoader<String, Frob>() {
        @Override
        public Frob load(String key) throws Exception {
            return new Frob(key);
        }
    });

    @Override
    public Frob findFrob(String frobId) {

        try {
            return FROBS.get(checkNotNull(frobId));
        } 
        catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Frob updateFrob(String frobId, String name) {

        try {
            return FROBS.get(checkNotNull(frobId));
        } 
        catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Response createFrob(Frob frob) {
        FROBS.put(frob.getId(), frob);
        return Response.ok().entity("success").build();
    }

    @Override
    public String frobString(String echo) {
        return String.format("You sent %s", echo);
    }

    @Override
    public Frob frobJsonError() {
        return new Frob("testId");
    }

    @Override
    public Frob frobErrorResponse(int status) {
        throw new WebApplicationException(Response.Status.fromStatusCode(status));
    }
}
