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
 * OmDevProxy.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.uned.lsi.gepec.om.axis;

public class OmDevProxy implements OmDev {
  private String _endpoint = null;
  private OmDev omDev = null;
  
  public OmDevProxy() {
    _initOmDevProxy();
  }
  
  public OmDevProxy(String endpoint) {
    _endpoint = endpoint;
    _initOmDevProxy();
  }
  
  private void _initOmDevProxy() {
    try {
      omDev = (new OmDevServiceLocator()).getOmDev();
      if (omDev != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)omDev)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)omDev)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (omDev != null)
      ((javax.xml.rpc.Stub)omDev)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public OmDev getOmDev() {
    if (omDev == null)
      _initOmDevProxy();
    return omDev;
  }
  
  public void createQuestion(java.lang.String packageName, java.lang.String path, java.lang.String[] extraPackages) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    omDev.createQuestion(packageName, path, extraPackages);
  }
  
  public boolean buildQuestion(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    return omDev.buildQuestion(packageName);
  }
  
  public void deleteQuestion(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    omDev.deleteQuestion(packageName);
  }
  
  public boolean existQuestionJar(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    return omDev.existQuestionJar(packageName);
  }
  
  public long getQuestionJarLastModified(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    return omDev.getQuestionJarLastModified(packageName);
  }
  
  public java.lang.String[] downloadQuestionJar(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omDev == null)
      _initOmDevProxy();
    return omDev.downloadQuestionJar(packageName);
  }
  
  
}