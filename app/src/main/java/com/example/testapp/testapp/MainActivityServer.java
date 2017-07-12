package com.example.testapp.testapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.btconnection.BTClient;
import com.example.testapp.btconnection.BTServer;
import com.example.testapp.keyexchange.KeyExchange;
import com.example.testapp.rsa.RSAUtils;
import com.example.testapp.aes.AESUtils;

import java.security.spec.KeySpec;

import javax.crypto.spec.PBEKeySpec;

import static com.example.testapp.aes.AESUtils.getAESKey;

public class MainActivityServer extends Activity
{
    public final static String ACTION_DISPLAY = "btdisplay";
    public final static String KEY_MSG = "msg";

    private Button mServerButton;
    private Button mClientButton1;
    private Button mPwdExButton;
    private TextView mTextView;
    private TextView mTextViewEncrypt;
    private Button mClientSendButton;
    private EditText mEditText;
    private BroadcastReceiver mReceiver;
    private EditText mServerName;
    private TextView mAESTextView;
    private Boolean mIsServer = false;

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        this.setContentView(R.layout.page_activity_server);
        this.mServerButton = (Button) findViewById(R.id.ServerButton);
        this.mClientButton1 = (Button) findViewById(R.id.ClientButton1);
        this.mPwdExButton = (Button) findViewById(R.id.KeyExchange);
        this.mTextView = (TextView) findViewById(R.id.TextView);
        this.mTextViewEncrypt = (TextView) findViewById(R.id.TextViewEncrypt);
        this.mClientSendButton = (Button) findViewById(R.id.ClientSendButton);
        this.mEditText = (EditText) findViewById(R.id.EditText);
        this.mServerName = (EditText) findViewById(R.id.ServerName);
        this.mAESTextView = (TextView) findViewById(R.id.AesKey);
        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        OnClickListener listener = new OnClickListener()
        {
            public void onClick(View view)
            {
                if (view == mServerButton)
                {
                    BTServer.startBTServer(MainActivityServer.this);
                    mClientSendButton.setVisibility( View.VISIBLE );
                    mPwdExButton.setVisibility(View.GONE  );
                    mClientButton1.setVisibility( View.GONE );
                    mPwdExButton.setVisibility( View.GONE );
                    mServerName.setVisibility( View.GONE );
                    mIsServer = true;
                    mEditText.setText( "Hello Client!" );
                }
                else if (view == mClientButton1)
                {
                    InputMethodManager imm =(InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mServerName.getWindowToken(), 0);
                    String serverName = mServerName.getEditableText().toString().trim();
                    if (serverName.length() > 0)
                    {
                        BTClient.setDistServerName(serverName);
                    }

                    BTClient.startConnect(MainActivityServer.this, 0);
                    mClientSendButton.setVisibility( View.VISIBLE );
                    mPwdExButton.setVisibility( View.VISIBLE );
                    mServerButton.setVisibility( View.GONE );
                    mIsServer = false;
                    mEditText.setText( "Hello Server!" );
                }
                else if (view == mPwdExButton)
                {
                    KeyExchange.sendKey();
                    byte[] tt = getAESKey();
                    mAESTextView.setText(Base64.encodeToString(tt, Base64.DEFAULT));
                    //TODO key exchange
                }
                else if (view == mClientSendButton)
                {
                    InputMethodManager imm =(InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    String msg = mEditText.getEditableText().toString().trim();
                    if (msg.length() == 0)
                    {
                        Toast.makeText(MainActivityServer.this,
                                "Msg should not be empty", Toast.LENGTH_SHORT)
                                .show();
                    }
                    else
                    {
                        //BTClient.send(msg);
                        //BTClient.send("xxxxx" + RSAUtils.encryptString(msg));
                        try {
                            if (mIsServer)
                            {
                                BTServer.send( "zzzzz" + AESUtils.AESencryptString( msg ) );
                            }
                            else {
                                BTClient.send( "zzzzz" + AESUtils.AESencryptString( msg ) );
                            }
                        }
                        catch (Exception e)
                        {

                        }
                    }
                }
            }
        };
        mServerButton.setOnClickListener(listener);
        mClientButton1.setOnClickListener(listener);
        mPwdExButton.setOnClickListener(listener);
        mClientSendButton.setOnClickListener(listener);

        IntentFilter filter = new IntentFilter(ACTION_DISPLAY);
        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String msg = intent.getStringExtra(KEY_MSG);
                if (msg.startsWith("zzzzz"))
                {
                    String submsg = msg.substring(5);
                    try {
                        mTextView.setText("Received decrypt: " + AESUtils.AESdecryptString(submsg));
                        //BTServer.send("Text received!");
                    }
                    catch (Exception e){
                        //TODO
                        //BTServer.send("Text receive failed!");
                    }
                    mTextViewEncrypt.setText("Received: " + submsg);
                }
                else if (msg.startsWith("xxxxx"))
                {
                    String submsg = msg.substring(5);
                    try {
                        KeyExchange.onKeyRecived(submsg);
                        mTextView.setText("Received decrypt(Base64): " + RSAUtils.decryptString(submsg));
                        mTextViewEncrypt.setText("Received: " + submsg);
                        byte[] tt = getAESKey();
                        mAESTextView.setText( Base64.encodeToString(tt, Base64.DEFAULT));
                        BTServer.send("Key received!");
                    }
                    catch (Exception e){
                        BTServer.send("Key receive failed!");
                    }
                }
                else
                {
                    mTextView.setText("Received: " +msg);
                    mTextViewEncrypt.setText("");
                }
            }
        };
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }
}
