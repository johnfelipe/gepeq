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
package es.uned.lsi.gepec.web.services;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Ofrece operaciones con los ficheros de configuracion
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages configuration properties.
 */
@ManagedBean(eager=true)
@ApplicationScoped
@SuppressWarnings("serial")
public class ConfigurationService implements Serializable
{
	public final static String PROPERTY_RESOURCES_FOLDER="ResourcesFolder";
	public final static String PROPERTY_OM_URL="OmUrl";
	public final static String PROPERTY_OM_TN_URL="OmTnUrl";
	public final static String PROPERTY_OM_QE_URL="OmQeUrl";
	public final static String PROPERTY_OM_TN_PRO_URL="OmTnProUrl";
	public final static String PROPERTY_OM_QUESTIONS_FOLDER="OmQuestionsFolder";
	public final static String PROPERTY_IMPORT_QUESTIONS_FOLDER="ImportQuestionsFolder";
	public final static String PROPERTY_EXPORT_QUESTIONS_FOLDER="ExportQuestionsFolder";
	public final static String PROPERTY_TMP_FOLDER="TmpFolder";
	public final static String PROPERTY_OM_TN_ENCRYPTION_PASWORD="OmTnEncryptionPassword";
	public final static String PROPERTY_OM_TN_PRO_ENCRYPTION_PASWORD="OmTnProEncryptionPassword";
	
	public final static String PRIVATE_KEY_FILE_NAME="WEB-INF/security/private.key";
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	/** Configuration properties */
	private Map<String,String> properties=null;
	
	public ConfigurationService()
	{
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/**
	 * @param name Property's name
	 * @return Property's value
	 * @throws ServiceException
	 */
	public String getProperty(String name) throws ServiceException
	{
		if (properties==null)
		{
			loadData();
		}
		return properties.get(name);
	}
	
	//Obtiene la ruta de la aplicación web
	/**
	 * @return Application real path
	 * @throws ServiceException
	 */
	public String getApplicationPath() throws ServiceException
	{
		String applicationPath=null;
		try
		{
			applicationPath=FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
		}
		catch (Exception e)
		{
			String configurationFatalError=null;
			try
			{
				configurationFatalError=localizationService.getLocalizedMessage("CONFIGURATION_FATAL_ERROR");
			}
			catch (ServiceException se)
			{
				configurationFatalError=null;
			}
			if (configurationFatalError==null)
			{
				configurationFatalError="Configuration fatal error";
			}
			throw new ServiceException(configurationFatalError,e);
		}
		return applicationPath; 
	}
	
	//Obtiene la ruta relativa de la carpeta de recursos
	/**
	 * @return Relative resources path
	 * @throws ServiceException
	 */
	public String getResourcesFolder() throws ServiceException
	{
		return getProperty(PROPERTY_RESOURCES_FOLDER);
	}
	
	//Obtiene la ruta absoluta de la carpeta de recursos
	/**
	 * @return Absolute resources path
	 * @throws ServiceException
	 */
	public String getResourcesPath() throws ServiceException
	{
		StringBuffer resourcesPath=new StringBuffer();
		resourcesPath.append(getApplicationPath());
		resourcesPath.append(File.separatorChar);
		resourcesPath.append(getResourcesFolder().replace('/',File.separatorChar));
		return resourcesPath.toString();
	}
	
	//Obtiene la url en la que se encuentra el sistema OM
	/**
	 * @return OM Developer web application URL
	 * @throws ServiceException
	 */
	public String getOmUrl() throws ServiceException
	{
		return getProperty(PROPERTY_OM_URL);
	}
	
	/**
	 * @return OM Test Navigator web application URL
	 * @throws ServiceException
	 */
	public String getOmTnUrl() throws ServiceException
	{
		return getProperty(PROPERTY_OM_TN_URL);
	}
	
	/**
	 * @return OM Question Engine web application URL
	 * @throws ServiceException
	 */
	public String getOmQeUrl() throws ServiceException
	{
		return getProperty(PROPERTY_OM_QE_URL);
	}
	
	/**
	 * @return OM Test Navigator (production) web application URL
	 * @throws ServiceException
	 */
	public String getOmTnProUrl() throws ServiceException
	{
		return getProperty(PROPERTY_OM_TN_PRO_URL);
	}
	
	//Obtiene la carpeta donde se copiarán las preguntas para OM
	/**
	 * @return Relative path to OM questions folder
	 * @throws ServiceException
	 */
	public String getOmQuestionsFolder() throws ServiceException
	{
		return getProperty(PROPERTY_OM_QUESTIONS_FOLDER);
	}
	
	//Obtiene la ruta de la carpeta para las preguntas de OM
	/**
	 * @return Absolute path to OM questions folder
	 * @throws ServiceException
	 */
	public String getOmQuestionsPath() throws ServiceException
	{
		StringBuffer omQuestionsPath=new StringBuffer();
		omQuestionsPath.append(getApplicationPath());
		omQuestionsPath.append(File.separatorChar);
		omQuestionsPath.append(getOmQuestionsFolder().replace('/',File.separatorChar));
		return omQuestionsPath.toString();
	}
	
	//Obtiene la carpeta donde se importarán las preguntas de QTI
	/**
	 * @return Relative path to import folder of QTI questions
	 * @throws ServiceException
	 */
	public String getQtiImportQuestionsFolder() throws ServiceException
	{
		return getProperty(PROPERTY_IMPORT_QUESTIONS_FOLDER);
	}
	
	//Obtiene la carpeta donde se exportarán las preguntas de QTI
	/**
	 * @return Relative path to export folder of QTI questions
	 * @throws ServiceException
	 */
	public String getQtiExportQuestionsFolder() throws ServiceException
	{
		return getProperty(PROPERTY_EXPORT_QUESTIONS_FOLDER);
	}
	
	/**
	 * @return Relative path for temporal files
	 * @throws ServiceException
	 */
	public String getTmpFolder() throws ServiceException
	{
		return getProperty(PROPERTY_TMP_FOLDER);
	}
	
	/**
	 * @return Absolute path for temporal files
	 * @throws ServiceException
	 */
	public String getTmpPath() throws ServiceException
	{
		StringBuffer tmpPath=new StringBuffer();
		tmpPath.append(getApplicationPath());
		tmpPath.append(File.separatorChar);
		tmpPath.append(getTmpFolder().replace('/',File.separatorChar));
		return tmpPath.toString();
	}
	
	/**
	 * @return Absolute path to private key used for decrypting encrypted properties
	 * @throws ServiceException
	 */
	public String getPrivateKeyPath() throws ServiceException
	{
		StringBuffer privateKeyPath=new StringBuffer();
		privateKeyPath.append(getApplicationPath());
		privateKeyPath.append(File.separatorChar);
		privateKeyPath.append(PRIVATE_KEY_FILE_NAME.replace('/',File.separatorChar));
		return privateKeyPath.toString();
	}
	
	/**
	 * @return Salted password used for encrypting messages to send to Test Navigator test environment 
	 */
	public String getOmTnEncryptionPassword()
	{
		return getProperty(PROPERTY_OM_TN_ENCRYPTION_PASWORD);
	}
	
	/**
	 * @return Salted password used for encrypting messages to send to Test Navigator production environment 
	 */
	public String getOmTnProEncryptionPassword()
	{
		return getProperty(PROPERTY_OM_TN_PRO_ENCRYPTION_PASWORD);
	}
	
	//Carga los datos del fichero de configuración
	/**
	 * Load data from /WEB-INF/configuration.xml
	 * @throws ServiceException
	 */
	private void loadData() throws ServiceException
	{
		loadData(getApplicationPath());
	}
	
	//Carga los datos del fichero de configuración
	/**
	 * Load data from /WEB-INF/configuration.xml
	 * @param applicationPath Application path
	 * @throws ServiceException
	 */
	private void loadData(String applicationPath) throws ServiceException
	{
		properties=new HashMap<String,String>();
		try
		{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder parser=factory.newDocumentBuilder();
			
			StringBuffer configurationPath=new StringBuffer();
			configurationPath.append(applicationPath);
			configurationPath.append(File.separatorChar);
			configurationPath.append("WEB-INF");
			configurationPath.append(File.separatorChar);
			configurationPath.append("configuration.xml");
			
			Document document=parser.parse(configurationPath.toString());
			
			Node config=document.getElementsByTagName("Configuration").item(0);
			NodeList configProperties=config.getChildNodes();
			for (int i=0;i<configProperties.getLength();i++)
			{
				Node configProperty=configProperties.item(i);
				if (configProperty.getNodeType()==Node.ELEMENT_NODE)
				{
					properties.put(configProperty.getNodeName(),configProperty.getTextContent());
				}
			}
		}
		catch (Exception e)
		{
			properties=null;
			String configurationFatalError=null;
			try
			{
				configurationFatalError=localizationService.getLocalizedMessage("CONFIGURATION_FATAL_ERROR");
			}
			catch (ServiceException se)
			{
				configurationFatalError="Configuration fatal error";
			}
			throw new ServiceException(configurationFatalError,e);
		}
	}
	
	/**
	 * Forces to reload properties from configuration.xml next time we need to access any of them.<br/><br/>
	 * It can be used if it is needed to apply changes done to configuration.xml without restarting server.
	 */
	public void resetConfiguration()
	{
		properties=null;
	}
}