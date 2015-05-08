package io.grpc.android.integrationtest;

import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by simonma on 5/7/15.
 */
public class TesterActivityTest extends ActivityInstrumentationTestCase2<TesterActivity> {

  String host = "104.155.207.2";
  int port = 8030;
  String result;

  public TesterActivityTest() {
    super(TesterActivity.class);
  }

  public void setUp() throws Exception {
    super.setUp();
    getActivity();
  }

  public void testStartEmptyUnary() throws Exception {
    startTest("empty_unary");
  }

  public void testStartLargeUnary() throws Exception {
    startTest("large_unary");
  }

  public void testStartClientStreaming() throws Exception {

  }

  public void testStartServerStreaming() throws Exception {

  }

  public void testStartPingPong() throws Exception {

  }

  private void startTest(String testCase) throws Exception {
    final CountDownLatch finished = new CountDownLatch(1);

    new GrpcTestTask(testCase, host, port, new GrpcTestTask.TestListener() {
      @Override
      public void onPreTest() {
      }

      @Override
      public void onPostTest(String result) {
        TesterActivityTest.this.result = result;
        finished.countDown();
      }
    }).execute();
    assertTrue("Timeout!", finished.await(10, TimeUnit.SECONDS));
    assertEquals(GrpcTestTask.SUCCESS_MESSAGE, result);
  }
}