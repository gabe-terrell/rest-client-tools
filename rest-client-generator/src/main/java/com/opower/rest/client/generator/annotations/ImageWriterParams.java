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
package com.opower.rest.client.generator.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.imageio.ImageWriteParam;

/**
 * An annotation that a resource class can use to pass parameters
 * to the {@link com.opower.rest.client.generator.plugins.providers.IIOImageProvider}.
 *
 * @author <a href="ryan@damnhandy.com>Ryan J. McDonough</a>
 * @version $Revision: $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ImageWriterParams
{

   /**
    * Specifies the compression quality of the image being written. By
    * default, the highest compression level is used. A float value
    * between 0.0f and 1.0f are acceptable. The default value is 1.0f;
    *
    * @return
    */
   float compressionQuality() default 1.0f;

   /**
    * Specifies the compression mode for the output image. By default,
    * it uses {@link javax.imageio.ImageWriteParam#MODE_COPY_FROM_METADATA}.
    *
    * @return
    */
   int compressionMode() default ImageWriteParam.MODE_COPY_FROM_METADATA;

}
