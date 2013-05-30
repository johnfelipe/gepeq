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
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.CopyrightsDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Copyright;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages copyrights.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class CopyrightsService implements Serializable
{
	private final static CopyrightsDao COPYRIGHTS_DAO=new CopyrightsDao();
	
	private final static Map<Long,Copyright> COPYRIGHTS_CACHED_BY_ID=new HashMap<Long,Copyright>();
	private final static Map<String,Copyright> COPYRIGHTS_CACHED_BY_COPYRIGHT=new HashMap<String,Copyright>();
	private final static List<Copyright> COPYRIGHTS_CACHED=new ArrayList<Copyright>();
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	public CopyrightsService()
	{
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/**
	 * @param id Copyright's identifier
	 * @return Copyright
	 * @throws ServiceException
	 */
	public Copyright getCopyright(long id) throws ServiceException
	{
		return getCopyright(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Copyright's identifier
	 * @return Copyright
	 * @throws ServiceException
	 */
	public Copyright getCopyright(Operation operation,long id) throws ServiceException
	{
		Copyright copyright=null;
		Copyright copyrightFromCache=COPYRIGHTS_CACHED_BY_ID.get(Long.valueOf(id));
		if (copyrightFromCache==null)
		{
			try
			{
				COPYRIGHTS_DAO.setOperation(operation);
				copyrightFromCache=COPYRIGHTS_DAO.getCopyright(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (copyrightFromCache!=null)
			{
				COPYRIGHTS_CACHED_BY_ID.put(Long.valueOf(id),copyrightFromCache);
				COPYRIGHTS_CACHED_BY_COPYRIGHT.put(copyrightFromCache.getCopyright(),copyrightFromCache);
			}
		}
		// We don't want caller accessing directly to a cached copyright so we return a copy
		if (copyrightFromCache!=null)
		{
			copyright=new Copyright();
			copyright.setFromOtherCopyright(copyrightFromCache);
		}
		return copyright;
	}
	
	/**
	 * @param copyrightStr Copyright string
	 * @return Copyright
	 * @throws ServiceException
	 */
	public Copyright getCopyright(String copyrightStr) throws ServiceException
	{
		return getCopyright(null,copyrightStr);
	}
	
	/**
	 * @param operation Operation
	 * @param copyrightStr Copyright string
	 * @return Copyright
	 * @throws ServiceException
	 */
	public Copyright getCopyright(Operation operation,String copyrightStr) throws ServiceException
	{
		Copyright copyright=null;
		Copyright copyrightFromCache=COPYRIGHTS_CACHED_BY_COPYRIGHT.get(copyrightStr);
		if (copyrightFromCache==null)
		{
			try
			{
				COPYRIGHTS_DAO.setOperation(operation);
				copyrightFromCache=COPYRIGHTS_DAO.getCopyright(copyrightStr);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (copyrightFromCache!=null)
			{
				COPYRIGHTS_CACHED_BY_ID.put(Long.valueOf(copyrightFromCache.getId()),copyrightFromCache);
				COPYRIGHTS_CACHED_BY_COPYRIGHT.put(copyrightStr,copyrightFromCache);
			}
		}
		// We don't want caller accessing directly to a cached copyright so we return a copy
		if (copyrightFromCache!=null)
		{
			copyright=new Copyright();
			copyright.setFromOtherCopyright(copyrightFromCache);
		}
		return copyright;
	}
	
	/**
	 * @return List of all copyrights
	 * @throws ServiceException
	 */
	public List<Copyright> getCopyrights() throws ServiceException
	{
		return getCopyrights(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all copyrights
	 * @throws ServiceException
	 */
	public List<Copyright> getCopyrights(Operation operation) throws ServiceException
	{
		List<Copyright> copyrights=new ArrayList<Copyright>();
		if (COPYRIGHTS_CACHED.isEmpty())
		{
			try
			{
				COPYRIGHTS_DAO.setOperation(operation);
				for (Copyright copyright:COPYRIGHTS_DAO.getCopyrights())
				{
					Long copyrightId=Long.valueOf(copyright.getId());
					if (COPYRIGHTS_CACHED_BY_ID.containsKey(copyrightId))
					{
						copyright=COPYRIGHTS_CACHED_BY_ID.get(copyrightId);
					}
					else
					{
						COPYRIGHTS_CACHED_BY_ID.put(copyrightId,copyright);
						COPYRIGHTS_CACHED_BY_COPYRIGHT.put(copyright.getCopyright(),copyright);
					}
					COPYRIGHTS_CACHED.add(copyright);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached copyrights so we return copies of all them
		for (Copyright copyrightFromCache:COPYRIGHTS_CACHED)
		{
			Copyright copyright=null;
			if (copyrightFromCache!=null)
			{
				copyright=new Copyright();
				copyright.setFromOtherCopyright(copyrightFromCache);
			}
			copyrights.add(copyright);
		}
		return copyrights;
	}
	
	/**
	 * Reset cached copyrights.
	 */
	public void resetCachedCopyrights()
	{
		COPYRIGHTS_CACHED_BY_ID.clear();
		COPYRIGHTS_CACHED_BY_COPYRIGHT.clear();
		COPYRIGHTS_CACHED.clear();
	}
	
	/**
	 * @param copyrightId Copyright identifier
	 * @return Localized copyright
	 * @throws ServiceException
	 */
	public String getLocalizedCopyright(long copyrightId) throws ServiceException
	{
		return getLocalizedCopyright(getCopyright(null,copyrightId));
	}
	
	/**
	 * @param operation Operation
	 * @param copyrightId Copyright identifier
	 * @return Localized copyright
	 * @throws ServiceException
	 */
	public String getLocalizedCopyright(Operation operation,long copyrightId) throws ServiceException
	{
		return getLocalizedCopyright(getCopyright(operation,copyrightId));
	}
	
	/**
	 * @param copyright Copyright
	 * @return Localized copyright
	 * @throws ServiceException
	 */
	private String getLocalizedCopyright(Copyright copyright) throws ServiceException
	{
		return localizationService.getLocalizedMessage(copyright.getCopyright());
	}
	
	/**
	 * @param copyrightId Copyright identifier
	 * @return Short version of localized copyright
	 * @throws ServiceException
	 */
	public String getLocalizedCopyrightShort(long copyrightId) throws ServiceException
	{
		return getLocalizedCopyrightShort(getCopyright(null,copyrightId));
	}
	
	/**
	 * @param operation Operation
	 * @param copyrightId Copyright identifier
	 * @return Short version of localized copyright
	 * @throws ServiceException
	 */
	public String getLocalizedCopyrightShort(Operation operation,long copyrightId) throws ServiceException
	{
		return getLocalizedCopyrightShort(getCopyright(operation,copyrightId));
	}
	
	/**
	 * @param copyright Copyright
	 * @return Short version of localized copyright
	 * @throws ServiceException
	 */
	private String getLocalizedCopyrightShort(Copyright copyright) throws ServiceException
	{
		StringBuffer copyrightShort=new StringBuffer(copyright.getCopyright());
		copyrightShort.append("_SHORT");
		String localizedCopyrightShort=localizationService.getLocalizedMessage(copyrightShort.toString());
		return localizedCopyrightShort==null?getLocalizedCopyright(copyright):localizedCopyrightShort;
	}
}
