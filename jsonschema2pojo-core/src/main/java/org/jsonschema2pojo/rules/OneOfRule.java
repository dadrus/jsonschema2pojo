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

package org.jsonschema2pojo.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class OneOfRule implements Rule<JDefinedClass, JDefinedClass> {

    private RuleFactory ruleFactory;

    public OneOfRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {
        
        if(!node.isArray()) {
            throw new IllegalArgumentException("oneOf definition (" + node.toString() + ") for " + schema.getUrl() + " schema is invalid. Must be an array [...]");
        }
        
        List<String> propertyNames = new ArrayList<String>();
        
        for (Iterator<JsonNode> elements = node.elements(); elements.hasNext(); ) {
            JsonNode oneOfNode = elements.next();
            if(oneOfNode.has("$ref")) {
                // all elements from the "properties" node of the referenced element is required here
                // pull all the elements from "properties"
                throw new IllegalArgumentException("Not supported yet");
             
            } else if(oneOfNode.has("properties")) {
                List<String> elementNames = getElementNames(oneOfNode.get("properties"));
                propertyNames.addAll(elementNames);
                
                ruleFactory.getPropertiesRule().apply(nodeName, oneOfNode.get("properties"), jclass, schema);
            } else {
                throw new IllegalArgumentException("Not supported yet");
                // is this allowed at all?
            }
        }
        
        // one of is a choice - modify setters that way, that if one setter is called, all properties,
        // which do not belong to the given setter are set to null, if the given value is not null.
        for (String propertyName : propertyNames) {
            JMethod setter = getSetterForProperty(jclass, propertyName);
            if(setter != null) {
                List<String> propertiesToNullify = new ArrayList<String>(propertyNames);
                propertiesToNullify.remove(propertyName);
                setter = rewriteSetter(jclass, setter, propertyName, propertiesToNullify);
                propertyAnnotations(nodeName, node, schema, setter);
            }
        }
        
        // TODO: builder methods must updated as well

        return jclass;
    }

    private List<String> getElementNames(JsonNode oneOfNode) {
        List<String> propertyNames = new ArrayList<String>();
        for (Iterator<String> properties = oneOfNode.fieldNames(); properties.hasNext(); ) {
            String propertyName = properties.next();
            propertyNames.add(ruleFactory.getNameHelper().getPropertyName(propertyName));
        }
        return propertyNames;
    }

    private JMethod getSetterForProperty(JDefinedClass jclass, String propertyName) {
        for (JMethod method : jclass.methods()) {
            if (method.name().startsWith("set")) {
                String accessedPropertyName = StringUtils.substringAfter(method.name(), "set").toLowerCase();
                if(accessedPropertyName.equalsIgnoreCase(propertyName)) {
                    return method;
                }
            }
        }
        return null;
    }

    private JMethod rewriteSetter(JDefinedClass jclass, JMethod method, String propertyName, List<String> propertiesToNullify) {
        // assuming a setter has just one argument
        Map<String, JFieldVar> fields = jclass.fields();
        JFieldVar field = fields.get(propertyName);
        
        jclass.methods().remove(method);
        
        JMethod setter = jclass.method(JMod.PUBLIC, void.class, getSetterName(propertyName));

        // add @param
        setter.javadoc().addParam(ruleFactory.getNameHelper().getPropertyName(propertyName)).append("The " + propertyName);

        JVar param = setter.param(field.type(), field.name());
        JBlock body = setter.body();
        
        body._if(param.eq(JExpr._null()))._then()._return();
        body.assign(JExpr._this().ref(field), param);
        // set other properties to null
        for(String otherPropertyName : propertiesToNullify) {
            body.assign(JExpr._this().ref(otherPropertyName), JExpr._null());
        }
        
        ruleFactory.getAnnotator().propertySetter(setter, propertyName);

        return setter;
    }
    
    private void propertyAnnotations(String nodeName, JsonNode node, Schema schema, JDocCommentable generatedJavaConstruct) {
        if (node.has("title")) {
            ruleFactory.getTitleRule().apply(nodeName, node.get("title"), generatedJavaConstruct, schema);
        }

        if (node.has("description")) {
            ruleFactory.getDescriptionRule().apply(nodeName, node.get("description"), generatedJavaConstruct, schema);
        }

        if (node.has("required")) {
            ruleFactory.getRequiredRule().apply(nodeName, node.get("required"), generatedJavaConstruct, schema);
        }
    }
    
    private String getSetterName(String propertyName) {
        return ruleFactory.getNameHelper().getSetterName(propertyName);
    }

}
