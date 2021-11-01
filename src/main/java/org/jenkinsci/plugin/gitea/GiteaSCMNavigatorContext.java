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
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;

public class GiteaSCMNavigatorContext extends SCMNavigatorContext<GiteaSCMNavigatorContext, GiteaSCMNavigatorRequest> {

     /**
     * If true, archived repositories will be ignored.
     */
    private boolean excludeArchivedRepositories;

    
    /**
     * @return True if archived repositories should be ignored, false if they should be included.
     */
    public boolean isExcludeArchivedRepositories() {
        return excludeArchivedRepositories;
    }

    /**
     * @param excludeArchivedRepositories Set true to exclude archived repositories
     */
    public void setExcludeArchivedRepositories(boolean excludeArchivedRepositories) {
        this.excludeArchivedRepositories = excludeArchivedRepositories;
    }
    
    @NonNull
    @Override
    public GiteaSCMNavigatorRequest newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer) {
        return new GiteaSCMNavigatorRequest(navigator, this, observer);
    }
}
