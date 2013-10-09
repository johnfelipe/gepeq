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
import es.uned.lsi.gepec.web.backbeans.QuestionOrderBean;
import es.uned.lsi.gepec.web.backbeans.SectionBean;

/**
 * JSF 2 converter for question references (question orders)
 */
@FacesConverter("QuestionOrderBeanConverter")
public class QuestionOrderBeanConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		int separatorIndex=newValue.indexOf(':');
		int sectionPos=Integer.parseInt(newValue.substring(0,separatorIndex));
		int questionOrderPos=Integer.parseInt(newValue.substring(separatorIndex+1));
		
		// Get EL context
		ELContext elContext=context.getELContext();
		
		// Get EL resolver
		ELResolver resolver=context.getApplication().getELResolver();
		
		// We get TestBean from EL resolver
		TestBean testBean=(TestBean)resolver.getValue(elContext,null,"testBean");
		
		SectionBean sectionBean=null;
		for (SectionBean sb:testBean.getSections())
		{
			if (sb.getOrder()==sectionPos)
			{
				sectionBean=sb;
				break;
			}
		}
		QuestionOrderBean questionOrderBean=null;
		if (sectionBean!=null)
		{
			for (QuestionOrderBean qob:sectionBean.getQuestionOrders())
			{
				if (qob.getOrder()==questionOrderPos)
				{
					questionOrderBean=qob;
					break;
				}
			}
		}
		return questionOrderBean;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		QuestionOrderBean questionOrder=(QuestionOrderBean)value;
		StringBuffer questionOrderStr=new StringBuffer();
		questionOrderStr.append(questionOrder.getSectionOrder());
		questionOrderStr.append(':');
		questionOrderStr.append(questionOrder.getOrder());
		return questionOrderStr.toString();
	}
}
