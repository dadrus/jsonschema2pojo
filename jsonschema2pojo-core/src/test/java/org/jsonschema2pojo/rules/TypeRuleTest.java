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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.util.PackageHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class TypeRuleTest {

    private GenerationConfig config = mock(GenerationConfig.class);
    private RuleFactory ruleFactory = mock(RuleFactory.class);

    private TypeRule rule = new TypeRule(ruleFactory);

    private JPackage jpackage;
    private ObjectNode objectNode;

    @Before
    public void wireUpConfig() {
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
        jpackage = new JCodeModel()._package(getClass().getPackage().getName());
        objectNode = new ObjectMapper().createObjectNode();
    }

    @Test
    public void applyGeneratesString() {
        // GIVEN
        objectNode.put("type", "string");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(String.class.getName()));
    }

    @Test
    public void applyGeneratesDate() {
        // GIVEN
        TextNode formatNode = TextNode.valueOf("date-time");
        objectNode.put("type", "string");
        objectNode.put("format", formatNode);

        JType mockDateType = mock(JType.class);
        FormatRule mockFormatRule = mock(FormatRule.class);
        when(mockFormatRule.apply(eq("fooBar"), eq(formatNode), Mockito.isA(JType.class), isNull(Schema.class))).thenReturn(mockDateType);
        when(ruleFactory.getFormatRule()).thenReturn(mockFormatRule);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result, equalTo(mockDateType));
    }

    @Test
    public void applyGeneratesInteger() {
        // GIVEN
        objectNode.put("type", "integer");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Integer.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerPrimitive() {
        // GIVEN
        objectNode.put("type", "integer");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeIntegerPrimitive() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("javaType", "int");

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("int"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeInteger() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("javaType", "java.lang.Integer");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.lang.Integer"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitive() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("javaType", "long");

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLong() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("javaType", "java.lang.Long");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.lang.Long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumGreaterThanIntegerMax() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumGreaterThanIntegerMax() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMaximumLessThanIntegerMin() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMaximumLessThanIntegerMin() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("maximum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumLessThanIntegerMin() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumLessThanIntegerMin() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MIN_VALUE - 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongPrimitiveWhenMinimumGreaterThanIntegerMax() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("long"));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeLongWhenMinimumGreaterThanIntegerMax() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("minimum", Integer.MAX_VALUE + 1L);

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Long.class.getName()));
    }

    @Test
    public void applyGeneratesIntegerUsingJavaTypeBigInteger() {
        // GIVEN
        objectNode.put("type", "integer");
        objectNode.put("javaType", "java.math.BigInteger");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.math.BigInteger"));
    }

    @Test
    public void applyGeneratesNumber() {
        // GIVEN
        objectNode.put("type", "number");

        when(config.isUseDoubleNumbers()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Double.class.getName()));
    }

    @Test
    public void applyGeneratesNumberPrimitive() {
        // GIVEN
        objectNode.put("type", "number");

        when(config.isUsePrimitives()).thenReturn(true);
        when(config.isUseDoubleNumbers()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloatPrimitive() {
        // GIVEN
        objectNode.put("type", "number");
        objectNode.put("javaType", "float");

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeFloat() {
        // GIVEN
        objectNode.put("type", "number");
        objectNode.put("javaType", "java.lang.Float");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.lang.Float"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDoublePrimitive() {
        // GIVEN
        objectNode.put("type", "number");
        objectNode.put("javaType", "double");

        when(config.isUsePrimitives()).thenReturn(false);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeDouble() {
        // GIVEN
        objectNode.put("type", "number");
        objectNode.put("javaType", "java.lang.Double");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.lang.Double"));
    }

    @Test
    public void applyGeneratesNumberUsingJavaTypeBigDecimal() {
        // GIVEN
        objectNode.put("type", "number");
        objectNode.put("javaType", "java.math.BigDecimal");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("java.math.BigDecimal"));
    }

    @Test
    public void applyGeneratesBoolean() {
        // GIVEN
        objectNode.put("type", "boolean");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Boolean.class.getName()));
    }

    @Test
    public void applyGeneratesBooleanPrimitive() {
        // GIVEN
        objectNode.put("type", "boolean");

        when(config.isUsePrimitives()).thenReturn(true);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is("boolean"));
    }

    @Test
    public void applyGeneratesAnyAsObject() {
        // GIVEN
        objectNode.put("type", "any");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesNullAsObject() {
        // GIVEN
        objectNode.put("type", "null");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyGeneratesArray() {
        // GIVEN
        objectNode.put("type", "array");

        JClass mockArrayType = mock(JClass.class);
        ArrayRule mockArrayRule = mock(ArrayRule.class);
        when(mockArrayRule.apply("fooBar", objectNode, jpackage, null)).thenReturn(mockArrayType);
        when(ruleFactory.getArrayRule()).thenReturn(mockArrayRule);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result, is((JType) mockArrayType));
    }

    @Test
    public void applyGeneratesCustomObject() {
        // GIVEN
        Schema schema = new Schema(URI.create("http://test.com"), null, null, null);
        objectNode.put("type", "object");

        PackageHelper packageHelper = mock(PackageHelper.class);
        JDefinedClass mockObjectType = mock(JDefinedClass.class);
        ObjectRule mockObjectRule = mock(ObjectRule.class);

        when(mockObjectRule.apply(eq("fooBar"), eq(objectNode), any(JPackage.class), any(Schema.class))).thenReturn(mockObjectType);
        when(ruleFactory.getObjectRule()).thenReturn(mockObjectRule);
        when(ruleFactory.getPackageHelper()).thenReturn(packageHelper);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, schema);

        // THEN
        assertThat(result, is((JType) mockObjectType));
    }

    @Test
    public void applyChoosesObjectOnUnrecognizedType() {
        // GIVEN
        objectNode.put("type", "unknown");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Object.class.getName()));
    }

    @Test
    public void applyDefaultsToTypeAnyObject() {
        // GIVEN

        // WHEN
        JType result = rule.apply("fooBar", objectNode, jpackage, null);

        // THEN
        assertThat(result.fullName(), is(Object.class.getName()));
    }
    
    @Test
    public void applyGeneratesEnumInAPackage() throws JClassAlreadyExistsException {
        // GIVEN
        Schema schema = new Schema(URI.create("http://test.com"), null, null, null);
        ArrayNode enumNode = objectNode.arrayNode();
        enumNode.add("A");
        enumNode.add("B");
        enumNode.add("C");
        objectNode.put("enum", enumNode);
        
        PackageHelper packageHelper = mock(PackageHelper.class);
        JDefinedClass mockEnumType = mock(JDefinedClass.class);
        EnumRule mockEnumRule = mock(EnumRule.class);
        GenerationConfig config = mock(GenerationConfig.class);

        when(mockEnumRule.apply(eq("fooBar"), eq(objectNode), any(JClassContainer.class), any(Schema.class))).thenReturn(mockEnumType);
        when(ruleFactory.getEnumRule()).thenReturn(mockEnumRule);
        when(ruleFactory.getPackageHelper()).thenReturn(packageHelper);
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
        
        JDefinedClass clazz = jpackage._class("Parent");

        // WHEN
        JType result = rule.apply("fooBar", objectNode, clazz, schema);

        // THEN
        assertThat(result, is((JType) mockEnumType));
        
        ArgumentCaptor<JClassContainer> containerCaptor = ArgumentCaptor.forClass(JClassContainer.class);
        verify(mockEnumRule).apply(eq("fooBar"), eq(objectNode), containerCaptor.capture(), eq(schema));
        JClassContainer container = containerCaptor.getValue();
        assertThat(container, is(instanceOf(JPackage.class)));
        JPackage jPackage =(JPackage) container;
        assertThat(jPackage.name(), equalTo("com.test"));
    }

    @Test
    public void applyGeneratesEnumInAClass() throws JClassAlreadyExistsException {
        // GIVEN
        Schema schema = new Schema(URI.create("http://test.com"), null, null, null);
        ArrayNode enumNode = objectNode.arrayNode();
        enumNode.add("A");
        enumNode.add("B");
        enumNode.add("C");
        objectNode.put("enum", enumNode);
        
        PackageHelper packageHelper = mock(PackageHelper.class);
        JDefinedClass mockEnumType = mock(JDefinedClass.class);
        EnumRule mockEnumRule = mock(EnumRule.class);
        GenerationConfig config = mock(GenerationConfig.class);

        when(mockEnumRule.apply(eq("fooBar"), eq(objectNode), any(JClassContainer.class), any(Schema.class))).thenReturn(mockEnumType);
        when(ruleFactory.getEnumRule()).thenReturn(mockEnumRule);
        when(ruleFactory.getPackageHelper()).thenReturn(packageHelper);
        when(ruleFactory.getGenerationConfig()).thenReturn(config);
        
        JDefinedClass clazz = jpackage._class("Parent");
        schema.setJavaType(clazz);

        // WHEN
        JType result = rule.apply("fooBar", objectNode, clazz, schema);

        // THEN
        assertThat(result, is((JType) mockEnumType));
        
        verify(mockEnumRule).apply(eq("fooBar"), eq(objectNode), eq(clazz), eq(schema));
    }
}
