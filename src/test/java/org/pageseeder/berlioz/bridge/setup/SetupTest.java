package org.pageseeder.berlioz.bridge.setup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterNSImpl;

public final class SetupTest {

  @Test
  public void testSetup() throws SetupException, IOException {
    File dir = new File("src/test/data");
    GlobalSettings.setRepository(dir);
    GlobalSettings.setMode("local");
    File f = new File("src/test/data/setup/setup.xml");
    Setup setup = Setup.parse(f);
    XMLWriter xml = new XMLWriterNSImpl(new PrintWriter(System.out), true);
    // TODO
//    setup.simulate(xml);
//    xml.flush();
  }
}
