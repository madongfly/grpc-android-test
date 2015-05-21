package io.grpc.android.integrationtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import java.util.LinkedList;
import java.util.List;

public class TesterActivity extends AppCompatActivity
    implements ProviderInstaller.ProviderInstallListener {
  private List<Button> buttons;
  private EditText hostEdit;
  private EditText portEdit;
  private TextView resultText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tester);
    buttons = new LinkedList<>();
    buttons.add((Button) findViewById(R.id.empty_unary_button));
    buttons.add((Button) findViewById(R.id.large_unary_button));
    buttons.add((Button) findViewById(R.id.client_streaming_button));
    buttons.add((Button) findViewById(R.id.server_streaming_button));
    buttons.add((Button) findViewById(R.id.ping_pong_button));

    hostEdit = (EditText) findViewById(R.id.host_edit_text);
    portEdit = (EditText) findViewById(R.id.port_edit_text);
    resultText = (TextView) findViewById(R.id.grpc_response_text);

    System.err.println("~~~ gms available: " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this));

    IntegrationTester.initTestCa(getResources().openRawResource(R.raw.ca));
    ProviderInstaller.installIfNeededAsync(this, this);
  }

  public void startEmptyUnary(View view) {
    startTest("empty_unary");
  }

  public void startLargeUnary(View view) {
    startTest("large_unary");
  }

  public void startClientStreaming(View view) {
    startTest("client_streaming");
  }

  public void startServerStreaming(View view) {
    startTest("server_streaming");
  }

  public void startPingPong(View view) {
    startTest("ping_pong");
  }

  private void enableButtons(boolean enable) {
    for (Button button : buttons) {
      button.setEnabled(enable);
    }
  }

  private void startTest(String testCase) {
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
        hostEdit.getWindowToken(), 0);
    enableButtons(false);
    String host = hostEdit.getText().toString();
    String portStr = portEdit.getText().toString();
    int port = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);

    // TODO (madongfly) support server_host_override, useTls and useTestCa in the App UI.
    new GrpcTestTask(testCase, host, port, "foo.test.google.fr", true, true, null,
        new GrpcTestTask.TestListener() {
      @Override public void onPreTest() {
        resultText.setText("Testing...");
      }

      @Override public void onPostTest(String result) {
        resultText.setText(result);
        enableButtons(true);
      }
    }).execute();
  }

  @Override
  public void onProviderInstalled() {
    // Provider is up-to-date, app can make secure network calls.
    System.err.println("~~~ Provider installed");
  }

  @Override
  public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
    // Bet the current security library is up to date or fail.
    System.err.println("~~~ Provider install failed!!!!");
  }
}
