/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

import java.util.Date;
import java.util.Optional;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugins.reviewedactions.model.ApprovalRequest;
import org.hippoecm.frontend.plugins.standardworkflow.validators.PublicationDateRangeValidator;

/**
 * A panel containing a publication date picker and a de-publication date picker.
 */
public class ApprovalRequestComponent extends Panel {

    private IFormValidator dateRangeFormValidator;

    public ApprovalRequestComponent(final IModel<ApprovalRequest> approvalRequestModel) {
        super("approval-request-fields");

        final PropertyModel<Date> publicationDateModel = new PropertyModel<>(approvalRequestModel, "publicationDate");
        final ResourceModel publicationDateLabel = new ResourceModel("schedule-publish-text");
        final boolean publicationDateVisible = approvalRequestModel.getObject().isPublicationDate();
        final DatePickerComponent publicationDate = addDatePicker(publicationDateModel, publicationDateLabel, "publicationDate");
        publicationDate.setVisible(publicationDateVisible);

        final PropertyModel<Date> depublicationDateModel = new PropertyModel<>(approvalRequestModel, "depublicationDate");
        final ResourceModel depublicationDateLabel = new ResourceModel("schedule-depublish-text");
        final boolean depublicationDateVisible = approvalRequestModel.getObject().isDepublicationDate();
        final DatePickerComponent depublicationDate = addDatePicker(depublicationDateModel, depublicationDateLabel, "depublicationDate");
        depublicationDate.setVisible(depublicationDateVisible);

        if (publicationDateVisible && depublicationDateVisible) {
            final FormComponent<Date> publicationDateField = publicationDate.getDateTimeField();
            final FormComponent<Date> depublicationDateDateTimeField = depublicationDate.getDateTimeField();
            dateRangeFormValidator = new PublicationDateRangeValidator(publicationDateField, depublicationDateDateTimeField);
        }
    }

    private DatePickerComponent addDatePicker(final PropertyModel<Date> dateModel, final ResourceModel dateLabel, final String componentId) {
        final DatePickerComponent picker = new DatePickerComponent(componentId, dateModel, dateLabel);
        add(picker);
        return picker;
    }

    public Optional<IFormValidator> getDateRangeFormValidator() {
        return Optional.ofNullable(dateRangeFormValidator);
    }
}
