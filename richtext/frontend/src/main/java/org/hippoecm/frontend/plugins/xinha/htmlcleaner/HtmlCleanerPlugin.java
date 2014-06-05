/*
 *  Copyright 2008-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.xinha.htmlcleaner;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.richtext.IHtmlCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.hippo.htmlcleaner.HtmlCleaner;
import nl.hippo.htmlcleaner.HtmlCleanerTemplate;

public class HtmlCleanerPlugin extends Plugin implements IHtmlCleanerService {

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(HtmlCleanerPlugin.class);

    private IPluginConfig htmlCleanerConfig;
    private transient HtmlCleanerTemplate htmlCleanerTemplate;
    private boolean lenient;

    public HtmlCleanerPlugin(IPluginContext context, final IPluginConfig config) {
        super(context, config);

        htmlCleanerConfig = config.getPluginConfig("cleaner.config");
        if (htmlCleanerConfig != null) {
            lenient = htmlCleanerConfig.getAsBoolean("lenient", true);
            try {
                String serviceId = config.getString(IHtmlCleanerService.SERVICE_ID, IHtmlCleanerService.class.getName());
                context.registerService(this, serviceId);
            } catch (Exception ex) {
                log.error("Exception whole creating HTMLCleaner template:", ex);
            }
        }
    }

    public String clean(final String value) throws Exception {
        if (htmlCleanerTemplate == null) {
            try {
                htmlCleanerTemplate = new JCRHtmlCleanerTemplateBuilder().buildTemplate(htmlCleanerConfig);
            } catch (Exception ex) {
                if (lenient) {
                    log.warn("Returning uncleaned HTML because cleaner component could not create new HtmlCleanerTemplate: " + ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }

        if (htmlCleanerTemplate != null) {
            HtmlCleaner cleaner = new HtmlCleaner(htmlCleanerTemplate);
            try {
                return cleaner.cleanToString(value);
            } catch (Exception e) {
                if(lenient) {
                    log.warn("Returning uncleaned HTML because cleaner component produced an error.");
                    if(log.isDebugEnabled()) {
                        log.debug("Cleanup attempt error message is: {}", e.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        }
        return value;
    }

}
