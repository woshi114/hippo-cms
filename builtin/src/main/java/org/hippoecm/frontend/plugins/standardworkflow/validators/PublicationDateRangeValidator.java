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

package org.hippoecm.frontend.plugins.standardworkflow.validators;

import java.time.format.FormatStyle;
import java.util.Date;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.hippoecm.frontend.plugins.standards.ClassResourceModel;
import org.hippoecm.frontend.plugins.standards.datetime.DateTimePrinter;

public class PublicationDateRangeValidator extends AbstractFormValidator {

    private final FormComponent<Date> publicationDateComponent;
    private final FormComponent<Date> depublicationDateComponent;

    public PublicationDateRangeValidator(final FormComponent<Date> publicationDateComponent, final FormComponent<Date> depublicationDateComponent) {
        super();
        this.publicationDateComponent = publicationDateComponent;
        this.depublicationDateComponent = depublicationDateComponent;
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{publicationDateComponent, depublicationDateComponent};
    }

    @Override
    public void validate(final Form<?> form) {
        final Date publicationDate = publicationDateComponent.getConvertedInput();
        final Date depublicationDate = depublicationDateComponent.getConvertedInput();
        if (depublicationDate.before(publicationDate)) {
            final String publicationDateString = DateTimePrinter.of(publicationDate).print(FormatStyle.LONG, FormatStyle.MEDIUM);
            final String depublicationDateString = DateTimePrinter.of(depublicationDate).print(FormatStyle.LONG, FormatStyle.MEDIUM);
            final ClassResourceModel classResourceModel = new ClassResourceModel("error-depublication-before-publication-date", DocumentFormValidator.class, publicationDateString, depublicationDateString);
            form.error(classResourceModel.getObject());
        }
    }
}
