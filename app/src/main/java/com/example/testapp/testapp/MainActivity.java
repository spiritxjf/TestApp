package com.example.testapp.testapp;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.example.testapp.rsa.RSAUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static android.provider.Contacts.SettingsColumns.KEY;
import com.example.testapp.aes.AESUtils;

public class MainActivity extends Activity implements OnClickListener
{
    private Button btn1, btn2,btMsg, audioEn;// 加密，解密
    private EditText et1, et2, et3;// 需加密的内容，加密后的内容，解密后的内容
    private KeyPair keyPair = null;

    /* 密钥内容 base64 code */
    private static String PUCLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCfRTdcPIH10gT9f31rQuIInLwe"
            + "\r" + "7fl2dtEJ93gTmjE9c2H+kLVENWgECiJVQ5sonQNfwToMKdO0b3Olf4pgBKeLThra" + "\r"
            + "z/L3nYJYlbqjHC3jTjUnZc0luumpXGsox62+PuSGBlfb8zJO6hix4GV/vhyQVCpG" + "\r"
            + "9aYqgE7zyTRZYX9byQIDAQAB" + "\r";
    private static String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ9FN1w8gfXSBP1/"
            + "kZrAJ2hwSBqptcABYk6ED70gRTQ1S53tyQXIOSjRBcugY/21qeswS3nMyq3xDEPK" + "\r"
            + "XpdyKPeaTyuK86AEkQJBAM1M7p1lfzEKjNw17SDMLnca/8pBcA0EEcyvtaQpRvaL" + "\r"
            + "n61eQQnnPdpvHamkRBcOvgCAkfwa1uboru0QdXii/gUCQQDGmkP+KJPX9JVCrbRt" + "\r"
            + "7wKyIemyNM+J6y1ZBZ2bVCf9jacCQaSkIWnIR1S9UM+1CFE30So2CA0CfCDmQy+y" + "\r"
            + "7A31AkB8cGFB7j+GTkrLP7SX6KtRboAU7E0q1oijdO24r3xf/Imw4Cy0AAIx4KAu" + "\r"
            + "L29GOp1YWJYkJXCVTfyZnRxXHxSxAkEAvO0zkSv4uI8rDmtAIPQllF8+eRBT/deD" + "\r"
            + "JBR7ga/k+wctwK/Bd4Fxp9xzeETP0l8/I+IOTagK+Dos8d8oGQUFoQJBAI4Nwpfo" + "\r"
            + "MFaLJXGY9ok45wXrcqkJgM+SN6i8hQeujXESVHYatAIL/1DgLi+u46EFD69fw0w+" + "\r" + "c7o0HLlMsYPAzJw="
            + "\r";
    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    private static final String CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";//AES是加密方式 CBC是工作模式 PKCS5Padding是填充模式
    private static final String PBKDF = "PBKDF2WithHmacSHA1";

    //二进制转字符
    @NonNull
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    public static SecretKey createAESKey(byte[] aSecret) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        SecretKeySpec secret = new SecretKeySpec(aSecret, "AES");
        return secret;
    }

    public static void testGenerate() throws InvalidKeySpecException,NoSuchAlgorithmException
    {
        char[] pwd = {'a','b','c'};
        byte[] salt = {1,2,3,4,5,6,7};
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        KeySpec spec = new PBEKeySpec(pwd, salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        keyPair = RSAUtils.generateRSAKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate();

        try{
            SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes_key = new byte[16];
            localSecureRandom.nextBytes(bytes_key);
            String str_key = toHex(bytes_key);
            AESUtils.generateAESByMsg(bytes_key);
        }
        catch (Exception e)
        {

        }
        //et2.setText(rsaPublicKey.getPublicExponent().toString());
        //et3.setText(rsaPrivateKey.getPrivateExponent().toString());
        /*
        String str_key = null;
        byte[] clear = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};

        try {
            testGenerate();
            SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] bytes_key = new byte[16];
            localSecureRandom.nextBytes(bytes_key);
            str_key = toHex(bytes_key);
            SecretKeySpec skeySpec = new SecretKeySpec(bytes_key, "AES");

            Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] encrypted = cipher.doFinal(clear);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] decrypted = cipher.doFinal(encrypted);
            Log.d("93367", "onCreate: " + skeySpec+" " + encrypted + " " + decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    private void initView()
    {
        btn1 = (Button) findViewById(R.id.encrypt);
        btn2 = (Button) findViewById(R.id.decrypt);
        btMsg = (Button) findViewById(R.id.BTMsgTest);
        audioEn = (Button) findViewById(R.id.AudioEncrypt);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btMsg.setOnClickListener(this);
        audioEn.setOnClickListener(this);

        et1 = (EditText) findViewById(R.id.source_content);
        et2 = (EditText) findViewById(R.id.encrypt_content);
        et3 = (EditText) findViewById(R.id.decrypt_content);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.BTMsgTest:
                Intent intent;
                intent = new Intent(MainActivity.this, MainActivityServer.class);
                startActivity(intent);
                break;
            case R.id.TestAesEncrypt:
                Intent intent2;
                intent2 = new Intent(MainActivity.this, TextAesEncryptionActivity.class);
                startActivity(intent2);
                break;
            case R.id.AudioEncrypt:
                Intent intent3;
                intent3 = new Intent(MainActivity.this, AudioEncryptionActivity.class);
                startActivity(intent3);
                break;
            // 加密
            case R.id.encrypt:
                String source = et1.getText().toString().trim();
                try
                {
                    // 从字符串中得到公钥
                    //PublicKey publicKey = keyPair.getPublic();
                    //PublicKey publicKey = RSAUtils.loadPublicKey(PUCLIC_KEY);
                    // 从文件中得到公钥
                    //InputStream inPublic = getResources().getAssets().open("rsa_public_key.pem");
                    //PublicKey publicKey = RSAUtils.loadPublicKey(inPublic);
                    // 加密
                    //byte[] encryptByte = RSAUtils.encryptData(source.getBytes(), publicKey);
                    // 为了方便观察吧加密后的数据用base64加密转一下，要不然看起来是乱码,所以解密是也是要用Base64先转换
                    //String afterencrypt = Base64Utils.encode(encryptByte);
                    //et2.setText(afterencrypt);
                    //et2.setText(RSAUtils.encryptString(source));
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
                    // 从字符串中得到私钥
                    //PrivateKey privateKey = keyPair.getPrivate();
                    //PrivateKey privateKey = RSAUtils.loadPrivateKey(PRIVATE_KEY);
                    // 从文件中得到私钥
                    //InputStream inPrivate = getResources().getAssets().open("pkcs8_rsa_private_key.pem");
                    //PrivateKey privateKey = RSAUtils.loadPrivateKey(inPrivate);
                    // 因为RSA加密后的内容经Base64再加密转换了一下，所以先Base64解密回来再给RSA解密
                    //byte[] decryptByte = RSAUtils.decryptData(Base64Utils.decode(encryptContent), privateKey);
                    //String decryptStr = new String(decryptByte);
                    //et3.setText(decryptStr);
                    //et3.setText(RSAUtils.decryptString(encryptContent));
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
