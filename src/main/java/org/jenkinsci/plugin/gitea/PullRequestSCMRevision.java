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
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;
import org.kohsuke.stapler.export.Exported;

public class PullRequestSCMRevision extends ChangeRequestSCMRevision<PullRequestSCMHead> {

    private final BranchSCMRevision origin;

    /**
     * Constructor.
     *
     * @param head   the {@link PullRequestSCMHead} that the {@link SCMRevision} belongs to.
     * @param target the {@link BranchSCMRevision} of the {@link PullRequestSCMHead#getTarget()}.
     * @param origin the {@link BranchSCMRevision} of the {@link PullRequestSCMHead#getOrigin()} head.
     */
    protected PullRequestSCMRevision(
            @NonNull PullRequestSCMHead head,
            @NonNull BranchSCMRevision target,
            @NonNull BranchSCMRevision origin) {
        super(head, target);
        this.origin = origin;
    }

    @Exported
    @NonNull
    public final BranchSCMRevision getOrigin() {
        return origin;
    }


    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> revision) {
        return (revision instanceof PullRequestSCMRevision)
                && origin.equals(((PullRequestSCMRevision) revision).getOrigin());
    }

    @Override
    protected int _hashCode() {
        return origin.hashCode();
    }

    @Override
    public String toString() {
        return (isMerge() ? ((BranchSCMRevision) getTarget()).getHash() + "+" : "") + origin.getHash();
    }
}
