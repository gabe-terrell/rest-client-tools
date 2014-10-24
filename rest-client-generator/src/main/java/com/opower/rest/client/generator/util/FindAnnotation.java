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

import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
@SuppressWarnings("unchecked")
public final class FindAnnotation {
    private FindAnnotation() {
    }

    /**
     * FIXME Comment this
     *
     * @param <T>
     * @param searchList
     * @param annotation
     * @return
     */
    public static <T> T findAnnotation(Annotation[] searchList, Class<T> annotation) {
        if (searchList == null) return null;
        for (Annotation ann : searchList) {
            if (ann.annotationType().equals(annotation)) {
                return (T) ann;
            }
        }
        return null;
    }
}
