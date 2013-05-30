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

import es.uned.lsi.gepec.model.entities.TestFeedback;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to test feedbacks data. 
 */
public class TestFeedbacksDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new test feedback to DB.
	 * @param testFeedback Test feedback
	 * @return Test feedback identifier 
	 * @throws DaoException
	 */
	public long saveTestFeedback(TestFeedback testFeedback) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(testFeedback)).longValue();
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
	 * Update a test feedback on BD.
	 * @param testFeedback Test feedback
	 * @throws DaoException
	 */
	public void updateTestFeedback(TestFeedback testFeedback) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(testFeedback);
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
	 * Deletes a test feedback from BD.
	 * @param testFeedback Test feedback
	 * @throws DaoException
	 */
	public void deleteTestFeedback(TestFeedback testFeedback) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(testFeedback);
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
	 * @param id Test feedback identifier
	 * @param includeSection Flag to indicate if we need to initialize section (but no its questions)
	 * @param includeScoreUnit Flag to indicate if we need to initialize score unit
	 * @return Test feedback from DB
	 * @throws DaoException
	 */
	public TestFeedback getTestFeedback(long id,boolean includeSection,boolean includeScoreUnit) 
		throws DaoException
	{
		TestFeedback testFeedback=null;
		try
		{
			startOperation();
			testFeedback=(TestFeedback)operation.session.get(TestFeedback.class,id);
			if (testFeedback!=null)
			{
				if (includeSection)
				{
					Hibernate.initialize(testFeedback.getSection());
				}
				if (includeScoreUnit)
				{
					Hibernate.initialize(testFeedback.getScoreUnit());
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
		return testFeedback;
	}
	
	/**
	 * @param testId Test identifier
	 * @param includeSection Flag to indicate if we need to initialize section (but no its questions)
	 * @param includeScoreUnit Flag to indicate if we need to initialize score unit
	 * @return List of feedbacks of a test
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<TestFeedback> getTestFeedbacks(long testId,boolean includeSection,boolean includeScoreUnit) 
		throws DaoException
	{
		List<TestFeedback> testFeedbacks=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from TestFeedback t");
			if (testId>0L)
			{
				queryString.append(" where t.test = :testId");
			}
			queryString.append(" Order by t.position");
			Query query=operation.session.createQuery(queryString.toString());
			if (testId>0L)
			{
				query.setParameter("testId",Long.valueOf(testId),StandardBasicTypes.LONG);
			}
			testFeedbacks=query.list();
			if (includeSection)
			{
				if (includeScoreUnit)
				{
					for (TestFeedback testFeedback:testFeedbacks)
					{
						Hibernate.initialize(testFeedback.getSection());
						Hibernate.initialize(testFeedback.getScoreUnit());
					}
				}
				else
				{
					for (TestFeedback testFeedback:testFeedbacks)
					{
						Hibernate.initialize(testFeedback.getSection());
					}
				}
			}
			else if (includeScoreUnit)
			{
				for (TestFeedback testFeedback:testFeedbacks)
				{
					Hibernate.initialize(testFeedback.getScoreUnit());
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
		return testFeedbacks;
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
	 * @param he Hibernate Exception
	 * @throws DaoException
	 */
	private void handleException(HibernateException he) throws DaoException
	{
		handleException(he,true);
	}
	
	/**
	 * Manage errors produced while accesing persistent data.
	 * @param he Hibernate Exception
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
