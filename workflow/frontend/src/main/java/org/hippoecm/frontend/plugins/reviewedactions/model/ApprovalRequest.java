/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

package org.hippoecm.frontend.plugins.reviewedactions.model;

import java.util.Date;
import java.util.Optional;

/**
 * Contains the properties required for a document approval request.
 */
public class ApprovalRequest {

    private final String documentReference;
    private Date publicationDate;
    private Date depublicationDate;
    private String processId;

    public ApprovalRequest(final String documentReference) {
        this.documentReference = documentReference;
        this.publicationDate = new Date(0);
        this.depublicationDate = new Date(0);
        this.processId = null;
    }

    /**
     * Returns the requested document publication date
     *
     * @return publication date
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    public boolean isPublicationDate() {
        return !publicationDate.equals(new Date(0));
    }

    public ApprovalRequest setPublicationDate(final Date publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    /**
     * Returns the requested document de-publication date
     *
     * @return de-publication date
     */
    public Date getDepublicationDate() {
        return depublicationDate;
    }

    public boolean isDepublicationDate() {
        return !depublicationDate.equals(new Date(0));
    }

    public ApprovalRequest setDepublicationDate(final Date depublicationDate) {
        this.depublicationDate = depublicationDate;
        return this;
    }

    public ApprovalRequest setProcessId(final String processId){
        this.processId = processId;
        return this;
    }

    /**
     * Returns the reference that uniquely identifies the document
     *
     * @return document reference
     */
    public String getDocumentReference() {
        return documentReference;
    }

    public Optional<String> getProcessId() {
        return Optional.ofNullable(processId);
    }
}
