package libcore.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.volley.Response.Listener;
/**
 * L1&L2 cache
 * @author hiphonezhu@gmail.com
 * @version [Android-Volley, 2015-1-13]
 */
public class BitmapCache implements CacheInterface
{
    private MemoryLruCache memoryLruCache;
    private DiskLruCache diskLruCache;
    private static BitmapCache sInstance;
    /**
     * HashMap of Cache keys -> used to track in-flight disk search task so
     * that we can coalesce multiple search to the same URL into a single task.
     */
    private final HashMap<String, Boolean> mInFlightRequests =
            new HashMap<String, Boolean>();
    
    public static synchronized BitmapCache getInstance(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new BitmapCache(context.getApplicationContext());
        }
        return sInstance;
    }
    
    private BitmapCache(Context context)
    {
        memoryLruCache = MemoryLruCache.getInstance(); 
        try
        {
            diskLruCache = DiskLruCache.open(Utils.getDiskCacheDir(context, "bitmap"), Utils.getVerCode(context), 1, 10 * 10 * 1024);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void getBitmap(String url, final Listener<Bitmap> listener)
    {
        final String key = Utils.hashKeyForDisk(url);
        // find in memory
        memoryLruCache.getBitmap(key, new Listener<Bitmap>()
        {
            @Override
            public void onResponse(Bitmap response)
            {
                if (response != null)
                {
                    listener.onResponse(response);
                }
                else
                {
                    // Check to see if a search is already in-flight.
                    if (mInFlightRequests.get(key))
                    {
                        return;
                    }
                    else
                    {
                        // find in disk
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                DiskLruCache.Snapshot snapShot;
                                try
                                {
                                    snapShot = diskLruCache.get(key);
                                    if (snapShot != null) 
                                    {  
                                        InputStream is = snapShot.getInputStream(0);  
                                        Bitmap bitmap = BitmapFactory.decodeStream(is);  
                                        if (bitmap != null)
                                        {
                                            memoryLruCache.putBitmap(key, bitmap);
                                        }
                                        listener.onResponse(bitmap);
                                    }
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }  
                            }
                        }).start();
                    }
                }
            }
        });
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap)
    {
        final String key = Utils.hashKeyForDisk(url);
        // save in memory
        memoryLruCache.putBitmap(key, bitmap);
        // save in disk
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
    }
}
