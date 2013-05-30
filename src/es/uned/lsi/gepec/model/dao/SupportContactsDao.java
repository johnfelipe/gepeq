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

import es.uned.lsi.gepec.model.entities.SupportContact;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;

/**
 * Manages access to support contacts data.
 */
public class SupportContactsDao
{
	private Operation operation=null;
	private boolean singleOp;
	
	/**
	 * Adds a new support contact to DB.
	 * @param supportContact Support contact
	 * @return Support contact identifier
	 * @throws DaoException
	 */
	public long saveSupportContact(SupportContact supportContact) throws DaoException
	{
		long id=0L;
		try
		{
			startOperation();
			id=((Long)operation.session.save(supportContact)).longValue();
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
	 * Updates a support contact on DB.
	 * @param supportContact Support contact
	 * @throws DaoException
	 */
	public void updateSupportContact(SupportContact supportContact) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.update(supportContact);
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
	 * Deletes a support contact from DB.
	 * @param supportContact Support contact
	 * @throws DaoException
	 */
	public void deleteSupportContact(SupportContact supportContact) throws DaoException
	{
		try
		{
			startOperation();
			operation.session.delete(supportContact);
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
	 * @param id Support contact identifier
	 * @return Support contact from DB
	 * @throws DaoException
	 */
	public SupportContact getSupportContact(long id) throws DaoException
	{
		return getSupportContact(id,true);
	}
	
	/**
	 * @param id Support contact identifier
	 * @param includeAddressType true to include address type information, false otherwise
	 * @return Support contact from DB
	 * @throws DaoException
	 */
	public SupportContact getSupportContact(long id,boolean includeAddressType) throws DaoException
	{
		SupportContact supportContact=null;
		try
		{
			startOperation();
			supportContact=(SupportContact)operation.session.get(SupportContact.class,id);
			if (supportContact!=null)
			{
				if (includeAddressType)
				{
					Hibernate.initialize(supportContact.getAddressType());
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
		return supportContact;
	}
	
	/**
	 * @return List of support contacts of a test
	 * @param testId Test identifier
	 * @param includeAddressType true to include address type information, false otherwise
	 * @throws DaoException
	 */
	@SuppressWarnings("unchecked")
	public List<SupportContact> getSupportContacts(long testId,boolean includeAddressType) throws DaoException
	{
		List<SupportContact> supportContacts=null;
		try
		{
			startOperation();
			StringBuffer queryString=new StringBuffer("from SupportContact s");
			if (testId>0L)
			{
				queryString.append(" where s.test = :testId");
			}
			queryString.append(" order by s.supportContact");
			Query query=operation.session.createQuery(queryString.toString());
			if (testId>0L)
			{
				query.setParameter("testId",Long.valueOf(testId),StandardBasicTypes.LONG);
			}
			supportContacts=query.list();
			if (includeAddressType)
			{
				for (SupportContact supportContact:supportContacts)
				{
					Hibernate.initialize(supportContact.getAddressType());
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
		return supportContacts;
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
