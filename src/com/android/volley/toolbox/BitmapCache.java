package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
/**
 * Bitmap L1 Cache
 * @author hiphonezhu@gmail.com
 * @version [Volley, 2014-9-16]
 */
public class BitmapCache implements ImageCache
{
    private LruCache<String, Bitmap> mCache;  
    private static BitmapCache sInstance;
    private BitmapCache() {  
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
        int cacheSize = maxMemory / 8;  
        mCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) {  
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    return bitmap.getByteCount() / 1024;
                }
                else
                {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;  
                }
            }  
        };  
    }  
    
    public static synchronized BitmapCache getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new BitmapCache();
        }
        return sInstance;
    }
  
    @Override  
    public Bitmap getBitmap(String url) {  
        return mCache.get(url);  
    }  
  
    @Override  
    public void putBitmap(String url, Bitmap bitmap) {  
        mCache.put(url, bitmap);  
    }  
}
