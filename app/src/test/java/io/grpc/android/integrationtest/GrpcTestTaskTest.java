package io.grpc.android.integrationtest;

import android.test.suitebuilder.annotation.LargeTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;

/**
 * Tests for GrpcTestTask.
 */
public class GrpcTestTaskTest extends TestCase {
  private String host = "localhost";
  private int port = 10008;
  private String result;

  @LargeTest
  public void testEmptyUnary() throws Exception {
    startTest("empty_unary");
  }

  @LargeTest
  public void testLargeUnary() throws Exception {
    startTest("large_unary");
  }

  @LargeTest
  public void testClientStreaming() throws Exception {
    // TODO(madongfly)
  }

  @LargeTest
  public void testServerStreaming() throws Exception {
    // TODO(madongfly)
  }

  @LargeTest
  public void testPingPong() throws Exception {
    // TODO(madongfly)
  }

  private void startTest(String testCase) throws Exception {
    final CountDownLatch finished = new CountDownLatch(1);

    new GrpcTestTask(testCase, host, port, new GrpcTestTask.TestListener() {
      @Override
      public void onPreTest() {
      }

      @Override
      public void onPostTest(String result) {
        GrpcTestTaskTest.this.result = result;
        finished.countDown();
      }
    }).execute();
    assertTrue("Timeout!", finished.await(10, TimeUnit.SECONDS));
    assertEquals(GrpcTestTask.SUCCESS_MESSAGE, result);
  }
}