/*
 *  Copyright 2010-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.richtext.jcr;

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugins.richtext.IImageURLProvider;
import org.hippoecm.frontend.plugins.richtext.IRichTextImageFactory;
import org.hippoecm.frontend.plugins.richtext.IRichTextLinkFactory;
import org.hippoecm.frontend.plugins.richtext.RichTextException;
import org.hippoecm.frontend.plugins.richtext.RichTextImage;

public class RichTextImageURLProvider implements IImageURLProvider {

    private static final long serialVersionUID = 1L;

    private final IRichTextImageFactory imageFactory;
    private final IRichTextLinkFactory linkFactory;
    private final IModel<Node> nodeModel;

    public RichTextImageURLProvider(IRichTextImageFactory factory, IRichTextLinkFactory linkFactory, IModel<Node> nodeModel) {
        this.imageFactory = factory;
        this.linkFactory = linkFactory;
        this.nodeModel = nodeModel;
    }

    public String getURL(String link) throws RichTextException {
        final String facetName = StringUtils.substringBefore(link, "/");
        final String type = StringUtils.substringAfterLast(link, "/");
        final Node node = this.nodeModel.getObject();
        final String uuid = RichTextFacetHelper.getChildDocBaseOrNull(node, facetName);
        if (uuid != null && linkFactory.getLinkUuids().contains(uuid)) {
            RichTextImage rti = imageFactory.loadImageItem(uuid, type);
            return rti.getUrl();
        }
        return link;
    }
}
