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

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.QuestionResourcesDao;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages resources of questions.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class QuestionResourcesService implements Serializable
{
	private final static QuestionResourcesDao QUESTION_RESOURCES_DAO=new QuestionResourcesDao();
	
	public QuestionResourcesService()
	{
	}
	
	/**
	 * @param id Resource of question identifier
	 * @return Resource of question
	 * @throws ServiceException
	 */
	public QuestionResource getQuestionResource(long id) throws ServiceException
	{
		return getQuestionResource((Operation)null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Resource of question identifier
	 * @return Resource of question
	 * @throws ServiceException
	 */
	public QuestionResource getQuestionResource(Operation operation,long id) throws ServiceException
	{
		QuestionResource questionResource=null;
		try
		{
			QUESTION_RESOURCES_DAO.setOperation(operation);
			questionResource=QUESTION_RESOURCES_DAO.getQuestionResource(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionResource;
	}
	
	/**
	 * Updates a resource of question.
	 * @param questionResource Resource of question to update
	 * @throws ServiceException
	 */
	public void updateQuestionResource(QuestionResource questionResource) throws ServiceException
	{
		updateQuestionResource(null,questionResource);
	}
	
	/**
	 * Updates a resource of question.
	 * @param operation Operation
	 * @param questionResource Resource of question to update
	 * @throws ServiceException
	 */
	public void updateQuestionResource(Operation operation,QuestionResource questionResource) 
		throws ServiceException
	{
		try
		{
			QUESTION_RESOURCES_DAO.setOperation(operation);
			QUESTION_RESOURCES_DAO.updateQuestionResource(questionResource);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Adds a new resource of question.
	 * @param questionResource Resource of question to add
	 * @throws ServiceException
	 */
	public void addQuestionResource(QuestionResource questionResource) throws ServiceException
	{
		addQuestionResource(null,questionResource);
	}
	
	/**
	 * Adds a new resource of question.
	 * @param operation Operation
	 * @param questionResource Resource of question to add
	 * @throws ServiceException
	 */
	public void addQuestionResource(Operation operation,QuestionResource questionResource) throws ServiceException
	{
		try
		{
			QUESTION_RESOURCES_DAO.setOperation(operation);
			QUESTION_RESOURCES_DAO.saveQuestionResource(questionResource);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a resource of question.
	 * @param questionResource Resource of question to delete
	 * @throws ServiceException
	 */
	public void deleteQuestionResource(QuestionResource questionResource) throws ServiceException
	{
		deleteQuestionResource(null,questionResource);
	}
	
	/**
	 * Deletes a resource of question.
	 * @param operation Operation
	 * @param questionResource Resource of question to delete
	 * @throws ServiceException
	 */
	public void deleteQuestionResource(Operation operation,QuestionResource questionResource)
		throws ServiceException
	{
		try
		{
			QUESTION_RESOURCES_DAO.setOperation(operation);
			QUESTION_RESOURCES_DAO.deleteQuestionResource(questionResource);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param question Question
	 * @return List of resources of a question
	 * @throws ServiceException
	 */
	public List<QuestionResource> getQuestionResources(Question question) throws ServiceException
	{
		return getQuestionResources(null,question==null?0L:question.getId());
	}
	
	/**
	 * @param questionId Question identifier
	 * @return List of resources of a question
	 * @throws ServiceException
	 */
	public List<QuestionResource> getQuestionResources(long questionId) throws ServiceException
	{
		return getQuestionResources(null,questionId);
	}
	
	/**
	 * @param operation Operation
	 * @param question Question
	 * @return List of resources of a question
	 * @throws ServiceException
	 */
	public List<QuestionResource> getQuestionResources(Operation operation,Question question)
		throws ServiceException
	{
		return getQuestionResources(operation,question==null?0L:question.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param questionId Question identifier
	 * @return List of resources of a question
	 * @throws ServiceException
	 */
	public List<QuestionResource> getQuestionResources(Operation operation,long questionId) throws ServiceException
	{
		List<QuestionResource> questionResources=null;
		try
		{
			QUESTION_RESOURCES_DAO.setOperation(operation);
			questionResources=QUESTION_RESOURCES_DAO.getQuestionResources(questionId);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionResources;
	}
}
