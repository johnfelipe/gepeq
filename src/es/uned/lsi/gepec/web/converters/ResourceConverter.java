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
package es.uned.lsi.gepec.web.converters;

import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.UserSessionService;

//Convertidor de recursos para JSF
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * JSF 2 converter for resources.
 */
@FacesConverter("ResourceConverter")
public class ResourceConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		// We get the resource identifier
		long id=Long.parseLong(newValue);
		
		// We get the resource
		Resource resource=null;
		if (id>0L)
		{
			// Get EL context
			ELContext elContext=context.getELContext();
			
			// Get EL resolver
			ELResolver resolver=context.getApplication().getELResolver();
			
			// We get UserSessionService from EL resolver
			UserSessionService userSessionService=
				(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
			
			// We get ResourcesService from EL resolver
			ResourcesService resourcesService=(ResourcesService)resolver.getValue(elContext,null,"resourcesService");
			
			resource=resourcesService.getResource(userSessionService.getCurrentUserOperation(),id);
		}
		else if (id==-1L)
		{
			resource=new Resource();
			resource.setId(-1L);
			ResourceBundle rb=context.getApplication().getResourceBundle(context,"msgs");
			String message=rb.getString("NO_RESOURCE");
			resource.setName(message);
		}
		return resource;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return Long.toString(((Resource)value).getId());
	}
}
