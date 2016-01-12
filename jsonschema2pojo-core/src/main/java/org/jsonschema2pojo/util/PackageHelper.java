/**
 * Copyright Â© 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.util;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.jsonschema2pojo.GenerationConfig;

public class PackageHelper {
    
    private final GenerationConfig generationConfig;

    public PackageHelper(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    public String getPackageName(URI id) {
        Map<URI, String> map = generationConfig.getIdMappings();
        for(Entry<URI, String> entry : map.entrySet()) {
            if(entry.getKey().equals(id)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
