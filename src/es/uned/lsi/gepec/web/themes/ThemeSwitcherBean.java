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
import java.util.ArrayList;  
import java.util.List;  
import java.util.Map;  
import java.util.TreeMap;  
import javax.annotation.PostConstruct;  
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
  
@SuppressWarnings("serial")
@ManagedBean(name="themeSwitcherBean")
@SessionScoped
public class ThemeSwitcherBean implements Serializable
{        
	private Map<String,String> themes;
	private List<Theme> advancedThemes;
	private String theme;
	
	@ManagedProperty(value="#{guestPreferencesBean}")
	private GuestPreferences gp;  
	
	public void setGp(GuestPreferences gp)
	{
		this.gp=gp;
	}
	
	public Map<String,String> getThemes()
	{
		return themes;
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
		
		advancedThemes=new ArrayList<Theme>();
		advancedThemes.add(new Theme("aristo","aristo.png"));
		advancedThemes.add(new Theme("cupertino","cupertino.png"));
		
		themes=new TreeMap<String,String>();
		themes.put("Aristo","aristo");
		themes.put("Blitzer","blitzer");
		themes.put("Bluesky","bluesky");
		themes.put("Casablanca","casablanca");
		themes.put("Cupertino","cupertino");
		themes.put("Eggplant","eggplant");
		themes.put("Excite-Bike","excite-bike");
		themes.put("Flick","flick");
		themes.put("Glass-X","glass-x");
		themes.put("Hot-Sneaks","hot-sneaks");
		themes.put("Overcast","overcast");
		themes.put("Pepper-Grinder","pepper-grinder");
		themes.put("South-Street","south-street");
		themes.put("Sunny","sunny");
		themes.put("UI-Lightness","ui-lightness");
	}
	
	public void saveTheme()
	{  
		gp.setTheme(theme);
	}
	
	public List<Theme> getAdvancedThemes()
	{
		return advancedThemes;
	}
}
