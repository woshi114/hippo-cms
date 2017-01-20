
/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

package org.hippoecm.frontend.plugins.gallery.processor.event;

import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoEventConstants;

public class ImageVariantEvent<T extends ImageVariantEvent<T>> extends HippoEvent<T> {

    private static final String VARIANT = "variant";
    private static final String IMAGE = "image";
    private static final String GALLERY = "gallery";

    public ImageVariantEvent(String application) {
        super(application);
        category(GALLERY);
    }

    public ImageVariantEvent(HippoEvent<?> event) {
        super(event);
    }

    public T variant(final String variant) {
        return put(VARIANT, variant);
    }

    public T node(final String image) {
        return put(IMAGE, image);
    }
    
}
