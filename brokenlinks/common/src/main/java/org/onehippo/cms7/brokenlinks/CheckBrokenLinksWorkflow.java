/*
 *  Copyright 2011-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.brokenlinks;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.CronExpression;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;

/**
 * @deprecated CheckBrokenLinksWorkflow has been deprecated.
 *             A repository daemon module implementing {@link BrokenLinksCheckerModule} will register a scheduled task
 *             to check broken links periodically instead.
 */
@Deprecated
public interface CheckBrokenLinksWorkflow extends Workflow {

    void checkLinks() throws WorkflowException, RepositoryException, RemoteException;

    void checkLinks(CronExpression cronExpression) throws WorkflowException, RepositoryException, RemoteException;

}