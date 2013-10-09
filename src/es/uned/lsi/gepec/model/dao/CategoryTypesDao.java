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

import es.uned.lsi.gepec.model.entities.CategoryType;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to user category types data.
 */
public class CategoryTypesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new category type to DB.
	 * @param categoryType Category type
	 * @return Category type identifier
	 * @throws DaoException
	 */
	public long saveCategoryType(CategoryType categoryType) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(categoryType)).longValue();
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
	
	/**
	 * Updates a category type on DB.
	 * @param categoryType Category type
	 * @throws DaoException
	 */
	public void updateCategoryType(CategoryType categoryType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(categoryType);
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
	
	/**
	 * Deletes a category type from DB.
	 * @param categoryType Category type
	 * @throws DaoException
	 */
	public void deleteCategoryType(CategoryType categoryType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(categoryType);
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
	
	/**
	 * @param id Category type identifier
	 * @return Category type from DB
	 * @throws DaoException
	 */
	public CategoryType getCategoryType(long id) throws DaoException
	{
		CategoryType categoryType=null;
		try
		{
			startOperation();
			categoryType=(CategoryType)operation.session.get(CategoryType.class,id);
			if (categoryType!=null)
			{
				Hibernate.initialize(categoryType.getParent());
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
		return categoryType;
	}
	
	/**
	 * @param type Category type string
	 * @return Category type from DB
	 * @throws DaoException
	 */
	public CategoryType getCategoryType(String type) throws DaoException
	{
		CategoryType categoryType=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from CategoryType c where c.type = :type");
			query.setParameter("type",type,StandardBasicTypes.STRING);
			categoryType=(CategoryType)query.uniqueResult();
			if (categoryType!=null)
			{
				Hibernate.initialize(categoryType.getParent());
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
		return categoryType;
	}
	
	/**
	 * @param categoryId Category identifier
	 * @return Category type from a category
	 * @throws DaoException
	 */
	public CategoryType getCategoryTypeFromCategoryId(long categoryId) throws DaoException
	{
		CategoryType categoryType=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery(
				"from CategoryType ct where ct.id = (select c.categoryType.id from Category c where c.id = :categoryId)");
			query.setParameter("categoryId",Long.valueOf(categoryId),StandardBasicTypes.LONG);
			categoryType=(CategoryType)query.uniqueResult();
			if (categoryType!=null)
			{
				Hibernate.initialize(categoryType.getParent());
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
		return categoryType;
	}
	
	/**
	 * @return List of all category types
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<CategoryType> getCategoryTypes() throws DaoException
	{
		List<CategoryType> categoryTypes=null;
		try
		{
			startOperation();
			categoryTypes=operation.session.createQuery("from CategoryType").list();
			for (CategoryType categoryType:categoryTypes)
			{
				Hibernate.initialize(categoryType.getParent());
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
		return categoryTypes;
	}
	
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
