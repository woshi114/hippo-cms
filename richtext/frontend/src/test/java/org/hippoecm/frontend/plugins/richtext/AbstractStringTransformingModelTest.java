/*
 *  Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugins.richtext;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AbstractStringTransformingModelTest {

    public class NoopTransformingModel extends AbstractStringTransformingModel {

        public NoopTransformingModel(IModel<String> valueModel) {
            super(valueModel);
        }

        @Override
        protected String transform(final String string) {
            fail("transform should not be called");
            return null;
        }
    }

    @Test
    public void test_null_values() {
        String nullValue = null;
        assertEquals(nullValue, new NoopTransformingModel(null).getObject());
        assertEquals(nullValue, new NoopTransformingModel(new Model<>(nullValue)).getObject());
    }
}
