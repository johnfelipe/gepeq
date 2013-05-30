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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.RedoQuestionValuesDao;
import es.uned.lsi.gepec.model.entities.RedoQuestionValue;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages available values of property 'redoQuestion'.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class RedoQuestionValuesService implements Serializable
{
	private final static RedoQuestionValuesDao REDO_QUESTION_VALUES_DAO=new RedoQuestionValuesDao();
	
	private final static Map<Long,RedoQuestionValue> REDO_QUESTION_VALUES_CACHED_BY_ID=
		new HashMap<Long,RedoQuestionValue>();
	private final static Map<String,RedoQuestionValue> REDO_QUESTION_VALUES_CACHED_BY_VALUE=
		new HashMap<String,RedoQuestionValue>();
	private final static List<RedoQuestionValue> REDO_QUESTION_VALUES_CACHED=new ArrayList<RedoQuestionValue>();
	
	public RedoQuestionValuesService()
	{
	}
	
	/**
	 * @param id Value of property 'redoQuestion' identifier
	 * @return Value of property 'redoQuestion'
	 * @throws ServiceException
	 */
	public RedoQuestionValue getRedoQuestion(long id) throws ServiceException
	{
		return getRedoQuestion(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Value of property 'redoQuestion' identifier
	 * @return Value of property 'redoQuestion'
	 * @throws ServiceException
	 */
	public RedoQuestionValue getRedoQuestion(Operation operation,long id) throws ServiceException
	{
		RedoQuestionValue redoQuestion=null;
		RedoQuestionValue redoQuestionFromCache=REDO_QUESTION_VALUES_CACHED_BY_ID.get(Long.valueOf(id));
		if (redoQuestionFromCache==null)
		{
			try
			{
				REDO_QUESTION_VALUES_DAO.setOperation(operation);
				redoQuestionFromCache=REDO_QUESTION_VALUES_DAO.getRedoQuestion(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (redoQuestionFromCache!=null)
			{
				REDO_QUESTION_VALUES_CACHED_BY_ID.put(Long.valueOf(id),redoQuestionFromCache);
				REDO_QUESTION_VALUES_CACHED_BY_VALUE.put(redoQuestionFromCache.getValue(),redoQuestionFromCache);
			}
		}
		// We don't want caller accessing directly to a cached available value of property 'redoQuestion' 
		// so we return a copy
		if (redoQuestionFromCache!=null)
		{
			redoQuestion=new RedoQuestionValue();
			redoQuestion.setFromOtherRedoQuestionValue(redoQuestionFromCache);
		}
		return redoQuestion;
	}
	
	/**
	 * @param value Value string
	 * @return Value of property 'redoQuestion'
	 * @throws ServiceException
	 */
	public RedoQuestionValue getRedoQuestion(String value) throws ServiceException
	{
		return getRedoQuestion(null,value);
	}
	
	/**
	 * @param operation Operation
	 * @param value Value string
	 * @return Value of property 'redoQuestion'
	 * @throws ServiceException
	 */
	public RedoQuestionValue getRedoQuestion(Operation operation,String value) throws ServiceException
	{
		RedoQuestionValue redoQuestion=null;
		RedoQuestionValue redoQuestionFromCache=REDO_QUESTION_VALUES_CACHED_BY_VALUE.get(value);
		if (redoQuestionFromCache==null)
		{
			try
			{
				REDO_QUESTION_VALUES_DAO.setOperation(operation);
				redoQuestionFromCache=REDO_QUESTION_VALUES_DAO.getRedoQuestion(value);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (redoQuestionFromCache!=null)
			{
				REDO_QUESTION_VALUES_CACHED_BY_ID.put(
					Long.valueOf(redoQuestionFromCache.getId()),redoQuestionFromCache);
				REDO_QUESTION_VALUES_CACHED_BY_VALUE.put(value,redoQuestionFromCache);
			}
		}
		// We don't want caller accessing directly to a cached available value of property 'redoQuestion' 
		// so we return a copy
		if (redoQuestionFromCache!=null)
		{
			redoQuestion=new RedoQuestionValue();
			redoQuestion.setFromOtherRedoQuestionValue(redoQuestionFromCache);
		}
		return redoQuestion;
	}
	
	/**
	 * @return List of all values for property 'redoQuestion'
	 * @throws ServiceException
	 */
	public List<RedoQuestionValue> getRedoQuestions() throws ServiceException
	{
		return getRedoQuestions(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all values for property 'redoQuestion'
	 * @throws ServiceException
	 */
	public List<RedoQuestionValue> getRedoQuestions(Operation operation) throws ServiceException
	{
		List<RedoQuestionValue> redoQuestions=new ArrayList<RedoQuestionValue>();
		if (REDO_QUESTION_VALUES_CACHED.isEmpty())
		{
			try
			{
				REDO_QUESTION_VALUES_DAO.setOperation(operation);
				for (RedoQuestionValue redoQuestion:REDO_QUESTION_VALUES_DAO.getRedoQuestions())
				{
					Long redoQuestionId=Long.valueOf(redoQuestion.getId());
					if (REDO_QUESTION_VALUES_CACHED_BY_ID.containsKey(redoQuestionId))
					{
						redoQuestion=REDO_QUESTION_VALUES_CACHED_BY_ID.get(redoQuestionId);
					}
					else
					{
						REDO_QUESTION_VALUES_CACHED_BY_ID.put(redoQuestionId,redoQuestion);
						REDO_QUESTION_VALUES_CACHED_BY_VALUE.put(redoQuestion.getValue(),redoQuestion);
					}
					REDO_QUESTION_VALUES_CACHED.add(redoQuestion);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached available values of property 'redoQuestion' 
		// so we return copies of all them
		for (RedoQuestionValue redoQuestionFromCache:REDO_QUESTION_VALUES_CACHED)
		{
			RedoQuestionValue redoQuestion=null;
			if (redoQuestionFromCache!=null)
			{
				redoQuestion=new RedoQuestionValue();
				redoQuestion.setFromOtherRedoQuestionValue(redoQuestionFromCache);
			}
			redoQuestions.add(redoQuestion);
		}
		return redoQuestions;
	}
	
	/**
	 * Reset cached values of property 'redoQuestion'.
	 */
	public void resetCachedRedoQuestions()
	{
		REDO_QUESTION_VALUES_CACHED_BY_ID.clear();
		REDO_QUESTION_VALUES_CACHED_BY_VALUE.clear();
		REDO_QUESTION_VALUES_CACHED.clear();
	}
}
