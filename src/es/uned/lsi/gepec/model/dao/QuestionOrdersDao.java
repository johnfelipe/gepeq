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
package es.uned.lsi.gepec.model.dao;

import java.util.List;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to questions references data.
 */
public class QuestionOrdersDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new question reference to DB.
	 * @param questionOrder Question reference to add
	 * @return Question reference identifier
	 * @throws DaoException
	 */
	public long saveQuestionOrder(QuestionOrder questionOrder) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(questionOrder)).longValue();
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return id;
	}
	
	/**
	 * Updates a question reference on DB.
	 * @param questionOrder Question reference to update
	 * @throws DaoException
	 */
	public void updateQuestionOrder(QuestionOrder questionOrder) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(questionOrder);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	/**
	 * Deletes a question reference from DB.
	 * @param questionOrder Question reference to delete
	 * @throws DaoException
	 */
	public void deleteQuestionOrder(QuestionOrder questionOrder) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(questionOrder);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	/**
	 * @param id Question reference identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategories Flag to indicate if we need to initialize categories
	 * @return Question reference from DB
	 * @throws DaoException
	 */
	public QuestionOrder getQuestionOrder(long id,boolean includeUsers,boolean includeCategories)
		throws DaoException
	{
		QuestionOrder questionOrder=null;
		try
		{
			startOperation();
			questionOrder=(QuestionOrder)operation.session.get(QuestionOrder.class,id);
			if (questionOrder!=null)
			{
				Question question=questionOrder.getQuestion();
				Hibernate.initialize(question);
				if (includeUsers)
				{
					if (includeCategories)
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
						Category category=question.getCategory();
						Hibernate.initialize(category);
						Hibernate.initialize(category.getUser());
					}
					else
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
					}
				}
				else if (includeCategories)
				{
					Category category=question.getCategory();
					Hibernate.initialize(category);
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return questionOrder;
	}
	
	/**
	 * @param sectionId Section identifier
	 * @param order Question reference order (position within section)
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategories Flag to indicate if we need to initialize categories
	 * @return Question reference from DB
	 * @throws DaoException
	 */
	public QuestionOrder getQuestionOrder(long sectionId,int order,boolean includeUsers,
		boolean includeCategories) throws DaoException
	{
		QuestionOrder questionOrder=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from QuestionOrder q where q.section = :sectionId and q.order = :order");
			query.setParameter("sectionId",Long.valueOf(sectionId),StandardBasicTypes.LONG);
			query.setParameter("order",Integer.valueOf(order),StandardBasicTypes.INTEGER);
			questionOrder=(QuestionOrder)query.uniqueResult();
			if (questionOrder!=null)
			{
				Question question=questionOrder.getQuestion();
				Hibernate.initialize(question);
				if (includeUsers)
				{
					if (includeCategories)
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
						Category category=question.getCategory();
						Hibernate.initialize(category);
						Hibernate.initialize(category.getUser());
					}
					else
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
					}
				}
				else if (includeCategories)
				{
					Category category=question.getCategory();
					Hibernate.initialize(category);
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return questionOrder;
	}
	
	/**
	 * @param sectionId Section identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategories Flag to indicate if we need to initialize categories
	 * @return List of question references of a section
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public List<QuestionOrder> getQuestionOrders(long sectionId,boolean includeUsers,boolean includeCategories)
		throws DaoException
	{
		List<QuestionOrder> questionOrders=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from QuestionOrder q where q.section = :sectionId order by q.order");
			query.setParameter("sectionId",Long.valueOf(sectionId),StandardBasicTypes.LONG);
			questionOrders=query.list();
			for (QuestionOrder questionOrder:questionOrders)
			{
				Question question=questionOrder.getQuestion();
				Hibernate.initialize(question);
				if (includeUsers)
				{
					if (includeCategories)
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
						Category category=question.getCategory();
						Hibernate.initialize(category);
						Hibernate.initialize(category.getUser());
					}
					else
					{
						Hibernate.initialize(question.getCreatedBy());
						Hibernate.initialize(question.getModifiedBy());
					}
				}
				else if (includeCategories)
				{
					Category category=question.getCategory();
					Hibernate.initialize(category);
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return questionOrders;
	}
	
	/**
	 * Starts a session and transaction against DBMS if needed.
	 * @throws DaoException
	 */
	private void startOperation() throws DaoException
	{
		try
		{
			if (operation==null)
			{
				operation=HibernateUtil.startOperation();
				singleOp=true;
			}
		}
		catch (HibernateException he)
		{
			operation=null;
			handleException(he,false);
			throw new DaoException(he);
		}
	}
	
	/**
	 * Sets a session and transaction against DBMS.
	 * @param operation Operation with started session and transaction
	 */
	public void setOperation(Operation operation)
	{
		this.operation=operation;
		singleOp=false;
	}
	
	/**
	 * Ends an operation, ending session and transaction against DBMS if this is a single operation.
	 * @throws DaoException
	 */
	private void endOperation() throws DaoException
	{
		try
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		catch (HibernateException he)
		{
			handleException(he,false);
			throw new DaoException(he);
		}
		finally
		{
			operation=null;
		}
	}
	
	/**
	 * Manage errors produced while accesing persistent data.<br/><br/>
	 * It also does a rollback.
	 * @param he Exception to handle
	 * @throws DaoException
	 */
	private void handleException(HibernateException he) throws DaoException
	{
		handleException(he,true);
	}
	
	/**
	 * Manage errors produced while accesing persistent data.
	 * @param he Exception to handle
	 * @param doRollback Flag to indicate to do a rollback
	 * @throws DaoException
	 */
	private void handleException(HibernateException he,boolean doRollback) throws DaoException 
	{ 
		String errorMessage=null;
		FacesContext facesContext=FacesContext.getCurrentInstance();
		if (facesContext==null)
		{
			errorMessage="Access error to the data layer";
		}
		else
		{
			ELContext elContext=facesContext.getELContext();
			LocalizationService localizationService=(LocalizationService)FacesContext.getCurrentInstance().
				getApplication().getELResolver().getValue(elContext,null,"localizationService");
			errorMessage=localizationService.getLocalizedMessage("ERROR_ACCESS_DATA_LAYER");
		}
		if (doRollback)
		{
			operation.rollback();
		}
		throw new DaoException(errorMessage,he); 
	}
}
