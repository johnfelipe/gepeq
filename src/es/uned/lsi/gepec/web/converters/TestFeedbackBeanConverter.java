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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import es.uned.lsi.gepec.web.TestBean;
import es.uned.lsi.gepec.web.backbeans.TestFeedbackBean;
import es.uned.lsi.gepec.web.services.UserSessionService;

/**
 * JSF 2 converter for test feedbacks beans.
 */
@FacesConverter("TestFeedbackBeanConverter")
public class TestFeedbackBeanConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		int testFeedbackPos=Integer.parseInt(newValue);
		
		// Get EL context
		ELContext elContext=context.getELContext();
		
		// Get EL resolver
		ELResolver resolver=context.getApplication().getELResolver();
		
		// We get UserSessionService from EL resolver
		UserSessionService userSessionService=(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
		
		// We get TestBean from EL resolver
		TestBean testBean=(TestBean)resolver.getValue(elContext,null,"testBean");
		
		TestFeedbackBean testFeedbackBean=null;
		for (TestFeedbackBean tfb:testBean.getFeedbacks(userSessionService.getCurrentUserOperation()))
		{
			if (tfb.getPosition()==testFeedbackPos)
			{
				testFeedbackBean=tfb;
				break;
			}
		}
		return testFeedbackBean;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return Integer.toString(((TestFeedbackBean)value).getPosition());
	}
}
