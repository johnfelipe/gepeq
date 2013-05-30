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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import es.uned.lsi.gepec.model.entities.AddressType;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to user feedback types data.
 */
public class AddressTypesDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new address type to DB.
	 * @param addressType Address type
	 * @return Address type identifier
	 * @throws DaoException
	 */
	public long saveAddressType(AddressType addressType) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(addressType)).longValue();
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
	 * Updates an address type on DB.
	 * @param addressType Address type
	 * @throws DaoException
	 */
	public void updateAddressType(AddressType addressType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(addressType);
			if (singleOp)
			{
				operation.commit();
			}
		}
		catch (HibernateException he)
		{
			handleException(he);
			throw new DaoException();
		}
		finally
		{
			endOperation();
		}
	}
	
	/**
	 * Deletes an address type from DB.
	 * @param addressType Address type
	 * @throws DaoException
	 */
	public void deleteAddressType(AddressType addressType) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(addressType);
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
	 * @param id Address type identifier
	 * @return Address type from DB
	 * @throws DaoException
	 */
	public AddressType getAddressType(long id) throws DaoException
	{
		AddressType addressType=null;
		try
		{
			startOperation();
			addressType=(AddressType)operation.session.get(AddressType.class,id);
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
		return addressType;
	}
	
	/**
	 * @param type Address type's type (string)
	 * @param subtype Address type's subtype (string)
	 * @return Address type from DB
	 * @throws DaoException
	 */
	public AddressType getAddressType(String type,String subtype) throws DaoException
	{
		AddressType addressType=null;
		try
		{
			startOperation();
			Query query=null;
			query=operation.session.createQuery("from AddressType a where a.type = :type and a.subtype = :subtype");
			query.setParameter("type",type,StandardBasicTypes.STRING);
			query.setParameter("subtype",subtype==null?"":subtype,StandardBasicTypes.STRING);
			addressType=(AddressType)query.uniqueResult();
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
		return addressType;
	}
	
	/**
	 * @return List of all address types
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<AddressType> getAddressTypes() throws DaoException
	{
		List<AddressType> addressTypes=null;
		try
		{
			startOperation();
			addressTypes=operation.session.createQuery("from AddressType order by id").list();
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
		return addressTypes;
	}
	
	/**
	 * @param type Address type's type (string)
	 * @return List of address types filtered by type
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<AddressType> getAddressTypes(String type)
	{
		List<AddressType> addressTypes=null;
		try
		{
			startOperation();
			Query query=operation.session.createQuery("from AddressType a where a.type = :type order by a.id");
			query.setParameter("type",type,StandardBasicTypes.STRING);
			addressTypes=query.list();
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
		return addressTypes;
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
