/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.translation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.translation.HippoTranslationNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.repository.translation.HippoTranslationNodeType.NT_TRANSLATED;

/**
 * Utility class.
 */
public class TranslationUtil {

    private static final Logger log = LoggerFactory.getLogger(TranslationUtil.class);

    private TranslationUtil() {
        // prevent instantiation
    }

    /**
     * Do both the argument document node and the containing folder node have the 'translated' mixin?
     */
    public static boolean hasTranslationContext(Node documentNode) {
        if (documentNode == null) {
            log.warn("Argument document node is null");
            return false;
        }

        try {
            if (!documentNode.isNodeType(HippoNodeType.NT_DOCUMENT)) {
                log.warn("Document node at {} is not a {}", documentNode.getPath(), HippoNodeType.NT_DOCUMENT);
                return false;
            }

            if (!isNtTranslated(documentNode)) {
                log.debug("Document node at {} is not a {}", documentNode.getPath(), NT_TRANSLATED);
                return false;
            }

            final Node folderNode = documentNode.getParent().getParent();
            if (!isNtTranslated(folderNode)) {
                log.debug("Folder node at {} is not a {}", folderNode.getPath(), NT_TRANSLATED);
                return false;
            }

            return true;
        } catch (RepositoryException e) {
            log.error("Failed to check the document's folder having mixin " + NT_TRANSLATED, e);
        }

        return false;
    }

    public static boolean isNtTranslated(Node node) throws RepositoryException {
        if (node.isNodeType(NT_TRANSLATED)) {
            return true;
        }
        return false;
    }

}
