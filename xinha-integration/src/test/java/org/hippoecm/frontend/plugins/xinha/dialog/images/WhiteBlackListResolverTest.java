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

import org.junit.Assert;
import org.junit.Test;

public class WhiteBlackListResolverTest {



    @Test
    public void  getAllowed_BlackListEmptyWhiteListEmpty_returnInitial(){
        WhiteBlackListResolver whiteBlackListResolver = new WhiteBlackListResolver();
        List<String> initials = new ArrayList<String>();
        initials.add("A");
        initials.add("B");
        initials.add("C");
        initials.add("D");
        List<String> whiteList = new ArrayList<String>();
        List<String> blackList = new ArrayList<String>();
        whiteBlackListResolver.setInitials(initials);
        whiteBlackListResolver.setBlackList(blackList);
        whiteBlackListResolver.setWhiteList(whiteList);
        whiteBlackListResolver.setUseWhiteList(false);
        whiteBlackListResolver.resolve();
        final List<String> actual = whiteBlackListResolver.getAllowedList();
        Assert.assertArrayEquals(initials.toArray(),actual.toArray());
    }

    @Test
    public void  getAllowed_BlackListEmptyWhiteListNotEmpty_returnWhiteListItemIfPresentInInitial(){
        WhiteBlackListResolver whiteBlackListResolver = new WhiteBlackListResolver();
        List<String> initials = new ArrayList<String>();
        initials.add("A");
        initials.add("B");
        initials.add("C");
        initials.add("D");
        List<String> whiteList = new ArrayList<String>();
        whiteList.add("E");
        whiteList.add("A");
        List<String> blackList = new ArrayList<String>();
        whiteBlackListResolver.setInitials(initials);
        whiteBlackListResolver.setBlackList(blackList);
        whiteBlackListResolver.setWhiteList(whiteList);
        whiteBlackListResolver.setUseWhiteList(true);
        whiteBlackListResolver.resolve();
        final List<String> actual = whiteBlackListResolver.getAllowedList();
        final List<String> expected = new ArrayList<String>();
        expected.add("A");
        Assert.assertArrayEquals(expected.toArray(),actual.toArray());
    }

    @Test
    public void getAllowed_BlackListNotEmpty_WhitelistNotEmpty_returnWhiteListMinusBlackList(){
        WhiteBlackListResolver whiteBlackListResolver = new WhiteBlackListResolver();
        List<String> initials = new ArrayList<String>();
        initials.add("A");
        initials.add("B");
        initials.add("C");
        initials.add("D");
        List<String> whiteList = new ArrayList<String>();
        whiteList.add("E");
        whiteList.add("A");
        List<String> blackList = new ArrayList<String>();
        blackList.add("A");
        whiteBlackListResolver.setInitials(initials);
        whiteBlackListResolver.setBlackList(blackList);
        whiteBlackListResolver.setWhiteList(whiteList);
        whiteBlackListResolver.setUseWhiteList(true);
        whiteBlackListResolver.resolve();
        final List<String> actual = whiteBlackListResolver.getAllowedList();
        final List<String> expected = new ArrayList<String>();
        Assert.assertArrayEquals(expected.toArray(),actual.toArray());
    }
}
