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
package com.opower.rest.client.generator.plugins.providers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

/**
 * A utility class to provide supporting functionality to various
 * entity clientProviders.
 *
 * @author <a href="ryan@damnhandy.com>Ryan J. McDonough</a>
 * @version $Revision: $
 */
public final class ProviderHelper
{

   private ProviderHelper()
   {

   }

   /**
    * @param in
    * @return
    * @throws java.io.IOException
    */
   public static String readString(InputStream in) throws IOException
   {
      char[] buffer = new char[1024];
      StringBuilder builder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      int wasRead = 0;
      do
      {
         wasRead = reader.read(buffer, 0, 1024);
         if (wasRead > 0)
         {
            builder.append(buffer, 0, wasRead);
         }
      }
      while (wasRead > -1);

      return builder.toString();
   }

   /**
    * @param in
    * @return
    * @throws java.io.IOException
    */
   public static String readString(InputStream in, MediaType mediaType) throws IOException
   {
      byte[] buffer = new byte[1024];
      ByteArrayOutputStream builder = new ByteArrayOutputStream();
      int wasRead = 0;
      do
      {
         wasRead = in.read(buffer, 0, 1024);
         if (wasRead > 0)
         {
            builder.write(buffer, 0, wasRead);
         }
      }
      while (wasRead > -1);
      byte[] bytes = builder.toByteArray();

      String charset = mediaType.getParameters().get("charset");
      if (charset != null) return new String(bytes, charset);
      else return new String(bytes, "UTF-8");
   }

   /**
    * @param mediaTypes
    * @return
    */
   public static List<MediaType> getAvailableMediaTypes(String[] mediaTypes)
   {
      List<MediaType> types = new ArrayList<MediaType>();
      for (String mediaType : mediaTypes)
      {
         types.add(MediaType.valueOf(mediaType));
      }
      return types;
   }

   /**
    * @param mediaTypes
    * @return
    */
   public static List<Variant> getAvailableVariants(String[] mediaTypes)
   {
      return getAvailableVariants(getAvailableMediaTypes(mediaTypes));
   }

   /**
    * @param mediaTypes
    * @return
    */
   public static List<Variant> getAvailableVariants(List<MediaType> mediaTypes)
   {
      VariantListBuilder builder = Variant.VariantListBuilder.newInstance();
      MediaType[] types = mediaTypes.toArray(new MediaType[mediaTypes.size()]);
      builder.mediaTypes(types);
      return builder.build();
   }

   /**
    * @param in
    * @param out
    * @throws java.io.IOException
    */
   public static void writeTo(final InputStream in, final OutputStream out) throws IOException
   {
      int read;
      final byte[] buf = new byte[2048];
      while ((read = in.read(buf)) != -1)
      {
         out.write(buf, 0, read);
      }
   }
}
