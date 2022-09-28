package org.uiuc;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final List<String> PUBLISH_GOALS = Arrays.asList("test");
    private final Invoker invoker;

    public Main() {
        this.invoker = new DefaultInvoker();
    }

    public void publishSite(String siteDirectory) throws Exception {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(siteDirectory));
        request.setGoals(PUBLISH_GOALS);
        request.addArg("-Dtest=org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig");
        request.addArg("  | tee ../../../test4.log");
        InvocationResult result = invoker.execute(request);
        System.out.println(result.getExitCode());
        System.out.println(result.toString());
        System.out.println(result.getExecutionException());
        System.out.println(invoker.getLogger().toString());
        System.out.println(invoker.getLocalRepositoryDirectory());
        System.out.println(invoker.getMavenExecutable().getAbsolutePath());
        System.out.println(invoker.getMavenExecutable().toString());
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
//        try {
//            Process process = Runtime.getRuntime().exec("mvn test -Dtest=org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig -DfailIfNoTests=false");
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
//
//            System.out.println(outputStreamWriter);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        System.out.println("Hello world!");
    }
}