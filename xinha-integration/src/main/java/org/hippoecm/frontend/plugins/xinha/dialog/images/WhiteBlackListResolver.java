/*
 *  Copyright 2008 Hippo.
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

package org.hippoecm.frontend.plugins.xinha.dialog.images;

import java.util.ArrayList;
import java.util.List;

public class WhiteBlackListResolver {

    private List<String> initials;
    private List<String> whiteList;
    private List<String> blackList;
    private List<String> allowedList = new ArrayList<String>();
    private boolean useWhiteList;


    /**
     *
     * @param initials  list with initial items
     * @param blackList list with items that should be omitted
     * @return The intersection of initials and blacklist
     */
    public static List<String> getAllowedList(List<String> initials, List<String> blackList) {
        WhiteBlackListResolver whiteBlackListResolver = new WhiteBlackListResolver();
        whiteBlackListResolver.setInitials(initials);
        whiteBlackListResolver.setBlackList(blackList);
        whiteBlackListResolver.setWhiteList(new ArrayList<String>());
        whiteBlackListResolver.setUseWhiteList(false);
        whiteBlackListResolver.resolve();
        return whiteBlackListResolver.getAllowedList();
    }

    /**
     * Constructs a list based on the initial item, a blacklist and a whitelist
     *
     * @param initials list with initial items
     * @param blackList list with items that should be omitted
     * @param whiteList list with items that should be added
     * @return The intersection of initials and whitelist minus the blacklist
     */
    public static List<String> getAllowedList(List<String> initials, List<String> blackList, List<String> whiteList) {
        WhiteBlackListResolver whiteBlackListResolver = new WhiteBlackListResolver();
        whiteBlackListResolver.setInitials(initials);
        whiteBlackListResolver.setBlackList(blackList);
        whiteBlackListResolver.setWhiteList(whiteList);
        whiteBlackListResolver.setUseWhiteList(true);
        whiteBlackListResolver.resolve();
        return whiteBlackListResolver.getAllowedList();
    }

    void resolve() {
        allowedList.addAll(initials);
        if (useWhiteList) {
            allowedList.retainAll(whiteList);
        }
        allowedList.removeAll(blackList);
    }

    void setUseWhiteList(final boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    List<String> getAllowedList() {
        return allowedList;
    }

    void setInitials(final List<String> initials) {
        this.initials = initials;
    }

    void setWhiteList(final List<String> whiteList) {
        this.whiteList = whiteList;
    }

    void setBlackList(final List<String> blackList) {
        this.blackList = blackList;
    }
}
