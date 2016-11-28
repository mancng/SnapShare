package com.parse.SnapShare;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    //Create getPhoto method (from Media browser) since it will be used in multiple places
    public void getPhoto() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);

    }

    //This will run once the permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {

            if (grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getPhoto();

            }
        }
    }

    //Create a onCreateOptionsMenu for additional features
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    //When an item from the menu is tapped
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Share
        if (item.getItemId() == R.id.share) {

            //Checking to see if permission is already granted. If not, then we explicitly ask for permission again
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            //If permission is granted
            } else {

                getPhoto();

            }

        //Sign Out user
        } else if (item.getItemId() == R.id.signOut) {

            ParseUser.logOut();

            Toast.makeText(getApplicationContext(),"You have logged out", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                //Log and verify first before implementing the upload of the image to the Parse server
//                Log.i("Photo", "Received");

                //Upload image to Parse server
                //Covert image to a parse file which then can be uploaded as a parse object to the server
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                //100 = high quality. Get the bitmap compressed into png format
                bitmap.compress(Bitmap.CompressFormat.PNG, 5, stream);

                //Convert the png format into the byteArray. In order to get a parse file, you'll need to go thru a byteArray
                byte[] byteArray = stream.toByteArray();

                //Covert into a parse file
                ParseFile file = new ParseFile("image.png",byteArray);

                //Create a new Parse object to store all the images called "Image" class
                ParseObject object = new ParseObject("Image");

                //Add additional data to the object
                object.put("image", file);

                object.put("username", ParseUser.getCurrentUser().getUsername());

                //Save image in the background
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {

                            Toast.makeText(UserListActivity.this, "Image Shared!", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(UserListActivity.this, "Image could not be shared. Please try again later.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        setTitle("User Feed");

        //Create array list for the data in Parse server
        final ArrayList<String> usernames = new ArrayList<>();

        //Verify to see if "Test" is shown on the array list. (Perform this before querying from Parse server
//        usernames.add("Test");

        final ListView viewUserList = (ListView) findViewById(R.id.viewUserList);

        //Setup an intent to jump to UserFeedActivity when user tabs on a user on viewUserList
        viewUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), UserFeedActivity.class);

                //Send username along when it's tapped
                intent.putExtra("username", usernames.get(position));
                startActivity(intent);

            }
        });

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);

        ParseQuery<ParseUser> query = ParseUser.getQuery();

        //Querying users other than the currently logged in user
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());

        query.addAscendingOrder("username");

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {

                if (e == null) {

                    if (objects.size() > 0) {

                        for (ParseUser user : objects) {

                            usernames.add(user.getUsername());

                        }

                        viewUserList.setAdapter(arrayAdapter);

                    }

                } else {

                    e.printStackTrace();
                }
            }
        });
    }
}
