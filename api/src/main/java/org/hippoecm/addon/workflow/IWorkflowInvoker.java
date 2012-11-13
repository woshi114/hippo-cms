package org.hippoecm.addon.workflow;

import org.apache.wicket.IClusterable;
/**
 * @version $Id$
 */
public interface IWorkflowInvoker extends IClusterable {

    public void invokeWorkflow() throws Exception;
}
