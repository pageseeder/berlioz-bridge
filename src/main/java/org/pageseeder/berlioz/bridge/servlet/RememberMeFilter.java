/*
 * Copyright (c) 2014 Allette Systems
 */
package org.pageseeder.berlioz.bridge.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pageseeder.berlioz.bridge.auth.AuthException;
import org.pageseeder.berlioz.bridge.auth.AuthenticationResult;
import org.pageseeder.berlioz.bridge.auth.Authenticator;
import org.pageseeder.berlioz.bridge.auth.RememberMe;
import org.pageseeder.berlioz.bridge.auth.Sessions;
import org.pageseeder.berlioz.bridge.auth.User;
import org.pageseeder.berlioz.bridge.auth.RememberMe.Credentials;
import org.pageseeder.berlioz.bridge.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RememberMeFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeFilter.class);

  private RememberMe rememberme = new RememberMe();

  @Override
  public void init(FilterConfig config) throws ServletException {
    try {
      ServletContext context = config.getServletContext();
      String path = context.getRealPath("/");
      Path root = new File(path).toPath();
      Path auth = root.resolve("WEB-INF/auth");
      this.rememberme.init(auth);
    } catch (IOException | GeneralSecurityException ex) {
      throw new ServletException(ex);
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    doHttpFilter((HttpServletRequest)req, (HttpServletResponse)res, chain);
  }

  public void doHttpFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      Cookie cookie = this.rememberme.getCookie(cookies);
      if (cookie != null) {
        HttpSession session = req.getSession();

        // Only check for stored credentials if no session
        if (session == null || session.getAttribute(Sessions.REQUEST_ATTRIBUTE) == null) {
          Credentials credentials = this.rememberme.getCredentials(cookie);

          // If some credentials are available try to login
          if (credentials != null) {
            LOGGER.info("Found credentials for {}", credentials.username());

            try {
              Authenticator<? extends User> authenticator = Configuration.getAuthenticator();
              AuthenticationResult result = authenticator.login(new ProxyRequest(req, credentials));
              if (result == AuthenticationResult.INCORRECT_DETAILS
               || result == AuthenticationResult.INSUFFICIENT_DETAILS) {
                LOGGER.info("Invalidating credentials for {}", credentials.username());
//                cookie.setValue("");
                cookie.setMaxAge(0);
                res.addCookie(cookie);
              }
            } catch (AuthException ex) {
              new ServletException(ex);
            }
          }
        }

        String path = req.getServletPath();
        if ("/logout.html".equals(path)) {
          LOGGER.info("Removing cookie");
//          cookie.setValue("");
          cookie.setMaxAge(0);
          res.addCookie(cookie);
        }

      }
    }

    // If rememberme was specified
    boolean rememberme = "true".equals(req.getParameter("rememberme"));
    if (rememberme) {
      String username = req.getParameter("username");
      String password = req.getParameter("password");
      if (username != null && password != null) {
        Credentials credentials = new Credentials(username, password);
        Cookie cookie = this.rememberme.newCookie(credentials);
        LOGGER.info("Storing credentials in cookie for {}", credentials.username());
        res.addCookie(cookie);
      }
    }

    // Continue
    chain.doFilter(req, res);

  }

  /**
   * A simple request wrapping the current one to the username and password parameters to send
   * to the authenticator.
   */
  private static final class ProxyRequest extends HttpServletRequestWrapper {

    /** The credentials to use for the authenticator. */
    private final Credentials _credentials;

    public ProxyRequest(HttpServletRequest original, Credentials credentials) {
      super(original);
      this._credentials = credentials;
    }

    @Override
    public String getParameter(String name) {
      String value = super.getParameter(name);
      if (value == null) {
        if ("username".equals(name)) {
          value = this._credentials.username();
        } else if ("password".equals(name)) {
          value = this._credentials.password();
        }
      }
      return value;
    }

  }
}
