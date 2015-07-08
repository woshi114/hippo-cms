/*
 *  Copyright 2008-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.editor.plugins;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.widgets.BooleanFieldWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanValueTemplatePlugin extends RenderPlugin<Boolean> {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(BooleanValueTemplatePlugin.class);

    public BooleanValueTemplatePlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        final IModel<Boolean> valueModel = getModel();
        final BooleanFieldWidget value = new BooleanFieldWidget("value", valueModel);
        value.setEnabled("edit".equals(config.getString("mode", "view")));
        add(value);

        setOutputMarkupId(true);
    }

}
