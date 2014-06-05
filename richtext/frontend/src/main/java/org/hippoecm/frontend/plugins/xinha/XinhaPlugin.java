/*
 *  Copyright 2008-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.xinha;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.model.IModelReference;
import org.hippoecm.frontend.model.properties.JcrPropertyValueModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.richtext.RichTextModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XinhaPlugin extends AbstractXinhaPlugin {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(XinhaPlugin.class);

    public XinhaPlugin(IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    protected JcrPropertyValueModel getValueModel() {
        return (JcrPropertyValueModel) getDefaultModel();
    }

    protected JcrPropertyValueModel getBaseModel() {
        IPluginConfig config = getPluginConfig();
        if (!config.containsKey("model.compareTo")) {
            log.warn("No model.compareTo reference configured");
            return null;
        }
        IModelReference modelRef = getPluginContext().getService(config.getString("model.compareTo"),
                IModelReference.class);
        if (modelRef == null || !(modelRef.getModel() instanceof JcrPropertyValueModel)) {
            log.warn("The configured model.compareTo service is not available or does provide a valid property value model");
            return null;
        }
        return (JcrPropertyValueModel) modelRef.getModel();
    }

    @Override
    protected IModel<String> newEditModel() {
        RichTextModel model = (RichTextModel) super.newEditModel();
        model.setCleaner(getHtmlCleanerOrNull());
        return model;
    }
}
