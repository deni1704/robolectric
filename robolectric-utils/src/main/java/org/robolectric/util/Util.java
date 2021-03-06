package org.robolectric.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic collection of utility methods.
 */
public class Util {
  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8196];
    int len;
    try {
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
    } finally {
      in.close();
    }
  }

  /**
   * This method consumes an input stream and returns its content.
   *
   * @param is The input stream to read from.
   * @return The bytes read from the stream.
   * @throws IOException Error reading from stream.
   */
  public static byte[] readBytes(InputStream is) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available())) {
      copy(is, bos);
      return bos.toByteArray();
    }
  }

  public static <T> T[] reverse(T[] array) {
    for (int i = 0; i < array.length / 2; i++) {
      int destI = array.length - i - 1;
      T o = array[destI];
      array[destI] = array[i];
      array[i] = o;
    }
    return array;
  }

  public static File file(String... pathParts) {
    return file(new File("."), pathParts);
  }

  public static File file(File f, String... pathParts) {
    for (String pathPart : pathParts) {
      f = new File(f, pathPart);
    }
    return f;
  }

  public static URL url(String path) throws MalformedURLException {
    //Starts with double backslash, is likely a UNC path
    if(path.startsWith("\\\\")) {
      path = path.replace("\\", "/");
    }
    return new URL("file:/" + (path.startsWith("/") ? "/" + path : path));
  }

  public static List<Integer> intArrayToList(int[] ints) {
    List<Integer> youSuckJava = new ArrayList<>();
    for (int attr1 : ints) {
      youSuckJava.add(attr1);
    }
    return youSuckJava;
  }

  public static int parseInt(String valueFor) {
    if (valueFor.startsWith("0x")) {
      return Integer.parseInt(valueFor.substring(2), 16);
    } else {
      return Integer.parseInt(valueFor, 10);
    }
  }
}
