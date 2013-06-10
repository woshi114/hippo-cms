AutoSave._pluginInfo = {
  name          : "AutoSave",
  version       : "1.0",
  developer    : "a.bogaart@1hippo.com",
  developer_url : "http://www.hippo.nl",
  c_owner       : "Arthur Bogaart",
  sponsor       : "",
  sponsor_url   : "",
  license       : "AL2"
}

Xinha.Config.prototype.AutoSave =
{
  'timeoutLength' : 2000,
  'callbackUrl' : ''
};

function AutoSave(editor) {
    this.editor = editor;
    this.lConfig = editor.config.AutoSave;

    this.timeoutID = null; // timeout ID, editor is dirty when non-null
    this.saving = false;   // whether a save is in progress

    // translation context
    this.context = {
        url : _editor_url + 'plugins/AutoSave/lang/',
        context: "AutoSave"
    };

    //Atach onkeyup and onchange event listeners to textarea for autosaving in htmlmode
    var txtArea = this.editor._textArea;

    var fn = Xinha.proxy(this, this.checkChanges);
    if(YAHOO.util.Event) {
        YAHOO.util.Event.addListener(txtArea, 'keyup', fn);
        YAHOO.util.Event.addListener(txtArea, 'cut', fn);
        YAHOO.util.Event.addListener(txtArea, 'paste', fn);
    } else {
        if (txtArea.addEventListener) {
            txtArea.addEventListener('keyup', fn, false);
            txtArea.addEventListener('cut', fn, false);
            txtArea.addEventListener('paste', fn, false);
        } else if (txtArea.attachEvent) {
            txtArea.attachEvent('onkeyup', fn);
            txtArea.attachEvent('cut', fn);
            txtArea.attachEvent('paste', fn);
        } else {
            txtArea['onkeyup'] = fn;
            txtArea['cut'] = fn;
            txtArea['paste'] = fn;
        }
    }

    // optional button for explicit save
    var cfg = editor.config;

    cfg.registerIcon('save', [_editor_url + cfg.imgURL + 'ed_buttons_main.png', 9, 1]);
    cfg.registerButton('save', this._lc("Save"), cfg.iconList.save, true, Xinha.proxy(this, this.saveAndClose));

}

AutoSave.prototype = {
    _lc : function(string) {
        return Xinha._lc(string, this.context);
    },

    getId : function() {
        return this.editor._textArea.getAttribute("id");
    },

    getContents : function() {
        return this.editor.getInnerHTML();
    },

    saveAndClose : function() {
        var id = this.getId();
        var close = Xinha.proxy(this, function() {
            if (this.editor.plugins['StateChange'] && this.editor.plugins['StateChange'].instance) {
                this.editor.plugins['StateChange'].instance.setActivated(false, function() {
                    // Deactivate editor at context. Although this happens after the render() has been called,
                    // it is enough to simply remove the editor from the activeEditors list.
                    YAHOO.hippo.EditorManager.deactivateEditor(id);
                });
            }
        });

        if (this.timeoutID == null) {
            close();
        } else {
            this.save(false, close, close);
        }
    },

    // Save the contents of the xinha field.  Only one throttled request is executed concurrently.
    save : function(throttled, success, failure) {
        // nothing to do if editor is not dirty
        if (this.timeoutID == null) {
            return;
        } else {
            window.clearTimeout(this.timeoutID);
        }

        if (throttled) {
            // reschedule when a save is already in progress
            if (this.saving) {
                this.checkChanges();
                return;
            }
            this.saving = true;
        }

        this.timeoutID = null;

        if (this.editor._editMode == 'wysiwyg') { //save Iframe html into textarea
            this.editor._textArea.value = this.editor.outwardHtml(this.editor.getHTML());
        }

        var body = wicketSerialize(Wicket.$(this.getId()));
        var url = this.editor.config.callbackUrl;

        var xmlHttpReq = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
        var afterCallbackHandler = function() {
            if (throttled) {
                this.saving = false;
            }
            if(xmlHttpReq.status == 200) {
                if (success) {
                    success();
                }
            } else if(failure) {
                failure();
            }
        }.bind(this);

        xmlHttpReq.open('POST', url, throttled);
        xmlHttpReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xmlHttpReq.setRequestHeader('Wicket-Ajax', "true");
        if (throttled) {
            xmlHttpReq.onreadystatechange = function() {
                if (xmlHttpReq.readyState == 4) {
                    afterCallbackHandler();
                }
            };
        }
        xmlHttpReq.send(body);

        if (!throttled) {
            afterCallbackHandler();
        }
    },

    onUpdateToolbar : function() {
        this.checkChanges();
    },

    onKeyPress : function(ev) {
        if( ev != null && ev.ctrlKey && this.editor.getKey(ev) == 's') {
            this.save(true);
            Xinha._stopEvent(ev);
            return true;
        }
        this.checkChanges();
    },

    checkChanges : function() {
        if(this.timeoutID != null) {
            window.clearTimeout(this.timeoutID);
        }

        var editorId = this.getId();
        this.timeoutID = window.setTimeout(function() {
            YAHOO.hippo.EditorManager.saveByTextareaId(editorId);
        }, this.lConfig.timeoutLength);
    },

    /**
     * Explicitly replace <p> </p> with general-purpose space (U+0020) with a <p> </p> including a non-breaking space (U+00A0)
     * to prevent the browser from not rendering these paragraphs
     *
     * See http://issues.onehippo.com/browse/HREPTWO-1713 for more info
     */
    inwardHtml : function(html) {
        this.imgRE = new RegExp('<p> <\/p>', 'gi');
        html = html.replace(this.imgRE, function(m) {
            return '<p>&nbsp;</p>';
        });
        return html;
    }
};
