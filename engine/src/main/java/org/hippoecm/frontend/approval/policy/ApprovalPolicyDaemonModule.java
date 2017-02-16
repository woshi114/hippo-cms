/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.approval.policy;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DaemonModule to load configuration for the {@link ApprovalPolicyService}.
 */
public class ApprovalPolicyDaemonModule extends AbstractReconfigurableDaemonModule {

    private static final String PROCESS_ID = "processId";
    private static Logger log = LoggerFactory.getLogger(ApprovalPolicyDaemonModule.class);
    private final Object configurationLock = new Object();
    private ApprovalPolicyService service;
    private String processId;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        synchronized (configurationLock) {
            processId = JcrUtils.getStringProperty(moduleConfig, PROCESS_ID, null);
            log.info("Reconfigured {}", this);
        }
    }

    @Override
    protected void doInitialize(final Session session) throws RepositoryException {
        HippoServiceRegistry.registerService(service = () -> processId, ApprovalPolicyService.class);
    }

    @Override
    protected void doShutdown() {
        HippoServiceRegistry.unregisterService(service, ApprovalPolicyService.class);
    }

    @Override
    public String toString() {
        return "ApprovalPolicyDaemonModule{" +
                "processId='" + processId + '\'' +
                '}';
    }
}
