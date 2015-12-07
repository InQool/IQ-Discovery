package org.jzkit.configuration.api;

import org.jzkit.ServiceDirectory.*;
import java.util.Iterator;
import java.util.Map;
import org.jzkit.search.util.Profile.ProfileDBO;
import org.jzkit.search.util.Profile.CrosswalkDBO;
import org.springframework.context.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

public class ConfigDumper {

  /**
   *  Output a JZKit config file based on the current configuration
   */
  public static Document output(Configuration c) {
    Document result = null;
    try {
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      dfactory.setValidating(false);
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();

      result = docBuilder.newDocument();
      // Element root = retval.createElement("text");
      // root.appendChild( retval.createTextNode(orig) );
      // retval.appendChild( root );
    }
    catch ( ParserConfigurationException pce ) {
      pce.printStackTrace();
    }

    return result;
  }
}
