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

package org.jsonschema2pojo.integration.ref;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.integration.util.CodeGenerationHelper;
import org.jsonschema2pojo.integration.util.Jsonschema2PojoRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.sun.codemodel.JCodeModel;

public class SelfRefIT {

    @ClassRule public static Jsonschema2PojoRule classSchemaRule = new Jsonschema2PojoRule();
    @Rule public Jsonschema2PojoRule schemaRule = new Jsonschema2PojoRule();

    private static Class<?> selfRefsClass;

    @BeforeClass
    public static void generateAndCompileEnum() throws ClassNotFoundException {

        ClassLoader selfRefsClassLoader = classSchemaRule.generateAndCompile("/schema/ref/selfRefs.json", "com.example");

        selfRefsClass = selfRefsClassLoader.loadClass("com.example.SelfRefs");

    }

    @Test
    public void selfRefUsedInAPropertyIsReadSuccessfully() throws NoSuchMethodException {

        Class<?> aClass = selfRefsClass.getMethod("getChildOfSelf").getReturnType();

        assertThat(aClass.getName(), is("com.example.SelfRefs"));
    }

    @Test
    public void selfRefUsedAsArrayItemsIsReadSuccessfully() throws NoSuchMethodException {

        Type listOfAType = selfRefsClass.getMethod("getArrayOfSelf").getGenericReturnType();
        Class<?> listEntryClass = (Class<?>) ((ParameterizedType) listOfAType).getActualTypeArguments()[0];

        assertThat(listEntryClass.getName(), is("com.example.SelfRefs"));
    }

    @Test
    public void selfRefUsedForAdditionalPropertiesIsReadSuccessfully() throws NoSuchMethodException {

        Type additionalPropertiesType = selfRefsClass.getMethod("getAdditionalProperties").getGenericReturnType();
        Class<?> mapEntryClass = (Class<?>) ((ParameterizedType) additionalPropertiesType).getActualTypeArguments()[1];

        assertThat(mapEntryClass.getName(), is("com.example.SelfRefs"));

    }
    
    @Test
    public void nestedSelfRefsInStringContentWithoutParentFile() throws NoSuchMethodException, ClassNotFoundException, IOException {
        String targetPackageName = "com.example";
        URL schemaUrl = CodeGenerationHelper.class.getResource("/schema/ref/nestedSelfRefsReadAsString.json");
        JCodeModel codeModel = new JCodeModel();
        
        RuleFactory ruleFactory = new RuleFactory();
        GenerationConfig config = spy(new DefaultGenerationConfig());
        when(config.getTargetPackage()).thenReturn(targetPackageName);
        
        ruleFactory.setGenerationConfig(config);

        SchemaMapper mapper = new SchemaMapper(ruleFactory, new SchemaGenerator());
        
        mapper.generate(codeModel, "NestedSelfRefsInString", schemaUrl);

        codeModel.build(schemaRule.getGenerateDir());
        
        ClassLoader classLoader = schemaRule.compile();
        
        Class<?> nestedSelfRefs = classLoader.loadClass("com.example.NestedSelfRefsInString");
        assertThat(nestedSelfRefs.getMethod("getThings").getReturnType().getSimpleName(), equalTo("List"));
        
        Class<?> listEntryType = (Class<?>) ((ParameterizedType)nestedSelfRefs.getMethod("getThings").getGenericReturnType()).getActualTypeArguments()[0];
        assertThat(listEntryType.getName(), equalTo("com.example.ThingList"));
        
        Class<?> thingClass = classLoader.loadClass("com.example.ThingList");
        assertThat(thingClass.getMethod("getNamespace").getReturnType().getSimpleName(), equalTo("String"));
        assertThat(thingClass.getMethod("getName").getReturnType().getSimpleName(), equalTo("String"));
        assertThat(thingClass.getMethod("getVersion").getReturnType().getSimpleName(), equalTo("String"));
        
    }    

}