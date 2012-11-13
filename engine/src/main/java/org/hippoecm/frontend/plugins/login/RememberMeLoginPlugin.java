/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.frontend.plugins.login;

import java.security.AccessControlException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.hippoecm.frontend.AuditLogger;
import org.hippoecm.frontend.PageExpiredErrorPage;
import org.hippoecm.frontend.PluginPage;
import org.hippoecm.frontend.custom.ServerCookie;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.frontend.util.AclChecker;
import org.hippoecm.frontend.util.WebApplicationHelper;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoSecurityEventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RememberMeLoginPlugin extends LoginPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id: $";

    private static final int COOKIE_DEFAULT_MAX_AGE = 1209600;
    private final String REMEMBERME_COOKIE_NAME = WebApplicationHelper.getFullyQualifiedCookieName(WebApplicationHelper.REMEMBERME_COOKIE_BASE_NAME);
    private final String HIPPO_AUTO_LOGIN_COOKIE_NAME = WebApplicationHelper.getFullyQualifiedCookieName(WebApplicationHelper.HIPPO_AUTO_LOGIN_COOKIE_BASE_NAME);
    private static final String HAL_REQUEST_ATTRIBUTE_NAME = "in_try_hippo_autologin";
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(RememberMeLoginPlugin.class);

    /** Algorithm to use for creating the passkey secret.
        Intentionally a relative weak algorithm, as this whole procedure isn't
        too safe to begin with.
    */
    static final String ALGORITHM = "MD5";

    public RememberMeLoginPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        String[] supported = config.getStringArray("browsers.supported");
        if (supported != null) {
            add(new BrowserCheckBehavior(supported));
        }
    }

    // Determine whether to try to auto-login or not
    @Override
    protected void onInitialize() {
        if (!PageExpiredErrorPage.class.isInstance(getPage())) {
            // Check for remember me cookie
            if ((WebApplicationHelper.retrieveWebRequest().getCookie(REMEMBERME_COOKIE_NAME) != null)
                    && (WebApplicationHelper.retrieveWebRequest().getCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME) != null)
                    && (WebApplicationHelper.retrieveWebRequest().getHttpServletRequest()
                            .getAttribute(HAL_REQUEST_ATTRIBUTE_NAME) == null)) {

                WebApplicationHelper.retrieveWebRequest().getHttpServletRequest()
                        .setAttribute(HAL_REQUEST_ATTRIBUTE_NAME, true);

                try {
                    tryToAutoLoginWithRememberMe();
                } finally {
                    WebApplicationHelper.retrieveWebRequest().getHttpServletRequest()
                            .removeAttribute(HAL_REQUEST_ATTRIBUTE_NAME);
                }
            }
        }

        super.onInitialize();
    }

    protected void tryToAutoLoginWithRememberMe() {
        SignInForm signInForm = (SignInForm) createSignInForm("rememberMeAutoLoginSignInForm");

        Cookie remembermeCookie = WebApplicationHelper.retrieveWebRequest().getCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
        String passphrase = remembermeCookie.getValue();
        String strings[] = passphrase.split("\\$");
        if (strings.length == 3) {
            username = new String(Base64.decodeBase64(strings[1]));
            password = strings[0] + "$" + strings[2];

            signInForm.onSubmit();
        } else {
            error("Invalid cookie format for " + HIPPO_AUTO_LOGIN_COOKIE_NAME);
        }

    }

    @Override
    protected LoginPlugin.SignInForm createSignInForm(String id) {
        Cookie rememberMeCookie = WebApplicationHelper.retrieveWebRequest().getCookie(
                WebApplicationHelper.getFullyQualifiedCookieName(WebApplicationHelper.REMEMBERME_COOKIE_BASE_NAME));

        boolean rememberme = (rememberMeCookie != null) ? Boolean.valueOf(rememberMeCookie.getValue()) : false;

        if (rememberme) {
            Cookie halCookie = WebApplicationHelper.retrieveWebRequest().getCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
            if (halCookie != null) {
                String passphrase = rememberMeCookie.getValue();
                if (passphrase != null && passphrase.contains("$")) {
                    username = new String(Base64.decodeBase64(passphrase.split("\\$")[1]));
                    password = "********";
                }
            }
        }

        return new SignInForm(id, rememberme);
    }

    protected class SignInForm extends org.hippoecm.frontend.plugins.login.LoginPlugin.SignInForm {
        private static final long serialVersionUID = 1L;

        private boolean rememberme;

        public void setRememberme(boolean value) {
            rememberme = value;
        }

        public boolean getRememberme() {
            return rememberme;
        }

        public SignInForm(final String id, boolean rememberme) {
            super(id);

            this.rememberme = rememberme;

            if (RememberMeLoginPlugin.this.getPluginConfig().getAsBoolean("signin.form.autocomplete", true)) {
                add(new AttributeModifier("autocomplete", true, new Model<String>("on")));
            } else {
                add(new AttributeModifier("autocomplete", true, new Model<String>("off")));
            }

            CheckBox rememberMeCheckbox = new CheckBox("rememberme", new PropertyModel<Boolean>(this, "rememberme"));
            add(rememberMeCheckbox);
            rememberMeCheckbox.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                private static final long serialVersionUID = 1L;

                protected void onUpdate(AjaxRequestTarget target) {
                    // When the 'Remember me' check-box is un-checked Username and Password fields should be cleared 
                    if (!SignInForm.this.getRememberme()) {
                        SignInForm.this.usernameTextField.setModelObject("");
                        SignInForm.this.passwordTextField.setModelObject("");
                        // Also remove the cookie which contains user information
                        WebApplicationHelper.clearCookie(REMEMBERME_COOKIE_NAME);
                        WebApplicationHelper.clearCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
                    } else {
                        Cookie remembermeCookie = new Cookie(REMEMBERME_COOKIE_NAME, String.valueOf(true));
                        remembermeCookie.setMaxAge(RememberMeLoginPlugin.this.getPluginConfig().getAsInteger(
                                "rememberme.cookie.maxage", COOKIE_DEFAULT_MAX_AGE));

                        WebApplicationHelper.retrieveWebResponse().addCookie(remembermeCookie);
                    }

                    setResponsePage(this.getFormComponent().getPage());
                }
            });

            usernameTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                private static final long serialVersionUID = 1L;

                protected void onUpdate(AjaxRequestTarget target) {
                    WebApplicationHelper.clearCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
                }
            });

            passwordTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                private static final long serialVersionUID = 1L;

                protected void onUpdate(AjaxRequestTarget target) {
                    WebApplicationHelper.clearCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
                }
            });

        }

        @Override
        public final void onSubmit() {
            PluginUserSession userSession = (PluginUserSession) getSession();

            if (!rememberme) {
                WebApplicationHelper.clearCookie(REMEMBERME_COOKIE_NAME);
                WebApplicationHelper.clearCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);
                // This still exists in case there is a cookie with the old name still exists in browser's cache
                WebApplicationHelper.clearCookie(getClass().getName());
            }

            boolean success = true;
            PageParameters loginExceptionPageParameters = null;

            try {
                userSession.login(new UserCredentials(new SimpleCredentials(username, password.toCharArray())));
                AclChecker.checkAccess(getPluginConfig(), userSession.getJcrSession());
            } catch (org.hippoecm.frontend.session.LoginException le) {
                success = false;
                loginExceptionPageParameters = buildPageParameters(le.getLoginExceptionCause());
            } catch (AccessControlException ace) {
                success = false;
                // Invalidate the current obtained JCR session and create an anonymous one
                userSession.login();
                loginExceptionPageParameters = buildPageParameters(org.hippoecm.frontend.session.LoginException.CAUSE.ACCESS_DENIED);
            } catch (RepositoryException re) {
                success = false;
                // Invalidate the current obtained JCR session and create an anonymous one
                userSession.login();
                if (log.isDebugEnabled()) {
                    log.warn("Repository error while trying to access the "
                            + WebApplicationHelper.getApplicationName("cms") + " application with user '" + username
                            + "'", re);
                }

                loginExceptionPageParameters = buildPageParameters(org.hippoecm.frontend.session.LoginException.CAUSE.REPOSITORY_ERROR);
            }

            if (success) {
                ConcurrentLoginFilter.validateSession(((WebRequest) SignInForm.this.getRequest())
                        .getHttpServletRequest().getSession(true), username, false);

                // If rememberme checkbox is checked and there is no cookie already, this happens in case of autologin
                if (rememberme
                        && WebApplicationHelper.retrieveWebRequest().getCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME) == null) {

                    Session jcrSession = userSession.getJcrSession();
                    if (jcrSession.getUserID().equals(username)) {
                        try {
                            Node userinfo = RememberMeLoginPlugin.this.getUserInfo(jcrSession);

                            if (userinfo != null) {
                                MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
                                digest.update(username.getBytes());
                                digest.update(password.getBytes());
                                String passphrase = digest.getAlgorithm() + "$"
                                        + Base64.encodeBase64URLSafeString(username.getBytes()) + "$"
                                        + Base64.encodeBase64URLSafeString(digest.digest());

                                String[] strings = passphrase.split("\\$");
                                userinfo.setProperty(HippoNodeType.HIPPO_PASSKEY, strings[0] + "$" + strings[2]);
                                userinfo.save();

                                final Cookie halCookie = new Cookie(HIPPO_AUTO_LOGIN_COOKIE_NAME, passphrase);
                                halCookie.setMaxAge(RememberMeLoginPlugin.this.getPluginConfig().getAsInteger(
                                        "hal.cookie.maxage", COOKIE_DEFAULT_MAX_AGE));

                                halCookie.setSecure(RememberMeLoginPlugin.this.getPluginConfig().getAsBoolean(
                                        "use.secure.cookies", false));

                                // Replace with Cookie#setHttpOnly when we upgrade to a container compliant with
                                // Servlet API(s) v3.0t his was added cause the setHttpOnly/isHttpOnly at the time of
                                // developing this code were not available cause we used to use Servlet API(s) v2.5
                                RememberMeLoginPlugin.this.addCookieWithHttpOnly(
                                        halCookie,
                                        WebApplicationHelper.retrieveWebResponse(),
                                        RememberMeLoginPlugin.this.getPluginConfig().getAsBoolean("use.httponly.cookies",
                                                false));
                            } else {
                                loginExceptionPageParameters = buildPageParameters(org.hippoecm.frontend.session.LoginException.CAUSE.REPOSITORY_ERROR);
                                handleLoginFailure(userSession);
                                success = false;
                            }
                        } catch (NoSuchAlgorithmException ex) {
                            log.error(ex.getClass().getName() + ": " + ex.getMessage());
                        } catch (LoginException ex) {
                            log.info("Invalid login as user: " + username);
                        } catch (RepositoryException ex) {
                            log.error(ex.getClass().getName() + ": " + ex.getMessage());
                        }
                    }
                }

                HippoEvent event = new HippoEvent(userSession.getApplicationName()).user(username).action("login")
                        .category(HippoSecurityEventConstants.CATEGORY_SECURITY)
                        .message(username + " logged in");

                AuditLogger.logHippoEvent(event);
            } else {
                handleLoginFailure(userSession);
            }

            userSession.setLocale(new Locale(selectedLocale));
            if (rememberme && success) {
                throw new RestartResponseException(PluginPage.class);
            } else {
                redirect(success, loginExceptionPageParameters);
            }
        }
    }

    // TO be deleted when we upgrade to a container compliant with Servlet API(s) v3.0
    // This was added cause the setHttpOnly/isHttpOnly at the time of developing this code were not available
    // cause we used to use Servlet API(s) v2.5
    private void addCookieWithHttpOnly(Cookie cookie, WebResponse response, boolean useHttpOnly) {
        if (useHttpOnly) {
            final StringBuffer setCookieHeaderBuffer = new StringBuffer();
            ServerCookie.appendCookieValue(setCookieHeaderBuffer, cookie.getVersion(), cookie.getName(),
                    cookie.getValue(), cookie.getPath(), cookie.getDomain(), cookie.getComment(), cookie.getMaxAge(),
                    cookie.getSecure(), useHttpOnly);

            response.getHttpServletResponse().addHeader("Set-Cookie", setCookieHeaderBuffer.toString());
        } else {
            response.addCookie(cookie);
        }
    }

    private Node getUserInfo(final Session session) throws RepositoryException {
        final String userId = sanitize(session.getUserID());
        StringBuilder statement = new StringBuilder();

        statement.append("//element");
        statement.append("(*, ").append(HippoNodeType.NT_USER).append(")");
        statement.append('[').append("fn:name() = ").append("'").append(NodeNameCodec.encode(userId, true)).append("'").append(']');

        try {
            Query q = session.getWorkspace().getQueryManager().createQuery(statement.toString(), Query.XPATH);
            QueryResult result = q.execute();
            NodeIterator nodesIterator = result.getNodes();

            if (nodesIterator.hasNext()) {
                return nodesIterator.nextNode();
            }
        } catch (RepositoryException rex) {
            log.info("Could not retrieve information of user: '{}'", userId);

            if (log.isDebugEnabled()) {
                log.debug("Error happened while retrieving information of user: '" + userId + "'", rex);
            }

            throw rex;
        }

        return null;
    }

    private String sanitize(final String userId) {
        return userId.trim();
    }

    private void handleLoginFailure(PluginUserSession userSession) {
        // Clear the Hippo Auto Login cookie
        WebApplicationHelper.clearCookie(HIPPO_AUTO_LOGIN_COOKIE_NAME);

        HippoEvent event = new HippoEvent(userSession.getApplicationName()).user(username).action("login")
                .category(HippoSecurityEventConstants.CATEGORY_SECURITY)
                .result("failure")
                .message(username + " failed to login");

        AuditLogger.logHippoEvent(event);

        // Get an anonymous readonly session
        userSession.login();
    }

}
