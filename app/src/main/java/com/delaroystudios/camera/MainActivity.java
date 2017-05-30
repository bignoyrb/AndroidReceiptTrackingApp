package com.delaroystudios.camera;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.ArrayRes;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FileDownloadTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.*;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import java.util.*;
import org.w3c.dom.Text;
import android.app.Activity;
import android.content.SharedPreferences;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileReader;
import static android.provider.Telephony.Mms.Part.FILENAME;



public class MainActivity extends ActionBarActivity {

    private Button btnCamera;
    private Button btnMan;
    private Button btnUpload;
    private Boolean waitLock = false;
    private ImageView capturedImage;
    private TextView imageTextBusiness;
    private TextView imageTextTotal;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "MainActivity";
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCgPuSGLlM4Xo9VtyAecNQ_DOSILq23MKg";
    private ArrayList<String[]> localStore;
    private Context context;
    private String lastImageUpload;
    private PopupWindow popupWindow;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;
    StorageReference imagesRef;
    StorageReference timeRef;
    StorageReference saveRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = storage.getReferenceFromUrl("gs://accordian-c7137.appspot.com");

        timeRef = mStorageRef.child(mAuth.getCurrentUser().getUid()+"/time.txt");
        saveRef = mStorageRef.child(mAuth.getCurrentUser().getUid()+"/save.txt");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                }
                // ...
            }
        };
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnMan = (Button) findViewById(R.id.btnMan);

        //imageText = (TextView) findViewById(R.id.imageText);
        //capturedImage = (ImageView) findViewById(R.id.capturedImage);
        btnCamera.setTypeface(font);
        btnMan.setTypeface(font);



        relativeLayout = (RelativeLayout) findViewById(R.id.relative);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Pop Up Open");
                layoutInflater = (LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.add_man,null);

                popupWindow = new PopupWindow(container, 1000,1000,true);
                popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY,50,300);

                btnUpload = (Button) container.findViewById(R.id.btnUpload);

                btnUpload.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View popupView) {
                        popupWindow.dismiss();
                    }
                });


            }

        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //localStore = populateStore(context);
        getCloudTimeStamp();
    }


    private void replaceLocalStore(byte[] bytes) {
        ArrayList<String[]> temp = new ArrayList<>();
        File file = new File(context.getFilesDir(), "localStore.txt");
        file.delete();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream (new File(file.getAbsolutePath().toString()), true);

            outputStream.write(bytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                temp.add(line.split(",###,"));
            }
            br.close();

        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        localStore = temp;

    }
    private void resolveSaveConflict(String cloudtime, String localtime) {
        //promp user for which they want

        //if cloud chosen
        downloadFirebaseStore();

        //if local chosen
        //useLocalStore();
    }
    private void useLocalStore(){
        File file = new File(context.getFilesDir(), "localStore.txt");
        ArrayList<String[]> temp = new ArrayList<>();
        if(file.exists()) {
            try {

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    temp.add(line.split(",###,"));
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
            }
            Toast.makeText(MainActivity.this, "Loaded Local Store", Toast.LENGTH_SHORT).show();
        }
        else {
            FileOutputStream outputStream;
            try {
                String s = "";
                outputStream = new FileOutputStream(new File(file.getAbsolutePath().toString()), true);
                outputStream.write(s.getBytes());
                outputStream.close();
                uploadFileToFirebase(file);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(MainActivity.this, "Created Local Store", Toast.LENGTH_SHORT).show();
        }
        localStore=temp;
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Bitmap bp = (Bitmap) data.getExtras().get("data");

            uploadToFirebase(bp);


            //Create popup
            Log.d(TAG, "Pop Up Open");
            layoutInflater = (LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.add_man,null);

            capturedImage = (ImageView) container.findViewById(R.id.capturedImage);
            imageTextBusiness = (TextView) container.findViewById(R.id.imageTextBusiness);
            imageTextTotal = (TextView) container.findViewById(R.id.imageTextTotal);
            capturedImage.setImageBitmap(bp);
            btnUpload = (Button) container.findViewById(R.id.btnUpload);


            popupWindow = new PopupWindow(container, 1000,1300,true);
            popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY,50,300);

            btnUpload.setOnClickListener(new View.OnClickListener() {

                public void onClick(View popupView) {
                    popupWindow.dismiss();
                }
            });

            try {
                callCloudVision(bp);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Annotating Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
    private void uploadTimestampToFirebase(String time) {
        byte[] data =  time.getBytes();

        UploadTask uploadTask = timeRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG,downloadUrl.toString());
            }
        });
    }
    private void getCloudTimeStamp() {
        final long ONE_MEGABYTE = 1024 * 1024;
        timeRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Toast.makeText(MainActivity.this, "CT Exists", Toast.LENGTH_SHORT).show();
                String cloudTime = bytes.toString();
                SharedPreferences sharedPref = context.getSharedPreferences("Time",Context.MODE_PRIVATE);

                String localTime = sharedPref.getString("LASTUPTIME","DEF");
                if(cloudTime.equalsIgnoreCase(localTime)) {
                    useLocalStore();
                } else {
                    resolveSaveConflict(cloudTime, localTime);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "No Hash", Toast.LENGTH_SHORT).show();
                useLocalStore();
            }
        });
    }
    private void uploadFileToFirebase(File file) {
        Uri ufile = Uri.fromFile(file);
        UploadTask uploadTask = saveRef.putFile(ufile);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG,downloadUrl.toString());
            }
        });
    }
    private void uploadToFirebase(Bitmap bp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        lastImageUpload = mAuth.getCurrentUser().getUid()+"/"+Long.toHexString(System.currentTimeMillis())+".jpg";
        imagesRef = mStorageRef.child(lastImageUpload);
        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                //File dir = cw.getDir("", Context.MODE_PRIVATE);
                File imageLinkList = new File("URLList.txt");
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
//
//                FileOutputStream fos = new FileOutputStream(imageLinkList);
//                fos.write(downloadUrl.getPath().getBytes());
//                fos.close()
//
            }
        });

    }
    private void downloadFirebaseStore() {

        final long ONE_MEGABYTE = 1024 * 1024;
        saveRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                replaceLocalStore(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    /*    saveRef.getFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                initLocalStore(pass);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });*/



    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        Toast.makeText(MainActivity.this, "Annotating Image", Toast.LENGTH_SHORT).show();

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, ArrayList<String>>() {
            @Override
            protected ArrayList<String> doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature textDetection = new Feature();
                            textDetection.setType("TEXT_DETECTION");
                            textDetection.setMaxResults(10);
                            add(textDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    return convertResponseToStringArr(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }

                return new ArrayList<String>();
            }

            protected void onPostExecute(ArrayList<String> result) {
                String[] dr = deriveInfo(result);
                //imageTextBusiness.setText("Business: "+dr[0]+"\nTotal: "+dr[1]);

                imageTextBusiness.setText("Business: "+dr[0]);
                imageTextTotal.setText("Total: "+dr[1]);
                updateLocalStore(lastImageUpload,dr[0],dr[1],"Gas");
            }
        }.execute();
    }

    // here
    private void updateLocalStore(String lastUploadPath,String business,String total, String category){
        String[] temp = {lastUploadPath,business,total,category};
        localStore.add(temp);
        updateLocalCloudFileStore();
    }
    private void updateLocalCloudFileStore() {
        String build = "";
        for(String[] line : localStore) {
            for(int i=0;i<line.length;i++) {
                build+=line[i];
                if(i!=line.length-1){
                    build+=",###,";
                }
            }
            build+="\n";
        }

        File file = new File(context.getFilesDir(), "localStore.txt");
        file.delete();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream (new File(file.getAbsolutePath().toString()), true);

            outputStream.write(build.getBytes());
            outputStream.close();
            uploadFileToFirebase(file);
            String currTime = getCurrentTimeStamp();

            SharedPreferences sharedPref = context.getSharedPreferences("Time",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("LASTUPTIME",currTime);
            editor.apply();
            uploadTimestampToFirebase(currTime);


        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Updated Local/Cloud Store", Toast.LENGTH_SHORT).show();



    }
    private String[] deriveInfo(ArrayList<String> result) { //terrible first try
        String[] derivedResult = new String[2];

        derivedResult[0]=result.get(1);
        derivedResult[1]="$";
        for(int i=0;i<result.size();i++) {
            Log.d(TAG,result.get(i));
            if(result.get(i).equalsIgnoreCase("TOTAL")){
                if(result.get(i+1).startsWith("$")) {
                    derivedResult[1] = result.get(i+1);
                }
                else if(result.get(i+1).startsWith("8")) {
                    derivedResult[1] = '$' + result.get(i+1).substring(1);
                }
                else if(result.get(i-1).startsWith("$")) {
                    derivedResult[1] = result.get(i+1);
                }
                else if (result.get(i-1).startsWith("8")) {
                    derivedResult[1] = '$' + result.get(i+1).substring(1);
                }
                else{
                    derivedResult[1]=result.get(i+1);
                    for(int j=i+1;j<result.size();j++) {
                        if(result.get(j).startsWith("$")) {
                            derivedResult[1]=result.get(j);
                            break;
                        }
                        else if (result.get(j).startsWith("8")) {
                            derivedResult[1] = '$' + result.get(j).substring(1);
                            break;
                        }
                    }
                }
                break;
            }
        }
        if(derivedResult[1]=="$"){
            for(String x : result) {
                if(x.startsWith("$")){
                    derivedResult[1]=x;
                }
            }
        }
        if(derivedResult[1]=="$"){
            for(String x : result) {
                if(x.contains("$")){
                    derivedResult[1]=x;
                }
            }
        }
        return derivedResult;
    }
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                message += String.format("%s", text.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }

    private ArrayList<String> convertResponseToStringArr(BatchAnnotateImagesResponse response) {
        ArrayList<String> textArr = new ArrayList<String>();
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                textArr.add(String.format("%s", text.getDescription()));
            }
        }
        return textArr;
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_home) {
            //mAuth.signOut();
            Intent i = new Intent(MainActivity.this, PickActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
