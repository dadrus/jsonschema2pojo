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
     * contents of the given reference as a URL. If a schema with the given
     * reference is already known, then a reference to the original schema will
     * be returned.
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
     * Create or look up a new schema using the given schema as a source for the
     * lookup and the path as an absolute or relative reference. If a schema
     * with the given source and path is already known, then a reference to the
     * original schema will be returned, otherwise the new schema with required
     * hierarchy is created.
     * 
     * @param source
     *            the schema which is the parent or the source for the lookup of
     *            the schema to be created.
     * @param path
     *            the path of the schema to be returned (will be used to create
     *            a complete {@link URL} by resolving this path against the
     *            source schema's reference)
     * 
     * @return a schema object containing the contents of the given path
     * 
     * @throws IllegalArgumentException
     *             if the path is an unsupported URL (e.g. unknown protocol) or
     *             if the path is a relative path within the source schema to a
     *             not existing type definition.
     */
    public Schema create(Schema source, String path) {

        if (path.equals("#")) {
            return source;
        }

        path = stripEnd(path, "#?&/");

        try {
            URL url;
            if (source == null || source.getUrl() == null) {
                url = URLUtil.getURL(path);
            } else {
                url = URLUtil.resolveURL(source.getUrl().toURI(), path);
            }

            if (selfReferenceWithoutParentFile(source, path)) {
                schemas.put(url, new Schema(source.getId(), url, fragmentResolver.resolve(source.getContent(), path), source));
                return schemas.get(url);
            }

            if (schemas.containsKey(url)) {
                return schemas.get(url);
            }

            if (source == null) {
                URL parentUrl = URLUtil.getURL(removeFragment(url).toString());
                JsonNode content = contentResolver.resolve(parentUrl.toURI());
                source = new Schema(null, parentUrl, content, null);
                schemas.put(parentUrl, source);
            }

            if (url.toString().contains("#")) {
                if (!url.toString().startsWith(source.getUrl().toString())) {
                    // not a self reference
                    // maybe this is not allowed by the json schema standard. But having this we can reference types from other schema files
                    source = create(null, removeFragment(url).toString());
                }

                schemas.put(url, resolveSchema(source, url));
            } else if (!schemas.containsKey(url)) {
                JsonNode content = contentResolver.resolve(url.toURI());
                schemas.put(url, new Schema(null, url, content, null));
            }

            return schemas.get(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Schema resolveSchema(Schema source, URL url) {
        String contentPath = '#' + substringAfter(url.toString(), "#");
        while(source != null) {
            try {
                JsonNode childContent = fragmentResolver.resolve(source.getContent(), contentPath);
                return new Schema(null, url, childContent, source);
            } catch(IllegalArgumentException e) {
                source = source.getParent();
            }
        }
        throw new IllegalArgumentException("No fragment defined for: " + url.toString());
    }

    private JsonNode resolveContent(Schema source, URL url) {
        try {
            return fragmentResolver.resolve(source.getContent(), '#' + substringAfter(url.toString(), "#"));
        } catch(IllegalArgumentException e) {
            if(source.getParent() == null) {
                throw e;
            }
            return resolveContent(source.getParent(), url);
        }
    }

    protected boolean selfReferenceWithoutParentFile(Schema parent, String path) {
        return parent != null && (parent.getUrl() == null || parent.getUrl().toString().startsWith("#/")) && path.startsWith("#/");
    }

    public synchronized void clearCache() {
        schemas.clear();
    }

}
