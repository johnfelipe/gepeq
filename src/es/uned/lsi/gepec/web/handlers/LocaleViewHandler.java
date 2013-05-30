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
package es.uned.lsi.gepec.web.handlers;

import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.sun.faces.application.view.MultiViewHandler;

// ViewHandler personalizado con soporte para internacionalización
// author Víctor Manuel Alonso Rodríguez
// since  12/2011
/**
 * Custom view handler supporting internationalization.
 */
public class LocaleViewHandler extends MultiViewHandler
{
	@Override
	public Locale calculateLocale(FacesContext context)
	{
		HttpSession session=(HttpSession)context.getExternalContext().getSession(false);
		if (session!= null)
		{
			String langCode=(String)session.getAttribute("es.uned.lsi.gepec.langCode");
			if (langCode!=null)
			{
				return new Locale(langCode);
			}
		}
		return super.calculateLocale(context);
	}
}
