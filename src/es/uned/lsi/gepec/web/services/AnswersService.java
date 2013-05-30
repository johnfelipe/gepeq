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
package es.uned.lsi.gepec.web.services;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.AnswersDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages answers.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class AnswersService implements Serializable
{
	private final static AnswersDao ANSWERS_DAO=new AnswersDao();
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	public AnswersService()
	{
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/**
	 * @param id Answer identifier
	 * @return Answer
	 * @throws ServiceException
	 */
	public Answer getAnswer(long id) throws ServiceException
	{
		return getAnswer(null,id);
	}
	
	/**
	 * @param id Answer identifier
	 * @param operation Operation
	 * @return Answer
	 * @throws ServiceException
	 */
	public Answer getAnswer(Operation operation,long id) throws ServiceException
	{
		Answer answer=null;
		try
		{
			ANSWERS_DAO.setOperation(operation);
			answer=ANSWERS_DAO.getAnswer(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de); 
		}
		return answer;
	}
	
	/**
	 * Updates an answer.
	 * @param answer Answer to update
	 * @throws ServiceException
	 */
	public void updateAnswer(Answer answer) throws ServiceException
	{
		updateAnswer(null,answer);
	}
	
	/**
	 * Updates an answer.
	 * @param operation Operation
	 * @param answer Answer to update
	 * @throws ServiceException
	 */
	public void updateAnswer(Operation operation,Answer answer) throws ServiceException
	{
		try
		{
			ANSWERS_DAO.setOperation(operation);
			ANSWERS_DAO.updateAnswer(answer);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Adds a new answer.
	 * @param answer Answer to add
	 * @throws ServiceException
	 */
	public void addAnswer(Answer answer) throws ServiceException
	{
		addAnswer(null,answer);
	}
	
	/**
	 * Adds a new answer.
	 * @param operation Operation
	 * @param answer Answer to add
	 * @throws ServiceException
	 */
	public void addAnswer(Operation operation,Answer answer) throws ServiceException
	{
		try
		{
			ANSWERS_DAO.setOperation(operation);
			ANSWERS_DAO.saveAnswer(answer);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes an answer.
	 * @param answer Answer to delete
	 * @throws ServiceException
	 */
	public void deleteAnswer(Answer answer) throws ServiceException
	{
		deleteAnswer(null,answer);
	}
	
	/**
	 * Deletes an answer.
	 * @param operation Operation
	 * @param answer Answer to delete
	 * @throws ServiceException
	 */
	public void deleteAnswer(Operation operation,Answer answer) throws ServiceException
	{
		try
		{
			ANSWERS_DAO.setOperation(operation);
			ANSWERS_DAO.deleteAnswer(answer);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param question Question
	 * @return List of answers of a question
	 * @throws ServiceException
	 */
	public List<Answer> getAnswers(Question question) throws ServiceException
	{
		return getAnswers(null,question==null?0L:question.getId());
	}
	
	/**
	 * @param questionId Question identifier
	 * @return List of answers of a question
	 * @throws ServiceException
	 */
	public List<Answer> getAnswers(long questionId) throws ServiceException
	{
		return getAnswers(null,questionId); 
	}
	
	/**
	 * @param operation Operation
	 * @param question Question
	 * @return List of answers of a question
	 * @throws ServiceException
	 */
	public List<Answer> getAnswers(Operation operation,Question question) throws ServiceException
	{
		return getAnswers(operation,question==null?0L:question.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param questionId Question identifier
	 * @return List of answers of a question
	 * @throws ServiceException
	 */
	public List<Answer> getAnswers(Operation operation,long questionId) throws ServiceException
	{
		List<Answer> answers=null;
		try
		{
			ANSWERS_DAO.setOperation(operation);
			answers=ANSWERS_DAO.getAnswers(questionId);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return answers; 
	}
	
	/**
	 * @param className Class name of question to instantiate
	 * @return New question
	 * @throws ServiceException
	 */
	@SuppressWarnings("rawtypes")
	public Answer getNewAnswer(String className) throws ServiceException
	{
		Answer answer=null;
		try
		{
			Class c=Class.forName(className);
			Object o=c.newInstance();
			answer=(Answer)o;
		}
		catch (Exception ex)
		{
			throwServiceException("ANSWER_NEW_INSTANCE_ERROR",
				"A critical error has been ocurred when trying to instantiate the answer.",ex);
		}
		return answer;
	}
	
	/**
	 * Throws a new ServiceException with the localized message get from error code or the plain message 
	 * if localization fails.
	 * @param errorCode Error code to get localized error message
	 * @param plainMessage Error message to be used if localization fails
	 * @throws ServiceException
	 */
	@SuppressWarnings("unused")
	private void throwServiceException(String errorCode,String plainMessage) throws ServiceException
	{
		throwServiceException(errorCode,plainMessage,null);
	}
	
	/**
	 * Throws a new ServiceException with the localized message get from error code or the plain message 
	 * if localization fails.
	 * @param errorCode Error code to get localized error message
	 * @param plainMessage Error message to be used if localization fails
	 * @param cause Exception that caused this exception
	 * @throws ServiceException
	 */
	private void throwServiceException(String errorCode,String plainMessage,Throwable cause) 
		throws ServiceException
	{
		String errorMessage=null;
		try
		{
			errorMessage=localizationService.getLocalizedMessage(errorCode);
		}
		catch (ServiceException se)
		{
			errorMessage=null;
		}
		if (errorMessage==null)
		{
			errorMessage=plainMessage;
		}
		throw new ServiceException(errorMessage,cause);
	}
}
