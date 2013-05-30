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
import es.uned.lsi.gepec.model.dao.ScoreTypesDao;
import es.uned.lsi.gepec.model.entities.ScoreType;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages score types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class ScoreTypesService implements Serializable
{
	private final static ScoreTypesDao SCORE_TYPES_DAO=new ScoreTypesDao();
	
	private final static Map<Long,ScoreType> SCORE_TYPES_CACHED_BY_ID=new HashMap<Long,ScoreType>();
	private final static Map<String,ScoreType> SCORE_TYPES_CACHED_BY_TYPE=new HashMap<String,ScoreType>();
	private final static List<ScoreType> SCORE_TYPES_CACHED=new ArrayList<ScoreType>();
	
	public ScoreTypesService()
	{
	}
	
	/**
	 * @param id Score type identifier
	 * @return Score type
	 * @throws ServiceException
	 */
	public ScoreType getScoreType(long id) throws ServiceException
	{
		return getScoreType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Score type identifier
	 * @return Score type
	 * @throws ServiceException
	 */
	public ScoreType getScoreType(Operation operation,long id) throws ServiceException
	{
		ScoreType scoreType=null;
		ScoreType scoreTypeFromCache=SCORE_TYPES_CACHED_BY_ID.get(Long.valueOf(id));
		if (scoreTypeFromCache==null)
		{
			try
			{
				SCORE_TYPES_DAO.setOperation(operation);
				scoreTypeFromCache=SCORE_TYPES_DAO.getScoreType(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (scoreTypeFromCache!=null)
			{
				SCORE_TYPES_CACHED_BY_ID.put(Long.valueOf(id),scoreTypeFromCache);
				SCORE_TYPES_CACHED_BY_TYPE.put(scoreTypeFromCache.getType(),scoreTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached score type so we return a copy
		if (scoreTypeFromCache!=null)
		{
			scoreType=new ScoreType();
			scoreType.setFromOtherScoreType(scoreTypeFromCache);
		}
		return scoreType;
	}
	
	/**
	 * @param type Score type string
	 * @return Score type
	 * @throws ServiceException
	 */
	public ScoreType getScoreType(String type) throws ServiceException
	{
		return getScoreType(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Score type string
	 * @return Score type
	 * @throws ServiceException
	 */
	public ScoreType getScoreType(Operation operation,String type) throws ServiceException
	{
		ScoreType scoreType=null;
		ScoreType scoreTypeFromCache=SCORE_TYPES_CACHED_BY_TYPE.get(type);
		if (scoreTypeFromCache==null)
		{
			try
			{
				SCORE_TYPES_DAO.setOperation(operation);
				scoreTypeFromCache=SCORE_TYPES_DAO.getScoreType(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (scoreTypeFromCache!=null)
			{
				SCORE_TYPES_CACHED_BY_ID.put(Long.valueOf(scoreTypeFromCache.getId()),scoreTypeFromCache);
				SCORE_TYPES_CACHED_BY_TYPE.put(type,scoreTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached score type so we return a copy
		if (scoreTypeFromCache!=null)
		{
			scoreType=new ScoreType();
			scoreType.setFromOtherScoreType(scoreTypeFromCache);
		}
		return scoreType;
	}
	
	/**
	 * @return List of all score types
	 * @throws ServiceException
	 */
	public List<ScoreType> getScoreTypes() throws ServiceException
	{
		return getScoreTypes(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all score types
	 * @throws ServiceException
	 */
	public List<ScoreType> getScoreTypes(Operation operation) throws ServiceException
	{
		List<ScoreType> scoreTypes=new ArrayList<ScoreType>();
		if (SCORE_TYPES_CACHED.isEmpty())
		{
			try
			{
				SCORE_TYPES_DAO.setOperation(operation);
				for (ScoreType scoreType:SCORE_TYPES_DAO.getScoreTypes())
				{
					Long scoreTypeId=Long.valueOf(scoreType.getId());
					if (SCORE_TYPES_CACHED_BY_ID.containsKey(scoreTypeId))
					{
						scoreType=SCORE_TYPES_CACHED_BY_ID.get(scoreTypeId);
					}
					else
					{
						SCORE_TYPES_CACHED_BY_ID.put(scoreTypeId,scoreType);
						SCORE_TYPES_CACHED_BY_TYPE.put(scoreType.getType(),scoreType);
					}
					SCORE_TYPES_CACHED.add(scoreType);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached score types so we return copies of all them
		for (ScoreType scoreTypeFromCache:SCORE_TYPES_CACHED)
		{
			ScoreType scoreType=null;
			if (scoreTypeFromCache!=null)
			{
				scoreType=new ScoreType();
				scoreType.setFromOtherScoreType(scoreTypeFromCache);
			}
			scoreTypes.add(scoreType);
		}
		return scoreTypes;
	}
	
	/**
	 * Reset cached score types.
	 */
	public void resetCachedScoreTypes()
	{
		SCORE_TYPES_CACHED_BY_ID.clear();
		SCORE_TYPES_CACHED_BY_TYPE.clear();
		SCORE_TYPES_CACHED.clear();
	}
}
