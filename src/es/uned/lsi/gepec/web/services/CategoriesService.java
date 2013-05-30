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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.dao.CategoriesDao;
import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.CategoryType;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

//Ofrece a la vista operaciones con categorías
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Manages categories.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class CategoriesService implements Serializable
{
	public final static int NOT_VIEW_OTHER_USERS_CATEGORIES=0;
	public final static int VIEW_OTHER_USERS_PUBLIC_CATEGORIES=1;
	public final static int VIEW_OTHER_USERS_PRIVATE_CATEGORIES=2;
	public final static int VIEW_OTHER_USERS_ALL_CATEGORIES=3;
	
	private class CategoryLocalizedCategoryLongNameComparator implements Comparator<Category>
	{
		private Operation operation;
		
		public CategoryLocalizedCategoryLongNameComparator(Operation operation)
		{
			this.operation=operation;
		}
		
		@Override
		public int compare(Category o1,Category o2)
		{
			int result=0;
			if (o1==null)
			{
				result=o2==null?0:-1;
			}
			else if (o2==null)
			{
				result=1;
			}
			else
			{
				String localizedCategoryLongName1=getLocalizedCategoryLongName(operation,o1);
				String localizedCategoryLongName2=getLocalizedCategoryLongName(operation,o2);
				result=localizedCategoryLongName1.compareTo(localizedCategoryLongName2);
			}
			return result;
		}
	};
	
	private static CategoriesDao CATEGORIES_DAO=new CategoriesDao();
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	public CategoriesService()
	{
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
	public void setCategoryTypesService(CategoryTypesService categoryTypesService)
	{
		this.categoryTypesService=categoryTypesService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	//Obtiene una categoría a partir de su id
	/**
	 * @param id Category identifier
	 * @return Category
	 * @throws ServiceException
	 */
	public Category getCategory(long id) throws ServiceException
	{
		return getCategory(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Category identifier
	 * @return Category
	 * @throws ServiceException
	 */
	public Category getCategory(Operation operation,long id) throws ServiceException
	{
		Category category=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get category
			CATEGORIES_DAO.setOperation(operation);
			category=CATEGORIES_DAO.getCategory(id,true,true,true);
			if (category!=null)
			{
				category.setCategoryType(
					categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
				category.setVisibility(
					visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
			}
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
		return category;
	}
	
	//Actualiza una categoría
	/**
	 * Updates a category.
	 * @param category Category
	 * @throws ServiceException
	 */
	public void updateCategory(Category category) throws ServiceException
	{
		updateCategory(null,category);
	}
	
	/**
	 * Updates a category.
	 * @param operation Operation
	 * @param category Category
	 * @throws ServiceException
	 */
	public void updateCategory(Operation operation,Category category) throws ServiceException
	{
		try
		{
			CATEGORIES_DAO.setOperation(operation);
			CATEGORIES_DAO.updateCategory(category);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	//Añade una nueva categoría
	/**
	 * Adds a new category.
	 * @param category Category
	 * @throws ServiceException
	 */
	public long addCategory(Category category) throws ServiceException
	{
		return addCategory(null,category);
	}
	
	/**
	 * Adds a new category.
	 * @param operation Operation
	 * @param category Category
	 * @throws ServiceException
	 */
	public long addCategory(Operation operation,Category category) throws ServiceException
	{
		long categoryId=0L;
		try
		{
			CATEGORIES_DAO.setOperation(operation);
			categoryId=CATEGORIES_DAO.saveCategory(category);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return categoryId;
	}
	
	//Elimina una categoría
	//return True si se elimina con éxito y false si no
	/**
	 * Deletes a category.
	 * @param category Category
	 * @throws ServiceException
	 */
	public void deleteCategory(Category category) throws ServiceException
	{
		deleteCategory(null,category);
	}
	
	/**
	 * Deletes a category.
	 * @param operation Operation
	 * @param category Category
	 * @throws ServiceException
	 */
	public void deleteCategory(Operation operation,Category category) throws ServiceException
	{
		try
		{
			CATEGORIES_DAO.setOperation(operation);
			CATEGORIES_DAO.deleteCategory(category);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	//Elimina una categoría
	//param resourceId Id de la categoría
	//throws Exception si ocurre algún error en la eliminación
	/**
	 * Deletes a category.
	 * @param categoryId Category identifier 
	 * @throws ServiceException
	 */
	public void deleteCategory(long categoryId) throws ServiceException
	{
		
		deleteCategory(null,categoryId);
	}
	
	/**
	 * Deletes a category.
	 * @param operation Operation
	 * @param categoryId Category identifier 
	 * @throws ServiceException
	 */
	public void deleteCategory(Operation operation,long categoryId) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check if it is not the default category
			CATEGORIES_DAO.setOperation(operation);
			Category category=CATEGORIES_DAO.getCategory(categoryId,false,true,true);
			
			// Delete category
			deleteCategory(operation,category);
			
			if (singleOp)
			{
				// Do commit
				operation.commit();
			}
		}
		catch (DaoException de)
		{
			if (singleOp)
			{
				// Do rollback
				operation.rollback();
			}
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
	
	/**
	 * @param user User
	 * @return Default category of an user
	 * @throws ServiceException
	 */
	public Category getDefaultCategory(User user) throws ServiceException
	{
		return getDefaultCategory(null,user);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return Default category of an user
	 * @throws ServiceException
	 */
	public Category getDefaultCategory(Operation operation,User user) throws ServiceException
	{
		Category defaultCategory=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			defaultCategory=CATEGORIES_DAO.getDefaultCategory(user.getId(),true,true,true);
			
			defaultCategory.setCategoryType(
				categoryTypesService.getCategoryType(operation,defaultCategory.getCategoryType().getId()));
			defaultCategory.setVisibility(
				visibilitiesService.getVisibility(operation,defaultCategory.getVisibility().getId()));
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
		return defaultCategory;
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @return List of all categories of an user filtered by type and sorted by category name if required
	 * @throws ServiceException
	 */
	private List<Category> getUserCategories(Operation operation,User user,CategoryType categoryType,
		boolean sortedByName) throws ServiceException
	{
		List<Category> categories=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			categories=CATEGORIES_DAO.getUserCategories(user.getId(),sortedByName,false,true,true);
			if (categoryType==null)
			{
				for (Category category:categories)
				{
					category.setUser(user);
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					category.setVisibility(
						visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				}
			}
			else
			{
				// Remove categories with a category type that cannot derive from/to the required category type
				List<Category> categoriesToRemove=new ArrayList<Category>();
				for (Category category:categories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					if (categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType))
					{
						category.setUser(user);
						category.setVisibility(
							visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
					}
					else
					{
						categoriesToRemove.add(category);
					}
				}
				for (Category categoryToRemove:categoriesToRemove)
				{
					categories.remove(categoryToRemove);
				}
			}
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
		return categories;
	}
	
	//Obtiene las categorías de un usuario
	/**
	 * @param user User
	 * @return List of all categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserCategories(User user) throws ServiceException
	{
		return getUserCategories(null,user,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserCategories(Operation operation,User user) throws ServiceException
	{
		return getUserCategories(operation,user,null,false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserCategories(User user,CategoryType categoryType) throws ServiceException
	{
		return getUserCategories(null,user,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserCategories(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return getUserCategories(operation,user,categoryType,false);
	}
	
	//Obtiene las categorías de un usuario ordenadas por su nombre
	/**
	 * @param user User
	 * @return List of all categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserCategoriesSortedByName(User user) throws ServiceException
	{
		return getUserCategories(null,user,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserCategoriesSortedByName(Operation operation,User user)
		throws ServiceException
	{
		return getUserCategories(operation,user,null,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories of an user sorted by name and filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserCategoriesSortedByName(User user,CategoryType categoryType)
		throws ServiceException
	{
		return getUserCategories(null,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories of an user sorted by name and filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserCategoriesSortedByName(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return getUserCategories(operation,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user Filtering user or null to get global categories from all users
	 * @param categoryType Category type
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @return List of all global categories filtered by type and sorted by category name if required
	 * @throws ServiceException
	 */
	private List<Category> getGlobalCategories(Operation operation,User user,CategoryType categoryType,
		boolean sortedByName) throws ServiceException
	{
		List<Category> globalCategories=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			globalCategories=
				CATEGORIES_DAO.getGlobalCategories(user==null?0L:user.getId(),sortedByName,true,true,true);
			if (categoryType==null)
			{
				for (Category category:globalCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					category.setVisibility(
						visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				}
			}
			else
			{
				// Remove categories with a category type that cannot derive from/to the required category type
				List<Category> categoriesToRemove=new ArrayList<Category>();
				for (Category category:globalCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					if (categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType))
					{
						category.setVisibility(
							visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
					}
					else
					{
						categoriesToRemove.add(category);
					}
				}
				for (Category categoryToRemove:categoriesToRemove)
				{
					globalCategories.remove(categoryToRemove);
				}
			}
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
		return globalCategories;
	}
	
	/**
	 * @return List of all global categories
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategories() throws ServiceException
	{
		return getGlobalCategories(null,null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all global categories
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategories(Operation operation) throws ServiceException
	{
		return getGlobalCategories(operation,null,null,false);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all global categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategories(CategoryType categoryType) throws ServiceException
	{
		return getGlobalCategories(null,null,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all global categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategories(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getGlobalCategories(operation,null,categoryType,false);
	}
	
	/**
	 * @return List of all global categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategoriesSortedByName() throws ServiceException
	{
		return getGlobalCategories(null,null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all global categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategoriesSortedByName(Operation operation) throws ServiceException
	{
		return getGlobalCategories(operation,null,null,true);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all global categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategoriesSortedByName(CategoryType categoryType) throws ServiceException
	{
		return getGlobalCategories(null,null,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all global categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getGlobalCategoriesSortedByName(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getGlobalCategories(operation,null,categoryType,true);
	}
	
	/**
	 * @param user User
	 * @return List of all global categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategories(User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(null,user,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all global categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategories(Operation operation,User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(operation,user,null,false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategories(User user,CategoryType categoryType) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(null,user,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategories(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return  user==null?new ArrayList<Category>():getGlobalCategories(operation,user,categoryType,false);
	}
	
	/**
	 * @param user User
	 * @return List of all global categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategoriesSortedByName(User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(null,user,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all global categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategoriesSortedByName(Operation operation,User user) 
		throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(operation,user,null,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories of an user filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategoriesSortedByName(User user,CategoryType categoryType) 
		throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(null,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories of an user filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserGlobalCategoriesSortedByName(Operation operation,User user,
		CategoryType categoryType) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getGlobalCategories(operation,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user Filtering user or null to get non global categories from all users
	 * @param categoryType Category type
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @return List of all non global categories filtered by type and sorted by category name if required
	 * @throws ServiceException
	 */
	private List<Category> getNonGlobalCategories(Operation operation,User user,CategoryType categoryType,
		boolean sortedByName) throws ServiceException
	{
		List<Category> nonGlobalCategories=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			nonGlobalCategories=
				CATEGORIES_DAO.getNonGlobalCategories(user==null?0L:user.getId(),sortedByName,true,true,true);
			if (categoryType==null)
			{
				for (Category category:nonGlobalCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					category.setVisibility(
						visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				}
			}
			else
			{
				// Remove categories with a category type that cannot derive from/to the required category type
				List<Category> categoriesToRemove=new ArrayList<Category>();
				for (Category category:nonGlobalCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					if (categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType))
					{
						category.setVisibility(
							visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
					}
					else
					{
						categoriesToRemove.add(category);
					}
				}
				for (Category categoryToRemove:categoriesToRemove)
				{
					nonGlobalCategories.remove(categoryToRemove);
				}
			}
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
		return nonGlobalCategories;
	}
	
	/**
	 * @return List of all non global categories
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategories() throws ServiceException
	{
		return getNonGlobalCategories(null,null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all non global categories
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategories(Operation operation) throws ServiceException
	{
		return getNonGlobalCategories(operation,null,null,false);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all non global categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategories(CategoryType categoryType) throws ServiceException
	{
		return getNonGlobalCategories(null,null,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all non global categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategories(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getNonGlobalCategories(operation,null,categoryType,false);
	}
	
	/**
	 * @return List of all non global categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategoriesSortedByName() throws ServiceException
	{
		return getNonGlobalCategories(null,null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all non global categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategoriesSortedByName(Operation operation) throws ServiceException
	{
		return getNonGlobalCategories(operation,null,null,true);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all non global categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategoriesSortedByName(CategoryType categoryType) throws ServiceException
	{
		return getNonGlobalCategories(null,null,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all non global categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getNonGlobalCategoriesSortedByName(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getNonGlobalCategories(operation,null,categoryType,true);
	}
	
	/**
	 * @param user User
	 * @return List of all non global categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategories(User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(null,user,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all non global categories of an user
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategories(Operation operation,User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(operation,user,null,false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all non global categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategories(User user,CategoryType categoryType) 
		throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(null,user,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all non global categories of an user filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategories(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return  user==null?new ArrayList<Category>():getNonGlobalCategories(operation,user,categoryType,false);
	}
	
	/**
	 * @param user User
	 * @return List of all non global categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategoriesSortedByName(User user) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(null,user,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all non global categories of an user sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategoriesSortedByName(Operation operation,User user) 
		throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(operation,user,null,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all non global categories of an user filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategoriesSortedByName(User user,CategoryType categoryType) 
		throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(null,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all non global categories of an user filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getUserNonGlobalCategoriesSortedByName(Operation operation,User user,
		CategoryType categoryType) throws ServiceException
	{
		return user==null?new ArrayList<Category>():getNonGlobalCategories(operation,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @return List of all public categories filtered by type and sorted by category name if required
	 * @throws ServiceException
	 */
	private List<Category> getPublicCategories(Operation operation,CategoryType categoryType,
		boolean sortedByName) throws ServiceException
	{
		List<Category> publicCategories=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			publicCategories=CATEGORIES_DAO.getVisibleCategories(false,
				visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel(),
				sortedByName,true,true,true);
			if (categoryType==null)
			{
				for (Category category:publicCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					category.setVisibility(
						visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				}
			}
			else
			{
				// Remove categories with a category type that cannot derive from/to the required category type
				List<Category> categoriesToRemove=new ArrayList<Category>();
				for (Category category:publicCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					if (categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType))
					{
						category.setVisibility(
							visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
					}
					else
					{
						categoriesToRemove.add(category);
					}
				}
				for (Category categoryToRemove:categoriesToRemove)
				{
					publicCategories.remove(categoryToRemove);
				}
			}
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
		return publicCategories;
	}
	
	/**
	 * @return List of all public categories
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategories() throws ServiceException
	{
		return getPublicCategories(null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all public categories
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategories(Operation operation) throws ServiceException
	{
		return getPublicCategories(operation,null,false);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all public categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategories(CategoryType categoryType) throws ServiceException
	{
		return getPublicCategories(null,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all public categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategories(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getPublicCategories(operation,categoryType,false);
	}
	
	/**
	 * @return List of all public categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategoriesSortedByName() throws ServiceException
	{
		return getPublicCategories(null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all public categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategoriesSortedByName(Operation operation) throws ServiceException
	{
		return getPublicCategories(operation,null,true);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all public categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategoriesSortedByName(CategoryType categoryType) throws ServiceException
	{
		return getPublicCategories(null,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all public categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPublicCategoriesSortedByName(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getPublicCategories(operation,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @return List of all private categories filtered by type and sorted by category name if required
	 * @throws ServiceException
	 */
	private List<Category> getPrivateCategories(Operation operation,CategoryType categoryType,boolean sortedByName) 
		throws ServiceException
	{
		List<Category> privateCategories=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			CATEGORIES_DAO.setOperation(operation);
			privateCategories=CATEGORIES_DAO.getNonVisibleCategories(false,
				visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel(),
				sortedByName,true,true,true);
			if (categoryType==null)
			{
				for (Category category:privateCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					category.setVisibility(
						visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				}
			}
			else
			{
				// Remove categories with a category type that cannot derive from/to the required category type
				List<Category> categoriesToRemove=new ArrayList<Category>();
				for (Category category:privateCategories)
				{
					category.setCategoryType(
						categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
					if (categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType))
					{
						category.setVisibility(
							visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
					}
					else
					{
						categoriesToRemove.add(category);
					}
				}
				for (Category categoryToRemove:categoriesToRemove)
				{
					privateCategories.remove(categoryToRemove);
				}
			}
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
		return privateCategories;
	}
	
	/**
	 * @return List of all private categories
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategories() throws ServiceException
	{
		return getPrivateCategories(null,null,false);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all private categories
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategories(Operation operation) throws ServiceException
	{
		return getPrivateCategories(operation,null,false);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all private categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategories(CategoryType categoryType) throws ServiceException
	{
		return getPrivateCategories(null,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all private categories filtered by type
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategories(Operation operation,CategoryType categoryType) throws ServiceException
	{
		return getPrivateCategories(operation,categoryType,false);
	}
	
	/**
	 * @return List of all private categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategoriesSortedByName() throws ServiceException
	{
		return getPrivateCategories(null,null,true);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all private categories sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategoriesSortedByName(Operation operation) throws ServiceException
	{
		return getPrivateCategories(operation,null,true);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all private categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategoriesSortedByName(CategoryType categoryType) throws ServiceException
	{
		return getPrivateCategories(null,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all private categories filtered by type and sorted by name
	 * @throws ServiceException
	 */
	public List<Category> getPrivateCategoriesSortedByName(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		return getPrivateCategories(operation,categoryType,true);
	}
	
	/**
	 * @param parent Parent category 
	 * @return List of child categories of the indicated parent category or root categories if parent category 
	 * is null
	 * @throws ServiceException
	 */
	public List<Category> getChildCategories(Category parent) throws ServiceException
	{
		return getChildCategories(null,parent);
	}
	
	/**
	 * @param operation Operation
	 * @param parent Parent category 
	 * @return List of child categories of the indicated parent category or root categories if parent category 
	 * is null
	 * @throws ServiceException
	 */
	public List<Category> getChildCategories(Operation operation,Category parent) throws ServiceException
	{
		List<Category> childCategories=null;
		try
		{
			CATEGORIES_DAO.setOperation(operation);
			childCategories=
				CATEGORIES_DAO.getChildCategories(parent==null?0L:parent.getId(),false,true,true,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return childCategories; 
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeGlobals Flag to indicate if we want to include user global categories in the results 
	 * @return List of all categories identifiers of an user derived from its default category including 
	 * also its global categories if indicated and allowing to filter results by category type
	 * @throws ServiceException
	 */
	private List<Long> getAllUserCategoriesIds(Operation operation,User user,CategoryType categoryType,
		boolean includeGlobals) throws ServiceException
	{
		List<Long> allMyCategoriesIds=new ArrayList<Long>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Category> userCategories=getUserCategories(operation,user,categoryType,false);
			
			// Find default category and all its derived categories
			Category defaultCategory=getDefaultCategory(operation,user);
			if (defaultCategory!=null)
			{
				// If we are filtering by category type we will not add default category and 
				// its derived categories if its category type cannot derive from/to the required category type
				if (categoryType==null || 
					categoryTypesService.isDerivedFrom(
					operation,categoryType,defaultCategory.getCategoryType()) || 
					categoryTypesService.isDerivedFrom(
					operation,defaultCategory.getCategoryType(),categoryType))
				{
					// Add default category
					allMyCategoriesIds.add(Long.valueOf(defaultCategory.getId()));
					
					// Now we add all categories derived from default category
					addDerivedCategoriesIds(userCategories,defaultCategory,allMyCategoriesIds);
				}
			}
			
			// Finally we include user global categories if required
			if (includeGlobals)
			{
				for (Category category:userCategories)
				{
					// We look for user global categories without a parent category 
					//(root user global categories)
					if (category.getParent()==null && category.getVisibility().isGlobal())
					{
						// Add root user global category
						allMyCategoriesIds.add(Long.valueOf(category.getId()));
						
						// Now we add all global categories derived from this root global category
						addDerivedCategoriesIds(userCategories,category,allMyCategoriesIds);
					}
				}
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allMyCategoriesIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesIds(User user,CategoryType categoryType) throws ServiceException
	{
		return getAllCategoriesIds(null,user,categoryType,true,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesIds(Operation operation,User user,CategoryType categoryType) 
		throws ServiceException
	{
		return getAllCategoriesIds(operation,user,categoryType);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all categories identifiers
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesIds(User user,CategoryType categoryType,boolean includeAdminsPrivateCategories,
		boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		return getAllCategoriesIds(
			null,user,categoryType,includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all categories identifiers
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesIds(Operation operation,User user,CategoryType categoryType,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		List<Long> allCategoriesIds=new ArrayList<Long>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			for (Long userCategoryId:getAllUserCategoriesIds(operation,user,categoryType,false))
			{
				allCategoriesIds.add(userCategoryId);
			}
			for (Long globalCategoryId:getAllGlobalCategoriesIds(operation,categoryType))
			{
				allCategoriesIds.add(globalCategoryId);
			}
			for (Long otherUserCategoryId:getAllCategoriesOfOtherUsersIds(
				operation,user,categoryType,includeAdminsPrivateCategories,includeSuperadminsPrivateCategories))
			{
				allCategoriesIds.add(otherUserCategoryId);
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers visibles for an user
	 * @throws ServiceException
	 */
	public List<Long> getAllVisibleCategoriesIds(User user,CategoryType categoryType) throws ServiceException
	{
		return getAllVisibleCategoriesIds(null,user,categoryType);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers visibles for an user
	 * @throws ServiceException
	 */
	public List<Long> getAllVisibleCategoriesIds(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		List<Long> allVisibleCategoriesIds=new ArrayList<Long>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			for (Long userCategoryId:getAllUserCategoriesIds(operation,user,categoryType,false))
			{
				allVisibleCategoriesIds.add(userCategoryId);
			}
			for (Long globalCategoryId:getAllGlobalCategoriesIds(operation,categoryType))
			{
				allVisibleCategoriesIds.add(globalCategoryId);
			}
			for (Long publicCategoryOfOtherUser:getAllPublicCategoriesOfOtherUsersIds(
				operation,user,categoryType))
			{
				allVisibleCategoriesIds.add(publicCategoryOfOtherUser);
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allVisibleCategoriesIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of an user derived from its default category ands its global 
	 * categories and allowing to filter results by category type
	 * @throws ServiceException
	 */
	public List<Long> getAllUserCategoriesIds(User user,CategoryType categoryType) throws ServiceException
	{
		return getAllUserCategoriesIds(null,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of an user derived from its default category ands its global 
	 * categories and allowing to filter results by category type
	 * @throws ServiceException
	 */
	public List<Long> getAllUserCategoriesIds(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return getAllUserCategoriesIds(operation,user,categoryType,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of an user derived from its default category ands its global 
	 * categories and allowing to filter results by category type
	 * @throws ServiceException
	 */
	public List<Long> getAllUserCategoriesIdsExceptGlobals(User user,CategoryType categoryType)
		throws ServiceException
	{
		return getAllUserCategoriesIds(null,user,categoryType,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of an user derived from its default category ands its global 
	 * categories and allowing to filter results by category type
	 * @throws ServiceException
	 */
	public List<Long> getAllUserCategoriesIdsExceptGlobals(Operation operation,User user,
		CategoryType categoryType) throws ServiceException
	{
		return getAllUserCategoriesIds(operation,user,categoryType,false);
	}
	
	/**
	 * @param categoryType Category type
	 * @return List of all global categories identifiers filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllGlobalCategoriesIds(CategoryType categoryType) throws ServiceException
	{
		return getAllGlobalCategoriesIds(null,categoryType);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryType Category type
	 * @return List of all global categories identifiers filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllGlobalCategoriesIds(Operation operation,CategoryType categoryType)
		throws ServiceException
	{
		List<Long> allGlobalCategoriesIds=new ArrayList<Long>();
		List<Category> globalCategories=getGlobalCategories(operation,null,categoryType,false);
		for (Category globalCategory:globalCategories)
		{
			// We look for user global categories without a parent category (root user global categories)
			if (globalCategory.getParent()==null)
			{
				// Add root user global category
				allGlobalCategoriesIds.add(Long.valueOf(globalCategory.getId()));
				
				// Now we add all global categories derived from this root global category
				addDerivedCategoriesIds(globalCategories,globalCategory,allGlobalCategoriesIds);
			}
		}
		return allGlobalCategoriesIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories identifiers of an user filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllUserGlobalCategoriesIds(User user,CategoryType categoryType) throws ServiceException
	{
		return getAllUserGlobalCategoriesIds(null,user,categoryType);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all global categories identifiers of an user filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllUserGlobalCategoriesIds(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		List<Long> allUserGlobalCategoriesIds=new ArrayList<Long>();
		List<Category> userGlobalCategories=
			user==null?new ArrayList<Category>():getGlobalCategories(operation,user,categoryType,false);
		for (Category globalCategory:userGlobalCategories)
		{
			// We look for user global categories without a parent category (root user global categories)
			if (globalCategory.getParent()==null)
			{
				// Add root user global category
				allUserGlobalCategoriesIds.add(Long.valueOf(globalCategory.getId()));
				
				// Now we add all global categories derived from this root global category
				addDerivedCategoriesIds(userGlobalCategories,globalCategory,allUserGlobalCategoriesIds);
			}
		}
		return allUserGlobalCategoriesIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all public categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPublicCategoriesOfOtherUsersIds(User user,CategoryType categoryType)
		throws ServiceException
	{
		return getAllPublicCategoriesOfOtherUsersIds(null,user,categoryType);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all public categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPublicCategoriesOfOtherUsersIds(Operation operation,User user,CategoryType categoryType) 
		throws ServiceException
	{
		List<Long> allPublicCategoriesOfOtherUsersIds=new ArrayList<Long>();
		List<Category> publicCategories=getPublicCategories(operation,categoryType,false);
		// Initialize an auxiliar list of checked users used to avoid adding several times the same public 
		// categories to the category tree
		List<User> checkedUsers=new ArrayList<User>();
		
		// Add current user to checked users because we have already managed current user categories
		// if required 
		checkedUsers.add(user);
		
		// We need to find default categories of other users without a parent category 
		// (root categories of other users)
		List<Category> rootPublicCategories=new ArrayList<Category>();
		for (Category publicCategory:publicCategories)
		{
			User userToCheck=publicCategory.getUser();
			if (!checkedUsers.contains(userToCheck))
			{
				Category rootPublicCategory=getUserDefaultCategory(publicCategories,userToCheck);
				checkedUsers.add(userToCheck);
				if (rootPublicCategory!=null)
				{
					rootPublicCategories.add(rootPublicCategory);
				}
			}
		}
		for (Category rootPublicCategory:rootPublicCategories)
		{
			// Add root public category of other user
			allPublicCategoriesOfOtherUsersIds.add(Long.valueOf(rootPublicCategory.getId()));
			
			// Now we add all public categories derived from this root private category of other user
			addDerivedCategoriesIds(
				publicCategories,rootPublicCategory,allPublicCategoriesOfOtherUsersIds);
		}
		return allPublicCategoriesOfOtherUsersIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all private categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPrivateCategoriesOfOtherUsersIds(User user,CategoryType categoryType)
		throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersIds(null,user,categoryType,true,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all private categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPrivateCategoriesOfOtherUsersIds(Operation operation,User user,
		CategoryType categoryType) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersIds(operation,user,categoryType,true,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all private categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPrivateCategoriesOfOtherUsersIds(User user,CategoryType categoryType,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		return getAllPrivateCategoriesOfOtherUsersIds(
			null,user,categoryType,includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all private categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllPrivateCategoriesOfOtherUsersIds(Operation operation,User user,CategoryType categoryType,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Long> allPrivateCategoriesOfOtherUsersIds=new ArrayList<Long>();
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Category> privateCategories=getPrivateCategories(operation,categoryType,false);
			
			// We remove private categories of administrators/superadministrators if needed
			if (!includeAdminsPrivateCategories)
			{
				if (includeSuperadminsPrivateCategories)
				{
					removeAdminsPrivateCategories(operation,privateCategories,false);
				}
				else
				{
					removeAdminsAndSuperadminsPrivateCategories(operation,privateCategories,false);
				}
			}
			else if (!includeSuperadminsPrivateCategories)
			{
				removeSuperadminsPrivateCategories(operation,privateCategories,false);
			}
			
			// Initialize an auxiliar list of checked users used to avoid adding several times the same public 
			// categories to the category tree
			List<User> checkedUsers=new ArrayList<User>();
			
			// Add current user to checked users because we have already managed current user categories
			// if required 
			checkedUsers.add(user);
			
			// We need to find default categories of other users without a parent category 
			// (root categories of other users)
			List<Category> rootPrivateCategories=new ArrayList<Category>();
			Visibility publicVisibility=
				visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC");
			for (Category privateCategory:privateCategories)
			{
				User userToCheck=privateCategory.getUser();
				if (!checkedUsers.contains(userToCheck))
				{
					Category rootPrivateCategory=getUserDefaultCategory(privateCategories,userToCheck);
					checkedUsers.add(userToCheck);
					if (rootPrivateCategory!=null)
					{
						rootPrivateCategories.add(rootPrivateCategory);
					}
				}
				// It is possible that we need to add this category as a root category even if 
				// it is not a default category without a parent category.
				// Specifically we must add this category if its parent category has public 
				// visibility (and obviously it it is not from the current user)
				if (!user.equals(userToCheck) && privateCategory.getParent()!=null &&
					(privateCategory.getParent().getVisibility().isGlobal() || 
					privateCategory.getParent().getVisibility().getLevel()<=publicVisibility.getLevel()))
				{
					rootPrivateCategories.add(privateCategory);
				}
			}
			for (Category rootPrivateCategory:rootPrivateCategories)
			{
				// Add root private category of other user
				allPrivateCategoriesOfOtherUsersIds.add(Long.valueOf(rootPrivateCategory.getId()));
				
				// Now we add all private categories derived from this root private category of other user
				addDerivedCategoriesIds(
					privateCategories,rootPrivateCategory,allPrivateCategoriesOfOtherUsersIds);
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allPrivateCategoriesOfOtherUsersIds;
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesOfOtherUsersIds(User user,CategoryType categoryType)
		throws ServiceException
	{
		return getAllCategoriesOfOtherUsersIds(null,user,categoryType,true,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @return List of all categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesOfOtherUsersIds(Operation operation,User user,CategoryType categoryType) 
		throws ServiceException
	{
		return getAllCategoriesOfOtherUsersIds(operation,user,categoryType,true,true);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesOfOtherUsersIds(User user,CategoryType categoryType,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		return getAllCategoriesOfOtherUsersIds(
			null,user,categoryType,includeAdminsPrivateCategories,includeSuperadminsPrivateCategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of all categories identifiers of other users filtered by category type if indicated
	 * @throws ServiceException
	 */
	public List<Long> getAllCategoriesOfOtherUsersIds(Operation operation,User user,CategoryType categoryType,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories) throws ServiceException
	{
		boolean singleOp=operation==null;
		List<Long> allCategoriesOfOtherUsersIds=new ArrayList<Long>();
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<Category> nonGlobalCategories=getNonGlobalCategories(operation,null,categoryType,false);
			
			// We remove non global categories of administrators/superadministrators if needed
			if (!includeAdminsPrivateCategories)
			{
				if (includeSuperadminsPrivateCategories)
				{
					removeAdminsPrivateCategories(operation,nonGlobalCategories,true);
				}
				else
				{
					removeAdminsAndSuperadminsPrivateCategories(operation,nonGlobalCategories,true);
				}
			}
			else if (!includeSuperadminsPrivateCategories)
			{
				removeSuperadminsPrivateCategories(operation,nonGlobalCategories,true);
			}
			
			// Initialize an auxiliar list of checked users used to avoid adding several times the same public 
			// categories to the category tree
			List<User> checkedUsers=new ArrayList<User>();
			
			// Add current user to checked users because we have already managed current user categories
			checkedUsers.add(user);
			
			// We need to find default categories of other users without a parent category 
			// (root categories of other users)
			List<Category> rootNonGlobalCategories=new ArrayList<Category>();
			for (Category nonGlobalCategory:nonGlobalCategories)
			{
				User userToCheck=nonGlobalCategory.getUser();
				if (!checkedUsers.contains(userToCheck))
				{
					Category rootNonGlobalCategory=getUserDefaultCategory(nonGlobalCategories,userToCheck);
					checkedUsers.add(userToCheck);
					if (rootNonGlobalCategory!=null)
					{
						rootNonGlobalCategories.add(rootNonGlobalCategory);
					}
				}
			}
			for (Category rootNonGlobalCategory:rootNonGlobalCategories)
			{
				// Add root category of other user
				allCategoriesOfOtherUsersIds.add(Long.valueOf(rootNonGlobalCategory.getId()));
				
				// Now we add all private categories derived from this root private category of other user
				addDerivedCategoriesIds(nonGlobalCategories,rootNonGlobalCategory,allCategoriesOfOtherUsersIds);
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return allCategoriesOfOtherUsersIds;
	}
	
	//Obtiene las categorías de un usuario ordenadas jerárquicamente
	//Para uso con combos
	/**
	 * @param user User
	 * @return List of all categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(User user) throws ServiceException
	{
		return getCategoriesSortedByHierarchy(null,user,null,true,true,true);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(Operation operation,User user) throws ServiceException
	{
		return getCategoriesSortedByHierarchy(operation,user,null,true,true,true);
	}
	
	/**
	 * @param user User
	 * @return List of all categories sorted hierarchically filtered by category type
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(User user,CategoryType categoryType) throws ServiceException
	{
		return getCategoriesSortedByHierarchy(
			null,user,categoryType,true,true,VIEW_OTHER_USERS_PUBLIC_CATEGORIES,false,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @return List of all categories sorted hierarchically filtered by category type
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(Operation operation,User user,CategoryType categoryType)
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(
			operation,user,categoryType,true,true,VIEW_OTHER_USERS_PUBLIC_CATEGORIES,false,false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Flag to indicate if we want to include public categories 
	 * of other users in results
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(User user,CategoryType categoryType,
		boolean includeUserCategories,boolean includeGlobalCategories,boolean includeOtherUserCategories)
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(null,user,categoryType,includeUserCategories,includeGlobalCategories,
			includeOtherUserCategories?VIEW_OTHER_USERS_PUBLIC_CATEGORIES:NOT_VIEW_OTHER_USERS_CATEGORIES,false,
			false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Flag to indicate if we want to include public categories of other 
	 * users in results
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(Operation operation,User user,CategoryType categoryType,
		boolean includeUserCategories,boolean includeGlobalCategories,boolean includeOtherUserCategories) 
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(operation,user,categoryType,includeUserCategories,
			includeGlobalCategories,
			includeOtherUserCategories?VIEW_OTHER_USERS_PUBLIC_CATEGORIES:NOT_VIEW_OTHER_USERS_CATEGORIES,false,
			false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree (none, public, private, all)
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(User user,CategoryType categoryType,
		boolean includeUserCategories,boolean includeGlobalCategories,int includeOtherUserCategories)
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(null,user,categoryType,includeUserCategories,includeGlobalCategories,
			includeOtherUserCategories,false,false);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree (none, public, private, all)
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(Operation operation,User user,CategoryType categoryType,
		boolean includeUserCategories,boolean includeGlobalCategories,int includeOtherUserCategories) 
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(operation,user,categoryType,includeUserCategories,
			includeGlobalCategories,includeOtherUserCategories,false,false);
	}
	
	/**
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree (none, public, private, all)
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(User user,CategoryType categoryType,
		boolean includeUserCategories,boolean includeGlobalCategories,int includeOtherUserCategories,
		boolean includeAdminsPrivateCategories,boolean includeSuperadminsPrivateCategories)
		throws ServiceException
	{
		return getCategoriesSortedByHierarchy(null,user,categoryType,includeUserCategories,
			includeGlobalCategories,includeOtherUserCategories,includeAdminsPrivateCategories,
			includeSuperadminsPrivateCategories);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param categoryType Category type
	 * @param includeUserCategories Flag to indicate if we want to include user categories in results
	 * @param includeGlobalCategories Flag to indicate if we want to include global categories in results
	 * @param includeOtherUserCategories Value indicating if we want to include public categories of 
	 * other users in the categories tree (none, public, private, all)
	 * @param includeAdminsPrivateCategories Flag to indicate if we want to include (true) or not to include 
	 * (false) private categories of administrators
	 * @param includeSuperadminsPrivateCategories Flag to indicate if we want to include (true) or 
	 * not to include (false) private categories of users with permission to improve permisions 
	 * over its owned ones
	 * @return List of filtered categories sorted hierarchically
	 * @throws ServiceException
	 */
	public List<Category> getCategoriesSortedByHierarchy(Operation operation,User user,
		CategoryType categoryType,boolean includeUserCategories,boolean includeGlobalCategories,
		int includeOtherUserCategories,boolean includeAdminsPrivateCategories,
		boolean includeSuperadminsPrivateCategories)
		throws ServiceException
	{
		// Initialize list of categories
		List<Category> sortedCategories=new ArrayList<Category>();
		boolean singleOp=operation==null;
		
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// First we include user categories if required
			if (includeUserCategories)
			{
				// Initialize user categories sorted by name
				List<Category> userCategories=getUserCategoriesSortedByName(operation,user,categoryType);
				
				// We only add default category and all its derived categories
				Category defaultCategory=getDefaultCategory(operation,user);
				if (defaultCategory!=null)
				{
					// If we are filtering by category type we will not add default category and its derived
					// categories if its category type cannot derive from/to the required category type
					if (categoryType==null || 
						categoryTypesService.isDerivedFrom(
						operation,categoryType,defaultCategory.getCategoryType()) ||
						categoryTypesService.isDerivedFrom(
						operation,defaultCategory.getCategoryType(),categoryType))
					{
						// Add default category
						sortedCategories.add(defaultCategory);
						
						// Now we add all categories derived from default category
						addDerivedCategories(userCategories,defaultCategory,sortedCategories);
					}
				}
			}
			
			// Next we include global categories if required
			if (includeGlobalCategories)
			{
				// Initialize global categories sorted by name
				List<Category> globalCategories=getGlobalCategoriesSortedByName(operation,categoryType);
				
				for (Category globalCategory:globalCategories)
				{
					// We look for global categories without a parent category (root global categories)
					if (globalCategory.getParent()==null)
					{
						// Add root global category
						sortedCategories.add(globalCategory);
						
						// Now we add all global categories derived from this root global category
						addDerivedCategories(globalCategories,globalCategory,sortedCategories);
					}
				}
			}
			
			// Finally we include categories of other users if required
			if (includeOtherUserCategories!=NOT_VIEW_OTHER_USERS_CATEGORIES)
			{
				// Initialize categories of other users sorted by name
				//List<Category> publicCategories=getPublicCategoriesSortedByName(operation,categoryType);
				List<Category> otherUserCategories=null;
				switch (includeOtherUserCategories)
				{
					case VIEW_OTHER_USERS_PUBLIC_CATEGORIES:
						otherUserCategories=getPublicCategoriesSortedByName(operation,categoryType);
						break;
					case VIEW_OTHER_USERS_PRIVATE_CATEGORIES:
						otherUserCategories=getPrivateCategories(operation,categoryType);
						
						// We remove private categories of administrators/superadministrators if needed
						if (!includeAdminsPrivateCategories)
						{
							if (includeSuperadminsPrivateCategories)
							{
								removeAdminsPrivateCategories(operation,otherUserCategories,false);
							}
							else
							{
								removeAdminsAndSuperadminsPrivateCategories(
									operation,otherUserCategories,false);
							}
						}
						else if (!includeSuperadminsPrivateCategories)
						{
							removeSuperadminsPrivateCategories(operation,otherUserCategories,false);
						}
						
						// In this case we also need to sort categories by localized category long name
						sortCategoriesByLocalizedCategoryLongName(operation,otherUserCategories);
						break;
					case VIEW_OTHER_USERS_ALL_CATEGORIES:
						otherUserCategories=getNonGlobalCategoriesSortedByName(operation,categoryType);
						
						// We remove private categories of administrators/superadministrators if needed
						if (!includeAdminsPrivateCategories)
						{
							if (includeSuperadminsPrivateCategories)
							{
								removeAdminsPrivateCategories(operation,otherUserCategories,true);
							}
							else
							{
								removeAdminsAndSuperadminsPrivateCategories(operation,otherUserCategories,true);
							}
						}
						else if (!includeSuperadminsPrivateCategories)
						{
							removeSuperadminsPrivateCategories(operation,otherUserCategories,true);
						}
				}
				
				// Initialize an auxiliar list of checked users used to avoid adding several times the same 
				// categories of other users to the category tree
				List<User> checkedUsers=new ArrayList<User>();
				
				// Add current user to checked users because we have already managed current user categories
				// if required 
				checkedUsers.add(user);
				
				// We need to find default categories of other users without a parent category 
				// (root categories of other users)
				List<Category> rootOtherUserCategories=new ArrayList<Category>();
				Visibility publicVisibility=
					visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC");
				for (Category otherUserCategory:otherUserCategories)
				{
					User userToCheck=otherUserCategory.getUser();
					if (!checkedUsers.contains(userToCheck))
					{
						Category rootOtherUserCategory=getUserDefaultCategory(otherUserCategories,userToCheck);
						checkedUsers.add(userToCheck);
						if (rootOtherUserCategory!=null)
						{
							rootOtherUserCategories.add(rootOtherUserCategory);
						}
					}
					// If we are viewing only private categories it is possible that we need to add 
					// this category as a root category even if it is not a default category without
					// a parent category.
					// Specifically we must add this category if its parent category has public 
					// visibility (and obviously it it is not from the current user)
					if (includeOtherUserCategories==VIEW_OTHER_USERS_PRIVATE_CATEGORIES)
					{
						if (!user.equals(otherUserCategory.getUser()) && otherUserCategory.getParent()!=null &&
							(otherUserCategory.getParent().getVisibility().isGlobal() || 
							otherUserCategory.getParent().getVisibility().getLevel()<=
							publicVisibility.getLevel()))
						{
							rootOtherUserCategories.add(otherUserCategory);
						}
					}
				}
				
				// Add root categories of other users and its derived categories 
				for (Category rootOtherUserCategory:rootOtherUserCategories)
				{
					// Add root category of other users
					sortedCategories.add(rootOtherUserCategory);
					
					// Now we add all categories of other users derived from this root category 
					// of other users
					addDerivedCategories(otherUserCategories,rootOtherUserCategory,sortedCategories);
				}
			}
		}
		finally
		{
			if (singleOp)
			{
				// End Hibernate operation
				HibernateUtil.endOperation(operation);
			}
		}
		return sortedCategories;
	}
	
	/**
	 * @param categories List of categories
	 * @param user User
	 * @return User default category
	 */
	private Category getUserDefaultCategory(List<Category> categories,User user)
	{
		Category defaultCategory=null;
		if (user!=null)
		{
			for (Category category:categories)
			{
				if (category.isDefaultCategory() && category.getParent()==null && 
					user.equals(category.getUser()))
				{
					defaultCategory=category;
					break;
				}
			}
		}
		return defaultCategory;
	}
	
	/**
	 * Add derived categories sorted hierarchically to the list of sorted categories.
	 * @param categories List of available categories
	 * @param parent Parent category
	 * @param sortedCategories List of sorted categories where to add the derived categories
	 */
	private void addDerivedCategories(List<Category> categories,Category parent,List<Category> sortedCategories)
	{
		for (Category category:getChildsCategories(categories,parent))
		{
			// Check to avoid infinite recursivity (can occur due to corrupted data in DB)
			if (!sortedCategories.contains(category))
			{
				sortedCategories.add(category);
				addDerivedCategories(categories,category,sortedCategories);
			}
		}
	}
	
	/**
	 * Add derived categories identifiers sorted hierarchically to the list of sorted categories.
	 * @param categories List of available categories
	 * @param parent Parent category
	 * @param sortedCategoriesIds List of sorted categories identifiers where to add the derived categories
	 */
	private void addDerivedCategoriesIds(List<Category> categories,Category parent,List<Long> sortedCategoriesIds)
	{
		for (Category category:getChildsCategories(categories,parent))
		{
			// Check to avoid infinite recursivity (can occur due to corrupted data in DB)
			if (!sortedCategoriesIds.contains(Long.valueOf(category.getId())))
			{
				sortedCategoriesIds.add(Long.valueOf(category.getId()));
				addDerivedCategoriesIds(categories,category,sortedCategoriesIds);
			}
		}
	}
	
	/**
	 * Checks if category derives from indicated category.<br/><br/>
	 * Note that this method keep track of previously checked categories to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param operation Operation
	 * @param checkedCategories Categories already checked
	 * @param category Category to check
	 * @param from Category from we check if category derives
	 * @return true if category derives from indicated category, false otherwise (or in case of corrupted data)
	 * @throws ServiceException
	 */
	private boolean isDerivedFrom(Operation operation,List<Category> checkedCategories,Category category,
		Category from) throws ServiceException
	{
		boolean isDerived=false;
		if (category!=null && !checkedCategories.contains(category))
		{
			isDerived=category.equals(from);
			if (!isDerived)
			{
				checkedCategories.add(category);
				isDerived=isDerivedFrom(operation,checkedCategories,
					category.getParent()==null?null:getCategory(operation,category.getParent().getId()),from);
			}
		}
		else if (category==null)
		{
			isDerived=from==null;
		}
		return isDerived;
	}
	
	/**
	 * Checks if category derives from indicated category.<br/><br/>
	 * Note that this method keep track of previously checked categories to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param category Category to check
	 * @param from Category from we check if category derives
	 * @return true if category derives from indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isDerivedFrom(Category category,Category from) throws ServiceException
	{
		return isDerivedFrom(null,new ArrayList<Category>(),category,from);
	}
	
	/**
	 * Checks if category derives from indicated category.<br/><br/>
	 * Note that this method keep track of previously checked categories to avoid an infinite recursivity.
	 * <br/><br/>
	 * This case can occur if there is corrupted data within DB and then this method will break recursivity
	 * returning false.
	 * @param operation Operation
	 * @param category Category to check
	 * @param from Category from we check if category derives
	 * @return true if category derives from indicated category, false otherwise
	 * @throws ServiceException
	 */
	public boolean isDerivedFrom(Operation operation,Category category,Category from) throws ServiceException
	{
		return isDerivedFrom(operation,new ArrayList<Category>(),category,from);
	}
	
	/**
	 * @param categoryId Category identifier
	 * @param user User
	 * @param categoryType Category type to check or null if we don't want to check it
	 * @return List of idenfiers from categories derived from a category also including that 
	 * category identifier allowing to filter by category type
	 * @throws ServiceException
	 */
	public List<Long> getDerivedCategoriesIds(long categoryId,User user,CategoryType categoryType)
		throws ServiceException
	{
		return getDerivedCategoriesIds(null,categoryId,user,categoryType,true);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @param user User
	 * @param categoryType Category type to check or null if we don't want to check it
	 * @return List of idenfiers from categories derived from a category also including that 
	 * category identifier allowing to filter by category type
	 * @throws ServiceException
	 */
	public List<Long> getDerivedCategoriesIds(Operation operation,long categoryId,User user,
		CategoryType categoryType) throws ServiceException
	{
		return getDerivedCategoriesIds(operation,categoryId,user,categoryType,true);
	}
	
	/**
	 * @param categoryId Category identifier
	 * @param user User
	 * @param categoryType Category type to check or null if we don't want to check it
	 * @param checkVisibility Flag to indicate that if we are going to include only visible categories 
	 * within results (true) or we are going to include all categories (false)
	 * @return List of idenfiers from categories derived from a category also including that 
	 * category identifier allowing to filter by category type
	 * @throws ServiceException
	 */
	public List<Long> getDerivedCategoriesIds(long categoryId,User user,CategoryType categoryType,
		boolean checkVisibility) throws ServiceException
	{
		return getDerivedCategoriesIds(null,categoryId,user,categoryType,checkVisibility);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @param user User
	 * @param categoryType Category type to check or null if we don't want to check it
	 * @param checkVisibility Flag to indicate that if we are going to include only visible categories 
	 * within results (true) or we are going to include all categories (false)
	 * @return List of idenfiers from categories derived from a category also including that 
	 * category identifier allowing to filter by category type
	 * @throws ServiceException
	 */
	public List<Long> getDerivedCategoriesIds(Operation operation,long categoryId,User user,CategoryType categoryType,
		boolean checkVisibility) throws ServiceException
	{
		List<Long> derivedCategoriesIds=new ArrayList<Long>();
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get category
			Category from=getCategory(operation,categoryId);
			
			// Get public visibility if needed
			Visibility publicVisibility=
				checkVisibility?visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PUBLIC"):null;			
			
			CATEGORIES_DAO.setOperation(operation);
			for (Category category:CATEGORIES_DAO.getCategories(false,true,true,true))
			{
				category.setCategoryType(
					categoryTypesService.getCategoryType(operation,category.getCategoryType().getId()));
				category.setVisibility(
					visibilitiesService.getVisibility(operation,category.getVisibility().getId()));
				boolean typeOk=
					categoryType==null || 
					categoryTypesService.isDerivedFrom(operation,categoryType,category.getCategoryType()) ||
					categoryTypesService.isDerivedFrom(operation,category.getCategoryType(),categoryType);
				if (typeOk)
				{
					boolean visible=
						!checkVisibility || user==null || category.getUser().equals(user) || 
						category.getVisibility().isGlobal() ||
						category.getVisibility().getLevel()<=publicVisibility.getLevel(); 
					if (visible && isDerivedFrom(operation,category,from))
					{
						derivedCategoriesIds.add(Long.valueOf(category.getId()));
					}
				}
			}
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
		return derivedCategoriesIds;
	}
	
	/**
	 * @param categories List of available categories
	 * @param parent Parent category
	 * @return List of child categories
	 */
	private List<Category> getChildsCategories(List<Category> categories,Category parent)
	{
		List<Category> childsCategories=new ArrayList<Category>();
		for (Category category:categories)
		{
			if (parent.equals(category.getParent()))
			{
				childsCategories.add(category);
			}
		}
		return childsCategories;
	}
	
	/**
	 * @param categoryId Category identifier
	 * @return Localized category name
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryName(long categoryId) throws ServiceException
	{
		return getLocalizedCategoryName(null,categoryId);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @return Localized category name
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryName(Operation operation,long categoryId) throws ServiceException
	{
		String localizedCategoryName="";
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			localizedCategoryName=getLocalizedCategoryName(operation,getCategory(operation,categoryId));
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
		return localizedCategoryName;
	}
	
	//Obtiene el nombre localizado de la categoría
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return Localized category long name
	 * @throws ServiceException
	 */
	private String getLocalizedCategoryName(Operation operation,Category category) throws ServiceException
	{
		StringBuffer localizedCategoryName=new StringBuffer();
		if (category.isDefaultCategory())
		{
			FacesContext context=FacesContext.getCurrentInstance();
			UserSessionService userSessionService=
				(UserSessionService)context.getApplication().getELResolver().getValue(
				context.getELContext(),null,"userSessionService");
			if (userSessionService.getCurrentUser(operation).equals(category.getUser()))
			{
				localizedCategoryName.append(localizationService.getLocalizedMessage("MY_CATEGORIES"));
			}
			else
			{
				localizedCategoryName.append(localizationService.getLocalizedMessage("CATEGORIES_OF"));
				localizedCategoryName.append(' ');
				localizedCategoryName.append(category.getUser().getNick());
			}
		}
		else
		{
			localizedCategoryName.append(category.getName());
		}
		return localizedCategoryName.toString();
	}
	
	/**
	 * @param categoryId Category identifier
	 * @return Localized category long name
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(long categoryId) throws ServiceException
	{
		return getLocalizedCategoryLongName(null,categoryId);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @return Localized category long name
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(Operation operation,long categoryId) throws ServiceException
	{
		String localizedCategoryLongName=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get localized category long name
			localizedCategoryLongName=getLocalizedCategoryLongName(operation,getCategory(operation,categoryId));
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
		return localizedCategoryLongName;
	}
	
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return Localized category long name
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(Operation operation,Category category) throws ServiceException
	{
		StringBuffer localizedCategoryLongName=new StringBuffer();
		if (category.isDefaultCategory())
		{
			FacesContext context=FacesContext.getCurrentInstance();
			UserSessionService userSessionService=(UserSessionService)context.getApplication().getELResolver().
				getValue(context.getELContext(),null,"userSessionService");
			if (userSessionService.getCurrentUser(operation).equals(category.getUser()))
			{
				localizedCategoryLongName.append(localizationService.getLocalizedMessage("MY_CATEGORIES"));
			}
			else
			{
				localizedCategoryLongName.append(localizationService.getLocalizedMessage("CATEGORIES_OF"));
				localizedCategoryLongName.append(' ');
				localizedCategoryLongName.append(category.getUser().getNick());
			}
		}
		else if (category.getParent()==null)
		{
			localizedCategoryLongName.append(category.getName());
		}
		else
		{
			boolean singleOp=operation==null;
			try
			{
				if (singleOp)
				{
					// Start Hibernate operation
					operation=HibernateUtil.startOperation();
				}
				
				// Add localized parent category long name
				localizedCategoryLongName.append(
					getLocalizedCategoryLongName(operation,getCategory(operation,category.getParent().getId())));
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
			localizedCategoryLongName.append(" / ");
			localizedCategoryLongName.append(category.getName());
		}
		return localizedCategoryLongName.toString();
	}
	
	
	/**
	 * @param category Category
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(Category category,int maxLength)
	{
		return getLocalizedCategoryLongName(null,category.getId(),maxLength);
	}
	
	
	/**
	 * @param operation Operation
	 * @param category Category
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(Operation operation,Category category,int maxLength)
	{
		return getLocalizedCategoryLongName(operation,category.getId(),maxLength);
	}
	
	/**
	 * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(long categoryId,int maxLevels)
	{
		return getLocalizedCategoryLongName(null,categoryId,maxLevels);
	}
	
	/**
	 * @param operation Operation
	 * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
	 * @throws ServiceException
	 */
	public String getLocalizedCategoryLongName(Operation operation,long categoryId,int maxLength)
	{
		StringBuffer localizedCategoryLongName=new StringBuffer();
		String fullLocalizedCategoryLongName=getLocalizedCategoryLongName(operation,categoryId);
		if (maxLength>0 && fullLocalizedCategoryLongName.length()>maxLength)
		{
			int iCategoryLastName=fullLocalizedCategoryLongName.lastIndexOf('/');
			if (iCategoryLastName==-1)
			{
				localizedCategoryLongName.append(
					fullLocalizedCategoryLongName.substring(0,maxLength-"...".length()));
				localizedCategoryLongName.append("...");
			}
			else
			{
				int lastNameLength=fullLocalizedCategoryLongName.length()-iCategoryLastName;
				int iCategoryLastVisibleName=iCategoryLastName;
				while (iCategoryLastVisibleName>=0 && 
					iCategoryLastVisibleName+"/ ... ".length()+lastNameLength>maxLength)
				{
					iCategoryLastVisibleName=
						fullLocalizedCategoryLongName.lastIndexOf('/',iCategoryLastVisibleName-1);
				}
				if (iCategoryLastVisibleName==-1)
				{
					localizedCategoryLongName.append("... ");
				}
				else
				{
					localizedCategoryLongName.append(
						fullLocalizedCategoryLongName.substring(0,iCategoryLastVisibleName));
					localizedCategoryLongName.append("/ ... ");
				}
				localizedCategoryLongName.append(
					fullLocalizedCategoryLongName.substring(iCategoryLastName));
			}
		}
		else
		{
			localizedCategoryLongName.append(fullLocalizedCategoryLongName);
		}
		return localizedCategoryLongName.toString();
	}
	/*
	public String getLocalizedCategoryLongName(Operation operation,long categoryId,int maxLevels)
	{
		StringBuffer localizedCategoryLongName=new StringBuffer();
		String fullLocalizedCategoryLongName=getLocalizedCategoryLongName(operation,categoryId);
		if (maxLevels>0)
		{
			int iStartAbbreviate=-1;
			for (int iLevel=1;(iLevel==1 || iStartAbbreviate!=-1) && iLevel<maxLevels;iLevel++)
			{
				iStartAbbreviate=fullLocalizedCategoryLongName.indexOf('/',iStartAbbreviate+1);
			}
			if (iStartAbbreviate!=-1)
			{
				int iEndAbbreviate=fullLocalizedCategoryLongName.lastIndexOf('/');
				if (iStartAbbreviate<iEndAbbreviate)
				{
					localizedCategoryLongName.append(fullLocalizedCategoryLongName.substring(0,iStartAbbreviate));
					localizedCategoryLongName.append("/ ... ");
					localizedCategoryLongName.append(fullLocalizedCategoryLongName.substring(iEndAbbreviate));
				}
				else
				{
					localizedCategoryLongName.append(fullLocalizedCategoryLongName);
				}
			}
			else
			{
				localizedCategoryLongName.append(fullLocalizedCategoryLongName);
			}
		}
		else
		{
			localizedCategoryLongName.append(fullLocalizedCategoryLongName);
		}
		return localizedCategoryLongName.toString();
	}
	*/
	
	/**
	 * Sort categories by localized category long name.
	 * @param categories List of categories to sort
	 */
	public void sortCategoriesByLocalizedCategoryLongName(List<Category> categories)
	{
		sortCategoriesByLocalizedCategoryLongName(null,categories);
	}
	
	/**
	 * Sort categories by localized category long name.
	 * @param operation Operation
	 * @param categories List of categories to sort
	 */
	public void sortCategoriesByLocalizedCategoryLongName(Operation operation,List<Category> categories)
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Sort categories by localized category long name
			Collections.sort(categories,new CategoryLocalizedCategoryLongNameComparator(operation));
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
	
	/**
	 * Remove private categories of administrators from received categories list.<br/><br/>
	 * Note that visibility checks can be avoided passing 'checkVisibility' argument as false, this is intended
	 * to improve performance in several cases that we don't need to check visibility.
	 * @param operation Operation
	 * @param categories List of categories
	 * @param checkVisibility Flag to indicate if we want to perform visibility checks to be sure that we only
	 * remove private categories (true) or we don't want to perform that checks (false)
	 */
	private void removeAdminsPrivateCategories(Operation operation,List<Category> categories,boolean checkVisibility)
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Find categories to remove
			Visibility privateVisibility=
				checkVisibility?visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE"):null;
			Map<User,Boolean> admins=new HashMap<User,Boolean>();
			List<Category> categoriesToRemove=new ArrayList<Category>();
			for (Category category:categories)
			{
				if (!checkVisibility || category.getVisibility().getLevel()>=privateVisibility.getLevel())
				{
					User categoryUser=category.getUser();
					if (admins.containsKey(categoryUser))
					{
						if (admins.get(categoryUser).booleanValue())
						{
							categoriesToRemove.add(category);
						}
					}
					else
					{
						boolean isAdmin=permissionsService.isGranted(
							operation,categoryUser,"PERMISSION_NAVIGATION_ADMINISTRATION");
						admins.put(categoryUser,Boolean.valueOf(isAdmin));
						if (isAdmin)
						{
							categoriesToRemove.add(category);
						}
					}
				}
			}
			
			// Remove admins categories
			for (Category categoryToRemove:categoriesToRemove)
			{
				categories.remove(categoryToRemove);
			}
		}
		finally
		{
			if (singleOp)
			{
				//End operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	/**
	 * Remove private categories of users with permission to improve permissions overs their owned ones
	 * (superadmins) from received categories list.<br/><br/>
	 * Note that visibility checks can be avoided passing 'checkVisibility' argument as false, this is intended
	 * to improve performance in several cases that we don't need to check visibility.
	 * @param operation Operation
	 * @param categories List of categories
	 * @param checkVisibility Flag to indicate if we want to perform visibility checks to be sure that we only
	 * remove private categories (true) or we don't want to perform that checks (false)
	 */
	private void removeSuperadminsPrivateCategories(Operation operation,List<Category> categories,
		boolean checkVisibility)
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Find categories to remove
			Visibility privateVisibility=
				checkVisibility?visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE"):null;
			Map<User,Boolean> superadmins=new HashMap<User,Boolean>();
			List<Category> categoriesToRemove=new ArrayList<Category>();
			for (Category category:categories)
			{
				if (!checkVisibility || category.getVisibility().getLevel()>=privateVisibility.getLevel())
				{
					User categoryUser=category.getUser();
					if (superadmins.containsKey(categoryUser))
					{
						if (superadmins.get(categoryUser).booleanValue())
						{
							categoriesToRemove.add(category);
						}
					}
					else
					{
						boolean isSuperadmin=permissionsService.isGranted(operation,categoryUser,
							"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
						superadmins.put(categoryUser,Boolean.valueOf(isSuperadmin));
						if (isSuperadmin)
						{
							categoriesToRemove.add(category);
						}
					}
				}
			}
			
			// Remove superadmins categories
			for (Category categoryToRemove:categoriesToRemove)
			{
				categories.remove(categoryToRemove);
			}
		}
		finally
		{
			if (singleOp)
			{
				//End operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
	
	/**
	 * Remove private categories of administrators and users with permission to improve permissions overs 
	 * their owned ones (superadmins) from received categories list.<br/><br/>
	 * Note that visibility checks can be avoided passing 'checkVisibility' argument as false, this is intended
	 * to improve performance in several cases that we don't need to check visibility.
	 * @param operation Operation
	 * @param categories List of categories
	 * @param checkVisibility Flag to indicate if we want to perform visibility checks to be sure that we only
	 * remove private categories (true) or we don't want to perform that checks (false)
	 */
	private void removeAdminsAndSuperadminsPrivateCategories(Operation operation,List<Category> categories,
		boolean checkVisibility)
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Find categories to remove
			Visibility privateVisibility=
				checkVisibility?visibilitiesService.getVisibility(operation,"CATEGORY_VISIBILITY_PRIVATE"):null;
			Map<User,Boolean> admins=new HashMap<User,Boolean>();
			Map<User,Boolean> superadmins=new HashMap<User,Boolean>();
			List<Category> categoriesToRemove=new ArrayList<Category>();
			for (Category category:categories)
			{
				if (!checkVisibility || category.getVisibility().getLevel()>=privateVisibility.getLevel())
				{
					User categoryUser=category.getUser();
					if (admins.containsKey(categoryUser))
					{
						if (admins.get(categoryUser).booleanValue())
						{
							categoriesToRemove.add(category);
						}
					}
					else
					{
						boolean isAdmin=permissionsService.isGranted(
							operation,categoryUser,"PERMISSION_NAVIGATION_ADMINISTRATION");
						admins.put(categoryUser,Boolean.valueOf(isAdmin));
						if (isAdmin)
						{
							categoriesToRemove.add(category);
						}
					}
					if (superadmins.containsKey(categoryUser))
					{
						if (superadmins.get(categoryUser).booleanValue())
						{
							categoriesToRemove.add(category);
						}
					}
					else
					{
						boolean isSuperadmin=permissionsService.isGranted(operation,categoryUser,
							"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
						superadmins.put(categoryUser,Boolean.valueOf(isSuperadmin));
						if (isSuperadmin)
						{
							categoriesToRemove.add(category);
						}
					}
				}
			}
			
			// Remove admins and superadmins categories
			for (Category categoryToRemove:categoriesToRemove)
			{
				categories.remove(categoryToRemove);
			}
		}
		finally
		{
			if (singleOp)
			{
				//End operation
				HibernateUtil.endOperation(operation);
			}
		}
	}
}
