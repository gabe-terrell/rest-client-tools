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


/**
 * EntityExtractor extract objects from responses. An extractor can extract a
 * status, a header, a cookie, the response body, the clientRequest object, the
 * clientResponse object, or anything else that a "response object" might need.
 *
 * @author <a href="mailto:sduskis@gmail.com">Solomon Duskis</a>
 *
 * @see EntityExtractorFactory
 */
public interface EntityExtractor<T> {
    T extractEntity(ClientRequestContext context, Object... args);
}
