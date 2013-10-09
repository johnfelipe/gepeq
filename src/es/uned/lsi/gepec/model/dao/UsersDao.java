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

import es.uned.lsi.encryption.PasswordDigester;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

/**
 * Manages access to users data. 
 */
public class UsersDao
{  
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda un usuario en la bd
	/**
	 * Adds a new user to DB.
	 * @param user User to add
	 * @return User identifier
	 * @throws DaoException
	 */
	public long saveUser(User user) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(user)).longValue();
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
	
	//Actualiza un usuario en la bd
	/**
	 * Updates an user on DB.
	 * @param user User to update
	 * @throws DaoException
	 */
	public void updateUser(User user) throws DaoException 
	{
		try 
		{
			startOperation(); 
			operation.session.update(user);
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
	
	//Elimina un usuario de la bd
	/**
	 * Deletes an user from DB.
	 * @param user User to delete
	 * @throws DaoException
	 */
	public void deleteUser(User user) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(user);
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
	
	//Obtiene un usuario a partir de su id
	/**
	 * @param userId User identifier
	 * @return User from DB
	 * @throws DaoException
	 */
	public User getUser(long userId) throws DaoException
	{
		return getUser(userId,true);
	}
	
	/**
	 * @param userId User identifier
	 * @param includeUserType true to include user type information, false otherwise
	 * @return User from DB
	 * @throws DaoException
	 */
	public User getUser(long userId,boolean includeUserType) throws DaoException
	{
		User user=null;
		try
		{
			startOperation();
			user=(User)operation.session.get(User.class,userId);
			if (user!=null)
			{
				if (includeUserType)
				{
					Hibernate.initialize(user.getUserType());
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
		return user;
	}
	
	/**
	 * @param userOucu User Oucu
	 * @return User from DB
	 * @throws DaoException
	 */
	public User getUserFromOucu(String userOucu) throws DaoException
	{
		return getUserFromOucu(userOucu,true);
	}
	
	/**
	 * @param userOucu User Oucu
	 * @param includeUserType true to include user type information, false otherwise
	 * @return User from DB
	 * @throws DaoException
	 */
	public User getUserFromOucu(String userOucu,boolean includeUserType) throws DaoException
	{
		User user=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from User u where u.oucu = :userOucu");
			query.setParameter("userOucu",userOucu,StandardBasicTypes.STRING);
			user=(User)query.uniqueResult();
			if (user!=null)
			{
				if (includeUserType)
				{
					Hibernate.initialize(user.getUserType());
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
		return user;
	}
	
	/**
	 * @param userLogin User login name
	 * @param userPassword User password
	 * @return User from DB if authentication is successful or null otherwise
	 * @throws DaoException
	 */
	public User getAuthenticatedUser(String userLogin,String userPassword) throws DaoException
	{
		return getAuthenticatedUser(userLogin,userPassword,true);
	}
	
	/**
	 * @param userLogin User login name
	 * @param userPassword Encrypted password
	 * @param includeUserType true to include user type information, false otherwise
	 * @return User from DB if authentication is successful or null otherwise
	 * @throws DaoException
	 */
	public User getAuthenticatedUser(String userLogin,String userPassword,boolean includeUserType) 
		throws DaoException
	{
		User user=null;
		if (userLogin!=null && userPassword!=null)
		{
			try
			{
				startOperation();
				Query query=operation.session.createQuery("from User u where u.login = :userLogin");
				query.setParameter("userLogin",userLogin,StandardBasicTypes.STRING);
				user=(User)query.uniqueResult();
				if (user!=null)
				{
					if (PasswordDigester.matches(userPassword,user.getPassword()))
					{
						if (!user.isGepeqUser())
						{
							String errorMessage=null;
							FacesContext facesContext=FacesContext.getCurrentInstance();
							if (facesContext==null)
							{
								errorMessage="The user entered is not authorized to execute this application.";
							}
							else
							{
								ELContext elContext=facesContext.getELContext();
								LocalizationService localizationService=
									(LocalizationService)FacesContext.getCurrentInstance().getApplication().
									getELResolver().getValue(elContext,null,"localizationService");
								errorMessage=localizationService.getLocalizedMessage("NON_GEPEQ_USER_LOGIN_ERROR");
							}
							throw new DaoException(errorMessage);
						}
						if (includeUserType)
						{
							Hibernate.initialize(user.getUserType());
						}
					}
					else
					{
						user=null;
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
		}
		return user;
	}
	
	/**
	 * @param userTypeId Filtering user type identifier or 0 to get users of all user types
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @param includeUserType true to include user type information, false otherwise
	 * @param sortedByLogin Flag to indicate if we want the results sorted by login
	 * @return List of all users filtered optionally by user type and excluding optionally non GEPEQ users
	 * and sorted optionally by login
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsers(long userTypeId,boolean includeOmUsers,boolean includeUserType,
		boolean sortedByLogin) throws DaoException
	{
		List<User> users=null;
		boolean addedFilter=false;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from User u");
			if (userTypeId>0L)
			{
				addedFilter=true;
				queryString.append(" Where u.userType = :userTypeId");
			}
			if (!includeOmUsers)
			{
				if (addedFilter)
				{
					queryString.append(" And u.gepeqUser = true");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where u.gepeqUser = true");
				}
			}
			if (sortedByLogin)
			{
				queryString.append(" order by u.login");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userTypeId>0L)
			{
				query.setParameter("userTypeId",Long.valueOf(userTypeId),StandardBasicTypes.LONG);
			}
			users=query.list();
			if (includeUserType)
			{
				for (User user:users)
				{
					Hibernate.initialize(user.getUserType());
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
		return users;
	}
	
	/**
	 * @param includeOmUsers true to get all users (even users that only have access to OpenMark), 
	 * false to get only users with access to GEPEQ
	 * @param sortedByLogin Flag to indicate if we want the results sorted by login
	 * @return List of all users without user type
	 * @return List of all users without user type filtered optionally by user type and excluding optionally 
	 * non GEPEQ users and sorted optionally by login
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getUsersWithoutUserType(boolean includeOmUsers,boolean sortedByLogin)
	{
		List<User> users=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from User u Where u.userType is null");
			if (!includeOmUsers)
			{
				queryString.append(" And u.gepeqUser = true ");
			}
			if (sortedByLogin)
			{
				queryString.append(" order by u.login");
			}
			users=operation.session.createQuery(queryString.toString()).list();
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
		return users;
	}
	
	/**
	 * @return Number of users
	 * @throws DaoException
	 */
	public long getUsersCount() throws DaoException
	{
		long usersCount=0L;
		try
		{
			startOperation();
			usersCount=
				((Long)operation.session.createQuery("select count(u) from User u").uniqueResult()).longValue();
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
		return usersCount;
	}
	
	/**
	 * Checks if exists an user with the indicated login
	 * @param login User's login
	 * @return true if exists an user with the indicated login, false otherwise
	 * @throws DaoException
	 */
	public boolean checkUserLogin(String login) throws DaoException
	{
		boolean userFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(u) from User u where u.login = :login");
			query.setParameter("login", login, StandardBasicTypes.STRING);
			userFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return userFound;
	}
	
	/**
	 * Checks if exists an user with the indicated identifier
	 * @param id Identifier
	 * @return true if exists an user with the indicated identifier, false otherwise
	 * @throws DaoException
	 */
	public boolean checkUserId(long id) throws DaoException
	{
		boolean userFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(u) from User u where u.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			userFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return userFound;
	}
	
	/**
	 * Checks if exists an user with the indicated OUCU
	 * @param oucu User's oucu
	 * @return true if exists an user with the indicated OUCU, false otherwise
	 * @throws DaoException
	 */
	public boolean checkOucu(String oucu) throws DaoException
	{
		boolean userFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(u) from User u where u.oucu = :oucu");
			query.setParameter("oucu",oucu,StandardBasicTypes.STRING);
			userFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return userFound;
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
