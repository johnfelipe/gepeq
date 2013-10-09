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

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.web.QuestionBean;

/**
 * JSF 2 converter for answers.
 */
@FacesConverter("AnswerConverter")
public class AnswerConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		Answer answer=null;
		int position=Integer.parseInt(newValue);
		QuestionBean questionBean=(QuestionBean)context.getApplication().getELResolver().getValue(
			context.getELContext(),null,"questionBean");
		Question question=questionBean.getQuestion();
		if (question!=null)
		{
			answer=question.getAnswer(position);
		}
		return answer;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return Integer.toString(((Answer)value).getPosition());
	}
}
