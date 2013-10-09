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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.entities.Assessement;
import es.uned.lsi.gepec.model.entities.NavLocation;
import es.uned.lsi.gepec.model.entities.RedoQuestionValue;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.om.axis.OmTnProxy;
import es.uned.lsi.gepec.util.EmailValidator;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;

/**
 * Manages test releases.
 */
@ManagedBean(eager=true)
@ApplicationScoped
@SuppressWarnings("serial")
public class TestReleasesService implements Serializable
{
	private final static int TEST_RELEASE_METADATA_SIGNATURE=0;
	private final static int TEST_RELEASE_METADATA_VERSION=1;
	private final static int TEST_RELEASE_METADATA_PUBLISHER=2;
	private final static int TEST_RELEASE_METADATA_RELEASE_DATE=3;
	private final static int TEST_RELEASE_METADATA_START_DATE=4;
	private final static int TEST_RELEASE_METADATA_CLOSE_DATE=5;
	private final static int TEST_RELEASE_METADATA_DELETE_DATE=6;
	private final static int TEST_RELEASE_METADATA_WARNING_DATE=7;
	private final static int TEST_RELEASE_METADATA_FEEDBACK_DATE=8;
	private final static int TEST_RELEASE_METADATA_ASSESSEMENT=9;
	private final static int TEST_RELEASE_METADATA_ALL_USERS_ALLOWED=10;
	private final static int TEST_RELEASE_METADATA_ALLOW_ADMIN_REPORTS=11;
	private final static int TEST_RELEASE_METADATA_FREE_SUMMARY=12;
	private final static int TEST_RELEASE_METADATA_FREE_STOP=13;
	private final static int TEST_RELEASE_METADATA_SUMMARY_QUESTIONS=14;
	private final static int TEST_RELEASE_METADATA_SUMMARY_SCORES=15;
	private final static int TEST_RELEASE_METADATA_SUMMARY_ATTEMPTS=16;
	private final static int TEST_RELEASE_METADATA_NAVIGATION=17;
	private final static int TEST_RELEASE_METADATA_NAV_LOCATION=18;
	private final static int TEST_RELEASE_METADATA_REDO_QUESTION=19;
	private final static int TEST_RELEASE_METADATA_REDO_TEST=20;
	private final static int TEST_RELEASE_METADATA_SUPPORT_CONTACTS=21;
	private final static int TEST_RELEASE_METADATA_EVALUATORS=22;
	
	private final static int TEST_RELEASE_ADDRESS_METADATA_FILTER_TYPE=0;
	private final static int TEST_RELEASE_ADDRESS_METADATA_FILTER_VALUE=1;
	private final static int TEST_RELEASE_ADDRESS_METADATA_MAILS=2;
	
	private final static String TEST_RELEASES_ADDRESS_METADATA_SEPARATOR=":";
	private final static String TEST_RELEASES_ADDRESS_MAILS_SEPARATOR=",";
	private final static String TEST_RELEASE_METADATA_USERGROUPS_SEPARATOR="@";
	private final static String TEST_RELEASE_METADATA_ADMINS_SEPARATOR="@";
	private final static String TEST_RELEASE_METADATA_ADMINGROUPS_SEPARATOR="@";
	
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{assessementsService}")
	private AssessementsService assessementsService;
	@ManagedProperty(value="#{navLocationsService}")
	private NavLocationsService navLocationsService;
	@ManagedProperty(value="#{redoQuestionValuesService}")
	private RedoQuestionValuesService redoQuestionValuesService;
	
	public TestReleasesService()
	{
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	public void setAssessementsService(AssessementsService assessementsService)
	{
		this.assessementsService=assessementsService;
	}
	
	public void setNavLocationsService(NavLocationsService navLocationsService)
	{
		this.navLocationsService=navLocationsService;
	}
	
	public void setRedoQuestionValuesService(RedoQuestionValuesService redoQuestionValuesService)
	{
		this.redoQuestionValuesService=redoQuestionValuesService;
	}
	
	/**
	 * @param includeOldVersions true to include old versions of tests released, false to get only last versions
	 * @return List of tests published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<TestRelease> getTestsReleases(boolean includeOldVersions) throws ServiceException
	{
		return getTestsReleases(null,includeOldVersions);
	}
	
	/**
	 * @param operation Operation
	 * @param includeOldVersions true to include old versions of tests released, false to get only last versions
	 * @return List of tests published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<TestRelease> getTestsReleases(Operation operation,boolean includeOldVersions) throws ServiceException
	{
		List<TestRelease> testReleases=getTestsReleases(operation);
		if (!includeOldVersions)
		{
			// Get lastest versions of test releases
			Map<Test,Integer> testsReleasesLatestVersions=new HashMap<Test,Integer>();
			for (TestRelease testRelease:testReleases)
			{
				if (testsReleasesLatestVersions.containsKey(testRelease.getTest()))
				{
					if (testRelease.getVersion()>testsReleasesLatestVersions.get(testRelease.getTest()))
					{
						testsReleasesLatestVersions.put(
							testRelease.getTest(),Integer.valueOf(testRelease.getVersion()));
					}
				}
				else
				{
					testsReleasesLatestVersions.put(testRelease.getTest(),Integer.valueOf(testRelease.getVersion()));
				}
			}
			
			// Remove old versions
			List<TestRelease> testReleasesToRemove=new ArrayList<TestRelease>();
			for (TestRelease testRelease:testReleases)
			{
				if (testRelease.getVersion()!=testsReleasesLatestVersions.get(testRelease.getTest()))
				{
					testReleasesToRemove.add(testRelease);
				}
			}
			for (TestRelease testReleaseToRemove:testReleasesToRemove)
			{
				testReleases.remove(testReleaseToRemove);
			}
		}
		return testReleases;
	}
	
	/**
	 * @return List of tests published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<TestRelease> getTestsReleases() throws ServiceException
	{
		return getTestsReleases(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of tests published within Test Navigator production environment
	 * @throws ServiceException
	 */
	public List<TestRelease> getTestsReleases(Operation operation) throws ServiceException
	{
		List<TestRelease> testsReleases=new ArrayList<TestRelease>();
		String testsReleasesMetadata=getTestsReleasesMetadata();
		if (!"".equals(testsReleasesMetadata))
		{
			boolean singleOp=operation==null;
			try
			{
				if (singleOp)
				{
					// Start Hibernate operation
					operation=HibernateUtil.startOperation();
				}
				
				int i=TEST_RELEASE_METADATA_SIGNATURE;
				Test test=null;
				int version=0;
				User publisher=null;
				DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date releaseDate=null;
				Date startDate=null;
				Date closeDate=null;
				Date deleteDate=null;
				Map<Long,Test> tests=new HashMap<Long,Test>();
				Map<String,User> users=new HashMap<String,User>();
				for (String testReleaseMetadata:testsReleasesMetadata.split(";",-1))
				{
					switch (i%(TEST_RELEASE_METADATA_DELETE_DATE+1))
					{
						case TEST_RELEASE_METADATA_SIGNATURE:
							int iStartTId=testReleaseMetadata.indexOf('t');
							long testId=Long.parseLong(testReleaseMetadata.substring(iStartTId+1));
							if (tests.containsKey(Long.valueOf(testId)))
							{
								test=tests.get(Long.valueOf(testId));
							}
							else
							{
								test=testsService.getTest(operation,testId);
								if (test!=null)
								{
									tests.put(Long.valueOf(testId),test);
								}
							}
							if (test!=null && test.getCreatedBy()!=null)
							{
								long userId=Long.parseLong(testReleaseMetadata.substring(1,iStartTId-1));
								if (test.getCreatedBy().getId()!=userId)
								{
									test=null;
								}
							}
							break;
						case TEST_RELEASE_METADATA_VERSION:
							version=Integer.parseInt(testReleaseMetadata);
							break;
						case TEST_RELEASE_METADATA_PUBLISHER:
							if ("".equals(testReleaseMetadata))
							{
								publisher=null;
							}
							else
							{
								if (users.containsKey(testReleaseMetadata))
								{
									publisher=users.get(testReleaseMetadata);
								}
								else
								{
									publisher=usersService.getUserFromOucu(operation,testReleaseMetadata);
									if (publisher!=null)
									{
										users.put(testReleaseMetadata,publisher);
									}
								}
							}
							break;
						case TEST_RELEASE_METADATA_RELEASE_DATE:
							if (!"".equals(testReleaseMetadata))
							{
								try
								{
									releaseDate=dateFormat.parse(testReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									test=null;
								}
							}
							break;
						case TEST_RELEASE_METADATA_START_DATE:
							if (!"".equals(testReleaseMetadata))
							{
								try
								{
									startDate=dateFormat.parse(testReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									test=null;
								}
							}
							break;
						case TEST_RELEASE_METADATA_CLOSE_DATE:
							if (!"".equals(testReleaseMetadata))
							{
								try
								{
									closeDate=dateFormat.parse(testReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									test=null;
								}
							}
							break;
						case TEST_RELEASE_METADATA_DELETE_DATE:
							if (!"".equals(testReleaseMetadata))
							{
								try
								{
									deleteDate=dateFormat.parse(testReleaseMetadata);
								} 
								catch (ParseException pe)
								{
									test=null;
								}
							}
							if (test!=null)
							{
								testsReleases.add(new TestRelease(
									test,version,publisher,releaseDate,startDate,closeDate,deleteDate));
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
		return testsReleases;
	}
	
	/**
	 * @param id Test identifier
	 * @return List of sorted versions of a test published within Test Navigator production environment
	 */
	public List<Integer> getTestReleaseVersions(long id) throws ServiceException
	{
		return getTestReleaseVersions(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Test identifier
	 * @return List of sorted versions of a test published within Test Navigator production environment
	 */
	public List<Integer> getTestReleaseVersions(Operation operation,long id) throws ServiceException
	{
		return getTestReleaseVersions(testsService.getTest(operation,id));
	}
	
	/**
	 * @param test Test
	 * @return List of sorted versions of a test published within Test Navigator production environment
	 */
	private List<Integer> getTestReleaseVersions(Test test) throws ServiceException
	{
		List<Integer> testReleaseVersions=new ArrayList<Integer>();
		if (test!=null)
		{
			String sTestReleaseVersions=getTestReleaseVersions(test.getSignature());
			if (!"".equals(sTestReleaseVersions))
			{
				for (String sTestReleaseVersion:sTestReleaseVersions.split(";"))
				{
					testReleaseVersions.add(Integer.valueOf(sTestReleaseVersion));
				}
				Collections.sort(testReleaseVersions);
			}
		}
		return testReleaseVersions;
	}
	
	/**
	 * @param id Test identifier
	 * @return Test release of indicated test (last version) within Test Navigator production environment, 
	 * null if it is not found
	 * @throws ServiceException
	 */
	public TestRelease getTestRelease(long id) throws ServiceException
	{
		return getTestRelease(null,id,Integer.MAX_VALUE);
	}
	
	/**
	 * @param operation Operation
	 * @param id Test identifier
	 * @return Test release of indicated test (last version) within Test Navigator production environment, 
	 * null if it is not found
	 * @throws ServiceException
	 */
	public TestRelease getTestRelease(Operation operation,long id) throws ServiceException
	{
		return getTestRelease(operation,id,Integer.MAX_VALUE);
	}
	
	/**
	 * @param id Test identifier
	 * @param version Version
	 * @return Test release of indicated test and version within Test Navigator production environment, 
	 * null if it is not found
	 * @throws ServiceException
	 */
	public TestRelease getTestRelease(long id,int version) throws ServiceException
	{
		return getTestRelease(null,id,version);
	}
	
	/**
	 * @param operation Operation
	 * @param id Test identifier
	 * @param version Version
	 * @return Test release of indicated test and version within Test Navigator production environment, 
	 * null if it is not found
	 * @throws ServiceException
	 */
	public TestRelease getTestRelease(Operation operation,long id,int version) throws ServiceException
	{
		TestRelease testRelease=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			Test test=testsService.getTest(operation,id);
			if (test!=null)
			{
				if (version==Integer.MAX_VALUE)
				{
					List<Integer> versions=getTestReleaseVersions(test);
					if (versions.size()>0)
					{
						version=versions.get(versions.size()-1).intValue();
					}
					else
					{
						version=0;
					}
				}
				String allTestReleaseMetadata=getTestReleaseMetadata(test.getSignature(),version);
				if (!"".equals(allTestReleaseMetadata))
				{
					int i=TEST_RELEASE_METADATA_PUBLISHER;
					User publisher=null;
					Date releaseDate=null;
					Assessement assessement=null;
					boolean allUsersAllowed=true;
					boolean allowAdminReports=true;
					Date startDate=null;
					Date closeDate=null;
					Date warningDate=null;
					Date feedbackDate=null;
					Date deleteDate=null;
					boolean freeSummary=false;
					boolean freeStop=true;
					boolean summaryQuestions=false;
					boolean summaryScores=false;
					boolean summaryAttempts=true;
					boolean navigation=true;
					NavLocation navLocation=null;
					RedoQuestionValue redoQuestion=null;
					boolean redoTest=true;
					List<SupportContactBean> supportContacts=new ArrayList<SupportContactBean>();
					List<EvaluatorBean> evaluators=new ArrayList<EvaluatorBean>();
					List<User> users=new ArrayList<User>();
					List<String> userGroups=new ArrayList<String>();
					List<User> admins=new ArrayList<User>();
					List<String> adminGroups=new ArrayList<String>();
					boolean usersDone=false;
					boolean userGroupsDone=false;
					boolean adminsDone=false;
					DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					for (String testReleaseMetadata:allTestReleaseMetadata.split(";",-1))
					{
						switch (i)
						{
							case TEST_RELEASE_METADATA_PUBLISHER:
								if ("".equals(testReleaseMetadata))
								{
									publisher=null;
								}
								else
								{
									publisher=usersService.getUserFromOucu(operation,testReleaseMetadata);
								}
								break;
							case TEST_RELEASE_METADATA_RELEASE_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										releaseDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_START_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										startDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_CLOSE_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										closeDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_DELETE_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										deleteDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_WARNING_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										warningDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_FEEDBACK_DATE:
								if (!"".equals(testReleaseMetadata))
								{
									try
									{
										feedbackDate=dateFormat.parse(testReleaseMetadata);
									}
									catch (ParseException pe)
									{
										test=null;
									}
								}
								break;
							case TEST_RELEASE_METADATA_ASSESSEMENT:
								assessement=assessementsService.getAssessement(operation,testReleaseMetadata);
								if (assessement==null)
								{
									test=null;
								}
								break;
							case TEST_RELEASE_METADATA_ALL_USERS_ALLOWED:
								allUsersAllowed=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_ALLOW_ADMIN_REPORTS:
								allowAdminReports=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_FREE_SUMMARY:
								freeSummary=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_FREE_STOP:
								freeStop=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_SUMMARY_QUESTIONS:
								summaryQuestions=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_SUMMARY_SCORES:
								summaryScores=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_SUMMARY_ATTEMPTS:
								summaryAttempts=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_NAVIGATION:
								navigation=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_NAV_LOCATION:
								navLocation=navLocationsService.getNavLocation(operation,testReleaseMetadata);
								if (navLocation==null)
								{
									test=null;
								}
								break;
							case TEST_RELEASE_METADATA_REDO_QUESTION:
								redoQuestion=
									redoQuestionValuesService.getRedoQuestion(operation,testReleaseMetadata);
								if (redoQuestion==null)
								{
									test=null;
								}
								break;
							case TEST_RELEASE_METADATA_REDO_TEST:
								redoTest=Boolean.valueOf(testReleaseMetadata).booleanValue();
								break;
							case TEST_RELEASE_METADATA_SUPPORT_CONTACTS:
								if (!"".equals(testReleaseMetadata))
								{
									int j=TEST_RELEASE_ADDRESS_METADATA_FILTER_TYPE;
									String filterType=null;
									String filterSubtype="";
									String filterValue="";
									for (String supportContactMetadata:
										testReleaseMetadata.split(TEST_RELEASES_ADDRESS_METADATA_SEPARATOR,-1))
									{
										switch (j%(TEST_RELEASE_ADDRESS_METADATA_MAILS+1))
										{
											case TEST_RELEASE_ADDRESS_METADATA_FILTER_TYPE:
												if ("all".equals(supportContactMetadata))
												{
													filterType="NO_FILTER";
												}
												else if ("single-oucu".equals(supportContactMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="USERS_SELECTION";
												}
												else if ("range-name".equals(supportContactMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="RANGE_NAME";
												}
												else if ("range-surname".equals(supportContactMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="RANGE_SURNAME";
												}
												else if ("single-group".equals(supportContactMetadata))
												{
													filterType="GROUP_FILTER";
												}
												break;
											case TEST_RELEASE_ADDRESS_METADATA_FILTER_VALUE:
												if (filterType!=null)
												{
													filterValue=supportContactMetadata;
												}
												break;
											case TEST_RELEASE_ADDRESS_METADATA_MAILS:
												if (filterType!=null)
												{
													
													List<String> supportContactMails=new ArrayList<String>();
													for (String supportContactMail:supportContactMetadata.split(
														TEST_RELEASES_ADDRESS_MAILS_SEPARATOR))
													{
														if (EmailValidator.validate(supportContactMail) && 
															!supportContactMails.contains(supportContactMail))
														{
															supportContactMails.add(supportContactMail);
														}
													}
													if (!supportContactMails.isEmpty())
													{
														List<String> filterValues=new ArrayList<String>();
														if ("RANGE_NAME".equals(filterSubtype) || 
															"RANGE_SURNAME".equals(filterSubtype))
														{
															for (String range:filterValue.split(","))
															{
																char lowerLimitChar='A';
																char upperLimitChar='Z';
																int limitSeparatorPos=range.indexOf('-');
																if (limitSeparatorPos!=-1)
																{
																	if (limitSeparatorPos>0)
																	{
																		if (Character.toUpperCase(range.charAt(0))>
																			lowerLimitChar)
																		{
																			lowerLimitChar=Character.toUpperCase(
																				range.charAt(0));
																		}
																		if (lowerLimitChar>'Z')
																		{
																			lowerLimitChar='Z';
																		}
																	}
																	if (limitSeparatorPos<range.length()-1)
																	{
																		if (Character.toUpperCase(
																			range.charAt(limitSeparatorPos+1))<
																			upperLimitChar)
																		{
																			upperLimitChar=Character.toUpperCase(
																				range.charAt(limitSeparatorPos+1));
																		}
																		if (upperLimitChar<'A')
																		{
																			upperLimitChar='A';
																		}
																	}
																}
																if (lowerLimitChar<=upperLimitChar)
																{
																	StringBuffer rangeValue=new StringBuffer();
																	rangeValue.append(lowerLimitChar);
																	rangeValue.append('-');
																	rangeValue.append(upperLimitChar);
																	if (!filterValues.contains(
																		rangeValue.toString()))
																	{
																		filterValues.add(rangeValue.toString());
																	}
																}
															}
														}
														else
														{
															filterValues.add(filterValue);
														}
														for (String fValue:filterValues)
														{
															for (String supportContactMail:supportContactMails)
															{
																supportContacts.add(
																	new SupportContactBean(supportContactMail,
																	filterType,filterSubtype,fValue));
															}
														}
													}
												}
										}
										j++;
									}
								}
								break;
							case TEST_RELEASE_METADATA_EVALUATORS:
								if (!"".equals(testReleaseMetadata))
								{
									int j=TEST_RELEASE_ADDRESS_METADATA_FILTER_TYPE;
									String filterType=null;
									String filterSubtype="";
									String filterValue="";
									for (String evaluatorMetadata:
										testReleaseMetadata.split(TEST_RELEASES_ADDRESS_METADATA_SEPARATOR,-1))
									{
										switch (j%(TEST_RELEASE_ADDRESS_METADATA_MAILS+1))
										{
											case TEST_RELEASE_ADDRESS_METADATA_FILTER_TYPE:
												if ("all".equals(evaluatorMetadata))
												{
													filterType="NO_FILTER";
												}
												else if ("single-oucu".equals(evaluatorMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="USERS_SELECTION";
												}
												else if ("range-name".equals(evaluatorMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="RANGE_NAME";
												}
												else if ("range-surname".equals(evaluatorMetadata))
												{
													filterType="USER_FILTER";
													filterSubtype="RANGE_SURNAME";
												}
												else if ("single-group".equals(evaluatorMetadata))
												{
													filterType="GROUP_FILTER";
												}
												break;
											case TEST_RELEASE_ADDRESS_METADATA_FILTER_VALUE:
												if (filterType!=null)
												{
													filterValue=evaluatorMetadata;
												}
												break;
											case TEST_RELEASE_ADDRESS_METADATA_MAILS:
												if (filterType!=null)
												{
													
													List<String> evaluatorMails=new ArrayList<String>();
													for (String evaluatorMail:evaluatorMetadata.split(
														TEST_RELEASES_ADDRESS_MAILS_SEPARATOR))
													{
														if (EmailValidator.validate(evaluatorMail) && 
															!evaluatorMails.contains(evaluatorMail))
														{
															evaluatorMails.add(evaluatorMail);
														}
													}
													if (!evaluatorMails.isEmpty())
													{
														List<String> filterValues=new ArrayList<String>();
														if ("RANGE_NAME".equals(filterSubtype) || 
															"RANGE_SURNAME".equals(filterSubtype))
														{
															for (String range:filterValue.split(","))
															{
																char lowerLimitChar='A';
																char upperLimitChar='Z';
																int limitSeparatorPos=range.indexOf('-');
																if (limitSeparatorPos!=-1)
																{
																	if (limitSeparatorPos>0)
																	{
																		if (Character.toUpperCase(range.charAt(0))>
																			lowerLimitChar)
																		{
																			lowerLimitChar=Character.toUpperCase(
																				range.charAt(0));
																		}
																		if (lowerLimitChar>'Z')
																		{
																			lowerLimitChar='Z';
																		}
																	}
																	if (limitSeparatorPos<range.length()-1)
																	{
																		if (Character.toUpperCase(
																			range.charAt(limitSeparatorPos+1))<
																			upperLimitChar)
																		{
																			upperLimitChar=Character.toUpperCase(
																				range.charAt(limitSeparatorPos+1));
																		}
																		if (upperLimitChar<'A')
																		{
																			upperLimitChar='A';
																		}
																	}
																}
																if (lowerLimitChar<=upperLimitChar)
																{
																	StringBuffer rangeValue=new StringBuffer();
																	rangeValue.append(lowerLimitChar);
																	rangeValue.append('-');
																	rangeValue.append(upperLimitChar);
																	if (!filterValues.contains(
																		rangeValue.toString()))
																	{
																		filterValues.add(rangeValue.toString());
																	}
																}
															}
														}
														else
														{
															filterValues.add(filterValue);
														}
														for (String fValue:filterValues)
														{
															for (String evaluatorMail:evaluatorMails)
															{
																evaluators.add(new EvaluatorBean(
																	evaluatorMail,filterType,filterSubtype,fValue));
															}
														}
													}
												}
										}
										j++;
									}
								}
								break;
							default:
								if (usersDone)
								{
									if (userGroupsDone)
									{
										if (adminsDone)
										{
											if (!adminGroups.contains(testReleaseMetadata))
											{
												adminGroups.add(testReleaseMetadata);
											}
										}
										else
										{
											if (TEST_RELEASE_METADATA_ADMINGROUPS_SEPARATOR.equals(
												testReleaseMetadata))
											{
												adminsDone=true;
											}
											else if (publisher!=null &&
												testReleaseMetadata.equals(publisher.getOucu()))
											{
												if (!admins.contains(publisher))
												{
													admins.add(publisher);
												}
											}
											else
											{
												User admin=null;
												for (User a:admins)
												{
													if (testReleaseMetadata.equals(a.getOucu()))
													{
														admin=a;
														break;
													}
												}
												if (admin==null)
												{
													for (User u:users)
													{
														if (testReleaseMetadata.equals(u.getOucu()))
														{
															admin=u;
															break;
														}
													}
													if (admin==null)
													{
														admin=usersService.getUserFromOucu(
															operation,testReleaseMetadata);
													}
													if (admin!=null)
													{
														admins.add(admin);
													}
												}
											}
										}
									}
									else
									{
										if (TEST_RELEASE_METADATA_ADMINS_SEPARATOR.equals(testReleaseMetadata))
										{
											userGroupsDone=true;
										}
										else if (!allUsersAllowed && !userGroups.contains(testReleaseMetadata))
										{
											userGroups.add(testReleaseMetadata);
										}
									}
								}
								else
								{
									if (TEST_RELEASE_METADATA_USERGROUPS_SEPARATOR.equals(testReleaseMetadata))
									{
										usersDone=true;
									}
									else if (!allUsersAllowed)
									{
										if (publisher!=null && testReleaseMetadata.equals(publisher.getOucu()))
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
												if (testReleaseMetadata.equals(u.getOucu()))
												{
													user=u;
													break;
												}
											}
											if (user==null)
											{
												user=usersService.getUserFromOucu(operation,testReleaseMetadata);
												if (user!=null)
												{
													users.add(user);
												}
											}
										}
									}
								}
						}
						if (test==null)
						{
							break;
						}
						else
						{
							i++;
						}
					}
					if (test!=null)
					{
						testRelease=new TestRelease(test,version,publisher,releaseDate,startDate,closeDate,
							deleteDate,warningDate,feedbackDate,assessement,allUsersAllowed,allowAdminReports,
							freeSummary,freeStop,summaryQuestions,summaryScores,summaryAttempts,navigation,
							navLocation,redoQuestion,redoTest,users,userGroups,admins,adminGroups,
							supportContacts,evaluators);
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
		return testRelease;
	}
	
	/**
	 * @return String with metadata of all tests compatible with GEPEQ published within 
	 * Test Navigator production environment
	 */
	private String getTestsReleasesMetadata()
	{
		String testsReleasesMetadata="";
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
				testsReleasesMetadata=omTnProWs.getTestsReleasesMetadata();
			}
			catch (RemoteException re)
			{
				testsReleasesMetadata="";
			}
		}
		return testsReleasesMetadata;
	}
	
	/**
	 * @param testName Test name
	 * @return String with versions of a test published within Test Navigator production environment
	 */
	private String getTestReleaseVersions(String testName)
	{
		String testReleaseVersions="";
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
				testReleaseVersions=omTnProWs.getTestReleaseVersions(testName);
			}
			catch (RemoteException re)
			{
				testReleaseVersions="";
			}
		}
		return testReleaseVersions;
	}
	
	/**
	 * @param testName Test name
	 * @param version Version
	 * @return String with metadata of the test with the indicated test name (signature) and version 
	 * if it has been published within Test Navigator production environment, empty string otherwise
	 */
	private String getTestReleaseMetadata(String testName,int version)
	{
		String testReleaseMetadata="";
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
				testReleaseMetadata=omTnProWs.getTestReleaseMetadata(testName,version);
			}
			catch (RemoteException re)
			{
				testReleaseMetadata="";
			}
		}
		return testReleaseMetadata;
	}
}
