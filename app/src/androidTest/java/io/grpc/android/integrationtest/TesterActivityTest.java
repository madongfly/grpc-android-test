package io.grpc.android.integrationtest;

import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestRunner;
import junit.framework.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by simonma on 5/7/15.
 */
public class TesterActivityTest extends ActivityInstrumentationTestCase2<TesterActivity> {

  private String host;
  private int port = 8030;
  private String result;
  private String serverHostOverride;
  private String testCase;
  private boolean useTls;
  private boolean useTestCa;

  public TesterActivityTest() {
    super(TesterActivity.class);
  }

  public void setUp() throws Exception {
    super.setUp();
    getActivity();
    Bundle args = ((InstrumentationTestRunner) getInstrumentation()).getArguments();
    host = args.getString("server_host", "");
    port = Integer.parseInt(args.getString("server_port", "0"));
    assertTrue("Please specify server_host and server_port.", !host.isEmpty() && port != 0);
    serverHostOverride = args.getString("server_host_override", null);
    testCase = args.getString("test_case", "empty_unary");
    useTls = Boolean.getBoolean(args.getString("use_tls", "true"));
    useTestCa = Boolean.getBoolean(args.getString("use_test_ca", "false"));
  }

  public void testGrpc() throws Exception {
    final CountDownLatch finished = new CountDownLatch(1);

    new GrpcTestTask(testCase, host, port, serverHostOverride, useTls, useTestCa,
        new GrpcTestTask.TestListener() {
      @Override
      public void onPreTest() {
      }

      @Override
      public void onPostTest(String result) {
        TesterActivityTest.this.result = result;
        finished.countDown();
      }
    }).execute();
    assertTrue("Timeout!", finished.await(60, TimeUnit.SECONDS));
    assertEquals(GrpcTestTask.SUCCESS_MESSAGE, result);
  }
}