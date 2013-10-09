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
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.EvaluatorsDao;
import es.uned.lsi.gepec.model.entities.Evaluator;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages evaluators.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class EvaluatorsService implements Serializable
{
	private final static EvaluatorsDao EVALUATORS_DAO=new EvaluatorsDao();
	
	/**
	 * @param id Evaluator identifier
	 * @return Evaluator
	 * @throws ServiceException
	 */
	public Evaluator getEvaluator(long id) throws ServiceException
	{
		return getEvaluator(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Evaluator identifier
	 * @return Evaluator
	 * @throws ServiceException
	 */
	public Evaluator getEvaluator(Operation operation,long id) throws ServiceException
	{
		Evaluator evaluator=null;
		try
		{
			// Get evaluator from DB
			EVALUATORS_DAO.setOperation(operation);
			Evaluator evaluatorFromDB=EVALUATORS_DAO.getEvaluator(id);
			if (evaluatorFromDB!=null)
			{
				evaluator=evaluatorFromDB.getEvaluatorCopy();
				if (evaluatorFromDB.getTest()!=null)
				{
					evaluator.setTest(evaluatorFromDB.getTest().getTestCopy());
				}
				if (evaluatorFromDB.getAddressType()!=null)
				{
					evaluator.setAddressType(evaluatorFromDB.getAddressType().getAddressTypeCopy());
				}
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return evaluator;
	}
	
	/**
	 * Updates an evaluator.
	 * @param evaluator Evaluator to update
	 * @throws ServiceException
	 */
	public void updateEvaluator(Evaluator evaluator) throws ServiceException
	{
		updateEvaluator(null,evaluator);
	}
	
	/**
	 * Updates an evaluator.
	 * @param operation Operation
	 * @param evaluator Evaluator to update
	 * @throws ServiceException
	 */
	public void updateEvaluator(Operation operation,Evaluator evaluator) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get evaluator from DB
			EVALUATORS_DAO.setOperation(operation);
			Evaluator evaluatorFromDB=EVALUATORS_DAO.getEvaluator(evaluator.getId(),false);
			
			// Set fields with the updated values
			evaluatorFromDB.setFromOtherEvaluator(evaluator);
			
			// Update evaluator
			EVALUATORS_DAO.setOperation(operation);
			EVALUATORS_DAO.updateEvaluator(evaluatorFromDB);
			
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
	 * Adds a new evaluator.
	 * @param evaluator Evaluator to add
	 * @throws ServiceException
	 */
	public void addEvaluator(Evaluator evaluator) throws ServiceException
	{
		addEvaluator(null,evaluator);
	}
	
	/**
	 * Adds a new evaluator.
	 * @param operation Operation
	 * @param evaluator Evaluator to add
	 * @throws ServiceException
	 */
	public void addEvaluator(Operation operation,Evaluator evaluator) throws ServiceException
	{
		try
		{
			// Add a new evaluator
			EVALUATORS_DAO.setOperation(operation);
			EVALUATORS_DAO.saveEvaluator(evaluator);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes an evaluator.
	 * @param evaluator Evaluator to delete
	 * @throws ServiceException
	 */
	public void deleteEvaluator(Evaluator evaluator) throws ServiceException
	{
		deleteEvaluator(null,evaluator);
	}
	
	/**
	 * Deletes an evaluator.
	 * @param operation Operation
	 * @param evaluator Evaluator to delete
	 * @throws ServiceException
	 */
	public void deleteEvaluator(Operation operation,Evaluator evaluator) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get evaluator from DB
			EVALUATORS_DAO.setOperation(operation);
			Evaluator evaluatorFromDB=EVALUATORS_DAO.getEvaluator(evaluator.getId(),false);
			
			// Delete evaluator
			EVALUATORS_DAO.setOperation(operation);
			EVALUATORS_DAO.deleteEvaluator(evaluatorFromDB);
			
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
	 * @param test Test
	 * @return List of evaluators of a test
	 * @throws ServiceException
	 */
	public List<Evaluator> getEvaluators(Test test) throws ServiceException
	{
		return getEvaluators(null,test==null?0L:test.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of evaluators of a test
	 * @throws ServiceException
	 */
	public List<Evaluator> getEvaluators(Operation operation,Test test) throws ServiceException
	{
		return getEvaluators(operation,test==null?0L:test.getId());
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of evaluators of a test
	 * @throws ServiceException
	 */
	public List<Evaluator> getEvaluators(long testId) throws ServiceException
	{
		return getEvaluators(null,testId);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of evaluators of a test
	 * @throws ServiceException
	 */
	public List<Evaluator> getEvaluators(Operation operation,long testId) throws ServiceException
	{
		List<Evaluator> evaluators=null;
		try
		{
			// We get evaluators from DB
			EVALUATORS_DAO.setOperation(operation);
			List<Evaluator> evaluatorsFromDB=EVALUATORS_DAO.getEvaluators(testId,true);
			
			// We return new referenced evaluators within a new list to avoid shared collection references
			// and object references to unsaved transient instances
			evaluators=new ArrayList<Evaluator>(evaluatorsFromDB.size());
			for (Evaluator evaluatorFromDB:evaluatorsFromDB)
			{
				Evaluator evaluator=evaluatorFromDB.getEvaluatorCopy();
				if (evaluatorFromDB.getTest()!=null)
				{
					evaluator.setTest(evaluatorFromDB.getTest().getTestCopy());
				}
				if (evaluatorFromDB.getAddressType()!=null)
				{
					evaluator.setAddressType(evaluatorFromDB.getAddressType().getAddressTypeCopy());
				}
				evaluators.add(evaluator);
			}
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return evaluators;
	}
}
