package io.grpc.android.integrationtest;

import com.google.protobuf.nano.MessageNano;
import io.grpc.ChannelImpl;
import io.grpc.stub.StreamRecorder;
import io.grpc.transport.okhttp.OkHttpChannelBuilder;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;

public final class IntegrationTester {

  private ChannelImpl channel;
  private TestServiceGrpc.TestServiceBlockingStub blockingStub;
  protected TestServiceGrpc.TestService asyncStub;

  public void init(String host, int port) {
    channel = OkHttpChannelBuilder.forAddress(host, port).build();
    blockingStub = TestServiceGrpc.newBlockingStub(channel);
    asyncStub = TestServiceGrpc.newStub(channel);
  }

  public void shutdown() {
    if (channel != null) {
      channel.shutdown();
    }
  }

  public void runTest(String testCase) throws Exception {
    if ("empty_unary".equals(testCase)) {
      emptyUnary();
    } else if ("large_unary".equals(testCase)) {
      largeUnary();
    } else if ("client_streaming".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else if ("server_streaming".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else if ("ping_pong".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else if ("empty_stream".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else if ("cancel_after_begin".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else if ("cancel_after_first_response".equals(testCase)) {
      throw new UnsupportedOperationException();
    } else {
      throw new IllegalArgumentException("Unknown test case: " + testCase);
    }
  }

  public void emptyUnary() {
    assertEquals(new EmptyProtos.Empty(), blockingStub.emptyCall(new EmptyProtos.Empty()));
  }

  public void largeUnary() {
    final Messages.SimpleRequest request = new Messages.SimpleRequest();
    request.responseSize = 314159;
    request.responseType = Messages.COMPRESSABLE;
    request.payload = new Messages.Payload();
    request.payload.body = new byte[271828];

    final Messages.SimpleResponse goldenResponse = new Messages.SimpleResponse();
    goldenResponse.payload = new Messages.Payload();
    goldenResponse.payload.body = new byte[314159];

    assertEquals(goldenResponse, blockingStub.unaryCall(request));
  }

  //public void serverStreaming() throws Exception {
  //  final Messages.StreamingOutputCallRequest request = new Messages.StreamingOutputCallRequest();
  //  request.responseType = Messages.COMPRESSABLE;
  //  request.responseParameters = new Messages.ResponseParameters[4];
  //  for (int i = 0; i < 4; i++) {
  //    request.responseParameters[i] = new Messages.ResponseParameters();
  //  }
  //  request.responseParameters[0].size = 31415;
  //  request.responseParameters[0].size = 9;
  //  request.responseParameters[0].size = 2653;
  //  request.responseParameters[0].size = 58979;
  //
  //  final Messages.StreamingOutputCallResponse[] goldenResponses =
  //      new Messages.StreamingOutputCallResponse[4];
  //  for (int i = 0; i < 4; i++) {
  //    goldenResponses[i] = new Messages.StreamingOutputCallResponse();
  //    goldenResponses[i].payload = new Messages.Payload();
  //    goldenResponses[i].payload.type = Messages.COMPRESSABLE;
  //  }
  //  goldenResponses[0].payload.body = new byte[31415];
  //  goldenResponses[1].payload.body = new byte[9];
  //  goldenResponses[2].payload.body = new byte[2653];
  //  goldenResponses[3].payload.body = new byte[58979];
  //
  //  StreamRecorder<Messages.StreamingOutputCallResponse> recorder = StreamRecorder.create();
  //  asyncStub.streamingOutputCall(request, recorder);
  //  recorder.awaitCompletion();
  //  assertSuccess(recorder);
  //  assertEquals(Arrays.asList(goldenResponses), recorder.getValues());
  //}

  public static void assertEquals(MessageNano expected, MessageNano actual) {
    if (expected == null || actual == null) {
      Assert.assertEquals(expected, actual);
    } else {
      if (!expected.equals(actual)) {
        // This assertEquals should always complete.
        Assert.assertEquals(expected.toString(), actual.toString());
        // But if it doesn't, then this should.
        Assert.assertEquals(expected, actual);
        Assert.fail("Messages not equal, but assertEquals didn't throw");
      }
    }
  }

  //private static void assertSuccess(StreamRecorder<?> recorder) {
  //  if (recorder.getError() != null) {
  //    throw new AssertionError(recorder.getError());
  //  }
  //}
  //
  //public static void assertEquals(List<? extends MessageNano> expected,
  //    List<? extends MessageNano> actual) {
  //  if (expected == null || actual == null) {
  //    Assert.assertEquals(expected, actual);
  //  } else if (expected.size() != actual.size()) {
  //    Assert.assertEquals(expected, actual);
  //  } else {
  //    for (int i = 0; i < expected.size(); i++) {
  //      assertEquals(expected.get(i), actual.get(i));
  //    }
  //  }
  //}
}
