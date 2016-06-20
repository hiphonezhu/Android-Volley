package com.android.volley.plus.download;

import java.io.InputStream;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
/**
 * A canned request for retrieving the response body at a given URL as a InputStream.
 */
public abstract class InputStreamRequest extends Request<InputStream>
{
    private Listener<InputStream> mListener;
    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public InputStreamRequest(int method, String url, Listener<InputStream> listener,
            ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public InputStreamRequest(String url, Listener<InputStream> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }
    
    @Override
    protected abstract Response<InputStream> parseNetworkResponse(NetworkResponse response);

    @Override
    protected void deliverResponse(InputStream response)
    {
        if (mListener != null)
        {
            mListener.onResponse(response);
        }
    }
    
    @Override
    public boolean isNeedStream()
    {
        return true;
    }
}
