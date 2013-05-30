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

import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestUser;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to users of tests data.
 */
public class TestUsersDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new user of a test to DB.
	 * @param testUser User of a test to add
	 * @return User of a test identifier
	 * @throws DaoException
	 */
	public long saveTestUser(TestUser testUser) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(testUser)).longValue();
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
	 * Updates an user of a test on DB.
	 * @param testUser User of a test to update
	 * @throws DaoException
	 */
	public void updateTestUser(TestUser testUser) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(testUser);
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
	 * Deletes an user of a test from DB.
	 * @param testUser User of a test to delete
	 * @throws DaoException
	 */
	public void deleteTestUser(TestUser testUser) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(testUser);
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
	 * @param id User of a test identifier
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @return User of a test from DB
	 * @throws DaoException
	 */
	public TestUser getTestUser(long id,boolean includeUserType) throws DaoException
	{
		TestUser testUser=null;
		try
		{
			startOperation();
			testUser=(TestUser)operation.session.get(TestUser.class,id);
			if (testUser!=null)
			{
				Test test=testUser.getTest();
				Hibernate.initialize(test);
				Hibernate.initialize(test.getCreatedBy());
				Hibernate.initialize(test.getModifiedBy());
				User user=testUser.getUser();
				Hibernate.initialize(user);
				if (includeUserType)
				{
					Hibernate.initialize(test.getCreatedBy().getUserType());
					Hibernate.initialize(test.getModifiedBy().getUserType());
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
		return testUser;
	}
	
	/**
	 * @param testId Test identifier
	 * @param userId User identifier
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @return User of a test from DB
	 * @throws DaoException
	 */
	public TestUser getTestUser(long testId,long userId,boolean includeUserType) throws DaoException
	{
		TestUser testUser=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from TestUser t where t.test = :testId and t.user = :userId");
			query.setParameter("testId",Long.valueOf(testId),StandardBasicTypes.LONG);
			query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			testUser=(TestUser)query.uniqueResult();
			if (testUser!=null)
			{
				Test test=testUser.getTest();
				Hibernate.initialize(test);
				Hibernate.initialize(test.getCreatedBy());
				Hibernate.initialize(test.getModifiedBy());
				User user=testUser.getUser();
				Hibernate.initialize(user);
				if (includeUserType)
				{
					Hibernate.initialize(test.getCreatedBy().getUserType());
					Hibernate.initialize(test.getModifiedBy().getUserType());
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
		return testUser;
	}
	
	/**
	 * @param testId Filtering test identifier or 0 to get users of all tests
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @param sortedByTest Flag to indicate if we want the results sorted by test
	 * @param sortedByUser Flag to indicate if we want the results sorted by user
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @return List of users of a test (or all users of any test if testId==0) optionally filtered by their
	 * permissions to do/administrate tests and optionally sorted by user
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<TestUser> getTestUsers(long testId,Boolean omUser,Boolean omAdmin,boolean sortedByTest,
		boolean sortedByUser,boolean includeUserType) throws DaoException
	{
		List<TestUser> testUsers=null;
		boolean addedFilter=false;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from TestUser t");
			StringBuffer sortedByString=null;
			if (testId>0L)
			{
				addedFilter=true;
				queryString.append(" where t.test = :testId");
			}
			else if (sortedByTest)
			{
				sortedByString=new StringBuffer(" order by t.test.name");
			}
			if (omUser!=null)
			{
				if (addedFilter)
				{
					queryString.append(" and t.omUser = :omUser");
				}
				else
				{
					addedFilter=true;
					queryString.append(" where t.omUser = :omUser");
				}
			}
			if (omAdmin!=null)
			{
				if (addedFilter)
				{
					queryString.append(" and t.omAdmin = :omAdmin");
				}
				else
				{
					addedFilter=true;
					queryString.append(" where t.omAdmin = :omAdmin");
				}
			}
			if (sortedByUser)
			{
				if (sortedByString==null)
				{
					sortedByString=new StringBuffer(" order by t.user.login");
				}
				else
				{
					sortedByString.append(", t.user.login");
				}
			}
			if (sortedByString!=null)
			{
				queryString.append(sortedByString);
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (testId>0L)
			{
				query.setParameter("testId",Long.valueOf(testId),StandardBasicTypes.LONG);
			}
			if (omUser!=null)
			{
				query.setParameter("omUser",omUser,StandardBasicTypes.BOOLEAN);
			}
			if (omAdmin!=null)
			{
				query.setParameter("omAdmin",omAdmin,StandardBasicTypes.BOOLEAN);
			}
			testUsers=query.list();
			if (includeUserType)
			{
				for (TestUser testUser:testUsers)
				{
					Test test=testUser.getTest();
					Hibernate.initialize(test);
					Hibernate.initialize(test.getCreatedBy());
					Hibernate.initialize(test.getCreatedBy().getUserType());
					Hibernate.initialize(test.getModifiedBy());
					Hibernate.initialize(test.getModifiedBy().getUserType());
					User user=testUser.getUser();
					Hibernate.initialize(user);
					Hibernate.initialize(user.getUserType());
				}
			}
			else
			{
				for (TestUser testUser:testUsers)
				{
					Test test=testUser.getTest();
					Hibernate.initialize(test);
					Hibernate.initialize(test.getCreatedBy());
					Hibernate.initialize(test.getModifiedBy());
					Hibernate.initialize(testUser.getUser());
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
		return testUsers;
	}
	
	/**
	 * @param userId Filtering user identifier or 0 to get tests of all users
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @param sortedByTest Flag to indicate if we want the results sorted by test
	 * @param sortedByUser Flag to indicate if we want the results sorted by user
	 * @param includeUserType Flag to indicate if we need to initialize user type
	 * @return List of tests of an user (or all tests of any user if userId==0) optionally filtered by the
	 * user permissions to do/administrate tests and optionally sorted by test
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<TestUser> getUserTests(long userId,Boolean omUser,Boolean omAdmin,boolean sortedByTest,
		boolean sortedByUser,boolean includeUserType) throws DaoException
	{
		List<TestUser> userTests=null;
		boolean addedFilter=false;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from TestUser t");
			StringBuffer sortedByString=null;
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" where t.user = :userId");
			}
			else if (sortedByUser)
			{
				sortedByString=new StringBuffer(" order by t.user.nick");
			}
			if (omUser!=null)
			{
				if (addedFilter)
				{
					queryString.append(" and t.omUser = :omUser");
				}
				else
				{
					addedFilter=true;
					queryString.append(" where t.omUser = :omUser");
				}
			}
			if (omAdmin!=null)
			{
				if (addedFilter)
				{
					queryString.append(" and t.omAdmin = :omAdmin");
				}
				else
				{
					addedFilter=true;
					queryString.append(" where t.omAdmin = :omAdmin");
				}
			}
			if (sortedByTest)
			{
				if (sortedByString==null)
				{
					sortedByString=new StringBuffer(" order by t.test.name");
				}
				else
				{
					sortedByString.append(", t.test.name");
				}
			}
			if (sortedByString!=null)
			{
				queryString.append(sortedByString);
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			if (omUser!=null)
			{
				query.setParameter("omUser",omUser,StandardBasicTypes.BOOLEAN);
			}
			if (omAdmin!=null)
			{
				query.setParameter("omAdmin",omAdmin,StandardBasicTypes.BOOLEAN);
			}
			userTests=query.list();
			if (includeUserType)
			{
				for (TestUser userTest:userTests)
				{
					Test test=userTest.getTest();
					Hibernate.initialize(test);
					Hibernate.initialize(test.getCreatedBy());
					Hibernate.initialize(test.getCreatedBy().getUserType());
					Hibernate.initialize(test.getModifiedBy());
					Hibernate.initialize(test.getModifiedBy().getUserType());
					User user=userTest.getUser();
					Hibernate.initialize(user);
					Hibernate.initialize(user.getUserType());
				}
			}
			else
			{
				for (TestUser userTest:userTests)
				{
					Test test=userTest.getTest();
					Hibernate.initialize(test);
					Hibernate.initialize(test.getCreatedBy());
					Hibernate.initialize(test.getModifiedBy());
					Hibernate.initialize(userTest.getUser());
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
		return userTests;
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
