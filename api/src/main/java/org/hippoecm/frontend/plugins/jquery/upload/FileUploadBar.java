/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.frontend.plugins.jquery.upload;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;

public class FileUploadBar extends Panel {
    private static final long serialVersionUID = 1L;

    public FileUploadBar(final String id, final FileUploadWidgetSettings settings) {
        super(id);
        setOutputMarkupId(true);

        add(new FileUploadBehavior(settings));

        Button uploadButton = new Button("button-upload-id", new StringResourceModel("button-upload-label", this, null));
        add(uploadButton);

        Button cancelButton = new Button("button-cancel-id", new StringResourceModel("button-cancel-label", this, null));
        add(cancelButton);
    }
}