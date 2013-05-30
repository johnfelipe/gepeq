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

import javax.faces.context.FacesContext;

import es.uned.lsi.encryption.SymmetricEncryptor;
import es.uned.lsi.gepec.web.services.ConfigurationService;

/**
 * Utility class to encrypt some messages to send to Test Navigator test environment.
 */
public class OmTnEncryptor
{
	private static ConfigurationService configurationService=null;
	
	/**
	 * Private default constructor to avoid class instantiation. 
	 */
	private OmTnEncryptor()
	{
	}
	
	/**
	 * @param message Message
	 * @return Encrypted message (or original message if encryption fails)
	 */
	public static String encrypt(String message)
	{
		String encryptedMessage=null;
		String password=getConfigurationService().getOmTnEncryptionPassword();
		if (password!=null)
		{
			encryptedMessage=SymmetricEncryptor.encrypt(message,password);
		}
		return encryptedMessage==null?message:encryptedMessage;
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
