package com.kwame.android.gallery.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.kwame.android.gallery.Adapter.ImageAdapter;
import com.kwame.android.gallery.Interface.ItemCallback;
import com.kwame.android.gallery.Model.Image;
import com.kwame.android.gallery.R;
import com.kwame.android.gallery.Realm.Albums;
import com.kwame.android.gallery.Realm.RealmController;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kwame on 10/14/2017.
 */
public class AlbumImagesActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EXTRAS = "extras";
    private static final String POSITION = "position";
    private static final int TAKE_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 3;
    private static final String IMAGE_PATH = "image";
    private static final String IMG_EXTRAS = "extras";

    private File directory;
    private File photoFile;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Image> imageData = new ArrayList<>();
    private String dirName;
    private String nameOfAlbum;
    private String pos;
    private Realm realm;


    //the entry point to google play services used by places api and fused service provider
    private GoogleApiClient googleApiClient;
    private int PLACE_PICKER_REQUEST = 2;

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private Place place;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.album_images);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Images");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        realm = RealmController.getInstance().getRealm();

        mGeoDataClient = Places.getGeoDataClient(this, null); //construct a GeoDataClient
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        googleApiClient.connect();


        Bundle bundle = getIntent().getBundleExtra(EXTRAS);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);

        dirName = bundle.getString(ID);
        nameOfAlbum = bundle.getString(NAME);
        pos = bundle.getString(POSITION);

       directory = new File(Environment.getExternalStorageDirectory()+"/Albums/"+dirName);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Image directory created");
        }


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        imageAdapter = new ImageAdapter(this, getImages());

        recyclerView.setAdapter(imageAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(AlbumImagesActivity.this, recyclerView, new ItemCallback() {
            @Override
            public void onItemClick(View view, int p) {
                Image image = imageData.get(p);
                Intent intent = new Intent(AlbumImagesActivity.this, FullScreenActivity.class);

                Bundle arg = new Bundle();
                arg.putString(IMAGE_PATH, image.getImagePath());

                intent.putExtra(IMG_EXTRAS, arg);

                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, final int p) {
                final CharSequence[] items = {"Share", "Delete"};
                AlertDialog.Builder alert = new AlertDialog.Builder(AlbumImagesActivity.this);

                final Image imageItem = imageData.get(p);

                alert.setTitle("Select Action");
                alert.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                JSONParser parser = new JSONParser();
                                File txt = new File(Environment.getExternalStorageDirectory()+"/Albums/"+dirName, nameOfAlbum+dirName+".txt");
                                String lat = null, lng = null;

                                try {
                                    Object object = parser.parse(new FileReader(txt));

                                    JSONObject jsonObject = (JSONObject)object;
                                    lat = (String)jsonObject.get("lat");
                                    lng = (String)jsonObject.get("lng");

                                } catch (IOException e) {

                                    e.printStackTrace();

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "latitude: "+lat+"\nlongitude: "+lng);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageItem.getImagePath()));
                                shareIntent.setType("image/*");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent, "Share via"));
                                break;

                            case 1:
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AlbumImagesActivity.this);
                                alertDialog.setTitle("Are you sure?");
                                alertDialog.setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                               File file = new File(imageItem.getImagePath());
                                                for(int j=0; j<imageData.size(); j++) {
                                                    if(imageItem.getImagePath().equals(imageData.get(j).getImagePath())) {
                                                        file.delete();
                                                        imageData.remove(j);
                                                        imageAdapter.notifyItemRemoved(p);

                                                        RealmResults<Albums> results = realm.where(Albums.class).findAll();
                                                        realm.beginTransaction();

                                                        if(imageData.size() > 0) {
                                                            results.get(Integer.parseInt(pos)).setPath(imageData.get(0).getImagePath());
                                                        } else if(imageData.size() == 0) {
                                                            results.get(Integer.parseInt(pos)).setPath("");
                                                        }

                                                        realm.commitTransaction();
                                                    }
                                                }

                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                }).create().show();
                                break;

                        }
                    }
                }).show();
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Albums/"+dirName);

        if (requestCode == TAKE_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap)data.getExtras().get("data");
            Uri tempUri = getImageUri(AlbumImagesActivity.this, photo);
            photoFile = new File(getRealPathFromURI(tempUri)); //get the actual path

            RealmResults<Albums> results = realm.where(Albums.class).findAll();
            realm.beginTransaction();
            results.get(Integer.parseInt(pos)).setPath(photoFile.getPath());
            realm.commitTransaction();

            try {
                File source = new File(getRealPathFromURI(tempUri));
                File destination = new File(dir, source.getName());

                if(source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    System.out.println("Image saved successfully");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageData.add(new Image(photoFile.getPath()));
            imageAdapter.notifyDataSetChanged();

        }else
        if(requestCode == CHOOSE_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
            c.moveToFirst();
            String picturePath = c.getString( c.getColumnIndex(filePath[0]));

            RealmResults<Albums> results = realm.where(Albums.class).findAll();
            realm.beginTransaction();
            results.get(Integer.parseInt(pos)).setPath(picturePath);
            realm.commitTransaction();

            try {
                File source = new File(picturePath);
                File destination = new File(dir, source.getName());

                if(source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    System.out.println("Image copied successfully");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageData.add(new Image((picturePath)));
            imageAdapter.notifyDataSetChanged();

            c.close();
        }else

        if(requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            place = PlacePicker.getPlace(this, data);
            File txt = new File(Environment.getExternalStorageDirectory()+"/Albums/"+dirName, nameOfAlbum+dirName+".txt");

            String[] coordinates = String.valueOf(place.getLatLng()).split(":");
            String latLng[] = coordinates[1].replaceAll("[()]", "").split(",");
            Toast.makeText(AlbumImagesActivity.this, latLng[0]+"_"+latLng[1], Toast.LENGTH_SHORT).show();

            JSONObject details = new JSONObject();
            details.put("album", nameOfAlbum+"_"+dirName);
            details.put("place", String.valueOf(place.getName()));
            details.put("lat", latLng[0]);
            details.put("lng", latLng[1]);

            try {
                FileWriter file_writer = new FileWriter(txt);
                file_writer.write(details.toJSONString());
                file_writer.flush();
                System.out.println("successful json done");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.take_photo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera:
                openPhotoChooser();
                return true;
            case R.id.location:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AlbumImagesActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openPhotoChooser() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumImagesActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera, TAKE_PHOTO);
                        break;
                    case 1:
                        Intent gallery = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        gallery.setType("image/*");
                        startActivityForResult(gallery, CHOOSE_PHOTO);
                        break;
                    default:
                        dialogInterface.cancel();
                }
            }
        }).show();
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    //    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = AlbumImagesActivity.this.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public List<Image> getImages() {
        File fileNames[] = directory.listFiles();
        if(fileNames != null)
            for(int i=0; i<fileNames.length; i++) {
                if(String.valueOf(fileNames[i]).endsWith(".jpg") || String.valueOf(fileNames[i]).endsWith(".jpeg") || String.valueOf(fileNames[i]).endsWith(".png"))
                    imageData.add(0, new Image(fileNames[i].getPath()));
            }
        return imageData;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    class RecyclerTouchListener implements  RecyclerView.OnItemTouchListener {

        private ItemCallback itemCallback;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ItemCallback itemCallback) {
            this.itemCallback = itemCallback;
            gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent motionEvent) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent motionEvent) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent motionEvent) {
                    View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    if (child != null && itemCallback != null) {
                        itemCallback.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }

                @Override
                public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                    return false;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(),  e.getY());
            if (child != null && itemCallback != null && gestureDetector.onTouchEvent(e)) {
                itemCallback.onItemClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
