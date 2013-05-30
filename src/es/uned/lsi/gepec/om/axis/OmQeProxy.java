/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * OmQeProxy.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.uned.lsi.gepec.om.axis;

public class OmQeProxy implements OmQe {
  private String _endpoint = null;
  private OmQe omQe = null;
  
  public OmQeProxy() {
    _initOmQeProxy();
  }
  
  public OmQeProxy(String endpoint) {
    _endpoint = endpoint;
    _initOmQeProxy();
  }
  
  private void _initOmQeProxy() {
    try {
      omQe = (new OmQeServiceLocator()).getOmQe();
      if (omQe != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)omQe)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)omQe)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (omQe != null)
      ((javax.xml.rpc.Stub)omQe)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public OmQe getOmQe() {
    if (omQe == null)
      _initOmQeProxy();
    return omQe;
  }
  
  public void deleteQuestionFromCache(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omQe == null)
      _initOmQeProxy();
    omQe.deleteQuestionFromCache(packageName);
  }
  
  
}