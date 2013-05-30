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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import es.uned.lsi.gepec.model.entities.Resource;

/**
 * JSF 2 converter for images.
 */
@FacesConverter("ImageConverter")
public class ImageConverter extends ResourceConverter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		// We get the resource identifier
		long id=Long.parseLong(newValue);
		
		// We get the resource
		Resource image=null;
		if (id==-1L)
		{
			image=new Resource();
			image.setId(-1L);
			ResourceBundle rb=context.getApplication().getResourceBundle(context,"msgs");
			String message=rb.getString("NO_IMAGE");
			image.setName(message);
		}
		else
		{
			image=(Resource)super.getAsObject(context,component,newValue);
		}
		return image;
	}
}
