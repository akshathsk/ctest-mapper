package org.uiuc;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Process p = null;

        try {
            p = Runtime.getRuntime().exec("mvn test -Dtest=org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoaderTestCase#testLoadConfig -DfailIfNoTests=false");
        } catch (IOException e) {
            System.err.println("Error on exec() method");
            e.printStackTrace();
        }
        OutputStream output = new OutputStream() {
            private StringBuilder string = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b);
            }

            public String toString() {
                return this.string.toString();
            }
        };

        copy(p.getInputStream(), output);
        BufferedReader bufReader = new BufferedReader(new StringReader(output.toString()));
        String prev = bufReader.readLine();
        String next = bufReader.readLine();
        while (next != null) {
            if (prev.contains("[CTEST][getModuleConfiguration]") && next.contains("[CTEST][getProviderConfiguration]")) {
                System.out.println(prev);
                System.out.println(next);
                break;
            }
            prev = next;
            next = bufReader.readLine();
        }
        p.waitFor();

    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1)
                break;
            out.write((char) c);
        }
    }
}