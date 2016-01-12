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

package org.jsonschema2pojo.rules;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.net.URL;

import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public class SchemaRuleTest {

    private static final String NODE_NAME = "nodeName";
    private static final String TARGET_CLASS_NAME = SchemaRuleTest.class.getName() + ".DummyClass";

    private RuleFactory mockRuleFactory = mock(RuleFactory.class);
    private SchemaRule rule = new SchemaRule(mockRuleFactory);

    @Test
    public void refsToOtherSchemasAreLoaded() throws URISyntaxException, JClassAlreadyExistsException {
        // GIVEN
        URL schemaUrl = getClass().getResource("/schema/address.json");
        Schema parentSchema = new Schema(null, schemaUrl, null, null);

        ObjectNode schemaWithRef = new ObjectMapper().createObjectNode();
        schemaWithRef.put("$ref", schemaUrl.toString());

        JDefinedClass jclass = new JCodeModel()._class(TARGET_CLASS_NAME);

        TypeRule mockTypeRule = mock(TypeRule.class);
        when(mockRuleFactory.getTypeRule()).thenReturn(mockTypeRule);
        when(mockRuleFactory.getSchemaStore()).thenReturn(new SchemaStore());

        // WHEN
        rule.apply(NODE_NAME, schemaWithRef, jclass, parentSchema);

        // THEN
        ArgumentCaptor<JsonNode> captureJsonNode = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<Schema> captureSchema = ArgumentCaptor.forClass(Schema.class);
        verify(mockTypeRule).apply(eq(NODE_NAME), captureJsonNode.capture(), eq(jclass), captureSchema.capture());

        assertThat(captureSchema.getValue().getUrl(), is(equalTo(schemaUrl)));
        assertThat(captureSchema.getValue().getContent(), is(equalTo(captureJsonNode.getValue())));

        assertThat(captureJsonNode.getValue().get("description").asText(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));
    }

    @Test
    public void existingTypeIsUsedWhenTypeIsAlreadyGenerated() throws URISyntaxException {
        // GIVEN
        JType previouslyGeneratedType = mock(JType.class);

        URL schemaUrl = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema schema = schemaStore.create(null, schemaUrl);
        schema.setJavaType(previouslyGeneratedType);

        when(mockRuleFactory.getSchemaStore()).thenReturn(schemaStore);

        ObjectNode schemaNode = new ObjectMapper().createObjectNode();
        schemaNode.put("$ref", schemaUrl.toString());

        // WHEN
        JType result = rule.apply(NODE_NAME, schemaNode, null, schema);

        // THEN
        assertThat(result, is(sameInstance(previouslyGeneratedType)));
    }
}
