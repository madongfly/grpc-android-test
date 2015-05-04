package io.grpc.android.integrationtest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

public class TesterActivity extends ActionBarActivity {
    private List<Button> mButtons;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private EditText mMessageEdit;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);
        mButtons = new LinkedList<Button>();
        mButtons.add((Button) findViewById(R.id.empty_unary_button));
        mButtons.add((Button) findViewById(R.id.large_unary_button));
        mButtons.add((Button) findViewById(R.id.client_streaming_button));
        mButtons.add((Button) findViewById(R.id.server_streaming_button));
        mButtons.add((Button) findViewById(R.id.ping_pong_button));

        mHostEdit = (EditText) findViewById(R.id.host_edit_text);
        mPortEdit = (EditText) findViewById(R.id.port_edit_text);
        mMessageEdit = (EditText) findViewById(R.id.message_edit_text);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);
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
        for (Button button : mButtons) {
            button.setEnabled(enable);
        }
    }

    private void startTest(String testCase) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        enableButtons(false);
        new GrpcTask(testCase).execute();
    }

    private class GrpcTask extends AsyncTask<Void, Void, String> {
        String mHost;
        int mPort;
        IntegrationTester mTester;
        String mTestCase;

        GrpcTask(String testCase) {
            mTestCase = testCase;
        }

        @Override
        protected void onPreExecute() {
            mResultText.setText("Testing...");
            mHost = mHostEdit.getText().toString();
            String portStr = mPortEdit.getText().toString();
            mPort = TextUtils.isEmpty(portStr) ? 0 : Integer.valueOf(portStr);
        }

        @Override
        protected String doInBackground(Void... nothing) {
            try {
                mTester = new IntegrationTester();
                mTester.init(mHost, mPort);
                mTester.runTest(mTestCase);
                return "Succeed!!!";
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return "Failed... : " + e.getMessage() + "\n" + sw.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mTester.shutdown();
            mResultText.setText(result);
            enableButtons(true);
        }
    }
}