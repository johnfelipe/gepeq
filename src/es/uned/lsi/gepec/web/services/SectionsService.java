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
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.SectionsDao;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages sections of a test.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class SectionsService implements Serializable
{
	private final static SectionsDao SECTIONS_DAO=new SectionsDao();
	
	@ManagedProperty(value="#{questionOrdersService}")
	private QuestionOrdersService questionOrdersService;
	
	public SectionsService()
	{
	}
	
	public void setQuestionOrdersService(QuestionOrdersService questionOrdersService)
	{
		this.questionOrdersService=questionOrdersService;
	}
	
	/**
	 * @param id Section identifier
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(long id) throws ServiceException
	{
		return getSection(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Section identifier
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(Operation operation,long id) throws ServiceException
	{
		Section section=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			SECTIONS_DAO.setOperation(operation);
			section=SECTIONS_DAO.getSection(id);
			
			if (section!=null)
			{
				// We need to get question references of this section
				section.setQuestionOrders(questionOrdersService.getQuestionOrders(operation,section));
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
		return section;
	}
	
	/**
	 * @param test Test
	 * @param order Section order (position within test)
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(Test test,int order) throws ServiceException
	{
		return getSection(null,test==null?0L:test.getId(),order);
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @param order Section order (position within test)
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(Operation operation,Test test,int order) throws ServiceException
	{
		return getSection(operation,test==null?0L:test.getId(),order);
	}
	
	/**
	 * @param testId Test identifier
	 * @param order Section order (position within test)
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(long testId,int order) throws ServiceException
	{
		return getSection(null,testId,order);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @param order Section order (position within test)
	 * @return Section
	 * @throws ServiceException
	 */
	public Section getSection(Operation operation,long testId,int order) throws ServiceException
	{
		Section section=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			SECTIONS_DAO.setOperation(operation);
			section=SECTIONS_DAO.getSection(testId,order);
			
			if (section!=null)
			{
				// We need to get question references of this section
				section.setQuestionOrders(questionOrdersService.getQuestionOrders(operation,section));
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
		return section;
	}
	
	/**
	 * Adds a new section.
	 * @param section Section to add
	 * @throws ServiceException
	 */
	public void addSection(Section section) throws ServiceException
	{
		addSection(null,section);
	}
	
	/**
	 * Adds a new section.
	 * @param operation Operation
	 * @param section Section to add
	 * @throws ServiceException
	 */
	public void addSection(Operation operation,Section section) throws ServiceException
	{
		try
		{
			SECTIONS_DAO.setOperation(operation);
			SECTIONS_DAO.saveSection(section);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * Updates a section.
	 * @param section Section to update
	 * @throws ServiceException
	 */
	public void updateSection(Section section) throws ServiceException
	{
		updateSection(null,section);
	}
	
	/**
	 * Updates a section.
	 * @param operation Operation
	 * @param section Section to update
	 * @throws ServiceException
	 */
	public void updateSection(Operation operation,Section section) throws ServiceException
	{
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			List<QuestionOrder> questionOrdersFromDB=questionOrdersService.getQuestionOrders(operation,section);
			
			// We delete removed question references from DB 
			for (QuestionOrder questionOrder:questionOrdersFromDB)
			{
				if (!section.getQuestionOrders().contains(questionOrder))
				{
					questionOrdersService.deleteQuestionOrder(operation,questionOrder);
				}
			}
			
			// We add/update question references on DB
			for (QuestionOrder questionOrder:section.getQuestionOrders())
			{
				if (questionOrdersFromDB.contains(questionOrder))
				{
					QuestionOrder questionOrderFromDB=
						questionOrdersFromDB.get(questionOrdersFromDB.indexOf(questionOrder));
					questionOrderFromDB.setFromOtherQuestionOrder(questionOrder);
					questionOrdersService.updateQuestionOrder(operation,questionOrderFromDB);
				}
				else
				{
					questionOrdersService.addQuestionOrder(operation,questionOrder);
				}
			}
			
			// We get section from DB
			SECTIONS_DAO.setOperation(operation);
			Section sectionFromDB=SECTIONS_DAO.getSection(section.getId());
			
			// We update section on DB
			sectionFromDB.setFromOtherSection(section);
			SECTIONS_DAO.setOperation(operation);
			SECTIONS_DAO.updateSection(section);
			
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
	 * Deletes a section.
	 * @param section Section to delete
	 * @throws ServiceException
	 */
	public void deleteSection(Section section) throws ServiceException
	{
		deleteSection(null,section);
	}
	
	/**
	 * Deletes a section.
	 * @param operation Operation
	 * @param section Section to delete
	 * @throws ServiceException
	 */
	public void deleteSection(Operation operation,Section section) throws ServiceException
	{
		try
		{
			SECTIONS_DAO.setOperation(operation);
			SECTIONS_DAO.deleteSection(section);
		}
		catch (DaoException de)
		{
			throw new ServiceException(de.getMessage(),de);
		}
	}
	
	/**
	 * @param test Test
	 * @return List of sections of a test
	 * @throws ServiceException
	 */
	public List<Section> getSections(Test test) throws ServiceException
	{
		return getSections(null,test==null?0L:test.getId());
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return List of sections of a test
	 * @throws ServiceException
	 */
	public List<Section> getSections(Operation operation,Test test) throws ServiceException
	{
		return getSections(operation,test==null?0L:test.getId());
	}
	
	/**
	 * @param testId Test identifier
	 * @return List of sections of a test
	 * @throws ServiceException
	 */
	public List<Section> getSections(long testId) throws ServiceException
	{
		return getSections(null,testId);
	}
	
	/**
	 * @param operation Operation
	 * @param testId Test identifier
	 * @return List of sections of a test
	 * @throws ServiceException
	 */
	public List<Section> getSections(Operation operation,long testId) throws ServiceException
	{
		List<Section> sections=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			SECTIONS_DAO.setOperation(operation);
			sections=SECTIONS_DAO.getSections(testId);
			
			// We need to get question references of sections
			for (Section section:sections)
			{
				section.setQuestionOrders(questionOrdersService.getQuestionOrders(operation,section));
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
		return sections;
	}
}
