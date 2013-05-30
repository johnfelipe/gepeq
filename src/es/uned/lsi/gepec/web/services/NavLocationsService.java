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
import es.uned.lsi.gepec.model.dao.NavLocationsDao;
import es.uned.lsi.gepec.model.entities.NavLocation;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages navigation locations.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class NavLocationsService implements Serializable
{
	private final static NavLocationsDao NAV_LOCATIONS_DAO=new NavLocationsDao();
	
	private final static Map<Long,NavLocation> NAV_LOCATIONS_CACHED_BY_ID=new HashMap<Long,NavLocation>();
	private final static Map<String,NavLocation> NAV_LOCATIONS_CACHED_BY_LOCATION=
		new HashMap<String,NavLocation>();
	private final static List<NavLocation> NAV_LOCATIONS_CACHED=new ArrayList<NavLocation>();
	
	public NavLocationsService()
	{
	}
	
	/**
	 * @param id Navigation location identifier
	 * @return Navigation location
	 * @throws ServiceException
	 */
	public NavLocation getNavLocation(long id) throws ServiceException
	{
		return getNavLocation(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Navigation location identifier
	 * @return Navigation location
	 * @throws ServiceException
	 */
	public NavLocation getNavLocation(Operation operation,long id) throws ServiceException
	{
		NavLocation navLocation=null;
		NavLocation navLocationFromCache=NAV_LOCATIONS_CACHED_BY_ID.get(Long.valueOf(id));
		if (navLocationFromCache==null)
		{
			try
			{
				NAV_LOCATIONS_DAO.setOperation(operation);
				navLocationFromCache=NAV_LOCATIONS_DAO.getNavLocation(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (navLocationFromCache!=null)
			{
				NAV_LOCATIONS_CACHED_BY_ID.put(Long.valueOf(id),navLocationFromCache);
				NAV_LOCATIONS_CACHED_BY_LOCATION.put(navLocationFromCache.getLocation(),navLocationFromCache);
			}
		}
		// We don't want caller accessing directly to a cached navigation location so we return a copy
		if (navLocationFromCache!=null)
		{
			navLocation=new NavLocation();
			navLocation.setFromOtherNavLocation(navLocationFromCache);
		}
		return navLocation;
	}
	
	/**
	 * @param location Location string
	 * @return Navigation location
	 * @throws ServiceException
	 */
	public NavLocation getNavLocation(String location) throws ServiceException
	{
		return getNavLocation(null,location);
	}
	
	/**
	 * @param operation Operation
	 * @param location Location string
	 * @return Navigation location
	 * @throws ServiceException
	 */
	public NavLocation getNavLocation(Operation operation,String location) throws ServiceException
	{
		NavLocation navLocation=null;
		NavLocation navLocationFromCache=NAV_LOCATIONS_CACHED_BY_LOCATION.get(location);
		if (navLocationFromCache==null)
		{
			try
			{
				NAV_LOCATIONS_DAO.setOperation(operation);
				navLocationFromCache=NAV_LOCATIONS_DAO.getNavLocation(location);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (navLocationFromCache!=null)
			{
				NAV_LOCATIONS_CACHED_BY_ID.put(Long.valueOf(navLocationFromCache.getId()),navLocationFromCache);
				NAV_LOCATIONS_CACHED_BY_LOCATION.put(location,navLocationFromCache);
			}
		}
		// We don't want caller accessing directly to a cached navigation location so we return a copy
		if (navLocationFromCache!=null)
		{
			navLocation=new NavLocation();
			navLocation.setFromOtherNavLocation(navLocationFromCache);
		}
		return navLocation;
	}
	
	/**
	 * @return List of all navigation locations
	 * @throws ServiceException
	 */
	public List<NavLocation> getNavLocations() throws ServiceException
	{
		return getNavLocations(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all navigation locations
	 * @throws ServiceException
	 */
	public List<NavLocation> getNavLocations(Operation operation)
	{
		List<NavLocation> navLocations=new ArrayList<NavLocation>();
		if (NAV_LOCATIONS_CACHED.isEmpty())
		{
			try
			{
				NAV_LOCATIONS_DAO.setOperation(operation);
				for (NavLocation navLocation:NAV_LOCATIONS_DAO.getNavLocations())
				{
					Long navLocationId=Long.valueOf(navLocation.getId());
					if (NAV_LOCATIONS_CACHED_BY_ID.containsKey(navLocationId))
					{
						navLocation=NAV_LOCATIONS_CACHED_BY_ID.get(navLocationId);
					}
					else
					{
						NAV_LOCATIONS_CACHED_BY_ID.put(navLocationId,navLocation);
						NAV_LOCATIONS_CACHED_BY_LOCATION.put(navLocation.getLocation(),navLocation);
					}
					NAV_LOCATIONS_CACHED.add(navLocation);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached navigation locations so we return copies of all them
		for (NavLocation navLocationFromCache:NAV_LOCATIONS_CACHED)
		{
			NavLocation navLocation=null;
			if (navLocationFromCache!=null)
			{
				navLocation=new NavLocation();
				navLocation.setFromOtherNavLocation(navLocationFromCache);
			}
			navLocations.add(navLocation);
		}
		return navLocations;
	}
	
	/**
	 * Reset cached navigation locations.
	 */
	public void resetCachedNavLocations()
	{
		NAV_LOCATIONS_CACHED_BY_ID.clear();
		NAV_LOCATIONS_CACHED_BY_LOCATION.clear();
		NAV_LOCATIONS_CACHED.clear();
	}
}
