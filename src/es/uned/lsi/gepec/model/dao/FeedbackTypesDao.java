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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import es.uned.lsi.gepec.model.entities.FeedbackType;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to user feedback types data.
 */
public class FeedbackTypesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new feedback type to DB.
	 * @param feedbackType Feedback type
	 * @return Feedback type identifier
	 * @throws DaoException
	 */
	public long saveFeedbackType(FeedbackType feedbackType) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(feedbackType)).longValue();
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
	 * Updates a feedback type on DB.
	 * @param feedbackType Feedback type
	 * @throws DaoException
	 */
	public void updateFeedbackType(FeedbackType feedbackType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(feedbackType);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException();
		}
		finally
		{
			endOperation();
		}
	}
	
	/**
	 * Deletes a feedback type from DB.
	 * @param feedbackType Feedback type
	 * @throws DaoException
	 */
	public void deleteFeedbackType(FeedbackType feedbackType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(feedbackType);
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
	 * @param id Feedback type identifier
	 * @return Feedback type from DB
	 * @throws DaoException
	 */
	public FeedbackType getFeedbackType(long id) throws DaoException
	{
		FeedbackType feedbackType=null;
		try
		{
			startOperation();
			feedbackType=(FeedbackType)operation.session.get(FeedbackType.class,id);
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
		return feedbackType;
	}
	
	/**
	 * @param type Feedback type string
	 * @return Feedback type from DB
	 * @throws DaoException
	 */
	public FeedbackType getFeedbackType(String type) throws DaoException
	{
		FeedbackType feedbackType=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from FeedbackType f where f.type = :type");
			query.setParameter("type",type,StandardBasicTypes.STRING);
			feedbackType=(FeedbackType)query.uniqueResult();
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
		return feedbackType;
	}
	
	/**
	 * @return List of all feedback types
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<FeedbackType> getFeedbackTypes() throws DaoException
	{
		List<FeedbackType> feedbackTypes=null;
		try
		{
			startOperation();
			feedbackTypes=operation.session.createQuery("from FeedbackType").list();
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
		return feedbackTypes;
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
