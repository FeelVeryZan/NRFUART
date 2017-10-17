package com.example.Zan.nrfuart;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Locale;

/**
 * Created by 恒 on 2016/12/2.
 */
public class Util {
    public static final String TAG = "Util";
    public static int LENGTH = 10;
    public static int count = 0;
    public static byte[] buffer = new byte[LENGTH];
    public static short ch1, ch2, ch3, ch4;
    public static int index = 0;

    private final static String mHexStr = "0123456789ABCDEF";

    public static int add(byte v) {
        buffer[index] = v;
        index = (index + 1) % LENGTH;
        count++;
        if (count >= LENGTH) {
            short crc = (short) (buffer[(LENGTH + index - 1) % LENGTH] << 8);
            crc |= (0x000000FF & buffer[(LENGTH + index - 2) % LENGTH]);


            if (crc == CRC16(buffer, (LENGTH + index - 10) % LENGTH, 8, LENGTH)) {
                ch1 = (short) (buffer[(LENGTH + index - 9) % LENGTH] << 8);
                ch1 |= (0x000000FF & buffer[(LENGTH + index - 10) % LENGTH]);
                ch2 = (short) (buffer[(LENGTH + index - 7) % LENGTH] << 8);
                ch2 |= (0x000000FF & buffer[(LENGTH + index - 8) % LENGTH]);
                ch3 = (short) (buffer[(LENGTH + index - 5) % LENGTH] << 8);
                ch3 |= (0x000000FF & buffer[(LENGTH + index - 6) % LENGTH]);
                ch4 = (short) (buffer[(LENGTH + index - 3) % LENGTH] << 8);
                ch4 |= (0x000000FF & buffer[(LENGTH + index - 4) % LENGTH]);
                count = 0;
                index = 0;
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public static short CRC16(byte[] bytes, int start, int cnt, int length) {
        int crc = 0x0000FFFF;
        for (int i = 0; i < cnt; i++) {
            crc ^= (0x000000FF & bytes[(start + i) % length]);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x01) != 0) {
                    crc = (crc >>> 1) ^ 0x0000A001;
                } else {
                    crc = (crc >>> 1);
                }
            }
        }
        return (short) (crc & 0x0000FFFF);
    }

    public static int CRC16_2(byte[] bytes, int start, int cnt, int length) {
        int crc = 0x0000;
        for (int i = 0; i < cnt; i++) {
            crc = (crc >>> 8) ^ table[(crc ^ bytes[(start + i) % length]) & 0xff];
        }
        return crc;
    }


    public static short getCrc16(byte[] data, int start, int cnt, int length) {
        int crc = 0;
        for (int i = 0; i < cnt; i++) {
            crc = ((crc & 0xFF) << 8) ^ table[(((crc & 0xFF00) >> 8) ^ data[(start + i) % length]) & 0xFF];
        }
        crc = crc & 0xFFFF;
        return (short) crc;
    }

    public static short[] table = {
            (short) 0x0000, (short) 0xC0C1, (short) 0xC181, (short) 0x0140, (short) 0xC301, (short) 0x03C0, (short) 0x0280, (short) 0xC241,
            (short) 0xC601, (short) 0x06C0, (short) 0x0780, (short) 0xC741, (short) 0x0500, (short) 0xC5C1, (short) 0xC481, (short) 0x0440,
            (short) 0xCC01, (short) 0x0CC0, (short) 0x0D80, (short) 0xCD41, (short) 0x0F00, (short) 0xCFC1, (short) 0xCE81, (short) 0x0E40,
            (short) 0x0A00, (short) 0xCAC1, (short) 0xCB81, (short) 0x0B40, (short) 0xC901, (short) 0x09C0, (short) 0x0880, (short) 0xC841,
            (short) 0xD801, (short) 0x18C0, (short) 0x1980, (short) 0xD941, (short) 0x1B00, (short) 0xDBC1, (short) 0xDA81, (short) 0x1A40,
            (short) 0x1E00, (short) 0xDEC1, (short) 0xDF81, (short) 0x1F40, (short) 0xDD01, (short) 0x1DC0, (short) 0x1C80, (short) 0xDC41,
            (short) 0x1400, (short) 0xD4C1, (short) 0xD581, (short) 0x1540, (short) 0xD701, (short) 0x17C0, (short) 0x1680, (short) 0xD641,
            (short) 0xD201, (short) 0x12C0, (short) 0x1380, (short) 0xD341, (short) 0x1100, (short) 0xD1C1, (short) 0xD081, (short) 0x1040,
            (short) 0xF001, (short) 0x30C0, (short) 0x3180, (short) 0xF141, (short) 0x3300, (short) 0xF3C1, (short) 0xF281, (short) 0x3240,
            (short) 0x3600, (short) 0xF6C1, (short) 0xF781, (short) 0x3740, (short) 0xF501, (short) 0x35C0, (short) 0x3480, (short) 0xF441,
            (short) 0x3C00, (short) 0xFCC1, (short) 0xFD81, (short) 0x3D40, (short) 0xFF01, (short) 0x3FC0, (short) 0x3E80, (short) 0xFE41,
            (short) 0xFA01, (short) 0x3AC0, (short) 0x3B80, (short) 0xFB41, (short) 0x3900, (short) 0xF9C1, (short) 0xF881, (short) 0x3840,
            (short) 0x2800, (short) 0xE8C1, (short) 0xE981, (short) 0x2940, (short) 0xEB01, (short) 0x2BC0, (short) 0x2A80, (short) 0xEA41,
            (short) 0xEE01, (short) 0x2EC0, (short) 0x2F80, (short) 0xEF41, (short) 0x2D00, (short) 0xEDC1, (short) 0xEC81, (short) 0x2C40,
            (short) 0xE401, (short) 0x24C0, (short) 0x2580, (short) 0xE541, (short) 0x2700, (short) 0xE7C1, (short) 0xE681, (short) 0x2640,
            (short) 0x2200, (short) 0xE2C1, (short) 0xE381, (short) 0x2340, (short) 0xE101, (short) 0x21C0, (short) 0x2080, (short) 0xE041,
            (short) 0xA001, (short) 0x60C0, (short) 0x6180, (short) 0xA141, (short) 0x6300, (short) 0xA3C1, (short) 0xA281, (short) 0x6240,
            (short) 0x6600, (short) 0xA6C1, (short) 0xA781, (short) 0x6740, (short) 0xA501, (short) 0x65C0, (short) 0x6480, (short) 0xA441,
            (short) 0x6C00, (short) 0xACC1, (short) 0xAD81, (short) 0x6D40, (short) 0xAF01, (short) 0x6FC0, (short) 0x6E80, (short) 0xAE41,
            (short) 0xAA01, (short) 0x6AC0, (short) 0x6B80, (short) 0xAB41, (short) 0x6900, (short) 0xA9C1, (short) 0xA881, (short) 0x6840,
            (short) 0x7800, (short) 0xB8C1, (short) 0xB981, (short) 0x7940, (short) 0xBB01, (short) 0x7BC0, (short) 0x7A80, (short) 0xBA41,
            (short) 0xBE01, (short) 0x7EC0, (short) 0x7F80, (short) 0xBF41, (short) 0x7D00, (short) 0xBDC1, (short) 0xBC81, (short) 0x7C40,
            (short) 0xB401, (short) 0x74C0, (short) 0x7580, (short) 0xB541, (short) 0x7700, (short) 0xB7C1, (short) 0xB681, (short) 0x7640,
            (short) 0x7200, (short) 0xB2C1, (short) 0xB381, (short) 0x7340, (short) 0xB101, (short) 0x71C0, (short) 0x7080, (short) 0xB041,
            (short) 0x5000, (short) 0x90C1, (short) 0x9181, (short) 0x5140, (short) 0x9301, (short) 0x53C0, (short) 0x5280, (short) 0x9241,
            (short) 0x9601, (short) 0x56C0, (short) 0x5780, (short) 0x9741, (short) 0x5500, (short) 0x95C1, (short) 0x9481, (short) 0x5440,
            (short) 0x9C01, (short) 0x5CC0, (short) 0x5D80, (short) 0x9D41, (short) 0x5F00, (short) 0x9FC1, (short) 0x9E81, (short) 0x5E40,
            (short) 0x5A00, (short) 0x9AC1, (short) 0x9B81, (short) 0x5B40, (short) 0x9901, (short) 0x59C0, (short) 0x5880, (short) 0x9841,
            (short) 0x8801, (short) 0x48C0, (short) 0x4980, (short) 0x8941, (short) 0x4B00, (short) 0x8BC1, (short) 0x8A81, (short) 0x4A40,
            (short) 0x4E00, (short) 0x8EC1, (short) 0x8F81, (short) 0x4F40, (short) 0x8D01, (short) 0x4DC0, (short) 0x4C80, (short) 0x8C41,
            (short) 0x4400, (short) 0x84C1, (short) 0x8581, (short) 0x4540, (short) 0x8701, (short) 0x47C0, (short) 0x4680, (short) 0x8641,
            (short) 0x8201, (short) 0x42C0, (short) 0x4380, (short) 0x8341, (short) 0x4100, (short) 0x81C1, (short) 0x8081, (short) 0x4040,
    };

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static Properties loadConfig(Context context, String file) {
        Properties props = new Properties();
        File f = new File(file);
        if (!f.exists()) {
            return null;
        }
        try {
            FileInputStream fs = new FileInputStream(file);
            props.load(fs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    public static boolean isPathExist(String path) {
        String[] paths = path.split("\\\\");
        StringBuffer fullpath = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            fullpath.append(paths[i]).append("\\\\");
            File file = new File(fullpath.toString());
            if (!file.exists()) {
                file.mkdir();
            }
        }
        return true;
    }

    public static boolean saveConfig(Context context, String file, Properties props) {
        try {
            File f = new File(file);
            if (!f.exists()) {
                f.createNewFile();
                FileOutputStream fs = new FileOutputStream(f);
                props.store(fs, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
    public static byte[] hexStr2byte(String src){
        /*对输入值进行规范化整理*/
        src = src.trim().replace(" ", "").toUpperCase(Locale.US);
        //处理值初始化
        int m=0,n=0;
        int iLen=src.length()/2; //计算长度
        byte[] ret = new byte[iLen]; //分配存储空间

        for (int i = 0; i < iLen; i++){
            m=i*2+1;
            n=m+1;
            ret[i] = (byte)(Integer.decode("0x"+ src.substring(i*2, m) + src.substring(m,n)) & 0xFF);
        }
        return ret;
    }

}
