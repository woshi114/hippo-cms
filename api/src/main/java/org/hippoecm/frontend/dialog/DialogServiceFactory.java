/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.frontend.dialog;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.frontend.PluginRequestTarget;
import org.hippoecm.frontend.dialog.IDialogService.Dialog;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.IServiceFactory;

public class DialogServiceFactory implements IServiceFactory<IDialogService> {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private IPluginContext context;
    private String serviceId;
    private DialogWindow rootService;

    public DialogServiceFactory(String wicketId) {
        rootService = new DialogWindow("dialog");
    }

    public void init(IPluginContext context, String serviceId) {
        this.context = context;
        this.serviceId = serviceId;
        context.registerService(this, serviceId);
    }

    public void destroy() {
        context.unregisterService(this, serviceId);
    }

    public IDialogService getService(IPluginContext context) {
        return new DialogServiceWrapper();
    }

    public Class<? extends IDialogService> getServiceClass() {
        return IDialogService.class;
    }

    public void releaseService(IPluginContext context, IDialogService service) {
        ((DialogServiceWrapper) service).dispose();
    }

    public void render(PluginRequestTarget target) {
        rootService.render(target);
    }

    /**
     * Internal method to give the home page access to the dialog window.
     * @return
     */
    public Component getComponent() {
        return rootService;
    }

    /**
     * Dialog service wrapper specific to a plugin context.  This service intercepts
     * all calls from the plugin to the underlying (root) dialog service.
     */
    private class DialogServiceWrapper implements IDialogService {
        private static final long serialVersionUID = 1L;

        private Set<DialogWrapper> dialogs = new HashSet<DialogWrapper>();
        
        public void close() {
            rootService.close();
        }

        void onClose(DialogWrapper wrapper) {
            dialogs.remove(wrapper);
        }

        public void render(PluginRequestTarget target) {
            throw new UnsupportedOperationException();
        }

        public void show(Dialog dialog) {
            DialogWrapper wrapper = new DialogWrapper(this, dialog);
            dialogs.add(wrapper);
            rootService.show(wrapper);
        }

        public void dispose() {
            for (DialogWrapper wrapper : dialogs) {
                rootService.hide(wrapper);
            }
            dialogs.clear();

            rootService.showPending();
        }

    }

    /**
     * Wrapper for dialogs.  The root service will only see these dialogs.
     */
    private class DialogWrapper implements Dialog {
        private static final long serialVersionUID = 1L;

        private Dialog dialog;
        private DialogServiceWrapper service;

        DialogWrapper(DialogServiceWrapper serviceWrapper, Dialog dialog) {
            this.service = serviceWrapper;
            this.dialog = dialog;
        }

        /**
         * @return
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#getComponent()
         */
        public Component getComponent() {
            return dialog.getComponent();
        }

        /**
         * @return
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#getProperties()
         */
        public IValueMap getProperties() {
            return dialog.getProperties();
        }

        /**
         * @return
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#getTitle()
         */
        public IModel getTitle() {
            return dialog.getTitle();
        }

        /**
         * 
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#onClose()
         */
        public void onClose() {
            dialog.onClose();
            service.onClose(this);
        }

        /**
         * @param target
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#render(org.hippoecm.frontend.PluginRequestTarget)
         */
        public void render(PluginRequestTarget target) {
            dialog.render(target);
        }

        /**
         * @param dialogService
         * @see org.hippoecm.frontend.dialog.IDialogService.Dialog#setDialogService(org.hippoecm.frontend.dialog.IDialogService)
         */
        public void setDialogService(IDialogService dialogService) {
            if (dialogService == rootService) {
                dialog.setDialogService(this.service);
            } else {
                throw new IllegalStateException();
            }
        }

    }

}
