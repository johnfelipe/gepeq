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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionRelease;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.UserGroupBean;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionReleasesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

/**
 * Managed bean for publishing a question.
 */
@SuppressWarnings("serial")
@ManagedBean(name="questionReleaseBean")
@ViewScoped
public class QuestionReleaseBean implements Serializable
{
	private final static String DATE_HIDDEN_PATTERN="MM-dd-yyyy HH:mm:ss";
	
	private final static String GENERAL_WIZARD_TAB="general";
	private final static String USERS_WIZARD_TAB="users";
	private final static String CALENDAR_WIZARD_TAB="calendar";
	private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionReleasesService}")
	private QuestionReleasesService questionReleasesService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private QuestionRelease questionRelease;				// Current question release
	
	/** UI Helper Properties */
	
	private String cancelPublishQuestionTarget;
	
	private String activeQuestionReleaseTabName;
	
	private boolean restrictDates;
	private String startDateHidden;
	private String closeDateHidden;
	private String warningDateHidden;
	private String deleteDateHidden;
	private List<User> filteredUsersForAddingUsers;
	private List<UserType> userTypes;
	private long filterUsersUserTypeId;
	private boolean filterUsersIncludeOmUsers;
	private List<UserGroupBean> questionUsersGroups;
	private DualListModel<User> usersDualList;
	private String userGroup;
	private boolean userGroupsDialogDisplayed;
	private String availableUserGroupsHidden;
	private String userGroupsToAddHidden;
	private DualListModel<String> userGroupsDualList;
	
	private boolean enabledCheckboxesSetters;
	
	public QuestionReleaseBean()
	{
		questionRelease=null;
		restrictDates=false;
		filteredUsersForAddingUsers=null;
		userTypes=null;
		filterUsersUserTypeId=0L;
		filterUsersIncludeOmUsers=true;
		questionUsersGroups=null;
		usersDualList=null;
		userGroup=null;
		userGroupsDialogDisplayed=false;
		availableUserGroupsHidden="";
		userGroupsToAddHidden="";
		userGroupsDualList=null;
		enabledCheckboxesSetters=true;
		activeQuestionReleaseTabName=GENERAL_WIZARD_TAB;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
	public void setCategoriesService(CategoriesService categoriesService)
	{
		this.categoriesService=categoriesService;
	}
	
	public void setCategoryTypesService(CategoryTypesService categoryTypesService)
	{
		this.categoryTypesService=categoryTypesService;
	}
	
	public void setVisibilitiesService(VisibilitiesService visibilitiesService)
	{
		this.visibilitiesService=visibilitiesService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setQuestionReleasesService(QuestionReleasesService questionReleasesService)
	{
		this.questionReleasesService=questionReleasesService;
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
    	if (operation!=null && questionRelease==null)
    	{
    		getQuestionRelease();
    		operation=null;
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
    
	private boolean isFilterGlobalQuestionsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_GLOBAL_FILTER_ENABLED");
	}
    
	private boolean isFilterOtherUsersQuestionsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_QUESTIONS_OTHER_USERS_FILTER_ENABLED");
	}
	
	private boolean isPublishEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishOtherUsersQuestionsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishAdminsQuestionsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_ADMINS_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishSuperadminsQuestionsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_SUPERADMINS_USERS_QUESTIONS_ENABLED");
	}
	
	private boolean isAdmin(Operation operation,User user)
	{
		return permissionsService.isGranted(
			getCurrentUserOperation(operation),user,"PERMISSION_NAVIGATION_ADMINISTRATION");
	}
	
	private boolean isSuperadmin(Operation operation,User user)
	{
		return permissionsService.isGranted(
			getCurrentUserOperation(operation),user,"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
	}
	
	private boolean isViewQuestionsFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		return userSessionService.isGranted(getCurrentUserOperation(operation),
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED");
	}
	
	private boolean isViewQuestionsFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		return userSessionService.isGranted(getCurrentUserOperation(operation),
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED");
	}
	
	private boolean isViewQuestionsFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		return userSessionService.isGranted(getCurrentUserOperation(operation),
			"PERMISSION_QUESTIONS_VIEW_QUESTIONS_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED");
	}
	
	public QuestionRelease getQuestionRelease()
	{
		if (questionRelease==null)
		{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
    		
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
			
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    		long questionId=Long.parseLong(params.get("questionId"));
    		
    		questionRelease=questionReleasesService.getQuestionRelease(operation,questionId);
    		User currentUser=userSessionService.getCurrentUser(operation);
    		if (questionRelease==null)
    		{
    			Question question=questionsService.getQuestion(operation,questionId);
    			questionRelease=new QuestionRelease(question,currentUser);
    		}
    		else
    		{
    			questionRelease.setPublisher(currentUser);
    		}
    		
    		if (questionRelease!=null)
    		{
    			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
    			Date startDate=questionRelease.getStartDate();
    			Date closeDate=questionRelease.getCloseDate();
    			Date warningDate=questionRelease.getWarningDate();
    			setRestrictDates(startDate!=null);
    	    	setStartDateHidden(startDate==null?"":df.format(startDate));
    	    	setCloseDateHidden(closeDate==null?"":df.format(closeDate));
    	    	setWarningDateHidden(warningDate==null?"":df.format(warningDate));
    	    	
    	    	List<UserGroupBean> questionUserGroups=new ArrayList<UserGroupBean>();
    	    	setQuestionUsersGroups(questionUserGroups);
    	    	for (User questionUser:questionRelease.getUsers())
    	    	{
    	    		questionUserGroups.add(new UserGroupBean(questionUser));
    	    	}
    	    	for (String questionUserGroup:questionRelease.getUserGroups())
    	    	{
    	    		questionUserGroups.add(new UserGroupBean(usersService,userSessionService,questionUserGroup));
    	    	}
    		}
		}
		return questionRelease;
	}
	
	public void setQuestionRelease(QuestionRelease questionRelease)
	{
		this.questionRelease=questionRelease;
	}
	
	public boolean isAllUsersAllowed()
	{
		return getQuestionRelease().isAllUsersAllowed();
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		if (isEnabledChecboxesSetters())
		{
			getQuestionRelease().setAllUsersAllowed(allUsersAllowed);
		}
	}
	
	public boolean isRestrictDates()
	{
		getQuestionRelease();
		return restrictDates;
	}
	
	public void setRestrictDates(boolean restrictDates)
	{
		if (isEnabledChecboxesSetters())
		{
			this.restrictDates=restrictDates;
		}
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
	
	public String getDeleteDateHidden()
	{
		return deleteDateHidden;
	}
	
	public void setDeleteDateHidden(String deleteDateHidden)
	{
		this.deleteDateHidden=deleteDateHidden;
	}
	
	public String getCancelPublishQuestionTarget()
	{
		return cancelPublishQuestionTarget;
	}
	
	public void setCancelPublishQuestionTarget(String cancelPublishQuestionTarget)
	{
		this.cancelPublishQuestionTarget=cancelPublishQuestionTarget;
	}
	
	public Date getStartDate()
	{
		QuestionRelease questionRelease=getQuestionRelease();
		if (getStartDateHidden()!=null && !getStartDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				questionRelease.setStartDate(df.parse(getStartDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setStartDateHidden("");
		}
		return questionRelease.getStartDate();
	}
	
	public void setStartDate(Date startDate)
	{
		getQuestionRelease().setStartDate(startDate);
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
		setStartDate(event.getDate());
	}
	
	public Date getCloseDate()
	{
		QuestionRelease questionRelease=getQuestionRelease();
		if (getCloseDateHidden()!=null && !getCloseDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				questionRelease.setCloseDate(df.parse(getCloseDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setCloseDateHidden("");
		}
		return questionRelease.getCloseDate();
	}
	
	public void setCloseDate(Date closeDate)
	{
		getQuestionRelease().setCloseDate(closeDate);
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
		setCloseDate(event.getDate());
	}
	
	public Date getWarningDate()
	{
		QuestionRelease questionRelease=getQuestionRelease();
		if (getWarningDateHidden()!=null && !getWarningDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				questionRelease.setWarningDate(df.parse(getWarningDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setWarningDateHidden("");
		}
		return questionRelease.getWarningDate();
	}
	
	public void setWarningDate(Date warningDate)
	{
		getQuestionRelease().setWarningDate(warningDate);
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
		setWarningDate(event.getDate());
	}
	
	public Date getDeleteDate()
	{
		QuestionRelease questionRelease=getQuestionRelease();
		if (getDeleteDateHidden()!=null && !getDeleteDateHidden().equals(""))
		{
			DateFormat df=new SimpleDateFormat(DATE_HIDDEN_PATTERN);
			try
			{
				questionRelease.setDeleteDate(df.parse(getDeleteDateHidden()));
			}
			catch (ParseException pe)
			{
			}
			setDeleteDateHidden("");
		}
		return questionRelease.getDeleteDate();
	}
	
	public void setDeleteDate(Date deleteDate)
	{
		getQuestionRelease().setDeleteDate(deleteDate);
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
		setDeleteDate(event.getDate());
	}
	
	public String getDisplayEquations()
	{
		return getQuestionRelease().getQuestion().isDisplayEquations()?
			localizationService.getLocalizedMessage("YES"):localizationService.getLocalizedMessage("NO");
	}
	
	public void setDisplayEquations(String displayEquations)
	{
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
	
	/**
	 * @return Users and groups with permission to do this question
	 */
	public List<UserGroupBean> getQuestionUsersGroups()
	{
    	if (questionUsersGroups==null)
    	{
    		getQuestionRelease();
    	}
    	return questionUsersGroups;
	}
	
	/**
	 * @param questionUsersGroups Users and groups with permission to do this question
	 */
	public void setQuestionUsersGroups(List<UserGroupBean> questionUsersGroups)
	{
		this.questionUsersGroups=questionUsersGroups;
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
    	if (!ok)
    	{
    		nextStep=oldStep;
    		
			// Scroll page to top position
			scrollToTop();
    	}
    	setEnabledCheckboxesSetters(!CONFIRMATION_WIZARD_TAB.equals(nextStep));
    	activeQuestionReleaseTabName=nextStep;
    	return nextStep;
    }
	
    public String getActiveQuestionReleaseTabName()
    {
    	return activeQuestionReleaseTabName;
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
    
    private boolean checkCalendarInputFields()
    {
    	boolean ok=true;
    	
    	if (isRestrictDates())
    	{
    		String startDateHidden=getStartDateHidden();
    		String closeDateHidden=getCloseDateHidden();
    		String warningDateHidden=getWarningDateHidden();
    		Date startDate=getStartDate();
    		Date closeDate=getCloseDate();
    		Date warningDate=getWarningDate();
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
   			setStartDateHidden(startDateHidden);
   			setCloseDateHidden(closeDateHidden);
   			setWarningDateHidden(warningDateHidden);
   		}
		return ok;
    }
    
	public String getUserGroup()
	{
		return userGroup;
	}
	
	public void setUserGroup(String userGroup)
	{
		this.userGroup=userGroup;
	}
	
	public boolean isUserGroupsDialogDisplayed()
	{
		return userGroupsDialogDisplayed;
	}
	
	public void setUserGroupsDialogDisplayed(boolean userGroupsDialogDisplayed)
	{
		this.userGroupsDialogDisplayed=userGroupsDialogDisplayed;
	}
    
	/**
	 * @return Users without permission to do this question release (except if it is allowed that all users do the 
	 * question release)
	 */
	public List<User> getAvailableUsers()
	{
		return getAvailableUsers(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Users without permission to do this question release (except if it is allowed that all users do the 
	 * question release)
	 */
	private List<User> getAvailableUsers(Operation operation)
	{
		List<User> availableUsers=new ArrayList<User>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		List<User> questionReleaseUsers=getQuestionRelease().getUsers();
		for (User user:getFilteredUsersForAddingUsers(operation))
		{
			if (!questionReleaseUsers.contains(user))
			{
				User availableUser=user.getUserCopy();
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
	 * @return Available groups within "Add groups" dialog as a string with the groups separated by commas
	 */
	public String getAvailableUserGroupsHidden()
	{
		return availableUserGroupsHidden;
	}
	
	/**
	 * @param availableUserGroupsHidden Available groups within "Add groups" dialog as a string with the groups 
	 * separated by commas
	 */
	public void setAvailableUserGroupsHidden(String availableUserGroupsHidden)
	{
		this.availableUserGroupsHidden=availableUserGroupsHidden;
	}
    
	/**
	 * @return Groups selected to add within "Add groups" dialog as a string with the groups separated by commas
	 */
	public String getUserGroupsToAddHidden()
	{
		return userGroupsToAddHidden;
	}
	
	/**
	 * @param userGroupsToAddHidden Groups selected to add within "Add groups" dialog as a string with the groups 
	 * separated by commas
	 */
	public void setUserGroupsToAddHidden(String userGroupsToAddHidden)
	{
		this.userGroupsToAddHidden=userGroupsToAddHidden;
	}
    
	/**
	 * @return User groups as dual list
	 */
	public DualListModel<String> getUserGroupsDualList()
	{
		return getUserGroupsDualList(null);
	}
	
	/**
	 * Set dual list for user groups.
	 * @param userGroupsDualList User groups as dual list
	 */
	public void setUserGroupsDualList(DualListModel<String> userGroupsDualList)
	{
		this.userGroupsDualList=userGroupsDualList;
	}
    
	/**
	 * @param operation Operation
	 * @return User groups as dual list
	 */
	private DualListModel<String> getUserGroupsDualList(Operation operation)
	{
		if (userGroupsDualList==null)
		{
			List<String> availableUserGroups=usersService.getGroups(getCurrentUserOperation(operation));
			for (UserGroupBean userGroup:getQuestionUsersGroups())
			{
				if (!userGroup.isTestUser())
				{
					availableUserGroups.remove(userGroup.getGroup());
				}
			}
			userGroupsDualList=new DualListModel<String>(availableUserGroups,new ArrayList<String>());
		}
		return userGroupsDualList;
	}
	
	/**
	 * Display a dialog to add groups to the user 
     * @param event Action event
	 */
	public void showAddUserGroups(ActionEvent event)
	{
		setUserGroupsDualList(null);
		setUserGroup("");
		setUserGroupsDialogDisplayed(true);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUserGroupsDialog.show()");
	}
	
    /**
     * Adds a user group.
     * @param event Action event
     */
	public void addUserGroup(ActionEvent event)
	{
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add groups" dialog
		refreshUserGroupsDualList(operation,event.getComponent());
		
		// Check group before adding it to dual list
		if (isEnabledAddUserGroup(operation,true))
		{
			getUserGroupsDualList(operation).getTarget().add(getUserGroup());
		}
	}
	
    /**
     * Add user groups selected within dialog
     * @param event Action event
     */
    public void acceptAddUserGroups(ActionEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
		
		// Refresh dual list of user groups for "Add groups" dialog
		refreshUserGroupsDualList(operation,event.getComponent());
    	
		// Add selected groups
		List<UserGroupBean> questionUsersGroups=getQuestionUsersGroups();
   		for (String userGroup:getUserGroupsDualList(operation).getTarget())
  		{
  			questionUsersGroups.add(new UserGroupBean(usersService,userSessionService,userGroup));
   		}
   		//Collections.sort(getUserGroups());
   		
   		setUserGroupsDialogDisplayed(false);
   		
		// Close dialog
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUserGroupsDialog.hide()");
    }
    
    /**
     * @param event Action event
     */
    public void cancelAddUserGroups(ActionEvent event)
    {
    	setUserGroupsDialogDisplayed(false);
    }
	
	/**
	 * Display a dialog to add users allowed to do test. 
     * @param event Action event
	 */
	public void showAddUsers(ActionEvent event)
	{
   		usersDualList=null;
   		
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("addUsersDialog.show()");
	}
	
    /**
     * Add users selected within dialog to list of users allowed to do test.
     * @param event Action event
     */
    public void acceptAddUsers(ActionEvent event)
    {
		QuestionRelease questionRelease=getQuestionRelease();
		List<UserGroupBean> questionUserGroups=getQuestionUsersGroups();
   		for (User user:getUsersDualList(getCurrentUserOperation(null)).getTarget())
  		{
   			questionRelease.getUsers().add(user);
   			questionUserGroups.add(new UserGroupBean(user));
   		}
    }
	
    /**
     * ActionListener that deletes an user or group from list of users allowed to do test.
     * @param event Action event
     */
    public void removeUserGroup(ActionEvent event)
    {
    	QuestionRelease questionRelease=getQuestionRelease();
    	UserGroupBean userGroup=(UserGroupBean)event.getComponent().getAttributes().get("userGroup");
    	if (userGroup!=null)
    	{
    		if (userGroup.isTestUser())
    		{
    			questionRelease.getUsers().remove(userGroup.getUser());
    		}
    		else
    		{
    			questionRelease.getUserGroups().remove(userGroup.getGroup());
    		}
    		getQuestionUsersGroups().remove(userGroup);
    	}
	}
	
	private void refreshUserGroupsDualList(Operation operation,UIComponent component)
	{
		// Process hidden fields with user groups
		if (processUserGroupsHiddens(component))
		{
			// Get current available user groups
			List<String> availableUserGroups=new ArrayList<String>();
			if (getAvailableUserGroupsHidden()!=null && !"".equals(getAvailableUserGroupsHidden()))
			{
				for (String availableUserGroup:getAvailableUserGroupsHidden().split(Pattern.quote(",")))
				{
					availableUserGroups.add(availableUserGroup);
				}
			}
			
			// Get current user groups to add
			List<String> userGroupsToAdd=new ArrayList<String>();
			if (getUserGroupsToAddHidden()!=null && !"".equals(getUserGroupsToAddHidden()))
			{
				for (String userGroupToAdd:getUserGroupsToAddHidden().split(Pattern.quote(",")))
				{
					userGroupsToAdd.add(userGroupToAdd);
				}
			}
			
			// Refresh user groups dual list
			DualListModel<String> userGroupsDualList=getUserGroupsDualList(getCurrentUserOperation(operation));
			userGroupsDualList.setSource(availableUserGroups);
			userGroupsDualList.setTarget(userGroupsToAdd);
		}
	}
	
	private boolean processUserGroupsHiddens(UIComponent component)
	{
		boolean submittedValue=false;
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput availableUserGroupsHiddenInput=
			(UIInput)component.findComponent(":userGroupsDialogForm:availableUserGroupsHidden");
		availableUserGroupsHiddenInput.processDecodes(context);
		if (availableUserGroupsHiddenInput.getSubmittedValue()!=null)
		{
			setAvailableUserGroupsHidden((String)availableUserGroupsHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		UIInput userGroupsToAddHiddenInput=
			(UIInput)component.findComponent(":userGroupsDialogForm:userGroupsToAddHidden");
		userGroupsToAddHiddenInput.processDecodes(context);
		if (userGroupsToAddHiddenInput.getSubmittedValue()!=null)
		{
			setUserGroupsToAddHidden((String)userGroupsToAddHiddenInput.getSubmittedValue());
			submittedValue=true;
		}
		return submittedValue;
	}
	
	/**
	 * Checks that group entered by user only includes valid characters or displays an error message if desired.
     * @param group Group
	 * @param displayError true to display error message, false otherwise
	 * @return true if group only includes valid characters (letters, digits), false otherwise
	 */
    private boolean checkValidCharactersForUserGroup(String group,boolean displayError)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(group,true,true,false,null))
    	{
    		if (displayError)
    		{
    			addErrorMessage("INCORRECT_OPERATION","USER_GROUP_INVALID_CHARACTERS");
    		}
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the group entered by user is a letter or displays an error message if
     * desired.
     * @param group Group
	 * @param displayError true to display error message, false otherwise
	 * @return true if first character of group is a letter, false otherwise
     */
    private boolean checkFirstCharacterLetterForUserGroup(String group,boolean displayError)
    {
    	boolean ok=true;
    	if (!StringUtils.isFirstCharacterLetter(group))
    	{
    		if (displayError)
    		{
    			addErrorMessage("INCORRECT_OPERATION","USER_GROUP_FIRST_CHARACTER_INVALID");
    		}
    		ok=false;
    	}
    	return ok;
    }
	
    /**
     * Check that group entered by user is valid displays error messages indicating the causes if desired.
     * @param group Group
	 * @param displayErrors true to display error messages, false otherwise
     * @return true if group entered by user is valid, false otherwise
     */
    private boolean checkUserGroup(String group,boolean displayErrors)
    {
    	boolean ok=true;
    	if (group!=null && !"".equals(group))
    	{
        	if (displayErrors)
        	{
        		if (!checkValidCharactersForUserGroup(group,true))
        		{
        			ok=false;
        		}
        		if (!checkFirstCharacterLetterForUserGroup(group,true))
        		{
        			ok=false;
        		}
        	}
        	else
        	{
        		ok=checkValidCharactersForUserGroup(group,false) && 
        			checkFirstCharacterLetterForUserGroup(group,false);
        	}
    	}
    	return ok;
    }
	
	/**
	 * @return true if button to add a user group is enabled, false if it is disabled
	 */
	public boolean isEnabledAddUserGroup()
	{
		return isEnabledAddUserGroup(null);
	}
    
	/**
	 * @param operation Operation
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(Operation operation)
	{
		return isEnabledAddUserGroup(operation,false);
	}
	
	/**
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(boolean displayErrors)
	{
		return isEnabledAddUserGroup(null,displayErrors);
	}
	
	/**
	 * @param operation Operation
	 * @param displayErrors true to display error messages, false otherwise
	 * @return true if user group entered by user is valid, false otherwise
	 */
	public boolean isEnabledAddUserGroup(Operation operation,boolean displayErrors)
	{
		boolean ok=true;
		if (displayErrors)
		{
			if (getUserGroup()==null || getUserGroup().equals(""))
			{
				addErrorMessage("INCORRECT_OPERATION","USER_GROUP_REQUIRED");
				ok=false;
			}
			else if (checkUserGroup(getUserGroup(),true))
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean userGroup:getQuestionUsersGroups())
				{
					if (!userGroup.isTestUser() && getUserGroup().equals(userGroup.getGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> userGroupsDualList=getUserGroupsDualList(operation);
					if (userGroupsDualList.getSource().contains(getUserGroup()) || 
						userGroupsDualList.getTarget().contains(getUserGroup()))
					{
						addErrorMessage("INCORRECT_OPERATION","USER_GROUP_ALREADY_DECLARED");
						ok=false;
					}
				}
			}
			else
			{
				ok=false;
			}
		}
		else
		{
			ok=getUserGroup()!=null && !getUserGroup().equals("") && checkUserGroup(getUserGroup(),false);
			if (ok)
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				for (UserGroupBean userGroup:getQuestionUsersGroups())
				{
					if (!userGroup.isTestUser() && getUserGroup().equals(userGroup.getGroup()))
					{
						ok=false;
						break;
					}
				}
				if (ok)
				{
					DualListModel<String> userGroupsDualList=getUserGroupsDualList(operation);
					if (userGroupsDualList.getSource().contains(getUserGroup()) || 
						userGroupsDualList.getTarget().contains(getUserGroup()))
					{
						ok=false;
					}
				}
			}
		}
		return ok;
	}
    
    /**
     * Reset start date.
     * @param event Action event
     */
    public void resetStartDate(ActionEvent event)
    {
    	setStartDate(null);
    	setStartDateHidden("");
    	
    	// We need to update manually 'startDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput startDateHidden=
    		(UIInput)event.getComponent().findComponent(":questionReleaseForm:startDateHidden");
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
    	setCloseDate(null);
    	setCloseDateHidden("");
    	
    	// We need to update manually 'closeDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput closeDateHidden=
    		(UIInput)event.getComponent().findComponent(":questionReleaseForm:closeDateHidden");
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
    	setWarningDate(null);
    	setWarningDateHidden("");
    	
    	// We need to update manually 'warningDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput warningDateHidden=
    		(UIInput)event.getComponent().findComponent(":questionReleaseForm:warningDateHidden");
    	warningDateHidden.pushComponentToEL(context,null);
    	warningDateHidden.setSubmittedValue("");
    	warningDateHidden.popComponentFromEL(context);
    }
	
    /**
     * Reset delete date.
     * @param event Action event
     */
    public void resetDeleteDate(ActionEvent event)
    {
    	setDeleteDate(null);
    	setDeleteDateHidden("");
    	
    	// We need to update manually 'deleteDateHidden' hidden input field
    	FacesContext context=FacesContext.getCurrentInstance();
    	UIInput deleteDateHidden=
    		(UIInput)event.getComponent().findComponent(":questionReleaseForm:deleteDateHidden");
    	deleteDateHidden.pushComponentToEL(context,null);
    	deleteDateHidden.setSubmittedValue("");
    	deleteDateHidden.popComponentFromEL(context);
    }
    
	/**
	 * @param operation Operation
	 * @param category Category
	 * @return true if a category is usable by current user, false otherwise
	 */
    private boolean checkCategory(Operation operation,Category category)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	// Check category type
    	boolean ok=category!=null && categoryTypesService.isDerivedFrom(operation,
    		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_QUESTIONS"),
    		categoryTypesService.getCategoryTypeFromCategoryId(operation,category.getId()));
    	
    	// Check visibility
    	if (ok)
    	{
    		ok=false;
    		Question question=getQuestionRelease().getQuestion();
    		User questionAuthor=question.getCreatedBy();
    		Visibility categoryVisibility=visibilitiesService.getVisibilityFromCategoryId(operation,category.getId());
    		if (categoryVisibility.isGlobal())	
    		{
    			ok=isFilterGlobalQuestionsEnabled(operation);
    		}
    		else if (questionAuthor.equals(category.getUser()))
    		{
    			if (questionAuthor.getId()==userSessionService.getCurrentUserId())
    			{
    				ok=true;
    			}
    			else if (isFilterOtherUsersQuestionsEnabled(operation)) 
    			{
    				if (categoryVisibility.getLevel()>=visibilitiesService.getVisibility(
    					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
    				{
    					ok=isViewQuestionsFromOtherUsersPrivateCategoriesEnabled(operation) && 
    						(!isAdmin(operation,questionAuthor) || 
    						isViewQuestionsFromAdminsPrivateCategoriesEnabled(operation)) && 
    						(!isSuperadmin(operation,questionAuthor) || 
    						isViewQuestionsFromSuperadminsPrivateCategoriesEnabled(operation));
    				}
    				else
    				{
    					ok=true;
    				}
    			}
    		}
    	}
    	return ok;
    }
    
	/**
	 * @param operation Operation
	 * @return true if current category of the question we are publishing is usable by current user, false otherwise
	 */
    private boolean checkCurrentCategory(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	return checkCategory(operation,categoriesService.getCategoryFromQuestionId(
    		operation,getQuestionRelease().getQuestion().getId()));
    }
	
	private boolean checkPublishQuestion(Operation operation)
	{
		boolean ok=false;
		
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		Question question=getQuestionRelease().getQuestion();
		if (isPublishEnabled(operation))
		{
			User questionAuthor=question.getCreatedBy();
			if (questionAuthor.getId()==userSessionService.getCurrentUserId())
			{
				ok=true;
			}
			else
			{
				ok=isPublishOtherUsersQuestionsEnabled(operation) && 
					(!isAdmin(operation,questionAuthor) || isPublishAdminsQuestionsEnabled(operation)) && 
					(!isSuperadmin(operation,questionAuthor) || isPublishSuperadminsQuestionsEnabled(operation));
			}
		}
		return ok;
	}
	
	private boolean checkQuestionNotChanged(Operation operation)
	{
		Question question=getQuestionRelease().getQuestion();
		Date timeModified=question.getTimemodified();
		Date timeModifiedFromDB=
			questionsService.getTimeModifiedFromQuestionId(getCurrentUserOperation(operation),question.getId());
		return timeModified.equals(timeModifiedFromDB);
	}
	
	/**
	 * Publish question to production navigator environment.
	 * @return Next wiew (publication page if save is sucessful, otherwise we keep actual view)
	 */
	public String publishQuestion()
	{
		String nextView="publication?faces-redirect=true";
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		Date startDateAux=null;
		Date closeDateAux=null;
		Date warningDateAux=null;
		QuestionRelease questionRelease=getQuestionRelease();
		Question question=questionRelease.getQuestion();
		User currentUser=userSessionService.getCurrentUser(operation);
		long questionId=question.getId();
		
		if (!questionsService.checkQuestionId(operation,questionId))
		{
			nextView=null;
			displayErrorPage(
				"QUESTION_PUBLISH_NOT_FOUND_ERROR","The question you are trying to publish no longer exists.");
		}
		else if (!checkPublishQuestion(operation) || !checkCurrentCategory(operation))
		{
			nextView=null;
	    	displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
		}
		else if (!checkQuestionNotChanged(operation))
		{
			nextView=null;
			displayErrorPage(
				"QUESTION_PUBLISH_CHANGED_ERROR","The question you are trying to publish has been modified.");
		}
		else
		{
			startDateAux=questionRelease.getStartDate();
			closeDateAux=questionRelease.getCloseDate();
			warningDateAux=questionRelease.getWarningDate();
			if (!isRestrictDates())
			{
				questionRelease.setStartDate(null);
				questionRelease.setCloseDate(null);
				questionRelease.setWarningDate(null);
			}
			questionRelease.getUsers().clear();
			questionRelease.getUserGroups().clear();
			for (UserGroupBean userGroup:getQuestionUsersGroups())
			{
				if (userGroup.isTestUser())
				{
					questionRelease.getUsers().add(userGroup.getUser());
				}
				else
				{
					questionRelease.getUserGroups().add(userGroup.getGroup());
				}
			}
			
			// Get package's name and destination path
			String packageName=questionRelease.getQuestion().getPackage();
			String path=configurationService.getOmQuestionsPath();
			
			// Get OM Develover, OM Test Navigator Pro and OM Question Engine Pro URLs
			String omURL=configurationService.getOmUrl();
			String omTnProURL=configurationService.getOmTnProUrl();
			
			// Check if we need to build and/or deploy question jar
			boolean buildQuestion=true;
			boolean deployQuestionJar=true;
			long lastTimeModified=questionRelease.getQuestion().getTimemodified()==null?
				-1:questionRelease.getQuestion().getTimemodified().getTime();
			long lastTimePublished=questionRelease.getQuestion().getTimepublished()==null?
				-1:questionRelease.getQuestion().getTimepublished().getTime();
			long lastTimeBuild=questionRelease.getQuestion().getTimebuild()==null?
				-1:questionRelease.getQuestion().getTimebuild().getTime();
			if (lastTimePublished!=-1 && lastTimePublished>lastTimeBuild && lastTimePublished>lastTimeModified)
			{
				try
				{
					TestGenerator.checkQuestionJar(packageName,null,omTnProURL);
					deployQuestionJar=lastTimePublished!=
						TestGenerator.getQuestionJarLastModifiedDate(packageName,null,omTnProURL).getTime();
				}
				catch (Exception e)
				{
				}
			}
			if (deployQuestionJar)
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
				try
				{
					// First we need to copy resources needed by question
					OmHelper.copyResources(
						questionRelease.getQuestion(),configurationService.getResourcesPath(),path);
					
					// Generate question files 
					QuestionGenerator.generateQuestion(questionRelease.getQuestion(),path);
					
					// Create question on OM Developer Web Application
					QuestionGenerator.createQuestion(packageName,path,new ArrayList<String>(),omURL);
					
					// Build question on OM Developer Web Application
					QuestionGenerator.buildQuestion(packageName,omURL);
					
					// Update build time on question
					questionRelease.getQuestion().setTimebuild(
						QuestionGenerator.getQuestionJarLastModifiedDate(packageName,omURL));
				}
				catch (Exception e)
				{
					addErrorMessage("PUBLISH_QUESTION_UNKNOWN_ERROR");
					nextView=null;
				}
			}
			
			if (nextView!=null)
			{
				// We set publisher and release date
				Date dateNow=new Date();
				questionRelease.setPublisher(currentUser);
				questionRelease.setReleaseDate(dateNow);
				
				// Publish question on OM Test Navigator Web Application production environment
				try
				{
					QuestionGenerator.publishQuestion(questionRelease,omURL,omTnProURL,deployQuestionJar);
					
					if (deployQuestionJar)
					{
						// Update published time on question
						questionRelease.getQuestion().setTimepublished(
							TestGenerator.getQuestionJarLastModifiedDate(packageName,null,omTnProURL));
					}
					
					// Save question if we need to update build or published time
					if (buildQuestion || deployQuestionJar)
					{
						try
						{
							questionsService.updateQuestion(questionRelease.getQuestion());
						}
						finally
						{
							// End current user session Hibernate operation
							userSessionService.endCurrentUserOperation();
						}
					}
				}
				catch (Exception e)
				{
					addErrorMessage("PUBLISH_QUESTION_UNKNOWN_ERROR");
					nextView=null;
				}
			}
		}
		if (nextView==null)
		{
			questionRelease.setStartDate(startDateAux);
			questionRelease.setCloseDate(closeDateAux);
			questionRelease.setWarningDate(warningDateAux);
		}
		return nextView;
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
	}
	
	/**
	 * Process a checkbox (all users allowed) of the users tab of a question release.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processAllUsersAllowed(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput allUsersAllowed=(UIInput)component.findComponent(":questionReleaseForm:allUsersAllowed");
		allUsersAllowed.processDecodes(context);
		if (allUsersAllowed.getSubmittedValue()!=null)
		{
			setAllUsersAllowed(Boolean.valueOf((String)allUsersAllowed.getSubmittedValue()));
		}
	}
	
	/**
	 * Process a checkbox (restrict dates) of the calendar tab of a question release.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processRestrictDates(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput restrictDates=(UIInput)component.findComponent(":questionReleaseForm:restrictDates");
		restrictDates.processDecodes(context);
		if (restrictDates.getSubmittedValue()!=null)
		{
			setRestrictDates(Boolean.valueOf((String)restrictDates.getSubmittedValue()));
		}
	}
	
	/**
	 * Action listener to show the dialog to confirm cancel of question publication.
	 * @param event Action event
	 */
	public void showConfirmCancelPublishQuestionDialog(ActionEvent event)
	{
		setCancelPublishQuestionTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelPublishQuestionDialog.show()");
	}
	
	/**
	 * Cancel question publication and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelPublishQuestion()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelPublishQuestionTarget()))
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
		else if (getCancelPublishQuestionTarget()!=null)
		{
			nextView=new StringBuffer(getCancelPublishQuestionTarget());
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
	 * Displays the error page with the indicated message.<br/><br/>
	 * Be careful that this method can only be invoked safely from non ajax actions.
	 * @param errorCode Error message (before localization)
	 * @param plainMessage Plain error message (used if it is not possible to localize error message)
	 */
	private void displayErrorPage(String errorCode,String plainMessage)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		ExternalContext externalContext=context.getExternalContext();
		Map<String,Object> requestMap=externalContext.getRequestMap();
		requestMap.put("errorCode",errorCode);
		requestMap.put("plainMessage",plainMessage);
		try
		{
			externalContext.dispatch("/pages/error");
		}
		catch (IOException ioe)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage(errorCode);
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage=plainMessage;
			}
			throw new FacesException(errorMessage,ioe);
		}
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
