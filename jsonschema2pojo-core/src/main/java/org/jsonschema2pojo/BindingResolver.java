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

    public Binding getTypeBinding(URI namespace, String typeName) {
        String absoluteName = namespace == null ? typeName : namespace.toString() + "/" + typeName;
        for(Binding binding : generationConfig.getTypeBindings()) {
            if(binding.getJsonType().equals(absoluteName)) {
                return binding;
            }
        }
        return null;
    }

    public Binding getFormatBinding(String format) {
        for(Binding binding : generationConfig.getFormatBindings()) {
            if(binding.getJsonType().equals(format)) {
                return binding;
            }
        }
        return null;
    }

    public Binding getMediaTypeBinding(String mediaType) {
        for(Binding binding : generationConfig.getMediaTypeBindings()) {
            if(binding.getJsonType().equals(mediaType)) {
                return binding;
            }
        }
        return null;
    }

    public Binding getMediaEncodingBinding(String binaryEncoding) {
        for(Binding binding : generationConfig.getMediaEncodingBindings()) {
            if(binding.getJsonType().equals(binaryEncoding)) {
                return binding;
            }
        }
        return null;
    }
    
    public Binding getTypeBinding(JType clazz) {
        for(Binding binding : generationConfig.getTypeBindings()) {
            if(clazz.fullName().equals(binding.getJavaType())) {
                return binding;
            }
        }
        
        for(Binding binding : generationConfig.getFormatBindings()) {
            if(clazz.fullName().equals(binding.getJavaType())) {
                return binding;
            }
        }
        
        for(Binding binding : generationConfig.getMediaTypeBindings()) {
            if(clazz.fullName().equals(binding.getJavaType())) {
                return binding;
            }
        }
        
        for(Binding binding : generationConfig.getMediaEncodingBindings()) {
            if(clazz.fullName().equals(binding.getJavaType())) {
                return binding;
            }
        }
        return null;
    }

}
