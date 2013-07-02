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
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.om.axis.OmTnProxy;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages question releases.
 */
@ManagedBean(eager=true)
@ApplicationScoped
@SuppressWarnings("serial")
public class QuestionReleasesService implements Serializable
{
	private final static int QUESTION_RELEASE_METADATA_PACKAGE_NAME=0;
	private final static int QUESTION_RELEASE_METADATA_PUBLISHER=1;
	private final static int QUESTION_RELEASE_METADATA_RELEASE_DATE=2;
	private final static int QUESTION_RELEASE_METADATA_START_DATE=3;
	private final static int QUESTION_RELEASE_METADATA_CLOSE_DATE=4;
	private final static int QUESTION_RELEASE_METADATA_DELETE_DATE=5;
	private final static int QUESTION_RELEASE_METADATA_WARNING_DATE=6;
	private final static int QUESTION_RELEASE_METADATA_ALL_USERS_ALLOWED=7;
	
	private final static String QUESTION_RELEASE_METADATA_USERGROUPS_SEPARATOR="@";
	
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	
	public QuestionReleasesService()
	{
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	/**
	 * @return List of questions published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<QuestionRelease> getQuestionsReleases() throws ServiceException
	{
		return getQuestionsReleases(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of questions published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<QuestionRelease> getQuestionsReleases(Operation operation) throws ServiceException
	{
		List<QuestionRelease> questionsReleases=new ArrayList<QuestionRelease>();
		String questionsReleasesMetadata=getQuestionsReleasesMetadata();
		if (!"".equals(questionsReleasesMetadata))
		{
			boolean singleOp=operation==null;
			try
			{
				if (singleOp)
				{
					// Start Hibernate operation
					operation=HibernateUtil.startOperation();
				}
				
				int i=QUESTION_RELEASE_METADATA_PACKAGE_NAME;
				Question question=null;
				User publisher=null;
				DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date releaseDate=null;
				Date startDate=null;
				Date closeDate=null;
				Date deleteDate=null;
				Map<Long,Question> questions=new HashMap<Long,Question>();
				Map<String,User> users=new HashMap<String,User>();
				for (String questionReleaseMetadata:questionsReleasesMetadata.split(";",-1))
				{
					switch (i%(QUESTION_RELEASE_METADATA_DELETE_DATE+1))
					{
						case QUESTION_RELEASE_METADATA_PACKAGE_NAME:
							int iStartQId=questionReleaseMetadata.indexOf('q');
							long questionId=Long.parseLong(questionReleaseMetadata.substring(iStartQId+1));
							if (questions.containsKey(Long.valueOf(questionId)))
							{
								question=questions.get(Long.valueOf(questionId));
							}
							else
							{
								question=questionsService.getQuestion(operation,questionId);
								if (question!=null)
								{
									questions.put(Long.valueOf(questionId),question);
								}
							}
							if (question!=null && question.getCreatedBy()!=null)
							{
								long userId=Long.parseLong(questionReleaseMetadata.substring(1,iStartQId-1));
								if (question.getCreatedBy().getId()!=userId)
								{
									question=null;
								}
							}
							break;
						case QUESTION_RELEASE_METADATA_PUBLISHER:
							if ("".equals(questionReleaseMetadata))
							{
								publisher=null;
							}
							else
							{
								if (users.containsKey(questionReleaseMetadata))
								{
									publisher=users.get(questionReleaseMetadata);
								}
								else
								{
									publisher=usersService.getUserFromOucu(operation,questionReleaseMetadata);
									if (publisher!=null)
									{
										users.put(questionReleaseMetadata,publisher);
									}
								}
							}
							break;
						case QUESTION_RELEASE_METADATA_RELEASE_DATE:
							if (!"".equals(questionReleaseMetadata))
							{
								try
								{
									releaseDate=dateFormat.parse(questionReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									question=null;
								}
							}
							break;
						case QUESTION_RELEASE_METADATA_START_DATE:
							if (!"".equals(questionReleaseMetadata))
							{
								try
								{
									startDate=dateFormat.parse(questionReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									question=null;
								}
							}
							break;
						case QUESTION_RELEASE_METADATA_CLOSE_DATE:
							if (!"".equals(questionReleaseMetadata))
							{
								try
								{
									closeDate=dateFormat.parse(questionReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									question=null;
								}
							}
							break;
						case QUESTION_RELEASE_METADATA_DELETE_DATE:
							if (!"".equals(questionReleaseMetadata))
							{
								try
								{
									deleteDate=dateFormat.parse(questionReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									question=null;
								}
							}
							if (question!=null)
							{
								questionsReleases.add(new QuestionRelease(
									question,publisher,releaseDate,startDate,closeDate,deleteDate));
							}
					}
					i++;
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
		}
		return questionsReleases;
	}
	
	/**
	 * @param id Question identifier
	 * @return Question published within Test Navigator production environment, null otherwise
	 * @throws ServiceException
	 */
	public QuestionRelease getQuestionRelease(long id) throws ServiceException
	{
		return getQuestionRelease(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Question identifier
	 * @return Question published within Test Navigator production environment, null otherwise
	 * @throws ServiceException
	 */
	public QuestionRelease getQuestionRelease(Operation operation,long id) throws ServiceException
	{
		QuestionRelease questionRelease=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			Question question=questionsService.getQuestion(operation,id);
			if (question!=null)
			{
				String allQuestionReleaseMetadata=getQuestionReleaseMetadata(question.getPackage());
				if (!"".equals(allQuestionReleaseMetadata))
				{
					int i=QUESTION_RELEASE_METADATA_PUBLISHER;
					User publisher=null;
					Date releaseDate=null;
					boolean allUsersAllowed=true;
					Date startDate=null;
					Date closeDate=null;
					Date warningDate=null;
					Date deleteDate=null;
					List<User> users=new ArrayList<User>();
					List<String> userGroups=new ArrayList<String>();
					boolean usersDone=false;
					DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					for (String questionReleaseMetadata:allQuestionReleaseMetadata.split(";",-1))
					{
						switch (i)
						{
							case QUESTION_RELEASE_METADATA_PUBLISHER:
								if ("".equals(questionReleaseMetadata))
								{
									publisher=null;
								}
								else
								{
									publisher=usersService.getUserFromOucu(operation,questionReleaseMetadata);
								}
								break;
							case QUESTION_RELEASE_METADATA_RELEASE_DATE:
								if (!"".equals(questionReleaseMetadata))
								{
									try
									{
										releaseDate=dateFormat.parse(questionReleaseMetadata);
									} 
									catch (ParseException pe)
									{
										question=null;
									}
								}
								break;
							case QUESTION_RELEASE_METADATA_START_DATE:
								if (!"".equals(questionReleaseMetadata))
								{
									try
									{
										startDate=dateFormat.parse(questionReleaseMetadata);
									} 
									catch (ParseException pe)
									{
										question=null;
									}
								}
								break;
							case QUESTION_RELEASE_METADATA_CLOSE_DATE:
								if (!"".equals(questionReleaseMetadata))
								{
									try
									{
										closeDate=dateFormat.parse(questionReleaseMetadata);
									} 
									catch (ParseException pe)
									{
										question=null;
									}
								}
								break;
							case QUESTION_RELEASE_METADATA_DELETE_DATE:
								if (!"".equals(questionReleaseMetadata))
								{
									try
									{
										deleteDate=dateFormat.parse(questionReleaseMetadata);
									}
									catch (ParseException pe)
									{
										question=null;
									}
								}
								break;
							case QUESTION_RELEASE_METADATA_WARNING_DATE:
								if (!"".equals(questionReleaseMetadata))
								{
									try
									{
										warningDate=dateFormat.parse(questionReleaseMetadata);
									} 
									catch (ParseException pe)
									{
										question=null;
									}
								}
								break;
							case QUESTION_RELEASE_METADATA_ALL_USERS_ALLOWED:
								allUsersAllowed=Boolean.valueOf(questionReleaseMetadata).booleanValue();
								break;
							default:
								if (usersDone)
								{
									if (!allUsersAllowed && !userGroups.contains(questionReleaseMetadata))
									{
										userGroups.add(questionReleaseMetadata);
									}
								}
								else
								{
									if (QUESTION_RELEASE_METADATA_USERGROUPS_SEPARATOR.equals(
										questionReleaseMetadata))
									{
										usersDone=true;
									}
									else if (!allUsersAllowed)
									{
										if (publisher!=null && questionReleaseMetadata.equals(publisher.getOucu()))
										{
											if (!users.contains(publisher))
											{
												users.add(publisher);
											}
										}
										else
										{
											User user=null;
											for (User u:users)
											{
												if (questionReleaseMetadata.equals(u.getOucu()))
												{
													user=u;
													break;
												}
											}
											if (user==null)
											{
												user=usersService.getUserFromOucu(operation,questionReleaseMetadata);
												if (user!=null)
												{
													users.add(user);
												}
											}
										}
									}
								}
						}
						if (question==null)
						{
							break;
						}
						else
						{
							i++;
						}
					}
					if (question!=null)
					{
						questionRelease=new QuestionRelease(question,publisher,releaseDate,startDate,closeDate,
							deleteDate,warningDate,allUsersAllowed,users,userGroups);
					}
				}
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
		return questionRelease;
	}
	
	/**
	 * @return String with metadata of all questions compatible with GEPEQ published within 
	 * Test Navigator production environment
	 */
	private String getQuestionsReleasesMetadata()
	{
		String questionsReleasesMetadata="";
		String omTnProURL=configurationService.getOmTnProUrl();
		if (omTnProURL!=null)
		{
			StringBuffer omTnProWsURL=new StringBuffer(omTnProURL);
			if (omTnProURL.charAt(omTnProURL.length()-1)!='/')
			{
				omTnProWsURL.append('/');
			}
			omTnProWsURL.append("services/OmTn");
			OmTnProxy omTnProWs=new OmTnProxy();
			omTnProWs.setEndpoint(omTnProWsURL.toString());
			try
			{
				questionsReleasesMetadata=omTnProWs.getQuestionsReleasesMetadata();
			}
			catch (RemoteException re)
			{
				questionsReleasesMetadata="";
			}
		}
		return questionsReleasesMetadata;
	}
	
	/**
	 * @param packageName Package name
	 * @return String with metadata of the question with the indicated package name 
	 * if it has been published within Test Navigator production environment, empty string otherwise
	 */
	private String getQuestionReleaseMetadata(String packageName)
	{
		String questionReleaseMetadata="";
		String omTnProURL=configurationService.getOmTnProUrl();
		if (omTnProURL!=null)
		{
			StringBuffer omTnProWsURL=new StringBuffer(omTnProURL);
			if (omTnProURL.charAt(omTnProURL.length()-1)!='/')
			{
				omTnProWsURL.append('/');
			}
			omTnProWsURL.append("services/OmTn");
			OmTnProxy omTnProWs=new OmTnProxy();
			omTnProWs.setEndpoint(omTnProWsURL.toString());
			try
			{
				questionReleaseMetadata=omTnProWs.getQuestionReleaseMetadata(packageName);
			}
			catch (RemoteException re)
			{
				questionReleaseMetadata="";
			}
		}
		return questionReleaseMetadata;
	}
}
