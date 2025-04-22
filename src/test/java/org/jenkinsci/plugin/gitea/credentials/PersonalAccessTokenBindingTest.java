package org.jenkinsci.plugin.gitea.credentials;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;

import hudson.model.Run;

@WithJenkins
class PersonalAccessTokenBindingTest {

    private static final String API_TOKEN = "secret";
    private static final String API_TOKEN_ID = "personalAccessTokenId";

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) throws Exception {
        jenkins = rule;
        for (CredentialsStore credentialsStore : CredentialsProvider.lookupStores(jenkins.jenkins)) {
            if (credentialsStore instanceof SystemCredentialsProvider.StoreImpl) {
                List<Domain> domains = credentialsStore.getDomains();
                credentialsStore.addCredentials(
                        domains.get(0),
                        new PersonalAccessTokenImpl(
                                CredentialsScope.GLOBAL,
                                API_TOKEN_ID,
                                "Gitea Personal Access Token",
                                API_TOKEN));
            }
        }
    }

    @Test
    void withCredentials_success() throws Exception {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        String pipelineText = IOUtils.toString(
                getClass().getResourceAsStream("pipeline/withCredentials-pipeline.groovy"), StandardCharsets.UTF_8);
        project.setDefinition(new CpsFlowDefinition(pipelineText, false));
        Run<?, ?> build = jenkins.buildAndAssertSuccess(project);
        // assert false to know we run it in tests
        jenkins.assertLogContains("Token1 is ecret", build);
        jenkins.assertLogContains("Token2 is ecret", build);
    }
}
