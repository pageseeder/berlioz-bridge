package org.pageseeder.berlioz.bridge.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.pageseeder.bridge.model.GroupOptions;
import org.pageseeder.bridge.model.PSGroup;
import org.pageseeder.bridge.model.PSNotification;
import org.pageseeder.bridge.model.PSProject;
import org.pageseeder.bridge.model.PSRole;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Class to use to automatically setup PageSeeder for use by a project.
 *
 * <p>This class loads a setup script in XML and creates the required groups and projects on
 * PageSeeder using the <i>setup</i> user.
 *
 * <p>Once a script is parsed, an instance of this class is created.
 * It can be either simulated or executed.
 *
 * @author Christophe Lauret
 *
 * @version 0.1.5
 * @since 0.1.0
 */
public final class Setup {

  /**
   * Captures the setup environment: variables and managers used during setup.
   */
  final SetupEnvironment env = new SetupEnvironment();

  /**
   * The list of setup actions to execute / simulate.
   */
  final List<Action> _actions = new ArrayList<>();

  /**
   * Simulate the setup script and returns the actions as XML.
   *
   * @param xml The XML to write to.
   *
   * @throws SetupException Any error related to the setup
   * @throws IOException    If thrown while writing on the XML string.
   */
  public void simulate(XMLWriter xml) throws SetupException, IOException {
    this.env.init();
    xml.openElement("setup", true);
    xml.attribute("simulate", "true");
    try {
      for (Action action : this._actions) {
        action.simulate(this.env, xml);
      }
    } finally {
      xml.closeElement();
    }
  }

  /**
   * Execute the setup script and returns the actions as XML.
   *
   * @param xml The XML to write to.
   *
   * @throws SetupException Any error related to the setup
   * @throws IOException    If thrown while writing on the XML string.
   */
  public void execute(XMLWriter xml) throws SetupException, IOException {
    this.env.init();
    xml.openElement("setup", true);
    try {
      // Iterate over the actions
      for (Action action : this._actions) {
        action.execute(this.env, xml);
      }
    } finally {
      xml.closeElement();
    }
  }

  /**
   * Parse the setup script and return a setup instance.
   *
   * @param f The file to parse
   *
   * @return the corresponding setup instance.
   *
   * @throws SetupException Will wrap any occurring exception
   */
  public static Setup parse(File f) throws SetupException {
    Setup setup = new Setup();
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      SAXParser parser = factory.newSAXParser();
      parser.parse(f, new Handler(setup));
      setup.env.setRoot(f.getParentFile());
    } catch (SAXException | ParserConfigurationException |IOException ex) {
      throw new SetupException("Unable to parse setup script", ex);
    }
    return setup;
  }

  static class Handler extends DefaultHandler {

    private final Setup _setup;

    public Handler(Setup setup) {
      this._setup = setup;
    }

    public Setup getSetup() {
      return this._setup;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      final String element = localName;
      switch (element) {
        case "group-options":
          String id = attributes.getValue("id");
          GroupOptions options = handleGroupOption(attributes);
          this._setup.env._groupOptions.put(id, options);
          break;

        case "project":
          PSProject project = handleProject(attributes);
          this._setup.env.putProject(project);
          break;

        case "group":
          PSGroup group = handleGroup(attributes);
          this._setup.env.putGroup(group);
          break;

        case "create-project":
          CreateProject createProject = new CreateProject();
          createProject.setProject(this._setup.env.getProject(attributes.getValue("name")));
          createProject.setOptions(this._setup.env._groupOptions.get(attributes.getValue("group-options")));
          this._setup._actions.add(createProject);
          break;

        case "create-group":
          CreateGroup createGroup = new CreateGroup();
          createGroup.setGroup(this._setup.env.getGroup(attributes.getValue("name")));
          createGroup.setOptions(this._setup.env._groupOptions.get(attributes.getValue("group-options")));
          this._setup._actions.add(createGroup);
          break;

        case "upload-resources":
          UploadResources uploadResources = new UploadResources();
          uploadResources.setTo(this._setup.env.getProject(attributes.getValue("to")));
          this._setup._actions.add(uploadResources);
          break;

        case "add-subgroup":
          AddSubGroup addSubGroup = new AddSubGroup();
          addSubGroup.setGroup(this._setup.env.getGroup(attributes.getValue("name")));
          addSubGroup.setTo(this._setup.env.getGroup(attributes.getValue("to")));
          this._setup._actions.add(addSubGroup);
          break;
      }
    }

    private static GroupOptions handleGroupOption(Attributes attributes) {
      GroupOptions options = new GroupOptions();
      for (int i = 0; i < attributes.getLength(); i++) {
        final String name = attributes.getLocalName(i);
        switch (name) {
          case "add-creator-as-member":
            Boolean addCreatorAsMember = getBoolean(attributes, i);
            if (addCreatorAsMember != null) {
              options.setAddCreatorAsMember(addCreatorAsMember);
            }
            break;
          case "create-documents":
            Boolean createDocuments = getBoolean(attributes, i);
            if (createDocuments != null) {
              options.setAddCreatorAsMember(createDocuments);
            }
            break;
          case "id":
            break;
          default:
            if (name.startsWith("property-")) {
              options.setProperty(name.substring("property-".length()), attributes.getValue(i));
            } else {

            }
        }
      }
      return options;
    }

    private static PSGroup handleGroup(Attributes attributes) {
      PSGroup group = new PSGroup();
      String name = attributes.getValue("name");
      String owner = attributes.getValue("owner");
      String description = attributes.getValue("description");
      String defaultRole = attributes.getValue("default-role");
      String defaultNotification = attributes.getValue("default-notification");
      String detailsType = attributes.getValue("details-type");
      group.setName(name);
      group.setOwner(owner);
      if (description != null) {
        group.setDescription(description);
      }
      if (defaultRole != null) {
        group.setDefaultRole(PSRole.valueOf(defaultRole));
      }
      if (defaultNotification != null) {
        group.setDefaultNotification(PSNotification.valueOf(defaultNotification));
      }
      if (detailsType != null) {
        group.setDetailsType(detailsType);
      }
      return group;
    }

    private static PSProject handleProject(Attributes attributes) {
      PSProject project = new PSProject();
      String name = attributes.getValue("name");
      String owner = attributes.getValue("owner");
      String description = attributes.getValue("description");
      String defaultRole = attributes.getValue("default-role");
      String defaultNotification = attributes.getValue("default-notification");
      String detailsType = attributes.getValue("details-type");
      project.setName(name);
      project.setOwner(owner);
      if (description != null) {
        project.setDescription(description);
      }
      if (defaultRole != null) {
        project.setDefaultRole(PSRole.valueOf(defaultRole));
      }
      if (defaultNotification != null) {
        project.setDefaultNotification(PSNotification.valueOf(defaultNotification));
      }
      if (detailsType != null) {
        project.setDetailsType(detailsType);
      }
      return project;
    }

    private static Boolean getBoolean(Attributes attributes, int index) {
      String value = attributes.getValue(index);
      if (value == null) return null;
      return "true".equals(value);
    }
  }

}
