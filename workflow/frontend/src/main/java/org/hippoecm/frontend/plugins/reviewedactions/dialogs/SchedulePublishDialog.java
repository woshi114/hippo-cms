/*
 *  Copyright 2010-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.reviewedactions.dialogs;

import java.util.Optional;

import javax.jcr.Node;

import org.apache.wicket.model.IModel;
import org.hippoecm.addon.workflow.IWorkflowInvoker;
import org.hippoecm.addon.workflow.WorkflowDialog;
import org.hippoecm.frontend.plugins.reviewedactions.UnpublishedReferenceNodeProvider;
import org.hippoecm.frontend.plugins.reviewedactions.model.ApprovalRequest;
import org.hippoecm.frontend.plugins.reviewedactions.model.ReferenceProvider;
import org.hippoecm.frontend.plugins.reviewedactions.model.UnpublishedReferenceProvider;
import org.hippoecm.frontend.plugins.standards.ClassResourceModel;
import org.hippoecm.frontend.service.IEditorManager;

public class SchedulePublishDialog extends WorkflowDialog<Node> {

    public SchedulePublishDialog(final IWorkflowInvoker invoker,
                                 final IModel<ApprovalRequest> approvalRequestModel,
                                 final IModel<Node> nodeModel,
                                 final IModel<String> titleModel,
                                 final IEditorManager editorMgr) {
        super(invoker, nodeModel, titleModel);
        addApprovalPolicyNotification(approvalRequestModel);

        setCssClass("hippo-workflow-dialog");
        setFocusOnCancel();

        UnpublishedReferenceNodeProvider provider = new UnpublishedReferenceNodeProvider(
                new UnpublishedReferenceProvider(new ReferenceProvider(nodeModel)));
        add(new UnpublishedReferencesView("links", provider, editorMgr));

        final ApprovalRequestComponent approvalRequestComponent = new ApprovalRequestComponent(approvalRequestModel);
        add(approvalRequestComponent);
        approvalRequestComponent.getDateRangeFormValidator().ifPresent(this::add);
    }

    private void addApprovalPolicyNotification(final IModel<ApprovalRequest> approvalRequestModel) {
        final Optional<String> processDescription = approvalRequestModel.getObject().getProcessDescription();
        if (processDescription.isPresent()){
            ClassResourceModel approvalText = new ClassResourceModel("approval-policy-text",SchedulePublishDialog.class,processDescription.get());
            setNotification(approvalText);
        }
    }

}
