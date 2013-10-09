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

import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.model.entities.UserType;

/**
 * Manages access to user types data.
 */
public class UserTypesDao
{
	private Operation operation=null;
	private boolean singleOp;

	//Guarda un tipo de usuario en la bd
	/**
	 * Adds a new user type to DB.
	 * @param userType User type to add
	 * @return User type identifier
	 * @throws DaoException
	 */
	public long saveUserType(UserType userType) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(userType)).longValue();
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
	
	//Actualiza un tipo de usuario en la bd
	/**
	 * Updates an user type on DB.
	 * @param userType User type to update
	 * @throws DaoException
	 */
	public void updateUserType(UserType userType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(userType);
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
	
	//Elimina un tipo de usuario de la bd
	/**
	 * Deletes an user type from DB.
	 * @param userType User type to delete
	 * @throws DaoException
	 */
	public void deleteUserType(UserType userType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(userType);
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
	
	//Obtiene un tipo de usuario a partir de su id
	/**
	 * @param id User type identifier
	 * @return User type from DB
	 * @throws DaoException
	 */
	public UserType getUserType(long id) throws DaoException
	{
		UserType userType=null;
		try
		{
			startOperation();
			userType=(UserType)operation.session.get(UserType.class,id);
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
		return userType;
	}
	
	/**
	 * @param userId User identifier
	 * @return User type from an user
	 * @throws DaoException
	 */
	public UserType getUserTypeFromUserId(long userId) throws DaoException
	{
		UserType userType=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from UserType ut where ut.id = (select u.userType.id from User u where u.id = :userId)");
			query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			userType=(UserType)query.uniqueResult();
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
		return userType;
	}
	
	//Obtiene la lista de tipos de usuario
	/**
	 * @return List of all user types
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<UserType> getUserTypes() throws DaoException
	{
		List<UserType> userTypes=null;
		try
		{
			startOperation();
			userTypes=operation.session.createQuery("from UserType").list();
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
		return userTypes;
	}
    
	/**
	 * Checks if exists an user type with the indicated identifier
	 * @param id Identifier
	 * @return true if exists an user type with the indicated identifier, false otherwise
	 * @throws DaoException
	 */
	public boolean checkUserTypeId(long id) throws DaoException
	{
		boolean userTypeFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(ut) from UserType ut Where ut.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			userTypeFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return userTypeFound;
	}
	
	/**
	 * Checks if exists an user type with the indicated type
	 * @param type User type's type
	 * @return true if exists an user type with the indicated type, false otherwise
	 * @throws DaoException
	 */
	public boolean checkUserTypeType(String type) throws DaoException
	{
		boolean userTypeFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(u) from UserType u where u.type = :type");
			query.setParameter("type",type,StandardBasicTypes.STRING);
			userTypeFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return userTypeFound;
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
