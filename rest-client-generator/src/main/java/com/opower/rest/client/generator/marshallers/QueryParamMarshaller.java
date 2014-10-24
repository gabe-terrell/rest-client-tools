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

import java.util.Collection;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class QueryParamMarshaller implements Marshaller {
    private String paramName;

    public QueryParamMarshaller(String paramName) {
        this.paramName = paramName;
    }

    public void build(ClientRequest request, Object object) {
        if (object == null) return;
        if (object instanceof Collection) {
            for (Object obj : (Collection) object) {
                request.queryParameter(paramName, obj);
            }
        } else if (object.getClass().isArray()) {
            if (object.getClass().getComponentType().isPrimitive()) {
                Class componentType = object.getClass().getComponentType();
                if (componentType.equals(boolean.class)) {
                    for (Boolean bool : (boolean[]) object) request.queryParameter(paramName, bool.toString());
                } else if (componentType.equals(byte.class)) {
                    for (Byte val : (byte[]) object) request.queryParameter(paramName, val.toString());
                } else if (componentType.equals(short.class)) {
                    for (Short val : (short[]) object) request.queryParameter(paramName, val.toString());
                } else if (componentType.equals(int.class)) {
                    for (Integer val : (int[]) object) request.queryParameter(paramName, val.toString());
                } else if (componentType.equals(long.class)) {
                    for (Long val : (long[]) object) request.queryParameter(paramName, val.toString());
                } else if (componentType.equals(float.class)) {
                    for (Float val : (float[]) object) request.queryParameter(paramName, val.toString());
                } else if (componentType.equals(double.class)) {
                    for (Double val : (double[]) object) request.queryParameter(paramName, val.toString());
                }
            } else {
                Object[] objs = (Object[]) object;
                for (Object obj : objs) {
                    request.queryParameter(paramName, obj);

                }
            }
        } else {
            request.queryParameter(paramName, object);
        }
    }
}
