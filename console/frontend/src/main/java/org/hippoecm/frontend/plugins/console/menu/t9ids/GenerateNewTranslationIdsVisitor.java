/*
 *  Copyright 2011-2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.console.menu.t9ids;

import java.util.AbstractMap;
import java.util.UUID;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.hippoecm.frontend.model.JcrHelper;

public class GenerateNewTranslationIdsVisitor implements ItemVisitor {

    private static final String HIPPOTRANSLATION_ID = "hippotranslation:id";
    private static final String HIPPO_HANDLE = "hippo:handle";
    private AbstractMap.SimpleEntry<String, String> handleTranslationIdPair;

    @Override
    public void visit(Property property) throws RepositoryException {
        if (property.getName().equals(HIPPOTRANSLATION_ID)) {
            final String handleId = findHandleId(property);
            if (handleId != null && handleTranslationIdPair != null) {
                final String key = handleTranslationIdPair.getKey();
                if (handleId.equals(key)) {
                    property.setValue(handleTranslationIdPair.getValue());
                    return;
                }
            }
            // invoked for folders or any other nodes with no handle as parent:
            property.setValue(UUID.randomUUID().toString());
        }
    }

    private String findHandleId(final Property property) throws RepositoryException {
        final Node handle = property.getParent().getParent();
        if (handle.isNodeType(HIPPO_HANDLE)) {
            return handle.getIdentifier();
        }
        return null;
    }

    @Override
    public void visit(Node node) throws RepositoryException {
        if (!JcrHelper.isVirtualNode(node)) {
            // store id for handle so handle child nodes have same translation id:
            if (node.isNodeType(HIPPO_HANDLE)) {
                handleTranslationIdPair = new AbstractMap.SimpleEntry<>(node.getIdentifier(), UUID.randomUUID().toString());
            }
            if (node.hasProperty(HIPPOTRANSLATION_ID)) {
                visit(node.getProperty(HIPPOTRANSLATION_ID));
            }
            NodeIterator children = node.getNodes();
            while (children.hasNext()) {
                children.nextNode().accept(this);
            }
        }
    }

}
