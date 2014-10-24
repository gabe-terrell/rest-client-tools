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

import java.util.Map;

/**
 * Allows projects to provide different client instances based on their ClientBuilder implementations.
 */
public interface FrobClientLoader {

    /**
     * Assembles a Map of client proxies that will test various configurations of the ClientBuilders.
     * @param port port that the jetty server is listening on
     * @param type type is used to resolve the correct web.xml for any supporting jetty servers
     *             that need to be created for tests. 
     * @return the map of idstring -> test client
     */
    Map<String, FrobResource> clientsToTest(int port, String type);
}
