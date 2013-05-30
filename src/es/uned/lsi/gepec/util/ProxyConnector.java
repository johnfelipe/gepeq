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
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import es.uned.lsi.encryption.AssymetricEncryptor;
import es.uned.lsi.encryption.AssymetricEncryptor.AssymetricKey;
import es.uned.lsi.gepec.web.services.ConfigurationService;

public class ProxyConnector
{
	private static boolean initialized=false;
	private static Proxy proxy=null;
	private static Authenticator authenticator=null;
	private static String username;
	private static char[] password;
	
	private static Pattern patternNonProxyHosts=null;
	
	public static void initialize()
	{
		FacesContext context=FacesContext.getCurrentInstance();
		ConfigurationService configurationService=(ConfigurationService)context.getApplication().
			evaluateExpressionGet(context,"#{configurationService}",ConfigurationService.class);
        String proxyUrl=configurationService.getProperty("Proxy-URL");
        int proxyPort=80; 
        String proxyUsername=null;
        String proxyPassword=null;
        if (proxyUrl!=null)
        {
        	String sProxyPort=configurationService.getProperty("Proxy-Port");
        	if (sProxyPort!=null)
        	{
        		try
        		{
        			proxyPort=Integer.parseInt(sProxyPort);
        		}
        		catch (NumberFormatException ex)
        		{
        			proxyPort=80;
        		}
        		if (proxyPort<0)
        		{
        			proxyPort=80;
        		}
        	}
        	proxyUsername=configurationService.getProperty("Proxy-Username");
            proxyPassword=Decryptor.decrypt(configurationService.getProperty("Proxy-Password"));
            if (proxyPassword==null)
            {
            	proxyPassword="";
            }
            else
            {
            	String decryptedProxyPassword=null;
            	try
            	{
            		AssymetricKey privateKey=AssymetricEncryptor.readAssymetricKeyFromEncryptedFile(
            			configurationService.getPrivateKeyPath());
            		decryptedProxyPassword=AssymetricEncryptor.decrypt(proxyPassword,privateKey);
            	}
            	catch (IOException ioe)
            	{
            		decryptedProxyPassword=null;
            	}
            	if (decryptedProxyPassword!=null)
            	{
            		proxyPassword=decryptedProxyPassword;
            	}
            }
    		proxy=new Proxy(Proxy.Type.HTTP,InetSocketAddress.createUnresolved(proxyUrl,proxyPort));
    		if ((proxyUsername!=null && !proxyUsername.equals("")))
    		{
    			username=proxyUsername;
    			password=proxyPassword.toCharArray();
    			authenticator=new Authenticator()
    			{
    				@Override
    				protected PasswordAuthentication getPasswordAuthentication()
    				{
    					PasswordAuthentication passwordAuthentication=null;
    					if (getRequestorType().equals(RequestorType.PROXY))
    					{
    						passwordAuthentication=new PasswordAuthentication(username,password);
    					}
    					return passwordAuthentication;
    				}
    			};
    		}
    		String proxyNonHosts=configurationService.getProperty("Non-Proxy-Hosts");
    		if (proxyNonHosts==null)
    		{
    			proxyNonHosts=System.getProperty("http.nonProxyHosts");
    		}
    		if (proxyNonHosts!=null)
    		{
    			patternNonProxyHosts=Pattern.compile(proxyNonHosts);
    		}
        }
        initialized=true;
	}
	
	public static void reset()
	{
		initialized=false;
	}
	
	public static HttpURLConnection openConnection(URL url) throws IOException
	{
		if (!initialized)
		{
			initialize();
		}
		HttpURLConnection connection=null;
		if (proxy==null)
		{
			connection=(HttpURLConnection)url.openConnection();
		}
		else
		{
			Matcher matcherNonProxyHost=null;
			if (patternNonProxyHosts!=null)
			{
				matcherNonProxyHost=patternNonProxyHosts.matcher(url.getHost());
			}
			if (matcherNonProxyHost!=null && matcherNonProxyHost.matches())
			{
				connection=(HttpURLConnection)url.openConnection();
			}
			else
			{
				if (authenticator!=null)
				{
					Authenticator.setDefault(authenticator);
				}
				connection=(HttpURLConnection)url.openConnection(proxy);
			}
		}
		return connection;
	}
}
