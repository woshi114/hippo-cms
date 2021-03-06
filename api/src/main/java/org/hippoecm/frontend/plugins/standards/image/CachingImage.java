/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.frontend.plugins.standards.image;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class CachingImage extends Image {

    public CachingImage(final String id) {
        super(id);
    }

    public CachingImage(final String id, final ResourceReference resourceReference) {
        super(id, resourceReference);
    }

    public CachingImage(final String id, final IModel<?> model) {
        super(id, model);
    }

    public CachingImage(final String id, final IResource imageResource) {
        super(id, imageResource);
    }

    public CachingImage(final String id, final String string) {
        super(id, string);
    }

    @Override
    protected boolean shouldAddAntiCacheParameter() {
        return false;
    }
}
