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

/**
 * Implement this interface and annotate your class with @Provider to provide marshalling and unmarshalling
 * of string-based, @HeaderParam, @MatrixParam, @QueryParam, and/or @PathParam injected values.
 * <p/>
 * Use this when toString(), valueOf, and/or constructor(String) can not satisfy your marshalling requirements.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public interface StringConverter<T> {
    T fromString(String string);
    String toString(T value);
}
