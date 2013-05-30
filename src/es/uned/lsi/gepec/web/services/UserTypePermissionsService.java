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
import es.uned.lsi.gepec.model.dao.UserTypePermissionsDao;
import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.UserTypePermission;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages permissions of user types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class UserTypePermissionsService implements Serializable
{
	private final static UserTypePermissionsDao USER_TYPE_PERMISSIONS_DAO=new UserTypePermissionsDao();
	
	public UserTypePermissionsService()
	{
	}
	
	/**
	 * @param id Permission of user type identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(long id) throws ServiceException
	{
		return getUserTypePermission((Operation)null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Permission of user type identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(Operation operation,long id) throws ServiceException
	{
		UserTypePermission userTypePermission=null;
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			userTypePermission=USER_TYPE_PERMISSIONS_DAO.getUserTypePermission(id,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userTypePermission;
	}
	
	/**
	 * @param userType User type
	 * @param permission Permission
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(UserType userType,Permission permission) throws ServiceException
	{
		return getUserTypePermission(null,userType==null?0L:userType.getId(),permission==null?0L:permission.getId());
	}
	
	/**
	 * @param userType User type
	 * @param permissionId Permission identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(UserType userType,long permissionId) throws ServiceException
	{
		return getUserTypePermission(null,userType==null?0L:userType.getId(),permissionId);
	}
	
	/**
	 * @param userTypeId User type identifier
	 * @param permission Permission
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(long userTypeId,Permission permission) throws ServiceException
	{
		return getUserTypePermission(null,userTypeId,permission==null?0L:permission.getId());
	}
	
	/**
	 * @param userTypeId User type identifier
	 * @param permissionId Permission identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(long userTypeId,long permissionId) throws ServiceException
	{
		return getUserTypePermission(null,userTypeId,permissionId);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permission Permission
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(Operation operation,UserType userType,Permission permission) 
		throws ServiceException
	{
		return getUserTypePermission(operation,userType==null?0L:userType.getId(),permission==null?0L:permission.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionId Permission identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(Operation operation,UserType userType,long permissionId) 
		throws ServiceException
	{
		return getUserTypePermission(operation,userType==null?0L:userType.getId(),permissionId);
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId User type identifier
	 * @param permission Permission
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(Operation operation,long userTypeId,Permission permission) 
		throws ServiceException
	{
		return getUserTypePermission(operation,userTypeId,permission==null?0L:permission.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId User type identifier
	 * @param permissionId Permission identifier
	 * @return Permission of user type
	 * @throws ServiceException
	 */
	public UserTypePermission getUserTypePermission(Operation operation,long userTypeId,long permissionId)
		throws ServiceException
	{
		UserTypePermission userTypePermission=null;
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			userTypePermission=USER_TYPE_PERMISSIONS_DAO.getUserTypePermission(userTypeId,permissionId,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userTypePermission;
	}
	
	/**
	 * Adds a new permission of user type.
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void addUserTypePermission(UserTypePermission userTypePermission) throws ServiceException
	{
		addUserTypePermission(null,userTypePermission);
	}
	
	/**
	 * Adds a new permission of user type.
	 * @param operation Operation
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void addUserTypePermission(Operation operation,UserTypePermission userTypePermission) throws ServiceException
	{
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			USER_TYPE_PERMISSIONS_DAO.saveUserTypePermission(userTypePermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a permission of user type.
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void updateUserTypePermission(UserTypePermission userTypePermission) throws ServiceException
	{
		updateUserTypePermission(null,userTypePermission);
	}
	
	/**
	 * Updates a permission of user type.
	 * @param operation Operation
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void updateUserTypePermission(Operation operation,UserTypePermission userTypePermission) 
		throws ServiceException
	{
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			USER_TYPE_PERMISSIONS_DAO.updateUserTypePermission(userTypePermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a permission of user type.
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void deleteUserTypePermission(UserTypePermission userTypePermission) throws ServiceException
	{
		deleteUserTypePermission(null,userTypePermission);
	}
	
	/**
	 * Deletes a permission of user type.
	 * @param operation Operation
	 * @param userTypePermission Permission of user type
	 * @throws ServiceException
	 */
	public void deleteUserTypePermission(Operation operation,UserTypePermission userTypePermission) 
		throws ServiceException
	{
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			USER_TYPE_PERMISSIONS_DAO.deleteUserTypePermission(userTypePermission);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId User type identifier
	 * @param sortedByUserType Flag to indicate if we want the results sorted by user type
	 * @return List of permissions of an user type (or all permissions of any user type if userType==0)
	 * optionally sorted by user type's type and permission's name
	 * @throws ServiceException
	 */
	private List<UserTypePermission> getUserTypePermissions(Operation operation,long userTypeId,boolean sortedByUserType) 
		throws ServiceException
	{
		List<UserTypePermission> userTypePermissions=null;
		try
		{
			USER_TYPE_PERMISSIONS_DAO.setOperation(operation);
			userTypePermissions=USER_TYPE_PERMISSIONS_DAO.getUserTypePermissions(userTypeId,sortedByUserType,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userTypePermissions;
	}
	
	/**
	 * @param userType User type
	 * @return List of permissions of an user type (or all permissions of any user type if userType==null)
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getUserTypePermissions(UserType userType) throws ServiceException
	{
		return getUserTypePermissions(null,userType==null?0L:userType.getId(),false);
	}
	
	/**
	 * @param userTypeId User type identifier
	 * @return List of permissions of an user type (or all permissions of any user type if userTypeId==0)
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getUserTypePermissions(long userTypeId) throws ServiceException
	{
		return getUserTypePermissions(null,userTypeId,false);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @return List of permissions of an user type (or all permissions of any user type if userType==null)
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getUserTypePermissions(Operation operation,UserType userType) throws ServiceException
	{
		return getUserTypePermissions(operation,userType==null?0L:userType.getId(),false);
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId User type identifier
	 * @return List of permissions of an user type (or all permissions of any user type if userTypeId==0)
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getUserTypePermissions(Operation operation,long userTypeId) throws ServiceException
	{
		return getUserTypePermissions(operation,userTypeId,false);
	}
	
	/**
	 * @param userType User type
	 * @return List of permissions of an user type (or all permissions of any user type if userType==null)
	 * sorted by user type
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getSortedUserTypePermissions(UserType userType) throws ServiceException
	{
		return getUserTypePermissions(null,userType==null?0L:userType.getId(),true);
	}
	
	/**
	 * @param userTypeId User type identifier
	 * @return List of permissions of an user type (or all permissions of any user type if userTypeId==0)
	 * sorted by user type
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getSortedUserTypePermissions(long userTypeId) throws ServiceException
	{
		return getUserTypePermissions(null,userTypeId,true);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @return List of permissions of an user type (or all permissions of any user type if userType==null)
	 * sorted by user type
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getSortedUserTypePermissions(Operation operation,UserType userType) 
		throws ServiceException
	{
		return getUserTypePermissions(operation,userType==null?0L:userType.getId(),true);
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId User type identifier
	 * @return List of permissions of an user type (or all permissions of any user type if userTypeId==0)
	 * sorted by user type
	 * @throws ServiceException
	 */
	public List<UserTypePermission> getSortedUserTypePermissions(Operation operation,long userTypeId) 
		throws ServiceException
	{
		return getUserTypePermissions(operation,userTypeId,true);
	}
}
