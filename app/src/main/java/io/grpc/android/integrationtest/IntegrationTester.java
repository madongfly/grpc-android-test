package io.grpc.android.integrationtest;

import android.net.SSLCertificateSocketFactory;
import android.support.annotation.Nullable;
import com.google.protobuf.nano.MessageNano;
import io.grpc.ChannelImpl;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.StreamRecorder;
import io.grpc.transport.okhttp.OkHttpChannelBuilder;
import junit.framework.Assert;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public final class IntegrationTester {

  private ChannelImpl channel;
  private TestServiceGrpc.TestServiceBlockingStub blockingStub;
  protected TestServiceGrpc.TestService asyncStub;

  private static TrustManager[] TMS;

  public IntegrationTester(String host,
      int port,
      @Nullable String serverHostOverride,
      boolean useTls,
      boolean useTestCa,
      @Nullable String tlsHandshake) {
    OkHttpChannelBuilder channelBuilder = OkHttpChannelBuilder.forAddress(host, port);
    if (serverHostOverride != null) {
      // Force the hostname to match the cert the server uses.
      channelBuilder.overrideHostForAuthority(serverHostOverride);
    }
    if (useTls) {
      try {
        SSLSocketFactory factory;
        if (tlsHandshake != null) {
          factory = getSslCertificateSocketFactory(useTestCa, tlsHandshake);
        } else {
          factory = getSslSocketFactory(useTestCa);
        }
        channelBuilder.sslSocketFactory(factory);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    channel = channelBuilder.build();
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
      clientStreaming();
    } else if ("server_streaming".equals(testCase)) {
      serverStreaming();
    } else if ("ping_pong".equals(testCase)) {
      pingPong();
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
    Messages.SimpleResponse response = blockingStub.unaryCall(request);
    assertEquals(goldenResponse, response);
  }

  public void serverStreaming() throws Exception {
    final Messages.StreamingOutputCallRequest request = new Messages.StreamingOutputCallRequest();
    request.responseType = Messages.COMPRESSABLE;
    request.responseParameters = new Messages.ResponseParameters[4];
    for (int i = 0; i < 4; i++) {
      request.responseParameters[i] = new Messages.ResponseParameters();
    }
    request.responseParameters[0].size = 31415;
    request.responseParameters[1].size = 9;
    request.responseParameters[2].size = 2653;
    request.responseParameters[3].size = 58979;

    final Messages.StreamingOutputCallResponse[] goldenResponses =
        new Messages.StreamingOutputCallResponse[4];
    for (int i = 0; i < 4; i++) {
      goldenResponses[i] = new Messages.StreamingOutputCallResponse();
      goldenResponses[i].payload = new Messages.Payload();
      goldenResponses[i].payload.type = Messages.COMPRESSABLE;
    }
    goldenResponses[0].payload.body = new byte[31415];
    goldenResponses[1].payload.body = new byte[9];
    goldenResponses[2].payload.body = new byte[2653];
    goldenResponses[3].payload.body = new byte[58979];

    StreamRecorder<Messages.StreamingOutputCallResponse> recorder = StreamRecorder.create();
    asyncStub.streamingOutputCall(request, recorder);
    recorder.awaitCompletion();
    assertSuccess(recorder);
    assertEquals(Arrays.asList(goldenResponses), recorder.getValues());
  }

  public void clientStreaming() throws Exception {
    final Messages.StreamingInputCallRequest[] requests = new Messages.StreamingInputCallRequest[4];
    for (int i = 0; i < 4; i++) {
      requests[i] = new Messages.StreamingInputCallRequest();
      requests[i].payload = new Messages.Payload();
    }
    requests[0].payload.body = new byte[27182];
    requests[1].payload.body = new byte[8];
    requests[2].payload.body = new byte[1828];
    requests[3].payload.body = new byte[45904];

    final Messages.StreamingInputCallResponse goldenResponse =
        new Messages.StreamingInputCallResponse();
    goldenResponse.aggregatedPayloadSize = 74922;

    StreamRecorder<Messages.StreamingInputCallResponse> responseObserver = StreamRecorder.create();
    StreamObserver<Messages.StreamingInputCallRequest> requestObserver =
        asyncStub.streamingInputCall(responseObserver);
    for (Messages.StreamingInputCallRequest request : requests) {
      requestObserver.onValue(request);
    }
    requestObserver.onCompleted();
    assertEquals(goldenResponse, responseObserver.firstValue().get());
  }

  public void pingPong() throws Exception {
    final Messages.StreamingOutputCallRequest[] requests =
        new Messages.StreamingOutputCallRequest[4];
    for (int i = 0; i < 4; i++) {
      requests[i] = new Messages.StreamingOutputCallRequest();
      requests[i].responseParameters = new Messages.ResponseParameters[1];
      requests[i].responseParameters[0] = new Messages.ResponseParameters();
      requests[i].payload = new Messages.Payload();
    }
    requests[0].responseParameters[0].size = 31415;
    requests[0].payload.body = new byte[27182];
    requests[1].responseParameters[0].size = 9;
    requests[1].payload.body = new byte[8];
    requests[2].responseParameters[0].size = 2653;
    requests[2].payload.body = new byte[1828];
    requests[3].responseParameters[0].size = 58979;
    requests[3].payload.body = new byte[45904];


    final Messages.StreamingOutputCallResponse[] goldenResponses =
        new Messages.StreamingOutputCallResponse[4];
    for (int i = 0; i < 4; i++) {
      goldenResponses[i] = new Messages.StreamingOutputCallResponse();
      goldenResponses[i].payload = new Messages.Payload();
      goldenResponses[i].payload.type = Messages.COMPRESSABLE;
    }
    goldenResponses[0].payload.body = new byte[31415];
    goldenResponses[1].payload.body = new byte[9];
    goldenResponses[2].payload.body = new byte[2653];
    goldenResponses[3].payload.body = new byte[58979];

    final LinkedBlockingDeque<Messages.StreamingOutputCallResponse> responses =
        new LinkedBlockingDeque();
    final Messages.StreamingOutputCallResponse magicTailResponse =
        new Messages.StreamingOutputCallResponse();
    @SuppressWarnings("unchecked")
    StreamObserver<Messages.StreamingOutputCallResponse> responseObserver =
        new StreamObserver<Messages.StreamingOutputCallResponse>() {

          @Override
          public void onValue(Messages.StreamingOutputCallResponse value) {
            responses.add(value);
          }

          @Override
          public void onError(Throwable t) {
          }

          @Override
          public void onCompleted() {
            responses.add(magicTailResponse);
          }
        };
    StreamObserver<Messages.StreamingOutputCallRequest> requestObserver
        = asyncStub.fullDuplexCall(responseObserver);
    for (int i = 0; i < requests.length; i++) {
      requestObserver.onValue(requests[i]);
      assertEquals(goldenResponses[i], responses.poll(5, TimeUnit.SECONDS));
      Assert.assertTrue("More than 1 responses received for ping pong test.", responses.isEmpty());
    }
    requestObserver.onCompleted();
    Assert.assertEquals(magicTailResponse, responses.poll(5, TimeUnit.SECONDS));
  }

  public static void assertEquals(MessageNano expected, MessageNano actual) {
    Assert.assertTrue("received message is not expected!",
        MessageNano.messageNanoEquals(expected, actual));
  }

  private static void assertSuccess(StreamRecorder<?> recorder) {
    if (recorder.getError() != null) {
      throw new AssertionError(recorder.getError());
    }
  }

  public static void assertEquals(List<? extends MessageNano> expected,
      List<? extends MessageNano> actual) {
    if (expected == null || actual == null) {
      Assert.assertEquals(expected, actual);
    } else if (expected.size() != actual.size()) {
      Assert.assertEquals(expected, actual);
    } else {
      for (int i = 0; i < expected.size(); i++) {
        assertEquals(expected.get(i), actual.get(i));
      }
    }
  }

  private SSLSocketFactory getSslSocketFactory(boolean useTestCa) throws Exception {
    if (!useTestCa) {
      return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    System.err.println("~~~ use upgrader");
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, TMS , null);
    return context.getSocketFactory();
  }

  public static void initTestCa(InputStream testCa) {
    try {
      KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
      ks.load(null);
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      X509Certificate cert = (X509Certificate) cf.generateCertificate(testCa);
      X500Principal principal = cert.getSubjectX500Principal();
      ks.setCertificateEntry(principal.getName("RFC2253"), cert);
      // Set up trust manager factory to use our key store.
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(ks);
      TMS = trustManagerFactory.getTrustManagers();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private SSLCertificateSocketFactory getSslCertificateSocketFactory(
      boolean useTestCa, String tlsHandshake) throws Exception {
    SSLCertificateSocketFactory factory = (SSLCertificateSocketFactory)
        SSLCertificateSocketFactory.getDefault(5000 /* Timeout in ms*/);
    // Use HTTP/2.0
    byte[] h2 = "h2".getBytes();
    byte[][] protocols = new byte[][]{h2};
    if (tlsHandshake.equals("alpn")) {
      System.err.println("~~~ set alpn");
      Method setAlpnProtocols =
          factory.getClass().getDeclaredMethod("setAlpnProtocols", byte[][].class);
      setAlpnProtocols.invoke(factory, new Object[] { protocols });
    } else if (tlsHandshake.equals("npn")) {
      System.err.println("~~~ set npn");
      Method setNpnProtocols =
          factory.getClass().getDeclaredMethod("setNpnProtocols", byte[][].class);
      setNpnProtocols.invoke(factory, new Object[]{protocols});
    } else {
      throw new RuntimeException("Unknown protocol for tls handshake: " + tlsHandshake);
    }

    if (useTestCa) {
      if (TMS == null) {
        throw new IllegalStateException("Failed installing test certificate!");
      }
      factory.setTrustManagers(TMS);
    }

    return factory;
  }
}
