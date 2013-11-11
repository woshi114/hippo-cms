/*
 * Copyright 2010-2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

Hippo.EnterHandler = function(elementId) {
    var obj, objonchange;

    obj = Wicket.$(elementId);
    obj.setAttribute("autocomplete", "off");

    objonchange = obj.onchange;
    obj.onkeypress = function(event) {
        Wicket.stopEvent(event);
        var key = wicketKeyCode(Wicket.fixEvent(event));
        if (key === 13) {
            objonchange();
            return false;
        }
        return true;
    };

    obj.onkeyup = function(event) {
        Wicket.stopEvent(event);
        return true;
    };

    obj.onchange = function(event) {
        Wicket.stopEvent(event);
        return false;
    };

};