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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is a trick used to extract GenericType information at runtime.  Java does not allow you get generic
 * type information easily, so this class does the trick.  For example:
 * <p/>
 * <pre>
 * Type genericType = (new GenericType<List<String>>() {}).getGenericType();
 * </pre>
 * <p/>
 * The above code will get you the genericType for List<String>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class GenericType<T> {
    final Class<T> type;
    final Type genericType;

    /**
     * Constructs a new generic entity. Derives represented class from type
     * parameter. Note that this constructor is protected, users should create
     * a (usually anonymous) subclass as shown above.
     *
     * @throws IllegalArgumentException if entity is null
     */
    @SuppressWarnings("unchecked")
    protected GenericType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        this.genericType = parameterized.getActualTypeArguments()[0];
        this.type = (Class<T>) Types.getRawType(genericType);
    }

    /**
     * Gets the raw type of the enclosed entity. Note that this is the raw type of
     * the instance, not the raw type of the type parameter. I.e. in the example
     * in the introduction, the raw type is {@code ArrayList} not {@code List}.
     *
     * @return the raw type
     */
    public final Class<T> getType() {
        return type;
    }

    /**
     * Gets underlying {@code Type} instance. Note that this is derived from the
     * type parameter, not the enclosed instance. I.e. in the example
     * in the introduction, the type is {@code List<String>} not
     * {@code ArrayList<String>}.
     *
     * @return the type
     */
    public final Type getGenericType() {
        return genericType;
    }

}
