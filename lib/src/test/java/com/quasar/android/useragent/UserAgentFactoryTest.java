package com.quasar.android.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;
import android.test.InstrumentationTestCase;

/*
 * For Chrome version for 4.4+ devices see
 *   - https://developer.chrome.com/multidevice/user-agent
 *   - https://developer.chrome.com/multidevice/webview/overview
 */
public class UserAgentFactoryTest extends InstrumentationTestCase {
  private static final String WEB_KIT_PATTERN = "^.*?AppleWebKit/(\\d{3}\\.\\d{2}).*$";
  private static final String SAFARI_PATTERN = "^.*?Version/4\\.0.*?Safari/(\\d{3}\\.\\d{2}).*$";

  private String userAgent;

  public void setUp() throws Exception {
    super.setUp();

    userAgent = UserAgentFactory.createUserAgent(getInstrumentation().getTargetContext());
  }

  public void testShouldContainMozillaBackwardsCompatibilityInUserAgent() {
    assertTrue("Missing Mozilla compatibility", userAgent.contains("Mozilla/5.0"));
  }

  public void testShouldContainOSVersionInUserAgent() {
    assertTrue("Missing OS version", userAgent.contains("Android " + Build.VERSION.RELEASE));
  }

  public void testShouldContainDeviceModelInUserAgent() {
    assertTrue("Missing device model", userAgent.contains(Build.MODEL));
  }

  public void testShouldContainBuildTagInUserAgent() {
    assertTrue("Missing build tag", userAgent.contains("Build/" + Build.ID));
  }

  public void testShouldContainOSBuildVersionInUserAgent() {
    assertTrue("Missing build version", userAgent.contains(""));
  }

  public void testShouldContainAppleWebKitVersionInUserAgent() {
    assertTrue("Missing web kit version", userAgent.matches(WEB_KIT_PATTERN));
  }

  public void testShouldBeKHTMLCompatible() {
    assertTrue("Not KHTML compatible", userAgent.contains("(KHTML, like Gecko)"));
  }

  public void testShouldContainSafariVersionNumber() {
    assertTrue("Missing Safari version number", userAgent.matches(SAFARI_PATTERN));
  }

  public void testShouldHaveSameVersionForWebKitAndSafari() {
    final String userAgent = this.userAgent;

    final String webkitVersion = parseVersionNumber(userAgent, WEB_KIT_PATTERN);
    final String safariVersion = parseVersionNumber(userAgent, SAFARI_PATTERN);

    assertEquals("WebKit and Safari versions not the same", webkitVersion, safariVersion);
  }

  public void testShouldContainMobileKeywordInUserAgentForPhone() {
    final boolean isTablet = getInstrumentation().getTargetContext().getResources().getBoolean(R.bool.isTablet);
    final String phoneKeyword = "Mobile";

    if (isTablet) {
      assertFalse("Phone keyword present", userAgent.contains(phoneKeyword));
    }
    else {
      assertTrue("Missing phone keyword", userAgent.contains(phoneKeyword));
    }
  }

  public void testShouldContainCorrectChromeVersionForKitKatDevices() {
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
      final String chromeVersion = isRunningOn443OrHigher() ? "33.0.0.0" : "30.0.0.0";

      assertTrue("Chrome version missing", userAgent.contains("Chrome/" + chromeVersion));
    }
  }

  public void testShouldContainCorrectChromeVersionForLollipopDevices() {
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
      final String chromeVersion = "36.0.0.0";

      assertTrue("Chrome version missing", userAgent.contains("Chrome/" + chromeVersion));
    }
  }

  private String parseVersionNumber(final String userAgent, final String pattern) {
    Matcher matcher = Pattern.compile(pattern).matcher(userAgent);

    if (!matcher.matches()) {
      fail("Didn't find version number");
    }

    return matcher.group(1);
  }

  private boolean isRunningOn443OrHigher() {
    // Are we running on 4.4.3 or higher?
    final Matcher matcher = Pattern.compile("\\d+\\.\\d+\\.(\\d+)").matcher(Build.VERSION.RELEASE);

    if (matcher.matches()) {
      final int patchVersion = Integer.valueOf(matcher.group(1));

      if (patchVersion >= 3) {
        return true;
      }
    }

    return false;
  }
}