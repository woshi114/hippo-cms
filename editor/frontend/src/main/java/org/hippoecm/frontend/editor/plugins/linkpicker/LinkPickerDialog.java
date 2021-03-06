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
package org.hippoecm.frontend.editor.plugins.linkpicker;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.PluginRequestTarget;
import org.hippoecm.frontend.dialog.Dialog;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.picker.NodePickerController;
import org.hippoecm.frontend.plugins.standards.picker.NodePickerControllerSettings;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.widgets.breadcrumb.NodeBreadcrumbWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkPickerDialog extends Dialog<String> {

    static final Logger log = LoggerFactory.getLogger(LinkPickerDialog.class);
    private static final String EMPTY_FRAGMENT_ID = "empty-fragment";

    private final IPluginContext context;
    private final IPluginConfig config;

    private final NodePickerController controller;
    private final NodeBreadcrumbWidget breadcrumbs;

    public LinkPickerDialog(IPluginContext context, IPluginConfig config, IModel<String> model) {
        super(model);

        this.context = context;
        this.config = config;

        setOutputMarkupId(true);

        setCssClass("hippo-dialog-picker");

        final NodePickerControllerSettings settings = NodePickerControllerSettings.fromPluginConfig(config);
        controller = new NodePickerController(context, settings) {

            @Override
            protected IModel<Node> getInitialModel() {
                final String uuid = getModelObject();
                try {
                    if (StringUtils.isNotEmpty(uuid)) {
                        return new JcrNodeModel(UserSession.get().getJcrSession().getNodeByIdentifier(uuid));
                    }
                } catch (ItemNotFoundException e) {
                    // valid case, node does not exist
                    return null;
                } catch (RepositoryException e) {
                    log.error("Error while getting link picker model for the node with UUID '" + uuid + "'", e);
                }
                return null;
            }

            @Override
            protected void onSelect(boolean isValid) {
                setOkEnabled(isValid);
            }

            @Override
            protected void onFolderSelected(final IModel<Node> model) {
                LinkPickerDialog.this.onFolderSelected(model);
            }
        };

        add(controller.create("content"));
        controller.initSelection();

        addOrReplace(breadcrumbs = new NodeBreadcrumbWidget(Dialog.BOTTOM_LEFT_ID, null, controller.getRootPaths()) {
            @Override
            protected void onClick(final IModel<Node> nodeModel, final AjaxRequestTarget target) {
                controller.setSelectedFolder(nodeModel);
            }
        });
        breadcrumbs.update(controller.getFolderModel());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(createTopFragment("top-fragment"));
    }
    /**
     * Override this method to customize the top section in the link-picker dialog
     */
    protected Fragment createTopFragment(final String id) {
        return new Fragment(id, EMPTY_FRAGMENT_ID, this);
    }

    @Override
    public final void onClose() {
        controller.onClose();
        super.onClose();
    }

    @Override
    public void render(PluginRequestTarget target) {
        if (controller.getRenderer() != null) {
            controller.getRenderer().render(target);
        }
        super.render(target);
    }

    @Override
    public void onOk() {
        final IModel<Node> selectedModel = controller.getSelectedModel();
        if (selectedModel != null) {
            saveNode(selectedModel.getObject());
        } else {
            error("No node selected");
        }
    }

    @Override
    protected void onDetach() {
        controller.detach();
        super.onDetach();
    }

    protected void saveNode(Node node) {
        try {
            getModel().setObject(node.getIdentifier());
        } catch (RepositoryException ex) {
            error(ex.getMessage());
        }
    }

    protected IModel<Node> getFolderModel() {
        return controller.getFolderModel();
    }

    protected void onFolderSelected(final IModel<Node> model) {
        if (breadcrumbs != null) {
            breadcrumbs.update(model);
        }
    }

    protected IPluginContext getPluginContext() {
        return context;
    }

    protected IPluginConfig getPluginConfig() {
        return config;
    }

}
