package org.pageseeder.berlioz.bridge;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.bridge.auth.AuthException;
import org.pageseeder.berlioz.bridge.auth.Authenticator;
import org.pageseeder.berlioz.bridge.config.Configuration;
import org.pageseeder.bridge.PSConfig;

public final class ConfigurationTest {

  @Test
  public void testConfigProvider() {
    File dir = new File("src/test/data");
    GlobalSettings.setRepository(dir);
    GlobalSettings.setMode("local");
    PSConfig manual = PSConfig.newInstance(new Properties());
    PSConfig auto = PSConfig.getDefault();
    System.out.println(manual.getAPIBaseURL());
    System.out.println(auto.getAPIBaseURL());
  }

  @Test
  public void testListAvailableAuthenticators() {
    List<String> available = Configuration.listAvailableAuthenticators();
    Assert.assertNotNull(available);
    Assert.assertFalse(available.isEmpty());
    Assert.assertTrue(available.contains("pageseeder"));
    System.out.println("Available authenticators: "+available);
  }

  @Test
  public void testGetAuthenticator() throws AuthException {
    Authenticator<?> auth = Configuration.getAuthenticator();
    Assert.assertNotNull(auth);
  }

}
