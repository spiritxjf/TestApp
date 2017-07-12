package com.example.testapp.testapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.testapp.aes.AESUtils;
import java.security.SecureRandom;

/**
 * Created by 93367 on 2017/5/23.
 */

public class TextAesEncryptionActivity extends Activity implements OnClickListener {
    private Button btn1, btn2;// 加密，解密
    private EditText et1, et2, et3;// 需加密的内容，加密后的内容，解密后的内容

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_aes_encrypt_activity);
        initView();

        try{
            SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes_key = new byte[16];
            localSecureRandom.nextBytes(bytes_key);
            AESUtils.generateAESByMsg(bytes_key);
        }
        catch (Exception e)
        {

        }
    }

    private void initView()
    {
        btn1 = (Button) findViewById(R.id.encrypt);
        btn2 = (Button) findViewById(R.id.decrypt);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        et1 = (EditText) findViewById(R.id.source_content);
        et2 = (EditText) findViewById(R.id.encrypt_content);
        et3 = (EditText) findViewById(R.id.decrypt_content);
    }

    @Override
    public void onClick(View v)
    {
        //将输入法隐藏，mPasswordEditText 代表密码输入框
        InputMethodManager imm =(InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et1.getWindowToken(), 0);
        switch (v.getId())
        {
            // 加密
            case R.id.encrypt:
                String source = et1.getText().toString().trim();
                try
                {
                    et2.setText(AESUtils.AESencryptString(source));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            // 解密
            case R.id.decrypt:
                String encryptContent = et2.getText().toString().trim();
                try
                {
                    et3.setText(AESUtils.AESdecryptString(encryptContent));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
