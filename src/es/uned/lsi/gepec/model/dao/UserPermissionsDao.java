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
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserPermission;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to permissions of users data.
 */
public class UserPermissionsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new permission of an user to DB.
	 * @param userPermission Permission of an user to add
	 * @return Permission of an user identifier
	 * @throws DaoException
	 */
	public long saveUserPermission(UserPermission userPermission) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(userPermission)).longValue();
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
	 * Updates a permission of an user on DB.
	 * @param userPermission Permission of an user to update
	 * @throws DaoException
	 */
	public void updateUserPermission(UserPermission userPermission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(userPermission);
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
	 * Deletes a permission of an user from DB.
	 * @param userPermission Permission of an user to delete
	 * @throws DaoException
	 */
	public void deleteUserPermission(UserPermission userPermission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(userPermission);
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
	 * @param id Permission of an user identifier
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission of an user type from DB
	 * @throws DaoException
	 */
	public UserPermission getUserPermission(long id,boolean includeUserType,boolean includePermissionType)
		throws DaoException
	{
		UserPermission userPermission=null;
		try
		{
			startOperation();
			userPermission=(UserPermission)operation.session.get(UserPermission.class,id);
			if (userPermission!=null)
			{
				User user=userPermission.getUser();
				Hibernate.initialize(user);
				if (includeUserType)
				{
					Hibernate.initialize(user.getUserType());
				}
				Permission permission=userPermission.getPermission();
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
		return userPermission;
	}
	
	/**
	 * @param userId User identifier
	 * @param permissionId Permission identifier
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission of an user type from DB
	 * @throws DaoException
	 */
	public UserPermission getUserPermission(long userId,long permissionId,boolean includeUserType,
		boolean includePermissionType) throws DaoException
	{
		UserPermission userPermission=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from UserPermission u where u.user = :userId and u.permission = :permissionId");
			query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			query.setParameter("permissionId",Long.valueOf(permissionId),StandardBasicTypes.LONG);
			userPermission=(UserPermission)query.uniqueResult();
			if (userPermission!=null)
			{
				User user=userPermission.getUser();
				Hibernate.initialize(user);
				if (includeUserType)
				{
					Hibernate.initialize(user.getUserType());
				}
				Permission permission=userPermission.getPermission();
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
		return userPermission;
	}
	
	/**
	 * @param userId Filtering user identifier or 0 to get permissions of all users
	 * @param sortedByUser Flag to indicate if we want the results sorted by user
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return List of permissions of an user (or all permissions of any user if userId==0) optionally sorted
	 * by user
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<UserPermission> getUserPermissions(long userId,boolean sortedByUser,boolean includeUserType,
		boolean includePermissionType) throws DaoException
	{
		List<UserPermission> userPermissions=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from UserPermission u");
			if (userId>0L)
			{
				queryString.append(" where u.user = :userId");
			}
			else if (sortedByUser)
			{
				queryString.append(" order by u.user.login");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			userPermissions=query.list();
			if (includeUserType)
			{
				if (includePermissionType)
				{
					for (UserPermission userPermission:userPermissions)
					{
						User user=userPermission.getUser();
						Hibernate.initialize(user);
						Hibernate.initialize(user.getUserType());
						Permission permission=userPermission.getPermission();
						Hibernate.initialize(permission);
						Hibernate.initialize(permission.getPermissionType());
					}
				}
				else
				{
					for (UserPermission userPermission:userPermissions)
					{
						User user=userPermission.getUser();
						Hibernate.initialize(user);
						Hibernate.initialize(user.getUserType());
						Hibernate.initialize(userPermission.getPermission());
					}
				}
			}
			else if (includePermissionType)
			{
				for (UserPermission userPermission:userPermissions)
				{
					Hibernate.initialize(userPermission.getUser());
					Permission permission=userPermission.getPermission();
					Hibernate.initialize(permission);
					Hibernate.initialize(permission.getPermissionType());
				}
			}
			else
			{
				for (UserPermission userPermission:userPermissions)
				{
					Hibernate.initialize(userPermission.getUser());
					Hibernate.initialize(userPermission.getPermission());
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
		return userPermissions;
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
