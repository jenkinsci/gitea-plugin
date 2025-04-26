package org.jenkinsci.plugin.gitea.credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.credentialsbinding.Binding;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.impl.StringBinding;
import org.kohsuke.stapler.DataBoundConstructor;

public class PersonalAccessTokenBinding extends StringBinding {

    @DataBoundConstructor
    public PersonalAccessTokenBinding(String variable, String credentialsId) {
        super(variable, credentialsId);
    }

    @Override
    public Binding.SingleEnvironment bindSingle(@NonNull Run<?,?> build,
                                                @Nullable FilePath workspace,
                                                @Nullable Launcher launcher,
                                                @NonNull TaskListener listener)
        throws IOException {
        return new SingleEnvironment(getCredentials(build).getSecret().getPlainText());
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
