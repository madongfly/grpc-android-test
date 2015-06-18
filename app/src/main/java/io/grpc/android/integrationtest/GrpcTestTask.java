package io.grpc.android.integrationtest;

import android.os.AsyncTask;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An AsyncTask for executing gRPC integration test.
 */
public class GrpcTestTask extends AsyncTask<Void, Void, String> {
  public final static String SUCCESS_MESSAGE = "Succeed!!!";

  private IntegrationTester tester;
  private final String testCase;
  private final String host;
  private final int port;
  private final TestListener listener;
  private final String serverHostOverride;
  private final boolean useTls;
  private final boolean useTestCa;
  private final String oauthScope;

  public GrpcTestTask(String testCase,
                      String host,
                      int port,
                      @Nullable String serverHostOverride,
                      boolean useTls,
                      boolean useTestCa,
                      String oauthScope,
                      TestListener listener) {
    this.testCase = testCase;
    this.host = host;
    this.port = port;
    this.serverHostOverride = serverHostOverride;
    this.useTls = useTls;
    this.useTestCa = useTestCa;
    this.oauthScope = oauthScope;
    this.listener = listener;
  }

  @Override
  protected void onPreExecute() {
    listener.onPreTest();
  }

  @Override
  protected String doInBackground(Void... nothing) {
    try {
      tester = new IntegrationTester(host, port, serverHostOverride, useTls, useTestCa, oauthScope);
      tester.runTest(testCase);
      return SUCCESS_MESSAGE;
    } catch (Exception | AssertionError e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace();
      e.printStackTrace(new PrintWriter(sw));
      return "Failed... : " + e.getMessage() + "\n" + sw.toString();
    } finally {
      tester.shutdown();
    }
  }

  @Override
  protected void onPostExecute(String result) {
    listener.onPostTest(result);
  }

  public interface TestListener {
    void onPreTest();

    void onPostTest(String result);
  }
}
