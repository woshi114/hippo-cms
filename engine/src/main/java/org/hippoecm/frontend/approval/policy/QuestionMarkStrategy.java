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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The QuestionMarkStrategy mimics a BPM process that immediately sends a publication request to the
 * Rest API that handles the publication
 */
public class QuestionMarkStrategy implements ApprovalStrategy {

    private static final Logger log  = LoggerFactory.getLogger(QuestionMarkStrategy.class);
    @Override
    public void requestPublication(final String identifier, final Date publicationDate, final Date takeOfflineDate, final String processId) {
        log.info("Calling publication rest api end point: {\"identifier\":{},\"publicationDate\":{},\"takeOfflineDate\":{},\"processId\":{}"
                , identifier, publicationDate, takeOfflineDate,processId);
    }
}
