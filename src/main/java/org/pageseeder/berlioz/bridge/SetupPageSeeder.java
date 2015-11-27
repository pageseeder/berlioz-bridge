/*
 * Copyright (c) 1999-2014 allette systems pty. ltd.
 */
package org.pageseeder.berlioz.bridge;

import java.io.File;
import java.io.IOException;

import org.pageseeder.berlioz.BerliozException;
import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.bridge.setup.Setup;
import org.pageseeder.berlioz.bridge.setup.SetupException;
import org.pageseeder.berlioz.content.ContentGenerator;
import org.pageseeder.berlioz.content.ContentRequest;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Executes the PageSeeder setup script located in <code>WEB-INF/setup/setup.xml</code>.
 *
 * @author Christophe Lauret
 * 
 * @version 0.1.4
 * @since 0.1.4
 */
public final class SetupPageSeeder implements ContentGenerator {

  @Override
  public void process(ContentRequest req, XMLWriter xml) throws BerliozException, IOException {
    boolean simulate = "true".equals(req.getParameter("simulate"));
    try {
      File r = GlobalSettings.getRepository();
      File config = new File(r, "setup/setup.xml");
      Setup setup = Setup.parse(config);
      if (simulate) {
        setup.simulate(xml);
      } else {
        setup.execute(xml);
      }
    } catch (SetupException ex) {
      throw new BerliozException("Unable to setup application", ex);
    }
  }

}
