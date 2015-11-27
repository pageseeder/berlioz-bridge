package org.pageseeder.berlioz.bridge.auth.spi;

import org.pageseeder.berlioz.bridge.auth.Authenticator;

/**
 * Used to provides a specific permission manager.
 *
 * @author Christophe Lauret
 *
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class PermissionsProvider {

  /**
   * Returns a permission manager for the specified name.
   *
   * @param name
   * @return
   */
  abstract Authenticator<?> authenticatorForName(String name);

}
