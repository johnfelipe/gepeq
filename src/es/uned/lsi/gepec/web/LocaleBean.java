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
package es.uned.lsi.gepec.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

//Backbean de soporte para internacionalización
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for internationalization support.
 */
@SuppressWarnings("serial")
@ManagedBean(name="localeBean")
@SessionScoped
public class LocaleBean implements Serializable
{	
	private String langCode;											// Language code
	
	public LocaleBean()
	{
		Application app=FacesContext.getCurrentInstance().getApplication();
		Locale locale=app.getDefaultLocale();
		setLangCode(locale.toString());
	}
	
	public String getLangCode()
	{
		return langCode;
	}
	
	public void setLangCode(String langCode)
	{
		this.langCode=langCode;
	}
	
	/**
	 * @param langCode Language code
	 * @return Suffix for a language code (empty string for default locale, otherwise same language code 
	 * but with the first letter capitalized)
	 */
	public String getLocaleSuffix(String langCode)
	{
		StringBuffer localeSuffix=new StringBuffer();
		Locale defaultLocale=FacesContext.getCurrentInstance().getApplication().getDefaultLocale();
		if (!langCode.equals(defaultLocale.toString()))
		{
			localeSuffix.append(Character.toUpperCase(langCode.charAt(0)));
			localeSuffix.append(langCode.substring(1));
		}
		return localeSuffix.toString();
	}
	
	//Obtiene todos los Locales soportados por la aplicación
	/**
	 * @return List of all language codes supported 
	 */
	public List<Locale> getLocales()
	{
		List<Locale> locales=new ArrayList<Locale>();
		Application app=FacesContext.getCurrentInstance().getApplication();
		Locale locale=app.getDefaultLocale();
		locales.add(locale);
		Iterator<Locale> iterator=app.getSupportedLocales();
		while (iterator.hasNext())
		{
			locale=iterator.next();
			locales.add(locale);
		}
		return locales;
	}
	
	//Muestra un lenguaje según la selección actual
	/**
	 * @param code Language code
	 * @return Localized language name
	 */
	public String getDisplayLanguage(String code)
	{
		StringBuffer displayLanguage=new StringBuffer();
		Locale current=new Locale(getLangCode());
		Locale locale=new Locale(code);
		String language=locale.getDisplayLanguage(current);
		if (language.length()>0)
		{
			// We want first character of language name always in uppercase  
			displayLanguage.append(Character.toUpperCase(language.charAt(0)));
		}
		if (language.length()>1)
		{
			displayLanguage.append(language.substring(1));
		}
		return displayLanguage.toString();
	}
	
	//Cambia el idioma de la aplicación
	/**
	 * Change display language.
	 * @return null so we keep current view
	 */
	public String changeLangCode()
	{
		HttpSession session=(HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		if (session!=null) 
		{
			session.setAttribute("es.uned.lsi.gepec.langCode",langCode);
		}
		
		// Change locale code of current view
		UIViewRoot viewRoot=FacesContext.getCurrentInstance().getViewRoot();
		viewRoot.setLocale(new Locale(getLangCode()));
		return null;
	}
}