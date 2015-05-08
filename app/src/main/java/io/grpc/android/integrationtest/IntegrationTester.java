package io.grpc.android.integrationtest;

import com.google.protobuf.nano.MessageNano;
import io.grpc.ChannelImpl;
import io.grpc.transport.okhttp.OkHttpChannelBuilder;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
import junit.framework.Assert;

public final class IntegrationTester {

  private ChannelImpl channel;
  private TestServiceGrpc.TestServiceBlockingStub blockingStub;
  protected TestServiceGrpc.TestService asyncStub;

  private String serverHostOverride = "foo.test.google.fr";
  private boolean useTls = true;
  private boolean useTestCa = true;
  private static TrustManager[] TMS;

  public void init(String host, int port) {
    OkHttpChannelBuilder channelBuilder = OkHttpChannelBuilder.forAddress(host, port);
    if (serverHostOverride != null) {
      // Force the hostname to match the cert the server uses.
      channelBuilder.overrideHostForAuthority(serverHostOverride);
    }
    if (useTls) {
      try {
        channelBuilder.sslSocketFactory(getSslSocketFactory());
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
    Messages.SimpleResponse response = blockingStub.unaryCall(request);
    assertEquals(goldenResponse, response);
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
    Assert.assertTrue("received message is not expected!",
        MessageNano.messageNanoEquals(expected, actual));
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

  private SSLSocketFactory getSslSocketFactory() throws Exception {
    if (!useTestCa) {
      return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }


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

  //private SSLCertificateSocketFactory getSslSocketFactory() throws Exception {
  //  SSLCertificateSocketFactory factory = (SSLCertificateSocketFactory)
  //      SSLCertificateSocketFactory.getDefault(5000 /* Timeout in ms*/);
  //  // Use HTTP/2.0 Draft 15
  //  byte[] h215 = "h2-15".getBytes();
  //  byte[][] protocols = new byte[][]{h215};
  //  //Method setAlpnProtocols =
  //  //    factory.getClass().getDeclaredMethod("setAlpnProtocols", byte[][].class);
  //  //setAlpnProtocols.invoke(factory, new Object[] { protocols });
  //  Method setNpnProtocols =
  //      factory.getClass().getDeclaredMethod("setNpnProtocols", byte[][].class);
  //  setNpnProtocols.invoke(factory, new Object[]{protocols});
  //
  //  if (useTestCa) {
  //    if (TMS == null) {
  //      throw new IllegalStateException("Failed installing test certificate!");
  //    }
  //    factory.setTrustManagers(TMS);
  //  }
  //
  //  return factory;
  //}
}
