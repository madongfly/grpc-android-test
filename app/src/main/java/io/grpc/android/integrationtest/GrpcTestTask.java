package io.grpc.android.integrationtest;

import android.os.AsyncTask;
import java.io.InputStream;
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
  private final InputStream testCa;

  public GrpcTestTask(String testCase,
      String host, int port, InputStream testCa, TestListener listener) {
    this.testCase = testCase;
    this.host = host;
    this.port = port;
    this.listener = listener;
    this.testCa = testCa;
  }

  @Override
  protected void onPreExecute() {
    listener.onPreTest();
  }

  @Override
  protected String doInBackground(Void... nothing) {
    try {
      tester = new IntegrationTester();
      tester.init(host, port, testCa);
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
