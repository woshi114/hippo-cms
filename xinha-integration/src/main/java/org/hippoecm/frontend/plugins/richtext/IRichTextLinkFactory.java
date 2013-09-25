/*
 *  Copyright 2010 Hippo.
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
package org.hippoecm.frontend.plugins.richtext;

import java.util.Set;

import org.apache.wicket.model.IDetachable;

public interface IRichTextLinkFactory extends IDetachable {
    @SuppressWarnings("unused")
    final String SVN_ID = "$Id$";

    Set<String> getLinks();

    void cleanup(Set<String> references);

    boolean isValid(IDetachable targetId);
    
    RichTextLink createLink(IDetachable targetId) throws RichTextException;

    RichTextLink loadLink(String relPath) throws RichTextException;

}