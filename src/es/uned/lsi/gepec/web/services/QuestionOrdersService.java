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
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.QuestionOrdersDao;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages question references of a section.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class QuestionOrdersService implements Serializable
{
	private final static QuestionOrdersDao QUESTION_ORDERS_DAO=new QuestionOrdersDao();
	
	public QuestionOrdersService()
	{
	}
	
	/**
	 * @param id Question reference identifier
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(long id) throws ServiceException
	{
		return getQuestionOrder(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Question reference identifier
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(Operation operation,long id) throws ServiceException
	{
		QuestionOrder questionOrder=null;
		try
		{
			// Get question reference from DB
			QUESTION_ORDERS_DAO.setOperation(operation);
			QuestionOrder questionOrderFromDB=QUESTION_ORDERS_DAO.getQuestionOrder(id,true,true);
			if (questionOrderFromDB!=null)
			{
				questionOrder=questionOrderFromDB.getQuestionOrderCopy();
				if (questionOrderFromDB.getQuestion()!=null)
				{
					Question questionFromDB=questionOrderFromDB.getQuestion();
					Question question=questionFromDB.getQuestionCopy();
					if (questionFromDB.getCreatedBy()!=null)
					{
						User questionAuthor=questionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionAuthor.setPassword("");
						
						question.setCreatedBy(questionAuthor);
					}
					if (questionFromDB.getModifiedBy()!=null)
					{
						User questionLastEditor=questionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionLastEditor.setPassword("");
						
						question.setModifiedBy(questionLastEditor);
					}
					if (questionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=questionFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						question.setCategory(category);
					}
					questionOrder.setQuestion(question);
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionOrder;
	}
	
	/**
	 * @param section Section
	 * @param order Question reference order (position within section)
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(Section section,int order) throws ServiceException
	{
		return getQuestionOrder(null,section==null?0L:section.getId(),order);
	}
	
	/**
	 * @param sectionId Section identifier
	 * @param order Question reference order (position within section)
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(long sectionId,int order) throws ServiceException
	{
		return getQuestionOrder(null,sectionId,order);
	}
	
	/**
	 * @param operation Operation
	 * @param section Section
	 * @param order Question reference order (position within section)
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(Operation operation,Section section,int order) throws ServiceException
	{
		return getQuestionOrder(operation,section==null?0L:section.getId(),order);
	}
	
	/**
	 * @param operation Operation
	 * @param sectionId Section identifier
	 * @param order Question reference order (position within section)
	 * @return Question reference
	 * @throws ServiceException
	 */
	public QuestionOrder getQuestionOrder(Operation operation,long sectionId,int order) throws ServiceException
	{
		QuestionOrder questionOrder=null;
		try
		{
			// Get question reference from DB
			QUESTION_ORDERS_DAO.setOperation(operation);
			QuestionOrder questionOrderFromDB=QUESTION_ORDERS_DAO.getQuestionOrder(sectionId,order,true,true);
			if (questionOrderFromDB!=null)
			{
				questionOrder=questionOrderFromDB.getQuestionOrderCopy();
				if (questionOrderFromDB.getQuestion()!=null)
				{
					Question questionFromDB=questionOrderFromDB.getQuestion();
					Question question=questionFromDB.getQuestionCopy();
					if (questionFromDB.getCreatedBy()!=null)
					{
						User questionAuthor=questionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionAuthor.setPassword("");
						
						question.setCreatedBy(questionAuthor);
					}
					if (questionFromDB.getModifiedBy()!=null)
					{
						User questionLastEditor=questionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionLastEditor.setPassword("");
						
						question.setModifiedBy(questionLastEditor);
					}
					if (questionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=questionFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						question.setCategory(category);
					}
					questionOrder.setQuestion(question);
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionOrder;
	}
	
	/**
	 * Adds a new question reference.
	 * @param questionOrder Question reference to add
	 * @throws ServiceException
	 */
	public void addQuestionOrder(QuestionOrder questionOrder) throws ServiceException
	{
		addQuestionOrder(null,questionOrder);
	}
	
	/**
	 * Adds a new question reference.
	 * @param operation Operation
	 * @param questionOrder Question reference to add
	 * @throws ServiceException
	 */
	public void addQuestionOrder(Operation operation,QuestionOrder questionOrder) throws ServiceException
	{
		try
		{
			// Add a new question reference
			QUESTION_ORDERS_DAO.setOperation(operation);
			QUESTION_ORDERS_DAO.saveQuestionOrder(questionOrder);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a question reference.
	 * @param questionOrder Question reference to update
	 * @throws ServiceException
	 */
	public void updateQuestionOrder(QuestionOrder questionOrder) throws ServiceException
	{
		updateQuestionOrder(null,questionOrder);
	}
	
	/**
	 * Updates a question reference.
	 * @param operation Operation
	 * @param questionOrder Question reference to update
	 * @throws ServiceException
	 */
	public void updateQuestionOrder(Operation operation,QuestionOrder questionOrder) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get question reference from DB
			QUESTION_ORDERS_DAO.setOperation(operation);
			QuestionOrder questionOrderFromDB=QUESTION_ORDERS_DAO.getQuestionOrder(questionOrder.getId(),false,false);
			
			// Set fields with the updated values
			questionOrderFromDB.setFromOtherQuestionOrder(questionOrder);
			
			// Update question reference
			QUESTION_ORDERS_DAO.setOperation(operation);
			QUESTION_ORDERS_DAO.updateQuestionOrder(questionOrderFromDB);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	/**
	 * Deletes a question reference.
	 * @param questionOrder Question reference to delete
	 * @throws ServiceException
	 */
	public void deleteQuestionOrder(QuestionOrder questionOrder) throws ServiceException
	{
		deleteQuestionOrder(null,questionOrder);
	}
	
	/**
	 * Deletes a question reference.
	 * @param operation Operation
	 * @param questionOrder Question reference to delete
	 * @throws ServiceException
	 */
	public void deleteQuestionOrder(Operation operation,QuestionOrder questionOrder) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get question reference from DB
			QUESTION_ORDERS_DAO.setOperation(operation);
			QuestionOrder questionOrderFromDB=
				QUESTION_ORDERS_DAO.getQuestionOrder(questionOrder.getId(),false,false);
			
			// Delete question reference
			QUESTION_ORDERS_DAO.setOperation(operation);
			QUESTION_ORDERS_DAO.deleteQuestionOrder(questionOrderFromDB);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	/**
	 * @param section Section
	 * @return List of question references of a section
	 * @throws ServiceException
	 */
	public List<QuestionOrder> getQuestionOrders(Section section) throws ServiceException
	{
		return getQuestionOrders(null,section==null?0L:section.getId());
	}
	
	/**
	 * @param sectionId Section identifier
	 * @return List of question references of a section
	 * @throws ServiceException
	 */
	public List<QuestionOrder> getQuestionOrders(long sectionId) throws ServiceException
	{
		return getQuestionOrders(null,sectionId); 
	}
	
	/**
	 * @param operation Operation
	 * @param section Section
	 * @return List of question references of a section
	 * @throws ServiceException
	 */
	public List<QuestionOrder> getQuestionOrders(Operation operation,Section section) throws ServiceException
	{
		return getQuestionOrders(operation,section==null?0L:section.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param sectionId Section identifier
	 * @return List of question references of a section
	 * @throws ServiceException
	 */
	public List<QuestionOrder> getQuestionOrders(Operation operation,long sectionId) throws ServiceException
	{
		List<QuestionOrder> questionOrders=null;
		try
		{
			QUESTION_ORDERS_DAO.setOperation(operation);
			List<QuestionOrder> questionOrdersFromDB=QUESTION_ORDERS_DAO.getQuestionOrders(sectionId,true,true);
			
			// We return new referenced question references within a new list to avoid shared collection references
			// and object references to unsaved transient instances
			questionOrders=new ArrayList<QuestionOrder>(questionOrdersFromDB.size());
			for (QuestionOrder questionOrderFromDB:questionOrdersFromDB)
			{
				QuestionOrder questionOrder=questionOrderFromDB.getQuestionOrderCopy();
				if (questionOrderFromDB.getQuestion()!=null)
				{
					Question questionFromDB=questionOrderFromDB.getQuestion();
					Question question=questionFromDB.getQuestionCopy();
					if (questionFromDB.getCreatedBy()!=null)
					{
						User questionAuthor=questionFromDB.getCreatedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionAuthor.setPassword("");
						
						question.setCreatedBy(questionAuthor);
					}
					if (questionFromDB.getModifiedBy()!=null)
					{
						User questionLastEditor=questionFromDB.getModifiedBy().getUserCopy();
						
						// Password is set to empty string before returning instance for security reasons
						questionLastEditor.setPassword("");
						
						question.setModifiedBy(questionLastEditor);
					}
					if (questionFromDB.getCategory()!=null)
					{
						Category categoryFromDB=questionFromDB.getCategory();
						Category category=categoryFromDB.getCategoryCopy();
						if (categoryFromDB.getUser()!=null)
						{
							User categoryUser=categoryFromDB.getUser().getUserCopy();
							
							// Password is set to empty string before returning instance for security reasons
							categoryUser.setPassword("");
							
							category.setUser(categoryUser);
						}
						question.setCategory(category);
					}
					questionOrder.setQuestion(question);
				}
				questionOrders.add(questionOrder);
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return questionOrders;
	}
}
