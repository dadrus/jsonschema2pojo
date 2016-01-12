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

import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsonschema2pojo.util.URLUtil;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaStore {

    protected Map<URL, Schema> schemas = new HashMap<URL, Schema>();

    protected FragmentResolver fragmentResolver = new FragmentResolver();
    protected ContentResolver contentResolver = new ContentResolver();

    /**
     * Create or look up a new schema which has the given reference and read the
     * contents of the given reference as a URL. If a schema with the given reference is
     * already known, then a reference to the original schema will be returned.
     * 
     * @param reference
     *            the reference of the schema being created
     * @return a schema object containing the contents of the given path
     */
    public synchronized Schema create(URI id, URL reference) {

        if (!schemas.containsKey(reference)) {

            JsonNode content = contentResolver.resolve(removeFragment(reference));

            if (reference.toString().contains("#")) {
                JsonNode childContent = fragmentResolver.resolve(content, '#' + substringAfter(reference.toString(), "#"));
                schemas.put(reference, new Schema(id, reference, childContent, null));
            } else {
                schemas.put(reference, new Schema(id, reference, content, null));
            }
        }

        return schemas.get(reference);
    }

    protected URI removeFragment(URL reference) {
        return URI.create(substringBefore(reference.toString(), "#"));
    }

    /**
     * Create or look up a new schema using the given schema as a parent and the
     * path as a relative reference. If a schema with the given parent and
     * relative path is already known, then a reference to the original schema
     * will be returned.
     * 
     * @param parent
     *            the schema which is the parent of the schema to be created.
     * @param path
     *            the relative path of this schema (will be used to create a
     *            complete URI by resolving this path against the parent
     *            schema's reference)
     * @return a schema object containing the contents of the given path
     */
    @SuppressWarnings("PMD.UselessParentheses")
    public Schema create(Schema parent, String path) {

        if (path.equals("#")) {
            return parent;
        }
        
        path = stripEnd(path, "#?&/");
        
        URL url;
        try {
            if(parent == null || parent.getUrl() == null) {
                url = URLUtil.getURL(path);
            } else {
                url = URLUtil.resolveURL(parent.getUrl().toURI(), path);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        if (selfReferenceWithoutParentFile(parent, path)) {
            
            schemas.put(url, new Schema(parent.getId(), url, fragmentResolver.resolve(parent.getContent(), path), parent));
            return schemas.get(url);
        }
        
        return create(parent.getId(), url);

    }

    protected boolean selfReferenceWithoutParentFile(Schema parent, String path) {
        return parent != null && (parent.getUrl() == null || parent.getUrl().toString().startsWith("#/")) && path.startsWith("#/");
    }

    public synchronized void clearCache() {
        schemas.clear();
    }

}
