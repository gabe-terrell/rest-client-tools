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

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class MessageBodyParameterMarshaller implements Marshaller {
    private Class type;
    private MediaType mediaType;
    private Type genericType;
    private Annotation[] annotations;

    public MessageBodyParameterMarshaller(MediaType mediaType, Class type, Type genericType, Annotation[] annotations) {
        this.type = type;
        this.mediaType = mediaType;
        this.genericType = genericType;
        this.annotations = annotations;
    }

    public void build(ClientRequest request, Object object) {
        request.body(mediaType, object, type, genericType, annotations);
    }

    public Class getType() {
        return type;
    }

}
