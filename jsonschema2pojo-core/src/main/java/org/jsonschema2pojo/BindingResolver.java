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

package org.jsonschema2pojo;

import java.net.URI;

import com.sun.codemodel.JType;

public class BindingResolver {

    private GenerationConfig generationConfig;

    public BindingResolver(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    public TypeBinding getTypeBinding(URI namespace, String typeName) {
        String absoluteName = namespace == null ? typeName : namespace.toString() + "/" + typeName;
        for(TypeBinding binding : generationConfig.getTypeBindings()) {
            if(binding.getJsonType().equals(absoluteName)) {
                return binding;
            }
        }
        return null;
    }

    public TypeBinding getTypeBinding(JType clazz) {
        for(TypeBinding binding : generationConfig.getTypeBindings()) {
            if(clazz.fullName().equals(binding.getJavaType())) {
                return binding;
            }
        }
        return null;
    }

}
