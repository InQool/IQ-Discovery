// Title:       HTMLRecord (SimpleUnstructuredTextRecord)
// @version:    $Id: HTMLRecord.java,v 1.3 2005/09/13 14:13:03 ibbo Exp $
// Copyright:   Copyright (C) 1999,2000 Knowledge Integration Ltd.
// @author:     Ian Ibbotson (ibbo@k-int.com)
// Company:     Knowledge Integration Ltd.
// Description: 

//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the license, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite
// 330, Boston, MA  02111-1307, USA.
// 


package org.jzkit.search.util.RecordModel;

// import org.jzkit.SearchProvider.iface.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class HTMLRecord implements InformationFragment, Serializable
{
  private String source_repository = null;
  private String source_collection_name = null;
  private String orig_schema = "sutrs";
  private Object handle = null;
  private String orig = null;
  private static ExplicitRecordFormatSpecification spec = null;
  private Map additional_info = new HashMap();

  public HTMLRecord(Object source) {
    this.orig=(String)source;
  }

  public HTMLRecord(String source_repository,
		    String source_collection_name, 
                    String handle,
                    Object source,
		    ExplicitRecordFormatSpecification spec) {
    this.source_repository = source_repository;
    this.source_collection_name = source_collection_name;
    this.handle = handle;
    this.orig=(String)source;
    HTMLRecord.spec = spec;
  }

  // Get hold of the original (maybe blob) object
  public Object getOriginalObject() {
    return orig;
  }

  // Name of class for above object
  public String getOriginalObjectClassName() {
    return "byte[]";
  }

  // We will use the root node to determine any applicable namespace

  // Get DOM Document for object
  public Document getDocument() {
    Document retval = null;
    try {
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      dfactory.setValidating(false);
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();

      retval = docBuilder.newDocument();
      Element root = retval.createElement("HTML");
      root.appendChild( retval.createTextNode(orig) );
      retval.appendChild( root );
    }
    catch ( ParserConfigurationException pce ) {
      pce.printStackTrace();
    }

    return retval;
  }

  public String getDocumentSchema() {
    return orig_schema;
  }

  public String toString() {
    return orig;
  }

  public String getSourceRepositoryID() {
    return source_repository;
  }

  public String getSourceCollectionName() {
    return source_collection_name;
  }
 
  public Object getSourceFragmentID() {
    if ( handle != null )
      return handle;
    else
      return null;
  } 

  public ExplicitRecordFormatSpecification getFormatSpecification() {
    return spec;
  }

  public void setFormatSpecification(ExplicitRecordFormatSpecification spec) {
    this.spec = spec;
  }
                                                                                                                                        
  public void setSourceRepositoryID(String id) {
    this.source_repository = id;
  }
                                                                                                                                        
  public void setSourceCollectionName(String collection_name) {
    this.source_collection_name = collection_name;
  }
                                                                                                                                        
  public void setSourceFragmentID(Object id) {
    this.handle = id;
  }

  /**
   *  Any extended record info such as hit highligting points, OAI Header record info etc that
   *  the search provider may return.
   */
  public Map getExtendedInfo() {
    return additional_info;
  }

  long hit_no;

  public long getHitNo() {
    return hit_no;
  }

  public void setHitNo(long hit_no) {
    this.hit_no = hit_no;
  }

}
