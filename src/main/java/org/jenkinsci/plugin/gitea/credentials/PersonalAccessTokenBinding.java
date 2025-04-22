package org.jenkinsci.plugin.gitea.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.kohsuke.stapler.DataBoundConstructor;

public class PersonalAccessTokenBinding extends MultiBinding<PersonalAccessToken> {

    // Environment variable name to be used in the binding
    private final String variable;

    @DataBoundConstructor
    public PersonalAccessTokenBinding(String credentialsId, String variable) {
        super(credentialsId);
        this.variable = variable;
    }

    @Override
    protected Class<PersonalAccessToken> type() {
        return PersonalAccessToken.class;
    }

    @Override
    public Set<String> variables() {
        // Return a set containing the environment variable name
        return Collections.singleton(variable);
    }

    @Override
    public MultiEnvironment bind(
            @NonNull Run<?, ?> build,
            @Nullable FilePath workspace,
            @Nullable Launcher launcher,
            @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        // Retrieve the PersonalAccessToken credentials
        PersonalAccessToken credentials = getCredentials(build);

        Map<String, String> values = new LinkedHashMap<>();
        values.put(variable, Secret.toString(credentials.getToken()));
        return new MultiEnvironment(values);
    }

    @Symbol("giteaPersonalAccessToken") // Symbol annotation for use in Jenkins UI
    @Extension
    public static class DescriptorImpl extends BindingDescriptor<PersonalAccessTokenImpl> {

        @Override
        protected Class<PersonalAccessTokenImpl> type() {
            return PersonalAccessTokenImpl.class;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.PersonalAccessTokenImpl_displayName(); // Localized display name
        }

        @Override
        public boolean requiresWorkspace() {
            return false; // This binding does not require a workspace
        }
    }
}
