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

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;

import org.hippoecm.frontend.model.JcrHelper;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.translation.HippoTranslationNodeType;

public class GenerateNewTranslationIdsVisitor extends TraversingItemVisitor.Default {

    private String handleId;
    private String handleT9Id;

    @Override
    protected void entering(final Node node, final int level) throws RepositoryException {
        if (JcrHelper.isVirtualNode(node)) {
            return;
        }

        if (node.isNodeType(HippoNodeType.NT_HANDLE)) {
            handleId = node.getIdentifier();
            handleT9Id = UUID.randomUUID().toString();
        }
    }

    @Override
    protected void leaving(final Node node, final int level) throws RepositoryException {
        if (JcrHelper.isVirtualNode(node)) {
            return;
        }

        if (node.isNodeType(HippoNodeType.NT_HANDLE)) {
            handleId = null;
            handleT9Id = null;
        }
    }

    @Override
    protected void entering(final Property property, final int level) throws RepositoryException {
        if (property.getName().equals(HippoTranslationNodeType.ID)) {
            if (parentIsHandle(property)) {
                property.setValue(handleT9Id);
            } else {
                // invoked for folders or any other nodes with no handle as parent:
                property.setValue(UUID.randomUUID().toString());
            }
        }
    }

    private boolean parentIsHandle(final Property property) throws RepositoryException {
        if (handleId != null) {
            final Node parent = property.getParent();
            final Node grandParent = parent.getParent();
            return grandParent != null && grandParent.getIdentifier().equals(handleId);
        }
        return false;
    }
}

