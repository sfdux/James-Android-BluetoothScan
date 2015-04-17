package bluetooth.aglogica.com.bluetoothtool;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by James.Shi on 1/8/15.
 */
public class SystemUtil {

    /**
     * Check string is null or empty
     *
     * @param value
     * @return boolean
     */
    public static boolean isStringNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    public static void writeDeviceBinaryToFile(final String folderPath, final String fileName, byte[] bytes, String extension) {
        String filePath = String.format("%1$s/%2$s.%3$s", folderPath, fileName, extension);
        writeDeviceBinaryToFile(filePath, bytes);
    }

    /**
     * Writes binary data to the file
     *
     * @param filePath The file path.
     * @param bytes    The byte array to write.
     */
    private static void writeDeviceBinaryToFile(String filePath, byte[] bytes) {
        File file;
        OutputStream output = null;

        try {
            file = new File(filePath);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (file.getParentFile().isDirectory() && !file.exists()) {
                file.createNewFile();
            }

            if (!file.canWrite()) {
                file.setWritable(true);
            }

            try {
                output = new BufferedOutputStream(new FileOutputStream(filePath, true));
                output.write(bytes);
            } finally {
                output.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static String getBinFolderPath() {
        return String.format("%s%s", Environment.getExternalStorageDirectory(), SystemConst.LOCALFOLDER_NAME);
    }
}
