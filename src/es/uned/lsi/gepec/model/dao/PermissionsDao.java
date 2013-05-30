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

import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to permissions.
 */
public class PermissionsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new permission to DB.
	 * @param permission Permission to add
	 * @return Permission identifier
	 * @throws DaoException
	 */
	public long savePermission(Permission permission) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(permission)).longValue();
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
	 * Updates a permission on DB.
	 * @param permission Permission to update
	 * @throws DaoException
	 */
	public void updatePermission(Permission permission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(permission);
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
	 * Deletes a permission from DB.
	 * @param permission Permission to delete
	 * @throws DaoException
	 */
	public void deletePermission(Permission permission) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(permission);
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
	 * @param id Permission identifier
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission from DB
	 * @throws DaoException
	 */
	public Permission getPermission(long id,boolean includePermissionType) throws DaoException
	{
		Permission permission=null;
		try
		{
			startOperation();
			permission=(Permission)operation.session.get(Permission.class,id);
			if (permission!=null && includePermissionType)
			{
				Hibernate.initialize(permission.getPermissionType());
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
		return permission;
	}
	
	/**
	 * @param name Permission name
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return Permission from DB
	 * @throws DaoException
	 */
	public Permission getPermission(String name,boolean includePermissionType) throws DaoException
	{
		Permission permission=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from Permission p where p.name = :name");
			query.setParameter("name",name,StandardBasicTypes.STRING);
			permission=(Permission)query.uniqueResult();
			if (permission!=null && includePermissionType)
			{
				Hibernate.initialize(permission.getPermissionType());
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
		return permission;
	}
	
	/**
	 * @param includePermissionType Flag to indicate if we need to initialize permission type
	 * @return List of all permissions
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<Permission> getPermissions(boolean includePermissionType) throws DaoException
	{
		List<Permission> permissions=null;
		try
		{
			startOperation();
			permissions=operation.session.createQuery("from Permission p").list();
			if (includePermissionType)
			{
				for (Permission permission:permissions)
				{
					Hibernate.initialize(permission.getPermissionType());
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
		return permissions;
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
