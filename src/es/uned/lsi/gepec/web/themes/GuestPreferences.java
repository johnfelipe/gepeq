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
package es.uned.lsi.gepec.web.themes;

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.web.services.ThemesService;

@SuppressWarnings("serial")
@ManagedBean(name="guestPreferencesBean")
@SessionScoped
public class GuestPreferences implements Serializable
{
	@ManagedProperty(value="#{themesService}")
	private ThemesService themesService;
	
	/** Current theme */
	private String theme=null;
	
	public void setThemesService(ThemesService themesService)
	{
		this.themesService=themesService;
	}
	
	public String getTheme()
	{
		Map<String,String> params=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		if (params.containsKey("theme"))
		{
			theme=params.get("theme");
		}
		if (theme==null)
		{
			theme=themesService.getDefaultTheme();
		}
		return theme;
	}
	
	public void setTheme(String theme)
	{
		this.theme=theme;
	}
}
