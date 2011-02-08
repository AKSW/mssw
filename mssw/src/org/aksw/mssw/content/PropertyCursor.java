package org.aksw.mssw.content;

import java.io.InputStream;
import java.net.URL;

import org.aksw.mssw.Constants;
import org.aksw.mssw.R;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

/**
 * 
 * @author natanael
 * 
 */
public class PropertyCursor extends SimpleCursorAdapter {
	
	public PropertyCursor(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		
		String predicate =  cursor.getString(2);
		String obj = cursor.getString(3);
		
		Drawable bm;
		Boolean hit = false;
		String image = "";
		
        for(int index = 0; index < Constants.PROPS_pictureProps.length; index++) {            
	        if (Constants.PROPS_pictureProps[index].equals(predicate)) {
	        	Log.v("PICMATCH", obj);
	        	hit = true;
	        	image = obj;
	        	break;
	        }
	    }
        
        ImageView img = (ImageView) view.findViewById(R.id.picon);
        
        if(hit && image.length() > 0){        	
        	if(img.getVisibility() == View.VISIBLE) return;
        	bm = loadImageFromWeb(image);
        	if(bm != null){
        		img.setImageDrawable(bm);
        		img.setVisibility(View.VISIBLE);
        	}
        }else{
        	img.setImageDrawable(null);
        	img.setVisibility(View.INVISIBLE);
        }
	}
	
	private Drawable loadImageFromWeb(String url){
        Log.i("IMGLOAD", "Fetching image");
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src");
            Log.i("IMGLOAD", "Created image from stream");
            return d;
        }catch (Exception e) {
            //TODO handle error
            Log.i("IMGLOAD", "Error fetching image");
            System.out.println("Exc="+e);
            return null;
        }
    }

}
