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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;  

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

/**
 * Manages access to tests data.
 */
public class TestsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda una prueba en la bd
	/**
	 * Adds a new test to DB.
	 * @param test Test to add
	 * @return Test identifier
	 * @throws DaoException
	 */
	public long saveTest(Test test) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(test)).longValue();
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleExpection(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
        return id;
	}
	
	//Actualiza una prueba en la bd
	/**
	 * Updates a test on DB.
	 * @param test Test to update
	 * @throws DaoException
	 */
	public void updateTest(Test test) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(test);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleExpection(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	//Elimina una prueba de la bd
	/**
	 * Deletes a test from DB.
	 * @param test Test to delete
	 * @throws DaoException
	 */
	public void deleteTest(Test test) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(test);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleExpection(he);
			throw new DaoException(he);
		}
		finally
		{
			endOperation();
		}
	}
	
	//Obtiene una prueba a partir de su id
	//param includeSections  Se cargan las secciones en la consulta
	/**
	 * @param id Test identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeAssessement Flag to indicate if we need to inialize assessement
	 * @param includeScoreType Flag to indicate if we need to initialize score type
	 * @param includeNavLocation Flag to indicate if we need to initialize navigation location
	 * @param includeRedoQuestion Flag to indicate if we need to initialize value of property 'redoQuestion'
	 * @return Test from DB
	 * @throws DaoException
	 */
	public Test getTest(long id,boolean includeUsers,boolean includeCategory,boolean includeAssessement,
		boolean includeScoreType,boolean includeNavLocation,boolean includeRedoQuestion) throws DaoException
	{
		Test test=null;
		try
		{
			startOperation();
			test=(Test)operation.session.get(Test.class,id);
			if (test!=null)
			{
				if (includeUsers)
				{
					Hibernate.initialize(test.getCreatedBy());
					Hibernate.initialize(test.getModifiedBy());
					if (includeCategory)
					{
						Hibernate.initialize(test.getCategory());
						if (test.getCategory()!=null)
						{
							Hibernate.initialize(test.getCategory().getUser());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
						}
					}
				}
				else if (includeCategory)
				{
					Hibernate.initialize(test.getCategory());
					Hibernate.initialize(test.getCategory().getCategoryType());
					Hibernate.initialize(test.getCategory().getVisibility());
				}
				if (includeAssessement)
				{
					Hibernate.initialize(test.getAssessement());
				}
				if (includeScoreType)
				{
					Hibernate.initialize(test.getScoreType());
				}
				if (includeNavLocation)
				{
					Hibernate.initialize(test.getNavLocation());
				}
				if (includeRedoQuestion)
				{
					Hibernate.initialize(test.getRedoQuestion());
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
		return test;
	}
	
	//Obtiene la lista de pruebas
	/**
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeAssessement Flag to indicate if we need to inialize assessement
	 * @param includeScoreType Flag to indicate if we need to initialize score type
	 * @param includeNavLocation Flag to indicate if we need to initialize navigation location
	 * @param includeRedoQuestion Flag to indicate if we need to initialize value of property 'redoQuestion'
	 * @return List of all tests
	 * @throws DaoException
	 */
	public List<Test> getTests(boolean includeUsers,boolean includeCategory,boolean includeAssessement,
		boolean includeScoreType,boolean includeNavLocation,boolean includeRedoQuestion) 
		throws DaoException
	{
		return getTests(0L,0L,includeUsers,includeCategory,includeAssessement,includeScoreType,
			includeNavLocation,includeRedoQuestion);
	}
	
	//Obtiene la lista de pruebas de un usuario
	/**
	 * @param userId User identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeAssessement Flag to indicate if we need to inialize assessement
	 * @param includeScoreType Flag to indicate if we need to initialize score type
	 * @param includeNavLocation Flag to indicate if we need to initialize navigation location
	 * @param includeRedoQuestion Flag to indicate if we need to initialize value of property 'redoQuestion'
	 * @return List of tests of an user
	 * @throws DaoException
	 */
	public List<Test> getTests(long userId,boolean includeUsers,boolean includeCategory,
		boolean includeAssessement,boolean includeScoreType,boolean includeNavLocation,
		boolean includeRedoQuestion) throws DaoException
	{
		return getTests(userId,0L,includeUsers,includeCategory,includeAssessement,includeScoreType,
			includeNavLocation,includeRedoQuestion);
	}
	
	/**
	 * @param userId User identifier
     * @param categoryId Filtering category identifier or 0 to get tests from all categories
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeAssessement Flag to indicate if we need to inialize assessement
	 * @param includeScoreType Flag to indicate if we need to initialize score type
	 * @param includeNavLocation Flag to indicate if we need to initialize navigation location
	 * @param includeRedoQuestion Flag to indicate if we need to initialize value of property 'redoQuestion'
	 * @return List of tests of an user
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Test> getTests(long userId,long categoryId,boolean includeUsers,boolean includeCategory,
		boolean includeAssessement,boolean includeScoreType,boolean includeNavLocation,
		boolean includeRedoQuestion) throws DaoException
	{
		List<Test> tests=null;
		boolean addedFilter=false;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Test t");
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" Where t.createdBy = :userId");
			}
			if (categoryId>0L)
			{
				if (addedFilter)
				{
					queryString.append(" And t.category = :categoryId");
				}
				else
				{
					queryString.append(" Where t.category = :categoryId");
				}
			}
			queryString.append(" Order by t.timeModified desc");
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			if (categoryId>0L)
			{
				query.setParameter("categoryId",Long.valueOf(categoryId),StandardBasicTypes.LONG);
			}
			tests=query.list();
			
			if (includeUsers)
			{
				if (includeCategory)
				{
					if (includeAssessement)
					{
						if (includeScoreType)
						{
							if (includeNavLocation)
							{
								if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
									}
								}
							}
							else if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
								}
							}
						}
						else if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getAssessement());
							}
						}
					}
					else if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Category category=test.getCategory();
							Hibernate.initialize(category);
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							Hibernate.initialize(category.getVisibility());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Category category=test.getCategory();
							Hibernate.initialize(category);
							Hibernate.initialize(category.getUser());
							Hibernate.initialize(category.getCategoryType());
							Hibernate.initialize(category.getVisibility());
						}
					}
				}
				else if (includeAssessement)
				{
					if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getAssessement());
						}
					}
				}
				else if (includeScoreType)
				{
					if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getScoreType());
						}
					}
				}
				else if (includeNavLocation)
				{
					if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getNavLocation());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getNavLocation());
						}
					}
				}
				else if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getCreatedBy());
						Hibernate.initialize(test.getModifiedBy());
						Hibernate.initialize(test.getRedoQuestion());
					}
				}
				else
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getCreatedBy());
						Hibernate.initialize(test.getModifiedBy());
					}
				}
			}
			else if (includeCategory)
			{
				if (includeAssessement)
				{
					if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getAssessement());
						}
					}
				}
				else if (includeScoreType)
				{
					if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getScoreType());
						}
					}
				}
				else if (includeNavLocation)
				{
					if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getNavLocation());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getNavLocation());
						}
					}
				}
				else if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getCategory());
						Hibernate.initialize(test.getCategory().getCategoryType());
						Hibernate.initialize(test.getCategory().getVisibility());
						Hibernate.initialize(test.getRedoQuestion());
					}
				}
				else
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getCategory());
						Hibernate.initialize(test.getCategory().getCategoryType());
						Hibernate.initialize(test.getCategory().getVisibility());
					}
				}
			}
			else if (includeAssessement)
			{
				if (includeScoreType)
				{
					if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getScoreType());
						}
					}
				}
				else if (includeNavLocation)
				{
					if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getNavLocation());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getNavLocation());
						}
					}
				}
				else if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getAssessement());
						Hibernate.initialize(test.getRedoQuestion());
					}
				}
				else
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getAssessement());
					}
				}
			}
			else if (includeScoreType)
			{
				if (includeNavLocation)
				{
					if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getNavLocation());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getNavLocation());
						}
					}
				}
				else if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getScoreType());
						Hibernate.initialize(test.getRedoQuestion());
					}
				}
				else
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getScoreType());
					}
				}
			}
			else if (includeNavLocation)
			{
				if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getNavLocation());
						Hibernate.initialize(test.getRedoQuestion());
					}
				}
				else
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getNavLocation());
					}
				}
			}
			else if (includeRedoQuestion)
			{
				for (Test test:tests)
				{
					Hibernate.initialize(test.getRedoQuestion());
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
		return tests;
	}
	
	/**
	 * @param userId User identifier
     * @param categoriesIds Filtering categories identifiers or null to get tests of all categories
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeAssessement Flag to indicate if we need to inialize assessement
	 * @param includeScoreType Flag to indicate if we need to initialize score type
	 * @param includeNavLocation Flag to indicate if we need to initialize navigation location
	 * @param includeRedoQuestion Flag to indicate if we need to initialize value of property 'redoQuestion'
	 * @return List of tests of an user
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Test> getTests(long userId,List<Long> categoriesIds,boolean includeUsers,boolean includeCategory,
		boolean includeAssessement,boolean includeScoreType,boolean includeNavLocation,
		boolean includeRedoQuestion) throws DaoException
	{
		List<Test> tests=null;
		if (categoriesIds!=null && categoriesIds.isEmpty())
		{
			tests=new ArrayList<Test>();
		}
		else if (categoriesIds!=null && categoriesIds.size()==1)
		{
			tests=getTests(userId,categoriesIds.get(0),includeUsers,includeCategory,includeAssessement,
				includeScoreType,includeNavLocation,includeRedoQuestion);
		}
		else
		{
			boolean addedFilter=false;
			try
			{
				startOperation();
				StringBuffer queryString=new StringBuffer("from Test t");
				if (userId>0L)
				{
					addedFilter=true;
					queryString.append(" Where t.createdBy = :userId");
					queryString.append(userId);
				}
				if (categoriesIds!=null)
				{
					if (addedFilter)
					{
						queryString.append(" And t.category In (:categoriesIds)");
					}
					else
					{
						queryString.append(" Where t.category In (:categoriesIds)");
					}
				}
				queryString.append(" Order by t.timeModified desc");
				Query query=operation.session.createQuery(queryString.toString());
				if (userId>0L)
				{
					query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
				}
				if (categoriesIds!=null)
				{
					query.setParameterList("categoriesIds",categoriesIds,StandardBasicTypes.LONG);
				}
				tests=query.list();
				if (includeUsers)
				{
					if (includeCategory)
					{
						if (includeAssessement)
						{
							if (includeScoreType)
							{
								if (includeNavLocation)
								{
									if (includeRedoQuestion)
									{
										for (Test test:tests)
										{
											Hibernate.initialize(test.getCreatedBy());
											Hibernate.initialize(test.getModifiedBy());
											Category category=test.getCategory();
											Hibernate.initialize(category);
											Hibernate.initialize(category.getUser());
											Hibernate.initialize(category.getCategoryType());
											Hibernate.initialize(category.getVisibility());
											Hibernate.initialize(test.getAssessement());
											Hibernate.initialize(test.getScoreType());
											Hibernate.initialize(test.getNavLocation());
											Hibernate.initialize(test.getRedoQuestion());
										}
									}
									else
									{
										for (Test test:tests)
										{
											Hibernate.initialize(test.getCreatedBy());
											Hibernate.initialize(test.getModifiedBy());
											Category category=test.getCategory();
											Hibernate.initialize(category);
											Hibernate.initialize(category.getUser());
											Hibernate.initialize(category.getCategoryType());
											Hibernate.initialize(category.getVisibility());
											Hibernate.initialize(test.getAssessement());
											Hibernate.initialize(test.getScoreType());
											Hibernate.initialize(test.getNavLocation());
										}
									}
								}
								else if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
									}
								}
							}
							else if (includeNavLocation)
							{
								if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getNavLocation());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getNavLocation());
									}
								}
							}
							else if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getAssessement());
								}
							}
						}
						else if (includeScoreType)
						{
							if (includeNavLocation)
							{
								if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Category category=test.getCategory();
										Hibernate.initialize(category);
										Hibernate.initialize(category.getUser());
										Hibernate.initialize(category.getCategoryType());
										Hibernate.initialize(category.getVisibility());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
									}
								}
							}
							else if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getScoreType());
								}
							}
						}
						else if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Category category=test.getCategory();
									Hibernate.initialize(category);
									Hibernate.initialize(category.getUser());
									Hibernate.initialize(category.getCategoryType());
									Hibernate.initialize(category.getVisibility());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Category category=test.getCategory();
								Hibernate.initialize(category);
								Hibernate.initialize(category.getUser());
								Hibernate.initialize(category.getCategoryType());
								Hibernate.initialize(category.getVisibility());
							}
						}
					}
					else if (includeAssessement)
					{
						if (includeScoreType)
						{
							if (includeNavLocation)
							{
								if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCreatedBy());
										Hibernate.initialize(test.getModifiedBy());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
									}
								}
							}
							else if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
								}
							}
						}
						else if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getAssessement());
							}
						}
					}
					else if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCreatedBy());
									Hibernate.initialize(test.getModifiedBy());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCreatedBy());
								Hibernate.initialize(test.getModifiedBy());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCreatedBy());
							Hibernate.initialize(test.getModifiedBy());
						}
					}
				}
				else if (includeCategory)
				{
					if (includeAssessement)
					{
						if (includeScoreType)
						{
							if (includeNavLocation)
							{
								if (includeRedoQuestion)
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCategory());
										Hibernate.initialize(test.getCategory().getCategoryType());
										Hibernate.initialize(test.getCategory().getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
										Hibernate.initialize(test.getRedoQuestion());
									}
								}
								else
								{
									for (Test test:tests)
									{
										Hibernate.initialize(test.getCategory());
										Hibernate.initialize(test.getCategory().getCategoryType());
										Hibernate.initialize(test.getCategory().getVisibility());
										Hibernate.initialize(test.getAssessement());
										Hibernate.initialize(test.getScoreType());
										Hibernate.initialize(test.getNavLocation());
									}
								}
							}
							else if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
								}
							}
						}
						else if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getAssessement());
							}
						}
					}
					else if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getCategory());
									Hibernate.initialize(test.getCategory().getCategoryType());
									Hibernate.initialize(test.getCategory().getVisibility());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getCategory());
								Hibernate.initialize(test.getCategory().getCategoryType());
								Hibernate.initialize(test.getCategory().getVisibility());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getCategory());
							Hibernate.initialize(test.getCategory().getCategoryType());
							Hibernate.initialize(test.getCategory().getVisibility());
						}
					}
				}
				else if (includeAssessement)
				{
					if (includeScoreType)
					{
						if (includeNavLocation)
						{
							if (includeRedoQuestion)
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
									Hibernate.initialize(test.getRedoQuestion());
								}
							}
							else
							{
								for (Test test:tests)
								{
									Hibernate.initialize(test.getAssessement());
									Hibernate.initialize(test.getScoreType());
									Hibernate.initialize(test.getNavLocation());
								}
							}
						}
						else if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getScoreType());
							}
						}
					}
					else if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getAssessement());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getAssessement());
						}
					}
				}
				else if (includeScoreType)
				{
					if (includeNavLocation)
					{
						if (includeRedoQuestion)
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
								Hibernate.initialize(test.getRedoQuestion());
							}
						}
						else
						{
							for (Test test:tests)
							{
								Hibernate.initialize(test.getScoreType());
								Hibernate.initialize(test.getNavLocation());
							}
						}
					}
					else if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getScoreType());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getScoreType());
						}
					}
				}
				else if (includeNavLocation)
				{
					if (includeRedoQuestion)
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getNavLocation());
							Hibernate.initialize(test.getRedoQuestion());
						}
					}
					else
					{
						for (Test test:tests)
						{
							Hibernate.initialize(test.getNavLocation());
						}
					}
				}
				else if (includeRedoQuestion)
				{
					for (Test test:tests)
					{
						Hibernate.initialize(test.getRedoQuestion());
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
		}
		return tests;
	}
	
	/**
	 * Checks if exists a test with the indicated identifier.
	 * @param id Identifier
	 * @return true if exists a test with the indicated identifier, false otherwise
	 * @throws DaoException
	 */
	public boolean checkTestId(long id) throws DaoException
	{
		boolean testFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(t) from Test t Where t.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			testFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return testFound;
	}
	
	/**
	 * @param id Identifier
	 * @return Last time the test has been modified
	 * @throws DaoException
	 */
	public Date getTimeModifiedFromTestId(long id) throws DaoException
	{
		Date timeModified=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select t.timeModified from Test t Where t.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			timeModified=(Date)query.uniqueResult();
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
		return timeModified;
	}
	
	//Inicia una sesión e inicia una transacción contra el dbms
	/**
	 * Starts a session and transaction against DBMS if needed.
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
	private void handleExpection(HibernateException he) throws DaoException
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
