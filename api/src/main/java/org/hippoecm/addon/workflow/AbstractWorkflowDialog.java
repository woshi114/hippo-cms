package org.hippoecm.addon.workflow;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Id$
 */
public abstract class AbstractWorkflowDialog<T> extends AbstractDialog<T> {

    private static Logger log = LoggerFactory.getLogger(AbstractWorkflowDialog.class);
    private IWorkflowInvoker invoker;

    public AbstractWorkflowDialog(IModel<T> model, IWorkflowInvoker invoker) {
        super(model);
        this.invoker = invoker;
    }

    @Override
    protected void onOk() {
        try {
            invoker.invokeWorkflow();
        } catch (Exception e) {
            log.error("Could not execute workflow.", e);
        }
    }
}
