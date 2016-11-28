package com.parse.SnapShare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class UserFeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);


        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        //To show the photo feed for tapped user
        //Get the Intent id passed along when the user was tapped
        Intent intent = getIntent();

        //Create a string to reference the username
        String selectedUsename = intent.getStringExtra("username");

        //Set the title to show selected username
        setTitle(selectedUsename + "'s Feed");

        //Get all the images from the "Image" class posted by tapped user
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Image");

        query.whereEqualTo("username", selectedUsename);
        //Set the newest record at the top
        query.orderByDescending("createdAt");

        //Run query
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {

                    if (objects.size() > 0) {

                        for (ParseObject object : objects) {

                            //Find the image
                            ParseFile file = (ParseFile) object.get("image");

                            //Download the image
                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {

                                    if (e == null && data != null) {

                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                        //Programmatically add an imageView and set an image to the linearLayout
                                        //Creating a new imageView (Not finding it by id)
                                        ImageView imageView = new ImageView(getApplicationContext());

                                        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT
                                        ));

                                        //Just add a random pic to verify before bitmap implementation
                                        imageView.setImageBitmap(bitmap);

                                        linearLayout.addView(imageView);

                                    }
                                }
                            });
                        }
                    } else {

                        Toast.makeText(getApplicationContext(),"User has not shared any photo!",Toast.LENGTH_LONG).show();
                    }

                } else {

                    Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();

                }
            }
        });


/*
        //Verify before implement further code
        //Programmatically add an imageView and set an image to the linearLayout
        //Creating a new imageView (Not finding it by id)
        ImageView imageView = new ImageView(getApplicationContext());

        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        //Just add a random pic to verify before bitmap implementation
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.instagramlogo));

        linearLayout.addView(imageView);
*/
    }
}
