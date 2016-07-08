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
package org.hippoecm.frontend.plugins.console.menu.t9ids;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.translation.HippoTranslationNodeType;
import org.junit.Before;
import org.junit.Test;
import org.onehippo.repository.testutils.RepositoryTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GenerateNewTranslationIdsVisitorTest extends RepositoryTestCase {

    private String[] content = new String[] {
        "/test", "nt:unstructured",

            "/test/folder", "hippostd:folder",
            "jcr:mixinTypes", "mix:referenceable,hippotranslation:translated",
            "hippotranslation:id", "test-folder-t9id",

                "/test/folder/document", "hippo:handle",
                "jcr:mixinTypes", "mix:referenceable",

                    "/test/folder/document/document", "hippo:document",
                    "jcr:mixinTypes", "mix:versionable,hippotranslation:translated",
                    "hippotranslation:id", "document-t9id",

                    "/test/folder/document/document", "hippo:document",
                    "jcr:mixinTypes", "mix:versionable,hippotranslation:translated",
                    "hippotranslation:id", "different-document-t9id",

                "/test/folder/nested-folder", "hippostd:folder",
                "jcr:mixinTypes", "mix:referenceable,hippotranslation:translated",
                "hippotranslation:id", "test-folder-nested-folder-t9id",

                "/test/folder/document-no-t9", "hippo:handle",
                "jcr:mixinTypes", "mix:referenceable",

                    "/test/folder/document-no-t9/document-no-t9", "hippo:document",
                    "jcr:mixinTypes", "mix:versionable",

            "/test/folder-no-t9", "hippostd:folder",
            "jcr:mixinTypes", "mix:referenceable",
    };

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        RepositoryTestCase.build(content, session);

        final Node testNode = session.getRootNode().getNode("test");
        testNode.accept(new GenerateNewTranslationIdsVisitor());
    }

    @Test
    public void testSkipNonTranslatedNodes() throws Exception {
        assertNull(getT9Id("test"));
        assertNull(getT9Id("test/folder-no-t9"));
        assertNull(getT9Id("test/folder/document-no-t9"));
        assertNull(getT9Id("test/folder/document-no-t9/document-no-t9"));
        assertNull(getT9Id("test/folder/document"));
    }

    @Test
    public void testNewT9IdForNonDocuments() throws Exception {
        // verify that t9id has changed
        final String folderT9Id = getT9Id("test/folder");
        assertThat("test-folder-t9id", is(not(folderT9Id)));

        final String nestedFolderT9Id = getT9Id("test/folder/nest-folder");
        assertThat("test-folder-nested-folder-t9id", is(not(nestedFolderT9Id)));

        // verify it is not the same for different nodes
        assertThat(folderT9Id, is(not(nestedFolderT9Id)));
    }

    @Test
    public void testSharedT9IdForDocuments() throws Exception {
        final String doc1T9Id = getT9Id("test/folder/document/document");
        final String doc2T9Id = getT9Id("test/folder/document/document[2]");

        // verify that t9id has changed
        assertThat("document-t9id", is(not(doc1T9Id)));
        assertThat("different-document-t9id", is(not(doc2T9Id)));

        // verify that t9id is shared between document variants
        assertThat(doc1T9Id, is(doc2T9Id));
    }

    private String getT9Id(final String relPath) throws RepositoryException {
        final Node root = session.getRootNode();
        if (root.hasNode(relPath)) {
            final Node node = root.getNode(relPath);
            if (node.hasProperty(HippoTranslationNodeType.ID)) {
                return node.getProperty(HippoTranslationNodeType.ID).getString();
            }
        }
        return null;
    }
}
