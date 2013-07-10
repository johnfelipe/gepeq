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

import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.web.QuestionBean;

/**
 * JSF 2 converter for resources of a question.
 */
@FacesConverter("QuestionResourceConverter")
public class QuestionResourceConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		QuestionResource questionResource=null;
		int position=Integer.parseInt(newValue);
		QuestionBean questionBean=(QuestionBean)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"questionBean");
		Question question=questionBean.getQuestion(questionBean.getCurrentUserOperation(null));
		if (question!=null)
		{
			questionResource=question.getQuestionResource(position);
		}
		return questionResource;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return Integer.toString(((QuestionResource)value).getPosition());
	}
}