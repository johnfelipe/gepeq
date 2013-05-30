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

import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.UserSessionService;

// Convertidor de preguntas para JSF
// author Víctor Manuel Alonso Rodríguez
// since  12/2011
/**
 * JSF 2 converter for draggable items and answers of "Drag & Drop" questions.
 */
@FacesConverter("QuestionConverter")
public class QuestionConverter implements Converter
{
	@Override
	public Object getAsObject(FacesContext context,UIComponent component,String newValue)
	{
		// We get question's identifier
		long id=Long.parseLong(newValue);
		
		// Get EL context
		ELContext elContext=context.getELContext();
		
		// Get EL resolver
		ELResolver resolver=context.getApplication().getELResolver();
		
		// We get UserSessionService from EL resolver
		UserSessionService userSessionService=(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
		
		// We get QuestionsService from EL resolver
		QuestionsService questionsService=(QuestionsService)resolver.getValue(elContext,null,"questionsService");
		
		// We get question
		Question question=questionsService.getQuestion(userSessionService.getCurrentUserOperation(),id);
		
		return question;
	}
	
	@Override
	public String getAsString(FacesContext context,UIComponent component,Object value)
	{
		return Long.toString(((Question)value).getId());
	}
}
