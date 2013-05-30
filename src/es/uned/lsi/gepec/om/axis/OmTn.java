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
 * OmTn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.uned.lsi.gepec.om.axis;

public interface OmTn extends java.rmi.Remote {
    public boolean existTestXmls(java.lang.String testName, int version) throws java.rmi.RemoteException;
    public boolean existQuestionXml(java.lang.String packageName) throws java.rmi.RemoteException;
    public boolean existQuestionJar(java.lang.String packageName, java.lang.String version) throws java.rmi.RemoteException;
    public long getQuestionJarLastModified(java.lang.String packageName, java.lang.String version) throws java.rmi.RemoteException;
    public long getTestXmlLastModified(java.lang.String testName) throws java.rmi.RemoteException;
    public long getDeployXmlLastModified(java.lang.String testName, int version) throws java.rmi.RemoteException;
    public boolean uploadTestXml(java.lang.String testName, java.lang.String[] base64TestXml) throws java.rmi.RemoteException;
    public boolean uploadDeployXml(java.lang.String name, int version, java.lang.String[] base64DeployXml) throws java.rmi.RemoteException;
    public boolean uploadQuestionJar(java.lang.String packageName, java.lang.String version, java.lang.String[] base64QuestionJar) throws java.rmi.RemoteException;
    public void deleteTestXmls(java.lang.String testName, int version) throws java.rmi.RemoteException;
    public void deleteQuestionXml(java.lang.String packageName) throws java.rmi.RemoteException;
    public void deleteQuestionJar(java.lang.String packageName) throws java.rmi.RemoteException;
    public void stopAllSessionsForQuestion(java.lang.String packageName) throws java.rmi.RemoteException;
    public boolean isOUCUAvailable(java.lang.String oucu) throws java.rmi.RemoteException;
    public java.lang.String getQuestionsReleasesMetadata() throws java.rmi.RemoteException;
    public java.lang.String getQuestionReleaseMetadata(java.lang.String packageName) throws java.rmi.RemoteException;
    public java.lang.String getTestsReleasesMetadata() throws java.rmi.RemoteException;
    public java.lang.String getTestReleaseMetadata(java.lang.String testName, int version) throws java.rmi.RemoteException;
    public java.lang.String getTestReleaseVersions(java.lang.String testName) throws java.rmi.RemoteException;
}
