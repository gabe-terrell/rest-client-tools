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

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author chris.phillips
 */
public class SimpleUriProvider implements UriProvider {

    private final URI uri;

    public SimpleUriProvider(URI uri) {
        this.uri = checkNotNull(uri);
    }

    public SimpleUriProvider(String uri) {
        this.uri = URI.create(checkNotNull(uri));
    }

    @Override
    public URI getUri() {
        return uri;
    }
}
