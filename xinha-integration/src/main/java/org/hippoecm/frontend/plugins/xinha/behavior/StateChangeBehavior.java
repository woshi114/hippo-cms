package org.hippoecm.frontend.plugins.xinha.behavior;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.hippoecm.frontend.plugins.xinha.AbstractXinhaPlugin;

public abstract class StateChangeBehavior extends AbstractDefaultAjaxBehavior {

    public static final String FULL_SCREEN = "fullScreen";
    public static final String ACTIVATED = "activated";

    private AbstractXinhaPlugin.Configuration configuration;

    public StateChangeBehavior(AbstractXinhaPlugin.Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void respond(final AjaxRequestTarget target) {
        Request request = RequestCycle.get().getRequest();

        if (request.getParameter(FULL_SCREEN) != null) {
            boolean fullScreen = Boolean.parseBoolean(request.getParameter(FULL_SCREEN));
            if (configuration.isRenderFullscreen() != fullScreen) {
                configuration.setRenderFullscreen(fullScreen);

                onStateChanged(FULL_SCREEN, fullScreen, target);
            }
        }

        if (request.getParameter(ACTIVATED) != null) {
            boolean activated = Boolean.parseBoolean(request.getParameter(ACTIVATED));
            if (configuration.getEditorStarted() != activated) {
                configuration.setEditorStarted(activated);
                configuration.setFocusAfterLoad(activated);

                onStateChanged(ACTIVATED, activated, target);
            }
        }
    }

    protected abstract void onStateChanged(final String param, final boolean value, final AjaxRequestTarget target);

}
