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

import es.uned.lsi.gepec.model.dao.AddressTypesDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.AddressType;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages address types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class AddressTypesService implements Serializable
{
	private final static AddressTypesDao ADDRESS_TYPES_DAO=new AddressTypesDao();
	
	private final static Map<Long,AddressType> ADDRESS_TYPES_CACHED_BY_ID=new HashMap<Long,AddressType>();
	private final static Map<String,Map<String,AddressType>> ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE=
		new HashMap<String,Map<String,AddressType>>();
	private final static Map<String,List<AddressType>> ADDRESS_TYPES_CACHED_BY_TYPE=
		new HashMap<String,List<AddressType>>();
	private final static List<AddressType> ADDRESS_TYPES_CACHED=new ArrayList<AddressType>();
	
	public AddressTypesService()
	{
	}
	
	/**
	 * @param id Address type identifier
	 * @return Address type
	 * @throws ServiceException
	 */
	public AddressType getAddressType(long id) throws ServiceException
	{
		return getAddressType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Address type identifier
	 * @return Address type
	 * @throws ServiceException
	 */
	public AddressType getAddressType(Operation operation,long id) throws ServiceException
	{
		AddressType addressType=null;
		AddressType addressTypeFromCache=ADDRESS_TYPES_CACHED_BY_ID.get(Long.valueOf(id));
		if (addressTypeFromCache==null)
		{
			try
			{
				ADDRESS_TYPES_DAO.setOperation(operation);
				addressTypeFromCache=ADDRESS_TYPES_DAO.getAddressType(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (addressTypeFromCache!=null)
			{
				ADDRESS_TYPES_CACHED_BY_ID.put(Long.valueOf(id),addressTypeFromCache);
				Map<String,AddressType> addressTypesCachedBySubtype=
					ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.get(addressTypeFromCache.getType());
				if (addressTypesCachedBySubtype==null)
				{
					addressTypesCachedBySubtype=new HashMap<String,AddressType>();
					ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.put(
						addressTypeFromCache.getType(),addressTypesCachedBySubtype);
				}
				addressTypesCachedBySubtype.put(addressTypeFromCache.getSubtype(),addressTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached address type so we return a copy
		if (addressTypeFromCache!=null)
		{
			addressType=new AddressType();
			addressType.setFromOtherAddressType(addressTypeFromCache);
		}
		return addressType;
	}
	
	/**
	 * @param type Address type's type (string)
	 * @param subtype Address type's subtype (string)
	 * @return Address type
	 * @throws ServiceException
	 */
	public AddressType getAddressType(String type,String subtype) throws ServiceException
	{
		return getAddressType(null,type,subtype);
	}
	
	/**
	 * @param operation Operation
	 * @param type Address type's type (string)
	 * @param subtype Address type's subtype (string)
	 * @return Address type
	 * @throws ServiceException
	 */
	public AddressType getAddressType(Operation operation,String type,String subtype) throws ServiceException
	{
		AddressType addressType=null;
		AddressType addressTypeFromCache=null;
		Map<String,AddressType> addressTypesCachedBySubtype=ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.get(type);
		if (addressTypesCachedBySubtype!=null)
		{
			addressTypeFromCache=addressTypesCachedBySubtype.get(subtype);
		}
		if (addressTypeFromCache==null)
		{
			try
			{
				ADDRESS_TYPES_DAO.setOperation(operation);
				addressTypeFromCache=ADDRESS_TYPES_DAO.getAddressType(type,subtype);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (addressTypeFromCache!=null)
			{
				ADDRESS_TYPES_CACHED_BY_ID.put(Long.valueOf(addressTypeFromCache.getId()),addressTypeFromCache);
				if (addressTypesCachedBySubtype==null)
				{
					addressTypesCachedBySubtype=new HashMap<String,AddressType>();
					ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.put(type,addressTypesCachedBySubtype);
				}
				addressTypesCachedBySubtype.put(subtype,addressTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached address type so we return a copy
		if (addressTypeFromCache!=null)
		{
			addressType=new AddressType();
			addressType.setFromOtherAddressType(addressTypeFromCache);
		}
		return addressType;
	}
	
	/**
	 * @return List of all address types
	 * @throws ServiceException
	 */
	public List<AddressType> getAddressTypes() throws ServiceException
	{
		return getAddressTypes((Operation)null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all address types
	 * @throws ServiceException
	 */
	public List<AddressType> getAddressTypes(Operation operation) throws ServiceException
	{
		List<AddressType> addressTypes=new ArrayList<AddressType>();
		if (ADDRESS_TYPES_CACHED.isEmpty())
		{
			try
			{
				ADDRESS_TYPES_DAO.setOperation(operation);
				for (AddressType addressType:ADDRESS_TYPES_DAO.getAddressTypes())
				{
					Long addressTypeId=Long.valueOf(addressType.getId());
					if (ADDRESS_TYPES_CACHED_BY_ID.containsKey(addressTypeId))
					{
						addressType=ADDRESS_TYPES_CACHED_BY_ID.get(addressTypeId);
					}
					else
					{
						ADDRESS_TYPES_CACHED_BY_ID.put(addressTypeId,addressType);
						Map<String,AddressType> addressTypesCachedBySubtype=
							ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.get(addressType.getType());
						if (addressTypesCachedBySubtype==null)
						{
							addressTypesCachedBySubtype=new HashMap<String,AddressType>();
							ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.put(
								addressType.getType(),addressTypesCachedBySubtype);
						}
						addressTypesCachedBySubtype.put(addressType.getSubtype(),addressType);
					}
					List<AddressType> addressTypesCachedWithType=
						ADDRESS_TYPES_CACHED_BY_TYPE.get(addressType.getType());
					if (addressTypesCachedWithType==null)
					{
						addressTypesCachedWithType=new ArrayList<AddressType>();
						ADDRESS_TYPES_CACHED_BY_TYPE.put(addressType.getType(),addressTypesCachedWithType);
					}
					if (!addressTypesCachedWithType.contains(addressType))
					{
						addressTypesCachedWithType.add(addressType);
					}
					ADDRESS_TYPES_CACHED.add(addressType);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		// We don't want caller accessing directly to cached address types so we return copies of all them
		for (AddressType addressTypeFromCache:ADDRESS_TYPES_CACHED)
		{
			AddressType addressType=null;
			if (addressTypeFromCache!=null)
			{
				addressType=new AddressType();
				addressType.setFromOtherAddressType(addressTypeFromCache);
			}
			addressTypes.add(addressType);
		}
		return addressTypes;
	}
	
	/**
	 * @param type Address type's type (string)
	 * @return List of address types filtered by type
	 * @throws ServiceException
	 */
	public List<AddressType> getAddressTypes(String type) throws ServiceException
	{
		return getAddressTypes(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Address type's type (string)
	 * @return List of address types filtered by type
	 * @throws ServiceException
	 */
	public List<AddressType> getAddressTypes(Operation operation,String type) throws ServiceException
	{
		List<AddressType> addressTypes=new ArrayList<AddressType>();
		List<AddressType> addressTypesCachedWithType=ADDRESS_TYPES_CACHED_BY_TYPE.get(type);
		if (addressTypesCachedWithType==null)
		{
			List<AddressType> addressTypesFromDAO=null;
			try
			{
				ADDRESS_TYPES_DAO.setOperation(operation);
				addressTypesFromDAO=ADDRESS_TYPES_DAO.getAddressTypes(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (!addressTypesFromDAO.isEmpty())
			{
				addressTypesCachedWithType=new ArrayList<AddressType>();
				for (AddressType addressType:addressTypesFromDAO)
				{
					Long addressTypeId=Long.valueOf(addressType.getId());
					if (ADDRESS_TYPES_CACHED_BY_ID.containsKey(addressTypeId))
					{
						addressType=ADDRESS_TYPES_CACHED_BY_ID.get(addressTypeId);
					}
					else
					{
						ADDRESS_TYPES_CACHED_BY_ID.put(addressTypeId,addressType);
						Map<String, AddressType> addressTypesCachedBySubtype=
							ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.get(type);
						if (addressTypesCachedBySubtype==null)
						{
							addressTypesCachedBySubtype=new HashMap<String,AddressType>();
							ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.put(type,addressTypesCachedBySubtype);
						}
						addressTypesCachedBySubtype.put(addressType.getSubtype(),addressType);
					}
					addressTypesCachedWithType.add(addressType);
				}
				ADDRESS_TYPES_CACHED_BY_TYPE.put(type,addressTypesCachedWithType);
			}
		}
		// We don't want caller accessing directly to cached address types so we return copies of all them
		if (addressTypesCachedWithType!=null)
		{
			for (AddressType addressTypeFromCache:addressTypesCachedWithType)
			{
				AddressType addressType=null;
				if (addressTypeFromCache!=null)
				{
					addressType=new AddressType();
					addressType.setFromOtherAddressType(addressTypeFromCache);
				}
				addressTypes.add(addressType);
			}
		}
		return addressTypes;
	}
	
	/**
	 * Reset cached address types.
	 */
	public void resetCachedAddressTypes()
	{
		ADDRESS_TYPES_CACHED_BY_ID.clear();
		ADDRESS_TYPES_CACHED_BY_TYPE_AND_SUBTYPE.clear();
		ADDRESS_TYPES_CACHED_BY_TYPE.clear();
		ADDRESS_TYPES_CACHED.clear();
	}
}
