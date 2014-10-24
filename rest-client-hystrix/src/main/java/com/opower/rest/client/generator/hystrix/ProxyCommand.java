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
package com.opower.rest.client.generator.hystrix;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.netflix.hystrix.HystrixCommand;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 
 * Generic HystrixCommand used by the HystrixClientBuilder to wrap ResourceInterface method invocations.
 * Not for use outside this package.
 *
 */
final class ProxyCommand extends HystrixCommand {

    private final Method toinvoke;
    private final Object[] args;
    private final Optional<Callable<Object>> fallback;
    private final Object target;

    /**
     * Creates a Proxy command with the specified settings.
     * @param setter the HystrixCommand.Setter to use
     * @param toinvoke the Method to be invoked
     * @param args the arguments for the method to be invoked
     * @param fallback the fallback Hystrix will use
     * @param target this ProxyCommand will invoke the provided method on this target object
     */
    ProxyCommand(Setter setter, Method toinvoke, Object[] args, Callable<Object> fallback, Object target) {
        super(setter);
        this.toinvoke = toinvoke;
        this.args = args;
        this.target = target;
        this.fallback = Optional.fromNullable(fallback);
        checkArgument(toinvoke.getDeclaringClass().isInstance(target), "The method to invoke must be present on the target object");
        checkArgument(!getProperties().fallbackEnabled().get() || this.fallback.isPresent(),
                      String.format("You didn't provide a fallback for %s.%s. You must either provide a "
                                    + "fallback or disable fallbacks "
                                    + "in the HystrixCommandProperties for this method.",
                                    toinvoke.getDeclaringClass().getCanonicalName(), toinvoke.getName()));
    }

    @Override
    protected Object run() throws Exception {
        return this.toinvoke.invoke(this.target, this.args);
    }

    @Override
    protected Object getFallback() {
        if (this.fallback.isPresent()) {
            try {
                return this.fallback.get().call();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        } else {
            return super.getFallback();
        }
    }
}
