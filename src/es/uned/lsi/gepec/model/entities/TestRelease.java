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
package es.uned.lsi.gepec.model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;

/**
 * Class to define a test published within a Test Navigator production environment.
 */
@SuppressWarnings("serial")
public class TestRelease implements Serializable
{
	public final static int MIN_VERSION=1;
	public final static int MAX_VERSION=99;
	
	private Test test;
	private int version;
	private User publisher;
	private Date releaseDate;
	private Assessement assessement;
	private boolean allUsersAllowed;
	private boolean allowAdminReports;
	private Date startDate;
	private Date closeDate;
	private Date warningDate;
	private Date feedbackDate;
	private Date deleteDate;
	private boolean freeSummary;
	private boolean freeStop;
	private boolean summaryQuestions;
	private boolean summaryScores;
	private boolean summaryAttempts;
	private boolean navigation;
	private NavLocation navLocation;
	private RedoQuestionValue redoQuestion;
	private boolean redoTest;
	private List<User> users;
	private List<User> admins;
	private List<SupportContactBean> supportContacts;
	private List<EvaluatorBean> evaluators;
	
	public TestRelease(Test test,int version,User publisher)
	{
		this(test,version,publisher,null,test.getStartDate(),test.getCloseDate(),null);
	}
	
	public TestRelease(Test test,int version,User publisher,Date releaseDate,Date startDate,Date closeDate,
			Date deleteDate)
	{
		this(test,version,publisher,releaseDate,startDate,closeDate,deleteDate,test.getWarningDate(),
			test.getFeedbackDate(),test.getAssessement(),test.isAllUsersAllowed(),test.isAllowAdminReports(),
			test.isFreeSummary(),test.isFreeStop(),test.isSummaryQuestions(),test.isSummaryScores(),
			test.isSummaryAttempts(),test.isNavigation(),test.getNavLocation(),test.getRedoQuestion(),
			test.isRedoTest(),new ArrayList<User>(0),new ArrayList<User>(0),new ArrayList<SupportContactBean>(0),
			new ArrayList<EvaluatorBean>(0));
		for (TestUser testUser:test.getTestUsers())
		{
			User user=testUser.getUser();
			if (testUser.isOmUser())
			{
				users.add(user);
			}
			if (testUser.isOmAdmin())
			{
				admins.add(user);
			}
		}
		for (SupportContact supportContact:test.getSupportContacts())
		{
			supportContacts.add(new SupportContactBean(supportContact));
		}
		for (Evaluator evaluator:test.getEvaluators())
		{
			evaluators.add(new EvaluatorBean(evaluator));
		}
	}
	
	public TestRelease(Test test,int version,User publisher,Date releaseDate,Date startDate,Date closeDate,
		Date deleteDate,Date warningDate,Date feedbackDate,Assessement assessement,boolean allUsersAllowed,
		boolean allowAdminReports,boolean freeSummary,boolean freeStop,boolean summaryQuestions,
		boolean summaryScores,boolean summaryAttempts,boolean navigation,NavLocation navLocation,
		RedoQuestionValue redoQuestion,boolean redoTest,List<User> users,List<User> admins,
		List<SupportContactBean> supportContacts,List<EvaluatorBean> evaluators)
	{
		this.test=test;
		this.version=version;
		this.publisher=publisher;
		this.releaseDate=releaseDate;
		this.startDate=startDate;
		this.closeDate=closeDate;
		this.deleteDate=deleteDate;
		this.warningDate=warningDate;
		this.feedbackDate=feedbackDate;
		this.assessement=assessement;
		this.allUsersAllowed=allUsersAllowed;
		this.allowAdminReports=allowAdminReports;
		this.freeSummary=freeSummary;
		this.freeStop=freeStop;
		this.summaryQuestions=summaryQuestions;
		this.summaryScores=summaryScores;
		this.summaryAttempts=summaryAttempts;
		this.navigation=navigation;
		this.navLocation=navLocation;
		this.redoQuestion=redoQuestion;
		this.redoTest=redoTest;
		this.users=users;
		this.admins=admins;
		this.supportContacts=supportContacts;
		this.evaluators=evaluators;
	}
	
	public Test getTest()
	{
		return test;
	}
	
	public void setTest(Test test)
	{
		this.test=test;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public void setVersion(int version)
	{
		this.version=version;
	}
	
	public User getPublisher()
	{
		return publisher;
	}
	
	public void setPublisher(User publisher)
	{
		this.publisher=publisher;
	}
	
	public Date getReleaseDate()
	{
		return releaseDate;
	}
	
	public void setReleaseDate(Date releaseDate)
	{
		this.releaseDate=releaseDate;
	}
	
	public Assessement getAssessement()
	{
		return assessement;
	}
	
	public void setAssessement(Assessement assessement)
	{
		this.assessement=assessement;
	}
	
	public boolean isAllUsersAllowed()
	{
		return allUsersAllowed;
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		this.allUsersAllowed=allUsersAllowed;
	}
	
	public boolean isAllowAdminReports()
	{
		return allowAdminReports;
	}
	
	public void setAllowAdminReports(boolean allowAdminReports)
	{
		this.allowAdminReports=allowAdminReports;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
	
	public void setStartDate(Date startDate)
	{
		this.startDate=startDate;
	}
	
	public Date getCloseDate()
	{
		return closeDate;
	}
	
	public void setCloseDate(Date closeDate)
	{
		this.closeDate=closeDate;
	}
	
	public Date getWarningDate()
	{
		return warningDate;
	}
	
	public void setWarningDate(Date warningDate)
	{
		this.warningDate=warningDate;
	}
	
	public Date getFeedbackDate()
	{
		return feedbackDate;
	}
	
	public void setFeedbackDate(Date feedbackDate)
	{
		this.feedbackDate=feedbackDate;
	}
	
	public Date getDeleteDate()
	{
		return deleteDate;
	}
	
	public void setDeleteDate(Date deleteDate)
	{
		this.deleteDate=deleteDate;
	}
	
	public boolean isFreeSummary()
	{
		return freeSummary;
	}
	
	public void setFreeSummary(boolean freeSummary)
	{
		this.freeSummary=freeSummary;
	}
	
	public boolean isFreeStop()
	{
		return freeStop;
	}
	
	public void setFreeStop(boolean freeStop)
	{
		this.freeStop=freeStop;
	}
	
	public boolean isSummaryQuestions()
	{
		return summaryQuestions;
	}
	
	public void setSummaryQuestions(boolean summaryQuestions)
	{
		this.summaryQuestions=summaryQuestions;
	}
	
	public boolean isSummaryScores()
	{
		return summaryScores;
	}
	
	public void setSummaryScores(boolean summaryScores)
	{
		this.summaryScores=summaryScores;
	}
	
	public boolean isSummaryAttempts()
	{
		return summaryAttempts;
	}
	
	public void setSummaryAttempts(boolean summaryAttempts)
	{
		this.summaryAttempts=summaryAttempts;
	}
	
	public boolean isNavigation()
	{
		return navigation;
	}
	
	public void setNavigation(boolean navigation)
	{
		this.navigation=navigation;
	}
	
	public NavLocation getNavLocation()
	{
		return navLocation;
	}
	
	public void setNavLocation(NavLocation navLocation)
	{
		this.navLocation=navLocation;
	}
	
	public RedoQuestionValue getRedoQuestion()
	{
		return redoQuestion;
	}
	
	public void setRedoQuestion(RedoQuestionValue redoQuestion)
	{
		this.redoQuestion=redoQuestion;
	}
	
	public boolean isRedoTest()
	{
		return redoTest;
	}
	
	public void setRedoTest(boolean redoTest)
	{
		this.redoTest=redoTest;
	}
	
	public List<User> getUsers()
	{
		return users;
	}
	
	public void setUsers(List<User> users)
	{
		this.users=users;
	}
	
	public List<User> getAdmins()
	{
		return admins;
	}
	
	public void setAdmins(List<User> admins)
	{
		this.admins=admins;
	}
	
	public List<SupportContactBean> getSupportContacts()
	{
		return supportContacts;
	}
	
	public void setSupportContacts(List<SupportContactBean> supportContacts)
	{
		this.supportContacts=supportContacts;
	}
	
	public List<EvaluatorBean> getEvaluators()
	{
		return evaluators;
	}
	
	public void setEvaluators(List<EvaluatorBean> evaluators)
	{
		this.evaluators=evaluators;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof TestRelease && test.equals(((TestRelease)obj).test) && 
			version==((TestRelease)obj).version;
	}
}
