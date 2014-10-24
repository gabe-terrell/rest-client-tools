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
package com.opower.rest.client.generator.util;

import javax.ws.rs.HttpMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class IsHttpMethod {
    public static Set<String> getHttpMethods(Method method) {
        HashSet<String> methods = new HashSet<String>();
        for (Annotation annotation : method.getAnnotations()) {
            HttpMethod http = annotation.annotationType().getAnnotation(HttpMethod.class);
            if (http != null) methods.add(http.value());
        }
        if (methods.size() == 0) return null;
        return methods;
    }

    public static boolean isHttpMethod(Method method) {
        return getHttpMethods(method) != null;
    }
}
