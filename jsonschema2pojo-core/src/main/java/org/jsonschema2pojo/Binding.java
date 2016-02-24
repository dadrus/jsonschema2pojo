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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

public class Binding {
    
    /**
     * A json type which shall be replaced by a Java type
     */
    private String jsonType;
    
    /**
     * A Java type which shall be used for the given json type 
     */
    private String javaType;
    
    /**
     * An implementation of a type adapter which is responsible for serialization and deserialisation of the Java type to the json type
     */
    private String typeAdapter;
    
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }
    
    public void setJsonType(String jsonType) {
        this.jsonType = jsonType;
    }
    
    public void setTypeAdapter(String typeAdapter) {
        this.typeAdapter = typeAdapter;
    }
    
    public String getJsonType() {
        return jsonType;
    }
    
    public String getJavaType() {
        return javaType;
    }

    public String getTypeAdapter() {
        return typeAdapter;
    }

    public JType getJavaType(JCodeModel jCodeModel) {
        return jCodeModel.ref(javaType);
    }
    
    public JType getTypeAdapter(JCodeModel jCodeModel) {
        return typeAdapter != null ? jCodeModel.ref(typeAdapter) : null;
    }

}
