package org.hippoecm.frontend.plugins.console.menu.check;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckInOutPlugin extends RenderPlugin<Node> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(CheckInOutPlugin.class);
    private final AjaxLink<Void> link;
            
    public CheckInOutPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        final Label label = new Label("link-text", new Model<String>() {
            private static final long serialVersionUID = 1L;
            @Override public String getObject() {
                return isCheckedOut() ? "Check In" : "Check Out";
            }
        });
        label.setOutputMarkupId(true);
        label.add(new AttributeModifier("style", true, new Model<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                if (!isVersionable()) {
                    return "color:grey";
                }
                return isCheckedOut() ? "color:green" : "color:red";
            }
        }));
        link = new AjaxLink<Void>("link") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (isVersionable()) {
                    if (isCheckedOut()) {
                        checkin();
                    }
                    else {
                        checkout();
                    }
                }
                target.addComponent(label);
            }
        };
        link.add(label);
        link.setEnabled(isVersionable());
        add(link);
    }
    
    private boolean isCheckedOut() {
        try {
            return getModelObject().isCheckedOut();
        } catch (RepositoryException e) {
            log.error("An error occurred determining if node is checked out.", e);
            return false;
        }
    }
    
    private boolean isVersionable() {
        try {
            return getModelObject().isNodeType("mix:versionable");
        } catch (RepositoryException e) {
            log.error("An error occurred determining if node is versionable.", e);
            return false;
        }
    }
    
    private void checkin() {
        try {
            getModelObject().checkin();
        } catch (RepositoryException e) {
            log.error("An error occurred trying to check in node.", e);
        }
    }
    
    private void checkout() {
        try {
            getModelObject().checkout();
        } catch (RepositoryException e) {
            log.error("An error occurred trying to check out node.", e);
        }
    }
    
    private Session getJcrSession() {
        Session session = ((UserSession) org.apache.wicket.Session.get()).getJcrSession();
        return session;
    }
    
    @Override
    protected void onModelChanged() {
        link.setEnabled(isVersionable());
        redraw();
    }

}
