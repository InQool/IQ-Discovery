/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jzkit.webapp.core;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.POST;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.api.view.ImplicitProduces;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlTransient;

import com.sun.jersey.spi.resource.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REST Web Service
 *
 * @author ibbo
 */


@XmlRootElement
@ImplicitProduces("text/html;qs=5")
@XmlAccessorType(XmlAccessType.FIELD)
@Path("/home")
@Singleton
public class Home {

  private static Log log = LogFactory.getLog(Home.class);

  @XmlTransient
  @Context
  private UriInfo context;

  public Home() {
  }

  /**
   * Retrieves representation of an instance of info.made4u.made4uws.workorder.Made4UWorkOrderResource
   * @param id resource URI parameter
   * @return an instance of java.lang.String
   */
  @GET
  @Produces({MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML})
  public Viewable getXml() {
    return new Viewable("index",this);
  }
}
