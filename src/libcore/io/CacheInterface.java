package libcore.io;
import com.android.volley.Response.Listener;
import android.graphics.Bitmap;
public interface CacheInterface
{
    public void getBitmap(String key, Listener<Bitmap> listener);
    public void putBitmap(String key, Bitmap bitmap);
}
