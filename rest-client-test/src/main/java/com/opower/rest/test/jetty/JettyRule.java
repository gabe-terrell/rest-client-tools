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
package com.opower.rest.test.jetty;

import org.eclipse.jetty.server.Server;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This class starts up a Jetty instance for a test class.
 */
public class JettyRule extends ExternalResource {

    private int port;
    private String descriptor;

    /**
     * Constructor creates an instace with a Jetty server running at the given port and with the given 
     * web.xml descriptor.
     * @param port the port jetty will listen on
     * @param descriptor the web.xml to be used
     */
    public JettyRule(int port, String descriptor) {
        this.port = port;
        this.descriptor = descriptor;
    }
    
    @Override
    public Statement apply(Statement statement, Description description) {
        return new JettyStatement(statement, this.port, this.descriptor);
    }

    private final class JettyStatement extends Statement {

        private final Statement wrapped;
        private final Server server;

        private JettyStatement(Statement wrapped, int port, String descriptor) {
            this.wrapped = wrapped;
            this.server = JettyServerBuilder.initServer(port, descriptor);
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                this.server.start();
                this.wrapped.evaluate();
            } 
            finally {
                this.server.stop();
            }
        }
    }
}
