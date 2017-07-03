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

import hudson.plugins.git.GitChangeLogParser;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import java.io.InputStream;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class GiteaBrowserTest {

    private static final String GITEA_URL = "http://gitea.test";
    
    private final GiteaBrowser instance = new GiteaBrowser(GITEA_URL);

    @Test
    public void given__instance__when__getUrl__then__urlEndsWithSlash() throws Exception {
        assertThat(instance.getUrl().toString(),
                is(GITEA_URL + "/"));
    }

    @Test
    public void given__instance__when__nonNormalizedUrl__then__urlNormalized() throws Exception {
        assertThat(new GiteaBrowser("http://gitea.test:80/foo/../bar/..").getUrl().toString(),
                is(GITEA_URL + "/"));
    }

    @Test
    public void given__instance__when__repoUrl_ends_with_slash__then__no_double_slash() throws Exception {
        assertThat(new GiteaBrowser(GITEA_URL + "/").getUrl().toString(),
                is(GITEA_URL + "/"));
    }

    @Test
    public void given__changeset__when__getChangeSetLink__then__url_generater() throws Exception {
        assertThat(instance.getChangeSetLink(loadChangeSet("rawchangelog")).toString(),
                is(GITEA_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d"));
    }

    @Test
    public void given__changeset__when__getDiffLink_first_file__then__fragment_1() throws Exception {
        assertThat(instance.getDiffLink(
                findPath(loadChangeSet("rawchangelog"), "src/main/java/hudson/plugins/git/browser/GithubWeb.java")
        ).toString(), is(GITEA_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-1"));
    }

    @Test
    public void given__changeset__when__getDiffLink_second_file__then__fragment_2() throws Exception {
        assertThat(instance.getDiffLink(
                findPath(loadChangeSet("rawchangelog"), "src/test/java/hudson/plugins/git/browser/GithubWebTest.java")
        ).toString(), is(GITEA_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-2"));
    }

    @Test
    public void given__changeset__when__getDiffLink_new_file__then__no_diff_link() throws Exception {
        assertThat(instance.getDiffLink(
                findPath(
                        loadChangeSet("rawchangelog"),
                        "src/test/resources/hudson/plugins/git/browser/rawchangelog-with-deleted-file"
                )
        ), is(nullValue()));
    }

    @Test
    public void given__path__when__getFileLink_existing_file__then__file_link_points_to_file() throws Exception {
        assertThat(instance.getFileLink(
                findPath(
                        loadChangeSet("rawchangelog"),
                        "src/main/java/hudson/plugins/git/browser/GithubWeb.java"
                )
        ).toString(), is(GITEA_URL
                + "/src/396fc230a3db05c427737aa5c2eb7856ba72b05d"
                + "/src/main/java/hudson/plugins/git/browser/GithubWeb.java"
        ));
    }

    @Test
    public void given__path__when__getFileLink_deleted_file__then__file_link_points_to_diff() throws Exception {
        assertThat(instance.getFileLink(
                findPath(loadChangeSet("rawchangelog-with-deleted-file"), "bar")
        ).toString(), is(GITEA_URL + "/commit/fc029da233f161c65eb06d0f1ed4f36ae81d1f4f#diff-1"));
    }

    private GitChangeSet loadChangeSet(String resourceName) throws Exception {
        try (InputStream is = GiteaBrowserTest.class.getResourceAsStream(resourceName)) {
            return new GitChangeLogParser(false).parse(is).get(0);
        }
    }

    private Path findPath(GitChangeSet changeSet, String path) throws Exception {
        for (final Path p : changeSet.getPaths()) {
            if (path.equals(p.getPath())) {
                return p;
            }
        }
        return null;
    }

}
