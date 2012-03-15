/*
 *  Copyright 2008-2012 Hippo.
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
package org.hippoecm.frontend.plugins.cms.admin.widgets;

import java.util.ArrayList;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class AjaxLinkLabelListPanel extends Panel {

    public AjaxLinkLabelListPanel(final String id, final IModel<ArrayList<AjaxLinkLabelContainer>> linksListModel) {
        super(id);

        ListView listView = new ListView("listView", new ArrayList(linksListModel.getObject())) {
            @Override
            protected void populateItem(ListItem listItem) {
                AjaxLinkLabelContainer linkAjaxLabel = (AjaxLinkLabelContainer) listItem.getDefaultModelObject();
                listItem.add(linkAjaxLabel.getAjaxFallbackLink());
            }
        };
        add(listView);
    }
}
