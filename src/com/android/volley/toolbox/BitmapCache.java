package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
/**
 * Bitmap L1����
 * @author hiphonezhu@gmail.com
 * @version [Volley, 2014-9-16]
 */
public class BitmapCache implements ImageCache
{
    private LruCache<String, Bitmap> mCache;  
    
    public BitmapCache() {  
        // ��ȡ�������ڴ�����ֵ��ʹ���ڴ泬�����ֵ������OutOfMemory�쳣��  
        // LruCacheͨ�����캯�����뻺��ֵ����KBΪ��λ��  
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
        // ʹ���������ڴ�ֵ��1/8��Ϊ����Ĵ�С��  
        int cacheSize = maxMemory / 8;  
        mCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) {  
                return bitmap.getRowBytes() * bitmap.getHeight();  
            }  
        };  
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
