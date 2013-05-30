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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import es.uned.lsi.gepec.web.LocaleBean;

//Ofrece servicios para la localización de la aplicación
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages messages localization.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class LocalizationService implements Serializable
{
	private final static Map<String,Boolean> DEFAULT_OPTIONS_GEN;
	static
	{
		DEFAULT_OPTIONS_GEN=new HashMap<String,Boolean>();
		DEFAULT_OPTIONS_GEN.put("COPYRIGHT_GEN",Boolean.TRUE);
		DEFAULT_OPTIONS_GEN.put("ROLE_GEN",Boolean.FALSE);
		DEFAULT_OPTIONS_GEN.put("DIF_GEN",Boolean.TRUE);
		DEFAULT_OPTIONS_GEN.put("QUESTION_TYPES_GEN",Boolean.FALSE);
		DEFAULT_OPTIONS_GEN.put("SECTION_GEN",Boolean.TRUE);
		DEFAULT_OPTIONS_GEN.put("CATEGORY_GEN",Boolean.TRUE);
	}
	
	public LocalizationService()
	{
	}
	
	//Obtiene un mensaje del ResourceBundle correspondiente al Locale actual
	/**
	 * @param messageId Message identifier
	 * @return Localized message or null if message identifier is not found whithin current locale file
	 * @throws ServiceException
	 */
	public String getLocalizedMessage(String messageId) throws ServiceException
	{
		String localizedMessage=null;
		try
		{
			FacesContext context=FacesContext.getCurrentInstance();
			ResourceBundle rb=context.getApplication().getResourceBundle(context,"msgs");
			try
			{
				localizedMessage=rb.getString(messageId);
			}
			catch (NullPointerException npe)
			{
				localizedMessage=null;
			}
			catch (MissingResourceException ex)
			{
				localizedMessage=null;
			}
		}
		catch (Exception e)
		{
			String localizationFatalError=null;
			try
			{
				FacesContext context=FacesContext.getCurrentInstance();
				ResourceBundle rb=context.getApplication().getResourceBundle(context,"msgs");
				localizationFatalError=rb.getString("LOCALIZATION_FATAL_ERROR");
			}
			catch (Exception lfe)
			{
				localizationFatalError="Localization fatal error";
			}
			throw new ServiceException(localizationFatalError,e);
		}
		return localizedMessage;
	}
	
	/**
	 * @param styleClasses List of style classes for current locale separated by , or whitespaces
	 * @return List of style classes for current locale
	 */
	public String getLocalizedStyleClasses(String styleClasses)
	{
		StringBuffer localizedStyleClasses=null;
		FacesContext context=FacesContext.getCurrentInstance();
		LocaleBean localeBean=(LocaleBean)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"localeBean");
		String localeSuffix=localeBean.getLocaleSuffix(localeBean.getLangCode());
		styleClasses=StringUtils.stripToEmpty(styleClasses);
		if (!styleClasses.equals("") && !localeSuffix.equals(""))
		{
			String separator=null;
			if (styleClasses.contains(","))
			{
				separator=",";
			}
			else if (styleClasses.contains(" "))
			{
				separator=" ";
			}
			localizedStyleClasses=new StringBuffer();
			if (separator==null)
			{
				localizedStyleClasses=new StringBuffer(styleClasses);
				localizedStyleClasses.append(localeSuffix);
			}
			else
			{
				String[] styleClassesArray=styleClasses.split(separator);
				for (String styleClass:styleClassesArray)
				{
					localizedStyleClasses.append(styleClass);
					localizedStyleClasses.append(localeSuffix);
					localizedStyleClasses.append(separator);
				}
				localizedStyleClasses.deleteCharAt(localizedStyleClasses.length()-1);
			}
		}
		return localizedStyleClasses==null?styleClasses:localizedStyleClasses.toString();
	}
	
    /**
     * @param genKey Key with the genere of 'All' keyword
     * @return 'All' keyword
     */
	public String getAllOptions(String genKey)
	{
		String allOptions=null;
		String gen=getLocalizedMessage(genKey);
		if ("M".equals(gen))
		{
			allOptions="ALL_OPTIONS";
		}
		else if ("F".equals(gen))
		{
			allOptions="ALL_OPTIONS_F";
		}
		else
		{
			boolean genF=false;
			if (DEFAULT_OPTIONS_GEN.containsKey(genKey))
			{
				genF=DEFAULT_OPTIONS_GEN.get(genKey).booleanValue();
			}
			allOptions=genF?"ALL_OPTIONS_F":"ALL_OPTIONS";
		}
		return getLocalizedMessage(allOptions);
	}
	
    /**
     * @param genKey Key with the genere of 'None' keyword
     * @return 'None' keyword
     */
	public String getNoneOptions(String genKey)
	{
		String noneOptions=null;
		String gen=getLocalizedMessage(genKey);
		if ("M".equals(gen))
		{
			noneOptions="NONE_OPTIONS";
		}
		else if ("F".equals(gen))
		{
			noneOptions="NONE_OPTIONS_F";
		}
		else
		{
			boolean genF=false;
			if (DEFAULT_OPTIONS_GEN.containsKey(genKey))
			{
				genF=DEFAULT_OPTIONS_GEN.get(genKey).booleanValue();
			}
			noneOptions=genF?"NONE_OPTIONS_F":"NONE_OPTIONS";
		}
		return getLocalizedMessage(noneOptions);
	}
}