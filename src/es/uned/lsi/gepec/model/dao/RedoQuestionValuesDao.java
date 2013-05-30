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

import es.uned.lsi.gepec.model.entities.RedoQuestionValue;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to available values of property 'redoQuestion'.
 */
public class RedoQuestionValuesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new available value of property 'redoQuestion' to DB.
	 * @param redoQuestion Value of property 'redoQuestion'
	 * @return Value of property 'redoQuestion' identifier
	 * @throws DaoException
	 */
	public long saveRedoQuestion(RedoQuestionValue redoQuestion)throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(redoQuestion)).longValue();
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
	 * Updates an available value of property 'redoQuestion' on DB.
	 * @param redoQuestion Value of property 'redoQuestion'
	 * @throws DaoException
	 */
	public void updateRedoQuestion(RedoQuestionValue redoQuestion) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(redoQuestion);
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
	 * Deletes an available value of property 'redoQuestion' from DB.
	 * @param redoQuestion Value of property 'redoQuestion'
	 * @throws DaoException
	 */
	public void deleteRedoQuestion(RedoQuestionValue redoQuestion) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(redoQuestion);
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
	 * @param id Value of property 'redoQuestion' identifier
	 * @return Value of property 'redoQuestion' from DB
	 * @throws DaoException
	 */
	public RedoQuestionValue getRedoQuestion(long id) throws DaoException
	{
		RedoQuestionValue redoQuestion=null;
		try
		{
			startOperation();
			redoQuestion=(RedoQuestionValue)operation.session.get(RedoQuestionValue.class,id);
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
		return redoQuestion;
	}
	
	/**
	 * @param value Value of property 'redoQuestion' string
	 * @return Value of property 'redoQuestion' from DB
	 * @throws DaoException
	 */
	public RedoQuestionValue getRedoQuestion(String value) throws DaoException
	{
		RedoQuestionValue redoQuestion=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from RedoQuestionValue r where r.value = :value");
			query.setParameter("value",value,StandardBasicTypes.STRING);
			redoQuestion=(RedoQuestionValue)query.uniqueResult();
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
		return redoQuestion;
	}
	
	/**
	 * @return List of all available values of property 'redoQuestion'
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<RedoQuestionValue> getRedoQuestions() throws DaoException
	{
		List<RedoQuestionValue> redoQuestions=null;
		try
		{
			startOperation();
			redoQuestions=operation.session.createQuery("from RedoQuestionValue r order by r.id").list();
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
		return redoQuestions;
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
