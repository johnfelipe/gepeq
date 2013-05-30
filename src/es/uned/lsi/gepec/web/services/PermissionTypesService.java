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
import es.uned.lsi.gepec.model.dao.PermissionTypesDao;
import es.uned.lsi.gepec.model.entities.PermissionType;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages permission types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class PermissionTypesService implements Serializable
{
	private final static PermissionTypesDao PERMISSION_TYPES_DAO=new PermissionTypesDao();
	
	private final static Map<Long,PermissionType> PERMISSION_TYPES_CACHED_BY_ID=new HashMap<Long,PermissionType>();
	private final static Map<String,PermissionType> PERMISSION_TYPES_CACHED_BY_TYPE=
		new HashMap<String,PermissionType>();
	
	public PermissionTypesService()
	{
	}
	
	/**
	 * @param id Permission type identifier
	 * @return Permission type
	 * @throws ServiceException
	 */
	public PermissionType getPermissionType(long id) throws ServiceException
	{
		return getPermissionType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Permission type identifier
	 * @return Permission type
	 * @throws ServiceException
	 */
	public PermissionType getPermissionType(Operation operation,long id) throws ServiceException
	{
		PermissionType permissionType=null;
		PermissionType permissionTypeFromCache=PERMISSION_TYPES_CACHED_BY_ID.get(Long.valueOf(id));
		if (permissionTypeFromCache==null)
		{
			try
			{
				PERMISSION_TYPES_DAO.setOperation(operation);
				permissionTypeFromCache=PERMISSION_TYPES_DAO.getPermissionType(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (permissionTypeFromCache!=null)
			{
				PERMISSION_TYPES_CACHED_BY_ID.put(Long.valueOf(id),permissionTypeFromCache);
				PERMISSION_TYPES_CACHED_BY_TYPE.put(permissionTypeFromCache.getType(),permissionTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached permission type so we return a copy
		if (permissionTypeFromCache!=null)
		{
			permissionType=new PermissionType();
			permissionType.setFromOtherPermissionType(permissionTypeFromCache);
		}
		return permissionType;
	}
	
	/**
	 * @param type Permission type string
	 * @return Permission type
	 * @throws ServiceException
	 */
	public PermissionType getPermissionType(String type) throws ServiceException
	{
		return getPermissionType(null,type);
	}
	
	/**
	 * @param type Permission type string
	 * @return Permission type
	 * @throws ServiceException
	 */
	public PermissionType getPermissionType(Operation operation,String type) throws ServiceException
	{
		PermissionType permissionType=null;
		PermissionType permissionTypeFromCache=PERMISSION_TYPES_CACHED_BY_TYPE.get(type);
		if (permissionTypeFromCache==null)
		{
			try
			{
				PERMISSION_TYPES_DAO.setOperation(operation);
				permissionTypeFromCache=PERMISSION_TYPES_DAO.getPermissionType(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (permissionTypeFromCache!=null)
			{
				PERMISSION_TYPES_CACHED_BY_ID.put(
					Long.valueOf(permissionTypeFromCache.getId()),permissionTypeFromCache);
				PERMISSION_TYPES_CACHED_BY_TYPE.put(type,permissionTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached permission type so we return a copy
		if (permissionTypeFromCache!=null)
		{
			permissionType=new PermissionType();
			permissionType.setFromOtherPermissionType(permissionTypeFromCache);
		}
		return permissionType;
	}
	
	/**
	 * Reset cached permission types.
	 */
	public void resetCachedPermissionTypes()
	{
		PERMISSION_TYPES_CACHED_BY_ID.clear();
		PERMISSION_TYPES_CACHED_BY_TYPE.clear();
	}
}
