package com.bc.geocoin.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

public class ZipUtil {

	public static byte[] compress(String str) throws Exception {
        if (str == null || str.length() == 0) {
            return new byte[0];
        }
        Log.d("ZipUtil", "String length : " + str.length());
        ByteArrayOutputStream obj=new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        String outStr = obj.toString("UTF-8");
        Log.d("ZipUtil", "Output String length : " + outStr.length());
        return obj.toByteArray();
     }

      public static String decompress(byte[] bytes) throws Exception {
        if (bytes == null || bytes.equals(null)) {
            return "";
        }
        Log.d("ZipUtil", "Input String length : " + bytes.length);
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String outStr = "";
        String line;
        while ((line=bf.readLine())!=null) {
          outStr += line;
        }
        Log.d("ZipUtil", "Output String lenght : " + outStr.length());
        return outStr;
     }
	
}
