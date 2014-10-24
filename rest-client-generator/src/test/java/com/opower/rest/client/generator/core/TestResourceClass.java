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

import org.junit.Test;

import javax.ws.rs.GET;

/**
 * Tests the validation logic in the ResourceClass.
 */
public class TestResourceClass {

    @Test(expected = IllegalArgumentException.class)
    public void nonInterfaceFailsValidation() {
        new ResourceInterface<>(String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noJaxRsAnnotatedMethodsFails() {
        new ResourceInterface<>(NonJaxRsAnnotated.class);
    }

    @Test(expected = NullPointerException.class)
    public void nullClassFails() {
        new ResourceInterface<Object>(null);
    }

    @Test
    public void validResourceDoesNotFail() {
        new ResourceInterface<>(ValidResourceClass.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void almostValidResourceFails() {
        new ResourceInterface<>(AlmostValidResourceClass.class);
    }
    private interface NonJaxRsAnnotated {
        String testMethod();
    }

    private interface AlmostValidResourceClass {
        @GET
        String getId();
        void createUser();
    }

    private interface ValidResourceClass {
        @GET
        public String getId();
    }
}
