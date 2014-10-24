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
package com.opower.rest.client.generator.marshallers;


import com.opower.rest.client.generator.core.ClientRequest;

import javax.ws.rs.core.Cookie;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class CookieParamMarshaller implements Marshaller {
    private String cookieName;

    public CookieParamMarshaller(String cookieName) {
        this.cookieName = cookieName;
    }

    public void build(ClientRequest request, Object object) {
        if (object == null) return;  // don't set a null value
        if (object instanceof Cookie) {
            Cookie cookie = (Cookie) object;
            request.cookie(cookie);
        } else {
            request.cookie(cookieName, object);
        }
    }
}
