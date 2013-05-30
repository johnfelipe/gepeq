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
 * OmTnProxy.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.uned.lsi.gepec.om.axis;

public class OmTnProxy implements OmTn {
  private String _endpoint = null;
  private OmTn omTn = null;
  
  public OmTnProxy() {
    _initOmTnProxy();
  }
  
  public OmTnProxy(String endpoint) {
    _endpoint = endpoint;
    _initOmTnProxy();
  }
  
  private void _initOmTnProxy() {
    try {
      omTn = (new OmTnServiceLocator()).getOmTn();
      if (omTn != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)omTn)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)omTn)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (omTn != null)
      ((javax.xml.rpc.Stub)omTn)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public OmTn getOmTn() {
    if (omTn == null)
      _initOmTnProxy();
    return omTn;
  }
  
  public boolean existTestXmls(java.lang.String testName, int version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.existTestXmls(testName, version);
  }
  
  public boolean existQuestionXml(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.existQuestionXml(packageName);
  }
  
  public boolean existQuestionJar(java.lang.String packageName, java.lang.String version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.existQuestionJar(packageName, version);
  }
  
  public long getQuestionJarLastModified(java.lang.String packageName, java.lang.String version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getQuestionJarLastModified(packageName, version);
  }
  
  public long getTestXmlLastModified(java.lang.String testName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getTestXmlLastModified(testName);
  }
  
  public long getDeployXmlLastModified(java.lang.String testName, int version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getDeployXmlLastModified(testName, version);
  }
  
  public boolean uploadTestXml(java.lang.String testName, java.lang.String[] base64TestXml) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.uploadTestXml(testName, base64TestXml);
  }
  
  public boolean uploadDeployXml(java.lang.String name, int version, java.lang.String[] base64DeployXml) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.uploadDeployXml(name, version, base64DeployXml);
  }
  
  public boolean uploadQuestionJar(java.lang.String packageName, java.lang.String version, java.lang.String[] base64QuestionJar) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.uploadQuestionJar(packageName, version, base64QuestionJar);
  }
  
  public void deleteTestXmls(java.lang.String testName, int version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    omTn.deleteTestXmls(testName, version);
  }
  
  public void deleteQuestionXml(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    omTn.deleteQuestionXml(packageName);
  }
  
  public void deleteQuestionJar(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    omTn.deleteQuestionJar(packageName);
  }
  
  public void stopAllSessionsForQuestion(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    omTn.stopAllSessionsForQuestion(packageName);
  }
  
  public boolean isOUCUAvailable(java.lang.String oucu) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.isOUCUAvailable(oucu);
  }
  
  public java.lang.String getQuestionsReleasesMetadata() throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getQuestionsReleasesMetadata();
  }
  
  public java.lang.String getQuestionReleaseMetadata(java.lang.String packageName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getQuestionReleaseMetadata(packageName);
  }
  
  public java.lang.String getTestsReleasesMetadata() throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getTestsReleasesMetadata();
  }
  
  public java.lang.String getTestReleaseMetadata(java.lang.String testName, int version) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getTestReleaseMetadata(testName, version);
  }
  
  public java.lang.String getTestReleaseVersions(java.lang.String testName) throws java.rmi.RemoteException{
    if (omTn == null)
      _initOmTnProxy();
    return omTn.getTestReleaseVersions(testName);
  }
  
  
}