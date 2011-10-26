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
package org.hippoecm.frontend.plugins.console.menu.t9ids;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.model.IModelReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class T9idsDialog extends AbstractDialog<Node> {
    
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(T9idsDialog.class);
    
    public T9idsDialog(IModelReference<Node> modelReference) {
        setModel(modelReference.getModel());
        String path = null;
        try {
            path = modelReference.getModel().getObject().getPath();
        } catch (RepositoryException e) {
            log.error("Failed to get path from model node", e);
        }
        add(new Label("message", new StringResourceModel("t9ids.message", this, null, new Object[] {path})));
        setFocusOnOk();
    }

    @Override
    public void onOk() {
        try {
            getModel().getObject().accept(new GenerateNewTranslationIdsVisitor());
        } catch (RepositoryException e) {
            log.error("Failure during setting of new translation ids", e);
        }
    }
    
    @Override
    public IModel<String> getTitle() {
        return new StringResourceModel("t9ids.title", this, null);
    }

    @Override
    public IValueMap getProperties() {
        return SMALL;
    }

}
