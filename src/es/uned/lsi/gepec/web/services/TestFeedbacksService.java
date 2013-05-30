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
import es.uned.lsi.gepec.model.dao.TestFeedbacksDao;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages test feedbacks.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class TestFeedbacksService implements Serializable
{
	private final static TestFeedbacksDao TEST_FEEDBACKS_DAO=new TestFeedbacksDao();
	
	public TestFeedbacksService()
	{
	}
	
	/**
	 * @param id Test feedback identifier
	 * @return Test feedback
	 * @throws ServiceException
	 */
	public TestFeedback getTestFeedback(long id) throws ServiceException
	{
		return getTestFeedback(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Test feedback identifier
	 * @return Test feedback
	 * @throws ServiceException
	 */
	public TestFeedback getTestFeedback(Operation operation,long id) throws ServiceException
	{
		TestFeedback testFeedback=null;
		try
		{
			TEST_FEEDBACKS_DAO.setOperation(operation);
			testFeedback=TEST_FEEDBACKS_DAO.getTestFeedback(id,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return testFeedback;
	}
	
	/**
	 * Updates a test feedback.
	 * @param testFeedback Test feedback to update
	 * @throws ServiceException
	 */
	public void updateTestFeedback(TestFeedback testFeedback) throws ServiceException
	{
		updateTestFeedback(null,testFeedback);
	}
	
	/**
	 * Updates a test feedback.
	 * @param operation Operation
	 * @param testFeedback Test feedback to update
	 * @throws ServiceException
	 */
	public void updateTestFeedback(Operation operation,TestFeedback testFeedback) throws ServiceException
	{
		try
		{
			TEST_FEEDBACKS_DAO.setOperation(operation);
			TEST_FEEDBACKS_DAO.updateTestFeedback(testFeedback);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Adds a new test feedback.
	 * @param testFeedback Test feedback to add
	 * @throws ServiceException
	 */
	public void addTestFeedback(TestFeedback testFeedback) throws ServiceException
	{
		addTestFeedback(null,testFeedback);
	}
	
	/**
	 * Adds a new test feedback.
	 * @param operation Operation
	 * @param testFeedback Test feedback to add
	 * @throws ServiceException
	 */
	public void addTestFeedback(Operation operation,TestFeedback testFeedback) throws ServiceException
	{
		try
		{
			TEST_FEEDBACKS_DAO.setOperation(operation);
			TEST_FEEDBACKS_DAO.saveTestFeedback(testFeedback);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a test feedback.
	 * @param testFeedback Test feedback to delete
	 * @throws ServiceException
	 */
	public void deleteTestFeedback(TestFeedback testFeedback) throws ServiceException
	{
		deleteTestFeedback(null,testFeedback);
	}
	
	/**
	 * Deletes a test feedback.
	 * @param operation Operation
	 * @param testFeedback Test feedback to delete
	 * @throws ServiceException
	 */
	public void deleteTestFeedback(Operation operation,TestFeedback testFeedback) throws ServiceException
	{
		try
		{
			TEST_FEEDBACKS_DAO.setOperation(operation);
			TEST_FEEDBACKS_DAO.deleteTestFeedback(testFeedback);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param test Test
	 * @return List of feedbacks of a test
	 * @throws ServiceException
	 */
	public List<TestFeedback> getTestFeedbacks(Test test) throws ServiceException
	{
		return getTestFeedbacks(null,test==null?0L:test.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of feedbacks of a test
	 * @throws ServiceException
	 */
	public List<TestFeedback> getTestFeedbacks(Operation operation,Test test) throws ServiceException
	{
		return getTestFeedbacks(operation,test==null?0L:test.getId());
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of feedbacks of a test
	 * @throws ServiceException
	 */
	public List<TestFeedback> getTestFeedbacks(long testId) throws ServiceException
	{
		return getTestFeedbacks(null,testId);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of feedbacks of a test
	 * @throws ServiceException
	 */
	public List<TestFeedback> getTestFeedbacks(Operation operation,long testId) throws ServiceException
	{
		List<TestFeedback> testFeedbacks=null;
		try
		{
			TEST_FEEDBACKS_DAO.setOperation(operation);
			testFeedbacks=TEST_FEEDBACKS_DAO.getTestFeedbacks(testId,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return testFeedbacks;
	}
}
