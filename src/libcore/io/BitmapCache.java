package libcore.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.ExecutorDelivery;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader.ImageCache;
/**
 * L1&L2 cache
 * @author hiphonezhu@gmail.com
 * @version [Android-Volley, 2015-1-13]
 */
public class BitmapCache implements ImageCache
{
    private MemoryLruCache memoryLruCache;
    private DiskLruCache diskLruCache;
    private ExecutorDelivery delivery;
    private Executor sDefaultExecutor = Executors.newFixedThreadPool(10); // 读取磁盘缓存线程池
    private static BitmapCache sInstance;
    
    /**
     * single instance
     * @param context
     * @param cacheDir directory used to cache bitmap
     * @return
     */
    public static synchronized BitmapCache getInstance(Context context, String cacheDir)
    {
        if (sInstance == null)
        {
            sInstance = new BitmapCache(context.getApplicationContext(), cacheDir);
        }
        return sInstance;
    }
    
    private BitmapCache(Context context, String cacheDir)
    {
        delivery = new ExecutorDelivery(new Handler(Looper.getMainLooper()));
        memoryLruCache = MemoryLruCache.getInstance(); 
        try
        {
            diskLruCache = DiskLruCache.open(Utils.getDiskCacheDir(context, cacheDir), Utils.getVerCode(context), 1, 10 * 10 * 1024);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public Bitmap getBitmap(String url)
    {
        final String key = Utils.hashKeyForDisk(url);
        return memoryLruCache.getBitmap(key);
    }
    

    @Override
    public void putBitmap(String url, Bitmap bitmap)
    {
        final String key = Utils.hashKeyForDisk(url);
        memoryLruCache.putBitmap(key, bitmap);
    }
    
    /**
     * Find bitmap from disk cache
     * @param url
     * @param listener
     * @see com.android.volley.toolbox.ImageLoader.ImageCache#getBitmap(java.lang.String, com.android.volley.Response.Listener)
     */
    public void getBitmap(String url, final Listener<Bitmap> listener)
    {
        final String key = Utils.hashKeyForDisk(url);
        sDefaultExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                Bitmap bitmap = null;
                try
                {
                    DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
                    if (snapShot != null) 
                    {  
                        InputStream is = snapShot.getInputStream(0);  
                        bitmap = BitmapFactory.decodeStream(is);  
                    }
                }
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
                finally
                {
                    final Bitmap response = bitmap;
                    delivery.postRunnable(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onResponse(response);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Save bitmap to disk cache
     * @param url
     * @param bitmap
     * @param listener
     * @see com.android.volley.toolbox.ImageLoader.ImageCache#putBitmap(java.lang.String, android.graphics.Bitmap, com.android.volley.Response.Listener)
     */
    @Override
    public void putBitmap(String url, final Bitmap bitmap, final Listener<Boolean> listener)
    {
        final String key = Utils.hashKeyForDisk(url);
        sDefaultExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);  
                    if (editor != null) {  
                        OutputStream outputStream = editor.newOutputStream(0); 
                        if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {  
                            editor.commit();  
                        } else {  
                            editor.abort();  
                        }  
                    }  
                    diskLruCache.flush();  
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    delivery.postRunnable(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onResponse(true);
                        }
                    });
                }
            }
        });
    }
}
