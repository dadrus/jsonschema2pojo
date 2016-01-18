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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;

/**
 * Applies a JSON schema.
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5</a>
 */
public class SchemaRule implements Rule<JClassContainer, JType> {

    private final RuleFactory ruleFactory;

    protected SchemaRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * At the root of a schema document this rule should be applied (schema
     * documents contain a schema), but also in many places within the document.
     * Each property of type "object" is itself defined by a schema, the items
     * attribute of an array is a schema, the additionalProperties attribute of
     * a schema is also a schema.
     * <p>
     * Where the schema value is a $ref, the ref URI is assumed to be applicable
     * as a URL (from which content will be read). Where the ref URI has been
     * encountered before, the root Java type created by that schema will be
     * re-used (generation steps won't be repeated).
     * 
     * @param schema
     *            the schema within which this schema rule is being applied
     */
    @Override
    public JType apply(String nodeName, JsonNode schemaNode, JClassContainer generatableType, Schema schema) {

        if (schemaNode.has("$ref")) {
            schema = ruleFactory.getSchemaStore().create(schema, schemaNode.get("$ref").asText());
            schemaNode = schema.getContent();

            if (schema.isGenerated()) {
                return schema.getJavaType();
            }

            return apply(nodeName, schemaNode, generatableType, schema);
        }
        
        if(schemaNode.has("type") || schemaNode.size() == 0) {
            JType javaType = ruleFactory.getTypeRule().apply(nodeName, schemaNode, generatableType, schema);
            schema.setJavaTypeIfEmpty(javaType);
    
            return javaType;
        } else {
            // this can only be the entry point for the type generation. Because of this
            // returning null is safe
            Map<String, JsonNode> typeDefinitions = collectTypeDefinitions(schemaNode);
            for(Entry<String, JsonNode> entry : typeDefinitions.entrySet()) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.put("$ref", schema.getUrl().toString() + "#/" + entry.getKey());
                
                apply(entry.getKey(), node, generatableType, schema);
            }
            return null;
        }
    }

    private Map<String, JsonNode> collectTypeDefinitions(JsonNode schemaNode) {
        Map<String, JsonNode> typeDefinitions = new HashMap<String, JsonNode>();
        Iterator<Entry<String, JsonNode>> it3 = schemaNode.fields();
        while(it3.hasNext()) {
            Entry<String, JsonNode> entry = it3.next();
            JsonNode node = entry.getValue();
            if(node.has("type")) {
                typeDefinitions.put(entry.getKey(), entry.getValue());
            }
        }
        return typeDefinitions;
    }


}
