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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.hibernate.HibernateException;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.UsersDao;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages user session. 
 */
@SuppressWarnings("serial")
@ManagedBean(name="userSessionService")
@SessionScoped
public class UserSessionService implements Serializable
{
	private static UsersDao USERS_DAO=new UsersDao();
	
	private long currentUserId;									// Current logged user identifier
	private Operation currentSessionOperation;					// Current Hibernate operation for this session 
	
	private String ajaxErrorCode;								// Ajax error code
	private String ajaxPlainError;								// Ajax plain error message
	
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	public UserSessionService()
	{
		currentUserId=0L;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/**
	 * @return Current user identifier
	 */
	public long getCurrentUserId()
	{
		return currentUserId;
	}
	
	/**
	 * @return Current user or null if user has not been logged
	 */
	public User getCurrentUser()
	{
		return getCurrentUser(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Current user or null if user has not been logged
	 */
	public User getCurrentUser(Operation operation)
	{
		User currentUser=null;
		if (currentUserId>0L)
		{
			currentUser=usersService.getUser(operation,currentUserId);
		}
		return currentUser;
	}
	
	/**
	 * @param operation
	 * @return Current user
	 * @throws ServiceException if user has not been logged
	 */
	private User checkCurrentUser(Operation operation) throws ServiceException
	{
		// Get current user
		User currentUser=getCurrentUser(operation);
		if (currentUser==null)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("NOT_LOGGED_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="You must login into the system to access that page.";
			}
			throw new ServiceException(errorMessage);
		}
		return currentUser;
	}
	
	/**
	 * Get current user session Hibernate operation, instantianting a new one if needed.
	 * @return Current user session Hibernate operation
	 * @throws ServiceException
	 */
	public Operation getCurrentUserOperation() throws ServiceException
	{
		boolean logged=isLogged();
		if (currentSessionOperation==null && logged)
		{
			// Start a new user session Hibernate operation
			startCurrentUserOperation();
		}
		if (currentSessionOperation==null || !logged)
		{
			// End current user session Hibernate operation
			endCurrentUserOperation();
			
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("NON_AUTHORIZED_ACTION_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="You are not authorized to execute that operation.";
			}
			throw new ServiceException(errorMessage);
		}
		return currentSessionOperation;
	}
	
	/**
	 * Start a new user session Hibernate operation.
	 * @throws ServiceException
	 */
	private void startCurrentUserOperation() throws ServiceException
	{
		try
		{
			// Start a new Hibernate operation
			currentSessionOperation=HibernateUtil.startOperation();
		}
		catch (HibernateException he)
		{
			currentSessionOperation=null;
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("ERROR_ACCESS_DATA_LAYER");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Access error to the data layer";
			}
			throw new ServiceException(errorMessage,he);
		}
	}
	
	/**
	 * End current user session Hibernate operation.
	 * @throws ServiceException
	 */
	public void endCurrentUserOperation() throws ServiceException
	{
		if (currentSessionOperation!=null)
		{
			try
			{
				// End current session Hibernate operation
				HibernateUtil.endOperation(currentSessionOperation);
			}
			catch (HibernateException he)
			{
				String errorMessage=null;
				try
				{
					errorMessage=localizationService.getLocalizedMessage("ERROR_ACCESS_DATA_LAYER");
				}
				catch (ServiceException se)
				{
					errorMessage=null;
				}
				if (errorMessage==null)
				{
					errorMessage="Access error to the data layer";
				}
				throw new ServiceException(errorMessage,he);
			}
			finally
			{
				currentSessionOperation=null;
			}
		}
	}
	
	public String getAjaxErrorCode()
	{
		return ajaxErrorCode;
	}
	
	public void setAjaxErrorCode(String ajaxErrorCode)
	{
		this.ajaxErrorCode=ajaxErrorCode;
	}
	
	public String getAjaxPlainError()
	{
		return ajaxPlainError;
	}
	
	public void setAjaxPlainError(String ajaxPlainError)
	{
		this.ajaxPlainError=ajaxPlainError;
	}
	
	/**
	 * Login an user into application.
	 * @param userLogin User's login
	 * @param userPassword User's password
	 * @throws ServiceException
	 */
	public void login(String userLogin,String userPassword) throws ServiceException
	{
		User authenticatedUser=null;
		try
		{
			// Start a new user session Hibernate operation
			startCurrentUserOperation();
			
			// Get user from DB
			USERS_DAO.setOperation(currentSessionOperation);
			User authenticatedUserFromDB=USERS_DAO.getAuthenticatedUser(userLogin,userPassword,true);
			if (authenticatedUserFromDB!=null)
			{
				authenticatedUser=authenticatedUserFromDB.getUserCopy();
				if (authenticatedUserFromDB.getUserType()!=null)
				{
					authenticatedUser.setUserType(authenticatedUserFromDB.getUserType().getUserTypeCopy());
				}
				
				// Password is set to empty string before returning instance for security reasons
				authenticatedUser.setPassword("");
			}
		}
		catch (DaoException de)
		{
			currentUserId=0L;
			throw new ServiceException(de.getMessage(),de);
		}
		finally
		{
			// Check if user has been logged successfully
			if (authenticatedUser==null)
			{
				currentUserId=0L;
				
				endCurrentUserOperation();
				
				currentSessionOperation=null;
				
				String loginError=null;
				try
				{
					loginError=localizationService.getLocalizedMessage("LOGIN_ERROR");
				}
				catch (ServiceException se)
				{
					loginError=null;
				}
				if (loginError==null)
				{
					loginError="The username and/or password is incorrect";
				}
				throw new ServiceException(loginError);
			}
			else
			{
				currentUserId=authenticatedUser.getId();
			}
		}
	}
	
	/**
	 * Logout current user from application.
	 * @throws ServiceException
	 */
	public void logout() throws ServiceException
	{
		currentUserId=0L;
		
		// End current user session Hibernate operation
		endCurrentUserOperation();
	}
	
	/**
	 * Check if user has been logged (true) or not (false).
	 * @return true if user has been logged, false otherwise
	 */
	public boolean isLogged()
	{
		return currentUserId>0L;
	}
	
	/**
	 * @param permissionName Permission name
	 * @return true if user is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(String permissionName) throws ServiceException
	{
		return isGranted(null,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @return true if user is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(Operation operation,String permissionName) throws ServiceException
	{
		boolean granted=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check if current user is granted that permission
			granted=permissionsService.isGranted(operation,checkCurrentUser(operation),permissionName);
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return granted; 
	}
	
	/**
	 * @param permissionName Permission name
	 * @return true if user is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(String permissionName) throws ServiceException
	{
		return !isGranted(null,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @return true if user is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(Operation operation,String permissionName) throws ServiceException
	{
		return !isGranted(operation,permissionName);
	}
	
	/**
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(String permissionName) throws ServiceException
	{
		return getIntegerPermission(null,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(Operation operation,String permissionName) throws ServiceException
	{
		int intValue=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get integer value of the permission
			intValue=permissionsService.getIntegerPermission(operation,checkCurrentUser(operation),permissionName);
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return intValue;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerEqual(null,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isEqual=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is equal to the value indicated
			isEqual=permissionsService.isIntegerEqual(operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isEqual;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerDistinct(null,permissionName,cmpValue); 
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isDistinct=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is distinct to the value indicated
			isDistinct=
				permissionsService.isIntegerDistinct(operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isDistinct;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreater(null,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isGreater=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is greater than the value indicated
			isGreater=
				permissionsService.isIntegerGreater(operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isGreater;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreaterEqual(null,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isGreaterEqual=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is greater or equal than the value indicated
			isGreaterEqual=permissionsService.isIntegerGreaterEqual(
				operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isGreaterEqual;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLess(null,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isLess=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is less than the value indicated
			isLess=permissionsService.isIntegerLess(operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isLess;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLessEqual(null,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(Operation operation,String permissionName,int cmpValue) throws ServiceException
	{
		boolean isLessEqual=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is less or equal than the value indicated
			isLessEqual=
				permissionsService.isIntegerLessEqual(operation,checkCurrentUser(operation),permissionName,cmpValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isLessEqual;
	}
	
	/**
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(String permissionName,int minValue,int maxValue) throws ServiceException
	{
		return isIntegerBetween(null,permissionName,minValue,maxValue);
	}
	
	/**
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(Operation operation,String permissionName,int minValue,int maxValue)
		throws ServiceException
	{
		boolean isBetween=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check that integer value of the permission is between the minimum and maximum values indicated
			isBetween=permissionsService.isIntegerBetween(
				operation,checkCurrentUser(operation),permissionName,minValue,maxValue);
		}
		finally
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		return isBetween;
	}
}
