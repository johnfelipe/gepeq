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
package es.uned.lsi.gepec.web;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.context.RequestContext;
import org.primefaces.event.DateSelectEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DualListModel;

import es.uned.lsi.gepec.model.entities.AddressType;
import es.uned.lsi.gepec.model.entities.Assessement;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.NavLocation;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionOrder;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.model.entities.RedoQuestionValue;
import es.uned.lsi.gepec.model.entities.ScoreType;
import es.uned.lsi.gepec.model.entities.Section;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.TestRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.EmailValidator;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.EvaluatorBean;
import es.uned.lsi.gepec.web.backbeans.SupportContactBean;
import es.uned.lsi.gepec.web.services.AddressTypesService;
import es.uned.lsi.gepec.web.services.AssessementsService;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.NavLocationsService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.RedoQuestionValuesService;
import es.uned.lsi.gepec.web.services.TestReleasesService;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

/**
 * Managed bean for publishing a test.
 */
@SuppressWarnings("serial")
@ManagedBean(name="testReleaseBean")
@ViewScoped
public class TestReleaseBean implements Serializable
{
	private final static String DATE_HIDDEN_PATTERN="MM-dd-yyyy HH:mm:ss";
	
	private final static String GENERAL_WIZARD_TAB="general";
	//private final static String PRESENTATION_WIZARD_TAB="presentation";
	//private final static String SECTIONS_WIZARD_TAB="sections";
	private final static String USERS_WIZARD_TAB="users";
	private final static String CALENDAR_WIZARD_TAB="calendar";
	//private final static String CONFIGURATION_WIZARD_TAB="configuration";
	//private final static String ADRESSESES_WIZARD_TAB="addresses";
	private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{navLocationsService}")
	private NavLocationsService navLocationsService;
	@ManagedProperty(value="#{redoQuestionValuesService}")
	private RedoQuestionValuesService redoQuestionValuesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionTypesService}")
	private QuestionTypesService questionTypesService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{testReleasesService}")
	private TestReleasesService testReleasesService;
	@ManagedProperty(value="#{assessementsService}")
	private AssessementsService assessementsService;
	@ManagedProperty(value="#{addressTypesService}")
	private AddressTypesService addressTypesService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private TestRelease testRelease;						// Current test release
	
	/** UI Helper Properties*/
	
	private String cancelPublishTestTarget;
	
	private String activeTestReleaseTabName;
	
	private boolean publishAllowed;
	private String lastErrorMessage;
	
	private String version;
	private Assessement assessement;
	
	private boolean restrictDates;
	private boolean restrictFeedbackDate;
	private String startDateHidden;
	private String closeDateHidden;
	private String warningDateHidden;
	private String feedbackDateHidden;
	private String deleteDateHidden;
	
	private List<User> filteredUsersForAddingUsers;
	private List<User> filteredUsersForAddingAdmins;
	private List<User> filteredUsersForAddingSupportContactFilterUsers;
	private List<User> filteredUsersForAddingEvaluatorFilterUsers;
	private List<UserType> userTypes;
	private long filterUsersUserTypeId;
	private boolean filterUsersIncludeOmUsers;
	private DualListModel<User> usersDualList;
	private long filterAdminsUserTypeId;
	private boolean filterAdminsIncludeOmUsers;
	private DualListModel<User> adminsDualList;
	
	private SupportContactBean currentSuportContact;
	private EvaluatorBean currentEvaluator;
	
	private String supportContactFilterType;
	private String supportContactFilterSubtype;
	private List<String> supportContactFilterSubtypes;
	private long filterSupportContactFilterUsersUserTypeId;
	private boolean filterSupportContactFilterUsersIncludeOmUsers;
	private List<User> testSupportContactFilterUsers;
	private String supportContactFilterUsersIdsHidden;
	private DualListModel<User> supportContactFilterUsersDualList;
	
	private String filterSupportContactRangeNameLowerLimit;
	private String filterSupportContactRangeNameUpperLimit;
	private String filterSupportContactRangeSurnameLowerLimit;
	private String filterSupportContactRangeSurnameUpperLimit;
	
	private String evaluatorFilterType;
	private String evaluatorFilterSubtype;
	private List<String> evaluatorFilterSubtypes;
	private long filterEvaluatorFilterUsersUserTypeId;
	private boolean filterEvaluatorFilterUsersIncludeOmUsers;
	private List<User> testEvaluatorFilterUsers;
	private String evaluatorFilterUsersIdsHidden;
	private DualListModel<User> evaluatorFilterUsersDualList;
	
	private String filterEvaluatorRangeNameLowerLimit;
	private String filterEvaluatorRangeNameUpperLimit;
	private String filterEvaluatorRangeSurnameLowerLimit;
	private String filterEvaluatorRangeSurnameUpperLimit;
	
	private List<String> letters;
	
	private String supportContact;
	private boolean supportContactDialogDisplayed;
	private String evaluator;
	private boolean evaluatorDialogDisplayed;
	
	private Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled;
	private Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	
	private Boolean viewOMEnabled;
	
	private Map<Long,Boolean> viewOMQuestionsEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	
	private boolean enabledCheckboxesSetters;
	
	// Assessements
	private List<Assessement> assessements;
	
	// Navigation locations
	private List<NavLocation> navLocations;
	
	// Values for property 'redoQuestion'
	private List<RedoQuestionValue> redoQuestions;
	
	public TestReleaseBean()
	{
		testRelease=null;
		version=null;
		assessement=null;
		restrictDates=false;
		restrictFeedbackDate=false;
		filteredUsersForAddingUsers=null;
		filteredUsersForAddingAdmins=null;
		filteredUsersForAddingSupportContactFilterUsers=null;
		filteredUsersForAddingEvaluatorFilterUsers=null;
		userTypes=null;
		filterUsersUserTypeId=0L;
		filterUsersIncludeOmUsers=true;
		usersDualList=null;
		filterAdminsUserTypeId=0L;
		filterAdminsIncludeOmUsers=true;
		adminsDualList=null;
		viewQuestionsFromOtherUsersPrivateCategoriesEnabled=null;
		viewQuestionsFromAdminsPrivateCategoriesEnabled=null;
		viewQuestionsFromSuperadminsPrivateCategoriesEnabled=null;
		viewOMEnabled=null;
		viewOMQuestionsEnabled=new HashMap<Long,Boolean>();
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		supportContact=null;
		supportContactDialogDisplayed=false;
		evaluator=null;
		evaluatorDialogDisplayed=false;
		supportContactFilterType="NO_FILTER";
		supportContactFilterSubtype=null;
		supportContactFilterSubtypes=null;
		filterSupportContactFilterUsersUserTypeId=0L;
		filterSupportContactFilterUsersIncludeOmUsers=true;
		testSupportContactFilterUsers=new ArrayList<User>();
		supportContactFilterUsersIdsHidden="";
		supportContactFilterUsersDualList=null;
		evaluatorFilterType="NO_FILTER";
		evaluatorFilterSubtype=null;
		evaluatorFilterSubtypes=null;
		filterEvaluatorFilterUsersUserTypeId=0L;
		filterEvaluatorFilterUsersIncludeOmUsers=true;
		testEvaluatorFilterUsers=new ArrayList<User>();
		evaluatorFilterUsersIdsHidden="";
		evaluatorFilterUsersDualList=null;
		letters=null;
		enabledCheckboxesSetters=true;
		activeTestReleaseTabName=GENERAL_WIZARD_TAB;
		publishAllowed=true;
		lastErrorMessage=null;
		assessements=null;
		navLocations=null;
		redoQuestions=null;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setNavLocationsService(NavLocationsService navLocationsService)
	{
		this.navLocationsService=navLocationsService;
	}
	
	public void setRedoQuestionValuesService(RedoQuestionValuesService redoQuestionValuesService)
	{
		this.redoQuestionValuesService=redoQuestionValuesService;
	}
	
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
	public void setCategoriesService(CategoriesService categoriesService)
	{
		this.categoriesService=categoriesService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setQuestionTypesService(QuestionTypesService questionTypesService)
	{
		this.questionTypesService=questionTypesService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setTestReleasesService(TestReleasesService testReleasesService)
	{
		this.testReleasesService=testReleasesService;
	}
	
	public void setAssessementsService(AssessementsService assessementsService)
	{
		this.assessementsService=assessementsService;
	}
	
	public void setAddressTypesService(AddressTypesService addressTypesService)
	{
		this.addressTypesService=addressTypesService;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	public void setUserTypesService(UserTypesService userTypesService)
	{
		this.userTypesService=userTypesService;
	}
	
	public void setUserSessionService(UserSessionService userSessionService)
	{
		this.userSessionService=userSessionService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
    private Operation getCurrentUserOperation(Operation operation)
    {
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
	private boolean isPublishEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_TESTS_ENABLED");
	}
	
	private boolean isPublishOtherUsersEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_TESTS_ENABLED");
	}
	
	private boolean isPublishAdminsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_ADMINS_TESTS_ENABLED");
	}
	
	private boolean isPublishSuperadminsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_USERS_TESTS_ENABLED");
	}
	
	public TestRelease getTestRelease()
	{
		return getTestRelease(null);
	}
	
	private TestRelease getTestRelease(Operation operation)
	{
		if (testRelease==null)
		{
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    		long testId=Long.parseLong(params.get("testId"));
    		int version=Integer.parseInt(params.get("version"));
			
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
    		testRelease=version>0?testReleasesService.getTestRelease(operation,testId,version):null;
    		User currentUser=userSessionService.getCurrentUser(operation);
			List<Integer> testReleaseVersions=testReleasesService.getTestReleaseVersions(operation,testId);
			version=testReleaseVersions.isEmpty()?1:testReleaseVersions.get(testReleaseVersions.size()-1)+1;
			if (version>TestRelease.MAX_VERSION && 
				testReleaseVersions.size()<TestRelease.MAX_VERSION-TestRelease.MIN_VERSION)
			{
				int i=testReleaseVersions.size()-1;
				int versionAux=TestRelease.MAX_VERSION;
				while (version>TestRelease.MAX_VERSION && i>0)
				{
					if (testReleaseVersions.get(i).intValue()!=versionAux)
					{
						version=versionAux;
					}
					i--;
					versionAux--;
				}
			}
    		if (testRelease==null)
    		{
    			Test test=testsService.getTest(operation,testId);
    			testRelease=new TestRelease(test,version,currentUser);
    		}
    		else
    		{
    			testRelease.setPublisher(currentUser);
    			testRelease.setVersion(version);
    		}
    		
    		if (testRelease!=null)
    		{
    			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
    			Date startDate=testRelease.getStartDate();
    			Date closeDate=testRelease.getCloseDate();
    			Date warningDate=testRelease.getWarningDate();
    			Date feedbackDate=testRelease.getFeedbackDate();
    			setRestrictDates(startDate!=null);
    	    	setStartDateHidden(startDate==null?"":df.format(startDate));
    	    	setCloseDateHidden(closeDate==null?"":df.format(closeDate));
    	    	setWarningDateHidden(warningDate==null?"":df.format(warningDate));
    			setRestrictFeedbackDate(feedbackDate!=null);
    			setFeedbackDateHidden(feedbackDate==null?"":df.format(feedbackDate));
    		}
		}
		return testRelease;
	}
	
	public void setTestRelease(TestRelease testRelease)
	{
		this.testRelease=testRelease;
	}
	
	public boolean isAllUsersAllowed()
	{
		return isAllUsersAllowed(null);
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		setAllUsersAllowed(null,allUsersAllowed);
	}
	
	private boolean isAllUsersAllowed(Operation operation)
	{
		return getTestRelease(getCurrentUserOperation(operation)).isAllUsersAllowed();
	}
	
	private void setAllUsersAllowed(Operation operation,boolean allUsersAllowed)
	{
		if (isEnabledChecboxesSetters())
		{
			getTestRelease(getCurrentUserOperation(operation)).setAllUsersAllowed(allUsersAllowed);
		}
	}
	
	public boolean isAllowAdminReports()
	{
		return isAllowAdminReports(null);
	}
	
	public void setAllowAdminReports(boolean allowAdminReports)
	{
		setAllowAdminReports(null,allowAdminReports);
	}
	
	private boolean isAllowAdminReports(Operation operation)
	{
		return getTestRelease(getCurrentUserOperation(operation)).isAllowAdminReports();
	}
	
	private void setAllowAdminReports(Operation operation,boolean allowAdminReports)
	{
		if (isEnabledChecboxesSetters())
		{
			getTestRelease(getCurrentUserOperation(operation)).setAllowAdminReports(allowAdminReports);
		}
	}
	
	public boolean isRestrictDates()
	{
		return isRestrictDates(null);
	}
	
	public void setRestrictDates(boolean restrictDates)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictDates=restrictDates;
		}
	}
	
	private boolean isRestrictDates(Operation operation)
	{
		getTestRelease(getCurrentUserOperation(operation));
		return restrictDates;
	}
	
	public boolean isRestrictFeedbackDate()
	{
		return isRestrictFeedbackDate(null);
	}
	
	public void setRestrictFeedbackDate(boolean restrictFeedbackDate)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictFeedbackDate=restrictFeedbackDate;
		}
	}
	
	private boolean isRestrictFeedbackDate(Operation operation)
	{
		getTestRelease(getCurrentUserOperation(operation));
		return restrictFeedbackDate;
	}
	
	public String getStartDateHidden()
	{
		return startDateHidden;
	}
	
	public void setStartDateHidden(String startDateHidden)
	{
		this.startDateHidden=startDateHidden;
	}
	
	public String getCloseDateHidden()
	{
		return closeDateHidden;
	}
	
	public void setCloseDateHidden(String closeDateHidden)
	{
		this.closeDateHidden=closeDateHidden;
	}
	
	public String getWarningDateHidden()
	{
		return warningDateHidden;
	}
	
	public void setWarningDateHidden(String warningDateHidden)
	{
		this.warningDateHidden=warningDateHidden;
	}
	
	public String getFeedbackDateHidden()
	{
		return feedbackDateHidden;
	}
	
	public void setFeedbackDateHidden(String feedbackDateHidden)
	{
		this.feedbackDateHidden=feedbackDateHidden;
	}
	
	public String getDeleteDateHidden()
	{
		return deleteDateHidden;
	}
	
	public void setDeleteDateHidden(String deleteDateHidden)
	{
		this.deleteDateHidden=deleteDateHidden;
	}
	
	public String getCancelPublishTestTarget()
	{
		return cancelPublishTestTarget;
	}
	
	public void setCancelPublishTestTarget(String cancelPublishTestTarget)
	{
		this.cancelPublishTestTarget=cancelPublishTestTarget;
	}
	
	public Date getStartDate()
	{
		return getStartDate(null);
	}
	
	public void setStartDate(Date startDate)
	{
		setStartDate(null,startDate);
	}
	
	private Date getStartDate(Operation operation)
	{
		TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
		if (getStartDateHidden()!=null && !getStartDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				testRelease.setStartDate(df.parse(getStartDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setStartDateHidden("");
		}
		return testRelease.getStartDate();
	}
	
	private void setStartDate(Operation operation,Date startDate)
	{
		getTestRelease(getCurrentUserOperation(operation)).setStartDate(startDate);
	}
	
	public void changeStartDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setStartDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setStartDateHidden(df.format(event.getDate()));
		}
		setStartDate(getCurrentUserOperation(null),event.getDate());
	}
	
	public Date getCloseDate()
	{
		return getCloseDate(null);
	}
	
	public void setCloseDate(Date closeDate)
	{
		setCloseDate(null,closeDate);
	}
	
	private Date getCloseDate(Operation operation)
	{
		TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
		if (getCloseDateHidden()!=null && !getCloseDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				testRelease.setCloseDate(df.parse(getCloseDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setCloseDateHidden("");
		}
		return testRelease.getCloseDate();
	}
	
	private void setCloseDate(Operation operation,Date closeDate)
	{
		getTestRelease(getCurrentUserOperation(operation)).setCloseDate(closeDate);
	}
	
	public void changeCloseDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setCloseDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setCloseDateHidden(df.format(event.getDate()));
		}
		setCloseDate(getCurrentUserOperation(null),event.getDate());
	}
	
	public Date getWarningDate()
	{
		return getWarningDate(null);
	}
	
	public void setWarningDate(Date warningDate)
	{
		setWarningDate(null,warningDate);
	}
	
	private Date getWarningDate(Operation operation)
	{
		TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
		if (getWarningDateHidden()!=null && !getWarningDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				testRelease.setWarningDate(df.parse(getWarningDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setWarningDateHidden("");
		}
		return testRelease.getWarningDate();
	}
	
	private void setWarningDate(Operation operation,Date warningDate)
	{
		getTestRelease(getCurrentUserOperation(operation)).setWarningDate(warningDate);
	}
	
	public void changeWarningDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setWarningDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setWarningDateHidden(df.format(event.getDate()));
		}
		setWarningDate(getCurrentUserOperation(null),event.getDate());
	}
	
	public Date getFeedbackDate()
	{
		return getFeedbackDate(null);
	}
	
	public void setFeedbackDate(Date feedbackDate)
	{
		setFeedbackDate(null,feedbackDate);
	}
	
	private Date getFeedbackDate(Operation operation)
	{
		TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
		if (getFeedbackDateHidden()!=null && !getFeedbackDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				testRelease.setFeedbackDate(df.parse(getFeedbackDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setFeedbackDateHidden("");
		}
		return testRelease.getFeedbackDate();
	}
	
	private void setFeedbackDate(Operation operation,Date feedbackDate)
	{
		getTestRelease(getCurrentUserOperation(operation)).setFeedbackDate(feedbackDate);
	}
	
	public void changeFeedbackDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setFeedbackDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setFeedbackDateHidden(df.format(event.getDate()));
		}
		setFeedbackDate(getCurrentUserOperation(null),event.getDate());
	}
	
	public Date getDeleteDate()
	{
		return getDeleteDate(null);
	}
	
	public void setDeleteDate(Date deleteDate)
	{
		setDeleteDate(null,deleteDate);
	}
	
	private Date getDeleteDate(Operation operation)
	{
		TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
		if (getDeleteDateHidden()!=null && !getDeleteDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				testRelease.setDeleteDate(df.parse(getDeleteDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setDeleteDateHidden("");
		}
		return testRelease.getDeleteDate();
	}
	
	private void setDeleteDate(Operation operation,Date deleteDate)
	{
		getTestRelease(getCurrentUserOperation(operation)).setDeleteDate(deleteDate);
	}
	
	public void changeDeleteDate(DateSelectEvent event)
	{
		if (event.getDate()==null)
		{
			setDeleteDateHidden("");
		}
		else
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			setDeleteDateHidden(df.format(event.getDate()));
		}
		setDeleteDate(getCurrentUserOperation(null),event.getDate());
	}
	
	public String getVersion()
	{
		return getVersion(null);
	}
	
	public void setVersion(String version)
	{
	}
	
	private String getVersion(Operation operation)
	{
		if (version==null)
		{
			TestRelease testRelease=getTestRelease(getCurrentUserOperation(operation));
			StringBuffer sVersion=new StringBuffer();
			if (testRelease.getVersion()<10)
			{
				sVersion.append('0');
			}
			sVersion.append(testRelease.getVersion());
			version=sVersion.toString();
		}
		return version;
	}
	
    /**
     * @return All navigation locations
     */
    public List<NavLocation> getNavLocations()
    {
    	return getNavLocations(null);
    }
	
    /**
     * @param operation Operation
     * @return All navigation locations
     */
    private List<NavLocation> getNavLocations(Operation operation)
    {
    	if (navLocations==null)
    	{
    		navLocations=navLocationsService.getNavLocations(getCurrentUserOperation(operation));
    	}
    	return navLocations;
    }
    
    /**
     * @param navLocations All navigation locations
     */
    public void setNavLocation(List<NavLocation> navLocations)
    {
    	this.navLocations=navLocations;
    }
    
	public NavLocation getNavLocation()
	{
		return getNavLocation(null);
	}
	
	public void setNavLocation(NavLocation navLocation)
	{
		setNavLocation(null,navLocation);
	}
	
	private NavLocation getNavLocation(Operation operation)
	{
		return getTestRelease(getCurrentUserOperation(operation)).getNavLocation();
	}
	
	private void setNavLocation(Operation operation,NavLocation navLocation)
	{
		getTestRelease(getCurrentUserOperation(operation)).setNavLocation(navLocation);
	}
	
	public long getNavLocationId()
	{
		return getNavLocationId(null);
	}
	
	public void setNavLocationId(long navLocationId)
	{
		setNavLocationId(null,navLocationId);
	}
	
	private long getNavLocationId(Operation operation)
	{
		NavLocation navLocation=getNavLocation(getCurrentUserOperation(operation));
		return navLocation==null?0L:navLocation.getId();
	}
	
	private void setNavLocationId(Operation operation,long navLocationId)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		setNavLocation(operation,navLocationsService.getNavLocation(operation,navLocationId));
	}
	
    /**
     * @return  All values for property 'redoQuestion'
     */
    public List<RedoQuestionValue> getRedoQuestions()
    {
    	return getRedoQuestions(null);
    }
    
    /**
     * @param redoQuestions All values for property 'redoQuestion'
     */
    public void setRedoQuestions(List<RedoQuestionValue> redoQuestions)
    {
    	this.redoQuestions=redoQuestions;
    }
    
    /**
     * @param operation Operation
     * @return All values for property 'redoQuestion'
     */
    private List<RedoQuestionValue> getRedoQuestions(Operation operation)
    {
    	if (redoQuestions==null)
    	{
    		redoQuestions=redoQuestionValuesService.getRedoQuestions(getCurrentUserOperation(operation));
    	}
    	return redoQuestions;
    }
	
	public RedoQuestionValue getRedoQuestion()
	{
		return getRedoQuestion(null);
	}
	
	public void setRedoQuestion(RedoQuestionValue redoQuestion)
	{
		setRedoQuestion(null,redoQuestion);
	}
	
	private RedoQuestionValue getRedoQuestion(Operation operation)
	{
		return getTestRelease(getCurrentUserOperation(operation)).getRedoQuestion();
	}
	
	private void setRedoQuestion(Operation operation,RedoQuestionValue redoQuestion)
	{
		getTestRelease(getCurrentUserOperation(operation)).setRedoQuestion(redoQuestion);
	}
	
	public long getRedoQuestionId()
	{
		return getRedoQuestionId(null);
	}
	
	public void setRedoQuestionId(long redoQuestionId)
	{
		setRedoQuestionId(null,redoQuestionId);
	}
	
	private long getRedoQuestionId(Operation operation)
	{
		RedoQuestionValue redoQuestion=getRedoQuestion(getCurrentUserOperation(operation));
		return redoQuestion==null?0L:redoQuestion.getId();
	}
	
	private void setRedoQuestionId(Operation operation,long redoQuestionId)
	{
		setRedoQuestion(redoQuestionValuesService.getRedoQuestion(getCurrentUserOperation(operation),redoQuestionId));
	}
	
    /**
     * @return All assessements
     */
    public List<Assessement> getAssessements()
    {
    	return getAssessements(null);
    }
    
    /**
     * @param assessements Assessements
     */
    public void setAssessement(List<Assessement> assessements)
    {
    	this.assessements=assessements;
    }
    
    /**
     * @param operation Operation
     * @return All assessements
     */
    private List<Assessement> getAssessements(Operation operation)
    {
    	if (assessements==null)
    	{
    		assessements=assessementsService.getAssessements(getCurrentUserOperation(operation));
    	}
    	return assessements;
    }
    
	public Assessement getAssessement()
	{
		return getAssessement(null);
	}
	
	public void setAssessement(Assessement assessement)
	{
		this.assessement=assessement;
	}
	
	private Assessement getAssessement(Operation operation)
	{
		if (assessement==null)
		{
			assessement=
				assessementsService.getAssessement(getCurrentUserOperation(operation),"ASSESSEMENT_NOT_ASSESSED");
		}
		return assessement;
	}
	
	public long getAssessementId()
	{
		return getAssessementId(null);
	}
	
	public void setAssessementId(long assessementId)
	{
		setAssessementId(null,assessementId);
	}
	
	private long getAssessementId(Operation operation)
	{
		Assessement assessement=getAssessement(getCurrentUserOperation(operation));
		return assessement==null?0L:assessement.getId();
	}
	
	private void setAssessementId(Operation operation,long assessementId)
	{
		setAssessement(assessementsService.getAssessement(getCurrentUserOperation(operation),assessementId));
	}
    
	public String getAssessementTip()
	{
		return getAssessementTip(null);
	}
	
	private String getAssessementTip(Operation operation)
	{
		String assessementTip=null;
		Assessement assessement=getAssessement(getCurrentUserOperation(operation));
		if (assessement!=null)
		{
			StringBuffer assessementTipLabel=new StringBuffer(assessement.getType());
			assessementTipLabel.append("_TIP");
			assessementTip=localizationService.getLocalizedMessage(assessementTipLabel.toString());
		}
		return assessementTip==null?"":assessementTip;
	}
	
	public String getScoreType()
	{
		return getScoreType(null);
	}
	
	public void setScoreType(String scoreType)
	{
	}
	
	private String getScoreType(Operation operation)
	{
		return localizationService.getLocalizedMessage(
			getTestRelease(getCurrentUserOperation(operation)).getTest().getScoreType().getType());
	}
	
	public String getScoreTypeTip()
	{
		return getScoreTypeTip(null);
	}
	
	private String getScoreTypeTip(Operation operation)
	{
		String scoreTypeTip=null;
		StringBuffer scoreTypeTipLabel=
			new StringBuffer(getTestRelease(getCurrentUserOperation(operation)).getTest().getScoreType().getType());
		scoreTypeTipLabel.append("_TIP");
		scoreTypeTip=localizationService.getLocalizedMessage(scoreTypeTipLabel.toString());
		return scoreTypeTip==null?"":scoreTypeTip;
	}
	
	/**
	 * Check if setters for checkboxes are enabled or not.<br/><br/>
	 * Note that this is needed because of a bug of &lt;p:wizard&gt; component that always set properties for 
	 * their checkboxes components as <i>false</i> when submitting the form.
	 * @return true if setters for checkboxes are enabled, false otherwise
	 */
	public boolean isEnabledChecboxesSetters()
	{
		return enabledCheckboxesSetters;
	}
	
	/**
	 * Enable or disable setters for checkboxes.<br/><br/>
	 * Note that this is needed because of a bug of &lt;p:wizard&gt; component that always set properties for 
	 * their checkboxes components as <i>false</i> when submitting the form.
	 * @param enabledCheckboxesSetters true to enable setters for checkboxes, false to disable setters 
	 * for checkboxes
	 */
	public void setEnabledCheckboxesSetters(boolean enabledCheckboxesSetters)
	{
		this.enabledCheckboxesSetters=enabledCheckboxesSetters;
	}
	
	/**
	 * @return Filtered users for the 'Add users' dialog
	 */
	public List<User> getFilteredUsersForAddingUsers()
	{
		return getFilteredUsersForAddingUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingUsers Filtered users for the 'Add users' dialog
	 */
	public void setFilteredUsersForAddingUsers(List<User> filteredUsersForAddingUsers)
	{
		this.filteredUsersForAddingUsers=filteredUsersForAddingUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the 'Add users' dialog
	 */
	private List<User> getFilteredUsersForAddingUsers(Operation operation)
	{
		if (filteredUsersForAddingUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingUsers=
					usersService.getSortedUsersWithoutUserType(operation,isFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingUsers=
					usersService.getSortedUsers(operation,getFilterUsersUserTypeId(),isFilterUsersIncludeOmUsers());
			}
		}
		return filteredUsersForAddingUsers;
	}
	
	/**
	 * @return Filtered users for the 'Add admins' dialog
	 */
	public List<User> getFilteredUsersForAddingAdmins()
	{
		return getFilteredUsersForAddingAdmins(null);
	}
	
	/**
	 * @param filteredUsersForAddingAdmins Filtered users for the 'Add admins' dialog
	 */
	public void setFilteredUsersForAddingAdmins(List<User> filteredUsersForAddingAdmins)
	{
		this.filteredUsersForAddingAdmins=filteredUsersForAddingAdmins;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the 'Add admins' dialog
	 */
	private List<User> getFilteredUsersForAddingAdmins(Operation operation)
	{
		if (filteredUsersForAddingAdmins==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterAdminsUserTypeId()==-1L)
			{
				filteredUsersForAddingAdmins=
					usersService.getSortedUsersWithoutUserType(operation,isFilterAdminsIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingAdmins=
					usersService.getSortedUsers(operation,getFilterAdminsUserTypeId(),isFilterAdminsIncludeOmUsers());
			}
		}
		return filteredUsersForAddingAdmins;
	}
	
	/**
	 * @return Filtered users for the "Add/Edit tech support address" dialog
	 */
	public List<User> getFilteredUsersForAddingSupportContactFilterUsers()
	{
		return getFilteredUsersForAddingSupportContactFilterUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingSupportContactFilterUsers Filtered users for the 
	 * 'Add/Edit tech support address' dialog
	 */
	public void setFilteredUsersForAddingSupportContactFilterUsers(
		List<User> filteredUsersForAddingSupportContactFilterUsers)
	{
		this.filteredUsersForAddingSupportContactFilterUsers=filteredUsersForAddingSupportContactFilterUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the "Add/Edit tech support address" dialog
	 */
	private List<User> getFilteredUsersForAddingSupportContactFilterUsers(Operation operation)
	{
		if (filteredUsersForAddingSupportContactFilterUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterSupportContactFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingSupportContactFilterUsers=usersService.getSortedUsersWithoutUserType(
					operation,isFilterSupportContactFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingSupportContactFilterUsers=usersService.getSortedUsers(operation,
					getFilterSupportContactFilterUsersUserTypeId(),isFilterSupportContactFilterUsersIncludeOmUsers());
			}
			if (!isAllUsersAllowed(operation))
			{
				List<User> testUsers=getTestRelease(operation).getUsers();
				List<User> testAdmins=getTestRelease(operation).getAdmins();
				List<User> usersNotAllowedToDoTest=new ArrayList<User>();
				for (User user:filteredUsersForAddingSupportContactFilterUsers)
				{
					if (!testUsers.contains(user) && !testAdmins.contains(user))
					{
						usersNotAllowedToDoTest.add(user);
					}
				}
				for (User userNotAllowedToDoTest:usersNotAllowedToDoTest)
				{
					filteredUsersForAddingSupportContactFilterUsers.remove(userNotAllowedToDoTest);
				}
			}
		}
		return filteredUsersForAddingSupportContactFilterUsers;
	}
	
	/**
	 * @return Filtered users for the "Add/Edit assessement address" dialog
	 */
	public List<User> getFilteredUsersForAddingEvaluatorFilterUsers()
	{
		return getFilteredUsersForAddingEvaluatorFilterUsers(null);
	}
	
	/**
	 * @param filteredUsersForAddingEvaluatorFilterUsers Filtered users for the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setFilteredUsersForAddingEvaluatorFilterUsers(List<User> filteredUsersForAddingEvaluatorFilterUsers)
	{
		this.filteredUsersForAddingEvaluatorFilterUsers=filteredUsersForAddingEvaluatorFilterUsers;
	}
	
	/**
	 * @param operation Operation
	 * @return Filtered users for the "'Add/Edit assessement address" dialog
	 */
	private List<User> getFilteredUsersForAddingEvaluatorFilterUsers(Operation operation)
	{
		if (filteredUsersForAddingEvaluatorFilterUsers==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (getFilterEvaluatorFilterUsersUserTypeId()==-1L)
			{
				filteredUsersForAddingEvaluatorFilterUsers=
					usersService.getSortedUsersWithoutUserType(operation,isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			else
			{
				filteredUsersForAddingEvaluatorFilterUsers=usersService.getSortedUsers(
					operation,getFilterEvaluatorFilterUsersUserTypeId(),isFilterEvaluatorFilterUsersIncludeOmUsers());
			}
			
			if (!isAllUsersAllowed(operation))
			{
				List<User> testUsers=getTestRelease(operation).getUsers();
				List<User> testAdmins=getTestRelease(operation).getAdmins();
				List<User> usersNotAllowedToDoTest=new ArrayList<User>();
				for (User user:filteredUsersForAddingEvaluatorFilterUsers)
				{
					if (!testUsers.contains(user) && !testAdmins.contains(user))
					{
						usersNotAllowedToDoTest.add(user);
					}
				}
				for (User userNotAllowedToDoTest:usersNotAllowedToDoTest)
				{
					filteredUsersForAddingEvaluatorFilterUsers.remove(userNotAllowedToDoTest);
				}
			}
		}
		return filteredUsersForAddingEvaluatorFilterUsers;
	}
	
	public List<UserType> getUserTypes()
	{
		return getUserTypes(null);
	}
	
	private List<UserType> getUserTypes(Operation operation)
	{
		if (userTypes==null)
		{
			userTypes=userTypesService.getUserTypes(getCurrentUserOperation(operation));
		}
		return userTypes; 
	}
	
	public void setUserTypes(List<UserType> userTypes)
	{
		this.userTypes=userTypes;
	}
	
	public long getFilterUsersUserTypeId()
	{
		return filterUsersUserTypeId;
	}
	
	public void setFilterUsersUserTypeId(long filterUsersUserTypeId)
	{
		this.filterUsersUserTypeId=filterUsersUserTypeId;
	}
	
	public boolean isFilterUsersIncludeOmUsers()
	{
		return filterUsersIncludeOmUsers;
	}
	
	public void setFilterUsersIncludeOmUsers(boolean filterUsersIncludeOmUsers)
	{
		this.filterUsersIncludeOmUsers=filterUsersIncludeOmUsers;
	}
	
	public long getFilterAdminsUserTypeId()
	{
		return filterAdminsUserTypeId;
	}
	
	public void setFilterAdminsUserTypeId(long filterAdminsUserTypeId)
	{
		this.filterAdminsUserTypeId=filterAdminsUserTypeId;
	}
	
	public boolean isFilterAdminsIncludeOmUsers()
	{
		return filterAdminsIncludeOmUsers;
	}
	
	public void setFilterAdminsIncludeOmUsers(boolean filterAdminsIncludeOmUsers)
	{
		this.filterAdminsIncludeOmUsers=filterAdminsIncludeOmUsers;
	}
	
	public Boolean getViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewQuestionsFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromOtherUsersPrivateCategoriesEnabled=
			viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromOtherUsersPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromOtherUsersPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromAdminsPrivateCategoriesEnabled(
		Boolean viewQuestionsFromAdminsPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromAdminsPrivateCategoriesEnabled=viewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromAdminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromAdminsPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromAdminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(
		Boolean viewQuestionsFromSuperadminsPrivateCategoriesEnabled)
	{
		this.viewQuestionsFromSuperadminsPrivateCategoriesEnabled=
			viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewQuestionsFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewQuestionsFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewQuestionsFromSuperadminsPrivateCategoriesEnabled==null)
		{
			viewQuestionsFromSuperadminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewQuestionsFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewOMEnabled()
	{
		return getViewOMEnabled(null);
	}
	
	public void setViewOMEnabled(Boolean viewOMEnabled)
	{
		this.viewOMEnabled=viewOMEnabled;
	}
	
	public boolean isViewOMEnabled()
	{
		return getViewOMEnabled().booleanValue();
	}
	
	private Boolean getViewOMEnabled(Operation operation)
	{
		if (viewOMEnabled==null)
		{
			viewOMEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_VIEW_OM_ENABLED"));
		}
		return viewOMEnabled;
	}
	
	public void resetViewOMQuestionsEnabled()
	{
		viewOMQuestionsEnabled.clear();
	}
	
	public void resetViewOmQuestionEnabled(Question question)
	{
		if (question!=null)
		{
			viewOMQuestionsEnabled.remove(Long.valueOf(question.getId()));
		}
	}
	
	public boolean isViewOMQuestionEnabled(Question question)
	{
		boolean viewOMQuestionEnabled=false;
		if (question!=null)
		{
			viewOMQuestionEnabled=isViewOMQuestionEnabled(null,question.getId());
		}
		return viewOMQuestionEnabled; 
	}
	
	public boolean isViewOMQuestionEnabled(long questionId)
	{
		boolean viewOMQuestionEnabled=false;
		if (questionId>0L)
		{
			viewOMQuestionEnabled=isViewOMQuestionEnabled(null,questionId);
		}
		return viewOMQuestionEnabled; 
	}
	
	private boolean isViewOMQuestionEnabled(Operation operation,long questionId)
	{
		boolean viewOMQuestionEnabled=false;
		if (questionId>0L)
		{
			if (viewOMQuestionsEnabled.containsKey(Long.valueOf(questionId)))
			{
				viewOMQuestionEnabled=viewOMQuestionsEnabled.get(Long.valueOf(questionId));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				Question question=questionsService.getQuestion(operation,questionId);
				viewOMQuestionEnabled=isViewOMQuestionEnabled(operation,question);
			}
		}
		return viewOMQuestionEnabled; 
	}
	
	private boolean isViewOMQuestionEnabled(Operation operation,Question question)
	{
		boolean viewOMQuestionEnabled=false;
		if (question!=null)
		{
			if (viewOMQuestionsEnabled.containsKey(Long.valueOf(question.getId())))
			{
				viewOMQuestionEnabled=viewOMQuestionsEnabled.get(Long.valueOf(question.getId()));
			}
			else
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				User questionAuthor=question.getCreatedBy();
				viewOMQuestionEnabled=getViewOMEnabled(operation) && 
					(questionAuthor.getId()==userSessionService.getCurrentUserId() || 
					(question.getCategory().getVisibility().isGlobal() ||
					question.getCategory().getVisibility().getLevel()<=visibilitiesService.getVisibility(
					operation,"CATEGORY_VISIBILITY_PUBLIC").getLevel()) ||
					(getViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() &&
					(!isAdmin(operation,questionAuthor) || 
					getViewQuestionsFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) &&
					(!isSuperadmin(operation,questionAuthor) || 
					getViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue())));
				viewOMQuestionsEnabled.put(Long.valueOf(question.getId()),Boolean.valueOf(viewOMQuestionEnabled));
			}
		}
		return viewOMQuestionEnabled;
	}
	
	private void resetAdmins()
	{
		admins.clear();
	}
	
	private void resetAdminFromQuestionAllowed(Question question)
	{
		if (question!=null && question.getCreatedBy()!=null)
		{
			admins.remove(Long.valueOf(question.getCreatedBy().getId()));
		}
	}
	
	private boolean isAdmin(Operation operation,User user)
	{
		boolean admin=false;
		if (user!=null)
		{
			Long userId=Long.valueOf(user.getId());
			if (admins.containsKey(userId))
			{
				admin=admins.get(userId).booleanValue();
			}
			else
			{
				admin=permissionsService.isGranted(
					getCurrentUserOperation(operation),user,"PERMISSION_NAVIGATION_ADMINISTRATION");
				admins.put(userId,Boolean.valueOf(admin));
			}
		}
		return admin;
	}
	
	private void resetSuperadmins()
	{
		superadmins.clear();
	}
	
	private void resetSuperadminFromQuestionAllowed(Question question)
	{
		if (question!=null && question.getCreatedBy()!=null)
		{
			superadmins.remove(Long.valueOf(question.getCreatedBy().getId()));
		}
	}
	
	private boolean isSuperadmin(Operation operation,User user)
	{
		boolean superadmin=false;
		if (user!=null)
		{
			Long userId=Long.valueOf(user.getId());
			if (superadmins.containsKey(userId))
			{
				superadmin=superadmins.get(userId).booleanValue();
			}
			else
			{
				superadmin=permissionsService.isGranted(getCurrentUserOperation(operation),user,
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
				superadmins.put(userId,Boolean.valueOf(superadmin));
			}
		}
		return superadmin;
	}
	
    /**
     * Flow listener to handle a step change within wizard component. 
     * @param event Flow event
     * @return Next step name
     */
    public String changeStep(FlowEvent event)
    {
    	boolean ok=true;
    	String oldStep=event.getOldStep();
    	String nextStep=event.getNewStep();
    	if (CALENDAR_WIZARD_TAB.equals(oldStep))
    	{
    		ok=USERS_WIZARD_TAB.equals(nextStep) || checkCalendarInputFields();
    	}
    	else if (CONFIRMATION_WIZARD_TAB.equals(oldStep))
    	{
    		if (!publishAllowed)
    		{
    			ok=false;
    			addErrorMessage(lastErrorMessage);
    		}
    	}
    	if (!ok)
    	{
    		nextStep=oldStep;
    		
			// Scroll page to top position
			scrollToTop();
    	}
    	setEnabledCheckboxesSetters(!CONFIRMATION_WIZARD_TAB.equals(nextStep));
    	setActiveTestReleaseTabName(nextStep);
    	return nextStep;
    }
	
    public String getActiveTestReleaseTabName()
    {
    	return activeTestReleaseTabName;
    }
    
    public void setActiveTestReleaseTabName(String activeTestReleaseTabName)
    {
    	this.activeTestReleaseTabName=activeTestReleaseTabName;
    }
    
	private boolean checkCalendarInputFields()
	{
		return checkCalendarInputFields(null);
	}
	
    private boolean checkCalendarInputFields(Operation operation)
    {
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	if (isRestrictDates(operation))
    	{
    		String startDateHidden=getStartDateHidden();
    		String closeDateHidden=getCloseDateHidden();
    		String warningDateHidden=getWarningDateHidden();
    		Date startDate=getStartDate(operation);
    		Date closeDate=getCloseDate(operation);
    		Date warningDate=getWarningDate(operation);
    		if (startDate!=null && closeDate!=null && !closeDate.after(startDate))
    		{
   				addErrorMessage("TEST_CLOSE_DATE_NOT_AFTER_START_DATE");
    			ok=false;
    		}
    		if (warningDate!=null)
    		{
        		if (startDate!=null && warningDate.before(startDate))
        		{
    				addErrorMessage("TEST_WARNING_DATE_BEFORE_START_DATE");
        			ok=false;
        		}
    			if (closeDate!=null && !warningDate.before(closeDate))
    			{
    				addErrorMessage("TEST_WARNING_DATE_NOT_BEFORE_CLOSE_DATE");
    				ok=false;
    			}
    		}
    		if (isRestrictFeedbackDate(operation))
    		{
    			String feedbackDateHidden=getFeedbackDateHidden();
    			Date feedbackDate=getFeedbackDate(operation);
    			if (feedbackDate!=null)
    			{
    				if (startDate!=null && feedbackDate.before(startDate))
    				{
						addErrorMessage("TEST_FEEDBACK_DATE_BEFORE_START_DATE");
    					ok=false;
    				}
    				if (closeDate!=null && !feedbackDate.before(closeDate))
    				{
						addErrorMessage("TEST_FEEDBACK_DATE_NOT_BEFORE_CLOSE_DATE");
    					ok=false;
    				}
    			}
    			setFeedbackDateHidden(feedbackDateHidden);
    		}
    		setStartDateHidden(startDateHidden);
    		setCloseDateHidden(closeDateHidden);
    		setWarningDateHidden(warningDateHidden);
    	}
		return ok;
    }
    
	/**
	 * @return Users without permission to do this test release (except if it is allowed that all users do the 
	 * test release)
	 */
	public List<User> getAvailableUsers()
	{
		return getAvailableUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users without permission to do this test release (except if it is allowed that all users do the 
	 * test release)
	 */
	private List<User> getAvailableUsers(Operation operation)
	{
		List<User> availableUsers=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> testReleaseUsers=getTestRelease(operation).getUsers();
		for (User user:getFilteredUsersForAddingUsers(operation))
		{
			if (!testReleaseUsers.contains(user))
			{
				User availableUser=new User();
				availableUser.setFromOtherUser(user);
				availableUsers.add(availableUser);
			}
		}
		return availableUsers;
	}
	
	/**
	 * @return Users as dual list
	 */
	public DualListModel<User> getUsersDualList()
	{
		return getUsersDualList(null);
	}
	
	public void setUsersDualList(DualListModel<User> usersDualList)
	{
		this.usersDualList=usersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users as dual list
	 */
	private DualListModel<User> getUsersDualList(Operation operation)
	{
		if (usersDualList==null)
		{
			usersDualList=
				new DualListModel<User>(getAvailableUsers(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return usersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add user" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyUsersFilter(ActionEvent event)
	{
		// Reload filtered users for "Add users" dialog and its dual list
		setFilteredUsersForAddingUsers(null);
		setUsersDualList(null);
	}
	
	/**
	 * Display a dialog to add users allowed to do test. 
     * @param event Action event
	 */
	public void showAddUsers(ActionEvent event)
	{
		setUsersDualList(null);
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUsersDialog.show()");
	}
	
    /**
     * Add users selected within dialog to list of users allowed to do test.
     * @param event Action event
     */
    public void acceptAddUsers(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
   		for (User user:getUsersDualList(operation).getTarget())
  		{
   			getTestRelease(operation).getUsers().add(user);
   		}
    }
	
    /**
     * ActionListener that deletes an user from list of users allowed to do test.
     * @param event Action event
     */
    public void removeUser(ActionEvent event)
    {
		getTestRelease(
			getCurrentUserOperation(null)).getUsers().remove((User)event.getComponent().getAttributes().get("user"));
	}
    
	/**
	 * @return Users without permission to administrate this test release
	 */
	public List<User> getAvailableAdmins()
	{
		return getAvailableAdmins(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users without permission to administrate this test release
	 */
	private List<User> getAvailableAdmins(Operation operation)
	{
		List<User> availableAdmins=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> testReleaseAdmins=getTestRelease(operation).getAdmins();
		for (User admin:getFilteredUsersForAddingAdmins(operation))
		{
			if (!testReleaseAdmins.contains(admin))
			{
				User availableAdmin=new User();
				availableAdmin.setFromOtherUser(admin);
				availableAdmins.add(availableAdmin);
			}
		}
		return availableAdmins;
	}
	
	/**
	 * @return Admins as dual list
	 */
	public DualListModel<User> getAdminsDualList()
	{
		return getAdminsDualList(null);
	}
	
	public void setAdminsDualList(DualListModel<User> adminsDualList)
	{
		this.adminsDualList=adminsDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Admins as dual list
	 */
	private DualListModel<User> getAdminsDualList(Operation operation)
	{
		if (adminsDualList==null)
		{
			adminsDualList=
				new DualListModel<User>(getAvailableAdmins(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return adminsDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add admin" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyAdminsFilter(ActionEvent event)
	{
		// Reload filtered users for "Add admins" dialog and its dual list
		setFilteredUsersForAddingAdmins(null);
		setAdminsDualList(null);
	}
	
	/**
	 * Display a dialog to add users allowed to administrate test.
     * @param event Action event
	 */
	public void showAddAdmins(ActionEvent event)
	{
		setAdminsDualList(null);
   		
		// We need to process some input fields
		//processCommonDataInputFields(event.getComponent());
		//processUsersTabCommonDataInputFields(event.getComponent());
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAdminsDialog.show()");
	}
	
    /**
     * Add users selected within dialog to list of admins allowed to administrate test.
     * @param event Action event
     */
    public void acceptAddAdmins(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
   		for (User admin:getAdminsDualList(operation).getTarget())
  		{
   			getTestRelease(operation).getAdmins().add(admin);
   		}
    }
	
    /**
     * ActionListener that deletes an user from list of users allowed to administrate test.
     * @param event Action event
     */
    public void removeAdmin(ActionEvent event)
    {
		getTestRelease(
			getCurrentUserOperation(null)).getAdmins().remove((User)event.getComponent().getAttributes().get("admin"));
	}
    
	public List<SupportContactBean> getSupportContacts()
	{
		return getSupportContacts(null);
	}
	
	public void setSupportContacts(List<SupportContactBean> supportContacts)
	{
		setSupportContacts(null,supportContacts);
	}
	
	private List<SupportContactBean> getSupportContacts(Operation operation)
	{
		return getTestRelease(getCurrentUserOperation(operation)).getSupportContacts();
	}
	
	private void setSupportContacts(Operation operation,List<SupportContactBean> supportContacts)
	{
		getTestRelease(getCurrentUserOperation(operation)).setSupportContacts(supportContacts);
	}
	
	public List<EvaluatorBean> getEvaluators()
	{
		return getEvaluators(null);
	}
	
	public void setEvaluators(List<EvaluatorBean> evaluators)
	{
		setEvaluators(null,evaluators);
	}
	
	private List<EvaluatorBean> getEvaluators(Operation operation)
	{
		return getTestRelease(operation).getEvaluators();
	}
	
	private void setEvaluators(Operation operation,List<EvaluatorBean> evaluators)
	{
		getTestRelease(getCurrentUserOperation(operation)).setEvaluators(evaluators);
	}
	
	public SupportContactBean getCurrentSupportContact()
	{
		return currentSuportContact;
	}
	
	public void setCurrentSupportContact(SupportContactBean currentSupportContact)
	{
		this.currentSuportContact=currentSupportContact;
	}
	
	public EvaluatorBean getCurrentEvaluator()
	{
		return currentEvaluator;
	}
	
	public void setCurrentEvaluator(EvaluatorBean currentEvaluator)
	{
		this.currentEvaluator=currentEvaluator;
	}
	
	/**
	 * @return Users for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	public DualListModel<User> getSupportContactFilterUsersDualList()
	{
		return getSupportContactFilterUsersDualList(null);
	}
	
	public void setSupportContactFilterUsersDualList(DualListModel<User> supportContactFilterUsersDualList)
	{
		this.supportContactFilterUsersDualList=supportContactFilterUsersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users for the filter of the "Add/Edit tech support address" dialog as dual list
	 */
	private DualListModel<User> getSupportContactFilterUsersDualList(Operation operation)
	{
		if (supportContactFilterUsersDualList==null)
		{
			supportContactFilterUsersDualList=new DualListModel<User>(
				getAvailableSupportContactFilterUsers(getCurrentUserOperation(operation)),new ArrayList<User>());
		}
		return supportContactFilterUsersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add/Edit tech support address" dialog based on filter. 
	 * @param event Action event
	 */
	public void applySupportContactFilterUsersFilter(ActionEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filtered users for "Add/Edit tech support address" dialog
		setFilteredUsersForAddingSupportContactFilterUsers(null);
	}
	
	private void refreshSupportContactFilterUsersDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with users ids
		if (processSupportContactFilterUsersIdsHidden(component))
		{
			// Fill the list of users included within the current user filter of the 
			// "Add/Edit tech support address" dialog with the information from hidden field
			List<User> testSupportContactFilterUsers=getTestSupportContactFilterUsers();
			testSupportContactFilterUsers.clear();
			if (getSupportContactFilterUsersIdsHidden()!=null && 
				!"".equals(getSupportContactFilterUsersIdsHidden()))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (String userId:getSupportContactFilterUsersIdsHidden().split(","))
				{
					testSupportContactFilterUsers.add(usersService.getUser(operation,Long.parseLong(userId)));
				}
			}
			
			// Reload dual list of filtered users for "Add/Edit assessement address" dialog
			setSupportContactFilterUsersDualList(null);
		}
	}
	
	private boolean processSupportContactFilterUsersIdsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput supportContactFilterUsersIdsHiddenInput=(UIInput)component.findComponent(
			":techSupportAddressDialogForm:supportContactFilterUsersIdsHidden");
		supportContactFilterUsersIdsHiddenInput.processDecodes(context);
		if (supportContactFilterUsersIdsHiddenInput.getSubmittedValue()!=null)
		{
			setSupportContactFilterUsersIdsHidden(
				(String)supportContactFilterUsersIdsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	/**
	 * @return Users for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	public DualListModel<User> getEvaluatorFilterUsersDualList()
	{
		return getEvaluatorFilterUsersDualList(null);
	}
	
	public void setEvaluatorFilterUsersDualList(DualListModel<User> evaluatorFilterUsersDualList)
	{
		this.evaluatorFilterUsersDualList=evaluatorFilterUsersDualList;
	}
	
	/**
	 * @param operation Operation
	 * @return Users for the filter of the "Add/Edit assessement address" dialog as dual list
	 */
	private DualListModel<User> getEvaluatorFilterUsersDualList(Operation operation)
	{
		if (evaluatorFilterUsersDualList==null)
		{
			evaluatorFilterUsersDualList=new DualListModel<User>(
				getAvailableEvaluatorFilterUsers(getCurrentUserOperation(operation)),getTestEvaluatorFilterUsers());
		}
		return evaluatorFilterUsersDualList;
	}
	
	/**
	 * Change users to display on picklist of the "Add/Edit assessement address" dialog based on filter. 
	 * @param event Action event
	 */
	public void applyEvaluatorFilterUsersFilter(ActionEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filtered users for "Add/Edit assessement address" dialog
		setFilteredUsersForAddingEvaluatorFilterUsers(null);
	}
	
	private void refreshEvaluatorFilterUsersDualList(Operation operation,UIComponent component)
	{
		// Process hidden field with users ids
		if (processEvaluatorFilterUsersIdsHidden(component))
		{
			// Fill the list of users included within the current user filter of the "Add/Edit assessement address" 
			// dialog with the information from hidden field
			List<User> testEvaluatorFilterUsers=getTestEvaluatorFilterUsers();
			testEvaluatorFilterUsers.clear();
			if (getEvaluatorFilterUsersIdsHidden()!=null && !"".equals(getEvaluatorFilterUsersIdsHidden()))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (String userId:getEvaluatorFilterUsersIdsHidden().split(","))
				{
					testEvaluatorFilterUsers.add(usersService.getUser(operation,Long.parseLong(userId)));
				}
			}
			
			// Reload dual list of filtered users for "Add/Edit assessement address" dialog
			setEvaluatorFilterUsersDualList(null);
		}
	}
	
	private boolean processEvaluatorFilterUsersIdsHidden(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput evaluatorFilterUsersIdsHiddenInput=
			(UIInput)component.findComponent(":assessementAddressDialogForm:evaluatorFilterUsersIdsHidden");
		evaluatorFilterUsersIdsHiddenInput.processDecodes(context);
		if (evaluatorFilterUsersIdsHiddenInput.getSubmittedValue()!=null)
		{
			setEvaluatorFilterUsersIdsHidden((String)evaluatorFilterUsersIdsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	public String getSupportContactFilterType()
	{
		return supportContactFilterType;
	}
	
	public void setSupportContactFilterType(String supportContactFilterType)
	{
		this.supportContactFilterType=supportContactFilterType;
	}
	
	public String getSupportContactFilterSubtype()
	{
		return getSupportContactFilterSubtype(null);
	}
	
	public void setSupportContactFilterSubtype(String supportContactFilterSubtype)
	{
		this.supportContactFilterSubtype=supportContactFilterSubtype;
	}
	
	public String getSupportContactFilterSubtype(Operation operation)
	{
		if (supportContactFilterSubtype==null)
		{
			List<String> supportContactFilterSubtypes=
				getSupportContactFilterSubtypes(getCurrentUserOperation(operation));
			if (!supportContactFilterSubtypes.isEmpty())
			{
				supportContactFilterSubtype=supportContactFilterSubtypes.get(0);
			}
		}
		return supportContactFilterSubtype;
	}
	
	public List<String> getSupportContactFilterSubtypes()
	{
		return getSupportContactFilterSubtypes(null);
	}
	
	public void setSupportContactFilterSubtypes(List<String> supportContactFilterSubtypes)
	{
		this.supportContactFilterSubtypes=supportContactFilterSubtypes;
	}
	
	private List<String> getSupportContactFilterSubtypes(Operation operation)
	{
		if (supportContactFilterSubtypes==null)
		{
			supportContactFilterSubtypes=new ArrayList<String>();
			for (AddressType addressType:
				addressTypesService.getAddressTypes(getCurrentUserOperation(operation),getSupportContactFilterType()))
			{
				String subtype=addressType.getSubtype();
				if (subtype!=null && !subtype.equals(""))
				{
					supportContactFilterSubtypes.add(addressType.getSubtype());
				}
			}
		}
		return supportContactFilterSubtypes;
	}
	
	public boolean isSupportContactFilterSubtypeEnabled()
	{
		return isSupportContactFilterSubtypeEnabled(null);
	}
	
	private boolean isSupportContactFilterSubtypeEnabled(Operation operation)
	{
		String filterSubtype=getSupportContactFilterSubtype(getCurrentUserOperation(operation));
		return filterSubtype!=null && !"".equals(filterSubtype);
	}
	
	/**
	 * Change filter of the "Add tech support address" dialog. 
	 * @param event Ajax event
	 */
	public void changeSupportContactFilterType(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshSupportContactFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filter subtypes for the "Add tech support address" dialog
		setSupportContactFilterSubtypes(null);
		setSupportContactFilterSubtype(null);
	}
	
	/**
	 * Change filter subtype of the "Add tech support address" dialog. 
	 * @param event Ajax event
	 */
	public void changeSupportContactFilterSubtype(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit tech support address" dialog
		refreshSupportContactFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
	}
	
	public long getFilterSupportContactFilterUsersUserTypeId()
	{
		return filterSupportContactFilterUsersUserTypeId;
	}
	
	public void setFilterSupportContactFilterUsersUserTypeId(long filterSupportContactFilterUsersUserTypeId)
	{
		this.filterSupportContactFilterUsersUserTypeId=filterSupportContactFilterUsersUserTypeId;
	}
	
	public boolean isFilterSupportContactFilterUsersIncludeOmUsers()
	{
		return filterSupportContactFilterUsersIncludeOmUsers;
	}
	
	public void setFilterSupportContactFilterUsersIncludeOmUsers(
		boolean filterSupportContactFilterUsersIncludeOmUsers)
	{
		this.filterSupportContactFilterUsersIncludeOmUsers=filterSupportContactFilterUsersIncludeOmUsers;
	}
	
	/**
	 * @return Users included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	public List<User> getTestSupportContactFilterUsers()
	{
		return testSupportContactFilterUsers;
	}
	
	/**
	 * @param testSupportContactFilterUsers Users included within the current user filter of the 
	 * "Add/Edit tech support address" dialog
	 */
	public void setTestSupportContactFilterUsers(List<User> testSupportContactFilterUsers)
	{
		this.testSupportContactFilterUsers=testSupportContactFilterUsers;
	}
	
	/**
	 * @return User's identifiers included within the current user filter of the "Add/Edit tech support address" 
	 * dialog as a string with the identifiers separated by commas
	 */
	public String getSupportContactFilterUsersIdsHidden()
	{
		return supportContactFilterUsersIdsHidden;
	}
	
	/**
	 * @param supportContactFilterUsersIdsHidden User's identifiers included within the current user filter 
	 * of the "Add/Edit tech support address" dialog as a string with the identifiers separated by commas
	 */
	public void setSupportContactFilterUsersIdsHidden(String supportContactFilterUsersIdsHidden)
	{
		this.supportContactFilterUsersIdsHidden=supportContactFilterUsersIdsHidden;
	}
	
	/**
	 * @return Users not included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	public List<User> getAvailableSupportContactFilterUsers()
	{
		return getAvailableSupportContactFilterUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users included within the current user filter of the "Add/Edit tech support address" dialog
	 */
	private List<User> getAvailableSupportContactFilterUsers(Operation operation)
	{
		List<User> availableSupportContactFilterUsers=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> testSupportContactFilterUsers=getTestSupportContactFilterUsers();
		for (User user:getFilteredUsersForAddingSupportContactFilterUsers(operation))
		{
			if (!testSupportContactFilterUsers.contains(user))
			{
				User availableSupportContactFilterUser=new User();
				availableSupportContactFilterUser.setFromOtherUser(user);
				availableSupportContactFilterUsers.add(availableSupportContactFilterUser);
			}
		}
		return availableSupportContactFilterUsers;
	}
	
	public String getFilterSupportContactRangeNameLowerLimit()
	{
		return filterSupportContactRangeNameLowerLimit;
	}
	
	public void setFilterSupportContactRangeNameLowerLimit(String filterSupportContactRangeNameLowerLimit)
	{
		this.filterSupportContactRangeNameLowerLimit=filterSupportContactRangeNameLowerLimit;
	}
	
	public String getFilterSupportContactRangeNameUpperLimit()
	{
		return filterSupportContactRangeNameUpperLimit;
	}
	
	public void setFilterSupportContactRangeNameUpperLimit(String filterSupportContactRangeNameUpperLimit)
	{
		this.filterSupportContactRangeNameUpperLimit=filterSupportContactRangeNameUpperLimit;
	}
	
	public String getFilterSupportContactRangeSurnameLowerLimit()
	{
		return filterSupportContactRangeSurnameLowerLimit;
	}
	
	public void setFilterSupportContactRangeSurnameLowerLimit(String filterSupportContactRangeSurnameLowerLimit)
	{
		this.filterSupportContactRangeSurnameLowerLimit=filterSupportContactRangeSurnameLowerLimit;
	}
	
	public String getFilterSupportContactRangeSurnameUpperLimit()
	{
		return filterSupportContactRangeSurnameUpperLimit;
	}
	
	public void setFilterSupportContactRangeSurnameUpperLimit(String filterSupportContactRangeSurnameUpperLimit)
	{
		this.filterSupportContactRangeSurnameUpperLimit=filterSupportContactRangeSurnameUpperLimit;
	}
	
	public String getEvaluatorFilterType()
	{
		return evaluatorFilterType;
	}
	
	public void setEvaluatorFilterType(String evaluatorFilterType)
	{
		this.evaluatorFilterType=evaluatorFilterType;
	}
	
	public String getEvaluatorFilterSubtype()
	{
		return getEvaluatorFilterSubtype(null);
	}
	
	public void setEvaluatorFilterSubtype(String evaluatorFilterSubtype)
	{
		this.evaluatorFilterSubtype=evaluatorFilterSubtype;
	}
	
	private String getEvaluatorFilterSubtype(Operation operation)
	{
		if (evaluatorFilterSubtype==null)
		{
			List<String> evaluatorFilterSubtypes=getEvaluatorFilterSubtypes(getCurrentUserOperation(operation));
			if (!evaluatorFilterSubtypes.isEmpty())
			{
				evaluatorFilterSubtype=evaluatorFilterSubtypes.get(0);
			}
		}
		return evaluatorFilterSubtype;
	}
	
	public List<String> getEvaluatorFilterSubtypes()
	{
		return getEvaluatorFilterSubtypes(null);
	}
	
	public void setEvaluatorFilterSubtypes(List<String> evaluatorFilterSubtypes)
	{
		this.evaluatorFilterSubtypes=evaluatorFilterSubtypes;
	}
	
	private List<String> getEvaluatorFilterSubtypes(Operation operation)
	{
		if (evaluatorFilterSubtypes==null)
		{
			evaluatorFilterSubtypes=new ArrayList<String>();
			for (AddressType addressType:
				addressTypesService.getAddressTypes(getCurrentUserOperation(operation),getEvaluatorFilterType()))
			{
				String subtype=addressType.getSubtype();
				if (subtype!=null && !subtype.equals(""))
				{
					evaluatorFilterSubtypes.add(addressType.getSubtype());
				}
			}
		}
		return evaluatorFilterSubtypes;
	}
	
	public boolean isEvaluatorFilterSubtypeEnabled()
	{
		return isEvaluatorFilterSubtypeEnabled(null);
	}
	
	private boolean isEvaluatorFilterSubtypeEnabled(Operation operation)
	{
		String filterSubtype=getEvaluatorFilterSubtype(getCurrentUserOperation(operation));
		return filterSubtype!=null && !"".equals(filterSubtype);
	}
	
	/**
	 * Change filter type of the "Add assessement address" dialog. 
	 * @param event Ajax event
	 */
	public void changeEvaluatorFilterType(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
		
		// Reload filter subtypes for the "Add assessement address" dialog
		setEvaluatorFilterSubtypes(null);
		setEvaluatorFilterSubtype(null);
	}
	
	/**
	 * Change filter subtype of the "Add assessement address" dialog. 
	 * @param event Ajax event
	 */
	public void changeEvaluatorFilterSubtype(AjaxBehaviorEvent event)
	{
		// Refresh dual list of filtered users for "Add/Edit assessement address" dialog
		refreshEvaluatorFilterUsersDualList(getCurrentUserOperation(null),event.getComponent());
	}
	
	public long getFilterEvaluatorFilterUsersUserTypeId()
	{
		return filterEvaluatorFilterUsersUserTypeId;
	}
	
	public void setFilterEvaluatorFilterUsersUserTypeId(long filterEvaluatorFilterUsersUserTypeId)
	{
		this.filterEvaluatorFilterUsersUserTypeId=filterEvaluatorFilterUsersUserTypeId;
	}
	
	public boolean isFilterEvaluatorFilterUsersIncludeOmUsers()
	{
		return filterEvaluatorFilterUsersIncludeOmUsers;
	}
	
	public void setFilterEvaluatorFilterUsersIncludeOmUsers(boolean filterEvaluatorFilterUsersIncludeOmUsers)
	{
		this.filterEvaluatorFilterUsersIncludeOmUsers=filterEvaluatorFilterUsersIncludeOmUsers;
	}
	
	/**
	 * @return Users included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	public List<User> getTestEvaluatorFilterUsers()
	{
		return testEvaluatorFilterUsers;
	}
	
	/**
	 * @param testEvaluatorFilterUsers Users included within the current user filter of the 
	 * "Add/Edit assessement address" dialog
	 */
	public void setTestEvaluatorFilterUsers(List<User> testEvaluatorFilterUsers)
	{
		this.testEvaluatorFilterUsers=testEvaluatorFilterUsers;
	}
	
	/**
	 * @return User's identifiers included within the current user filter of the "Add/Edit assessement address" 
	 * dialog as a string with the identifiers separated by commas
	 */
	public String getEvaluatorFilterUsersIdsHidden()
	{
		return evaluatorFilterUsersIdsHidden;
	}
	
	/**
	 * @param evaluatorFilterUsersIdsHidden User's identifiers included within the current user filter 
	 * of the "Add/Edit assessement address" dialog as a string with the identifiers separated by commas
	 */
	public void setEvaluatorFilterUsersIdsHidden(String evaluatorFilterUsersIdsHidden)
	{
		this.evaluatorFilterUsersIdsHidden=evaluatorFilterUsersIdsHidden;
	}
	
	/**
	 * @return Users not included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	public List<User> getAvailableEvaluatorFilterUsers()
	{
		return getAvailableEvaluatorFilterUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users included within the current user filter of the "Add/Edit assessement address" dialog
	 */
	private List<User> getAvailableEvaluatorFilterUsers(Operation operation)
	{
		List<User> availableEvaluatorFilterUsers=new ArrayList<User>();
		List<User> testEvaluatorFilterUsers=getTestEvaluatorFilterUsers();
		for (User user:getFilteredUsersForAddingEvaluatorFilterUsers(getCurrentUserOperation(operation)))
		{
			if (!testEvaluatorFilterUsers.contains(user))
			{
				User availableEvaluatorFilterUser=new User();
				availableEvaluatorFilterUser.setFromOtherUser(user);
				availableEvaluatorFilterUsers.add(availableEvaluatorFilterUser);
			}
		}
		return availableEvaluatorFilterUsers;
	}
	
	public String getFilterEvaluatorRangeNameLowerLimit()
	{
		return filterEvaluatorRangeNameLowerLimit;
	}
	
	public void setFilterEvaluatorRangeNameLowerLimit(String filterEvaluatorRangeNameLowerLimit)
	{
		this.filterEvaluatorRangeNameLowerLimit=filterEvaluatorRangeNameLowerLimit;
	}
	
	public String getFilterEvaluatorRangeNameUpperLimit()
	{
		return filterEvaluatorRangeNameUpperLimit;
	}
	
	public void setFilterEvaluatorRangeNameUpperLimit(String filterEvaluatorRangeNameUpperLimit)
	{
		this.filterEvaluatorRangeNameUpperLimit=filterEvaluatorRangeNameUpperLimit;
	}
	
	public String getFilterEvaluatorRangeSurnameLowerLimit()
	{
		return filterEvaluatorRangeSurnameLowerLimit;
	}
	
	public void setFilterEvaluatorRangeSurnameLowerLimit(String filterEvaluatorRangeSurnameLowerLimit)
	{
		this.filterEvaluatorRangeSurnameLowerLimit=filterEvaluatorRangeSurnameLowerLimit;
	}
	
	public String getFilterEvaluatorRangeSurnameUpperLimit()
	{
		return filterEvaluatorRangeSurnameUpperLimit;
	}
	
	public void setFilterEvaluatorRangeSurnameUpperLimit(String filterEvaluatorRangeSurnameUpperLimit)
	{
		this.filterEvaluatorRangeSurnameUpperLimit=filterEvaluatorRangeSurnameUpperLimit;
	}
	
	public String getSupportContact()
	{
		return supportContact;
	}
	
	public void setSupportContact(String supportContact)
	{
		this.supportContact=supportContact;
	}
	
	public boolean isSupportContactDialogDisplayed()
	{
		return supportContactDialogDisplayed;
	}
	
	public void setSupportContactDialogDisplayed(boolean supportContactDialogDisplayed)
	{
		this.supportContactDialogDisplayed=supportContactDialogDisplayed;
	}
	
	public String getEvaluator()
	{
		return evaluator;
	}
	
	public void setEvaluator(String evaluator)
	{
		this.evaluator=evaluator;
	}
	
	public boolean isEvaluatorDialogDisplayed()
	{
		return evaluatorDialogDisplayed;
	}
	
	public void setEvaluatorDialogDisplayed(boolean evaluatorDialogDisplayed)
	{
		this.evaluatorDialogDisplayed=evaluatorDialogDisplayed;
	}
	
    /**
     * @return Letters from A to Z
     */
    public List<String> getLetters()
    {
    	if (letters==null)
    	{
        	letters=new ArrayList<String>();
        	for (char c='A';c<='Z';c++)
        	{
        		letters.add(Character.toString(c));
        	}
    	}
    	return letters;
    }
	
	/**
	 * Display a dialog to add a support contact. 
     * @param event Action event
	 */
	public void showAddSupportContact(ActionEvent event)
	{
	   	setCurrentSupportContact(null);
		setSupportContactFilterType("NO_FILTER");
		setSupportContactFilterSubtype("");
	   	getTestSupportContactFilterUsers().clear();
	   	setFilteredUsersForAddingSupportContactFilterUsers(null);
	   	setSupportContactFilterUsersDualList(null);
	   	setFilterSupportContactRangeNameLowerLimit("A");
	   	setFilterSupportContactRangeNameUpperLimit("Z");
	   	setFilterSupportContactRangeSurnameLowerLimit("A");
	   	setFilterSupportContactRangeSurnameUpperLimit("Z");
	   	setSupportContact("");
	   	setSupportContactDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addTechSupportAddressDialog.show()");
	}
	
	/**
	 * Display a dialog to edit a support contact. 
     * @param event Action event
	 */
	public void showEditSupportContact(ActionEvent event)
	{
		// Copy support contact so we work at dialog with a copy
		SupportContactBean supportContact=(SupportContactBean)event.getComponent().getAttributes().get("supportContact");
		setCurrentSupportContact(new SupportContactBean(supportContact.getSupportContact(),
			supportContact.getFilterType(),supportContact.getFilterSubtype(),supportContact.getFilterValue()));
		getCurrentSupportContact().setId(supportContact.getId());
		String filterType=getCurrentSupportContact().getFilterType();
		String filterSubtype=getCurrentSupportContact().getFilterSubtype();
		String filterValue=getCurrentSupportContact().getFilterValue();
		setSupportContactFilterType(filterType);
		setSupportContactFilterSubtype(filterSubtype);
		setSupportContactFilterSubtypes(null);
		getTestSupportContactFilterUsers().clear();
		setFilteredUsersForAddingSupportContactFilterUsers(null);
		setSupportContactFilterUsersDualList(null);
		setFilterSupportContactRangeNameLowerLimit("A");
		setFilterSupportContactRangeNameUpperLimit("Z");
		setFilterSupportContactRangeSurnameLowerLimit("A");
		setFilterSupportContactRangeSurnameUpperLimit("Z");
		if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
		{
			if ("USERS_SELECTION".equals(filterSubtype))
			{
				// Get current user session Hibernate operation
				Operation operation=getCurrentUserOperation(null);
				
				List<String> checkedOUCUs=new ArrayList<String>();
				for (String sOUCU:filterValue.split(","))
				{
					if (!checkedOUCUs.contains(sOUCU))
					{
						User user=usersService.getUserFromOucu(operation,sOUCU);
						if (user!=null)
						{
							getTestSupportContactFilterUsers().add(user);
						}
						checkedOUCUs.add(sOUCU);
					}
				}
			}
			else if ("RANGE_NAME".equals(filterSubtype))
			{
				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
				{
					char lowerLimitChar=filterValue.charAt(0);
					char upperLimitChar=filterValue.charAt(2);
					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
					{
						setFilterSupportContactRangeNameLowerLimit(Character.toString(lowerLimitChar));
					}
					if (upperLimitChar>='A' && upperLimitChar<='Z')
					{
						setFilterSupportContactRangeNameUpperLimit(Character.toString(upperLimitChar));
					}
				}
			}
			else if ("RANGE_SURNAME".equals(filterSubtype))
			{
				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
				{
					char lowerLimitChar=filterValue.charAt(0);
					char upperLimitChar=filterValue.charAt(2);
					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
					{
						setFilterSupportContactRangeSurnameLowerLimit(Character.toString(lowerLimitChar));
					}
					if (upperLimitChar>='A' && upperLimitChar<='Z')
					{
						setFilterSupportContactRangeSurnameUpperLimit(Character.toString(upperLimitChar));
					}
				}
			}
		}
		setSupportContact(getCurrentSupportContact().getSupportContact());
		setSupportContactDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addTechSupportAddressDialog.show()");
	}
    
    /**
     * Adds a support contact.
     * @param event Action event
     */
	public void acceptAddSupportContact(ActionEvent event)
	{
		// Check support contact
		if (checkSupportContact())
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			String filterType=getSupportContactFilterType();
			String filterSubtype=getSupportContactFilterSubtype(operation);
			if (filterSubtype==null)
			{
				filterSubtype="";
			}
			StringBuffer filterValue=new StringBuffer();
			if ("USER_FILTER".equals(filterType))
			{
				if ("USERS_SELECTION".equals(filterSubtype))
				{
					refreshSupportContactFilterUsersDualList(operation,event.getComponent());
					for (User supportedUser:getTestSupportContactFilterUsers())
					{
						if (filterValue.length()>0)
						{
							filterValue.append(',');
						}
						filterValue.append(supportedUser.getOucu());
					}
				}
				else if ("RANGE_NAME".equals(filterSubtype))
				{
					filterValue.append(getFilterSupportContactRangeNameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterSupportContactRangeNameUpperLimit());
				}
				else if ("RANGE_SURNAME".equals(filterSubtype))
				{
					filterValue.append(getFilterSupportContactRangeSurnameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterSupportContactRangeSurnameUpperLimit());
				}
			}
			SupportContactBean currentSupportContact=getCurrentSupportContact();
			if (currentSupportContact==null)
			{
				// Add a new support contact
				getSupportContacts(operation).add(
					new SupportContactBean(getSupportContact(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				SupportContactBean supportContact=null;
				for (SupportContactBean s:getSupportContacts(operation))
				{
					if (currentSupportContact.equals(s))
					{
						supportContact=s;
					}
				}
				if (supportContact!=null)
				{
					supportContact.setSupportContact(getSupportContact());
					supportContact.setFilterType(filterType);
					supportContact.setFilterSubtype(filterSubtype);
					supportContact.setFilterValue(filterValue.toString());
				}
			}
	   		setSupportContactDialogDisplayed(false);
			
			// Close dialog
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addTechSupportAddressDialog.hide()");
		}
	}
	
    /**
     * @param event Action event
     */
	public void cancelAddSupportContact(ActionEvent event)
	{
   		setSupportContactDialogDisplayed(false);
	}
	
	/**
	 * Deletes a support contact.
     * @param event Action event
	 */
	public void removeSupportContact(ActionEvent event)
	{
		getSupportContacts(
			getCurrentUserOperation(null)).remove(event.getComponent().getAttributes().get("supportContact"));
	}
	
	/**
	 * Check that the support contact is a valid email address, displaying an error message otherwise.
	 * @return true if the support contact is a valid email address, false otherwise
	 */
	public boolean checkSupportContact()
	{
		boolean ok=true;
		if (getSupportContact()==null || "".equals(getSupportContact()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_REQUIRED");
		}
		else if (!EmailValidator.validate(getSupportContact()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_INVALID");
		}
		return ok;
	}
    
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeNameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeNameLowerLimit().compareTo(getFilterSupportContactRangeNameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeNameUpperLimit(getFilterSupportContactRangeNameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeNameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeNameLowerLimit().compareTo(getFilterSupportContactRangeNameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeNameLowerLimit(getFilterSupportContactRangeNameUpperLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeSurnameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeSurnameLowerLimit().compareTo(
    		getFilterSupportContactRangeSurnameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeSurnameUpperLimit(getFilterSupportContactRangeSurnameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterSupportContactRangeSurnameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterSupportContactRangeSurnameLowerLimit().compareTo(
    		getFilterSupportContactRangeSurnameUpperLimit())>0)
    	{
    		setFilterSupportContactRangeSurnameLowerLimit(getFilterSupportContactRangeSurnameUpperLimit());
    	}
    }
	
	/**
	 * Display a dialog to add an evaluator. 
     * @param event Action event
	 */
	public void showAddEvaluator(ActionEvent event)
	{
		setCurrentEvaluator(null);
		setEvaluatorFilterType("NO_FILTER");
		setEvaluatorFilterSubtype("");
		getTestEvaluatorFilterUsers().clear();
		setFilteredUsersForAddingEvaluatorFilterUsers(null);
		setEvaluatorFilterUsersDualList(null);
		setFilterEvaluatorRangeNameLowerLimit("A");
		setFilterEvaluatorRangeNameUpperLimit("Z");
		setFilterEvaluatorRangeSurnameLowerLimit("A");
		setFilterEvaluatorRangeSurnameUpperLimit("Z");
		setEvaluator("");
		setEvaluatorDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAssessementAddressDialog.show()");
	}
	
	/**
	 * Display a dialog to edit an evaluator. 
     * @param event Action event
	 */
	public void showEditEvaluator(ActionEvent event)
	{
		// Copy evaluator so we work at dialog with a copy
		EvaluatorBean evaluator=(EvaluatorBean)event.getComponent().getAttributes().get("evaluator");
		setCurrentEvaluator(new EvaluatorBean(
			evaluator.getEvaluator(),evaluator.getFilterType(),evaluator.getFilterSubtype(),evaluator.getFilterValue()));
		getCurrentEvaluator().setId(evaluator.getId());
		String filterType=getCurrentEvaluator().getFilterType();
		String filterSubtype=getCurrentEvaluator().getFilterSubtype();
		String filterValue=getCurrentEvaluator().getFilterValue();
		setEvaluatorFilterType(filterType);
		setEvaluatorFilterSubtype(filterSubtype);
		setEvaluatorFilterSubtypes(null);
		getTestEvaluatorFilterUsers().clear();
		setFilteredUsersForAddingEvaluatorFilterUsers(null);
		setEvaluatorFilterUsersDualList(null);
		setFilterEvaluatorRangeNameLowerLimit("A");
		setFilterEvaluatorRangeNameUpperLimit("Z");
		setFilterEvaluatorRangeSurnameLowerLimit("A");
		setFilterEvaluatorRangeSurnameUpperLimit("Z");
		if (filterValue!=null && !"".equals(filterValue) && "USER_FILTER".equals(filterType))
		{
			if ("USERS_SELECTION".equals(filterSubtype))
			{
				List<String> checkedOUCUs=new ArrayList<String>();
				for (String sOUCU:filterValue.split(","))
				{
					// Get current user session Hibernate operation 
					Operation operation=getCurrentUserOperation(null);
					
					if (!checkedOUCUs.contains(sOUCU))
					{
						User user=usersService.getUserFromOucu(operation,sOUCU);
						if (user!=null)
						{
							getTestEvaluatorFilterUsers().add(user);
						}
						checkedOUCUs.add(sOUCU);
					}
				}
			}
			else if ("RANGE_NAME".equals(filterSubtype))
			{
				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
				{
					char lowerLimitChar=filterValue.charAt(0);
					char upperLimitChar=filterValue.charAt(2);
					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
					{
						setFilterEvaluatorRangeNameLowerLimit(Character.toString(lowerLimitChar));
					}
					if (upperLimitChar>='A' && upperLimitChar<='Z')
					{
						setFilterEvaluatorRangeNameUpperLimit(Character.toString(upperLimitChar));
					}
				}
			}
			else if ("RANGE_SURNAME".equals(filterSubtype))
			{
				if (filterValue.length()==3 && filterValue.charAt(1)=='-')
				{
					char lowerLimitChar=filterValue.charAt(0);
					char upperLimitChar=filterValue.charAt(2);
					if (lowerLimitChar>='A' && lowerLimitChar<='Z')
					{
						setFilterEvaluatorRangeSurnameLowerLimit(Character.toString(lowerLimitChar));
					}
					if (upperLimitChar>='A' && upperLimitChar<='Z')
					{
						setFilterEvaluatorRangeSurnameUpperLimit(Character.toString(upperLimitChar));
					}
				}
			}
		}
		setEvaluator(getCurrentEvaluator().getEvaluator());
		setEvaluatorDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addAssessementAddressDialog.show()");
	}
	
    /**
     * Adds an evaluator.
     * @param event Action event
     */
	public void acceptAddEvaluator(ActionEvent event)
	{
		// Check evaluator
		if (checkEvaluator())
		{
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			String filterType=getEvaluatorFilterType();
			String filterSubtype=getEvaluatorFilterSubtype(operation);
			if (filterSubtype==null)
			{
				filterSubtype="";
			}
			StringBuffer filterValue=new StringBuffer();
			if ("USER_FILTER".equals(filterType))
			{
				if ("USERS_SELECTION".equals(filterSubtype))
				{
					refreshEvaluatorFilterUsersDualList(operation,event.getComponent());
					for (User assessedUser:getTestEvaluatorFilterUsers())
					{
						if (filterValue.length()>0)
						{
							filterValue.append(',');
						}
						filterValue.append(assessedUser.getOucu());
					}
				}
				else if ("RANGE_NAME".equals(filterSubtype))
				{
					filterValue.append(getFilterEvaluatorRangeNameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterEvaluatorRangeNameUpperLimit());
				}
				else if ("RANGE_SURNAME".equals(filterSubtype))
				{
					filterValue.append(getFilterEvaluatorRangeSurnameLowerLimit());
					filterValue.append('-');
					filterValue.append(getFilterEvaluatorRangeSurnameUpperLimit());
				}
			}
			EvaluatorBean currentEvaluator=getCurrentEvaluator();
			if (currentEvaluator==null)
			{
				// Add a new evaluator
				getEvaluators(operation).add(
					new EvaluatorBean(getEvaluator(),filterType,filterSubtype,filterValue.toString()));
			}
			else
			{
				EvaluatorBean evaluator=null;
				for (EvaluatorBean e:getEvaluators(operation))
				{
					if (currentEvaluator.equals(e))
					{
						evaluator=e;
					}
				}
				if (evaluator!=null)
				{
					evaluator.setEvaluator(getEvaluator());
					evaluator.setFilterType(filterType);
					evaluator.setFilterSubtype(filterSubtype);
					evaluator.setFilterValue(filterValue.toString());
				}
			}
			
	   		setEvaluatorDialogDisplayed(false);
			
			// Close dialog
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("addAssessementAddressDialog.hide()");
		}
	}
	
    /**
     * @param event Action event
     */
	public void cancelAddEvaluator(ActionEvent event)
	{
   		setEvaluatorDialogDisplayed(false);
	}
	
	/**
	 * Deletes an evaluator.
     * @param event Action event
	 */
	public void removeEvaluator(ActionEvent event)
	{
		getEvaluators(getCurrentUserOperation(null)).remove(event.getComponent().getAttributes().get("evaluator"));
	}
	
	/**
	 * Check that the evaluator to add is a valid email address, displaying an error message otherwise.
	 * @return true if the evaluator to add is a not used valid email address, false otherwise
	 */
	public boolean checkEvaluator()
	{
		boolean ok=true;
		if (getEvaluator()==null || "".equals(getEvaluator()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_REQUIRED");
		}
		else if (!EmailValidator.validate(getEvaluator()))
		{
			ok=false;
			addErrorMessage("INCORRECT_OPERATION","TEST_ADDRESS_INVALID");
		}
		return ok;
	}
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeNameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeNameLowerLimit().compareTo(getFilterEvaluatorRangeNameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeNameUpperLimit(getFilterEvaluatorRangeNameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeNameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeNameLowerLimit().compareTo(getFilterEvaluatorRangeNameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeNameLowerLimit(getFilterEvaluatorRangeNameUpperLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeSurnameLowerLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeSurnameLowerLimit().compareTo(getFilterEvaluatorRangeSurnameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeSurnameUpperLimit(getFilterEvaluatorRangeSurnameLowerLimit());
    	}
    }
	
    /**
     * @param event Ajax event
     */
    public void changeFilterEvaluatorRangeSurnameUpperLimit(AjaxBehaviorEvent event)
    {
    	if (getFilterEvaluatorRangeSurnameLowerLimit().compareTo(getFilterEvaluatorRangeSurnameUpperLimit())>0)
    	{
    		setFilterEvaluatorRangeSurnameLowerLimit(getFilterEvaluatorRangeSurnameUpperLimit());
    	}
    }
	
    /**
     * @param section Section
     * @return Section's name
     */
    public String getSectionName(Section section)
    {
    	return getSectionName(null,section);
    }
    
    /**
     * @param operation Operation
     * @param section Section
     * @return Section's name
     */
    private String getSectionName(Operation operation,Section section)
    {
    	StringBuffer sectionName=new StringBuffer();
    	sectionName.append(localizationService.getLocalizedMessage("SECTION"));
    	sectionName.append(' ');
    	sectionName.append(section.getOrder());
    	if ((section.getName()!=null && !section.getName().equals("")))
    	{
    		sectionName.append(": ");
    		sectionName.append(getNumberedSectionName(getCurrentUserOperation(operation),section));
    	}
    	return sectionName.toString();
    }
    
    /**
     * @param operation Operation
     * @param section Section
     * @return Section's name with a number appended if it is needed to distinguish sections with the same name
     */
    private String getNumberedSectionName(Operation operation,Section section)
    {
    	StringBuffer sectionName=new StringBuffer();
    	if (section!=null)
    	{
        	if (section.getName()!=null && !section.getName().equals(""))
        	{
        		
        		sectionName.append(section.getName());
        		int itNumber=1;
        		for (Section s:getTestRelease(getCurrentUserOperation(operation)).getTest().getSections())
        		{
        			if (s.getOrder()<section.getOrder() && section.getName().equals(s.getName()))
        			{
        				itNumber++;
        			}
        		}
        		if (itNumber>1)
        		{
        			sectionName.append('(');
        			sectionName.append(itNumber);
        			sectionName.append(')');
        		}
        	}
        	else
        	{
        		sectionName.append(localizationService.getLocalizedMessage("SECTION"));
        		sectionName.append(' ');
        		sectionName.append(section.getOrder());
        	}
    	}
    	return sectionName.toString();
    }
    
    /**
     * @return true if input boxes for sections weights are displayed, false otherwise
     */
    public boolean isSectionsWeightsDisplayed()
    {
    	return isSectionsWeightsDisplayed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if input boxes for sections weights are displayed, false otherwise
     */
    private boolean isSectionsWeightsDisplayed(Operation operation)
    {
    	ScoreType scoreType=getTestRelease(getCurrentUserOperation(operation)).getTest().getScoreType();
    	return scoreType!=null && "SCORE_TYPE_SECTIONS".equals(scoreType.getType());
    }
    
    /**
     * @param qt1 Type of first question
     * @param qt2 Type of second question
     * @return 1 if type of first question greater than type of second question,
     * -1 if type of first question less than type of second question,
     * 0 if both questions have same type.
     */
    public int sortQuestionTypes(Object qt1,Object qt2)
    {
    	List<QuestionType> questionTypes=questionTypesService.getQuestionTypes();
    	QuestionType questionType1=questionTypesService.getQuestionType((String)qt1);
    	QuestionType questionType2=questionTypesService.getQuestionType((String)qt2);
    	int iQT1=questionTypes.indexOf(questionType1);
    	int iQT2=questionTypes.indexOf(questionType2);
    	return iQT1>iQT2?1:iQT1<iQT2?-1:0;
    }
    
    /**
     * Reset start date.
     * @param event Action event
     */
    public void resetStartDate(ActionEvent event)
    {
    	setStartDate(getCurrentUserOperation(null),null);
    	setStartDateHidden("");
    	
    	// We need to update manually 'startDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput startDateHidden=(UIInput)event.getComponent().findComponent(":testReleaseForm:startDateHidden");
    	startDateHidden.pushComponentToEL(context,null);
    	startDateHidden.setSubmittedValue("");
    	startDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset close date.
     * @param event Action event
     */
    public void resetCloseDate(ActionEvent event)
    {
    	setCloseDate(getCurrentUserOperation(null),null);
    	setCloseDateHidden("");
    	
    	// We need to update manually 'closeDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput closeDateHidden=(UIInput)event.getComponent().findComponent(":testReleaseForm:closeDateHidden");
    	closeDateHidden.pushComponentToEL(context,null);
    	closeDateHidden.setSubmittedValue("");
    	closeDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset warning date.
     * @param event Action event
     */
    public void resetWarningDate(ActionEvent event)
    {
    	setWarningDate(getCurrentUserOperation(null),null);
    	setWarningDateHidden("");
    	
    	// We need to update manually 'warningDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput warningDateHidden=(UIInput)event.getComponent().findComponent(":testReleaseForm:warningDateHidden");
    	warningDateHidden.pushComponentToEL(context,null);
    	warningDateHidden.setSubmittedValue("");
    	warningDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset feedback date.
     * @param event Action event
     */
    public void resetFeedbackDate(ActionEvent event)
    {
    	setFeedbackDate(getCurrentUserOperation(null),null);
    	setFeedbackDateHidden("");
    	
    	// We need to update manually 'feedbackDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput feedbackDateHidden=(UIInput)event.getComponent().findComponent(":testReleaseForm:feedbackDateHidden");
    	feedbackDateHidden.pushComponentToEL(context,null);
    	feedbackDateHidden.setSubmittedValue("");
    	feedbackDateHidden.popComponentFromEL(context);
    }
    
    /**
     * Reset delete date.
     * @param event Action event
     */
    public void resetDeleteDate(ActionEvent event)
    {
    	setDeleteDate(getCurrentUserOperation(null),null);
    	setDeleteDateHidden("");
    	
    	// We need to update manually 'deleteDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput deleteDateHidden=
    		(UIInput)event.getComponent().findComponent(":testReleaseForm:deleteDateHidden");
    	deleteDateHidden.pushComponentToEL(context,null);
    	deleteDateHidden.setSubmittedValue("");
    	deleteDateHidden.popComponentFromEL(context);
    }
    
	/**
	 * Display a question in OM Test Navigator web application.
	 * @param questionId Question's identifier
	 * @return Next view
	 * @throws Exception
	 */
	public String viewOM(long questionId) throws Exception
	{
		// Get current user sesssion Hibernate operation
		Operation operation=getCurrentUserOperation(null);
			
		// Get question
		Question question=questionsService.getQuestion(operation,questionId);
		
		setViewOMEnabled(null);
		setViewQuestionsFromOtherUsersPrivateCategoriesEnabled(null);
		setViewQuestionsFromAdminsPrivateCategoriesEnabled(null);
		setViewQuestionsFromSuperadminsPrivateCategoriesEnabled(null);
		resetViewOmQuestionEnabled(question);
		resetAdminFromQuestionAllowed(question);
		resetSuperadminFromQuestionAllowed(question);
		if (!isViewOMQuestionEnabled(operation,question))
		{
			question=null;
		}
		
		if (question==null)
		{
    		//addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
    		
    		resetViewOMQuestionsEnabled();
    		resetAdmins();
			resetSuperadmins();
			
    		RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url","error");
		}
		else
		{
			// Get package's name and destination path
			String packageName=question.getPackage();
			String path=configurationService.getOmQuestionsPath();
			
			// Get OM Develover, OM Test Navigator and OM Question Engine URLs
			String omURL=configurationService.getOmUrl();
			String omTnURL=configurationService.getOmTnUrl();
			String omQeURL=configurationService.getOmQeUrl();
			
			// Check if we need to build and/or deploy question jar
			boolean buildQuestion=true;
			boolean deployQuestion=true;
			long lastTimeModified=question.getTimemodified()==null?-1:question.getTimemodified().getTime();
			long lastTimeDeploy=question.getTimedeploy()==null?-1:question.getTimedeploy().getTime();
			long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
			if (lastTimeDeploy!=-1 && lastTimeDeploy>lastTimeBuild && lastTimeDeploy>lastTimeModified)
			{
				try
				{
					TestGenerator.checkQuestionJar(packageName,"1.0",omTnURL);
					deployQuestion=lastTimeDeploy!=TestGenerator.getQuestionJarLastModifiedDate(
						packageName,"1.0",omTnURL).getTime();
				}
				catch (Exception e)
				{
				}
			}
			if (deployQuestion)
			{
				if (lastTimeBuild!=-1 && lastTimeBuild>lastTimeModified)
				{
					try
					{
						QuestionGenerator.checkQuestionJar(packageName,omURL);
						buildQuestion=lastTimeBuild!=
							QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL).getTime();
					}
					catch (Exception e)
					{
					}
				}
			}
			else
			{
				buildQuestion=false;
			}
			
			// Build question if needed
			if (buildQuestion)
			{
				// First we need to copy resources needed by question
				OmHelper.copyResources(question,configurationService.getResourcesPath(),path);
				
				// Generate question files 
				QuestionGenerator.generateQuestion(question,path);
				
				// Create question on OM Developer Web Application
				QuestionGenerator.createQuestion(packageName,path,new ArrayList<String>(),omURL);
				
				// Build question on OM Developer Web Application
				QuestionGenerator.buildQuestion(packageName,omURL);
				
				// Update build time on question
				question.setTimebuild(QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL));
			}
			
			// Deploy question on OM Test Navigator Web Application
			QuestionGenerator.deployQuestion(question,omURL,omTnURL,deployQuestion,true);
			if (deployQuestion)
			{
				// Update deploy time on question
				question.setTimedeploy(TestGenerator.getQuestionJarLastModifiedDate(packageName,"1.0",omTnURL));
				
				// Stop all Test Navigator sessions using that question
				QuestionGenerator.stopAllSessionsForQuestion(packageName,omTnURL);
				
				// Delete cached question from OM Question Engine Web Application
				QuestionGenerator.deleteJarFileFromQuestionEngineCache(packageName,omQeURL);
			}
			
			// Save question if we need to update build and/or deploy time
			if (buildQuestion || deployQuestion)
			{
				try
				{
					questionsService.updateQuestion(question);
				}
				finally
				{
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
				}
			}
			
			// Add callback parameters to display question
			StringBuffer url=new StringBuffer(omTnURL);
			if (omTnURL.charAt(omTnURL.length()-1)!='/')
			{
				url.append('/');
			}
			RequestContext requestContext=RequestContext.getCurrentInstance();
			requestContext.addCallbackParam("url",url.toString());
			requestContext.addCallbackParam("packageName",packageName);
		}
	    return null;
	}
	
	public boolean isPublishAllowed()
	{
		return publishAllowed;
	}
	
	/**
	 * Publish test to production navigator environment.
	 * @return Next wiew (publication page if save is sucessful, otherwise we keep actual view)
	 */
	public String publishTest()
	{
		String nextView="publication?faces-redirect=true";
		TestRelease testRelease=null;
		Date startDateAux=null;
		Date closeDateAux=null;
		Date warningDateAux=null;
		Date feedbackDateAux=null;
		User currentUser=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (isPublishEnabled(operation))
		{
			// Get test release
			testRelease=getTestRelease(operation);
			User testAuthor=testRelease.getTest().getCreatedBy();
			currentUser=userSessionService.getCurrentUser(operation);			
			
			if (!currentUser.equals(testAuthor) && !isPublishOtherUsersEnabled(operation) && 
				(isAdmin(operation,testAuthor) || !isPublishAdminsEnabled(operation)) &&
				(isSuperadmin(operation,testAuthor) || !isPublishSuperadminsEnabled(operation)))
			{
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
				nextView=null;
			}
			else
			{
				startDateAux=testRelease.getStartDate();
				closeDateAux=testRelease.getCloseDate();
				warningDateAux=testRelease.getWarningDate();
				feedbackDateAux=testRelease.getFeedbackDate();
				if (!isRestrictDates(operation))
				{
					testRelease.setStartDate(null);
					testRelease.setCloseDate(null);
					testRelease.setWarningDate(null);
				}
				if (!isRestrictFeedbackDate(operation))
				{
					testRelease.setFeedbackDate(null);
				}
			}
		}
		
		if (!checkTest(operation,testRelease.getTest()))
		{
			publishAllowed=false;
			nextView=null;
		}
		
		if (nextView!=null)
		{
			// Get destination path
			String path=configurationService.getOmQuestionsPath();
			
			// Get OM Develover, OM Test Navigator and OM Question Engine URLs
			String omURL=configurationService.getOmUrl();
			String omTnProURL=configurationService.getOmTnProUrl();
			
			// Check that this test has not been published yet for this version
			try
			{
				TestGenerator.checkTestNavigatorXmls(testRelease,omTnProURL);
			}
			catch (Exception e)
			{
				addErrorMessage("PUBLISH_TEST_ALREADY_PUBLISHED_ERROR");
				publishAllowed=false;
				nextView=null;
			}
			if (nextView!=null)
			{
				// List of questions included in test
				List<Question> questions=new ArrayList<Question>();
				
				// Get section's questions
				for (Section section:testRelease.getTest().getSections())
				{
					
					for (QuestionOrder questionOrder:section.getQuestionOrders())
					{
						Question question=questionOrder.getQuestion();
						if (!questions.contains(question))
						{
							questions.add(questionsService.getQuestion(operation,question.getId()));
						}
					}
				}
				
				boolean copyJarFiles=false;
				for (Question question:questions)
				{
					// Get package's name
					String packageName=question.getPackage();
					
					// Check if we need to build question jar
					boolean buildQuestion=true;
					long lastTimeModified=
						question.getTimemodified()==null?-1:question.getTimemodified().getTime();
					long lastTimePublished=
						question.getTimepublished()==null?-1:question.getTimepublished().getTime();
					long lastTimeBuild=question.getTimebuild()==null?-1:question.getTimebuild().getTime();
					if (lastTimePublished!=-1 && lastTimePublished>lastTimeBuild && lastTimePublished>lastTimeModified)
					{
						try
						{
							TestGenerator.checkQuestionJar(packageName,null,omTnProURL);
							buildQuestion=lastTimePublished!=TestGenerator.getQuestionJarLastModifiedDate(
								packageName,null,omTnProURL).getTime();
						}
						catch (Exception e)
						{
							// Question's jar don't exists on OM Test Navigator Web Application 
							// so we ignore this exception
						}
					}
					if (buildQuestion && lastTimeBuild!=-1 && lastTimeBuild>lastTimeModified)
					{
						try
						{
							QuestionGenerator.checkQuestionJar(packageName,omURL);
							buildQuestion=lastTimeBuild!=QuestionGenerator.getQuestionJarLastModifiedDate(
								packageName,omURL).getTime();
						}
						catch (Exception e)
						{
							// Question's jar don't exists on OM Developer Web Application
							// so we ignore this exception
						}
					}
					
					// Build question if needed
					if (buildQuestion)
					{
						try
						{
							// If we need to build any question we will need to copy jar files even 
							// if we won't need to deploy the test
							copyJarFiles=true;
							
							// First we need to copy resources needed by question
							OmHelper.copyResources(question,configurationService.getResourcesPath(),path);
							
							// Generate question files 
							QuestionGenerator.generateQuestion(question,path);
							
							// Create question on OM Developer Web Application
							QuestionGenerator.createQuestion(packageName,path,new ArrayList<String>(),omURL);
							
							// Build question on OM Developer Web Application
							QuestionGenerator.buildQuestion(packageName,omURL);
							
							// Update build time on question
							question.setTimebuild(QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL));
							
							// Save question
							try
							{
								questionsService.updateQuestion(question);
							}
							finally
							{
								// End current user session Hibernate operation
								userSessionService.endCurrentUserOperation();
							}
						}
						catch (Exception e)
						{
							addErrorMessage("PUBLISH_TEST_UNKNOWN_ERROR");
							nextView=null;
						}
					}
					if (nextView==null)
					{
						break;
					}
				}
				if (nextView!=null)
				{
					// We set publisher and release date
					Date dateNow=new Date();
					testRelease.setPublisher(currentUser);
					testRelease.setReleaseDate(dateNow);
					
					// Publish test on OM Test Navigator Web Application production environment
					try
					{
						if (copyJarFiles)
						{
							// Copy jar files
							TestGenerator.copyJarFiles(testRelease.getTest(),omURL,null,omTnProURL,true);
						}
						
						// Publish test
						TestGenerator.publishTest(testRelease,omURL,omTnProURL);
					}
					catch (Exception e)
					{
						addErrorMessage("PUBLISH_TEST_UNKNOWN_ERROR");
						nextView=null;
					}
				}
			}
		}
		if (nextView==null && testRelease!=null)
		{
			testRelease.setStartDate(startDateAux);
			testRelease.setCloseDate(closeDateAux);
			testRelease.setWarningDate(warningDateAux);
			testRelease.setFeedbackDate(feedbackDateAux);
		}
		return nextView;
	}
	
	/**
	 * @param operation Operation
	 * @param test Test
	 * @return true if test hast not been modified nor deleted while publishing it
	 */
	private boolean checkTest(Operation operation,Test test)
	{
		boolean ok=true;
		Test testFromDB=testsService.getTest(getCurrentUserOperation(operation),test.getId());
		if (testFromDB==null)
		{
			ok=false;
			addErrorMessage("PUBLISH_TEST_DELETED_ERROR");
		}
		else if (!testFromDB.getTimeModified().equals(test.getTimeModified()))
		{
			ok=false;
			addErrorMessage("PUBLISH_TEST_CHANGED_ERROR");
		}
		return ok;
	}
	
	/**
	 * Ajax listener to perform checks when modifying a property.
	 * @param event Ajax event
	 */
	public void changeProperty(AjaxBehaviorEvent event)
	{
		String property=(String)event.getComponent().getAttributes().get("property");
		if ("allUsersAllowed".equals(property))
		{
			if (!isEnabledChecboxesSetters())
			{
				setEnabledCheckboxesSetters(true);
				processAllUsersAllowed(event.getComponent());
			}
		}
		else if ("restrictDates".equals(property))
		{
			if (!isEnabledChecboxesSetters())
			{
				setEnabledCheckboxesSetters(true);
				processRestrictDates(event.getComponent());
			}
		}
		else if ("restrictFeedbackDate".equals(property))
		{
			if (!isEnabledChecboxesSetters())
			{
				setEnabledCheckboxesSetters(true);
				processRestrictFeedbackDate(event.getComponent());
			}
		}
	}
	
	/**
	 * Process a checkbox (all users allowed) of the users tab of a test release.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processAllUsersAllowed(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput allUsersAllowed=(UIInput)component.findComponent(":testReleaseForm:allUsersAllowed");
		allUsersAllowed.processDecodes(context);
		if (allUsersAllowed.getSubmittedValue()!=null)
		{
			setAllUsersAllowed(Boolean.valueOf((String)allUsersAllowed.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (restrict dates) of the calendar tab of a test release.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processRestrictDates(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput restrictDates=(UIInput)component.findComponent(":testReleaseForm:restrictDates");
		restrictDates.processDecodes(context);
		if (restrictDates.getSubmittedValue()!=null)
		{
			setRestrictDates(Boolean.valueOf((String)restrictDates.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (restrict feedback date) of the calendar tab of a test release.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processRestrictFeedbackDate(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput restrictFeedbackDate=(UIInput)component.findComponent(":testReleaseForm:restrictFeedbackDate");
		restrictFeedbackDate.processDecodes(context);
		if (restrictFeedbackDate.getSubmittedValue()!=null)
		{
			setRestrictFeedbackDate(Boolean.valueOf((String)restrictFeedbackDate.getSubmittedValue()));
		}
	}
	
	/**
	 * @return Title of dialog for adding/updating a support contact
	 */
	public String getAddEditSupportContactTitle()
	{
		String title="";
		SupportContactBean currentSupportContact=getCurrentSupportContact();
		if (currentSupportContact==null)
		{
			title=localizationService.getLocalizedMessage("ADD_TECH_SUPPORT_ADDRESS");
		}
		else
		{
			title=localizationService.getLocalizedMessage("EDIT_TECH_SUPPORT_ADDRESS");
		}
		return title;
	}
	
	/**
	 * @return Title of dialog for adding/updating an evaluator
	 */
	public String getAddEditEvaluatorTitle()
	{
		String title="";
		EvaluatorBean currentEvaluator=getCurrentEvaluator();
		if (currentEvaluator==null)
		{
			title=localizationService.getLocalizedMessage("ADD_ASSESSEMENT_ADDRESS");
		}
		else
		{
			title=localizationService.getLocalizedMessage("EDIT_ASSESSEMENT_ADDRESS");
		}
		return title;
	}
	
    /**
     * @param category Category
     * @return Localized category name
     */
    public String getLocalizedCategoryName(Category category)
    {
    	return getLocalizedCategoryName(null,category);
    }
    
    /**
     * @param operation Operation
     * @param category Category
     * @return Localized category name
     */
    private String getLocalizedCategoryName(Operation operation,Category category)
    {
    	return categoriesService.getLocalizedCategoryName(getCurrentUserOperation(operation),category.getId());
    }
    
    /**
     * @param category Category
     * @return Localized category long name
     */
    public String getLocalizedCategoryLongName(Category category)
    {
    	return getLocalizedCategoryLongName(null,category);
    }
    
    /**
     * @param operation Operation
     * @param category Category
     * @return Localized category long name
     */
    private String getLocalizedCategoryLongName(Operation operation,Category category)
    {
    	return categoriesService.getLocalizedCategoryLongName(getCurrentUserOperation(operation),category.getId());
    }
	
	/**
	 * Action listener to show the dialog to confirm cancel of test publication.
	 * @param event Action event
	 */
	public void showConfirmCancelPublishTestDialog(ActionEvent event)
	{
		String target=(String)event.getComponent().getAttributes().get("target");
		if (isPublishAllowed())
		{
			setCancelPublishTestTarget(target);
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmCancelPublishTestDialog.show()");
		}
		else
		{
	        FacesContext context=FacesContext.getCurrentInstance();
	        if ("logout".equals(target))
	        {
				LoginBean loginBean=null;
				try
				{
					loginBean=(LoginBean)context.getApplication().getELResolver().getValue(
						context.getELContext(),null,"loginBean");
				}
				catch (Exception e)
				{
					loginBean=null;
					target=null;
				}
				if (loginBean!=null)
				{
					target=loginBean.logout();
				}
	        }
	        if (target!=null)
	        {
		        ExternalContext externalContext=context.getExternalContext();
		        StringBuffer targetXhtml=new StringBuffer("/pages/");
		        targetXhtml.append(target);
		        targetXhtml.append(".xhtml");
		        String url=externalContext.encodeActionURL(
		        	context.getApplication().getViewHandler().getActionURL(context,targetXhtml.toString()));
		        try 
		        {
		        	externalContext.redirect(url);
		        }
		        catch (IOException ioe)
		        {
		            throw new FacesException(ioe);
		        }
	        }
		}
	}
	
	/**
	 * Cancel test publication and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelPublishTest()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelPublishTestTarget()))
		{
			FacesContext facesContext=FacesContext.getCurrentInstance();
			LoginBean loginBean=null;
			try
			{
				loginBean=(LoginBean)facesContext.getApplication().getELResolver().getValue(
					facesContext.getELContext(),null,"loginBean");
			}
			catch (Exception e)
			{
				loginBean=null;
			}
			if (loginBean!=null)
			{
				nextView=new StringBuffer(loginBean.logout());
			}
		}
		else if (getCancelPublishTestTarget()!=null)
		{
			nextView=new StringBuffer(getCancelPublishTestTarget());
			nextView.append("?faces-redirect=true");
		}
		return nextView==null?null:nextView.toString();
	}
	
	
	/**
	 * Displays an error message.
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String message)
	{
		addErrorMessage(message,null);
	}
	
	/**
	 * Displays an error message.
	 * @param title Error title (before localization)
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String title,String message)
	{
		lastErrorMessage=message==null?title:message;
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,
			localizationService.getLocalizedMessage(title),
			message==null?null:localizationService.getLocalizedMessage(message)));
	}
	
	/**
	 * Scroll page to top position.
	 */
	private void scrollToTop()
	{
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("window.scrollTo(0,0)");
	}
	
	/**
	 * Handles a change of the current locale.<br/><br/>
	 * This implementation disable setters because of a bug of &lt;p:wizard&gt; component that always set 
	 * properties for their checkboxes components as <i>false</i> when submitting the form.
	 * @param event Action event
	 */
	public void changeLocale(ActionEvent event)
	{
		setEnabledCheckboxesSetters(false);
	}
}
