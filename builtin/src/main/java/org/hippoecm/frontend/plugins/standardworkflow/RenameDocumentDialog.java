/*
 *  Copyright 2012-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.standardworkflow;

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.addon.workflow.AbstractWorkflowDialog;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClassAppender;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenameDocumentDialog extends AbstractWorkflowDialog<RenameDocumentArguments> {

    private static Logger log = LoggerFactory.getLogger(RenameDocumentDialog.class);

    private IModel title;
    private TextField nameComponent;
    private TextField uriComponent;
    private boolean uriModified;
    private final IModel<StringCodec> nodeNameCodecModel;

    public RenameDocumentDialog(RenameDocumentArguments renameDocumentModel, IModel title, IWorkflowInvoker invoker, IModel<StringCodec> nodeNameCodec, final WorkflowDescriptorModel workflowDescriptorModel) {
        super(Model.of(renameDocumentModel), invoker);
        this.title = title;
        this.nodeNameCodecModel = nodeNameCodec;

        final PropertyModel<String> nameModel = new PropertyModel<String>(renameDocumentModel, "targetName");
        final PropertyModel<String> uriModel = new PropertyModel<String>(renameDocumentModel, "uriName");

        String s1 = nameModel.getObject();
        String s2 = uriModel.getObject();
        uriModified = !s1.equals(s2);

        nameComponent = new TextField<String>("name", nameModel);
        nameComponent.setRequired(true);
        nameComponent.setLabel(new StringResourceModel("name-label", this, null));
        nameComponent.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (!uriModified) {
                    uriModel.setObject(getNodeNameCodec().encode(nameModel.getObject()));
                    target.addComponent(uriComponent);
                }
            }
        }.setThrottleDelay(Duration.milliseconds(500)));

        nameComponent.setOutputMarkupId(true);
        setFocus(nameComponent);
        add(nameComponent);

        uriComponent = new TextField<String>("uriinput", uriModel) {
            @Override
            public boolean isEnabled() {
                return uriModified;
            }
        };

        uriComponent.setLabel(new StringResourceModel("url-label", this, null));
        add(uriComponent);

        uriComponent.add(new CssClassAppender(new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return uriModified ? "grayedin" : "grayedout";
            }
        }));
        uriComponent.setOutputMarkupId(true);
        uriComponent.setRequired(true);

        AjaxLink<Boolean> uriAction = new AjaxLink<Boolean>("uriAction") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                uriModified = !uriModified;
                if (!uriModified) {
                    uriModel.setObject(Strings.isEmpty(nameModel.getObject()) ? "" : getNodeNameCodec().encode(
                            nameModel.getObject()));
                    uriComponent.modelChanged();
                } else {
                    target.focusComponent(uriComponent);
                }
                target.addComponent(RenameDocumentDialog.this);
            }
        };
        uriAction.add(new Label("uriActionLabel", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return uriModified ? getString("url-reset") : getString("url-edit");
            }
        }));
        add(uriAction);

        final Locale cmsLocale = UserSession.get().getLocale();
        final RenameMessage message = new RenameMessage(cmsLocale, renameDocumentModel.getLocalizedNames());
        if (message.shouldShow()) {
            warn(message.forFolder());
        }

        add(new IFormValidator() {
            @Override
            public FormComponent<?>[] getDependentFormComponents() {
                return new FormComponent<?>[] { uriComponent };
            }

            @Override
            public void validate(final Form<?> form) {
                String finalNodeName = uriComponent.getValue().toLowerCase();
                try {
                    Node embeddingNode = workflowDescriptorModel.getNode().getParent();
                    if (embeddingNode.hasNode(finalNodeName)) {
                        error(getString("sns-node-exists"));
                    }
                } catch (RepositoryException e) {
                    log.error("Error obtaining parent node", e);
                    error(getString("Error obtaining parent node"));
                }
            }
        });
    }

    @Override
    public IModel getTitle() {
        return title;
    }

    @Override
    public IValueMap getProperties() {
        return MEDIUM;
    }

    protected StringCodec getNodeNameCodec() {
        return nodeNameCodecModel.getObject();
    }

    @Override
    protected void onDetach() {
        nodeNameCodecModel.detach();
        super.onDetach();
    }
}
