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
package com.opower.rest.test.resource;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for looking at the jar file and extracting the maven version information.
 */
public class MavenVersionLoader {

    private String groupId;
    private String artifactId;

    /**
     * Create a version loader instance for the given groupId and artifactId.
     * @param groupId the groupId to use
     * @param artifactId the artifactId to use
     */
    public MavenVersionLoader(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * Look for the pom.properties file in the jar based on the groupId and artifactId.
     * @return the version string or an empty string
     */
    public String loadVersion() {
        String v = "";
        try {
            Properties p = new Properties();
            InputStream is = getClass().getResourceAsStream(
                    String.format("/META-INF/maven/%s/%s/pom.properties", this.groupId, this.artifactId));
            if (is != null) {
                p.load(is);
                v = p.getProperty("version", "");
            }
        } 
        catch (Exception e) {
            // ignore
        }

        return v;
    }

}
