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
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.UserTypesDao;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.UserTypePermission;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages user types (a.k.a. roles).
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class UserTypesService implements Serializable
{
	private static UserTypesDao USER_TYPES_DAO=new UserTypesDao();
	
	@ManagedProperty(value="#{userTypePermissionsService}")
	private UserTypePermissionsService userTypePermissionsService;
	
	public UserTypesService()
	{
	}
	
	public void setUserTypePermissionsService(UserTypePermissionsService userTypePermissionsService)
	{
		this.userTypePermissionsService=userTypePermissionsService;
	}
	
	/**
	 * @param id User type's identifier
	 * @return User type
	 * @throws ServiceException
	 */
	public UserType getUserType(long id) throws ServiceException
	{
		return getUserType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id User type's identifier
	 * @return User type
	 * @throws ServiceException
	 */
	public UserType getUserType(Operation operation,long id) throws ServiceException
	{
		UserType userType=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USER_TYPES_DAO.setOperation(operation);
			UserType userTypeFromDB=USER_TYPES_DAO.getUserType(id);
			
			if (userTypeFromDB!=null)
			{
				// As we need to change some fields it is better to use a copy
				userType=new UserType();
				userType.setFromOtherUserType(userTypeFromDB);
				
				// We need to get permissions of this user type
				userType.setUserTypePermissions(userTypePermissionsService.getUserTypePermissions(operation,id));
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return userType;
	}
	
	/**
	 * @param id User type's identifier
	 * @return User type as a string
	 * @throws ServiceException
	 */
	public String getUserTypeString(long id) throws ServiceException
	{
		return getUserTypeString(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id User type's identifier
	 * @return User type as a string
	 * @throws ServiceException
	 */
	public String getUserTypeString(Operation operation,long id) throws ServiceException
	{
		UserType userType=getUserType(operation,id);
		return userType==null?null:userType.getType();
	}
	
	/**
	 * Updates an user type.
	 * @param userType User type to update
	 * @throws ServiceException
	 */
	public void updateUserType(UserType userType) throws ServiceException
	{
		updateUserType(null,userType);
	}
	
	/**
	 * Updates an user type.
	 * @param operation Operation
	 * @param userType User type to update
	 * @throws ServiceException
	 */
	public void updateUserType(Operation operation,UserType userType) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<UserTypePermission> userTypePermissionsFromDB=
				userTypePermissionsService.getUserTypePermissions(operation,userType);
			
			// We delete removed permissions of the user type from DB 
			for (UserTypePermission userTypePermission:userTypePermissionsFromDB)
			{
				if (!userType.getUserTypePermissions().contains(userTypePermission))
				{
					userTypePermissionsService.deleteUserTypePermission(operation,userTypePermission);
				}
			}
			
			// We add/update permissions of the user type on DB
			for (UserTypePermission userTypePermission:userType.getUserTypePermissions())
			{
				if (userTypePermissionsFromDB.contains(userTypePermission))
				{
					UserTypePermission userTypePermissionFromDB=
						userTypePermissionsFromDB.get(userTypePermissionsFromDB.indexOf(userTypePermission));
					userTypePermissionFromDB.setFromOtherUserTypePermission(userTypePermission);
					userTypePermissionsService.updateUserTypePermission(operation,userTypePermissionFromDB);
				}
				else
				{
					userTypePermissionsService.addUserTypePermission(operation,userTypePermission);
				}
			}
			
			// We get user type from DB
			USER_TYPES_DAO.setOperation(operation);
			UserType userTypeFromDB=USER_TYPES_DAO.getUserType(userType.getId());
			
			// We update user type on DB
			userTypeFromDB.setFromOtherUserType(userType);
			USER_TYPES_DAO.setOperation(operation);
			USER_TYPES_DAO.updateUserType(userTypeFromDB);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{	
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	/**
	 * Adds a new user type.
	 * @param userType User type to add
	 * @throws ServiceException
	 */
	public void addUserType(UserType userType) throws ServiceException
	{
		addUserType(null,userType);
	}
	
	/**
	 * Adds a new user type.
	 * @param operation Operation
	 * @param userType User type to add
	 * @throws ServiceException
	 */
	public void addUserType(Operation operation,UserType userType) throws ServiceException
	{
		try
		{
			USER_TYPES_DAO.setOperation(operation);
			USER_TYPES_DAO.saveUserType(userType);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes an user type.
	 * @param userType User type to delete
	 * @throws ServiceException
	 */
	public void deleteUserType(UserType userType) throws ServiceException
	{
		deleteUserType(null,userType);
	}
	
	/**
	 * Deletes an user type.
	 * @param operation Operation
	 * @param userType User type to delete
	 * @throws ServiceException
	 */
	public void deleteUserType(Operation operation,UserType userType) throws ServiceException
	{
		try
		{
			USER_TYPES_DAO.setOperation(operation);
			USER_TYPES_DAO.deleteUserType(userType);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @return List of all user types
	 * @throws ServiceException
	 */
	public List<UserType> getUserTypes() throws ServiceException
	{
		return getUserTypes(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all user types
	 * @throws ServiceException
	 */
	public List<UserType> getUserTypes(Operation operation) throws ServiceException
	{
		List<UserType> userTypes=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USER_TYPES_DAO.setOperation(operation);
			userTypes=USER_TYPES_DAO.getUserTypes();
			
			// We need to get permissions of user types
			for (UserType userType:userTypes)
			{
				userType.setUserTypePermissions(userTypePermissionsService.getUserTypePermissions(operation,userType));
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			if (singleOp)
			{	
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return userTypes; 
	}
	
	/**
	 * Checks if exists an user type with the indicated type
	 * @param type User type's type
	 * @return true if exists an user type with the indicated type, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkUserType(String type) throws ServiceException
	{
		return checkUserType(null,type);
	}
	
	/**
	 * Checks if exists an user type with the indicated type
	 * @param operation Operation
	 * @param type User type's type
	 * @return true if exists an user type with the indicated type, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkUserType(Operation operation,String type) throws ServiceException
	{
		boolean userTypeFound=false;
		try
		{
			USER_TYPES_DAO.setOperation(operation);
			userTypeFound=USER_TYPES_DAO.checkUserType(type);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userTypeFound;
	}
}
