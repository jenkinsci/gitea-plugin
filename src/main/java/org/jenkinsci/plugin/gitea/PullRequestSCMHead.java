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

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;

public class PullRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {
    private final long id;
    private final BranchSCMHead target;
    private final ChangeRequestCheckoutStrategy strategy;
    private final String originName;
    private final String originOwner;
    private final String originRepository;
    private final SCMHeadOrigin origin;

    /**
     * Constructor.
     *
     * @param id               the pull request id.
     * @param name             the name of the head.
     * @param target           the target of this pull request.
     * @param strategy         the checkout strategy
     * @param origin           the origin of the pull request
     * @param originOwner      the name of the owner of the origin repository
     * @param originRepository the name of the origin repository
     * @param originName       the name of the branch in the origin repository
     */
    public PullRequestSCMHead(@NonNull String name, long id, BranchSCMHead target,
                              ChangeRequestCheckoutStrategy strategy, SCMHeadOrigin origin, String originOwner,
                              String originRepository, String originName) {
        super(name);
        this.id = id;
        this.target = target;
        this.strategy = strategy;
        this.origin = origin;
        this.originOwner = originOwner;
        this.originRepository = originRepository;
        this.originName = originName;
    }

    @NonNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return strategy;
    }

    @NonNull
    @Override
    public String getOriginName() {
        return originName;
    }

    @NonNull
    @Override
    public String getId() {
        return Long.toString(id);
    }

    @NonNull
    @Override
    public BranchSCMHead getTarget() {
        return target;
    }

    @NonNull
    @Override
    public SCMHeadOrigin getOrigin() {
        return origin;
    }

    public String getOriginOwner() {
        return originOwner;
    }

    public String getOriginRepository() {
        return originRepository;
    }
}
