/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.editor.cnd;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FieldIdentifier implements Serializable {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id: $";

    private static final long serialVersionUID = 1L;

    public String path;

    public String type;

    private Object writeReplace() throws ObjectStreamException {
        Map<String, String> obj =  new HashMap<String, String>();
        obj.put("path", path);
        obj.put("type", type);
        return obj;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object != null) {
            if (object instanceof FieldIdentifier) {
                FieldIdentifier id = (FieldIdentifier) object;
                return id.path.equals(path) && id.type.equals(type);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (path.hashCode() * (type != null ? type.hashCode() : 0)) % 1001;
    }
}