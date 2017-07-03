/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugin.gitea;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.HttpResponses;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.scm.api.SCMEvent;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GiteaWebhookAction extends CrumbExclusion implements UnprotectedRootAction {
    private Logger LOGGER = Logger.getLogger(GiteaWebhookAction.class.getName());

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "gitea-webhook";
    }

    @Override
    public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/" + getUrlName() + "/post")) {
            chain.doFilter(req, resp);
            return true;
        }
        return false;
    }

    public HttpResponse doPost(StaplerRequest request) throws IOException {
        if (!request.getMethod().equals("POST")) {
            return HttpResponses
                    .error(HttpServletResponse.SC_BAD_REQUEST,
                            "Only POST requests are supported, this was a " + request.getMethod() + " request");
        }
        if (!"application/json".equals(request.getContentType())) {
            return HttpResponses
                    .error(HttpServletResponse.SC_BAD_REQUEST,
                            "Only application/json content is supported, this was " + request.getContentType());
        }
        String type = request.getHeader("X-Gitea-Event");
        if (StringUtils.isBlank(type)) {
            return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST,
                    "Expecting a Gitea event, missing expected X-Gitea-Event header");
        }
        String origin = SCMEvent.originOf(request);
        boolean processed = false;
        for (GiteaWebhookHandler<?, ?> h : ExtensionList.lookup(GiteaWebhookHandler.class)) {
            if (h.matches(type)) {
                h.process(request.getInputStream(), origin);
                processed = true;
            }
        }
        return HttpResponses.plainText(processed ? "Processed" : "Ignored");
    }
}
