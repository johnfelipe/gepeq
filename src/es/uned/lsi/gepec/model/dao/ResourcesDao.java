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
import java.util.List;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.model.entities.Resource;

/**
 * Manages access to resources data. 
 */
public class ResourcesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	//Guarda un recurso en la bd
	/**
	 * Adds a new resource to DB.
	 * @param resource Resource to add
	 * @return Resource identifier
	 * @throws DaoException
	 */
	public long saveResource(Resource resource) throws DaoException
	{
		long id=0L;  
		try
		{
			startOperation();
			id=(Long)operation.session.save(resource);
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
	
	//Actualiza un recurso en la bd
	/**
	 * Updates a resource on DB.
	 * @param resource Resource to update
	 * @throws DaoException
	 */
	public void updateResource(Resource resource) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(resource);
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
	
	//Elimina un recurso de la bd
	/**
	 * Deletes a resource from DB.
	 * @param resource Resource to delete
	 * @throws DaoException
	 */
	public void deleteResource(Resource resource) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(resource);
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
	
	//Obtiene un recurso a partir de su id
	/**
	 * @param resourceId Resource identifier
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeCopyright Flag to indicate if we need to initialize copyright
	 * @return Resource from DB
	 * @throws DaoException
	 */
	public Resource getResource(long resourceId,boolean includeUsers,boolean includeCategory,boolean includeCopyright) 
		throws DaoException
	{
		Resource resource=null;
		try
		{
			startOperation();
			resource=(Resource)operation.session.get(Resource.class,resourceId);
			if (resource!=null)
			{
				if (includeUsers)
				{
					Hibernate.initialize(resource.getUser());
					if (includeCategory)
					{
						Hibernate.initialize(resource.getCategory());
						if (resource.getCategory()!=null)
						{
							Hibernate.initialize(resource.getCategory().getUser());
							Hibernate.initialize(resource.getCategory().getCategoryType());
							Hibernate.initialize(resource.getCategory().getVisibility());
						}
					}
				}
				else if (includeCategory)
				{
					Hibernate.initialize(resource.getCategory());
					if (resource.getCategory()!=null)
					{
						Hibernate.initialize(resource.getCategory().getCategoryType());
						Hibernate.initialize(resource.getCategory().getVisibility());
					}
				}
				if (includeCopyright)
				{
					Hibernate.initialize(resource.getCopyright());
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
		return resource;
	}
	
	//Obtiene la lista de recursos de un usuario
	/**
	 * @param userId User identifier
     * @param categoryId Filtering category identifier or 0 to get resources from all categories
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeCopyright Flag to indicate if we need to initialize copyright
	 * @return List of resources that can be filtered by user and MIME type and sorted by name
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Resource> getResources(long userId,long categoryId,String mimeType,long copyrightId,
		boolean sortedByName,boolean includeUsers,boolean includeCategory,boolean includeCopyright) 
		throws DaoException
	{
		List<Resource> resources=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from Resource r");
			boolean addedFilter=false;
			if (userId>0L)
			{
				addedFilter=true;
				queryString.append(" Where r.user = :userId");
			}
			if (categoryId>0L)
			{
				if (addedFilter)
				{
					queryString.append(" And r.category = :categoryId");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where r.category = :categoryId");
				}
			}
			if (mimeType!=null && !mimeType.equals(""))
			{
				if (mimeType.indexOf('*')==-1)
				{
					if (addedFilter)
					{
						queryString.append(" And r.mimeType = :mimeType");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where r.mimeType = :mimeType");
					}
				}
				else
				{
					mimeType=mimeType.replace('*','%');
					if (addedFilter)
					{
						queryString.append(" And r.mimeType like :mimeType");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where r.mimeType like :mimeType");
					}
				}
			}
			if (copyrightId>0L)
			{
				if (addedFilter)
				{
					queryString.append(" And r.copyright = :copyrightId");
				}
				else
				{
					addedFilter=true;
					queryString.append(" Where r.copyright = :copyrightId");
				}
			}
			if (sortedByName)
			{
				queryString.append(" Order by r.name");
			}
			Query query=operation.session.createQuery(queryString.toString());
			if (userId>0L)
			{
				query.setParameter("userId",Long.valueOf(userId),StandardBasicTypes.LONG);
			}
			if (categoryId>0L)
			{
				query.setParameter("categoryId",Long.valueOf(categoryId),StandardBasicTypes.LONG);
			}
			if (mimeType!=null && !mimeType.equals(""))
			{
				query.setParameter("mimeType",mimeType,StandardBasicTypes.STRING);
			}
			if (copyrightId>0L)
			{
				query.setParameter("copyrightId",Long.valueOf(copyrightId),StandardBasicTypes.LONG);
			}
			resources=query.list();
			if (includeUsers)
			{
				if (includeCategory)
				{
					if (includeCopyright)
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getUser());
							Hibernate.initialize(resource.getCategory());
							if (resource.getCategory()!=null)
							{
								Hibernate.initialize(resource.getCategory().getUser());
								Hibernate.initialize(resource.getCategory().getCategoryType());
								Hibernate.initialize(resource.getCategory().getVisibility());
							}
							Hibernate.initialize(resource.getCopyright());
						}
					}
					else
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getUser());
							Hibernate.initialize(resource.getCategory());
							if (resource.getCategory()!=null)
							{
								Hibernate.initialize(resource.getCategory().getUser());
								Hibernate.initialize(resource.getCategory().getCategoryType());
								Hibernate.initialize(resource.getCategory().getVisibility());
							}
						}
					}
				}
				else if (includeCopyright)
				{
					for (Resource resource:resources)
					{
						Hibernate.initialize(resource.getUser());
						Hibernate.initialize(resource.getCopyright());
					}
				}
				else
				{
					for (Resource resource:resources)
					{
						Hibernate.initialize(resource.getUser());
					}
				}
			}
			else if (includeCategory)
			{
				if (includeCopyright)
				{
					for (Resource resource:resources)
					{
						Hibernate.initialize(resource.getCategory());
						if (resource.getCategory()!=null)
						{
							Hibernate.initialize(resource.getCategory().getCategoryType());
							Hibernate.initialize(resource.getCategory().getVisibility());
						}
						Hibernate.initialize(resource.getCopyright());
					}
				}
				else
				{
					for (Resource resource:resources)
					{
						Hibernate.initialize(resource.getCategory());
						if (resource.getCategory()!=null)
						{
							Hibernate.initialize(resource.getCategory().getCategoryType());
							Hibernate.initialize(resource.getCategory().getVisibility());
						}
					}
				}
			}
			else if (includeCopyright)
			{
				for (Resource resource:resources)
				{
					Hibernate.initialize(resource.getCopyright());
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
		return resources;
	}
	
	/**
	 * @param userId User identifier
     * @param categoriesIds Filtering categories identifiers or null to get resources of all categories
	 * @param mimeType Filtering MIME type or empty string to get resources with all MIME types
	 * @param copyrightId Filtering copyright identifier or 0 to get resources with all copyrights
	 * @param sortedByName Flag to indicate if we want the results sorted by category name
	 * @param includeUsers Flag to indicate if we need to initialize users
	 * @param includeCategory Flag to indicate if we need to initialize category
	 * @param includeCopyright Flag to indicate if we need to initialize copyright
	 * @return List of resources that can be filtered by user and MIME type and sorted by name
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Resource> getResources(long userId,List<Long> categoriesIds,String mimeType,long copyrightId,
		boolean sortedByName,boolean includeUsers,boolean includeCategory,boolean includeCopyright) 
		throws DaoException
	{
		List<Resource> resources=null;
		if (categoriesIds!=null && categoriesIds.isEmpty())
		{
			resources=new ArrayList<Resource>();
		}
		else if (categoriesIds!=null && categoriesIds.size()==1)
		{
			resources=getResources(userId,categoriesIds.get(0),mimeType,copyrightId,sortedByName,includeUsers,
				includeCategory,includeCopyright);
		}
		else
		{
			try
			{
				startOperation();
				StringBuffer queryString=new StringBuffer("from Resource r");
				boolean addedFilter=false;
				if (userId>0L)
				{
					addedFilter=true;
					queryString.append(" Where r.user = :userId");
				}
				if (categoriesIds!=null)
				{
					if (addedFilter)
					{
						queryString.append(" And r.category In (:categoriesIds)");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where r.category In (:categoriesIds)");
					}
				}
				if (mimeType!=null && !mimeType.equals(""))
				{
					if (mimeType.indexOf('*')==-1)
					{
						if (addedFilter)
						{
							queryString.append(" And r.mimeType = :mimeType");
						}
						else
						{
							addedFilter=true;
							queryString.append(" Where r.mimeType = :mimeType");
						}
					}
					else
					{
						mimeType=mimeType.replace('*','%');
						if (addedFilter)
						{
							queryString.append(" And r.mimeType like :mimeType");
						}
						else
						{
							addedFilter=true;
							queryString.append(" Where r.mimeType like :mimeType");
						}
					}
				}
				if (copyrightId>0L)
				{
					if (addedFilter)
					{
						queryString.append(" And r.copyright = :copyrightId");
					}
					else
					{
						addedFilter=true;
						queryString.append(" Where r.copyright = :copyrightId");
					}
				}
				if (sortedByName)
				{
					queryString.append(" Order by r.name");
				}
				Query query=operation.session.createQuery(queryString.toString());
				if (userId>0L)
				{
					query.setParameter("userId", Long.valueOf(userId),StandardBasicTypes.LONG);
				}
				if (categoriesIds!=null)
				{
					query.setParameterList("categoriesIds",categoriesIds,StandardBasicTypes.LONG);
				}
				if (mimeType!=null && !mimeType.equals(""))
				{
					query.setParameter("mimeType",mimeType,StandardBasicTypes.STRING);
				}
				if (copyrightId>0L)
				{
					query.setParameter("copyrightId", Long.valueOf(copyrightId),StandardBasicTypes.LONG);
				}
				resources=query.list();
				if (includeUsers)
				{
					if (includeCategory)
					{
						if (includeCopyright)
						{
							for (Resource resource:resources)
							{
								Hibernate.initialize(resource.getUser());
								Hibernate.initialize(resource.getCategory());
								if (resource.getCategory()!=null)
								{
									Hibernate.initialize(resource.getCategory().getUser());
									Hibernate.initialize(resource.getCategory().getCategoryType());
									Hibernate.initialize(resource.getCategory().getVisibility());
								}
								Hibernate.initialize(resource.getCopyright());
							}
						}
						else
						{
							for (Resource resource:resources)
							{
								Hibernate.initialize(resource.getUser());
								Hibernate.initialize(resource.getCategory());
								if (resource.getCategory()!=null)
								{
									Hibernate.initialize(resource.getCategory().getUser());
									Hibernate.initialize(resource.getCategory().getCategoryType());
									Hibernate.initialize(resource.getCategory().getVisibility());
								}
							}
						}
					}
					else if (includeCopyright)
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getUser());
							Hibernate.initialize(resource.getCopyright());
						}
					}
					else
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getUser());
						}
					}
				}
				else if (includeCategory)
				{
					if (includeCopyright)
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getCategory());
							if (resource.getCategory()!=null)
							{
								Hibernate.initialize(resource.getCategory().getCategoryType());
								Hibernate.initialize(resource.getCategory().getVisibility());
							}
							Hibernate.initialize(resource.getCopyright());
						}
					}
					else
					{
						for (Resource resource:resources)
						{
							Hibernate.initialize(resource.getCategory());
							if (resource.getCategory()!=null)
							{
								Hibernate.initialize(resource.getCategory().getCategoryType());
								Hibernate.initialize(resource.getCategory().getVisibility());
							}
						}
					}
				}
				else if (includeCopyright)
				{
					for (Resource resource:resources)
					{
						Hibernate.initialize(resource.getCopyright());
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
		return resources;
	}
	
	/**
	 * Checks if exists a resource with the indicated identifier
	 * @param id Identifier
	 * @return true if exists a resource with the indicated identifier, false otherwise
	 * @throws DaoException
	 */
	public boolean checkResourceId(long id) throws DaoException
	{
		boolean resourceFound=false;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("select count(r) from Resource r Where r.id = :id");
			query.setParameter("id",Long.valueOf(id),StandardBasicTypes.LONG);
			resourceFound=((Long)query.uniqueResult()).longValue()==1L;
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
		return resourceFound;
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
