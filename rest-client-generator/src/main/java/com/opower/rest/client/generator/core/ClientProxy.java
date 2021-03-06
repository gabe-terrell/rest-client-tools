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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
public class ClientProxy implements InvocationHandler {
    private Map<Method, MethodInvoker> methodMap;
    private Class<?> clazz;
    private final ProxyConfig config;

    public ClientProxy(Map<Method, MethodInvoker> methodMap, ProxyConfig config) {
        super();
        this.methodMap = methodMap;
        this.config = config;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object invoke(Object o, Method method, Object[] args)
            throws Throwable {
        // equals and hashCode were added for cases where the rest is added to
        // collections. The Spring transaction management, for example, adds
        // transactional Resources to a Collection, and it calls equals and
        // hashCode.

        MethodInvoker clientInvoker = methodMap.get(method);
        if (clientInvoker == null) {
            if (method.getName().equals("equals")) {
                return this.equals(o);
            } else if (method.getName().equals("hashCode")) {
                return this.hashCode();
            } else if (method.getName().equals("toString") && (args == null || args.length == 0)) {
                return this.toString();
            }
        }

        if (clientInvoker == null) {
            throw new RuntimeException("Could not find a method for: " + method);
        }
        return clientInvoker.invoke(args);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ClientProxy))
            return false;
        ClientProxy other = (ClientProxy) obj;
        if (other == this)
            return true;
        if (other.clazz != this.clazz)
            return false;
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    public String toString() {
        return "Client Proxy for :" + clazz.getName();
    }
}
