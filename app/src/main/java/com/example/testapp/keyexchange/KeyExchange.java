package com.example.testapp.keyexchange;

import android.util.Base64;

import com.example.testapp.btconnection.BTClient;
import com.example.testapp.btconnection.BTServer;
import com.example.testapp.rsa.RSAUtils;

import java.io.UnsupportedEncodingException;

import static com.example.testapp.aes.AESUtils.*;

/**
 * Created by 93367 on 2017/5/18.
 */

public class KeyExchange {
    /**
     *
     */
    public static void sendKey()
    {
        //get aeskey
        byte[] aesEncoded = getAESKey();
        //msg start with xxxxx
        try {
            String sendText = Base64.encodeToString(aesEncoded, Base64.DEFAULT);
            BTClient.send("xxxxx" + RSAUtils.encryptString(sendText));
        }catch (Exception e){

        }
        //encrypt aeskey
        return;
    }

    public static void onKeyRecived(String text) throws UnsupportedEncodingException {
        String encodeString = RSAUtils.decryptString(text);
        byte[] encodeKey = Base64.decode(encodeString, Base64.DEFAULT);

        try {
            generateAESByMsg(encodeKey);
        }
        catch (Exception e)
        {
            //TODO send fail message
        }

        return;
    }
}
