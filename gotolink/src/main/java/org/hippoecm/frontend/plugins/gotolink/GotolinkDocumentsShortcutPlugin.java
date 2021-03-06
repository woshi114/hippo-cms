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
package org.hippoecm.frontend.plugins.gotolink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.icon.HippoIcon;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.IconSize;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.skin.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GotolinkDocumentsShortcutPlugin extends RenderPlugin {

    static final Logger log = LoggerFactory.getLogger(GotolinkDocumentsShortcutPlugin.class);

    public GotolinkDocumentsShortcutPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        AjaxLink link = new AjaxLink("link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                final String browserId = config.getString("browser.id");
                final IBrowseService browseService = context.getService(browserId, IBrowseService.class);
                final String location = config.getString("option.location", "/content");
                if (browseService != null) {
                    browseService.browse(new JcrNodeModel(location));
                } else {
                    log.warn("no browse service found with id '{}', cannot browse to '{}'", browserId, location);
                }
            }
        };

        add(link);

        final HippoIcon icon = HippoIcon.fromSprite("gotolink-icon", Icon.FOLDER_OPEN, IconSize.XL);
        link.add(icon);
    }
}
