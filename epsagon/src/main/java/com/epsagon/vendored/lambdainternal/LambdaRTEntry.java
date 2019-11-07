package com.epsagon.vendored.lambdainternal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LambdaRTEntry {
  private static SimpleDateFormat FORMAT;

  public LambdaRTEntry() {
  }

  public static String logDate() {
    return logDate(System.currentTimeMillis());
  }

  public static String logDate(long millis) {
    if (FORMAT == null) {
      FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
    }

    return FORMAT.format(new Date(millis));
  }

  public static String getEnvOrExit(String envVariableName) {
    String value = System.getenv(envVariableName);
    if (value == null) {
      System.err.println("Could not get environment variable " + envVariableName);
      System.exit(-1);
    }

    return value;
  }

  private static URL newURL(String s) {
    try {
      return new URL("file", (String)null, -1, s);
    } catch (MalformedURLException var2) {
      throw new RuntimeException(var2);
    }
  }

  private static URL[] getJars(File dir, int numPrepend) {
    String[] paths = dir.list();
    boolean[] accepted = new boolean[paths.length];
    int count = 0;

    for(int i = 0; i < paths.length; ++i) {
      if (paths[i].endsWith(".jar")) {
        accepted[i] = true;
        ++count;
      }
    }

    URL[] result = new URL[numPrepend + count];
    int cur = numPrepend;

    for(int i = 0; i < paths.length; ++i) {
      if (accepted[i]) {
        result[cur++] = newURL(dir.getPath() + "/" + paths[i]);
      }
    }

    return result;
  }

  public static URLClassLoader makeClassLoader(String path, ClassLoader parent, boolean includeRootDir) {
    File rootDir = new File(path);
    File libDir = new File(path + "/lib");
    URL url = newURL(rootDir.getPath() + "/");
    URL[] allTheUrls;
    if (includeRootDir) {
      if (libDir.isDirectory()) {
        allTheUrls = getJars(libDir, 1);
        allTheUrls[0] = url;
      } else {
        allTheUrls = new URL[]{url};
      }
    } else {
      allTheUrls = libDir.isDirectory() ? getJars(libDir, 0) : new URL[0];
    }

    return new URLClassLoader(allTheUrls, parent);
  }

}
