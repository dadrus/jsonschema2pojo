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

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

public class URLUtilTest {
    
    @Test
    public void getClassPathURL() throws FileNotFoundException {
        URL url = URLUtil.getURL("classpath:schema/address.json");
        assertThat(url, notNullValue());
    }
    
    @Test
    public void getJavaPathURL() throws FileNotFoundException {
        URL url = URLUtil.getURL("java:schema/address.json");
        assertThat(url, notNullValue());
    }
    
    @Test
    public void getResourcePathURL() throws FileNotFoundException {
        URL url = URLUtil.getURL("resource:schema/address.json");
        assertThat(url, notNullValue());
    }
    
    @Test
    public void getFilePathURL() throws FileNotFoundException {
        URL url = URLUtil.getURL("schema/address.json");
        assertThat(url, notNullValue());
    }
    
    @Test(expected = FileNotFoundException.class)
    public void getNotExistingClassPathURL() throws FileNotFoundException {
        URLUtil.getURL("classpath:schema/does_not_exist.json");
    }
    
    @Test(expected = FileNotFoundException.class)
    public void getNotExistingJavaPathURL() throws FileNotFoundException {
        URLUtil.getURL("java:schema/does_not_exist.json");
    }
    
    @Test(expected = FileNotFoundException.class)
    public void getNotExistingResourcePathURL() throws FileNotFoundException {
        URLUtil.getURL("resource:schema/does_not_exist.json");
    }
    
    @Test
    public void getNotExistingFilePathURL() throws FileNotFoundException {
        // files are special case
        URL url = URLUtil.getURL("schema/does_not_exist.json");
        assertThat(url, notNullValue());
    }
    
    @Test
    public void resolveFilePathURL() throws FileNotFoundException, URISyntaxException {
        URL resource = URLUtilTest.class.getClassLoader().getResource("schema/address.json");
        URL url = URLUtil.resolveURL(resource.toURI(), "#/test");
        assertThat(url.toString(), endsWith("schema/address.json#/test"));
    }
}
