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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manages themes.
 */
@ManagedBean(eager=true)
@ApplicationScoped
@SuppressWarnings("serial")
public class ThemesService implements Serializable
{
	/** Name used in /WEB-INF/themes.xml for theme's name */
	public final static String THEME_NAME="Name";

	/** Name used in /WEB-INF/themes.xml for theme's display name */
	public final static String THEME_DISPLAY_NAME="DisplayName";
	
	/** Name used in /WEB-INF/themes.xml for theme's default attribute name */
	public final static String THEME_DEFAULT="default";
	
	@ManagedProperty(value="#{configurationService}")
	ConfigurationService configurationService;
	@ManagedProperty(value="#{localizationService}")
	LocalizationService localizationService;
	
	public ThemesService()
	{
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/** Themes */
	public Map<String,String> themes=null;
	
	/** Default theme */
	public String defaultTheme=null;
	
	/**
	 * @return Map of themes
	 */
	public Map<String,String> getThemes()
	{
		if (themes==null)
		{
			loadThemes();
		}
		return themes;
	}
	
	/**
	 * @return Default theme
	 */
	public String getDefaultTheme()
	{
		if (defaultTheme==null)
		{
			loadThemes();
		}
		return defaultTheme;
	}
	
	/**
	 * Load themes from /WEB-INF/themes.xml
	 * @throws ServiceException
	 */
	private void loadThemes() throws ServiceException
	{
		loadThemes(configurationService.getApplicationPath());
	}
	
	/**
	 * Load themes from /WEB-INF/themes.xml
	 * @param applicationPath Application path
	 * @throws ServiceException
	 */
	private void loadThemes(String applicationPath) throws ServiceException
	{
		themes=new LinkedHashMap<String,String>();
		defaultTheme=null;
		try
		{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder parser=factory.newDocumentBuilder();
			
			StringBuffer themesPath=new StringBuffer();
			themesPath.append(applicationPath);
			themesPath.append(File.separatorChar);
			themesPath.append("WEB-INF");
			themesPath.append(File.separatorChar);
			themesPath.append("themes.xml");
			
			Document document=parser.parse(new File(themesPath.toString()));
			
			NodeList themes=document.getElementsByTagName("Theme");
			String firstTheme=null;
			for (int i=0;i<themes.getLength();i++)
			{
				Element theme=(Element)themes.item(i);
				String name=theme.getElementsByTagName(THEME_NAME).item(0).getTextContent();
				String displayName=theme.getElementsByTagName(THEME_DISPLAY_NAME).item(0).getTextContent();
				if (defaultTheme==null && "true".equals(theme.getAttribute(THEME_DEFAULT)))
				{
					defaultTheme=name;
				}
				else if (firstTheme==null)
				{
					firstTheme=name;
				}
				this.themes.put(displayName,name);
			}
			if (defaultTheme==null)
			{
				defaultTheme=firstTheme;
			}
		}
		catch (Exception e)
		{
			themes=null;
			defaultTheme=null;
			String themesFatalError=null;
			try
			{
				themesFatalError=localizationService.getLocalizedMessage("THEMES_FATAL_ERROR");
			}
			catch (ServiceException se)
			{
				themesFatalError="Themes configuration fatal error.";
			}
			throw new ServiceException(themesFatalError,e);
		}
	}
	
	/**
	 * Forces to reload themes from themes.xml next time we need to access any of them.<br/><br/>
	 * It can be used if it is needed to apply changes done to themes.xml without restarting server.
	 */
	public void resetThemes()
	{
		themes=null;
		defaultTheme=null;
	}
}
