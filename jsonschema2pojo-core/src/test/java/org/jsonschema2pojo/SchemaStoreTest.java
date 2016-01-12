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

import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;

public class SchemaStoreTest {

    @Test
    public void createWithAbsolutePath() throws URISyntaxException {
        // GIVEN
        URL schemaUrl = getClass().getResource("/schema/address.json");

        // WHEN
        Schema schema = new SchemaStore().create(null, schemaUrl);

        // THEN
        assertThat(schema, is(notNullValue()));
        assertThat(schema.getUrl(), is(equalTo(schemaUrl)));
        assertThat(schema.getId(), nullValue());
        assertThat(schema.getParent(), nullValue());
        assertThat(schema.getContent().has("description"), is(true));
        assertThat(schema.getContent().get("description").asText(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));
    }

    @Test
    public void createWithRelativePath() throws URISyntaxException, MalformedURLException {
        // GIVEN
        URL addressSchemaUrl = getClass().getResource("/schema/address.json");
        URL expectedUri = new URL(removeEnd(addressSchemaUrl.toString(), "address.json") + "enum.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, addressSchemaUrl);
        
        // WHEN
        Schema enumSchema = schemaStore.create(addressSchema, "enum.json");

        // THEN
        assertThat(enumSchema, is(notNullValue()));
        assertThat(enumSchema.getUrl(), is(equalTo(expectedUri)));
        assertThat(enumSchema.getId(), is(nullValue()));
        assertThat(enumSchema.getParent(), is(nullValue()));
        assertThat(enumSchema.getContent().has("enum"), is(true));

    }

    @Test
    public void createWithSelfRef() throws URISyntaxException {
        // GIVEN
        URL schemaUri = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, schemaUri);
        
        // WHEN
        Schema selfRefSchema = schemaStore.create(addressSchema, "#");

        // THEN
        assertThat(addressSchema, is(sameInstance(selfRefSchema)));
    }

    @Test
    public void createWithFragmentResolutionFromTopLevel() throws URISyntaxException, MalformedURLException {
        // GIVEN
        URL addressSchemaUrl = getClass().getResource("/schema/address.json");
        URL expectedUrl = new URL(addressSchemaUrl.toString() + "#/properties/post-office-box");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, addressSchemaUrl);

        // WHEN
        Schema innerSchema = schemaStore.create(addressSchema, "#/properties/post-office-box");

        // THEN
        assertThat(innerSchema, is(notNullValue()));
        assertThat(innerSchema.getUrl(), is(equalTo(expectedUrl)));
        assertThat(innerSchema.getId(), is(nullValue()));
        assertThat(innerSchema.getParent(), is(addressSchema));
        assertThat(innerSchema.getContent().has("type"), is(true));
        assertThat(innerSchema.getContent().get("type").asText(), is("string"));
    }
    
    @Test
    public void createWithFragmentResolutionFromSubType() throws URISyntaxException, MalformedURLException {
        // GIVEN
        URL selfRefsSchemaUrl = getClass().getResource("/schema/selfreferences.json");
        URL expectedUrl = new URL(selfRefsSchemaUrl.toString() + "#/definitions/other");

        SchemaStore schemaStore = new SchemaStore();
        Schema topSchema = schemaStore.create(null, selfRefsSchemaUrl);
        Schema childSchema = schemaStore.create(topSchema, "#/definitions/child");

        // WHEN
        Schema otherSchema = schemaStore.create(childSchema, "#/definitions/other");

        // THEN
        assertThat(otherSchema, is(notNullValue()));
        assertThat(otherSchema.getUrl(), is(equalTo(expectedUrl)));
        assertThat(otherSchema.getId(), is(nullValue()));
        assertThat(otherSchema.getParent(), is(topSchema));
        assertThat(otherSchema.getContent().has("type"), is(true));
        assertThat(otherSchema.getContent().get("type").asText(), is("object"));
    }
    
    @Test
    public void createWithExternalFragmentResolution() throws URISyntaxException, MalformedURLException {
        // GIVEN
        URL addressSchemaUrl = getClass().getResource("/schema/address.json");
        URL arraySchemaUrl = getClass().getResource("/schema/array.json");
        URL expectedUrl = new URL(arraySchemaUrl.toString() + "#/definitions/product-type");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, addressSchemaUrl);

        // WHEN
        Schema externalSchema = schemaStore.create(addressSchema, expectedUrl.toString());

        // THEN
        assertThat(externalSchema, is(notNullValue()));
        assertThat(externalSchema.getUrl(), is(equalTo(expectedUrl)));
        assertThat(externalSchema.getId(), is(nullValue()));
        assertThat(externalSchema.getParent(), is(not(nullValue())));
        assertThat(externalSchema.getParent(), is(not(addressSchema)));
        assertThat(externalSchema.getParent().getUrl(), is(arraySchemaUrl));
        assertThat(externalSchema.getContent().has("type"), is(true));
        assertThat(externalSchema.getContent().get("type").asText(), is("object"));
    }

    @Test
    public void schemaAlreadyReadIsReused() throws URISyntaxException {
        // GIVEN
        URL schemaUrl = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema schema1 = schemaStore.create(null, schemaUrl);

        // WHEN
        Schema schema2 = schemaStore.create(null, schemaUrl);

        // THEN
        assertThat(schema1, is(sameInstance(schema2)));
    }

    @Test
    public void setIfEmptyOnlySetsIfEmpty() throws URISyntaxException {
        // GIVEN
        JType firstClass = mock(JDefinedClass.class);
        JType secondClass = mock(JDefinedClass.class);

        URL schemaUri = getClass().getResource("/schema/address.json");

        Schema schema = new SchemaStore().create(null, schemaUri);

        // WHEN
        schema.setJavaTypeIfEmpty(firstClass);
        schema.setJavaTypeIfEmpty(secondClass);

        // THEN
        assertThat(schema.getJavaType(), is(equalTo(firstClass)));
        assertThat(schema.getJavaType(), is(not(equalTo(secondClass))));
    }
}
