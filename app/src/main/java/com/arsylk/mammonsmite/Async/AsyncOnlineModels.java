package com.arsylk.mammonsmite.Async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.arsylk.mammonsmite.Adapters.OnlineModelItem;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Log;
import com.arsylk.mammonsmite.utils.Utils;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;


public class AsyncOnlineModels extends AsyncWithDialog<Integer, OnlineModelItem, Boolean> {

    public AsyncOnlineModels(Context context, boolean showGui) {
        super(context, showGui, "Loading models...");
    }

    @Override
    protected Boolean doInBackground(Integer... offsets) {
        final String url = String.format(Define.ONLINE_MODELS_URL, offsets[0]);
        Log.append(AsyncOnlineModels.class.getSimpleName(), "url: "+url);
        try {
            String response = Ion.with(context.get()).load(url).asString(Charset.forName("utf-8")).get();
            Log.append(AsyncOnlineModels.class.getSimpleName(), "response: "+response);

            JSONObject json = new JSONObject(response);
            JSONArray modelsJson = json.getJSONArray("models");

            // iter all models
            for(int i = 0; i < modelsJson.length(); i++) {
                OnlineModelItem onlineModel = new OnlineModelItem(modelsJson.getJSONObject(i));

                // load preview bitmap
                if(onlineModel.getPreviewUrl() != null) {
                    File previewCache = new File(Define.BITMAP_CACHE_DIRECTORY, onlineModel.getId()+"_online.png");

                    // check for cached file
                    if(!previewCache.exists()) {
                        try {
                            // load & trim bitmap
                            Bitmap previewBitmapRaw = Ion.with(context.get()).load(onlineModel.getPreviewUrl()).asBitmap().get();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            Bitmap previewBitmapCut = Utils.trim(previewBitmapRaw);
                            previewBitmapCut.compress(Bitmap.CompressFormat.PNG, 100, bos);

                            // save to file
                            FileUtils.writeByteArrayToFile(previewCache, bos.toByteArray());

                            // recycle bitmaps & close stream
                            previewBitmapRaw.recycle();
                            previewBitmapCut.recycle();
                            bos.close();
                        }catch(Exception e) {
                            Log.append(e.getClass().getSimpleName(), e.toString());
                            e.printStackTrace();
                        }
                    }
                    onlineModel.setPreviewBitmap(BitmapFactory.decodeFile(previewCache.getAbsolutePath()));
                    Log.append(AsyncOnlineModels.class.getSimpleName(), "preview: "+previewCache.getAbsolutePath());
                }

                // return loaded model
                publishProgress(onlineModel);
            }

            // check if any models left
            return modelsJson.length() > 0;
        }catch(Exception e) {
            Log.append(e.getClass().getSimpleName(), e.toString());
            e.printStackTrace();
        }
        return false;
    }
}
