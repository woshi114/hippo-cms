/*
 *  Copyright 2015 Hippo B.V. (http://www.onehippo.com)
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

import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.WebSession;

public class ReplaceAposForIE8Model extends AbstractStringTransformingModel {

    private static Pattern APOS_PATTERN = Pattern.compile("&apos;",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private boolean clientIsIE8;

    public ReplaceAposForIE8Model(IModel<String> valueModel) {
        super(valueModel);

        ClientProperties clientProperties = WebSession.get().getClientInfo().getProperties();
        clientIsIE8 = clientProperties.isBrowserInternetExplorer() && clientProperties.getBrowserVersionMajor() == 8;
    }

    @Override
    protected String transform(final String string) {
        if (clientIsIE8) {
            return replaceApos(string);
        }

        return string;
    }

    protected static String replaceApos(final String string) {
        return APOS_PATTERN.matcher(string).replaceAll("&#39;");
    }

}
