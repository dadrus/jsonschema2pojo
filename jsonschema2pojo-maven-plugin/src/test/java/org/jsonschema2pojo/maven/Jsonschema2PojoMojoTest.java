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

package org.jsonschema2pojo.maven;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Test;

public class Jsonschema2PojoMojoTest {

    @Test
    public void getIdMappings() throws IllegalAccessException {
        // GIVEN
        URI id1 = URI.create("http://test.com/foo");
        String name1 = "com.test.foo";
        URI id2 = URI.create("http://test.com/moo");
        String name2 = "com.test.mo";

        List<IdMapping> mappings = new ArrayList<IdMapping>();
        mappings.add(new IdMapping(id1, name1));
        mappings.add(new IdMapping(id2, name2));

        Jsonschema2PojoMojo mojo = new Jsonschema2PojoMojo();
        ReflectionUtils.setVariableValueInObject(mojo, "idMappings", mappings);

        // WHEN
        Map<URI, String> map = mojo.getIdMappings();
        
        // THEN
        assertThat(map.size(), equalTo(2));
        assertThat(map.get(id1), equalTo(name1));
        assertThat(map.get(id2), equalTo(name2));
    }
}
