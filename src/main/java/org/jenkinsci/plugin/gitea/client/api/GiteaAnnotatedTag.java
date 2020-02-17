/*
 * The MIT License
 *
 * Copyright (c) 2017-2020, CloudBees, Inc.
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

package org.jenkinsci.plugin.gitea.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

/**
 * Represents an annotated tag.
 */
@JsonIgnoreProperties(
        value = {"added", "removed", "modified", "verification"},
        ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES
)
public final class GiteaAnnotatedTag extends GiteaObject<GiteaAnnotatedTag> {
    /**
     * The SHA1 of the tag object.
     */
    private String sha;
    private String tag;
    private String message;
    private String url;
    private Tagger tagger;
    private TaggedObject object;
    private Verification verification;

    public GiteaAnnotatedTag() {
    }

    public GiteaAnnotatedTag(String sha, String tag, String message, String url,
                             Tagger tagger, TaggedObject object,
                             Verification verification) {
        this.sha = sha;
        this.tag = tag;
        this.message = message;
        this.url = url;
        this.tagger = tagger;
        this.object = object;
        this.verification = verification;
    }

    /**
     * Gets the SHA1 of the tag object.
     *
     * @return the SHA1 of the tag object.
     */
    public String getSha() {
        return sha;
    }

    /**
     * Sets the SHA1 of the commit.
     *
     * @param sha the SHA1 of the commit.
     */
    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Tagger getTagger() {
        return tagger == null ? null : tagger.clone();
    }

    public void setTagger(Tagger tagger) {
        this.tagger = tagger == null ? null : tagger.clone();
    }

    public TaggedObject getObject() {
        return object == null ? null : object.clone();
    }

    public void setObject(TaggedObject object) {
        this.object = object == null ? null : object.clone();
    }

    public Verification getVerification() {
        return verification == null ? null : verification.clone();
    }

    public void setVerification(Verification verification) {
        this.verification = verification == null ? null : verification.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaAnnotatedTag{"
                + "sha='" + sha + '\''
                + ", tag='" + tag + '\''
                + ", message='" + message + '\''
                + ", url='" + url + '\''
                + ", tagger=" + tagger
                + ", object=" + object
                + ", verification=" + verification
                + '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class Tagger extends GiteaObject<Tagger> implements Cloneable {
        private String name;
        private String email;
        private Date date;

        public Tagger() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Date getDate() {
            return date == null ? null : (Date) date.clone();
        }

        public void setDate(Date date) {
            this.date = date == null ? null : (Date) date.clone();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Tagger{"
                    + "name='" + name + '\''
                    + ", email='" + email + '\''
                    + ", date='" + date + '\''
                    + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class TaggedObject extends GiteaObject<TaggedObject> implements Cloneable {
        private String type;
        private String url;
        private String sha;

        public TaggedObject() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "TaggedObject{"
                    + "type='" + type + '\''
                    + ", url='" + url + '\''
                    + ", sha='" + sha + '\''
                    + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class Verification extends GiteaObject<Verification> implements Cloneable {
        private String payload;
        private String reason;
        private String signature;
        private GiteaCommit.Actor signer;
        private boolean verified;

        public Verification() {
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public GiteaCommit.Actor getSigner() {
            return signer == null ? null : signer.clone();
        }

        public void setSigner(GiteaCommit.Actor signer) {
            this.signer = signer == null ? null : signer.clone();
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        @Override
        public String toString() {
            return "Verification{"
                    + "payload='" + payload + '\''
                    + ", reason='" + reason + '\''
                    + ", signature='" + signature + '\''
                    + ", signer=" + signer
                    + ", verified=" + verified
                    + '}';
        }
    }
}
