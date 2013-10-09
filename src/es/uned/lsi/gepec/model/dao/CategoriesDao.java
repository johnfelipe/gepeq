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
package es.uned.lsi.gepec.model.dao;

import java.util.List;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to categories data.
 */
public class CategoriesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda un objeto categoría en la bd
	/**
	 * Adds a new category to DB.
	 * @param category Category to add
	 * @return Category identifier
	 * @throws DaoException
	 */
	public long saveCategory(Category category) throws DaoException
	{
		long id=0L;
		try
        {
			startOperation();
			id=(Long) operation.session.save(category);
			if (singleOp)
			{
				operation.commit();
			}
        }
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
        return id;
	}
	
	//Actualiza una categoría en la bd
	/**
	 * Updates a category on DB.
	 * @param category Category to update
	 * @throws DaoException
	 */
	public void updateCategory(Category category) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(category);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation(); 
		}
	}
	
	//Elimina una categoría de la bd
	/**
	 * Deletes a category from DB.
	 * @param category Category to delete
	 * @throws DaoException
	 */
	public void deleteCategory(Category category) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(category);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	//Obtiene una categoría a partir de su id
	/**
	 * @param categoryId Category identifier
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return Category from DB
	 * @throws DaoException
	 */
	public Category getCategory(long categoryId,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		Category category=null;
		try
		{
			startOperation();
			category=(Category)operation.session.get(Category.class,categoryId);
			if (category!=null)
			{
				Hibernate.initialize(category.getParent());
				if (includeUser)
				{
					Hibernate.initialize(category.getUser());
				}
				if (includeCategoryType)
				{
					Hibernate.initialize(category.getCategoryType());
				}
				if (includeVisibility)
				{
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return category;
	}
	
	/**
	 * @param userId User identifier
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return Default category of an user from DB
	 * @throws DaoException
	 */
	public Category getDefaultCategory(long userId,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		Category category=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from Category c Where c.defaultCategory = 'true' And c.user = :userId");
			query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			category=(Category)query.uniqueResult();
			if (category!=null)
			{
				Hibernate.initialize(category.getParent());
				if (includeUser)
				{
					Hibernate.initialize(category.getUser());
				}
				if (includeCategoryType)
				{
					Hibernate.initialize(category.getCategoryType());
				}
				if (includeVisibility)
				{
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return category;
	}
	
	/**
	 * @param resourceId Resource identifier
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return Category from a resource
	 * @throws DaoException
	 */
	public Category getCategoryFromResourceId(long resourceId,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		Category category=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from Category c where c.id = (select r.category.id from Resource r where r.id = :resourceId)");
			query.setParameter("resourceId",Long.valueOf(resourceId),StandardBasicTypes.LONG);
			category=(Category)query.uniqueResult();
			if (category!=null)
			{
				Hibernate.initialize(category.getParent());
				if (includeUser)
				{
					Hibernate.initialize(category.getUser());
				}
				if (includeCategoryType)
				{
					Hibernate.initialize(category.getCategoryType());
				}
				if (includeVisibility)
				{
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return category;
	}
	
	/**
	 * @param questionId Question identifier
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return Category from a question
	 * @throws DaoException
	 */
	public Category getCategoryFromQuestionId(long questionId,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		Category category=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from Category c where c.id = (select q.category.id from Question q where q.id = :questionId)");
			query.setParameter("questionId",Long.valueOf(questionId),StandardBasicTypes.LONG);
			category=(Category)query.uniqueResult();
			if (category!=null)
			{
				Hibernate.initialize(category.getParent());
				if (includeUser)
				{
					Hibernate.initialize(category.getUser());
				}
				if (includeCategoryType)
				{
					Hibernate.initialize(category.getCategoryType());
				}
				if (includeVisibility)
				{
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return category;
	}
	
	/**
	 * @param testId Test identifier
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return Category from a test
	 * @throws DaoException
	 */
	public Category getCategoryFromTestId(long testId,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		Category category=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from Category c where c.id = (select t.category.id from Test t where t.id = :testId)");
			query.setParameter("testId",Long.valueOf(testId),StandardBasicTypes.LONG);
			category=(Category)query.uniqueResult();
			if (category!=null)
			{
				Hibernate.initialize(category.getParent());
				if (includeUser)
				{
					Hibernate.initialize(category.getUser());
				}
				if (includeCategoryType)
				{
					Hibernate.initialize(category.getCategoryType());
				}
				if (includeVisibility)
				{
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return category;
	}
	
	//TODO Is this method to get all categories without any filtering really needed?
	/**
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of all categories
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getCategories(boolean sortedByName,boolean includeUser,boolean includeCategoryType,
		boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Category c");
			if (sortedByName)
			{
				queryString.append(" Order by c.name,c.user.nick");
			}
			categories=operation.session.createQuery(queryString.toString()).list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	/**
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of all categories
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getChildCategories(long parentId,boolean sortedByName,boolean includeUser,
		boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Category c where c.parent");
			if (parentId>0L)
			{
				queryString.append(" = :parentId");
			}
			else
			{
				queryString.append(" is null");
			}
			if (sortedByName)
			{
				queryString.append(" order by c.name,c.user.nick");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (parentId>0L)
			{
				query.setParameter("parentId",Long.valueOf(parentId),StandardBasicTypes.LONG);
			}
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	//Obtiene la lista de categorías de un usuario
	/**
	 * @param userId User identifier
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of filtered categories of an user
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getUserCategories(long userId,boolean sortedByName,boolean includeUser,
		boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Category c Where c.user = :userId");
			if (sortedByName)
			{
				queryString.append(" Order by c.name");
			}
			Query query=operation.session.createQuery(queryString.toString());
			query.setParameter("userId",userId,StandardBasicTypes.LONG);
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	/**
	 * @param userId Filtering user identifier or 0 to get global categories from all users 
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of all global categories
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getGlobalCategories(long userId,boolean sortedByName,boolean includeUser,
		boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Category c");
			boolean addedFilter=false;
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" where c.user = :userId");
			}
			if (addedFilter)
			{
				queryString.append(" and c.visibility in (select v from Visibility v where v.global='true')");
			}
			else
			{
				queryString.append(" where c.visibility in (select v from Visibility v where v.global='true')");
			}
			if (sortedByName)
			{
				queryString.append(" order by c.name,c.user.nick");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	/**
	 * @param userId Filtering user identifier or 0 to get non global categories from all users 
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of all non global categories
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getNonGlobalCategories(long userId,boolean sortedByName,boolean includeUser,
		boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Category c");
			boolean addedFilter=false;
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" where c.user = :userId");
			}
			if (addedFilter)
			{
				queryString.append(" and c.visibility in (select v from Visibility v where v.global='false')");
			}
			else
			{
				queryString.append(" where c.visibility in (select v from Visibility v where v.global='false')");
			}
			if (sortedByName)
			{
				queryString.append(" order by c.name,c.user.nick");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	/**
	 * @param global Flag to filter results by global categories (true) or non-global (false)
	 * @param visibilityLevel Maximum visibility level to consider a category visible
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of visible categories filtered (global/not global visibility and with a visibility 
	 * level less or equal to the maximum allowed)
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getVisibleCategories(boolean global,int visibilityLevel,boolean sortedByName,
		boolean includeUser,boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer(
				"from Category c where c.visibility in (select v from Visibility v where v.global = :global and v.level <= :visibilityLevel)");
			if (sortedByName)
			{
				queryString.append(" order by c.name,c.user.nick");
			}
			Query query=operation.session.createQuery(queryString.toString());
			query.setParameter("global",Boolean.valueOf(global),StandardBasicTypes.BOOLEAN);
			query.setParameter("visibilityLevel",Integer.valueOf(visibilityLevel),StandardBasicTypes.INTEGER);
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	/**
	 * @param global Flag to filter results by global categories (true) or non-global (false)
	 * @param visibilityLevel Maximum visibility level to consider a category visible
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUser Flag to indicate if we need to initialize user
	 * @param includeCategoryType Flag to indicate if we need to initialize category type
	 * @param includeVisibility Flag to indicate if we need to initialize category visibility
	 * @return List of non visible categories filtered (global/not global visibility and with a visibility 
	 * level greater than the maximum allowed)
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Category> getNonVisibleCategories(boolean global,int visibilityLevel,boolean sortedByName,
		boolean includeUser,boolean includeCategoryType,boolean includeVisibility) throws DaoException
	{
		List<Category> categories=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer(
				"from Category c where c.visibility in (select v from Visibility v where v.global = :global and v.level > :visibilityLevel)");
			if (sortedByName)
			{
				queryString.append(" order by c.name,c.user.nick");
			}
			Query query=operation.session.createQuery(queryString.toString());
			query.setParameter("global",Boolean.valueOf(global),StandardBasicTypes.BOOLEAN);
			query.setParameter("visibilityLevel",Integer.valueOf(visibilityLevel),StandardBasicTypes.INTEGER);
			categories=query.list();
			if (includeUser)
			{
				if (includeCategoryType)
				{
					if (includeVisibility)
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
							Hibernate.initialize(category.getVisibility());
						}
					}
					else
					{
						for (Category category:categories)
						{
							Hibernate.initialize(category.getParent());
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							if (category.getCategoryType()!=null)
							{
								Hibernate.initialize(category.getCategoryType().getParent());
							}
						}
					}
				}
				else if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getUser());
					}
				}
			}
			else if (includeCategoryType)
			{
				if (includeVisibility)
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
						Hibernate.initialize(category.getVisibility());
					}
				}
				else
				{
					for (Category category:categories)
					{
						Hibernate.initialize(category.getParent());
						Hibernate.initialize(category.getCategoryType());
						if (category.getCategoryType()!=null)
						{
							Hibernate.initialize(category.getCategoryType().getParent());
						}
					}
				}
			}
			else if (includeVisibility)
			{
				for (Category category:categories)
				{
					Hibernate.initialize(category.getParent());
					Hibernate.initialize(category.getVisibility());
				}
			}
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categories;
	}
	
	//Obtiene el número de preguntas de una determinada categoría
	/**
	 * @param categoryId
	 * @return Number of questions within a category
	 * @throws DaoException
	 */
	public long getQuestionsCount(long categoryId) throws HibernateException
	{
		long questionsCount=0L;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"select count(q) from Question q Where q.category = :categoryId");
			query.setParameter("categoryId",Long.valueOf(categoryId),StandardBasicTypes.LONG);
			questionsCount=((Long)query.uniqueResult()).longValue();
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return questionsCount;
	}
	
	/**
	 * Checks if exists a category with the indicated identifier
	 * @param id Identifier
	 * @return true if exists a category with the indicated identifier, false otherwise
	 * @throws DaoException
	 */
	public boolean checkCategoryId(long id) throws DaoException
	{
		boolean categoryFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(c) from Category c Where c.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			categoryFound=((Long)query.uniqueResult()).longValue()==1L;
		}
		catch (HibernateException he)
		{
			handleException(he,!singleOp);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
		return categoryFound;
	}
	
	//Inicia una sesión e inicia una transacción contra el dbms
	/**
	 * Starts a session and transaction against DBMS.
	 * @throws DaoException
	 */
	private void startOperation() throws DaoException
	{
		try
		{
			if (operation==null)
			{
				operation=HibernateUtil.startOperation();
				singleOp=true;
			}
		}
		catch (HibernateException he)
		{
			operation=null;
			handleException(he,false);
			throw new DaoException(he);
		}
	}
	
	/**
	 * Sets a session and transaction against DBMS.
	 * @param operation Operation with started session and transaction
	 */
	public void setOperation(Operation operation)
	{
		this.operation=operation;
		singleOp=false;
	}
	
	/**
	 * Ends an operation, ending session and transaction against DBMS if this is a single operation.
	 * @throws DaoException
	 */
	private void endOperation() throws DaoException
	{
		try
		{
			if (singleOp)
			{
				HibernateUtil.endOperation(operation);
			}
		}
		catch (HibernateException he)
		{
			handleException(he,false);
			throw new DaoException(he);
		}
		finally
		{
			operation=null;
		}
	}
	
	//Maneja los errores producidos en la operación de acceso a datos
	/**
	 * Manage errors produced while accesing persistent data.<br/><br/>
	 * It also does a rollback.
	 * @param he Exception to handle
	 * @throws DaoException
	 */
	private void handleException(HibernateException he) throws DaoException
	{
		handleException(he,true);
	}
	
	/**
	 * Manage errors produced while accesing persistent data.
	 * @param he Exception to handle
	 * @param doRollback Flag to indicate to do a rollback
	 * @throws DaoException
	 */
	private void handleException(HibernateException he,boolean doRollback) throws DaoException 
	{ 
		String errorMessage=null;
		FacesContext facesContext=FacesContext.getCurrentInstance();
		if (facesContext==null)
		{
			errorMessage="Access error to the data layer";
		}
		else
		{
			ELContext elContext=facesContext.getELContext();
			LocalizationService localizationService=(LocalizationService)FacesContext.getCurrentInstance().
				getApplication().getELResolver().getValue(elContext,null,"localizationService");
			errorMessage=localizationService.getLocalizedMessage("ERROR_ACCESS_DATA_LAYER");
		}
		if (doRollback)
		{
			operation.rollback();
		}
		throw new DaoException(errorMessage,he); 
	}
}
