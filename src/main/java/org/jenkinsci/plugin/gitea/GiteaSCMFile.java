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
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import jenkins.scm.api.SCMFile;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;

class GiteaSCMFile extends SCMFile {

    private final GiteaConnection connection;
    private final GiteaRepository repo;
    private final String ref;
    private Boolean isFile;

    GiteaSCMFile(GiteaConnection connection, GiteaRepository repo, String ref) {
        super();
        this.connection = connection;
        type(Type.DIRECTORY);
        this.repo = repo;
        this.ref = ref;
    }

    private GiteaSCMFile(@NonNull GiteaSCMFile parent, String name, Boolean isFile) {
        super(parent, name);
        this.connection = parent.connection;
        this.repo = parent.repo;
        this.ref = parent.ref;
        this.isFile = isFile;
    }

    @NonNull
    @Override
    protected SCMFile newChild(String name, boolean assumeIsDirectory) {
        return new GiteaSCMFile(this, name, assumeIsDirectory ? Boolean.FALSE : null);
    }

    @NonNull
    @Override
    public Iterable<SCMFile> children() throws IOException {
        // TODO once https://github.com/go-gitea/gitea/issues/1978
        return Collections.emptyList();
    }

    @Override
    public long lastModified() throws IOException, InterruptedException {
        // TODO once https://github.com/go-gitea/gitea/issues/1978
        return 0L;
    }

    @NonNull
    @Override
    protected Type type() throws IOException, InterruptedException {
        // TODO once https://github.com/go-gitea/gitea/issues/1978
        if (isFile == null) {
            isFile = connection.checkFile(repo, ref, getPath());
        }
        return isFile ? Type.REGULAR_FILE : Type.NONEXISTENT;
    }

    @NonNull
    @Override
    public InputStream content() throws IOException, InterruptedException {
        if (isFile != null && !isFile) {
            throw new FileNotFoundException(getPath());
        }
        try {
            byte[] content = connection.fetchFile(repo, ref, getPath());
            isFile = true;
            return new ByteArrayInputStream(content);
        } catch (FileNotFoundException e) {
            isFile = false;
            throw e;
        }
    }
}
