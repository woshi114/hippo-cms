
/**
 * Hippo utilities
 */
(function (hippoTools, undefined) {


//############################################
// UTIL FUNCTIONS
//############################################
    hippoTools.opera =  !!window.opera || navigator.userAgent.indexOf('Opera') >= 0;
    hippoTools.firefox = typeof InstallTrigger !== 'undefined';
    hippoTools.safari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
    hippoTools.chrome =  !!window.chrome;
    hippoTools.explorer= /*@cc_on!@*/false;

    hippoTools.replaceHtml = function (editor, html) {
        var selection, range, nodes, selected, insert;

        selection = rangy.getIframeSelection(editor._iframe);
        range = selection.getRangeAt(0);
        nodes = range.getNodes();
        if (nodes.length === 0) {
            console.warn('No nodes found to replace element');
            return false;
        }

        if (range.createContextualFragment) { //provide fallback using div?
            insert = range.createContextualFragment(html);
            selected = hippoTools.findFirstTextNode(insert);
            range.extractContents();
            range.insertNode(insert);

            for (var i= 0; i<nodes.length; i++) {
                try {
                    nodes[i].parentNode.removeChild(nodes[i]);
                } catch (ignore) {
                    //nodes is a flat list so nodes might already be removed when their parent was removed.
                }
            }
            selection.collapse(selected, 0);
            return true;
        }
        return false;

    };

    hippoTools.findFirstTextNode = function(node) {
        for (var i=0; i<node.childNodes.length; i++) {
            var child = node.childNodes[i];
            if (child.nodeType === 3) {
                return child;
            } else if (child.nodeType === 1) {
                var value = hippoTools.findFirstTextNode(child);
                if (value != child) {
                    return value;
                }
            }
        }
        return node;
    };

    /**
     * Check if browser is at least Ie9 :
     * NOTE not checking document.documentMode
     * 10 = window.atob
     * 9 = document.addEventListener
     * 8 = document.querySelector
     * 7 = window.XMLHttpRequest
     * 6 = document.compatMode
     */
    hippoTools.isIe9 = function () {
        // document.documentMode &&  document.documentMode > 8;
        return document.all && document.addEventListener;
    };


    /**
     * Create a collection for easier lookup. e.g.:
     * <code>if(x in collection(1,2,3,4){doSomething()};</code>
     * <code>if(x in collection([1,2,3,4]){doSomething()};</code>
     * @param optional optional parameter  (type of array)
     * @returns {{}}
     */
    hippoTools.collection = function (optional) {
        var obj = {}, arr = arguments;
        if (optional instanceof Array) {
            arr = optional;
        }
        for (var i = 0; i < arr.length; i++) {
            obj[arr[i]] = true;
        }
        return obj;
    };


    /**
     * given two arrays, get same values
     * @param array1
     * @param array2
     * @returns {Array}
     */
    hippoTools.intersect = function (array1, array2) {
        var result = [];
        var c = hippoTools.collection(array2);
        for (var i = 0; i < array1.length; i++) {
            if (array1[i] in c) {
                result.push(array1[i]);
            }
        }
        return result;
    };

    /**
     *  given two arrays, get elements that are in second one but not in first one
     * @param array1
     * @param array2
     * @returns {Array}
     */
    hippoTools.complement = function (array1, array2) {
        var result = [];
        var c = hippoTools.collection(array2);
        for (var i = 0; i < array1.length; i++) {
            if (!(array1[i] in c)) {
                result.push(array1[i]);
            }
        }
        return result;
    };

    hippoTools.isEmpty = function (str) {
        return typeof str === "undefined" || str == null || str.trim().length == 0;
    };

    hippoTools.removeAttribute = function (el, name) {
        el.removeAttribute(name);
    };

    hippoTools.setAttribute = function (el, name, value) {
        el.setAttribute(name, value);
    };


    /**
     * Unwraps given element (node)
     * @param editor editor instance
     * @param el  element to unwrap
     */
    hippoTools.unwrap = function (editor, el) {
        if (el == null || el.parentNode == null) {
            return;
        }
        var parent = el.parentNode;
        var html = el.innerHTML;
        var range;
        if (document.createRange) {
            range = document.createRange();
            range.selectNodeContents(el);
        }
        // IE 9
        if ((typeof Range !== 'undefined') && !Range.prototype.createContextualFragment) {
            Range.prototype.createContextualFragment = function (html) {
                var frag = document.createDocumentFragment();
                var div = document.createElement('div');
                frag.appendChild(div);
                div.outerHTML = html;
                return frag;
            };
        }
        if (document.createRange) {
            var fragment = range.createContextualFragment(html);
            parent.replaceChild(fragment, el);
        } else {
            // IE 8
            el.outerHTML = html;
        }
    };


    /**
     * Wrap node into <code>wrapperName</code> and apply supplied classes
     * @param el node
     * @param wrapperName tag name
     * @param classes classes string to apply
     * @param skipTextNodes skip wrapping if text node
     */
    hippoTools.wrap = function (el, wrapperName, classes, skipTextNodes) {
        if (el == null || el.parentNode == null) {
            logMessage("Empty element, skipping");
            return;
        }
        var nodeType = el.nodeType;
        if (skipTextNodes && nodeType == 3) {
            logMessage("skipping text node", el);
            return;
        }
        if(nodeType == 3 && el.nodeValue.trim().length == 0){
            logMessage("skipping *empty* text node", el);
            return;
        }
        var parent = el.parentNode;
        var wrapper = document.createElement(wrapperName);
        parent.insertBefore(wrapper, el);
        parent.removeChild(el);
        wrapper.appendChild(el);
        var classesString = classes;
        if (classes instanceof Array) {
            classesString = classes.join(' ');
        }
        wrapper.setAttribute("class", classesString);
    };

    hippoTools.wrapKid = function (el, wrapperName, classes, skipText) {
        if (el == null || el.parentNode == null) {
            logMessage("Empty element, skipping");
            return;
        }
        if (el.nodeType == 3) {
            hippoTools.wrap(el, wrapperName, classes, false);
            return;
        }
        var kids = el.childNodes;
        if (kids != null) {
            for (var i = 0; i < kids.length; i++) {
                var node = kids[i];
                hippoTools.wrap(node, wrapperName, classes, skipText);
            }
        }
    };

    /**
     * Walk through given node tree
     * @param node
     * @param callback function callback
     */
    hippoTools.walk = function (node, callback) {
        var stopTraversing, tmp;
        var depth = 0;
        do {
            if (!stopTraversing) {
                stopTraversing = callback.call(node, depth) === false;
            }
            if (!stopTraversing && (tmp = node.firstChild)) {
                depth++;
            } else if (tmp = node.nextSibling) {
                stopTraversing = false;
            } else {
                tmp = node.parentNode;
                depth--;
                stopTraversing = true;
            }
            node = tmp;
        } while (depth > 0);
    };


    hippoTools.findElements = function (node, name) {
        name = name.toUpperCase();
        var el = [];
        hippoTools.walk(node, function () {
            if (this.nodeType == 1 && this.nodeName.toUpperCase() == name) {
                el.push(this);
            }
            return true;
        });
        return el;
    };

    hippoTools.containsSameNodes = function (node, name) {
        name = name.toUpperCase();
        hippoTools.walk(node, function () {
            return !(this.nodeType == 1 && this.nodeName.toUpperCase() != name);

        });
        return true;
    };

    hippoTools.findElement = function (node, name) {
        name = name.toUpperCase();
        var el = null;
        hippoTools.walk(node, function () {
            if (this.nodeType == 1 && this.nodeName.toUpperCase() == name) {
                el = this;
                return false;
            }
            return true;
        });
        return el;
    };


    hippoTools.wrapKidsOfNodes = function (node, tagCollection) {
        hippoTools.walk(node, function () {
            return !(this.nodeType == 1 && this.nodeName.toUpperCase() in tagCollection);

        });
        return true;
    };

})(window.hippoTools = window.hippoTools || {});


// https://developer.mozilla.org/en-US/docs/Web/API/DOMParser?redirectlocale=en-US&redirectslug=DOM%2FDOMParser
(function (DOMParser) {
    if(!DOMParser){
        DOMParser = {};
    }
    var DOMParser_proto = DOMParser.prototype, real_parseFromString = DOMParser_proto.parseFromString;

    // Firefox/Opera/IE throw errors on unsupported types
    try {
        // WebKit returns null on unsupported types
        if ((new DOMParser).parseFromString("", "text/html")) {
            // text/html parsing is natively supported
            return;
        }
    } catch (ex) {
    }

    DOMParser_proto.parseFromString = function (markup, type) {
        if (/^\s*text\/html\s*(?:;|$)/i.test(type)) {
            var doc = document.implementation.createHTMLDocument("");
            if (markup.toLowerCase().indexOf('<!doctype') > -1) {
                doc.documentElement.innerHTML = markup;
            }
            else {
                doc.body.innerHTML = markup;
            }
            return doc;
        } else {
            return real_parseFromString.apply(this, arguments);
        }
    };
}(DOMParser));


//############################################
// GLOBAL VARS
//############################################

/**
 * All tags defined in here will be removed (unwrapped) if:
 * - they have no id attribute
 * - class attribute value is empty
 * @type {*}
 */
var EMPTY_WRAPPERS = hippoTools.collection(["DIV", "SPAN"]);
/**
 * All tags mentioned below will be wrapped by this plugin and styles will be applied
 * to a wrapper instead of element itself
 * so, <p>foo</p> will become <wrapper class="xx"><p>foo</p></wrapper>
 * @type {*}
 */
var WRAP_TAGS = hippoTools.collection(["P", "UL", "TABLE"]);
// FOR CONVENIENCE: here are some tag names:

/*
 var tagNames = ["A", "ABBR", "ADDRESS", "AREA", "ARTICLE", "ASIDE", "AUDIO"
 , "B", "BASE", "BDI", "BDO", "BLOCKQUOTE", "BODY", "BR", "BUTTON",
 "CANVAS", "CAPTION", "CENTER", "CITE", "CODE", "COL", "COLGROUP", "COMMAND",
 "DATALIST", "DD", "DEL", "DETAILS", "DFN", "DIV", "DL", "DT", "EM", "EMBED",
 "FIELDSET", "FIGCAPTION", "FIGURE", "FONT", "FOOTER",
 "FORM", "H1", "H2", "H3", "H4", "H5", "H6", "HEAD", "HEADER",
 "HGROUP", "HR", "HTML", "I", "IFRAME", "IMG", "INPUT", "INS", "KBD",
 "KEYGEN", "LABEL", "LEGEND", "LI", "LINK", "MAP", "MARK",
 "MATH", "MENU", "META", "METER", "NAV", "NOBR",
 "NOSCRIPT", "OBJECT", "OL", "OPTION", "OPTGROUP",
 "OUTPUT", "P", "PARAM", "PRE", "PROGRESS", "Q", "RP", "RT",
 "RUBY", "S", "SAMP", "SCRIPT", "SECTION", "SELECT", "SMALL",
 "SOURCE", "SPAN", "STRONG", "STYLE", "SUB", "SUMMARY",
 "SUP", "SVG", "TABLE", "TBODY", "TD", "TEXTAREA",
 "TFOOT", "TH", "THEAD", "TIME", "TITLE", "TR", "TRACK",
 "U", "UL", "VAR", "VIDEO", "WBR"];
 */

var KIDS_WRAPPING = ['LI', 'TD', "TR"];
var WRAP_KID_TAGS = hippoTools.collection(KIDS_WRAPPING);

function logMessage(){
    // uncomment for logging:
    //console.log(arguments);
}

// Make our right side panel and insert appropriately
function Stylist(editor, args) {
    this.editor = editor;
}

Stylist._pluginInfo = {
    name: "Stylist",
    version: "1.0",
    developer: "James Sleeman",
    developer_url: "http://www.gogo.co.nz/",
    c_owner: "Gogo Internet Services",
    license: "HTMLArea",
    sponsor: "Gogo Internet Services",
    sponsor_url: "http://www.gogo.co.nz/"
};

Stylist.prototype.onGenerateOnce = function () {

    var cfg = this.editor.config['Stylist'];
    var cssFiles =  cfg !== undefined && cfg.css !== undefined &&
        Xinha.objectProperties(cfg.css).length > 0 ? cfg.css.split(',') : [];

    this.pageStyleSheets = this.pageStyleSheets || {};

    var cfgPageStyleSheets = this.editor.config.pageStyleSheets;
    if (typeof cfgPageStyleSheets != 'undefined') {
        for (var j = 0; j < cfgPageStyleSheets.length; j++) {
            var cssUrl = cfgPageStyleSheets[j], skip = false;
            if (cssFiles.length > 0) {

                for (var k=0; k<cssFiles.length; k++) {
                    if (cssUrl.indexOf(cssFiles[k]) === -1) {
                        skip = true;
                        break;
                    }
                }
            }
            if (!skip) {
                Xinha.Config.prototype.stylistLoadStylesheet(cssUrl, {}, false, this.editor.config);
            }
        }
    }

    this._prepareDialog();

    Stylist.loadAssets();
};

Stylist.loadAssets = function() {
    if (this.assetsLoaded) {
        return;
    }
    this.assetsLoaded = false;

    var modules = ['rangy-core-min.js'];

    var next = function() {
        if (modules.length > 0) {
            var url = _editor_url + 'plugins/Stylist/' + modules.shift();
            Xinha._getback(url, function(data) {
                eval.apply(window, [data]);
                next();
            });
        } else {
            this.assetsLoaded = true;

            if (rangy !== undefined) {
                rangy.init();
            } else {
                console.error('Failed to load Rangy library.');
            }
        }
    }.bind(this);

    next();
};

Stylist.prototype._prepareDialog = function () {
    var editor = this.editor;
    var stylist = this;

    var html = '<h1><l10n>Styles</l10n></h1>';

    this.dialog = new Xinha.Dialog(editor, html, 'Stylist', {width: 200}, {modal: false, closable: false});
    Xinha._addClass(this.dialog.rootElem, 'Stylist');
    this.dialog.attachToPanel('right');
    this.dialog.show();

    var dialog = this.dialog;
    var main = this.dialog.main;
    var caption = this.dialog.captionBar;

    main.style.overflow = "auto";
    main.style.height = this.editor._framework.ed_cell.offsetHeight - caption.offsetHeight + 'px';

    editor.notifyOn('modechange',
        function (e, args) {
            if (!dialog.attached) {
                return;
            }
            switch (args.mode) {
                case 'text':
                {
                    dialog.hide();
                    break;
                }
                case 'wysiwyg':
                {
                    dialog.show();
                    break;
                }
            }
        }
    );
    editor.notifyOn('panel_change',
        function (e, args) {
            if (!dialog.attached) {
                return;
            }
            switch (args.action) {
                case 'show':
                    var newHeight = main.offsetHeight - args.panel.offsetHeight;
                    main.style.height = ((newHeight > 0) ? main.offsetHeight - args.panel.offsetHeight : 0) + 'px';
                    dialog.rootElem.style.height = caption.offsetHeight + "px";
                    editor.sizeEditor();
                    break;
                case 'hide':
                    stylist.resize();
                    break;
            }
        }
    );
    editor.notifyOn('before_resize',
        function () {
            if (!dialog.attached) {
                return;
            }
            dialog.rootElem.style.height = caption.offsetHeight + "px";
        }
    );
    editor.notifyOn('resize',
        function () {
            if (!dialog.attached) {
                return;
            }
            stylist.resize();
        }
    );
};

Stylist.prototype.resize = function () {
    var rootElem = this.dialog.rootElem;

    if (rootElem.style.display == 'none') {
        return;
    }

    var panelContainer = rootElem.parentNode;

    var newSize = panelContainer.offsetHeight;
    for (var i = 0; i < panelContainer.childNodes.length; ++i) {
        if (panelContainer.childNodes[i] == rootElem || !panelContainer.childNodes[i].offsetHeight) {
            continue;
        }
        newSize -= panelContainer.childNodes[i].offsetHeight;
    }
    rootElem.style.height = newSize - 5 + 'px';
    this.dialog.main.style.height = newSize - this.dialog.captionBar.offsetHeight - 5 + 'px';
};

Stylist.prototype.onUpdateToolbar = function () {
    if (this.dialog) {
        if (this._timeoutID) {
            window.clearTimeout(this._timeoutID);
        }

        var e = this.editor;
        this._timeoutID = window.setTimeout(function () {
            e._fillStylist();
        }, 250);
    }
};

/**
 * Add an empty css_style to Config object's prototype
 *  the format is { '.className' : 'Description' }
 */
Xinha.Config.prototype.Stylist = Xinha.Config.prototype.Stylist || { };

Xinha.Config.prototype.css_style = { };


/**
 * This method takes raw style definitions and uses them in the stylist
 *
 * @param string CSS
 *
 * @param hash Alternate descriptive names for your classes
 *              { '.fooclass': 'Foo Description' }
 *
 * @param bool If set true then @import rules in the stylesheet are skipped,
 *   otherwise they will be incorporated if possible.
 *
 * @param string If skip_imports is false, this string should contain
 *   the "URL" of the stylesheet these styles came from (doesn't matter
 *   if it exists or not), it is used when resolving relative URLs etc.
 *   If not provided, it defaults to Xinha.css in the Xinha root.
 */

Xinha.Config.prototype.stylistLoadStyles = function (styles, altnames, skip_imports, imports_relative_to) {
    if (!altnames) {
        altnames = { };
    }
    var newStyles = Xinha.ripStylesFromCSSString(styles, skip_imports);
    for (var i in newStyles) {
        if (altnames[i]) {
            this.css_style[i] = altnames[i];
        }
        else {
            this.css_style[i] = newStyles[i];
        }
    }
    this.pageStyle += styles;
};


/**
 * Fill the stylist panel with styles that may be applied to the current selection.  Styles
 * are supplied in the css_style property of the Xinha.Config object, which is in the format
 * { '.className' : 'Description' }
 * classes that are defined on a specific tag (eg 'a.email_link') are only shown in the panel
 *    when an element of that type is selected.
 * classes that are defined with selectors/psuedoclasses (eg 'a.email_link:hover') are never
 *    shown (if you have an 'a.email_link' without the pseudoclass it will be shown of course)
 * multiple classes (eg 'a.email_link.staff_member') are shown as a single class, and applied
 *    to the element as multiple classes (class="email_link staff_member")
 * you may click a class name in the stylist panel to add it, and click again to remove it
 * you may add multiple classes to any element
 * spans will be added where no single _and_entire_ element is selected
 */
Xinha.prototype._fillStylist = function () {
    if (!this.plugins.Stylist.instance.dialog) {
        return false;
    }
    var main = this.plugins.Stylist.instance.dialog.main;
    main.innerHTML = '';

    var may_apply = true;
    var sel = this._getSelection();

    // What is applied
    // var applied = this._getAncestorsClassNames(this._getSelection());

    // Get an active element
    var active_elem = this._activeElement(sel);

    for (var x in this.config.css_style) {
        var tag = null;
        var className = x.trim();
        var applicable = true;
        var apply_to = active_elem;

        if (applicable && /[^a-zA-Z0-9_.-]/.test(className)) {
            applicable = false; // Only basic classes are accepted, no selectors, etc.. presumed
            // that if you have a.foo:visited you'll also have a.foo
            // alert('complex');
        }

        if (className.indexOf('.') < 0) {
            // No class name, just redefines a tag
            applicable = false;
        }

        if (applicable && (className.indexOf('.') > 0)) {
            // requires specific html tag
            tag = className.substring(0, className.indexOf('.')).toLowerCase();
            className = className.substring(className.indexOf('.'), className.length);

            // To apply we must have an ancestor tag that is the right type
            if (active_elem != null && active_elem.tagName.toLowerCase() == tag) {
                applicable = true;
                apply_to = active_elem;
            }
            else {
                if (this._getFirstAncestor(this._getSelection(), [tag]) != null) {
                    applicable = true;
                    apply_to = this._getFirstAncestor(this._getSelection(), [tag]);
                }
                else {
                    // alert (this._getFirstAncestor(this._getSelection(), tag));
                    // If we don't have an ancestor, but it's a div/span/p/hx stle, we can make one
                    if (( tag == 'div' || tag == 'span' || tag == 'p'
                        || (tag.substr(0, 1) == 'h' && tag.length == 2 && tag != 'hr'))) {
                        if (!this._selectionEmpty(this._getSelection())) {
                            applicable = true;
                            apply_to = 'new';
                        }
                        else {
                            // See if we can get a paragraph or header that can be converted
                            apply_to = this._getFirstAncestor(sel, ['p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'h7']);
                            if (apply_to != null) {
                                applicable = true;
                            }
                            else {
                                applicable = false;
                            }
                        }
                    }
                    else {
                        applicable = false;
                    }
                }
            }
        }

        if (applicable) {
            // Remove the first .
            className = className.substring(className.indexOf('.'), className.length);

            // Replace any futher ones with spaces (for multiple class definitions)
            className = className.replace('.', ' ');

            if (apply_to == null) {
                if (this._selectionEmpty(this._getSelection())) {
                    // Get the previous element and apply to that
                    apply_to = this._getFirstAncestor(this._getSelection(), null);
                }
                else {
                    apply_to = 'new';
                    tag = 'span';
                }
            }
        }

        var applied = (this._ancestorsWithClasses(sel, tag, className).length > 0 ? true : false);
        var applied_to = this._ancestorsWithClasses(sel, tag, className);

        if (applicable) {
            var anch = document.createElement('a');
            anch.onfocus = function () {
                this.blur()
            }; // prevent dotted line around link that causes horizontal scrollbar
            anch._stylist_className = className.trim();
            anch._stylist_applied = applied;
            anch._stylist_appliedTo = applied_to;
            anch._stylist_applyTo = apply_to;
            anch._stylist_applyTag = tag;

            anch.innerHTML = this.config.css_style[x];
            anch.href = 'javascript:void(0)';
            var editor = this;
            anch.onclick = function () {
                if (this._stylist_applied == true) {
                    editor._stylistRemoveClasses(editor, this._stylist_className, this._stylist_appliedTo);
                }
                else {
                    editor._stylistAddClasses(editor, this._stylist_applyTo, this._stylist_applyTag, this._stylist_className);
                }
                return false;
            };

            anch.style.display = 'block';
            anch.style.paddingLeft = '3px';
            anch.style.paddingTop = '1px';
            anch.style.paddingBottom = '1px';
            anch.style.textDecoration = 'none';

            if (applied) {
                anch.style.background = 'Highlight';
                anch.style.color = 'HighlightText';
            }
            anch.style.position = 'relative';
            main.appendChild(anch);
        }
    }
};

/**
 * Add the given classes (space separated list) to the currently selected element
 * (will add a span if none selected)
 */
Xinha.prototype._stylistAddClasses = function (editor, el, tag, classes) {
    if (el == 'new') {
        logMessage("Wrapping into new, tag is:"+ tag, el);

        // triggered when for example lists are selected etc.
        var selectedHTML = this.getSelectedHTML();
        logMessage("selected:", selectedHTML);
        var doNewWrapping = false;
        var canWrap = hippoTools.isIe9 || hippoTools.firefox || hippoTools.chrome;

        if (canWrap && tag != null) {
            var parser = new DOMParser();
            var myDom = parser.parseFromString(selectedHTML, "text/html");
            var myBody = hippoTools.findElement(myDom, "body");
            var allSame = hippoTools.wrapKidsOfNodes(myBody, WRAP_KID_TAGS);
            //TODO: detect if a listitem is selected alongside with elements outside of the ul/ol element.
            //This will cause the selectedHTML to contain a ul/ol with only the selected li in it, not the other child
            //nodes, if present. Resolution would be to treat the li separate from the other elements

            if (allSame) {
                var kidNodes = myBody.childNodes;
                for (var kidCounter = 0; kidCounter < kidNodes.length; kidCounter++) {
                    var node = kidNodes[kidCounter];
                    logMessage("wrapping kid", node);
                    hippoTools.wrapKid(node, tag, classes, false);
                }
                logMessage("BODY: ", myBody);
                logMessage("BODY: ", myBody.innerHTML);
                logMessage("all same nodes, wrapped");
                // TODO test this:
                hippoTools.replaceHtml(editor, myBody.innerHTML);
                //this.insertHTML(myBody.innerHTML);
                doNewWrapping = true;
            }
        }
        if (!doNewWrapping) {
            logMessage("Wrapping whole selection into new tag:");
            this.insertHTML('<' + tag + ' class="' + classes + '">' + selectedHTML + '</' + tag + '>');
        }
    }
    else {
        var existingTag = el.tagName.toUpperCase();
        if (tag != null) {
            var toInsertTag = tag.toUpperCase();
            var shouldWrap = existingTag in WRAP_TAGS;
            // we should leave existing tag and wrap it:
            if (shouldWrap) {
                hippoTools.wrap(el, toInsertTag, classes);
                logMessage("wrapped element into: ", tag);
            } else if (toInsertTag != existingTag) {
                var new_el = this.switchElementTag(el, tag);
                Xinha._addClasses(new_el, classes);
                logMessage("swapped element" + existingTag + " to: ", toInsertTag);
            } else {
                Xinha._addClasses(el, classes);
                logMessage("Just added classes xx: ", classes);
            }


        } else {

            // just add classes to existing element:
            Xinha._addClasses(el, classes);
            logMessage("Just added classes: " , classes);
        }
    }
    this.focusEditor();
    this.updateToolbar();
};



/**
 * Removes CSS classes when unselected. NOTE: in case there are no classes left, wrapping element is also removed
 * @param el
 * @param classes
 * @private
 */
Xinha.prototype._stylistRemoveClassesFull = function (editor, el, classes) {
    if (el != null) {
        var classNames = el.className.trim().split(' ');
        var ourClasses = classes.split(' ');
        var elementId = el.id;
        var remainingClasses = hippoTools.complement(classNames, ourClasses);
        if (remainingClasses.length == 0) {
            // NOTE: if id is there, keep node:
            if (hippoTools.isEmpty(elementId)) {
                // remove wrapping element:
                // NOTE: should we check other attributes as well, e.g. style etc.?
                // check if we need to remove element:
                if(el.tagName.toUpperCase() in EMPTY_WRAPPERS){
                    logMessage("unwrapping: ", el);
                    hippoTools.unwrap(editor, el);
                } else{
                    hippoTools.removeAttribute(el, "class");
                    logMessage("@ removed attribute, class, not in wrapper collection", elementId);
                }

            } else {
                // remove classes attribute only:
                hippoTools.removeAttribute(el, "class");
                logMessage("@ removed attribute, class, id is present: ", elementId);
            }

        } else {
            // we still have (custom) classes, keep everything as it is...
            var clazzString = remainingClasses.join(' ');
            hippoTools.setAttribute(el, "class", clazzString);
            logMessage("Setting class attribute to:[" + clazzString + "]");
        }
    }
};


/**
 * Change the tag of an element
 */
Xinha.prototype.switchElementTag = function (el, tag) {
    var prnt = el.parentNode;
    var new_el = this._doc.createElement(tag);

    if (Xinha.is_ie || el.hasAttribute('id')) {
        new_el.setAttribute('id', el.getAttribute('id'));
    }
    if (Xinha.is_ie || el.hasAttribute('style')) {
        new_el.setAttribute('style', el.getAttribute('style'));
    }

    var childs = el.childNodes;
    for (var x = 0; x < childs.length; x++) {
        new_el.appendChild(childs[x].cloneNode(true));
    }

    prnt.insertBefore(new_el, el);
    new_el._stylist_usedToBe = [el.tagName];
    prnt.removeChild(el);
    this.selectNodeContents(new_el);
    return new_el;
};

Xinha.prototype._getAncestorsClassNames = function (sel) {
    // Scan upwards to find a block level element that we can change or apply to
    var prnt = this._activeElement(sel);
    if (prnt == null) {
        prnt = (Xinha.is_ie ? this._createRange(sel).parentElement() : this._createRange(sel).commonAncestorContainer);
    }

    var classNames = [ ];
    while (prnt) {
        if (prnt.nodeType == 1) {
            var classes = prnt.className.trim().split(' ');
            for (var x = 0; x < classes.length; x++) {
                classNames[classNames.length] = classes[x];
            }

            if (prnt.tagName.toLowerCase() == 'body') {
                break;
            }
            if (prnt.tagName.toLowerCase() == 'table') {
                break;
            }
        }
        prnt = prnt.parentNode;
    }

    return classNames;
};


//############################################
// FINDER
//############################################

Xinha.prototype._ancestorsWithClasses = function (sel, tag, classes) {
    var ancestors = [ ];
    var prnt = this._activeElement(sel);
    if (prnt == null) {
        try {
            prnt = (Xinha.is_ie ? this._createRange(sel).parentElement() : this._createRange(sel).commonAncestorContainer);
        }
        catch (e) {
            return ancestors;
        }
    }
    var search_classes = classes.trim().split(' ');

    while (prnt) {
        if (prnt.nodeType == 1 && prnt.className) {
            if (tag == null || prnt.tagName.toLowerCase() == tag) {
                var myCLasses = prnt.className.trim().split(' ');
                var found_all = true;
                for (var i = 0; i < search_classes.length; i++) {
                    var found_class = false;
                    for (var x = 0; x < myCLasses.length; x++) {
                        if (search_classes[i] == myCLasses[x]) {
                            found_class = true;
                            break;
                        }
                    }

                    if (!found_class) {
                        found_all = false;
                        break;
                    }
                }

                if (found_all) {
                    ancestors[ancestors.length] = prnt;
                }
            }
            if (prnt.tagName.toLowerCase() == 'body') {
                break;
            }
            if (prnt.tagName.toLowerCase() == 'table') {
                break;
            }
        }
        prnt = prnt.parentNode;
    }

    return ancestors;
};


//############################################
// COMMON STUFF
//############################################


/**
 * Remove the given classes (space separated list) from the given elements (array of elements)
 */
Xinha.prototype._stylistRemoveClasses = function (editor, classes, from) {

    for (var x = 0; x < from.length; x++) {
        this._stylistRemoveClassesFull(editor, from[x], classes);
    }
    this.focusEditor();
    this.updateToolbar();
};


Xinha.ripStylesFromCSSFile = function (URL, skip_imports) {
    var css = Xinha._geturlcontent(URL);

    return Xinha.ripStylesFromCSSString(css, skip_imports, URL);
};

Xinha.ripStylesFromCSSString = function (css, skip_imports, imports_relative_to) {
    if (!skip_imports) {
        if (!imports_relative_to) {
            imports_relative_to = _editor_url + 'Xinha.css'
        }

        var seen = { };

        function resolve_imports(css, url) {
            seen[url] = true; // protects against infinite recursion

            var RE_atimport = '@import\\s*(url\\()?["\'](.*)["\'].*';
            var imports = css.match(new RegExp(RE_atimport, 'ig'));
            var m, file, re = new RegExp(RE_atimport, 'i');

            if (imports) {
                var path = url.replace(/\?.*$/, '').split("/");
                path.pop();
                path = path.join('/');
                for (var i = 0; i < imports.length; i++) {

                    m = imports[i].match(re);
                    file = m[2];
                    if (!file.match(/^([^:]+\:)?\//)) {
                        file = Xinha._resolveRelativeUrl(path, file);
                    }

                    if (seen[file] || file.toLowerCase().indexOf("xinhainternal.css") >= 0) {
                        continue;
                    }

                    css += resolve_imports(Xinha._geturlcontent(file), file);
                }
            }

            return css;
        }

        css = resolve_imports(css, imports_relative_to);
    }

    // We are only interested in the selectors, the rules are not important
    //  so we'll drop out all comments and rules
    var RE_comment = /\/\*(.|\r|\n)*?\*\//g;
    var RE_rule = /\{(.|\r|\n)*?\}/g;
    css = css.replace(RE_comment, '');
    css = css.replace(RE_rule, ',');

    // And split on commas
    css = css.split(',');

    // And add those into our structure
    var selectors = { };
    for (var x = 0; x < css.length; x++) {
        if (css[x].trim()) {
            selectors[css[x].trim()] = css[x].trim();
        }
    }


    return selectors;
};


/**
 * This method loads an external stylesheet and uses it in the stylist
 *
 * @param string URL to the stylesheet
 * @param hash Alternate descriptive names for your classes
 *              { '.fooclass': 'Foo Description' }
 * @param bool If set true then @import rules in the stylesheet are skipped,
 *   otherwise they will be incorporated if possible.
 */

Xinha.Config.prototype.stylistLoadStylesheet = function (url, altnames, skip_imports, config) {
    if (typeof config == 'undefined') {
        // backwards compatibility
        config = this;
    }
    if (!altnames) {
        altnames = { };
    }
    var newStyles = Xinha.ripStylesFromCSSFile(url, skip_imports);
    for (var i in newStyles) {
        if (altnames[i]) {
            config.css_style[i] = altnames[i];
        }
        else {
            config.css_style[i] = newStyles[i];
        }
    }
    if (typeof config.pageStyleSheets == 'undefined') {
        config.pageStyleSheets = {};
    }
    for (var x = 0; x < config.pageStyleSheets.length; x++) {
        if (config.pageStyleSheets[x] == url) {
            return;
        }
    }
    config.pageStyleSheets[cfg.pageStyleSheets.length] = url;
};