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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.rules.SchemaRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class SchemaMapperTest {

    private static final String DEFAULT_PACKAGE_NAME = "";

    @Test
    public void generateReadsSchemaAsObject() throws IOException {

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        GenerationConfig config = spy(new DefaultGenerationConfig());
        when(config.getTargetPackage()).thenReturn("com.example.package");
        when(mockRuleFactory.getGenerationConfig()).thenReturn(config);

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "Address", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), capturePackage.capture(), isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is(DEFAULT_PACKAGE_NAME));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }

    @Test
    public void generateCreatesSchemaFromExampleJsonWhenInJsonMode() throws IOException {

        URL schemaContent = this.getClass().getResource("/schema/address.json");

        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getTargetPackage()).thenReturn("com.example.package");
        when(mockGenerationConfig.getSourceType()).thenReturn(SourceType.JSON);

        final SchemaGenerator mockSchemaGenerator = mock(SchemaGenerator.class);
        when(mockSchemaGenerator.schemaFromExample(schemaContent)).thenReturn(schemaNode);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        new SchemaMapper(mockRuleFactory, mockSchemaGenerator).generate(new JCodeModel(), "Address", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);

        verify(mockSchemaRule).apply(eq("Address"), eq(schemaNode), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is(DEFAULT_PACKAGE_NAME));

    }

    @Test
    public void generateCreatesSchemaFromExampleJSONAsStringInput() throws IOException {

        String jsonContent = new Scanner(this.getClass().getResourceAsStream("/example-json/user.json")).useDelimiter("\\Z").next();

        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final GenerationConfig mockGenerationConfig = mock(GenerationConfig.class);
        when(mockGenerationConfig.getTargetPackage()).thenReturn("com.example.package");
        when(mockGenerationConfig.getSourceType()).thenReturn(SourceType.JSON);

        final SchemaGenerator mockSchemaGenerator = mock(SchemaGenerator.class);
        when(mockSchemaGenerator.schemaFromExample(new ObjectMapper().readTree(jsonContent))).thenReturn(schemaNode);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        when(mockRuleFactory.getGenerationConfig()).thenReturn(mockGenerationConfig);

        new SchemaMapper(mockRuleFactory, mockSchemaGenerator).generate(new JCodeModel(), "User", jsonContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);

        verify(mockSchemaRule).apply(eq("User"), eq(schemaNode), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is(DEFAULT_PACKAGE_NAME));
    }

    @Test
    public void generateCreatesSchemaFromSchemaAsStringInput() throws IOException {

        String schemaContent = new Scanner(this.getClass().getResourceAsStream("/schema/address.json")).useDelimiter("\\Z").next();

        final SchemaRule mockSchemaRule = mock(SchemaRule.class);

        final RuleFactory mockRuleFactory = mock(RuleFactory.class);
        when(mockRuleFactory.getSchemaRule()).thenReturn(mockSchemaRule);
        GenerationConfig config = spy(new DefaultGenerationConfig());
        when(config.getTargetPackage()).thenReturn("com.example.package");
        when(mockRuleFactory.getGenerationConfig()).thenReturn(config);

        new SchemaMapper(mockRuleFactory, new SchemaGenerator()).generate(new JCodeModel(), "Address", schemaContent);

        ArgumentCaptor<JPackage> capturePackage = ArgumentCaptor.forClass(JPackage.class);
        ArgumentCaptor<JsonNode> captureNode = ArgumentCaptor.forClass(JsonNode.class);

        verify(mockSchemaRule).apply(eq("Address"), captureNode.capture(), capturePackage.capture(), Mockito.isA(Schema.class));

        assertThat(capturePackage.getValue().name(), is(DEFAULT_PACKAGE_NAME));
        assertThat(captureNode.getValue(), is(notNullValue()));

    }
}
