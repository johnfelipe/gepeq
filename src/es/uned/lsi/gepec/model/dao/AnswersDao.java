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

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

/**
 * Manages access to answers data.
 */
public class AnswersDao
{  
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda una respuesta en la bd
	//param answer
	//return id generado por el dbms
	/**
	 * Adds a new answer to DB.
	 * @param answer Answer to add
	 * @return Answer identifier
	 * @throws DaoException
	 */
	public long saveAnswer(Answer answer) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(answer)).longValue();
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
	
	//Actualiza una respuesta en la bd
	/**
	 * Updates an answer on DB.
	 * @param answer Answer to update.
	 * @throws DaoException
	 */
	public void updateAnswer(Answer answer) throws DaoException
	{ 
		try 
		{ 
			startOperation();
			operation.session.update(answer);
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
	
	//Elimina una respuesta de la bd
	/**
	 * Deletes an answer from DB.
	 * @param answer Answer to delete
	 * @throws DaoException
	 */
	public void deleteAnswer(Answer answer) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(answer);
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
	
	//Obtiene una respuesta a partir de su id
	//return Answer con el id solicitado o null si no se encuentra
	/**
	 * @param id Answer identifier
	 * @return Answer from DB
	 * @throws DaoException
	 */
	public Answer getAnswer(long id) throws DaoException
	{
		Answer answer=null;
		try
		{
			startOperation();
			answer=(Answer)operation.session.get(Answer.class,id);
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
		return answer;
	}
	
	//Obtiene la lista de respuestas de una pregunta
	/**
	 * @param questionId Question identifier
	 * @return List of answers of a question
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Answer> getAnswers(long questionId) throws DaoException
	{
		List<Answer> answers=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Answer a");
			if (questionId>0L)
			{
				queryString.append(" Where a.question = :questionId");
			}
			queryString.append(" Order by a.position");
			Query query=operation.session.createQuery(queryString.toString());
			if (questionId>0L)
			{
				query.setParameter("questionId",Long.valueOf(questionId),StandardBasicTypes.LONG);
			}
			answers=query.list();
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
		return answers;
	}
	
	//Inicia una sesión e inicia una transacción contra el dbms
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
	
	//Maneja los errores producidos en la operación de acceso a datos
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
