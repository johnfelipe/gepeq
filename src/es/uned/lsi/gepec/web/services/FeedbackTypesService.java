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
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.FeedbackTypesDao;
import es.uned.lsi.gepec.model.entities.FeedbackType;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages feedback types.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class FeedbackTypesService implements Serializable
{
	private final static FeedbackTypesDao FEEDBACK_TYPES_DAO=new FeedbackTypesDao();
	
	private final static Map<Long,FeedbackType> FEEDBACK_TYPES_CACHED_BY_ID=new HashMap<Long,FeedbackType>();
	private final static Map<String,FeedbackType> FEEDBACK_TYPES_CACHED_BY_TYPE=new HashMap<String,FeedbackType>();
	
	public FeedbackTypesService()
	{
	}
	
	/**
	 * @param id Feedback type's identifier
	 * @return Feedback type
	 * @throws ServiceException
	 */
	public FeedbackType getFeedbackType(long id) throws ServiceException
	{
		return getFeedbackType(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Feedback type's identifier
	 * @return Feedback type
	 * @throws ServiceException
	 */
	public FeedbackType getFeedbackType(Operation operation,long id) throws ServiceException
	{
		FeedbackType feedbackType=null;
		FeedbackType feedbackTypeFromCache=FEEDBACK_TYPES_CACHED_BY_ID.get(Long.valueOf(id));
		if (feedbackTypeFromCache==null)
		{
			try
			{
				FEEDBACK_TYPES_DAO.setOperation(operation);
				feedbackTypeFromCache=FEEDBACK_TYPES_DAO.getFeedbackType(id);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (feedbackTypeFromCache!=null)
			{
				FEEDBACK_TYPES_CACHED_BY_ID.put(Long.valueOf(id),feedbackTypeFromCache);
				FEEDBACK_TYPES_CACHED_BY_TYPE.put(feedbackTypeFromCache.getType(),feedbackTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached feedback type so we return a copy
		if (feedbackTypeFromCache!=null)
		{
			feedbackType=new FeedbackType();
			feedbackType.setFromOtherFeedbackType(feedbackTypeFromCache);
		}
		return feedbackType;
	}
	
	/**
	 * @param type Feedback type string
	 * @return Feedback type
	 * @throws ServiceException
	 */
	public FeedbackType getFeedbackType(String type) throws ServiceException
	{
		return getFeedbackType(null,type);
	}
	
	/**
	 * @param operation Operation
	 * @param type Feedback type string
	 * @return Feedback type
	 * @throws ServiceException
	 */
	public FeedbackType getFeedbackType(Operation operation,String type) throws ServiceException
	{
		FeedbackType feedbackType=null;
		FeedbackType feedbackTypeFromCache=FEEDBACK_TYPES_CACHED_BY_TYPE.get(type);
		if (feedbackTypeFromCache==null)
		{
			try
			{
				FEEDBACK_TYPES_DAO.setOperation(operation);
				feedbackTypeFromCache=FEEDBACK_TYPES_DAO.getFeedbackType(type);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (feedbackTypeFromCache!=null)
			{
				FEEDBACK_TYPES_CACHED_BY_ID.put(Long.valueOf(feedbackTypeFromCache.getId()),feedbackTypeFromCache);
				FEEDBACK_TYPES_CACHED_BY_TYPE.put(type,feedbackTypeFromCache);
			}
		}
		// We don't want caller accessing directly to a cached feedback type so we return a copy
		if (feedbackTypeFromCache!=null)
		{
			feedbackType=new FeedbackType();
			feedbackType.setFromOtherFeedbackType(feedbackTypeFromCache);
		}
		return feedbackType;
	}
	
	/**
	 * Reset cached feedback types.
	 */
	public void resetCachedFeedbackTypes()
	{
		FEEDBACK_TYPES_CACHED_BY_ID.clear();
		FEEDBACK_TYPES_CACHED_BY_TYPE.clear();
	}
}
