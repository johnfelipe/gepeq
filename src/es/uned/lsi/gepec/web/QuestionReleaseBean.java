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
import es.uned.lsi.gepec.om.OmHelper;
import es.uned.lsi.gepec.om.QuestionGenerator;
import es.uned.lsi.gepec.om.TestGenerator;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionReleasesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;

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
	
	private boolean publishAllowed;
	private String lastErrorMessage;
	
	private boolean restrictDates;
	private String startDateHidden;
	private String closeDateHidden;
	private String warningDateHidden;
	private String deleteDateHidden;
	private List<User> filteredUsersForAddingUsers;
	private List<UserType> userTypes;
	private long filterUsersUserTypeId;
	private boolean filterUsersIncludeOmUsers;
	private DualListModel<User> usersDualList;
	private boolean enabledCheckboxesSetters;
	
	public QuestionReleaseBean()
	{
		questionRelease=null;
		restrictDates=false;
		filteredUsersForAddingUsers=null;
		userTypes=null;
		filterUsersUserTypeId=0L;
		filterUsersIncludeOmUsers=true;
		usersDualList=null;
		enabledCheckboxesSetters=true;
		activeQuestionReleaseTabName=GENERAL_WIZARD_TAB;
		publishAllowed=true;
		lastErrorMessage=null;
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
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
    
	private boolean isPublishEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishOtherUsersEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_OTHER_USERS_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishAdminsEnabled(Operation operation)
	{
		return userSessionService.isGranted(
			getCurrentUserOperation(operation),"PERMISSION_PUBLICATION_PUBLISH_ADMINS_QUESTIONS_ENABLED");
	}
	
	private boolean isPublishSuperadminsEnabled(Operation operation)
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
	
	public QuestionRelease getQuestionRelease()
	{
		return getQuestionRelease(null);
	}
	
	private QuestionRelease getQuestionRelease(Operation operation)
	{
		if (questionRelease==null)
		{
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    		long questionId=Long.parseLong(params.get("questionId"));
    		
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
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
		return isAllUsersAllowed(null);
	}
	
	public void setAllUsersAllowed(boolean allUsersAllowed)
	{
		setAllUsersAllowed(null,allUsersAllowed);
	}
	
	private boolean isAllUsersAllowed(Operation operation)
	{
		return getQuestionRelease(getCurrentUserOperation(operation)).isAllUsersAllowed();
	}
	
	private void setAllUsersAllowed(Operation operation,boolean allUsersAllowed)
	{
		if (isEnabledChecboxesSetters())
		{
			getQuestionRelease(getCurrentUserOperation(operation)).setAllUsersAllowed(allUsersAllowed);
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
		getQuestionRelease(getCurrentUserOperation(operation));
		return restrictDates;
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
		return getStartDate(null);
	}
	
	public void setStartDate(Date startDate)
	{
		setStartDate(null,startDate);
	}
	
	private Date getStartDate(Operation operation)
	{
		QuestionRelease questionRelease=getQuestionRelease(getCurrentUserOperation(operation));
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
	
	private void setStartDate(Operation operation,Date startDate)
	{
		getQuestionRelease(getCurrentUserOperation(operation)).setStartDate(startDate);
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
		return getCloseDate(null);
	}
	
	public void setCloseDate(Date closeDate)
	{
		setCloseDate(null,closeDate);
	}
	
	private Date getCloseDate(Operation operation)
	{
		QuestionRelease questionRelease=getQuestionRelease(getCurrentUserOperation(operation));
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
	
	private void setCloseDate(Operation operation,Date closeDate)
	{
		getQuestionRelease(getCurrentUserOperation(operation)).setCloseDate(closeDate);
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
		return getWarningDate(null);
	}
	
	public void setWarningDate(Date warningDate)
	{
		setWarningDate(null,warningDate);
	}
	
	private Date getWarningDate(Operation operation)
	{
		QuestionRelease questionRelease=getQuestionRelease(getCurrentUserOperation(operation));
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
	
	private void setWarningDate(Operation operation,Date warningDate)
	{
		getQuestionRelease(getCurrentUserOperation(operation)).setWarningDate(warningDate);
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
		return getDeleteDate(null);
	}
	
	public void setDeleteDate(Date deleteDate)
	{
		setDeleteDate(null,deleteDate);
	}
	
	private Date getDeleteDate(Operation operation)
	{
		QuestionRelease questionRelease=getQuestionRelease(getCurrentUserOperation(operation));
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
	
	private void setDeleteDate(Operation operation,Date deleteDate)
	{
		getQuestionRelease(getCurrentUserOperation(operation)).setDeleteDate(deleteDate);
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
		return getDisplayEquations(null);
	}
	
	private String getDisplayEquations(Operation operation)
	{
		return getQuestionRelease(getCurrentUserOperation(operation)).getQuestion().isDisplayEquations()?
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
   			setStartDateHidden(startDateHidden);
   			setCloseDateHidden(closeDateHidden);
   			setWarningDateHidden(warningDateHidden);
   		}
		return ok;
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
		
		List<User> questionReleaseUsers=getQuestionRelease(operation).getUsers();
		for (User user:getFilteredUsersForAddingUsers(operation))
		{
			if (!questionReleaseUsers.contains(user))
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
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
   		for (User user:usersDualList.getTarget())
  		{
   			getQuestionRelease(operation).getUsers().add(user);
   		}
    }
	
    /**
     * ActionListener that deletes an user from list of users allowed to do test.
     * @param event Action event
     */
    public void removeUser(ActionEvent event)
    {
		// We need to process some input fields
		getQuestionRelease(getCurrentUserOperation(null)).getUsers().remove(
			(User)event.getComponent().getAttributes().get("user"));
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
    
	public boolean isPublishAllowed()
	{
		return publishAllowed;
	}
    
	/**
	 * Publish question to production navigator environment.
	 * @return Next wiew (publication page if save is sucessful, otherwise we keep actual view)
	 */
	public String publishQuestion()
	{
		String nextView="publication?faces-redirect=true";
		QuestionRelease questionRelease=null;
		Date startDateAux=null;
		Date closeDateAux=null;
		Date warningDateAux=null;
		User currentUser=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		if (isPublishEnabled(operation))
		{
			// Get question release
			questionRelease=getQuestionRelease(operation);
			User questionAuthor=questionRelease.getQuestion().getCreatedBy();
			currentUser=userSessionService.getCurrentUser(operation);			
			
			if (!currentUser.equals(questionAuthor) && !isPublishOtherUsersEnabled(operation) && 
				(isAdmin(operation,questionAuthor) || !isPublishAdminsEnabled(operation)) &&
				(isSuperadmin(operation,questionAuthor) || !isPublishSuperadminsEnabled(operation)))
			{
				addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
				nextView=null;
			}
			else
			{
				startDateAux=questionRelease.getStartDate();
				closeDateAux=questionRelease.getCloseDate();
				warningDateAux=questionRelease.getWarningDate();
				if (!isRestrictDates(operation))
				{
					questionRelease.setStartDate(null);
					questionRelease.setCloseDate(null);
					questionRelease.setWarningDate(null);
				}
			}
		}
		if (!checkQuestion(operation,questionRelease.getQuestion()))
		{
			publishAllowed=false;
			nextView=null;
		}
		
		if (nextView!=null)
		{
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
		if (nextView==null && questionRelease!=null)
		{
			questionRelease.setStartDate(startDateAux);
			questionRelease.setCloseDate(closeDateAux);
			questionRelease.setWarningDate(warningDateAux);
		}
		return nextView;
	}
	
	/**
	 * @param operation Operation
	 * @param question Question
	 * @return true if question has not been modified nor deleted while publishing it
	 */
	private boolean checkQuestion(Operation operation,Question question)
	{
		boolean ok=true;
		Question questionFromDB=questionsService.getQuestion(getCurrentUserOperation(operation),question.getId());
		if (questionFromDB==null)
		{
			ok=false;
			addErrorMessage("PUBLISH_QUESTION_DELETED_ERROR");
		}
		else if (!questionFromDB.getTimemodified().equals(question.getTimemodified()))
		{
			ok=false;
			addErrorMessage("PUBLISH_QUESTION_CHANGED_ERROR");
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
		String target=(String)event.getComponent().getAttributes().get("target");
		if (isPublishAllowed())
		{
			setCancelPublishQuestionTarget(target);
			RequestContext rq=RequestContext.getCurrentInstance();
			rq.execute("confirmCancelPublishQuestionDialog.show()");
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
		lastErrorMessage=message;
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,
			new FacesMessage(FacesMessage.SEVERITY_ERROR,localizationService.getLocalizedMessage(message),null));
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
