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
import javax.annotation.PostConstruct;  
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import es.uned.lsi.gepec.web.services.ThemesService;

@SuppressWarnings("serial")
@ManagedBean(name="themeSwitcherBean")
@SessionScoped
public class ThemeSwitcherBean implements Serializable
{        
	private String theme;
	
	@ManagedProperty(value="#{guestPreferencesBean}")
	private GuestPreferences gp;  
	@ManagedProperty(value="#{themesService}")
	private ThemesService themesService;
	
	public void setGp(GuestPreferences gp)
	{
		this.gp=gp;
	}
	
	public void setThemesService(ThemesService themesService)
	{
		this.themesService=themesService;
	}
	
	public Map<String,String> getThemes()
	{
		return themesService.getThemes();
	}
	
	public String getTheme()
	{
		return theme;
	}
	
	public void setTheme(String theme)
	{  
		this.theme=theme;
	}
	
	@PostConstruct  
	public void init()
	{
		theme=gp.getTheme();
	}
	
	public void saveTheme()
	{  
		gp.setTheme(theme);
	}
}
