package com.example.tobi.billingapp2.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.tobi.billingapp2.R;
import com.example.tobi.billingapp2.Utility.CSVFileReader;
import com.example.tobi.billingapp2.Utility.CsvContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //In an Activity
    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "/Gemuese-Files");
    private File pPath;
    private String mChosenFile;
    private static final String FTYPE = ".csv";
    private static final int DIALOG_LOAD_FILE = 1000;
    private static final int DIALOG_DELETE_FILE = 9000;
    private static final int CLEAN_REDUNDANT_FILE = 9999;
    private String pathString;
    private String productString;
    ArrayList<String[]> rowList;
    CsvContent csvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Action Bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.gemuese11));
        background.setTileModeX(Shader.TileMode.MIRROR);
        actionBar.setBackgroundDrawable(background);
        //ActionBarEnd

        isReadStoragePermissionGranted();
        isWriteStoragePermissionGranted();

        ArrayList<String[]> rowList = new ArrayList<>();

        pathString = Environment.getExternalStorageDirectory().toString() + "/Gemuese-Files";
        productString = pathString + "/Products";
        pPath = new File(productString);

        Log.i("Debug: ", Environment.getExternalStorageDirectory().toString());

        loadFileList();
    }


    //Button "Search Files" onClick Method
    public void openExplorer(View view) {
        onCreateDialogOpen(1000);
    }

    //Button "Delete Files" onClick Method
    public void deleteFiles(View view) {
        onCreateDialogOpen(9000);
    }

    //Button "DeleteRedundantFiles" onCLick Method
    public void deleteRedundantFiles(View view) {
        onCreateDialogOpen(9999);
    }

    //Button "Products" onClick Method
    public void productsClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), Products.class);
        startActivity(intent);
    }

    private void loadFileList() {
        try {
            mPath.mkdirs();
            Log.i("Debug: ", "Folder Created");
            pPath.mkdirs();

        } catch (SecurityException e) {
            Log.i("Gemuese-Files: ", "unable to write on the sd card " + e.toString());
        }
        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(FTYPE) || sel.isDirectory();
                }

            };
            mFileList = mPath.list(filter);
        } else {
            mFileList = new String[0];
        }

    }


    protected Dialog onCreateDialogOpen(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if (mFileList == null) {
                    Log.i("Gemuese-Files: ", "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        //load the File
                        try {
                            csvData = new CsvContent();
                            csvData.read(pathString + "/" + mChosenFile);
                            //start CSV_Content
                            Intent intent = new Intent(getApplicationContext(), CSV_Content.class);
                            intent.putExtra("rowList", rowList);
                            intent.putExtra("csvData", csvData);
                            startActivity(intent);
                            finish();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("File Loaded: " + mFileList[which]);
                    }

                });
                break;
            case DIALOG_DELETE_FILE:
                builder.setTitle("Delete selected files");
                if (mFileList == null) {
                    Log.i("Gemuese-Files: ", "Showing file picker before deleting the file list");
                    dialog = builder.create();
                    return dialog;
                }
                final LinkedList<String> deleteFileList = new LinkedList();
                //Need to create CharSequence[] instead of String[]
                List<String> list = Arrays.asList(mFileList);
                CharSequence[] mFileListCS = list.toArray(new CharSequence[list.size()]);

                builder.setMultiChoiceItems(mFileListCS, null, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                        if (isChecked) {
                            deleteFileList.add(mFileList[which]);

                        } else if (!isChecked) {
                            deleteFileList.remove(mFileList[which]);
                        }
                    }
                });

                builder.setPositiveButton("Delete Files", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (String file : deleteFileList) {
                            File toDelete = new File(pathString + "/" + file);
                            Boolean deleted = toDelete.delete();
                            if (!deleted) {
                                Toast.makeText(MainActivity.this, "Could not delete: " + mChosenFile, Toast.LENGTH_SHORT).show();
                            }
                        }
                        loadFileList();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFileList.clear();
                    }
                });
                break;
            case CLEAN_REDUNDANT_FILE:
                builder.setTitle("Clear redundand data -> Just newest file of the day");
                if (mFileList == null) {
                    Log.i("Gemuese-Files: ", "Showing file picker before deleting the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setPositiveButton("Delete Redundant Files", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (String file : mFileList) {
                            String ts_String = file.replace(".csv", "");
                            Timestamp maybeDelete;
                            try {
                                maybeDelete = Timestamp.valueOf(ts_String);
                            } catch (Exception e) {
                                continue;
                            }
                            long timestamp = maybeDelete.getTime();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp);
                            int year = calendar.get(Calendar.YEAR);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            int hours = calendar.get(Calendar.HOUR);
                            int minutes = calendar.get(Calendar.MINUTE);
                            int seconds = calendar.get(Calendar.SECOND);
                            int milliseconds = calendar.get(Calendar.MILLISECOND);
                            int sumTime = (hours*1000 + minutes*100 + seconds*10 + milliseconds);

                            for (String comparedFile : mFileList) {
                                String compared_String = comparedFile.replace(".csv", "");
                                Timestamp compareableTS;
                                try {
                                    compareableTS = Timestamp.valueOf(compared_String);
                                } catch (Exception e) {
                                    continue;
                                }
                                long comparedTimestamp = compareableTS.getTime();
                                Calendar comparedCalendar = Calendar.getInstance();
                                comparedCalendar.setTimeInMillis(comparedTimestamp);
                                int comparedYear = comparedCalendar.get(Calendar.YEAR);
                                int comparedDay = comparedCalendar.get(Calendar.DAY_OF_MONTH);
                                int comparedMonth = comparedCalendar.get(Calendar.MONTH);
                                int comparedHours = comparedCalendar.get(Calendar.HOUR);
                                int comparedMinutes = comparedCalendar.get(Calendar.MINUTE);
                                int comparedSeconds = comparedCalendar.get(Calendar.SECOND);
                                int comparedMilliseconds = comparedCalendar.get(Calendar.MILLISECOND);
                                int comparedSumTime = (comparedHours*1000 + comparedMinutes*100 + comparedSeconds*10 + comparedMilliseconds);

                                if (year == comparedYear && day == comparedDay && month == comparedMonth &&
                                        sumTime < comparedSumTime) {
                                    //Log.i("Delete Debug:", "");
                                    File toDelete = new File(pathString + "/" + file);
                                                Boolean deleted = toDelete.delete();
                                                if (!deleted) {
                                                    Toast.makeText(MainActivity.this, "Could not delete: " + mChosenFile, Toast.LENGTH_SHORT).show();
                                                }
                                    Log.i("Delete Debug: ", toDelete.toString());
                                    break;
                                }
                            }
                        }

                        loadFileList();
                        Toast.makeText(MainActivity.this, "Deleted Redundant Files", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i("Delete Debug: ", "Canceled Delete Redundant");
                    }
                });



        }

        dialog = builder.show();
        return dialog;
    }


    //Methods to check if user has permissions:

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission: ", "Permission is granted1");
                return true;
            } else {

                Log.i("Permission: ", "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.i("Permission: ", "Permission is granted1");
            return true;
        }
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission: ", "Permission is granted2");
                return true;
            } else {

                Log.i("Permission: ", "Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.i("Permission: ", "Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.i("Permission: ", "External storage2");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission: ", "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    Toast.makeText(this, "Click Button again", Toast.LENGTH_SHORT).show();
                } else {
                    System.exit(0);
                }
                break;

            case 3:
                Log.i("Permission: ", "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission: ", "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    Toast.makeText(this, "Click Button again", Toast.LENGTH_SHORT).show();
                } else {
                    System.exit(0);
                }
                break;
        }
    }
}