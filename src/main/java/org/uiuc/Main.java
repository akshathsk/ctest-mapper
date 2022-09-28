package org.uiuc;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final List<String> PUBLISH_GOALS = Arrays.asList("clean", "site-deploy");
    private final Invoker invoker;

    public Main() {
        this.invoker = new DefaultInvoker();
    }

    public Main(File localRepositoryDir) {
        Invoker newInvoker = new DefaultInvoker();
        newInvoker.setLocalRepositoryDirectory(localRepositoryDir);

        this.invoker = newInvoker;
    }

    public void publishSite(String siteDirectory) throws Exception {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(siteDirectory));
        request.setGoals(PUBLISH_GOALS);
        InvocationResult result = invoker.execute(request);
        System.out.println(result);
        if (result.getExitCode() != 0) {
            if (result.getExecutionException() != null) {
                throw new Exception("Failed to publish site." +
                        result.getExecutionException());
            } else {
                throw new Exception("Failed to publish site. Exit code: " +
                        result.getExitCode());
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.publishSite("/home/sk117/final-project/apache-skywalking-apm-9.2.0/oap-server/server-starter/pom.xml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello world!");
    }
}