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

import es.uned.lsi.gepec.model.dao.AssessementsDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Assessement;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages assessements.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class AssessementsService implements Serializable
{
	private final static AssessementsDao ASSESSEMENTS_DAO=new AssessementsDao();
	
	private final static Map<Long,Assessement> ASSESSEMENTS_CACHED_BY_ID=new HashMap<Long,Assessement>();
	private final static Map<String,Assessement> ASSESSEMENTS_CACHED_BY_TYPE=new HashMap<String, Assessement>();
	private final static List<Assessement> ASSESSEMENTS_CACHED=new ArrayList<Assessement>();
	
	public AssessementsService()
	{
	}
	
	/**
	 * @param id Assessement identifier
	 * @return Assessement
	 * @throws ServiceException
	 */
	public Assessement getAssessement(long id) throws ServiceException
	{
		return getAssessement(null,id);
	}
		
	/**
	 * @param operation Operation
	 * @param id Assessement identifier
	 * @return Assessement
	 * @throws ServiceException
	 */
	public Assessement getAssessement(Operation operation,long id) throws ServiceException
	{
		Assessement assessement=null;
		Assessement assessementFromCache=ASSESSEMENTS_CACHED_BY_ID.get(Long.valueOf(id));
		if (assessementFromCache==null)
		{
			try
			{
				ASSESSEMENTS_DAO.setOperation(operation);
				assessementFromCache=ASSESSEMENTS_DAO.getAssessement(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (assessementFromCache!=null)
			{
				ASSESSEMENTS_CACHED_BY_ID.put(Long.valueOf(id),assessementFromCache);
				ASSESSEMENTS_CACHED_BY_TYPE.put(assessementFromCache.getType(),assessementFromCache);
			}
		}
		// We don't want caller accessing directly to a cached assessement so we return a copy
		if (assessementFromCache!=null)
		{
			assessement=assessementFromCache.getAssessementCopy();
		}
		return assessement;
	}
	
	/**
	 * @param type Assesement type
	 * @return Assessement
	 * @throws ServiceException
	 */
	public Assessement getAssessement(String type) throws ServiceException
	{
		return getAssessement(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Assesement type
	 * @return Assessement
	 * @throws ServiceException
	 */
	public Assessement getAssessement(Operation operation,String type) throws ServiceException
	{
		Assessement assessement=null;
		Assessement assessementFromCache=ASSESSEMENTS_CACHED_BY_TYPE.get(type);
		if (assessementFromCache==null)
		{
			try
			{
				ASSESSEMENTS_DAO.setOperation(operation);
				assessementFromCache=ASSESSEMENTS_DAO.getAssessement(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (assessementFromCache!=null)
			{
				ASSESSEMENTS_CACHED_BY_ID.put(Long.valueOf(assessementFromCache.getId()),assessementFromCache);
				ASSESSEMENTS_CACHED_BY_TYPE.put(type,assessementFromCache);
			}
		}
		// We don't want caller accessing directly to a cached assessement so we return a copy
		if (assessementFromCache!=null)
		{
			assessement=new Assessement();
			assessement.setFromOtherAssessement(assessementFromCache);
		}
		return assessement;
	}
	
	/**
	 * @return List of all assessements
	 * @throws ServiceException
	 */
	public List<Assessement> getAssessements()
	{
		return getAssessements(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all assessements
	 * @throws ServiceException
	 */
	public List<Assessement> getAssessements(Operation operation)
	{
		List<Assessement> assessements=new ArrayList<Assessement>();
		if (ASSESSEMENTS_CACHED.isEmpty())
		{
			try
			{
				ASSESSEMENTS_DAO.setOperation(operation);
				for (Assessement assessement:ASSESSEMENTS_DAO.getAssessements())
				{
					Long assessementId=Long.valueOf(assessement.getId());
					if (ASSESSEMENTS_CACHED_BY_ID.containsKey(assessementId))
					{
						assessement=ASSESSEMENTS_CACHED_BY_ID.get(assessementId);
					}
					else
					{
						ASSESSEMENTS_CACHED_BY_ID.put(assessementId,assessement);
						ASSESSEMENTS_CACHED_BY_TYPE.put(assessement.getType(),assessement);
					}
					ASSESSEMENTS_CACHED.add(assessement);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached assessements so we return copies of all them
		for (Assessement assessementFromCache:ASSESSEMENTS_CACHED)
		{
			Assessement assessement=null;
			if (assessementFromCache!=null)
			{
				assessement=assessementFromCache.getAssessementCopy();
			}
			assessements.add(assessement);
		}
		return assessements;
	}
	
	/**
	 * Reset cached assessements.
	 */
	public void resetCachedAssessements()
	{
		ASSESSEMENTS_CACHED_BY_ID.clear();
		ASSESSEMENTS_CACHED_BY_TYPE.clear();
		ASSESSEMENTS_CACHED.clear();
	}
}
