package libcore.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
/**
 * Utils
 * @author hiphonezhu@gmail.com
 * @version [Android-Volley, 2015-1-13]
 */
public class Utils
{
    /**
     * 获得版本号
     * 
     * @return
     */
    public static int getVerCode(Context context)
    {
        int verCode = -1;
        try
        {
            verCode = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(),
                    0).versionCode;
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return verCode;
    }

    /**
     * 获得磁盘缓存目录 [PS：应用卸载后会被自动删除]
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName)
    {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
        {
            cachePath = context.getExternalCacheDir().getPath();
        }
        else
        {
            cachePath = context.getFilesDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
    
    public static String hashKeyForDisk(String key) {  
        String cacheKey;  
        try {  
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");  
            mDigest.update(key.getBytes());  
            cacheKey = bytesToHexString(mDigest.digest());  
        } catch (NoSuchAlgorithmException e) {  
            cacheKey = String.valueOf(key.hashCode());  
        }  
        return cacheKey;  
    }  
      
    private static String bytesToHexString(byte[] bytes) {  
        StringBuilder sb = new StringBuilder();  
        for (int i = 0; i < bytes.length; i++) {  
            String hex = Integer.toHexString(0xFF & bytes[i]);  
            if (hex.length() == 1) {  
                sb.append('0');  
            }  
            sb.append(hex);  
        }  
        return sb.toString();  
    }  
    
    public static SSLSocketFactory buildSSLSocketFactory(Context context,  
            int certRawResId) {  
        KeyStore keyStore = null;  
        try {  
            keyStore = buildKeyStore(context, certRawResId);  
        } catch (KeyStoreException e) {  
            e.printStackTrace();  
        } catch (CertificateException e) {  
            e.printStackTrace();  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
  
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();  
        TrustManagerFactory tmf = null;  
        try {  
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);  
            tmf.init(keyStore);  
  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (KeyStoreException e) {  
            e.printStackTrace();  
        }  
  
        SSLContext sslContext = null;  
        try {  
            sslContext = SSLContext.getInstance("TLS");  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
        try {  
            sslContext.init(null, tmf.getTrustManagers(), null);  
        } catch (KeyManagementException e) {  
            e.printStackTrace();  
        }  
  
        return sslContext.getSocketFactory();  
  
    }  
    
    private static KeyStore buildKeyStore(Context context, int certRawResId)  
        throws KeyStoreException, CertificateException,  
        NoSuchAlgorithmException, IOException {  
        String keyStoreType = KeyStore.getDefaultType();  
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);  
        keyStore.load(null, null);  
        
        Certificate cert = readCert(context, certRawResId);  
        keyStore.setCertificateEntry("ca", cert);  
        
        return keyStore;  
    }  

    private static Certificate readCert(Context context, int certResourceID) {  
        InputStream inputStream = context.getResources().openRawResource(  
                certResourceID);  
        Certificate ca = null;  
        
        CertificateFactory cf = null;  
        try {  
            cf = CertificateFactory.getInstance("X.509");  
            ca = cf.generateCertificate(inputStream);  
        
        } catch (CertificateException e) {  
            e.printStackTrace();  
        }  
        return ca;  
    }  
}
