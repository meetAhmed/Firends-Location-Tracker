package example.api.location.google;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import example.api.location.google.preferenceManager.preferenceManager;

public class storeRoom {

    public static String IMAGES_ROOT_DIR = "/FriendsLocatorApp/RawData";

    public static String getImagesRootDir(String ref) {
        File dir = null;
        try {
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGES_ROOT_DIR + "/" + ref + ".cached_no_extension");
            if (dir.exists()) {
                Log.i("1232312", "Exists 1");
                return dir + "";
            } else {
                Log.i("1232312", "Exists 2");
                return Environment.getExternalStorageDirectory() + IMAGES_ROOT_DIR + "/" + ref + ".cached_no_extension" + "";
            }
        } catch (Exception e) {
            Log.i("1232312", e.getMessage());
            return null;
        }
    }

    public static String getDay(long x) {
        long y = 1000 * x;
        Date d = new Date(y);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        return simpleDateFormat.format(d);
    }

    public static String getDate(long data) {
        long CurrentDate = 1000 * data;
        Date date = new Date(data);
        Format format = new SimpleDateFormat("EEEE d MMMM yyyy  h:mm a");
        return format.format(date);
    }

    public static String getDateForNotification(long data) {
        long CurrentDate = 1000 * data;
        Date date = new Date(data);
        Format format = new SimpleDateFormat("d/MMMM/yyyy  h:mm a");
        return format.format(date);
    }

    public static String getTime(long x) {
        long current = 1000 * x;
        Date date = new Date(current);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h a");
        return simpleDateFormat.format(date);
    }

    public static ArrayList<LatLng> reverseList(ArrayList<LatLng> coordList) {
        ArrayList<LatLng> cord = new ArrayList<>();
        for (int i = coordList.size() - 1; i > 0; i--) {
            cord.add(coordList.get(i));
        }
        return cord;
    }

    public static String getUserLocAddress(Double lat, Double lon, Context context) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addressList;
            addressList = geocoder.getFromLocation(lat, lon, 1);
            if (addressList.size() > 0) {
                final String address = addressList.get(0).getAddressLine(0);
                return address;
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }//ends here

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;
            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    } // function ends here

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Boolean moveImageToLoc(String ref, Bitmap bitmap) {
        File rootDir = null;
        try {
            FileOutputStream outStream = null;

            File sdCard = Environment.getExternalStorageDirectory();
            rootDir = new File(sdCard.getAbsolutePath() + IMAGES_ROOT_DIR);
            boolean check = rootDir.mkdirs();
            if (!check) {
                rootDir = new File(Environment.getExternalStorageDirectory(), IMAGES_ROOT_DIR);
            }
            String fileName = String.format("%s.cached_no_extension", ref);
            File outFile = new File(rootDir, fileName);
            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } // catch ends here
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static String getFileType(Uri uri, Context context) {
        ContentResolver c = context.getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(c.getType(uri));
    } // method for getting the type of file

    public static void createImageFile(byte[] bytes, String ref) {
        File rootDir = null;
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            rootDir = new File(sdCard.getAbsolutePath() + IMAGES_ROOT_DIR);
            boolean check = rootDir.mkdirs();
            if (!check) {
                rootDir = new File(Environment.getExternalStorageDirectory(), IMAGES_ROOT_DIR);
            }
            String fileName = String.format("%s.cached_no_extension", ref);
            File f = new File(rootDir, fileName);
            if (!f.exists()) {
                check = f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes);
                fo.close();
                Log.i("12323", ref + " File created ( " + check + " )");
            } else {
                Log.i("12323", "File exists ( " + ref + " )");
            }
        } catch (IOException e) {
            Log.i("12323", e.getMessage());
            e.printStackTrace();
        }
    }// ends here

    public static Boolean isImageFileExit(String ref) {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + IMAGES_ROOT_DIR);
            dir.mkdirs();
            String fileName = String.format("%s.cached_no_extension", ref);
            File f = new File(dir, fileName);
            return f.exists();
        } catch (Exception e) {
            return false;
        }
    }// ends here

    public static void sentLocToServer(Double lat, Double lon, Context context) {
        preferenceManager.writeLocation(context, String.valueOf(lat), String.valueOf(lon));
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("user-Locations").child(preferenceManager.getkey(context));
        model objMode = new model(lat, lon, System.currentTimeMillis());
        String key = firebaseDatabase.push().getKey();
        firebaseDatabase.child(key).setValue(objMode);
    }

}
