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

import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.model.entities.UserTypePermission;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to permissions of user types data.
 */
public class UserTypePermissionsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new permission of an user type to DB.
	 * @param userTypePermission Permission of an user type to add
	 * @return Permission of an user type identifier
	 * @throws DaoException
	 */
	public long saveUserTypePermission(UserTypePermission userTypePermission) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(userTypePermission)).longValue();
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
	 * Updates a permission of an user type on DB.
	 * @param userTypePermission Permission of an user type to update
	 * @throws DaoException
	 */
	public void updateUserTypePermission(UserTypePermission userTypePermission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(userTypePermission);
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
	 * Deletes a permission of an user type from DB.
	 * @param userTypePermission Permission of an user type to delete
	 * @throws DaoException
	 */
	public void deleteUserTypePermission(UserTypePermission userTypePermission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(userTypePermission);
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
	 * @param id Permission of an user type identifier
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission of an user type from DB
	 * @throws DaoException
	 */
	public UserTypePermission getUserTypePermission(long id,boolean includePermissionType) 
		throws DaoException
	{
		UserTypePermission userTypePermission=null;
		try
		{
			startOperation();
			userTypePermission=(UserTypePermission)operation.session.get(UserTypePermission.class,id);
			if (userTypePermission!=null)
			{
				Hibernate.initialize(userTypePermission.getUserType());
				Permission permission=userTypePermission.getPermission();
				Hibernate.initialize(permission);
				if (includePermissionType)
				{
					Hibernate.initialize(permission.getPermissionType());
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
		return userTypePermission;
	}
	
	/**
	 * @param userTypeId User type identifier
	 * @param permissionId Permission identifier
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission of an user type from DB
	 * @throws DaoException
	 */
	public UserTypePermission getUserTypePermission(long userTypeId,long permissionId,
		boolean includePermissionType) throws DaoException
	{
		UserTypePermission userTypePermission=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from UserTypePermission u where u.userType = :userTypeId and u.permission = :permissionId");
			query.setParameter("userTypeId",Long.valueOf(userTypeId),StandardBasicTypes.LONG);
			query.setParameter("permissionId",Long.valueOf(permissionId),StandardBasicTypes.LONG);
			userTypePermission=(UserTypePermission)query.uniqueResult();
			if (userTypePermission!=null)
			{
				Hibernate.initialize(userTypePermission.getUserType());
				Permission permission=userTypePermission.getPermission();
				Hibernate.initialize(permission);
				if (includePermissionType)
				{
					Hibernate.initialize(permission.getPermissionType());
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
		return userTypePermission;
	}
	
	/**
	 * @param userTypeId Filtering user type identifier or 0 to get permissions of all user types
	 * @param sortedByUserType Flag to indicate if we want the results sorted by user type
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return List of permissions of an user type (or all permissions of any user type if userTypeId==0) optionally
	 * sorted by user type
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<UserTypePermission> getUserTypePermissions(long userTypeId,boolean sortedByUserType,
		boolean includePermissionType) throws DaoException
	{
		List<UserTypePermission> userTypePermissions=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from UserTypePermission u");
			if (userTypeId>0L)
			{
				queryString.append(" where u.userType = :userTypeId");
			}
			else if (sortedByUserType)
			{
				queryString.append(" order by u.userType.type");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userTypeId>0L)
			{
				query.setParameter("userTypeId",Long.valueOf(userTypeId),StandardBasicTypes.LONG);
			}
			userTypePermissions=query.list();
			if (includePermissionType)
			{
				for (UserTypePermission userTypePermission:userTypePermissions)
				{
					Hibernate.initialize(userTypePermission.getUserType());
					Permission permission=userTypePermission.getPermission();
					Hibernate.initialize(permission);
					Hibernate.initialize(permission.getPermissionType());
				}
			}
			else
			{
				for (UserTypePermission userTypePermission:userTypePermissions)
				{
					Hibernate.initialize(userTypePermission.getUserType());
					Hibernate.initialize(userTypePermission.getPermission());
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
		return userTypePermissions;
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
