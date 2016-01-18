/**
 * Copyright ¬© 2010-2014 Nokia
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jsonschema2pojo.URLProtocol;

import com.fasterxml.jackson.databind.JsonNode;

public final class URLUtil {

    private URLUtil() {
    }

    public static URLProtocol parseProtocol(String input) {
        return URLProtocol.fromString(StringUtils.substringBefore(input, ":"));
    }

    public static URL parseURL(String input) {
        try {
            switch (parseProtocol(input)) {
                case NO_PROTOCOL:
                    return new File(input).toURI().toURL();
                default:
                    return URI.create(input).toURL();
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Unable to parse source: %s", input), e);
        }
    }

    public static File getFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("URL contains an invalid URI syntax: %s", url), e);
        }
    }

    /**
     * Resolve the given resource location to a {@link java.net.URL}.
     * <p>
     * This function simple returns the URL that the given
     * {@code resourceLocation} would correspond.
     * 
     * <p>
     * The resource location can be one of:
     * <li>a "classpath:" pseudo URL,</li>
     * <li>a "java:" pseudo URL,</li>
     * <li>a "resource:" pseudo URL,</li>
     * <li>a "file:" URL,</li>
     * <li>a web ("http:, etc") URL,</li>
     * <li>or a plain file path</li>
     * <p>
     * 
     * @param resourceLocation
     *            the resource location to resolve.
     * 
     * @return a corresponding {@link URL} object
     * 
     * @throws FileNotFoundException
     *             if the resource cannot be resolved to a URL
     */
    public static URL getURL(String resourceLocation) throws FileNotFoundException {
        if (resourceLocation.startsWith(URLProtocol.CLASSPATH.getProtocol())) {
            return URLProtocol.CLASSPATH.toUrl(resourceLocation);
        } else if (resourceLocation.startsWith(URLProtocol.JAVA.getProtocol())) {
            return URLProtocol.JAVA.toUrl(resourceLocation);
        } else if (resourceLocation.startsWith(URLProtocol.RESOURCE.getProtocol())) {
            return URLProtocol.RESOURCE.toUrl(resourceLocation);
        }

        try {
            return new URL(resourceLocation);
        } catch (MalformedURLException ex) {
            try {
                return new File(resourceLocation).toURI().toURL();
            } catch (MalformedURLException ex2) {
                throw new FileNotFoundException("Resource location [" + resourceLocation + "] is neither a URL not a well-formed file path");
            }
        }
    }

    /**
     * Constructs a new URL by parsing the given path fragment and then
     * resolving it against the {@link URI}
     * 
     * @param baseUri
     *            the base {@link URI} to resolve the given fragment against.
     * @param fragment
     *            the path fragment to be resolved.
     * 
     * @return a resolved {@link URL} object.
     * 
     * @throws FileNotFoundException
     *             if the resource cannot be resolved to a URL
     * 
     * @see URLUtil#getURL which is used internally.
     */
    public static URL resolveURL(URI baseUri, String fragment) throws FileNotFoundException {
        URI uri = baseUri.resolve(fragment);
        return getURL(uri.toString());
    }
    
    public static String getSchemaName(URL baseUrl, JsonNode node) {
        if (node.has("$ref")) {
            final String reference = node.get("$ref").asText();

            if (reference.startsWith("#/")) {
                // self reference with type definition
                // use the name of the type

                // TODO: what about nested references? How to handle them: e.g: "$ref": "#/definitions/level/sublevel/actual_element"
                // with type information available only on actual_element and level/sublevel used as namespaces to avoid type conflicts
                // e.g. level could also contain an actual_element definition which differs from the sublevel/actual_element.
                // Maybe it is worth to generate java subpackages for such cases if the id is not present. 
                return reference.substring(reference.lastIndexOf('/') + 1);
            } else {
                // global reference (other file, absolute url to same file, url, whatever)
                try {
                    URL url = (baseUrl == null ? getURL(reference) : resolveURL(baseUrl.toURI(), reference));
                    
                    String fragment = (url.toURI().getFragment() == null ? "" : url.toURI().getFragment());

                    if (fragment.isEmpty()) {
                        // the type name is given by the file name
                        return FilenameUtils.getBaseName(url.getPath());
                    } else {
                        // the type name is defined by the fragment
                        return fragment.contains("/") ? 
                        StringUtils.substringAfterLast(fragment, "/") : fragment;
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        } else {
            // schema does not have a name
            return null;
        }
    }
}
