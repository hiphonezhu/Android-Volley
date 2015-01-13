package libcore.io;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;
/**
 * Bitmap L1 Cache
 * @author hiphonezhu@gmail.com
 * @version [Volley, 2014-9-16]
 */
public class MemoryLruCache
{
    private LruCache<String, Bitmap> mCache;  
    private static MemoryLruCache sInstance;
    private MemoryLruCache() {  
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
    
    public static synchronized MemoryLruCache getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new MemoryLruCache();
        }
        return sInstance;
    }
  
    public Bitmap getBitmap(String key) 
    {  
        return mCache.get(key);  
    }  
  
    public void putBitmap(String key, Bitmap bitmap) 
    {  
        mCache.put(key, bitmap);  
    }  
}
