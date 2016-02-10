/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.http;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.servlet.AbortWithWebErrorCodeException;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsrfPreventionWebRequestCycle extends WebRequestCycle {

    private static final Logger log = LoggerFactory.getLogger(CsrfPreventionWebRequestCycle.class);

    /**
     * The error code to report when the action to take for a CSRF request is
     */
    private final static int errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

    private final String[] acceptedOrigins;

    /**
     * The error message to report when the action to take for a CSRF request is {@code ERROR}.
     * Default {@code "Origin does not correspond to request"}.
     */
    private String errorMessage = "Origin does not correspond to request";

    public CsrfPreventionWebRequestCycle(final WebApplication application,
                                         final WebRequest request,
                                         final Response response,
                                         final String[] acceptedOrigins) {
        super(application, request, response);
        this.acceptedOrigins = acceptedOrigins;
    }

    @Override
    protected void onBeginRequest() {

        final HttpServletRequest httpServletRequest = getWebRequest().getHttpServletRequest();
        final String origin = httpServletRequest.getHeader("origin");
        if (origin == null || origin.isEmpty()) {
            super.onBeginRequest();
            return;
        }

        if (isWhitelistedOrigin(origin)) {
            log.info("Allow whitelisted origin '{}'", origin);
            return;
        }

        if (!isLocalOrigin(httpServletRequest, origin)) {
            log.info("Possible CSRF attack, request URL: {}, Origin: {}, action: aborted with error {} {}",
                    new Object[] { httpServletRequest.getRequestURL(), origin, errorCode, errorMessage });
            throw new AbortWithWebErrorCodeException(errorCode, errorMessage);
        }
        super.onBeginRequest();
    }

    private boolean isWhitelistedOrigin(final String origin) {
        if (acceptedOrigins == null || acceptedOrigins.length == 0) {
            return false;
        }

        try
        {
            final URI originUri = new URI(origin);
            final String originHost = originUri.getHost();
            if (Strings.isEmpty(originHost))
                return false;
            for (String whitelistedOrigin : acceptedOrigins)
            {
                if (originHost.equalsIgnoreCase(whitelistedOrigin) ||
                        originHost.endsWith("." + whitelistedOrigin))
                {
                    log.trace("Origin {} matched whitelisted origin {}, request accepted", origin,
                            whitelistedOrigin);
                    return true;
                }
            }
        }
        catch (URISyntaxException e)
        {
            log.debug("Origin: {} not parseable as an URI. Whitelisted-origin check skipped.",
                    origin);
        }

        return false;
    }

    /**
     * Checks whether the {@code Origin} HTTP header of the request matches where the request came
     * from.
     *
     * @param containerRequest the current container request
     * @param originHeader     the contents of the {@code Origin} HTTP header
     * @return {@code true} when the origin of the request matches the {@code Origin} HTTP header
     */
    private boolean isLocalOrigin(final HttpServletRequest containerRequest, final String originHeader) {
        // Make comparable strings from Origin and Location
        String origin = getOriginHeaderOrigin(originHeader);
        if (origin == null) {
            return false;
        }

        String request = getLocationHeaderOrigin(containerRequest);
        if (request == null) {
            return false;
        }

        return origin.equalsIgnoreCase(request);
    }

    /**
     * Creates a RFC-6454 comparable origin from the {@code origin} string.
     *
     * @param origin the contents of the Origin HTTP header
     * @return only the scheme://host[:port] part, or {@code null} when the origin string is not
     * compliant
     */
    private String getOriginHeaderOrigin(final String origin) {
        // the request comes from a privacy sensitive context, flag as non-local origin. If
        // alternative action is required, an implementor can override any of the onAborted,
        // onSuppressed or onAllowed and implement such needed action.

        if ("null".equals(origin)) {
            return null;
        }

        StringBuilder target = new StringBuilder();

        try {
            URI originUri = new URI(origin);
            String scheme = originUri.getScheme();
            if (scheme == null) {
                return null;
            } else {
                scheme = scheme.toLowerCase(Locale.ENGLISH);
            }

            target.append(scheme);
            target.append("://");

            String host = originUri.getHost();
            if (host == null) {
                return null;
            }
            target.append(host);

            int port = originUri.getPort();
            boolean portIsSpecified = port != -1;
            boolean isAlternateHttpPort = "http".equals(scheme) && port != 80;
            boolean isAlternateHttpsPort = "https".equals(scheme) && port != 443;

            if (portIsSpecified && (isAlternateHttpPort || isAlternateHttpsPort)) {
                target.append(':');
                target.append(port);
            }
            return target.toString();
        } catch (URISyntaxException e) {
            log.debug("Invalid Origin header provided: {}, marked conflicting", origin);
            return null;
        }
    }

    /**
     * Creates a RFC-6454 comparable origin from the {@code request} requested resource.
     *
     * @param request the incoming request
     * @return only the scheme://host[:port] part, or {@code null} when the origin string is not
     * compliant
     */
    private String getLocationHeaderOrigin(final HttpServletRequest request) {

        String host = request.getHeader("X-Forwarded-Host");
        if (host != null) {
            String[] hosts = host.split(",");
            return getFarthestRequestScheme(request) + "://" + hosts[0];
        }

        host = request.getHeader("Host");
        if (host != null && !"".equals(host)) {
            return getFarthestRequestScheme(request) + "://" + host;
        }

        // Build scheme://host:port from request
        StringBuilder target = new StringBuilder();
        String scheme = request.getScheme();
        if (scheme == null) {
            return null;
        } else {
            scheme = scheme.toLowerCase(Locale.ENGLISH);
        }
        target.append(scheme);
        target.append("://");

        host = request.getServerName();
        if (host == null) {
            return null;
        }
        target.append(host);

        int port = request.getServerPort();
        if ("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443) {
            target.append(':');
            target.append(port);
        }

        return target.toString();
    }

    public static String getFarthestRequestScheme(final HttpServletRequest request) {
        String [] schemes = getCommaSeparatedMultipleHeaderValues(request, "X-Forwarded-Proto");

        if (schemes != null && schemes.length != 0) {
            return schemes[0].toLowerCase();
        }

        schemes = getCommaSeparatedMultipleHeaderValues(request, "X-Forwarded-Scheme");

        if (schemes != null && schemes.length != 0) {
            return schemes[0].toLowerCase();
        }

        String [] sslEnabledArray = getCommaSeparatedMultipleHeaderValues(request, "X-SSL-Enabled");

        if (sslEnabledArray == null) {
            sslEnabledArray = getCommaSeparatedMultipleHeaderValues(request, "Front-End-Https");
        }

        if (sslEnabledArray != null && sslEnabledArray.length != 0) {
            String sslEnabled = sslEnabledArray[0];

            if (sslEnabled.equalsIgnoreCase("on") || sslEnabled.equalsIgnoreCase("yes") || sslEnabled.equals("1")) {
                return "https";
            }
        }

        return request.getScheme();
    }


    /**
     * Parse comma separated multiple header value and return an array if the header exists.
     * If the header doesn't exist, it returns null.
     * @param request
     * @param headerName
     * @return null if the header doesn't exist or an array parsed from the comma separated string header value.
     */
    private static String [] getCommaSeparatedMultipleHeaderValues(final HttpServletRequest request, final String headerName) {
        String value = request.getHeader(headerName);

        if (value == null) {
            return null;
        }

        String [] tokens = value.split(",");

        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }

        return tokens;
    }

}
