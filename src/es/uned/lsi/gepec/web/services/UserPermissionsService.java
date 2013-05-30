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
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.UserPermissionsDao;
import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserPermission;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages permissions of users.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class UserPermissionsService implements Serializable
{
	private final static UserPermissionsDao USER_PERMISSIONS_DAO=new UserPermissionsDao();
	
	public UserPermissionsService()
	{
	}
	
	/**
	 * @param id Permission of user identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(long id) throws ServiceException
	{
		return getUserPermission((Operation)null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Permission of user identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(Operation operation,long id) throws ServiceException
	{
		UserPermission userPermission=null;
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			userPermission=USER_PERMISSIONS_DAO.getUserPermission(id,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userPermission;
	}
	
	/**
	 * @param user User
	 * @param permission Permission
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(User user,Permission permission) throws ServiceException
	{
		return getUserPermission(null,user==null?0L:user.getId(),permission==null?0L:permission.getId());
	}
	
	/**
	 * @param user User
	 * @param permissionId Permission identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(User user,long permissionId) throws ServiceException
	{
		return getUserPermission(null,user==null?0L:user.getId(),permissionId);
	}
	
	/**
	 * @param userId User identifier
	 * @param permission Permission
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(long userId,Permission permission) throws ServiceException
	{
		return getUserPermission(null,userId,permission==null?0L:permission.getId());
	}
	
	/**
	 * @param userId User identifier
	 * @param permissionId Permission identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(long userId,long permissionId) throws ServiceException
	{
		return getUserPermission(null,userId,permissionId);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permission Permission
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(Operation operation,User user,Permission permission) 
		throws ServiceException
	{
		return getUserPermission(operation,user==null?0L:user.getId(),permission==null?0L:permission.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionId Permission identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(Operation operation,User user,long permissionId) throws ServiceException
	{
		return getUserPermission(operation,user==null?0L:user.getId(),permissionId);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param permission Permission
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(Operation operation,long userId,Permission permission) 
		throws ServiceException
	{
		return getUserPermission(operation, userId,permission==null?0L:permission.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param permissionId Permission identifier
	 * @return Permission of user
	 * @throws ServiceException
	 */
	public UserPermission getUserPermission(Operation operation,long userId,long permissionId) throws ServiceException
	{
		UserPermission userPermission=null;
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			userPermission=USER_PERMISSIONS_DAO.getUserPermission(userId,permissionId,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userPermission;
	}
	
	/**
	 * Adds a new permission of user.
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void addUserPermission(UserPermission userPermission) throws ServiceException
	{
		addUserPermission(null,userPermission);
	}
	
	/**
	 * Adds a new permission of user.
	 * @param operation Operation
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void addUserPermission(Operation operation,UserPermission userPermission) throws ServiceException
	{
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			USER_PERMISSIONS_DAO.saveUserPermission(userPermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a permission of user.
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void updateUserPermission(UserPermission userPermission) throws ServiceException
	{
		updateUserPermission(null,userPermission);
	}
	
	/**
	 * Updates a permission of user.
	 * @param operation Operation
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void updateUserPermission(Operation operation,UserPermission userPermission) throws ServiceException
	{
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			USER_PERMISSIONS_DAO.updateUserPermission(userPermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a permission of user.
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void deleteUserPermission(UserPermission userPermission) throws ServiceException
	{
		deleteUserPermission(null,userPermission);
	}
	
	/**
	 * Deletes a permission of user.
	 * @param operation Operation
	 * @param userPermission Permission of user
	 * @throws ServiceException
	 */
	public void deleteUserPermission(Operation operation,UserPermission userPermission) throws ServiceException
	{
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			USER_PERMISSIONS_DAO.deleteUserPermission(userPermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param sortedByUser Flag to indicate if we want the results sorted by user
	 * @return List of permissions of an user (or all permissions of any user if user==0) optionally sorted 
	 * by user
	 * @throws ServiceException
	 */
	private List<UserPermission> getUserPermissions(Operation operation,long userId,boolean sortedByUser)
		throws ServiceException
	{
		List<UserPermission> userPermissions=null;
		try
		{
			USER_PERMISSIONS_DAO.setOperation(operation);
			userPermissions=USER_PERMISSIONS_DAO.getUserPermissions(userId,sortedByUser,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userPermissions;
	}
	
	/**
	 * @param user User
	 * @return List of permissions of an user (or all permissions of any user if user==null)
	 * @throws ServiceException
	 */
	public List<UserPermission> getUserPermissions(User user) throws ServiceException
	{
		return getUserPermissions(null,user==null?0L:user.getId(),false);
	}
	
	/**
	 * @param userId User identifier
	 * @return List of permissions of an user (or all permissions of any user if user==null)
	 * @throws ServiceException
	 */
	public List<UserPermission> getUserPermissions(long userId) throws ServiceException
	{
		return getUserPermissions(null,userId,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of permissions of an user (or all permissions of any user if user==null)
	 * @throws ServiceException
	 */
	public List<UserPermission> getUserPermissions(Operation operation,User user) throws ServiceException
	{
		return getUserPermissions(operation,user==null?0L:user.getId(),false);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @return List of permissions of an user (or all permissions of any user if user==null)
	 * @throws ServiceException
	 */
	public List<UserPermission> getUserPermissions(Operation operation,long userId) throws ServiceException
	{
		return getUserPermissions(operation,userId,false);
	}
	
	/**
	 * @param user User
	 * @return List of permissions of an user (or all permissions of any user if user==null) sorted by 
	 * user's login and permission's name
	 * @throws ServiceException
	 */
	public List<UserPermission> getSortedUserPermissions(User user) throws ServiceException
	{
		return getUserPermissions(null,user==null?0L:user.getId(),true);
	}
	
	/**
	 * @param userId User identifier
	 * @return List of permissions of an user (or all permissions of any user if user==null) sorted by user
	 * @throws ServiceException
	 */
	public List<UserPermission> getSortedUserPermissions(long userId) throws ServiceException
	{
		return getUserPermissions(null,userId,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of permissions of an user (or all permissions of any user if user==null) sorted by user
	 * @throws ServiceException
	 */
	public List<UserPermission> getSortedUserPermissions(Operation operation,User user) throws ServiceException
	{
		return getUserPermissions(operation,user==null?0L:user.getId(),true);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @return List of permissions of an user (or all permissions of any user if user==null) sorted by user
	 * @throws ServiceException
	 */
	public List<UserPermission> getSortedUserPermissions(Operation operation,long userId) throws ServiceException
	{
		return getUserPermissions(operation,userId,true);
	}
}
