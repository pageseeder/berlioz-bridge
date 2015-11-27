/*
 * Copyright (c) 1999-2014 allette systems pty. ltd.
 */
package org.pageseeder.berlioz.bridge.auth;

import java.io.Serializable;

/**
 * A URL to save.
 *
 * @author Christophe Lauret
 *
 * @version 0.1.0
 * @since 0.1.0
 */
public final class ProtectedRequest implements Serializable {

  /**
   * As per requirement for the {@link Serializable} interface.
   */
  private static final long serialVersionUID = 129183325321391637L;

  /**
   * The protected URL
   */
  private final String _url;

  /**
   * Creates a new protected request.
   *
   * @param url the protected URL to access.
   */
  public ProtectedRequest(String url) {
    this._url =  url;
  }

  /**
   * @return The protected URL to access.
   */
  public String url() {
    return this._url;
  }

  /**
   * @return Same as {@link #url()}
   */
  @Override
  public String toString() {
    return this._url;
  }

}
