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
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.ScoreUnitsDao;
import es.uned.lsi.gepec.model.entities.ScoreUnit;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages score units.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class ScoreUnitsService implements Serializable
{
	private final static ScoreUnitsDao SCORE_UNITS_DAO=new ScoreUnitsDao();
	
	private final static Map<Long,ScoreUnit> SCORE_UNITS_CACHED_BY_ID=new HashMap<Long,ScoreUnit>();
	private final static Map<String,ScoreUnit> SCORE_UNITS_CACHED_BY_UNIT=new HashMap<String,ScoreUnit>();
	
	public ScoreUnitsService()
	{
	}
	
	/**
	 * @param id Score unit identifier
	 * @return Score unit
	 * @throws ServiceException
	 */
	public ScoreUnit getScoreUnit(long id) throws ServiceException
	{
		return getScoreUnit(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Score unit identifier
	 * @return Score unit
	 * @throws ServiceException
	 */
	public ScoreUnit getScoreUnit(Operation operation,long id) throws ServiceException
	{
		ScoreUnit scoreUnit=null;
		ScoreUnit scoreUnitFromCache=SCORE_UNITS_CACHED_BY_ID.get(Long.valueOf(id));
		if (scoreUnitFromCache==null)
		{
			try
			{
				SCORE_UNITS_DAO.setOperation(operation);
				scoreUnitFromCache=SCORE_UNITS_DAO.getScoreUnit(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (scoreUnitFromCache!=null)
			{
				SCORE_UNITS_CACHED_BY_ID.put(Long.valueOf(id),scoreUnitFromCache);
				SCORE_UNITS_CACHED_BY_UNIT.put(scoreUnitFromCache.getUnit(),scoreUnitFromCache);
			}
		}
		// We don't want caller accessing directly to a cached score unit so we return a copy
		if (scoreUnitFromCache!=null)
		{
			scoreUnit=new ScoreUnit();
			scoreUnit.setFromOtherScoreUnit(scoreUnitFromCache);
		}
		return scoreUnit;
	}
	
	/**
	 * @param unit Score unit string
	 * @return Score unit
	 * @throws ServiceException
	 */
	public ScoreUnit getScoreUnit(String unit) throws ServiceException
	{
		return getScoreUnit(null,unit);
	}
	
	/**
	 * @param operation Operation
	 * @param unit Score unit string
	 * @return Score unit
	 * @throws ServiceException
	 */
	public ScoreUnit getScoreUnit(Operation operation,String unit) throws ServiceException
	{
		ScoreUnit scoreUnit=null;
		ScoreUnit scoreUnitFromCache=SCORE_UNITS_CACHED_BY_UNIT.get(unit);
		if (scoreUnitFromCache==null)
		{
			try
			{
				SCORE_UNITS_DAO.setOperation(operation);
				scoreUnitFromCache=SCORE_UNITS_DAO.getScoreUnit(unit);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (scoreUnitFromCache!=null)
			{
				SCORE_UNITS_CACHED_BY_ID.put(Long.valueOf(scoreUnitFromCache.getId()),scoreUnitFromCache);
				SCORE_UNITS_CACHED_BY_UNIT.put(unit,scoreUnitFromCache);
			}
		}
		// We don't want caller accessing directly to a cached score unit so we return a copy
		if (scoreUnitFromCache!=null)
		{
			scoreUnit=new ScoreUnit();
			scoreUnit.setFromOtherScoreUnit(scoreUnitFromCache);
		}
		return scoreUnit;
	}
	
	/**
	 * Reset cached score units.
	 */
	public void resetCachedScoreUnits()
	{
		SCORE_UNITS_CACHED_BY_ID.clear();
		SCORE_UNITS_CACHED_BY_UNIT.clear();
	}
}
