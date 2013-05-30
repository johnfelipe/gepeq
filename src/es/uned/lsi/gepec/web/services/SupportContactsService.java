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
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.SupportContactsDao;
import es.uned.lsi.gepec.model.entities.SupportContact;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages support contacts.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class SupportContactsService implements Serializable
{
	private final static SupportContactsDao SUPPORT_CONTACTS_DAO=new SupportContactsDao();
	
	/**
	 * @param id Support contact identifier
	 * @return Support contact
	 * @throws ServiceException
	 */
	public SupportContact getSupportContact(long id) throws ServiceException
	{
		return getSupportContact(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Support contact identifier
	 * @return Support contact
	 * @throws ServiceException
	 */
	public SupportContact getSupportContact(Operation operation,long id) throws ServiceException
	{
		SupportContact supportContact=null;
		try
		{
			SUPPORT_CONTACTS_DAO.setOperation(operation);
			supportContact=SUPPORT_CONTACTS_DAO.getSupportContact(id);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return supportContact; 
	}
	
	/**
	 * Updates a support contact.
	 * @param supportContact Support contact to update
	 * @throws ServiceException
	 */
	public void updateSupportContact(SupportContact supportContact) throws ServiceException
	{
		updateSupportContact(null,supportContact);
	}
	
	/**
	 * Updates a support contact.
	 * @param operation Operation
	 * @param supportContact Support contact to update
	 * @throws ServiceException
	 */
	public void updateSupportContact(Operation operation,SupportContact supportContact) throws ServiceException
	{
		try
		{
			SUPPORT_CONTACTS_DAO.setOperation(operation);
			SUPPORT_CONTACTS_DAO.updateSupportContact(supportContact);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Adds a new support contact.
	 * @param supportContact Support contact to add
	 * @throws ServiceException
	 */
	public void addSupportContact(SupportContact supportContact) throws ServiceException
	{
		addSupportContact(null,supportContact);
	}
	
	/**
	 * Adds a new support contact.
	 * @param operation Operation
	 * @param supportContact Support contact to add
	 * @throws ServiceException
	 */
	public void addSupportContact(Operation operation,SupportContact supportContact) throws ServiceException
	{
		try
		{
			SUPPORT_CONTACTS_DAO.setOperation(operation);
			SUPPORT_CONTACTS_DAO.saveSupportContact(supportContact);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Deletes a support contact.
	 * @param supportContact Support contact to delete
	 * @throws ServiceException
	 */
	public void deleteSupportContact(SupportContact supportContact) throws ServiceException
	{
		deleteSupportContact(null,supportContact);
	}
	
	/**
	 * Deletes a support contact.
	 * @param operation Operation
	 * @param supportContact Support contact to delete
	 * @throws ServiceException
	 */
	public void deleteSupportContact(Operation operation,SupportContact supportContact) throws ServiceException
	{
		try
		{
			SUPPORT_CONTACTS_DAO.setOperation(operation);
			SUPPORT_CONTACTS_DAO.deleteSupportContact(supportContact);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param test Test
	 * @return List of support contacts of a test
	 * @throws ServiceException
	 */
	public List<SupportContact> getSupportContacts(Test test) throws ServiceException
	{
		return getSupportContacts(null,test==null?0L:test.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of support contacts of a test
	 * @throws ServiceException
	 */
	public List<SupportContact> getSupportContacts(Operation operation,Test test) throws ServiceException
	{
		return getSupportContacts(operation,test==null?0L:test.getId());
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of support contacts of a test
	 * @throws ServiceException
	 */
	public List<SupportContact> getSupportContacts(long testId) throws ServiceException
	{
		return getSupportContacts(null,testId);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of support contacts of a test
	 * @throws ServiceException
	 */
	public List<SupportContact> getSupportContacts(Operation operation,long testId) throws ServiceException
	{
		List<SupportContact> supportContacts=null;
		try
		{
			SUPPORT_CONTACTS_DAO.setOperation(operation);
			supportContacts=SUPPORT_CONTACTS_DAO.getSupportContacts(testId,true);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
		return supportContacts;
	}
}
