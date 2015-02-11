package com.quasar.android.useragent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class UserAgentFactory {
  private static final String NO_VALUE = null;
  private static final String TAG = UserAgentFactory.class.getSimpleName();

  private static final String DEFAULT_WEBKIT_VERSION = "537.36";
  private static final Pattern VERSION_NUMBER = Pattern.compile("\\.\\d$");
  private static final Pattern KIT_KAT_PATCH_NUMBER = Pattern.compile("4\\.4\\.(\\d+)");

  /*
   * Taken from http://jimbergman.net/webkit-version-in-android-version/
   */
  private static final Map<String, String> webKitCodes = new HashMap<String, String>() {{
    put("2.1-update1", "530.17");
    put("2.2", "533.1");
    put("2.2.1", "533.1");
    put("2.2.2", "533.1");
    put("2.2.3", "533.1");
    put("2.3.2", "533.1");
    put("2.3.3", "533.1");
    put("2.3.4", "533.1");
    put("2.3.5", "533.1");
    put("2.3.6", "533.1");
    put("2.3.7", "533.1");
    put("3.2.1", "534.13");
    put("4.0.1", "534.30");
    put("4.0.2", "534.30");
    put("4.0.3", "534.30");
    put("4.0.4", "534.30");
    put("4.1.1", "534.30");
    put("4.1.2", "534.30");
    put("4.2", "534.30");
    put("4.2.1", "534.30");
    put("4.2.2", "534.30");
    put("4.3", "534.30");
    put("4.4", DEFAULT_WEBKIT_VERSION);
    put("5.0", DEFAULT_WEBKIT_VERSION);
  }};

  private static String userAgent;

  @SuppressWarnings("StringEquality")
  public static String createUserAgent(final Context context) {
    if (userAgent == NO_VALUE) {
      final StringBuilder buffer = new StringBuilder();

      addMozillaCompatibility(buffer);
      addOperatingSystem(buffer);
      addWebKitVersion(buffer);
      addChromeVersion(buffer);
      addDeviceType(context, buffer);
      addSafariVersion(buffer);

      userAgent = buffer.toString();
    }

    return userAgent;
  }

  private static void addMozillaCompatibility(final StringBuilder buffer) {
    buffer.append("Mozilla/5.0");
  }

  private static void addOperatingSystem(final StringBuilder buffer) {
    buffer.append(" (Linux; Android ").append(Build.VERSION.RELEASE).append(";");
    buffer.append(" ").append(Build.MODEL);
    buffer.append("; Build/").append(Build.ID);
    buffer.append(")");
  }

  private static void addWebKitVersion(final StringBuilder buffer) {
    buffer.append(" AppleWebKit/");

    try {
      buffer.append(determineWebKitVersion());
      buffer.append(" (");
    }
    catch (CantDetermineWebKitVersionException e) {
      buffer.append(DEFAULT_WEBKIT_VERSION).append(" (default, ");
    }

    buffer.append("KHTML, like Gecko) Version/4.0");
  }

  private static void addChromeVersion(final StringBuilder buffer) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      buffer.append(" Chrome/");

      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
        final int patchNumber = parsePatchNumber();

        if (patchNumber >= 3) {
          buffer.append("33.0.0.0");
        }
        else {
          buffer.append("30.0.0.0");
        }
      }

      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
        buffer.append("36.0.0.0");
      }
    }
  }

  private static void addDeviceType(final Context context, final StringBuilder buffer) {
    if (!context.getResources().getBoolean(R.bool.isTablet)) {
      buffer.append(" Mobile");
    }
  }

  private static void addSafariVersion(final StringBuilder buffer) {
    buffer.append(" Safari/");

    try {
      buffer.append(determineWebKitVersion());
    }
    catch (CantDetermineWebKitVersionException e) {
      buffer.append(DEFAULT_WEBKIT_VERSION).append(" (default)");
    }
  }

  @SuppressWarnings("StringEquality")
  private static String determineWebKitVersion() throws CantDetermineWebKitVersionException {
    final String webKitVersion = getWebKitVersion(Build.VERSION.RELEASE);

    if (webKitVersion != NO_VALUE) {
      return webKitVersion;
    }

    throw new CantDetermineWebKitVersionException();
  }

  @SuppressWarnings("StringEquality")
  private static String getWebKitVersion(final String version) {
    final String webKitVersion = webKitCodes.get(version);

    if (webKitVersion == NO_VALUE && !version.matches("\\d+")) {
      final Matcher matcher = VERSION_NUMBER.matcher(version);

      return getWebKitVersion(matcher.replaceAll(""));
    }

    return webKitVersion;
  }

  private static int parsePatchNumber() {
    final Matcher matcher = KIT_KAT_PATCH_NUMBER.matcher(Build.VERSION.RELEASE);

    if (matcher.matches()) {
      return Integer.valueOf(matcher.group(1));
    }
    else {
      Log.w(TAG, "Can't determine Kit Kat patch version");

      return 0;
    }
  }

  private static class CantDetermineWebKitVersionException extends Exception {
  }
}
