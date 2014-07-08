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
package org.hippoecm.frontend.plugins.cms.root;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.hippoecm.frontend.model.IModelReference;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.event.IObserver;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.EditorException;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.IController;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.IEditorManager;
import org.hippoecm.frontend.service.ServiceException;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerPlugin extends Plugin implements IController {

    public static final String URL_PARAMETER_PATH = "path";
    public static final String URL_PARAMETER_UUID = "uuid";
    public static final String URL_PARAMETER_MODE = "mode";
    public static final String URL_PARAMETER_MODE_VALUE_EDIT = "edit";

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ControllerPlugin.class);

    public ControllerPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        context.registerService(this, IController.class.getName());

        registerPathInUrlController(context);
    }

    private void registerPathInUrlController(final IPluginContext context) {
        @SuppressWarnings("unchecked")
        final IModelReference<Node> modelReference = context.getService("model.browse.document", IModelReference.class);

        if (modelReference != null) {
            final PathInUrlController controller = new PathInUrlController(modelReference, URL_PARAMETER_PATH);
            context.registerService(controller, IObserver.class.getName());
            context.registerService(controller, IBehavior.class.getName());
        }
    }

    public void process(Map parameters) {
        JcrNodeModel nodeModel = loadJcrNodeModel(parameters);
        if(nodeModel != null){
            browseTo(nodeModel);
            setEditorMode(nodeModel, parameters);
        }
    }

    protected JcrNodeModel loadJcrNodeModel(final Map parameters) {
        String[] urlPaths = (String[]) parameters.get(URL_PARAMETER_PATH);
        if (urlPaths != null && urlPaths.length > 0) {
            return new JcrNodeModel(
                    WicketURLDecoder.PATH_INSTANCE.decode(urlPaths[0]));
        }

        String[] uuids = (String[]) parameters.get(URL_PARAMETER_UUID);
        if (uuids != null && uuids.length > 0) {
            String uuid = WicketURLDecoder.PATH_INSTANCE.decode(uuids[0]);
            try {
                return new JcrNodeModel(
                        UserSession.get().getJcrSession().getNodeByIdentifier(uuid));

            } catch (RepositoryException e) {
                log.info("Could not find document with uuid: {}", uuid);
            }
        }

        return null;
    }

    protected void browseTo(final JcrNodeModel nodeModel) {
        IBrowseService browseService = getPluginContext().getService(getPluginConfig().getString("browser.id", "service.browse"),
                IBrowseService.class);
        if (browseService != null) {
            browseService.browse(nodeModel);
        } else {
            log.info("Could not find browse service - document " + nodeModel.getItemModel().getPath() + " will not be selected");
        }
    }

    protected void setEditorMode(final JcrNodeModel nodeModel, final Map parameters) {
        if (parameters.containsKey(URL_PARAMETER_MODE)) {
            String[] modeStr = (String[]) parameters.get(URL_PARAMETER_MODE);
            if (modeStr != null && modeStr.length > 0) {
                IEditor.Mode mode;
                if (URL_PARAMETER_MODE_VALUE_EDIT.equals(modeStr[0])) {
                    mode = IEditor.Mode.EDIT;
                } else {
                    mode = IEditor.Mode.VIEW;
                }
                IEditorManager editorMgr = getPluginContext().getService(getPluginConfig().getString("editor.id", "service.edit"),
                        IEditorManager.class);
                if (editorMgr != null) {
                    IEditor editor = editorMgr.getEditor(nodeModel);
                    try {
                        if (editor == null) {
                            editor = editorMgr.openPreview(nodeModel);
                        }
                        editor.setMode(mode);
                    } catch (EditorException e) {
                        log.info("Could not open editor for " + nodeModel.getItemModel().getPath());
                    } catch (ServiceException e) {
                        log.info("Could not open preview for " + nodeModel.getItemModel().getPath());
                    }
                }
            }
        }
    }


}
