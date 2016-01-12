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

        URL schemaUri = getClass().getResource("/schema/address.json");

        Schema schema = new SchemaStore().create(null, schemaUri);

        assertThat(schema, is(notNullValue()));
        assertThat(schema.getUrl(), is(equalTo(schemaUri)));
        assertThat(schema.getContent().has("description"), is(true));
        assertThat(schema.getContent().get("description").asText(), is(equalTo("An Address following the convention of http://microformats.org/wiki/hcard")));

    }

    @Test
    public void createWithRelativePath() throws URISyntaxException, MalformedURLException {

        URL addressSchemaUri = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, addressSchemaUri);
        Schema enumSchema = schemaStore.create(addressSchema, "enum.json");

        String expectedUri = removeEnd(addressSchemaUri.toString(), "address.json") + "enum.json";

        assertThat(enumSchema, is(notNullValue()));
        assertThat(enumSchema.getUrl(), is(equalTo(new URL(expectedUri))));
        assertThat(enumSchema.getContent().has("enum"), is(true));

    }

    @Test
    public void createWithSelfRef() throws URISyntaxException {

        URL schemaUri = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, schemaUri);
        Schema selfRefSchema = schemaStore.create(addressSchema, "#");

        assertThat(addressSchema, is(sameInstance(selfRefSchema)));

    }

    @Test
    public void createWithFragmentResolution() throws URISyntaxException, MalformedURLException {

        URL addressSchemaUri = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();
        Schema addressSchema = schemaStore.create(null, addressSchemaUri);
        Schema innerSchema = schemaStore.create(addressSchema, "#/properties/post-office-box");

        String expectedUri = addressSchemaUri.toString() + "#/properties/post-office-box";

        assertThat(innerSchema, is(notNullValue()));
        assertThat(innerSchema.getUrl(), is(equalTo(new URL(expectedUri))));
        assertThat(innerSchema.getContent().has("type"), is(true));
        assertThat(innerSchema.getContent().get("type").asText(), is("string"));

    }

    @Test
    public void schemaAlreadyReadIsReused() throws URISyntaxException {

        URL schemaUri = getClass().getResource("/schema/address.json");

        SchemaStore schemaStore = new SchemaStore();

        Schema schema1 = schemaStore.create(null, schemaUri);

        Schema schema2 = schemaStore.create(null, schemaUri);

        assertThat(schema1, is(sameInstance(schema2)));

    }

    @Test
    public void setIfEmptyOnlySetsIfEmpty() throws URISyntaxException {

        JType firstClass = mock(JDefinedClass.class);
        JType secondClass = mock(JDefinedClass.class);

        URL schemaUri = getClass().getResource("/schema/address.json");

        Schema schema = new SchemaStore().create(null, schemaUri);

        schema.setJavaTypeIfEmpty(firstClass);
        assertThat(schema.getJavaType(), is(equalTo(firstClass)));

        schema.setJavaTypeIfEmpty(secondClass);
        assertThat(schema.getJavaType(), is(not(equalTo(secondClass))));

    }

}
