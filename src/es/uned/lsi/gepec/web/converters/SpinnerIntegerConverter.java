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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * JSF 2 converter for spinners (used with Integer objects).
 */
@FacesConverter("SpinnerIntegerConverter")
public class SpinnerIntegerConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		int iValue;
		try
		{
			iValue=Integer.parseInt(newValue);
		}
		catch (NumberFormatException e)
		{
			// If value is not a valid integer it is because it is very big, so we set the maximum integer value
			// possible to allow spinner to be updated so we can detect and handle this user input
			iValue=Integer.MAX_VALUE;
		}
		return new Integer(iValue);
	}

	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return ((Integer)value).toString();
	}
}
