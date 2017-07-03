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

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.scm.api.SCMEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaEvent;
import org.jvnet.tiger_types.Types;

public abstract class GiteaWebhookHandler<E extends SCMEvent<P>, P extends GiteaEvent> implements ExtensionPoint {

    private final String eventName;
    private final Class<E> eventClass;
    private final Class<P> payloadClass;
    private final ObjectMapper mapper = new ObjectMapper();

    protected GiteaWebhookHandler(String eventName, Class<E> eventClass, Class<P> payloadClass) {
        this.eventName = eventName;
        this.eventClass = eventClass;
        this.payloadClass = payloadClass;
    }

    protected GiteaWebhookHandler(String eventName) {
        this.eventName = eventName;
        Type bt = Types.getBaseClass(getClass(), GiteaWebhookHandler.class);
        if (bt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) bt;
            // this 'p' is the closest approximation of P of GiteadSCMEventHandler
            Class e = Types.erasure(pt.getActualTypeArguments()[0]);
            if (!SCMEvent.class.isAssignableFrom(e)) {
                throw new AssertionError(
                        "Could not determine the " + SCMEvent.class + " event class generic parameter of "
                                + getClass() + " best guess was " + e);
            }
            Class p = Types.erasure(pt.getActualTypeArguments()[1]);
            if (!GiteaEvent.class.isAssignableFrom(p)) {
                throw new AssertionError(
                        "Could not determine the " + GiteaEvent.class + " payload class generic parameter of "
                                + getClass() + " best guess was " + p);
            }
            this.eventClass = e;
            this.payloadClass = p;
        } else {
            throw new AssertionError("Type inferrence failure for subclass " + getClass()
                    + " of parameterized type " + GiteaWebhookHandler.class
                    + ". Use the constructor that takes the Class objects explicitly.");
        }
    }

    public GiteaWebhookHandler() {
        Type bt = Types.getBaseClass(getClass(), GiteaWebhookHandler.class);
        if (bt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) bt;
            // this 'p' is the closest approximation of P of GiteadSCMEventHandler
            Class e = Types.erasure(pt.getActualTypeArguments()[0]);
            if (!SCMEvent.class.isAssignableFrom(e)) {
                throw new AssertionError(
                        "Could not determine the " + SCMEvent.class + " event class generic parameter of "
                                + getClass() + " best guess was " + e);
            }
            Class p = Types.erasure(pt.getActualTypeArguments()[1]);
            if (!GiteaEvent.class.isAssignableFrom(p)) {
                throw new AssertionError(
                        "Could not determine the " + GiteaEvent.class + " payload class generic parameter of "
                                + getClass() + " best guess was " + p);
            }
            this.eventClass = e;
            this.payloadClass = p;
        } else {
            throw new AssertionError("Type inferrence failure for subclass " + getClass()
                    + " of parameterized type " + GiteaWebhookHandler.class
                    + ". Use the constructor that takes the Class objects explicitly.");
        }
        Matcher eventNameMatcher =
                Pattern.compile("^\\QGitea\\E([A-Z][a-z_]*)\\QSCM\\E(Head|Source|Navigator)?\\QEvent\\E$")
                        .matcher(eventClass.getSimpleName());
        if (eventNameMatcher.matches()) {
            this.eventName = eventNameMatcher.group(1).toLowerCase(Locale.ENGLISH);
        } else {
            throw new AssertionError("Could not infer event name from " + eventClass
                    + " as it does not follow the convention Gitea[Name]SCMEvent. Use the constructor that specifies "
                    + "the event name explicitly");
        }
    }

    public final boolean matches(String eventName) {
        return this.eventName.equals(eventName);
    }

    public final void process(InputStream inputStream, String origin) throws IOException {
        process(createEvent(payloadClass.cast(mapper.readerFor(payloadClass).readValue(inputStream)), origin));
    }

    protected abstract E createEvent(P payload, String origin);

    protected abstract void process(E event);

    public final String getEventName() {
        return eventName;
    }

    public final Class<E> getEventClass() {
        return eventClass;
    }

    public final Class<P> getPayloadClass() {
        return payloadClass;
    }

    @Override
    public String toString() {
        return "GiteaWebhookHandler{" +
                "eventName='" + eventName + '\'' +
                ", eventClass=" + eventClass +
                ", payloadClass=" + payloadClass +
                '}';
    }
}
