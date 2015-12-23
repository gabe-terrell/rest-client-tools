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
package com.opower.rest.client.generator.extractors;

import com.opower.rest.client.generator.core.BaseClientResponse;
import com.opower.rest.client.generator.core.ClientResponse;
import com.opower.rest.client.generator.core.ClientResponseFailure;
import com.opower.rest.client.generator.util.Types;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Solomon.Duskis
 */
@SuppressWarnings("unchecked")
public class DefaultEntityExtractorFactory implements EntityExtractorFactory {

   public static final EntityExtractor clientResponseExtractor = new EntityExtractor<ClientResponse>()
   {
      public ClientResponse extractEntity(ClientRequestContext context, Object... args)
      {
          context.getClientResponse().setReturnType(Response.class);
         return context.getClientResponse();
      }
   };

    public static EntityExtractor<Response.Status> createStatusExtractor(final boolean release) {
        return new EntityExtractor<Response.Status>() {
            public Response.Status extractEntity(ClientRequestContext context, Object... args) {
                if (release)
                    context.getClientResponse().releaseConnection();
                return context.getClientResponse().getResponseStatus();
            }
        };
    }

    public static final EntityExtractor createVoidExtractor(final Method method, final boolean release) {
        checkNotNull(method);
        return new EntityExtractor() {
            public Object extractEntity(ClientRequestContext context, Object... args) {
                handleResponseErrors(method, context);
                if (release) {
                    context.getClientResponse().releaseConnection();
                }
                return null;
            }
        };
    }

    public EntityExtractor createExtractor(final Method method) {
        checkNotNull(method);
        final Class returnType = method.getReturnType();
        if (isVoidReturnType(returnType)) {
            return createVoidExtractor(method, true);
        }
        if (returnType.equals(Response.Status.class)) {
            return createStatusExtractor(true);
        }
        if (Response.class.isAssignableFrom(returnType) || returnType.getCanonicalName().equals("javax.ws.rs.core.Response")) {
            return createResponseTypeEntityExtractor(method);
        }

        // We are not a ClientResponse type so we need to unmarshall and narrow it
        // to right type. If we are unable to unmarshall, or encounter any kind of
        // Exception, give the ClientErrorHandlers a chance to handle the
        // ClientResponse manually.

        return new BodyEntityExtractor(method);
    }

    protected EntityExtractor createResponseTypeEntityExtractor(final Method method) {
        final Type methodGenericReturnType = method.getGenericReturnType();
        if (methodGenericReturnType instanceof ParameterizedType) {
            final ParameterizedType zType = (ParameterizedType) methodGenericReturnType;
            final Type genericReturnType = zType.getActualTypeArguments()[0];
            final Class<?> responseReturnType = Types.getRawType(genericReturnType);
            return new EntityExtractor() {
                public Object extractEntity(ClientRequestContext context, Object... args) {
                    handleResponseErrors(method, context);
                    context.getClientResponse().setReturnType(responseReturnType);
                    context.getClientResponse().setGenericReturnType(genericReturnType);
                    return context.getClientResponse();
                }
            };
        } else {
            return clientResponseExtractor;
        }
    }

    protected static void handleResponseErrors(Method method, ClientRequestContext context) {
        checkNotNull(method);
        final BaseClientResponse response = context.getClientResponse();
        try {
            response.checkFailureStatus();
        } catch (ClientResponseFailure ce) {
            // If ClientResponseFailure do a copy of the response and then release the connection,
            // we need to use the copy here and not the original response
            context.getErrorHandler().clientErrorHandling(method, (BaseClientResponse) ce.getResponse(), ce);
        } catch (RuntimeException e) {
            context.getErrorHandler().clientErrorHandling(method, response, e);
        }
    }

    public static final boolean isVoidReturnType(Class<?> returnType) {
        return returnType == null || void.class.equals(returnType) || Void.class.equals(returnType);
    }

}
