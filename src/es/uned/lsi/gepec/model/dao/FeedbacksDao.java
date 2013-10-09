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

import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to question feedbacks data. 
 */
public class FeedbacksDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new question feedback to DB.
	 * @param feedback Question feedback
	 * @return Question feedback identifier 
	 * @throws DaoException
	 */
	public long saveFeedback(Feedback feedback) throws DaoException
	{
		long id=0;
		try
		{
			startOperation();
			id=((Long)operation.session.save(feedback)).longValue();
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
	 * Update a question feedback on BD.
	 * @param feedback Question feedback
	 * @throws DaoException
	 */
	public void updateFeedback(Feedback feedback) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(feedback);
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
	 * Deletes a question feedback from BD.
	 * @param feedback Question feedback
	 * @throws DaoException
	 */
	public void deleteFeedback(Feedback feedback) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(feedback);
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
	 * @param id Question feedback identifier
	 * @return Question feedback from DB
	 * @throws DaoException
	 */
	public Feedback getFeedback(long id) throws DaoException
	{
		Feedback feedback=null;
		try
		{
			startOperation();
			feedback=(Feedback)operation.session.get(Feedback.class,id);
			if (feedback!=null)
			{
				Hibernate.initialize(feedback.getQuestion());
				if (feedback.getResource()!=null)
				{
					Hibernate.initialize(feedback.getResource());
				}
				Hibernate.initialize(feedback.getFeedbackType());
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
		return feedback;
	}
	
	/**
	 * @param questionId Question identifier
	 * @return List of feedbacks of a question
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Feedback> getFeedbacks(long questionId) throws DaoException
	{
		List<Feedback> feedbacks=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Feedback f");
			if (questionId>0L)
			{
				queryString.append(" where f.question = :questionId");
			}
			queryString.append(" Order by f.position");
			Query query=operation.session.createQuery(queryString.toString());
			if (questionId>0L)
			{
				query.setParameter("questionId",Long.valueOf(questionId),StandardBasicTypes.LONG);
			}
			feedbacks=query.list();
			for (Feedback feedback:feedbacks)
			{
				Hibernate.initialize(feedback.getQuestion());
				if (feedback.getResource()!=null)
				{
					Hibernate.initialize(feedback.getResource());
				}
				Hibernate.initialize(feedback.getFeedbackType());
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
		return feedbacks;
	}
	
	/**
	 * Starts a session and transaction against DBMS if needed.
	 * @throws HibernateException
	 */
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
	 * @param he Hibernate Exception
	 * @throws DaoException
	 */
	private void handleException(HibernateException he) throws DaoException
	{
		handleException(he,true);
	}
	
	/**
	 * Manage errors produced while accesing persistent data.
	 * @param he Hibernate Exception
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
