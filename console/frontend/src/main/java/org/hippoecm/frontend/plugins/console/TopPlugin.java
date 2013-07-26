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

public class TopPlugin extends RenderPlugin {

    public TopPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        WebMarkupContainerWithAssociatedMarkup breadcrumb =
                    new WebMarkupContainerWithAssociatedMarkup("bar.styles");

        breadcrumb.add(new AbstractBehavior() {

            public void onComponentTag(Component component, ComponentTag tag) {
                String style = obtainBreadcrumbStyle(config);
                if (StringUtils.isNotEmpty(style)); {
                    tag.put("style", style);
                }
            }
        });

        add(breadcrumb);
    }

    public String obtainBreadcrumbStyle(IPluginConfig config){

        String[] urlparts = config.getStringArray("bar.style.urlparts");
        String[] barStyles = config.getStringArray("bar.styles");

            if(urlparts != null){
            for (int i = 0 ; urlparts.length > i; i++){

                if (StringUtils.isNotEmpty(urlparts[i])){
                    String urlpart = urlparts[i];
                    String url = getRequestUrl();
                    if(url.contains(urlpart) && barStyles != null && StringUtils.isNotBlank(barStyles[i])){
                        return barStyles[i];
                    }
                }
            }
        }

        return "";
    }

    private String getRequestUrl(){
        // this is a wicket-specific request interface
        final Request request = getRequest();
        if(request instanceof WebRequest){
            final WebRequest wr = (WebRequest) request;
            // but this is the real thing
            final HttpServletRequest hsr = wr.getHttpServletRequest();
            return hsr.getRequestURL().toString();
        }
        return null;

    }
}
