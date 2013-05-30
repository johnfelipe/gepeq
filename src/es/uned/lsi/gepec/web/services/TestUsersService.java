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
import es.uned.lsi.gepec.model.dao.TestUsersDao;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestUser;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages users of tests.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class TestUsersService implements Serializable
{
	private final static TestUsersDao TEST_USERS_DAO=new TestUsersDao();
	
	public TestUsersService()
	{
	}
	
	/**
	 * @param id User of test identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(long id) throws ServiceException
	{
		return getTestUser((Operation)null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id User of test identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Operation operation,long id) throws ServiceException
	{
		TestUser testUser=null;
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			testUser=TEST_USERS_DAO.getTestUser(id,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return testUser;
	}
	
	/**
	 * @param test Test
	 * @param user User
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Test test,User user) throws ServiceException
	{
		return getTestUser(null,test==null?0L:test.getId(),user==null?0L:user.getId()); 
	}
	
	/**
	 * @param test Test
	 * @param userId User identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Test test,long userId) throws ServiceException
	{
		return getTestUser(null,test==null?0L:test.getId(),userId);
	}
	
	/**
	 * @param testId Test identifier
	 * @param user User
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(long testId,User user) throws ServiceException
	{
		return getTestUser(null,testId,user==null?0L:user.getId());
	}
	
	/**
	 * @param testId Test identifier
	 * @param userId User identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(long testId,long userId) throws ServiceException
	{
		return getTestUser(null,testId,userId);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @param user User
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Operation operation,Test test,User user) throws ServiceException
	{
		return getTestUser(operation,test==null?0L:test.getId(),user==null?0L:user.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @param userId User identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Operation operation,Test test,long userId) throws ServiceException
	{
		return getTestUser(operation,test==null?0L:test.getId(),userId);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param user User
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Operation operation,long testId,User user) throws ServiceException
	{
		return getTestUser(operation,testId,user==null?0L:user.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param userId User identifier
	 * @return User of test
	 * @throws ServiceException
	 */
	public TestUser getTestUser(Operation operation,long testId,long userId) throws ServiceException
	{
		TestUser testUser=null;
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			testUser=TEST_USERS_DAO.getTestUser(testId,userId,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return testUser;
	}
	
	/**
	 * Adds a new user of test.
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void addTestUser(TestUser testUser) throws ServiceException
	{
		addTestUser(null,testUser);
	}
	
	/**
	 * Adds a new user of test.
	 * @param operation Operation
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void addTestUser(Operation operation,TestUser testUser) throws ServiceException
	{
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			TEST_USERS_DAO.saveTestUser(testUser);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a user of test.
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void updateTestUser(TestUser testUser) throws ServiceException
	{
		updateTestUser(null,testUser);
	}
	
	/**
	 * Updates a user of test.
	 * @param operation Operation
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void updateTestUser(Operation operation,TestUser testUser) throws ServiceException
	{
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			TEST_USERS_DAO.updateTestUser(testUser);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a user of test.
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void deleteTestUser(TestUser testUser) throws ServiceException
	{
		deleteTestUser(null,testUser);
	}
	
	/**
	 * Deletes a user of test.
	 * @param operation Operation
	 * @param testUser User of test
	 * @throws ServiceException
	 */
	public void deleteTestUser(Operation operation,TestUser testUser) throws ServiceException
	{
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			TEST_USERS_DAO.deleteTestUser(testUser);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @param sorted Flag to indicate if we want the results sorted by test and user
	 * @return List of users of a test (or all users of any test if testId==0) optionally filtered by their
	 * permissions to do/administrate tests and optionally sorted by test and user
	 * @throws ServiceException
	 */
	private List<TestUser> getTestUsers(Operation operation,long testId,Boolean omUser,Boolean omAdmin,boolean sorted) 
		throws ServiceException
	{
		List<TestUser> testUsers=null;
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			testUsers=TEST_USERS_DAO.getTestUsers(testId,omUser,omAdmin,sorted,sorted,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return testUsers;
	}
	
	/**
	 * @param test Test
	 * @return List of users of a test
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Test test) throws ServiceException
	{
		return getTestUsers(null,test==null?0L:test.getId(),null,null,false);
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of users of a test
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(long testId) throws ServiceException
	{
		return getTestUsers(null,testId,null,null,false);
	}
	
	/**
	 * @param test Test
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if test==null) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Test test,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getTestUsers(null,test==null?0L:test.getId(),omUser,omAdmin,false);
	}
	
	/**
	 * @param testId Test identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if testId==0) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(long testId,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getTestUsers(null,testId,omUser,omAdmin,false);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of users of a test
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Operation operation,Test test) throws ServiceException
	{
		return getTestUsers(operation,test==null?0L:test.getId(),null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of users of a test
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Operation operation,long testId) throws ServiceException
	{
		return getTestUsers(operation,testId,null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if test==null) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Operation operation,Test test,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getTestUsers(operation,test==null?0L:test.getId(),omUser,omAdmin,false);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if testId==0) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getTestUsers(Operation operation,long testId,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getTestUsers(operation,testId,omUser,omAdmin,false);
	}
	
	/**
	 * @param test Test
	 * @return List of users of a test sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Test test) throws ServiceException
	{
		return getTestUsers(null,test==null?0L:test.getId(),null,null,true);
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of users of a test sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(long testId) throws ServiceException
	{
		return getTestUsers(null,testId,null,null,true);
	}
	
	/**
	 * @param test Test
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if test==null) filtered by 
	 * their permissions to do/administrate tests and sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Test test,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getTestUsers(null,test==null?0L:test.getId(),Boolean.valueOf(omUser),Boolean.valueOf(omAdmin),true);
	}
	
	/**
	 * @param testId Test identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if testId==0) filtered by 
	 * their permissions to do/administrate tests and sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(long testId,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getTestUsers(null,testId,omUser,omAdmin,true);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of users of a test sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Operation operation,Test test) throws ServiceException
	{
		return getTestUsers(operation,test==null?0L:test.getId(),null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of users of a test sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Operation operation,long testId) throws ServiceException
	{
		return getTestUsers(operation,testId,null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if test==null) filtered by 
	 * their permissions to do/administrate tests and sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Operation operation,Test test,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getTestUsers(operation,test==null?0L:test.getId(),omUser,omAdmin,true);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of users of a test (or all users of any test if testId==0) filtered by 
	 * their permissions to do/administrate tests and sorted by test and user
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedTestUsers(Operation operation,long testId,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getTestUsers(operation,testId,omUser,omAdmin,true);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @param sorted Flag to indicate if we want the results sorted by user and test
	 * @return List of tests of an user (or all tests of any user if userId==0) optionally filtered by their
	 * permissions to do/administrate tests and optionally sorted by user and test
	 * @throws ServiceException
	 */
	private List<TestUser> getUserTests(Operation operation,long userId,Boolean omUser,Boolean omAdmin,boolean sorted) 
		throws ServiceException
	{
		List<TestUser> userTests=null;
		try
		{
			TEST_USERS_DAO.setOperation(operation);
			userTests=TEST_USERS_DAO.getUserTests(userId,omUser,omAdmin,sorted,sorted,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return userTests;
	}
	
	/**
	 * @param user User
	 * @return List of tests of an user
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(User user) throws ServiceException
	{
		return getUserTests(null,user==null?0L:user.getId(),null,null,false);
	}
	
	/**
	 * @param userId User identifier
	 * @return List of tests of an user
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(long userId) throws ServiceException
	{
		return getUserTests(null,userId,null,null,false);
	}
	
	/**
	 * @param user User
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if user==null) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(User user,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getUserTests(null,user==null?0L:user.getId(),omUser,omAdmin,false);
	}
	
	/**
	 * @param userId User identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if userId==0) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(long userId,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getUserTests(null,userId,omUser,omAdmin,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of tests of an user
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(Operation operation,User user) throws ServiceException
	{
		return getUserTests(operation,user==null?0L:user.getId(),null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @return List of tests of an user
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(Operation operation,long userId) throws ServiceException
	{
		return getUserTests(operation,userId,null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if user==null) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(Operation operation,User user,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getUserTests(operation,user==null?0L:user.getId(),omUser,omAdmin,false);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if userId==0) filtered by 
	 * their permissions to do/administrate tests
	 * @throws ServiceException
	 */
	public List<TestUser> getUserTests(Operation operation,long userId,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getUserTests(operation,userId,omUser,omAdmin,false);
	}
	
	/**
	 * @param user User
	 * @return List of tests of an user sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(User user) throws ServiceException
	{
		return getUserTests(null,user==null?0L:user.getId(),null,null,true);
	}
	
	/**
	 * @param userId User identifier
	 * @return List of tests of an user sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(long userId) throws ServiceException
	{
		return getUserTests(null,userId,null,null,true);
	}
	
	/**
	 * @param user User
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if user==null) filtered by 
	 * their permissions to do/administrate tests and sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(User user,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getUserTests(null,user==null?0L:user.getId(),omUser,omAdmin,true);
	}
	
	/**
	 * @param userId User identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if userId==0) filtered by 
	 * their permissions to do/administrate tests and sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(long userId,Boolean omUser,Boolean omAdmin) throws ServiceException
	{
		return getUserTests(null,userId,omUser,omAdmin,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of tests of an user sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(Operation operation,User user) throws ServiceException
	{
		return getUserTests(operation,user==null?0L:user.getId(),null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @return List of tests of an user sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(Operation operation,long userId) throws ServiceException
	{
		return getUserTests(operation,userId,null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if user==null) filtered by 
	 * their permissions to do/administrate tests and sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(Operation operation,User user,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getUserTests(operation,user==null?0L:user.getId(),omUser,omAdmin,true);
	}
	
	/**
	 * @param operation Operation
	 * @param userId User identifier
	 * @param omUser Filtering by users allowed to do test (true) or not (false) or non filtering (null)
	 * @param omAdmin Filtering by users with administration privileges for the test (true) or not (false)
	 * or non filtering (null)
	 * @return List of tests of an user (or all tests of any user if userId==0) filtered by 
	 * their permissions to do/administrate tests and sorted by user and test
	 * @throws ServiceException
	 */
	public List<TestUser> getSortedUserTests(Operation operation,long userId,Boolean omUser,Boolean omAdmin) 
		throws ServiceException
	{
		return getUserTests(operation,userId,omUser,omAdmin,true);
	}
}
