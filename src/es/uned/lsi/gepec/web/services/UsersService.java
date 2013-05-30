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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.encryption.PasswordDigester;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.UsersDao;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.TestUser;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserPermission;
import es.uned.lsi.gepec.om.axis.OmTnProxy;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

//Ofrece a la vista operaciones con usuarios
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages users.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class UsersService implements Serializable
{
	private static UsersDao USERS_DAO=new UsersDao();
	
	private final static int OUCU_MAX_LENGTH=8;
	
	@ManagedProperty(value="#{userPermissionsService}")
	private UserPermissionsService userPermissionsService;
	@ManagedProperty(value="#{testUsersService}")
	private TestUsersService testUsersService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	
	public UsersService()
	{
	}
	
	public void setUserPermissionsService(UserPermissionsService userPermissionsService)
	{
		this.userPermissionsService=userPermissionsService;
	}
	
	public void setTestUsersService(TestUsersService testUsersService)
	{
		this.testUsersService=testUsersService;
	}
	
	public void setCategoriesService(CategoriesService categoriesService)
	{
		this.categoriesService=categoriesService;
	}
	
	public void setCategoryTypesService(CategoryTypesService categoryTypesService)
	{
		this.categoryTypesService=categoryTypesService;
	}
	
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	//Obtiene un usuario a partir de su id
	/**
	 * @param id User's identifier
	 * @return User
	 * @throws ServiceException
	 */
	public User getUser(long id) throws ServiceException
	{
		return getUser(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id User's identifier
	 * @return User
	 * @throws ServiceException
	 */
	public User getUser(Operation operation,long id) throws ServiceException
	{
		User user=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USERS_DAO.setOperation(operation);
			User userFromDB=USERS_DAO.getUser(id);
			
			if (userFromDB!=null)
			{
				// As we need to change some fields it is better to use a copy
				user=new User();
				user.setFromOtherUser(userFromDB);
				
				// We need to get permissions of this user
				user.setUserPermissions(userPermissionsService.getUserPermissions(operation,id));
				
				// Password is set to empty string before returning instance for security reasons
				user.setPassword("");
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
		return user;
	}
	
	/**
	 * @param oucu User's OUCU
	 * @return User
	 * @throws ServiceException
	 */
	public User getUserFromOucu(String oucu) throws ServiceException
	{
		return getUserFromOucu(null,oucu);
	}
	
	/**
	 * @param operation Operation
	 * @param oucu User's OUCU
	 * @return User
	 * @throws ServiceException
	 */
	public User getUserFromOucu(Operation operation,String oucu) throws ServiceException
	{
		User user=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USERS_DAO.setOperation(operation);
			User userFromDB=USERS_DAO.getUserFromOucu(oucu);
			
			if (userFromDB!=null)
			{
				// As we need to change some fields it is better to use a copy
				user=new User();
				user.setFromOtherUser(userFromDB);
				
				// We need to get permissions of this user
				user.setUserPermissions(userPermissionsService.getUserPermissions(operation,user));
				
				// Password is set to empty string before returning instance for security reasons
				user.setPassword("");
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
		return user;
	}
	
	//Actualiza un usuario
	/**
	 * Updates an user, except its password.
	 * @param user User to update
	 * @throws ServiceException
	 */
	public void updateUser(User user) throws ServiceException
	{
		updateUser(null,user,false);
	}
	
	/**
	 * Updates an user, except its password.
	 * @param operation Operation
	 * @param user User to update
	 * @throws ServiceException
	 */
	public void updateUser(Operation operation,User user) throws ServiceException
	{
		updateUser(operation,user,false);
	}
	
	/**
	 * Updates an user including its password if provided old user password is correct.
	 * @param user User to update
	 * @param oldUserPassword Old user password
	 * @throws ServiceException
	 */
	public void updateUser(User user,String oldUserPassword) throws ServiceException
	{
		updateUser(null,user,oldUserPassword);
	}
	
	/**
	 * Updates an user including its password if provided old user password is correct.
	 * @param operation Operation
	 * @param user User to update
	 * @param oldUserPassword Old user password
	 * @throws ServiceException
	 */
	public void updateUser(Operation operation,User user,String oldUserPassword) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// We get user from DB
			USERS_DAO.setOperation(operation);
			User userFromDB=USERS_DAO.getUser(user.getId(),false);
			
			// We check old password
			if (PasswordDigester.matches(oldUserPassword,userFromDB.getPassword()))
			{
				// Old password is correct so we update user including its password
				updateUser(operation,user,true);
			}
			else
			{
				String updateUserOldPasswordError=null;
				try
				{
					updateUserOldPasswordError=
						localizationService.getLocalizedMessage("UPDATE_USER_OLD_PASSWORD_ERROR");
				}
				catch (ServiceException se)
				{
					updateUserOldPasswordError=null;
				}
				if (updateUserOldPasswordError==null)
				{
					updateUserOldPasswordError=
						"It is not possible to realize operation because old password is incorrect.";
				}
				throw new ServiceException(updateUserOldPasswordError);
			}
			
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
	 * Updated an user and optionally its password.
	 * @param user User to update
	 * @param updatePassword Flag to indicate if we want to update password (true) or not (false)
	 * @throws ServiceException
	 */
	public void updateUser(User user,boolean updatePassword)
	{
		updateUser(null,user,updatePassword);
	}
	
	/**
	 * Updated an user and optionally its password.
	 * @param operation Operation
	 * @param user User to update
	 * @param updatePassword Flag to indicate if we want to update password (true) or not (false)
	 * @throws ServiceException
	 */
	public void updateUser(Operation operation,User user,boolean updatePassword)
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<UserPermission> userPermissionsFromDB=
				userPermissionsService.getUserPermissions(operation,user);
			
			if (user.isGepeqUser())
			{
				// We delete removed permissions of the user from DB 
				for (UserPermission userPermission:userPermissionsFromDB)
				{
					if (!user.getUserPermissions().contains(userPermission))
					{
						userPermissionsService.deleteUserPermission(operation,userPermission);
					}
				}
				
				// We add/update permissions of the user on DB
				for (UserPermission userPermission:user.getUserPermissions())
				{
					if (userPermissionsFromDB.contains(userPermission))
					{
						UserPermission userPermissionFromDB=
							userPermissionsFromDB.get(userPermissionsFromDB.indexOf(userPermission));
						userPermissionFromDB.setFromOtherUserPermission(userPermission);
						userPermissionsService.updateUserPermission(operation,userPermissionFromDB);
					}
					else
					{
						userPermissionsService.addUserPermission(operation,userPermission);
					}
				}
			}
			else
			{
				// No role for non GEPEQ users
				user.setUserType(null);
				
				// We delete all permissions of the user from DB (No permissions for non GEPEQ users) 
				for (UserPermission userPermission:userPermissionsFromDB)
				{
					userPermissionsService.deleteUserPermission(operation,userPermission);
				}
			}
			
			// We get tests of an user
			List<TestUser> userTestsFromDB=testUsersService.getUserTests(operation,user);
			
			// We delete removed users of a test from DB
			for (TestUser userTest:userTestsFromDB)
			{
				if (!user.getUserTests().contains(userTest))
				{
					testUsersService.deleteTestUser(operation,userTest);
				}
			}
			
			// We add/update users of a test on DB
			for (TestUser userTest:user.getUserTests())
			{
				if (userTestsFromDB.contains(userTest))
				{
					TestUser userTestFromDB=userTestsFromDB.get(userTestsFromDB.indexOf(userTest));
					userTestFromDB.setFromOtherTestUser(userTest);
					testUsersService.updateTestUser(operation,userTestFromDB);
				}
				else
				{
					testUsersService.addTestUser(operation,userTest);
				}
			}
			
			// We get user from DB
			USERS_DAO.setOperation(operation);
			User userFromDB=USERS_DAO.getUser(user.getId(),false);
			
			// We never change OUCU
			user.setOucu(userFromDB.getOucu());
			
			// We get digested user password
			String digestedUserPassword=null;
			if (updatePassword)
			{
				digestedUserPassword=PasswordDigester.digest(user.getPassword());
			}
			else
			{
				digestedUserPassword=userFromDB.getPassword();
			}
			
			// We update user on DB
			userFromDB.setFromOtherUser(user);
			userFromDB.setPassword(digestedUserPassword);
			USERS_DAO.setOperation(operation);
			USERS_DAO.updateUser(userFromDB);
			
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
	
	//Añade un nuevo usuario
	/**
	 * Adds a new user.
	 * @param user User to add
	 * @throws ServiceException
	 */
	public void addUser(User user) throws ServiceException
	{
		addUser(null,user);
	}
	
	/**
	 * Adds a new user.
	 * @param operation Operation
	 * @param user User to add
	 * @throws ServiceException
	 */
	public void addUser(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// We generate user's OUCU
			generateUnusedOucuForUser(operation,user);
			
			// We need to digest password before adding user
			User userToAdd=new User();
			userToAdd.setFromOtherUser(user);
			userToAdd.setPassword(PasswordDigester.digest(user.getPassword()));
			
			// We need to include user permissions
			List<UserPermission> userPermissionsToAdd=new ArrayList<UserPermission>();
			if (user.isGepeqUser())
			{
				for (UserPermission userPermission:user.getUserPermissions())
				{
					UserPermission userPermissionToAdd=new UserPermission();
					userPermissionToAdd.setFromOtherUserPermission(userPermission);
					userPermissionToAdd.setUser(userToAdd);
					userPermissionsToAdd.add(userPermissionToAdd);
				}
			}
			userToAdd.setUserPermissions(userPermissionsToAdd);
			
			// We add user with the digested password 
			USERS_DAO.setOperation(operation);
			long userId=USERS_DAO.saveUser(userToAdd);
			
			// If user is a GEPEQ user we also need to create a default category for the user
			if (user.isGepeqUser())
			{
				// We get new created user from DB
				USERS_DAO.setOperation(operation);
				User userFromDB=USERS_DAO.getUser(userId);
				
				// We create a default category for the user
				Category defaultCategory=new Category();
				defaultCategory.setName("DEFAULT_CATEGORY");
				defaultCategory.setDescription("SYSTEM_CATEGORY");
				defaultCategory.setParent(null);
				defaultCategory.setUser(userFromDB);
				defaultCategory.setDefaultCategory(true);
				defaultCategory.setCategoryType(
					categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_GENERAL"));
				defaultCategory.setVisibility(
					visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE"));
				categoriesService.addCategory(operation,defaultCategory);
			}
			
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
	
	//Elimina un usuario
	/**
	 * Deletes an user.
	 * @param user User to delete
	 * @throws ServiceException
	 */
	public void deleteUser(User user) throws ServiceException
	{
		deleteUser(null,user);
	}
	
	/**
	 * Deletes an user.
	 * @param operation Operation
	 * @param user User to delete
	 * @throws ServiceException
	 */
	public void deleteUser(Operation operation,User user) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// We need to delete all categories of the user
			// IMPORTANT: Note that categories need to be empty previously or this operation will fail.
			List<Category> categories=categoriesService.getUserCategories(operation,user);
			
			// First we clear parent category of all user categories to avoid constraints deletion conflicts
			// (I think this method is faster than checking dependencies and deleting categories in appropiate order,
			// moreover this way we don't need to worry about infinite loops issues that can be caused if there
			// are data corrupted on DB)
			for (Category category:categories)
			{
				category.setParent(null);
				categoriesService.updateCategory(operation,category);
			}
			
			// Finally we delete categories
			for (Category category:categories)
			{
				categoriesService.deleteCategory(operation,category);
			}
			
			// We need to get user from DB
			USERS_DAO.setOperation(operation);
			User userFromDB=USERS_DAO.getUser(user.getId());
			
			// We delete user
			USERS_DAO.setOperation(operation);
			USERS_DAO.deleteUser(userFromDB);
			
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
	 * @param operation Operation
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @param sortedByLogin Flag to indicate if we want the results sorted by login
	 * @return List of all users optionally sorted by login
	 * @throws ServiceException
	 */
	private List<User> getUsers(Operation operation,long userTypeId,boolean includeOmUsers,boolean sortedByLogin) 
		throws ServiceException
	{
		List<User> users=new ArrayList<User>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USERS_DAO.setOperation(operation);
			List<User> usersFromDB=USERS_DAO.getUsers(userTypeId,includeOmUsers,true,sortedByLogin);
			
			for (User userFromDB:usersFromDB)
			{
				// As we need to change some fields it is better to use a copy
				User user=new User();
				user.setFromOtherUser(userFromDB);
				
				// We need to get permissions of users
				user.setUserPermissions(userPermissionsService.getUserPermissions(operation,user));
				
				// Users passwords are set to empty string before returning instances for security reasons
				user.setPassword("");
				
				// We add user to users list
				users.add(user);
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
		return users;
	}
	
	/**
	 * @return List of all users
	 * @throws ServiceException
	 */
	public List<User> getUsers() throws ServiceException
	{
		return getUsers(null,0L,true,false);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all users
	 * @throws ServiceException
	 */
	public List<User> getUsers(Operation operation) throws ServiceException
	{
		return getUsers(operation,0L,true,false);
	}
	
	/**
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users optionally filtered by user type and optionally excluded non GEPEQ users
	 * @throws ServiceException
	 */
	public List<User> getUsers(long userTypeId,boolean includeOmUsers)
	{
		return getUsers(null,userTypeId,includeOmUsers,false);
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users optionally filtered by user type and optionally excluded non GEPEQ users
	 * @throws ServiceException
	 */
	public List<User> getUsers(Operation operation,long userTypeId,boolean includeOmUsers)
	{
		return getUsers(operation,userTypeId,includeOmUsers,false);
	}
	
	/**
	 * @return List of all users sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsers() throws ServiceException
	{
		return getUsers(null,0L,true,true);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all users sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsers(Operation operation) throws ServiceException
	{
		return getUsers(operation,0L,true,true);
	}
	
	/**
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users optionally filtered by user type and optionally excluded non GEPEQ users 
	 * and sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsers(long userTypeId,boolean includeOmUsers)
	{
		return getUsers(null,userTypeId,includeOmUsers,true);
	}
	
	/**
	 * @param operation Operation
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users optionally filtered by user type and optionally excluded non GEPEQ users 
	 * and sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsers(Operation operation,long userTypeId,boolean includeOmUsers)
	{
		return getUsers(operation,userTypeId,includeOmUsers,true);
	}
	
	/**
	 * @param operation Operation
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @param sortedByLogin Flag to indicate if we want the results sorted by login
	 * @return List of all users without user type optionally filtered by user type and optionally excluded
	 * non GEPEQ users and optionally sorted by login
	 * @throws ServiceException
	 */
	private List<User> getUsersWithoutUserType(Operation operation,boolean includeOmUsers,boolean sortedByLogin) 
		throws ServiceException
	{
		List<User> users=new ArrayList<User>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			USERS_DAO.setOperation(operation);
			List<User> usersFromDB=USERS_DAO.getUsersWithoutUserType(includeOmUsers,true,sortedByLogin);
			
			for (User userFromDB:usersFromDB)
			{
				// As we need to change some fields it is better to use a copy
				User user=new User();
				user.setFromOtherUser(userFromDB);
				
				// We need to get permissions of users
				user.setUserPermissions(userPermissionsService.getUserPermissions(operation,user));
				
				// Users passwords are set to empty string before returning instances for security reasons
				user.setPassword("");
				
				// We add user to users list
				users.add(user);
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
		return users;
	}
	
	/**
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users without user type optionally excluded non GEPEQ users
	 * @throws ServiceException
	 */
	public List<User> getUsersWithoutUserType(boolean includeOmUsers)
	{
		return getUsersWithoutUserType(null,includeOmUsers,false);
	}
	
	/**
	 * @param operation Operation
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users without user type optionally excluded non GEPEQ users
	 * @throws ServiceException
	 */
	public List<User> getUsersWithoutUserType(Operation operation,boolean includeOmUsers)
	{
		return getUsersWithoutUserType(operation,includeOmUsers,false);
	}
	
	/**
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users without user type optionally excluded non GEPEQ users and sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsersWithoutUserType(boolean includeOmUsers)
	{
		return getUsersWithoutUserType(null,includeOmUsers,true);
	}
	
	/**
	 * @param operation Operation
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @return List of all users without user type optionally excluded non GEPEQ users and sorted by login
	 * @throws ServiceException
	 */
	public List<User> getSortedUsersWithoutUserType(Operation operation,boolean includeOmUsers)
	{
		return getUsersWithoutUserType(operation,includeOmUsers,true);
	}
	
	/**
	 * @return Number of users
	 * @throws ServiceException
	 */
	public long getUsersCount() throws ServiceException
	{
		return getUsersCount(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all users
	 * @throws ServiceException
	 */
	public long getUsersCount(Operation operation) throws ServiceException
	{
		long usersCount=0L;
		try
		{
			USERS_DAO.setOperation(operation);
			usersCount=USERS_DAO.getUsersCount();
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return usersCount;
	}
	
	/**
	 * Checks if exists an user with the indicated login
	 * @param login User's login
	 * @return true if exists an user with the indicated login, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkUser(String login) throws ServiceException
	{
		return checkUser(null,login);
	}
	
	/**
	 * Checks if exists an user with the indicated login
	 * @param operation Operation
	 * @param login User's login
	 * @return true if exists an user with the indicated login, false otherwise
	 * @throws ServiceException
	 */
	public boolean checkUser(Operation operation,String login) throws ServiceException
	{
		boolean userFound=false;
		try
		{
			USERS_DAO.setOperation(operation);
			userFound=USERS_DAO.checkUser(login);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userFound;
	}
	
	/**
	 * Generate an unused OUCU for an user.
	 * @param operation Operation
	 * @param user User
	 */
	private void generateUnusedOucuForUser(Operation operation,User user)
	{
		boolean foundUnusedOucu=false;
		String oucu=null;
		String userLogin=user.getLogin();
		
		// First we try as OUCU the user's nick clipped to 8 characters as maximum length
		if (userLogin.length()>OUCU_MAX_LENGTH)
		{
			oucu=userLogin.substring(0,OUCU_MAX_LENGTH);
		}
		else
		{
			oucu=userLogin;
		}
		try
		{
			USERS_DAO.setOperation(operation);
			foundUnusedOucu=!USERS_DAO.checkOucu(oucu) && checkUnusedOucuOnTestNavigators(oucu);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		// If that OUCU is not available we will try more OUCUs with an integer value at the end
		// until we find an available one
		if (!foundUnusedOucu)
		{
			int i=1;
			while (!foundUnusedOucu)
			{
				String iStr=Integer.toString(i);
				StringBuffer newOucu=new StringBuffer();
				if (oucu.length()+iStr.length()<=OUCU_MAX_LENGTH)
				{
					newOucu.append(oucu);
				}
				else
				{
					newOucu.append(oucu.substring(0,OUCU_MAX_LENGTH-iStr.length()));
				}
				newOucu.append(iStr);
				i++;
				try
				{
					USERS_DAO.setOperation(operation);
					foundUnusedOucu=!USERS_DAO.checkOucu(newOucu.toString()) && 
						checkUnusedOucuOnTestNavigators(newOucu.toString());
				}
				catch (DaoException de)
				{
					throw new ServiceException(de.getMessage(),de);
				}
				if (foundUnusedOucu)
				{
					oucu=newOucu.toString();
				}
			}
		}
		// Finally we set user OUCU with generated one
		user.setOucu(oucu);
	}
	
	/**
	 * Check if an OUCU is unused on Test Navigators (checking only available environments).
	 * @param oucu OUCU
	 * @return true if OUCU is unused on Test Navigators (checking only available environments),
	 * false otherwise
	 * @throws Exception
	 */
	private boolean checkUnusedOucuOnTestNavigators(String oucu)
	{
		boolean unused=true;
		String omTnURL=configurationService.getOmTnUrl();
		if (omTnURL!=null)
		{
			StringBuffer omTnWsURL=new StringBuffer(omTnURL);
			if (omTnURL.charAt(omTnURL.length()-1)!='/')
			{
				omTnWsURL.append('/');
			}
			omTnWsURL.append("services/OmTn");
			OmTnProxy omTnWs=new OmTnProxy();
			omTnWs.setEndpoint(omTnWsURL.toString());
			try
			{
				unused=omTnWs.isOUCUAvailable(oucu);
			}
			catch (RemoteException re)
			{
				unused=true;
			}
		}
		if (unused)
		{
			String omTnProURL=configurationService.getOmTnProUrl();
			if (omTnProURL!=null)
			{
				StringBuffer omTnProWsURL=new StringBuffer(omTnProURL);
				if (omTnProURL.charAt(omTnProURL.length()-1)!='/')
				{
					omTnProWsURL.append('/');
				}
				omTnProWsURL.append("services/OmTn");
				OmTnProxy omTnProWs=new OmTnProxy();
				omTnProWs.setEndpoint(omTnProWsURL.toString());
				try
				{
					unused=omTnProWs.isOUCUAvailable(oucu);
				}
				catch (RemoteException re)
				{
					unused=true;
				}
			}
		}
		return unused;
	}
}
