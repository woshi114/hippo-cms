/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.console;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.protocol.http.WebRequest;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.util.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopPlugin extends RenderPlugin {

    static final Logger log = LoggerFactory.getLogger(TopPlugin.class);

    public TopPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        WebMarkupContainerWithAssociatedMarkup toolbar = new WebMarkupContainerWithAssociatedMarkup("bar.styles");

        toolbar.add(new AbstractBehavior() {
            public void onComponentTag(Component component, ComponentTag tag) {
                String style = obtainBreadcrumbStyle(config);
                if (style != null) {
                    tag.put("style", style);
                }
            }
        });

        add(toolbar);
    }

    public String obtainBreadcrumbStyle(IPluginConfig config){
        String[] urlParts = config.getStringArray("bar.style.urlparts");
        String[] barStyles = config.getStringArray("bar.styles");

        if(urlParts == null || barStyles == null) {
            return null;
        }
        if(urlParts.length != barStyles.length) {
            log.warn("Number of values on the properties \"bar.style.urlparts\" and \"bar.styles\" must be equal on "
                    + config);
            return null;
        }

        String requestUrl = getRequestURL();
        if(requestUrl != null) {
            for (int i = 0 ; i < urlParts.length; i++) {
                if (StringUtils.isNotEmpty(urlParts[i])) {
                    String urlPart = urlParts[i];
                    if(requestUrl.contains(urlPart) && StringUtils.isNotBlank(barStyles[i])) {
                        if(log.isDebugEnabled()) {
                            log.debug("Style [" + i + "] choosen: " + barStyles[i]);
                        }
                        return barStyles[i];
                    }
                }
            }
        }

        return null;
    }

    private String getRequestURL() {
        String requestURL = null;
        // this is a wicket-specific request interface
        final Request request = getRequest();
        if(request instanceof WebRequest) {
            final HttpServletRequest httpServletRequest = ((WebRequest) request).getHttpServletRequest();
            String host = HttpRequestUtils.getFarthestRequestHost(httpServletRequest);
            if(StringUtils.isBlank(host)) {
                host = httpServletRequest.getRequestURL().toString();
            }
            requestURL = host + httpServletRequest.getRequestURI();
            if(log.isDebugEnabled()) {
                log.debug("Request URL found: " + requestURL);
            }
        } else {
            log.debug("Request is not a WebRequest but " + request.getClass());
        }
        return requestURL;
    }
}
