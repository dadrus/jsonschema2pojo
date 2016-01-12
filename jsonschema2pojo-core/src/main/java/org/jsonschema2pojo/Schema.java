/**
 * Copyright © 2010-2014 Nokia
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
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JType;

/**
 * A JSON Schema document.
 */
public class Schema {

    private final URL url;
    private URI id;
    private final JsonNode content;
    private final Schema parent;
    private JType javaType;

    public Schema(URI id, URL url, JsonNode content, Schema parent) {
        this.url = url;
        this.id = id;
        this.content = content;
        this.parent = parent;
    }

    public JType getJavaType() {
        return javaType;
    }

    public void setJavaType(JType javaType) {
        this.javaType = javaType;
    }

    public void setJavaTypeIfEmpty(JType javaType) {
        if (this.getJavaType() == null) {
            this.setJavaType(javaType);
        }
    }

    public URL getUrl() {
        return url;
    }
    
    public URI getId() {
        if(id == null) {
            id = getId(this);
        }
        return id;
    }
    
    private URI getId(Schema source) {
        if(source != null) {
            if(source.getContent().has("id")) {
                return URI.create(source.getContent().get("id").asText());
            } else {
                return getId(source.parent);
            }
        }
        return null;
    }

    public JsonNode getContent() {
        return content;
    }

    public Schema getParent() {
        return parent;
    }
    
    public boolean isGenerated() {
        return javaType != null;
    }

}
