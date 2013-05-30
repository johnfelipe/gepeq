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

import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to resources of questions data.
 */
public class QuestionResourcesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new resource of a question to DB.
	 * @param questionResource Resource of a question to add
	 * @return Resource of a question identifier
	 * @throws DaoException
	 */
	public long saveQuestionResource(QuestionResource questionResource) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(questionResource)).longValue();
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
	 * Updates a resource of a question on DB.
	 * @param questionResource Resource of a question to update
	 * @throws DaoException
	 */
	public void updateQuestionResource(QuestionResource questionResource) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(questionResource);
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
	 * Deletes a resource of a question from DB.
	 * @param questionResource Resource of a question to delete
	 * @throws DaoException
	 */
	public void deleteQuestionResource(QuestionResource questionResource) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(questionResource);
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
	 * @param id Resource of a question identifier
	 * @return Resource of a question from DB
	 * @throws DaoException
	 */
	public QuestionResource getQuestionResource(long id) throws DaoException
	{
		QuestionResource questionResource=null;
		try
		{
			startOperation();
			questionResource=(QuestionResource)operation.session.get(QuestionResource.class,id);
			if (questionResource!=null)
			{
				Hibernate.initialize(questionResource.getQuestion());
				Hibernate.initialize(questionResource.getResource());
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
		return questionResource;
	}
	
	/**
	 * @param questionId Question identifier
	 * @return List of resources of a question
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<QuestionResource> getQuestionResources(long questionId) throws DaoException
	{
		List<QuestionResource> questionResources=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from QuestionResource q");
			if (questionId>0L)
			{
				queryString.append(" Where q.question = :questionId");
			}
			queryString.append(" Order by q.position");
			Query query=operation.session.createQuery(queryString.toString());
			if (questionId>0L)
			{
				query.setParameter("questionId",Long.valueOf(questionId),StandardBasicTypes.LONG);
			}
			questionResources=query.list();
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
		return questionResources;
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
		he.printStackTrace();
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
