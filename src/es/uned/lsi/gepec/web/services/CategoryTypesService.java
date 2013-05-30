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

import es.uned.lsi.gepec.model.dao.CategoryTypesDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.CategoryType;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages category types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class CategoryTypesService implements Serializable
{
	private final static CategoryTypesDao CATEGORY_TYPES_DAO=new CategoryTypesDao();
	
	private final static Map<Long,CategoryType> CATEGORY_TYPES_CACHED_BY_ID=new HashMap<Long,CategoryType>();
	private final static Map<String,CategoryType> CATEGORY_TYPES_CACHED_BY_TYPE=new HashMap<String,CategoryType>();
	private final static List<CategoryType> CATEGORY_TYPES_CACHED=new ArrayList<CategoryType>();
	
	public CategoryTypesService()
	{
	}
	
	/**
	 * @param id Category type's identifier
	 * @return Category type
	 * @throws ServiceException
	 */
	public CategoryType getCategoryType(long id) throws ServiceException
	{
		return getCategoryType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Category type's identifier
	 * @return Category type
	 * @throws ServiceException
	 */
	public CategoryType getCategoryType(Operation operation,long id) throws ServiceException
	{
		CategoryType categoryType=null;
		CategoryType categoryTypeFromCache=CATEGORY_TYPES_CACHED_BY_ID.get(Long.valueOf(id));
		if (categoryTypeFromCache==null)
		{
			try
			{
				CATEGORY_TYPES_DAO.setOperation(operation);
				categoryTypeFromCache=CATEGORY_TYPES_DAO.getCategoryType(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (categoryTypeFromCache!=null)
			{
				CATEGORY_TYPES_CACHED_BY_ID.put(Long.valueOf(id),categoryTypeFromCache);
				CATEGORY_TYPES_CACHED_BY_TYPE.put(categoryTypeFromCache.getType(),categoryTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached category type so we return a copy
		if (categoryTypeFromCache!=null)
		{
			categoryType=new CategoryType();
			categoryType.setFromOtherCategoryType(categoryTypeFromCache);
			categoryType.setParent(getParentCopy(categoryType.getParent()));
		}
		return categoryType;
	}
	
	/**
	 * @param type Category type string
	 * @return Category type
	 * @throws ServiceException
	 */
	public CategoryType getCategoryType(String type) throws ServiceException
	{
		return getCategoryType(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Category type string
	 * @return Category type
	 * @throws ServiceException
	 */
	public CategoryType getCategoryType(Operation operation,String type) throws ServiceException
	{
		CategoryType categoryType=null;
		CategoryType categoryTypeFromCache=CATEGORY_TYPES_CACHED_BY_TYPE.get(type);
		if (categoryTypeFromCache==null)
		{
			try
			{
				CATEGORY_TYPES_DAO.setOperation(operation);
				categoryTypeFromCache=CATEGORY_TYPES_DAO.getCategoryType(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (categoryTypeFromCache!=null)
			{
				CATEGORY_TYPES_CACHED_BY_ID.put(Long.valueOf(categoryTypeFromCache.getId()),categoryTypeFromCache);
				CATEGORY_TYPES_CACHED_BY_TYPE.put(type,categoryTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached category type so we return a copy
		if (categoryTypeFromCache!=null)
		{
			categoryType=new CategoryType();
			categoryType.setFromOtherCategoryType(categoryTypeFromCache);
			categoryType.setParent(getParentCopy(categoryType.getParent()));
		}
		return categoryType;
	}
	
	/**
	 * @return List of all category types
	 * @throws ServiceException
	 */
	public List<CategoryType> getCategoryTypes() throws ServiceException
	{
		return getCategoryTypes(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all category types
	 * @throws ServiceException
	 */
	public List<CategoryType> getCategoryTypes(Operation operation) throws ServiceException
	{
		List<CategoryType> categoryTypes=new ArrayList<CategoryType>();
		if (CATEGORY_TYPES_CACHED.isEmpty())
		{
			try
			{
				CATEGORY_TYPES_DAO.setOperation(operation);
				for (CategoryType categoryType:CATEGORY_TYPES_DAO.getCategoryTypes())
				{
					Long categoryTypeId=Long.valueOf(categoryType.getId());
					if (CATEGORY_TYPES_CACHED_BY_ID.containsKey(categoryTypeId))
					{
						categoryType=CATEGORY_TYPES_CACHED_BY_ID.get(categoryTypeId);
					}
					else
					{
						CATEGORY_TYPES_CACHED_BY_ID.put(categoryTypeId,categoryType);
						CATEGORY_TYPES_CACHED_BY_TYPE.put(categoryType.getType(),categoryType);
					}
					CATEGORY_TYPES_CACHED.add(categoryType);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			for (CategoryType categoryType:CATEGORY_TYPES_CACHED)
			{
				if (categoryType.getParent()!=null)
				{
					categoryType.setParent(
						CATEGORY_TYPES_CACHED_BY_ID.get(Long.valueOf(categoryType.getParent().getId())));
				}
			}
		}
		// We don't want caller accessing directly to cached category types so we return copies of all them
		for (CategoryType categoryTypeFromCache:CATEGORY_TYPES_CACHED)
		{
			CategoryType categoryType=null;
			if (categoryTypeFromCache!=null)
			{
				categoryType=new CategoryType();
				categoryType.setFromOtherCategoryType(categoryTypeFromCache);
			}
			categoryTypes.add(categoryType);
		}
		// We also need to use the new copies of category types in their 'parent' field
		for (CategoryType categoryType:categoryTypes)
		{
			if (categoryType.getParent()!=null)
			{
				categoryType.setParent(categoryTypes.get(categoryTypes.indexOf(categoryType.getParent())));
			}
		}
		return categoryTypes;
	}
	
	/**
	 * Reset cached category types.
	 */
	public void resetCachedCategoryTypes()
	{
		CATEGORY_TYPES_CACHED_BY_ID.clear();
		CATEGORY_TYPES_CACHED_BY_TYPE.clear();
		CATEGORY_TYPES_CACHED.clear();
	}
	
	/**
	 * Checks if category type derives from indicated category type.<br/><br/>
	 * Note that this method keep track of previously checked categories types to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param operation Operation
	 * @param checkedCategoryTypes Category types already checked
	 * @param categoryType Category type to check
	 * @param from Category type from we check if category type derives
	 * @return true if category type derives from indicated category type, false otherwise (or in case of corrupted 
	 * data)
	 * @throws ServiceException
	 */
	private boolean isDerivedFrom(Operation operation,List<CategoryType> checkedCategoryTypes,
		CategoryType categoryType,CategoryType from) throws ServiceException
	{
		boolean isDerived=false;
		if (categoryType!=null && !checkedCategoryTypes.contains(categoryType))
		{
			isDerived=categoryType.equals(from);
			if (!isDerived)
			{
				boolean singleOp=operation==null;
				try
				{
					if (singleOp)
					{
						// Start Hibernate operation
						operation=HibernateUtil.startOperation();
					}
					
					checkedCategoryTypes.add(categoryType);
					if (categoryType.getParent()!=null)
					{
						categoryType.setParent(getCategoryType(operation,categoryType.getParent().getId()));
					}
					isDerived=isDerivedFrom(operation,checkedCategoryTypes,categoryType.getParent(),from);
				}
				catch (DaoException de)
				{
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
		}
		else if (categoryType==null)
		{
			isDerived=from==null;
		}
		return isDerived;
	}
	
	/**
	 * Checks if category type derives from indicated category type.<br/><br/>
	 * Note that this method keep track of previously checked category types to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param categoryType Category type to check
	 * @param from Category type from we check if category type derives
	 * @return true if category type derives from indicated category type, false otherwise
	 * @throws ServiceException
	 */
	public boolean isDerivedFrom(CategoryType categoryType,CategoryType from) throws ServiceException
	{
		return isDerivedFrom(null,new ArrayList<CategoryType>(),categoryType,from);
	}
	
	/**
	 * Checks if category type derives from indicated category type.<br/><br/>
	 * Note that this method keep track of previously checked category types to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param operation Operation
	 * @param categoryType Category type to check
	 * @param from Category type from we check if category type derives
	 * @return true if category type derives from indicated category type, false otherwise
	 * @throws ServiceException
	 */
	public boolean isDerivedFrom(Operation operation,CategoryType categoryType,CategoryType from)
		throws ServiceException
	{
		return isDerivedFrom(operation,new ArrayList<CategoryType>(),categoryType,from);
	}
	
	/**
	 * @param parent Parent category type
	 * @param parentCopies List with copies of parent categories types already done
	 * @return Copy of parent category type
	 */
	private CategoryType getParentCopy(CategoryType parent,List<CategoryType> parentCopies)
	{
		CategoryType parentCopy=null;
		if (parent!=null)
		{
			if (parentCopies.contains(parent))
			{
				parentCopy=parentCopies.get(parentCopies.indexOf(parent));
			}
			else
			{
				parentCopy=new CategoryType();
				parentCopy.setFromOtherCategoryType(parent);
				parentCopies.add(parentCopy);
				parentCopy.setParent(getParentCopy(parentCopy.getParent(),parentCopies));
			}
		}
		return parentCopy;
	}
	
	/**
	 * @param parent Parent category type
	 * @return Copy of parent category type
	 */
	private CategoryType getParentCopy(CategoryType parent)
	{
		return getParentCopy(parent,new ArrayList<CategoryType>());
	}
}
