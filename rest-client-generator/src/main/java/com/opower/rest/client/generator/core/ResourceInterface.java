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

import com.opower.rest.client.generator.util.IsHttpMethod;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the interface to a JAX-RS Resource that will be shared between servers and clients.
 * @author chris.phillips
 */
public class ResourceInterface<T> {
    private final Class<T> resourceInterface;

    public ResourceInterface(Class<T> resourceInterface) {
        this.resourceInterface = validate(resourceInterface);
    }

    public Class<T> getInterface() {
        return resourceInterface;
    }

    private Class<T> validate(Class<T> resourceInterface) {
        checkNotNull(resourceInterface);

        checkArgument(resourceInterface.isInterface(),
                "The resource class must be an interface with all methods annotated with JAX-RS annotations");

        for (Method m : resourceInterface.getMethods()) {
            checkArgument(IsHttpMethod.isHttpMethod(m),
                    String.format("Method [%s] is not annotated with one or more HttpMethod annotations.", m.getName()));
        }

        return resourceInterface;
    }
}
