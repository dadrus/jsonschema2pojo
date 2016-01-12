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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SchemaTest {

    @Test
    public void getIdSetByConstructor() {
        // GIVEN
        URI expectedId = URI.create("http://test.com");
        Schema schema = new Schema(expectedId, null, null, null);

        // WHEN
        URI id = schema.getId();

        // THEN
        assertThat(id, equalTo(expectedId));
    }

    @Test
    public void getIdFromOwnContent() {
        // GIVEN
        URI expectedId = URI.create("http://test.com");

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("id", expectedId.toString());

        Schema schema = new Schema(null, null, objectNode, null);

        // WHEN
        URI id = schema.getId();

        // THEN
        assertThat(id, equalTo(expectedId));
    }
    
    @Test
    public void getIdFromParentContent() {
        // GIVEN
        URI expectedId = URI.create("http://test.com");

        ObjectNode parentNode = new ObjectMapper().createObjectNode();
        parentNode.put("id", expectedId.toString());
        
        ObjectNode owntNode = new ObjectMapper().createObjectNode();
        parentNode.put("type", "string");

        Schema parent = new Schema(null, null, parentNode, null);
        Schema schema = new Schema(null, null, owntNode, parent);

        // WHEN
        URI id = schema.getId();

        // THEN
        assertThat(id, equalTo(expectedId));
    }

}
