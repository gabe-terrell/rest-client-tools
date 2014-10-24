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
package com.opower.rest.client.generator.specimpl;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *
 */
@SuppressWarnings("serial")
public class MultivaluedMapImpl<K, V> extends HashMap<K, List<V>> implements MultivaluedMap<K, V> {
    public void putSingle(K key, V value) {
        List<V> list = new ArrayList<V>();
        list.add(value);
        put(key, list);
    }

    public final void add(K key, V value) {
        getList(key).add(value);
    }


    public final void addMultiple(K key, Collection<V> values) {
        getList(key).addAll(values);
    }

    public V getFirst(K key) {
        List<V> list = get(key);
        return list == null ? null : list.get(0);
    }

    public final List<V> getList(K key) {
        List<V> list = get(key);
        if (list == null)
            put(key, list = new ArrayList<V>());
        return list;
    }
}
