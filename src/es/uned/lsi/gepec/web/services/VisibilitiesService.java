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
import es.uned.lsi.gepec.model.dao.VisibilitiesDao;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages category visibilities.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class VisibilitiesService implements Serializable
{
	private final static VisibilitiesDao VISIBILITIES_DAO=new VisibilitiesDao();
	
	private final static Map<Long,Visibility> VISIBILITIES_CACHED_BY_ID=new HashMap<Long,Visibility>();
	private final static Map<String,Visibility> VISIBILITIES_CACHED_BY_VISIBILITY=new HashMap<String,Visibility>();
	private final static List<Visibility> VISIBILITIES_CACHED=new ArrayList<Visibility>();
	
	public VisibilitiesService()
	{
	}
	
	/**
	 * @param id Category visibility identifier
	 * @return Category visibility
	 * @throws ServiceException
	 */
	public Visibility getVisibility(long id) throws ServiceException
	{
		return getVisibility(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Category visibility identifier
	 * @return Category visibility
	 * @throws ServiceException
	 */
	public Visibility getVisibility(Operation operation,long id) throws ServiceException
	{
		Visibility visibility=null;
		Visibility visibilityFromCache=VISIBILITIES_CACHED_BY_ID.get(Long.valueOf(id));
		if (visibilityFromCache==null)
		{
			try
			{
				VISIBILITIES_DAO.setOperation(operation);
				visibilityFromCache=VISIBILITIES_DAO.getVisibility(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (visibilityFromCache!=null)
			{
				VISIBILITIES_CACHED_BY_ID.put(Long.valueOf(id),visibilityFromCache);
				VISIBILITIES_CACHED_BY_VISIBILITY.put(visibilityFromCache.getVisibility(),visibilityFromCache);
			}
		}
		// We don't want caller accessing directly to a cached visibility so we return a copy
		if (visibilityFromCache!=null)
		{
			visibility=visibilityFromCache.getVisibilityCopy();
		}
		return visibility;
	}
	
	/**
	 * @param visibilityStr Category visibility string
	 * @return Category visibility
	 * @throws ServiceException
	 */
	public Visibility getVisibility(String visibilityStr) throws ServiceException
	{
		return getVisibility(null,visibilityStr);
	}
	
	/**
	 * @param operation Operation
	 * @param visibilityStr Category visibility string
	 * @return Category visibility
	 * @throws ServiceException
	 */
	public Visibility getVisibility(Operation operation,String visibilityStr) throws ServiceException
	{
		Visibility visibility=null;
		Visibility visibilityFromCache=VISIBILITIES_CACHED_BY_VISIBILITY.get(visibilityStr);
		if (visibilityFromCache==null)
		{
			try
			{
				VISIBILITIES_DAO.setOperation(operation);
				visibilityFromCache=VISIBILITIES_DAO.getVisibility(visibilityStr);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (visibilityFromCache!=null)
			{
				VISIBILITIES_CACHED_BY_ID.put(Long.valueOf(visibilityFromCache.getId()),visibilityFromCache);
				VISIBILITIES_CACHED_BY_VISIBILITY.put(visibilityStr,visibilityFromCache);
			}
		}
		// We don't want caller accessing directly to a cached visibility so we return a copy
		if (visibilityFromCache!=null)
		{
			visibility=visibilityFromCache.getVisibilityCopy();
		}
		return visibility;
	}
	
	/**
	 * @param categoryId Category identifier
	 * @return Visibility from a category
	 * @throws ServiceException
	 */
	public Visibility getVisibilityFromCategoryId(long categoryId) throws ServiceException
	{
		return getVisibilityFromCategoryId(null,categoryId);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @return Visibility from a category
	 * @throws ServiceException
	 */
	public Visibility getVisibilityFromCategoryId(Operation operation,long categoryId) throws ServiceException
	{
		Visibility visibility=null;
		if (categoryId>0L)
		{
			Visibility visibilityFromDB=null;
			try
			{
				VISIBILITIES_DAO.setOperation(operation);
				visibilityFromDB=VISIBILITIES_DAO.getVisibilityFromCategoryId(categoryId);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (visibilityFromDB!=null)
			{
				visibility=visibilityFromDB.getVisibilityCopy();
			}
		}
		return visibility;
	}
	
	/**
	 * @return List of all category visibilities
	 * @throws ServiceException
	 */
	public List<Visibility> getVisibilities() throws ServiceException
	{
		return getVisibilities(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all category visibilities
	 * @throws ServiceException
	 */
	public List<Visibility> getVisibilities(Operation operation) throws ServiceException
	{
		List<Visibility> visibilities=new ArrayList<Visibility>();
		if (VISIBILITIES_CACHED.isEmpty())
		{
			try
			{
				VISIBILITIES_DAO.setOperation(operation);
				for (Visibility visibility:VISIBILITIES_DAO.getVisibilities())
				{
					Long visibilityId=Long.valueOf(visibility.getId());
					if (VISIBILITIES_CACHED_BY_ID.containsKey(visibilityId))
					{
						visibility=VISIBILITIES_CACHED_BY_ID.get(visibilityId);
					}
					else
					{
						VISIBILITIES_CACHED_BY_ID.put(visibilityId,visibility);
						VISIBILITIES_CACHED_BY_VISIBILITY.put(visibility.getVisibility(),visibility);
					}
					VISIBILITIES_CACHED.add(visibility);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached visibilities so we return copies of all them
		for (Visibility visibilityFromCache:VISIBILITIES_CACHED)
		{
			Visibility visibility=null;
			if (visibilityFromCache!=null)
			{
				visibility=visibilityFromCache.getVisibilityCopy();
			}
			visibilities.add(visibility);
		}
		return visibilities;
	}
	
	/**
	 * Reset cached category visibilities.
	 */
	public void resetCachedVisibilities()
	{
		VISIBILITIES_CACHED_BY_ID.clear();
		VISIBILITIES_CACHED_BY_VISIBILITY.clear();
		VISIBILITIES_CACHED.clear();
	}
}
