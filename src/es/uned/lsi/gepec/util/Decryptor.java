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
package es.uned.lsi.gepec.util;

import java.io.IOException;

import javax.faces.context.FacesContext;

import es.uned.lsi.encryption.AssymetricEncryptor;
import es.uned.lsi.encryption.AssymetricEncryptor.AssymetricKey;
import es.uned.lsi.gepec.web.services.ConfigurationService;

/**
 * Utility class to decrypt some encrypted properties.
 */
public final class Decryptor
{
	private static ConfigurationService configurationService=null;
	
	/**
	 * Private default constructor to avoid class instantiation. 
	 */
	private Decryptor()
	{
	}
	
	/**
	 * @param message Message
	 * @return Decrypted message (or original message if decryption fails)
	 */
	public static String decrypt(String message)
	{
    	String decryptedMessage=null;
    	if (message!=null)
    	{
    		ConfigurationService configurationService=getConfigurationService();
    		if (configurationService!=null)
    		{
            	try
            	{
               		AssymetricKey privateKey=AssymetricEncryptor.readAssymetricKeyFromEncryptedFile(
               			configurationService.getPrivateKeyPath());
               		decryptedMessage=AssymetricEncryptor.decrypt(message,privateKey);
            	}
            	catch (IOException ioe)
            	{
            		decryptedMessage=null;
            	}
    		}
    	}
		return decryptedMessage==null?message:decryptedMessage;
	}
	
	/**
	 * @return Configuration service from Java Server Faces context (or null if it can not be retrieved)
	 */
	private static ConfigurationService getConfigurationService()
	{
		if (configurationService==null)
		{
			try
			{
				FacesContext context=FacesContext.getCurrentInstance();
				configurationService=(ConfigurationService)context.getApplication().getELResolver().getValue(
					context.getELContext(),null,"configurationService");
			}
			catch (Exception e)
			{
				configurationService=null;
			}
		}
		return configurationService;
	}
}
