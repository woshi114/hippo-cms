/*
 *  Copyright 2011 Hippo.
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
package org.hippoecm.frontend.upgrade;

import org.hippoecm.repository.ext.UpdaterContext;
import org.hippoecm.repository.ext.UpdaterItemVisitor;
import org.hippoecm.repository.ext.UpdaterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class CmsUpgrader18b implements UpdaterModule{

static final Logger log = LoggerFactory.getLogger(CmsUpgrader18b.class);

    @Override
    public void register(UpdaterContext updaterContext) {
        updaterContext.registerName("v18b-cms-updater");
        updaterContext.registerStartTag("v18-cms");
        updaterContext.registerEndTag("v18b-cms");

        registerConsoleVisitors(updaterContext);
    }

    private void registerConsoleVisitors(final UpdaterContext context) {
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/hippo:configuration/hippo:initialize") {

            @Override
            protected void leaving(Node node, int level) throws RepositoryException {
                if (node.hasNode("frontend-console")) {
                    node.getNode("frontend-console").remove();
                }
            }

        });
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/hippo:configuration/hippo:frontend") {

            @Override
            protected void leaving(Node node, int level) throws RepositoryException {
                if (node.hasNode("console")) {
                    node.getNode("console").remove();
                }
            }

        });


    }
}
