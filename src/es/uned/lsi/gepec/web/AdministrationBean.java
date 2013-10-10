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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;

import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.UserGroupBean;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.TestsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;

/**
 * Managed bean for users/roles management.
 */
@SuppressWarnings("serial")
@ManagedBean(name="administrationBean")
@ViewScoped
public class AdministrationBean implements Serializable
{
	private final static int USERS_TABVIEW_TAB=0;
	private final static int ROLES_TABVIEW_TAB=1;
	
	private final static String CONFIRM_DELETE_USER="CONFIRM_DELETE_USER";
	private final static String CONFIRM_DELETE_USERTYPE="CONFIRM_DELETE_ROLE";
	
	private final static long FILTER_ALL_ROLES=0L;
	private final static long FILTER_NONE_ROLES=-1;
	
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	@ManagedProperty(value="#{resourcesService}")
	private ResourcesService resourcesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{testsService}")
	private TestsService testsService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	private long filterUserTypeId;
	private boolean filterIncludeOmUsers;
	
	private List<User> users;
	private long userId;
	
	private List<UserType> userTypes;
	private long userTypeId;
	
	private int activeAdministrationTabIndex;
	
	private String confirmType;
	
	private boolean criticalErrorMessage;
	
	private Boolean adminUsersEnabled;
	private Boolean adminUserTypesEnabled;
	private Boolean addUsersEnabled;
	private Boolean editUsersEnabled;
	private Boolean deleteUsersEnabled;
	private Boolean addUserTypesEnabled;
	private Boolean editUserTypesEnabled;
	private Boolean deleteUserTypesEnabled;
	private Boolean editAdminsEnabled;
	private Boolean editSuperadminsEnabled;
	private Boolean deleteAdminsEnabled;
	private Boolean deleteSuperadminsEnabled;
	private Boolean editAdminRolesEnabled;
	private Boolean editSuperadminRolesEnabled;
	private Boolean deleteAdminRolesEnabled;
	private Boolean deleteSuperadminRolesEnabled;
	
	private Map<Long,Boolean> admins;
	private Map<Long,Boolean> superadmins;
	private Map<Long,Boolean> adminRoles;
	private Map<Long, Boolean> superadminRoles;
	
	private Map<Long,Boolean> editUsersAllowed;
	private Map<Long,Boolean> deleteUsersAllowed;
	private Map<Long,Boolean> editUserTypesAllowed;
	private Map<Long,Boolean> deleteUserTypesAllowed;
	
	private User lastUser;
	private String lastUserGroupsShort;
	private String lastUserGroupsLong;
	
	public AdministrationBean()
	{
		activeAdministrationTabIndex=USERS_TABVIEW_TAB;
		confirmType=null;
		criticalErrorMessage=false;
		filterUserTypeId=0L;
		filterIncludeOmUsers=false;
		users=null;
		userId=0L;
		userTypes=null;
		userTypeId=0L;
		adminUsersEnabled=null;
		adminUserTypesEnabled=null;
		addUsersEnabled=null;
		editUsersEnabled=null;
		deleteUsersEnabled=null;
		addUserTypesEnabled=null;
		editUserTypesEnabled=null;
		deleteUserTypesEnabled=null;
		editAdminsEnabled=null;
		editSuperadminsEnabled=null;
		deleteAdminsEnabled=null;
		deleteSuperadminsEnabled=null;
		editAdminRolesEnabled=null;
		editSuperadminRolesEnabled=null;
		deleteAdminRolesEnabled=null;
		deleteSuperadminRolesEnabled=null;
		admins=new HashMap<Long,Boolean>();
		superadmins=new HashMap<Long,Boolean>();
		adminRoles=new HashMap<Long,Boolean>();
		superadminRoles=new HashMap<Long,Boolean>();
		editUsersAllowed=new HashMap<Long,Boolean>();
		deleteUsersAllowed=new HashMap<Long,Boolean>();
		editUserTypesAllowed=new HashMap<Long,Boolean>();
		deleteUserTypesAllowed=new HashMap<Long,Boolean>();
		lastUser=null;
	}
	
	public void setUserSessionService(UserSessionService userSessionService)
	{
		this.userSessionService=userSessionService;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	public void setUserTypesService(UserTypesService userTypesService)
	{
		this.userTypesService=userTypesService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	public void setResourcesService(ResourcesService resourcesService)
	{
		this.resourcesService=resourcesService;
	}
	
	public void setQuestionsService(QuestionsService questionsService)
	{
		this.questionsService=questionsService;
	}
	
	public void setTestsService(TestsService testsService)
	{
		this.testsService=testsService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
    private Operation getCurrentUserOperation(Operation operation)
    {
    	if (operation!=null)
    	{
    		if (users==null && activeAdministrationTabIndex==USERS_TABVIEW_TAB)
    		{
    			getUsers();
    			operation=null;
    		}
    		else if (userTypes==null && activeAdministrationTabIndex==ROLES_TABVIEW_TAB)
    		{
    			getUserTypes();
    			operation=null;
    		}
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
    /**
     * @return true if current user is allowed to navigate "Administration" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Administration" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_ADMINISTRATION");
    }
    
	public long getFilterUserTypeId()
	{
		return filterUserTypeId;
	}
	
	public void setFilterUserTypeId(long filterUserTypeId)
	{
		this.filterUserTypeId=filterUserTypeId;
	}
	
	public boolean isFilterIncludeOmUsers()
	{
		return filterIncludeOmUsers;
	}
	
	public void setFilterIncludeOmUsers(boolean filterIncludeOmUsers)
	{
		this.filterIncludeOmUsers=filterIncludeOmUsers;
	}
	
	/**
	 * Tab change listener for displaying other tab of the 'Administration' page.
	 * @param event Tab change event
	 */
	public void changeActiveAdministrationTab(TabChangeEvent event)
	{
    	TabView administrationFormTabs=(TabView)event.getComponent();
    	activeAdministrationTabIndex=administrationFormTabs.getActiveIndex();
    	switch (activeAdministrationTabIndex)
    	{
    		case USERS_TABVIEW_TAB:
    			setUsers(null);
    			setUserTypes(null);
    			break;
    		case ROLES_TABVIEW_TAB:
    			setUserTypes(null);
    	}
	}
	
	public List<User> getUsers()
	{
		List<User> users=null;
		if (this.users==null)
		{
			users=new ArrayList<User>();
			if (activeAdministrationTabIndex==USERS_TABVIEW_TAB)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
	    		
				lastUser=null;
				long filterUserTypeId=getFilterUserTypeId();
				if (filterUserTypeId>0L)
				{
					users=usersService.getSortedUsers(getCurrentUserOperation(null),filterUserTypeId,false);
				}
				else if (filterUserTypeId==FILTER_ALL_ROLES)
				{
					users=usersService.getSortedUsers(
						getCurrentUserOperation(null),FILTER_ALL_ROLES,isFilterIncludeOmUsers());
				}
				else if (filterUserTypeId==FILTER_NONE_ROLES)
				{
					users=usersService.getSortedUsersWithoutUserType(
						getCurrentUserOperation(null),isFilterIncludeOmUsers());
				}
				this.users=users;
			}
		}
		else
		{
			users=this.users;
		}
		return users;
	}
	
	public void setUsers(List<User> users)
	{
		this.users=users;
	}
	
	public long getUserId()
	{
		return userId;
	}
	
	public void setUserId(long id)
	{
		this.userId=id;
	}
	
	public List<UserType> getUserTypes()
	{
		List<UserType> userTypes=null;
		if (this.userTypes==null)
		{
			userTypes=new ArrayList<UserType>();
			
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
	    	
			userTypes=userTypesService.getUserTypes(getCurrentUserOperation(null));
			this.userTypes=userTypes;
		}
		else
		{
			userTypes=this.userTypes;
		}
		return userTypes;
	}
	
	public void setUserTypes(List<UserType> userTypes)
	{
		this.userTypes=userTypes;
	}
	
	public long getUserTypeId()
	{
		return userTypeId;
	}
	
	public void setUserTypeId(long userTypeId)
	{
		this.userTypeId=userTypeId;
	}
	
	public UserType getUserType()
	{
		UserType userType=null;
		if (userTypeId>0L)
		{
			// We search user type in the bean list of user types
			// IMPORTANT: We do it this way to avoid Hibernate errors than can be produced if we have
			// different instances of the same user type
			userType=new UserType();
			userType.setId(userTypeId);
			List<UserType> userTypes=getUserTypes();
			int userTypePos=userTypes.indexOf(userType);
			userType=userTypePos==-1?null:userTypes.get(userTypePos);
		}
		return userType;
	}
	
	public String getConfirmType()
	{
		return confirmType;
	}
	
	public void setConfirmType(String confirmType)
	{
		this.confirmType=confirmType;
	}
	
	public boolean isCriticalErrorMessage()
	{
		return criticalErrorMessage;
	}
	
	public void setCriticalErrorMessage(boolean criticalErrorMessage)
	{
		this.criticalErrorMessage=criticalErrorMessage;
	}
	
	private Boolean getAdminUsersEnabled(Operation operation)
	{
		if (adminUsersEnabled==null)
		{
			try
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				adminUsersEnabled=
					Boolean.valueOf(userSessionService.isGranted(operation,"PERMISSION_ADMINISTRATION_ADMIN_USERS"));
			}
			catch (ServiceException se)
			{
				adminUsersEnabled=Boolean.FALSE;
			}
		}
		return adminUsersEnabled;
	}
	
	public Boolean getAdminUsersEnabled()
	{
		return getAdminUsersEnabled(null);
	}
	
	public void setAdminUsersEnabled(Boolean adminUsersEnabled)
	{
		this.adminUsersEnabled=adminUsersEnabled;
	}
	
	public boolean isAdminUsersEnabled()
	{
		return getAdminUsersEnabled().booleanValue(); 
	}
	
	private Boolean getAdminUserTypesEnabled(Operation operation)
	{
		if (adminUserTypesEnabled==null)
		{
			try
			{
				// Get current user session Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				adminUserTypesEnabled=
					Boolean.valueOf(userSessionService.isGranted(operation,"PERMISSION_ADMINISTRATION_ADMIN_ROLES"));
			}
			catch (ServiceException se)
			{
				adminUserTypesEnabled=Boolean.FALSE;
			}
		}
		return adminUserTypesEnabled;
	}
	
	public Boolean getAdminUserTypesEnabled()
	{
		return getAdminUserTypesEnabled(null);
	}
	
	public void setAdminUserTypesEnabled(Boolean adminUserTypesEnabled)
	{
		this.adminUserTypesEnabled=adminUserTypesEnabled;
	}
	
	public boolean isAdminUserTypesEnabled()
	{
		return getAdminUserTypesEnabled().booleanValue(); 
	}
	
	public boolean isAdminEnabled()
	{
		return isAdminEnabled(null);
	}
	
	private boolean isAdminEnabled(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		return getAdminUsersEnabled(operation).booleanValue() || getAdminUserTypesEnabled(operation).booleanValue();
	}
	
	private Boolean getAddUsersEnabled(Operation operation)
	{
		if (addUsersEnabled==null)
		{
			addUsersEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_ADD_USER_ENABLED"));
		}
		return addUsersEnabled;
	}
	
	public Boolean getAddUsersEnabled()
	{
		return getAddUsersEnabled(null);
	}
	
	public void setAddUsersEnabled(Boolean addUsersEnabled)
	{
		this.addUsersEnabled=addUsersEnabled;
	}
	
	public boolean isAddUsersEnabled()
	{
		return getAddUsersEnabled().booleanValue();
	}
	
	private Boolean getEditUsersEnabled(Operation operation)
	{
		if (editUsersEnabled==null)
		{
			editUsersEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_USER_ENABLED"));
		}
		return editUsersEnabled;
	}
	
	public Boolean getEditUsersEnabled()
	{
		return getEditUsersEnabled(null);
	}
	
	public void setEditUsersEnabled(Boolean editUsersEnabled)
	{
		this.editUsersEnabled=editUsersEnabled;
	}
	
	public boolean isEditUsersEnabled()
	{
		return getEditUsersEnabled().booleanValue();
	}
	
	private Boolean getDeleteUsersEnabled(Operation operation)
	{
		if (deleteUsersEnabled==null)
		{
			deleteUsersEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_USER_ENABLED"));
		}
		return deleteUsersEnabled;
	}
	
	public Boolean getDeleteUsersEnabled()
	{
		return getDeleteUsersEnabled(null);
	}
	
	public void setDeleteUsersEnabled(Boolean deleteUsersEnabled)
	{
		this.deleteUsersEnabled=deleteUsersEnabled;
	}
	
	public boolean isDeleteUsersEnabled()
	{
		return getDeleteUsersEnabled().booleanValue();
	}
	
	private Boolean getAddUserTypesEnabled(Operation operation)
	{
		if (addUserTypesEnabled==null)
		{
			addUserTypesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_ADD_ROLE_ENABLED"));
		}
		return addUserTypesEnabled;
	}
	
	public Boolean getAddUserTypesEnabled()
	{
		return getAddUserTypesEnabled(null);
	}
	
	public void setAddUserTypesEnabled(Boolean addUserTypesEnabled)
	{
		this.addUserTypesEnabled=addUserTypesEnabled;
	}
	
	public boolean isAddUserTypesEnabled()
	{
		return getAddUserTypesEnabled().booleanValue();
	}
	
	private Boolean getEditUserTypesEnabled(Operation operation)
	{
		if (editUserTypesEnabled==null)
		{
			editUserTypesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_ROLE_ENABLED"));
		}
		return editUserTypesEnabled;
	}
	
	public Boolean getEditUserTypesEnabled()
	{
		return getEditUserTypesEnabled(null);
	}
	
	public void setEditUserTypesEnabled(Boolean editUserTypesEnabled)
	{
		this.editUserTypesEnabled=editUserTypesEnabled;
	}
	
	public boolean isEditUserTypesEnabled()
	{
		return getEditUserTypesEnabled().booleanValue();
	}
	
	private Boolean getDeleteUserTypesEnabled(Operation operation)
	{
		if (deleteUserTypesEnabled==null)
		{
			deleteUserTypesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_ROLE_ENABLED"));
		}
		return deleteUserTypesEnabled;
	}
	
	public Boolean getDeleteUserTypesEnabled()
	{
		return getDeleteUserTypesEnabled(null);
	}
	
	public void setDeleteUserTypesEnabled(Boolean deleteUserTypesEnabled)
	{
		this.deleteUserTypesEnabled=deleteUserTypesEnabled;
	}
	
	public boolean isDeleteUserTypesEnabled()
	{
		return getDeleteUserTypesEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsEnabled(Operation operation)
	{
		if (editAdminsEnabled==null)
		{
			editAdminsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_ADMINS_ENABLED"));
		}
		return editAdminsEnabled;
	}
	
	public Boolean getEditAdminsEnabled()
	{
		return getEditAdminsEnabled(null);
	}
	
	public void setEditAdminsEnabled(Boolean editAdminsEnabled)
	{
		this.editAdminsEnabled=editAdminsEnabled;
	}
	
	public boolean isEditAdminsEnabled()
	{
		return getEditAdminsEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsEnabled(Operation operation)
	{
		if (editSuperadminsEnabled==null)
		{
			editSuperadminsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_SUPERADMINS_ENABLED"));
		}
		return editSuperadminsEnabled;
	}
	
	public Boolean getEditSuperadminsEnabled()
	{
		return getEditSuperadminsEnabled(null);
	}
	
	public void setEditSuperadminsEnabled(Boolean editSuperadminsEnabled)
	{
		this.editSuperadminsEnabled=editSuperadminsEnabled;
	}
	
	public boolean isEditSuperadminsEnabled()
	{
		return getEditSuperadminsEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminsEnabled(Operation operation)
	{
		if (deleteAdminsEnabled==null)
		{
			deleteAdminsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_ADMINS_ENABLED"));
		}
		return deleteAdminsEnabled;
	}
	
	public Boolean getDeleteAdminsEnabled()
	{
		return getDeleteAdminsEnabled(null);
	}
	
	public void setDeleteAdminsEnabled(Boolean deleteAdminsEnabled)
	{
		this.deleteAdminsEnabled=deleteAdminsEnabled;
	}
	
	public boolean isDeleteAdminsEnabled()
	{
		return getDeleteAdminsEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminsEnabled(Operation operation)
	{
		if (deleteSuperadminsEnabled==null)
		{
			deleteSuperadminsEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_SUPERADMINS_ENABLED"));
		}
		return deleteSuperadminsEnabled;
	}
	
	public Boolean getDeleteSuperadminsEnabled()
	{
		return getDeleteSuperadminsEnabled(null);
	}
	
	public void setDeleteSuperadminsEnabled(Boolean deleteSuperadminsEnabled)
	{
		this.deleteSuperadminsEnabled=deleteSuperadminsEnabled;
	}
	
	public boolean isDeleteSuperadminsEnabled()
	{
		return getDeleteSuperadminsEnabled().booleanValue();
	}
	
	private Boolean getEditAdminRolesEnabled(Operation operation)
	{
		if (editAdminRolesEnabled==null)
		{
			editAdminRolesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_ADMIN_ROLES_ENABLED"));
		}
		return editAdminRolesEnabled;
	}
	
	public Boolean getEditAdminRolesEnabled()
	{
		return getEditAdminRolesEnabled(null);
	}
	
	public void setEditAdminRolesEnabled(Boolean editAdminRolesEnabled)
	{
		this.editAdminRolesEnabled=editAdminRolesEnabled;
	}
	
	public boolean isEditAdminRolesEnabled()
	{
		return getEditAdminRolesEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminRolesEnabled(Operation operation)
	{
		if (editSuperadminRolesEnabled==null)
		{
			editSuperadminRolesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_EDIT_SUPERADMIN_ROLES_ENABLED"));
		}
		return editSuperadminRolesEnabled;
	}
	
	public Boolean getEditSuperadminRolesEnabled()
	{
		return getEditSuperadminRolesEnabled(null);
	}
	
	public void setEditSuperadminRolesEnabled(Boolean editSuperadminRolesEnabled)
	{
		this.editSuperadminRolesEnabled=editSuperadminRolesEnabled;
	}
	
	public boolean isEditSuperadminRolesEnabled()
	{
		return getEditSuperadminRolesEnabled().booleanValue();
	}
	
	private Boolean getDeleteAdminRolesEnabled(Operation operation)
	{
		if (deleteAdminRolesEnabled==null)
		{
			deleteAdminRolesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_ADMIN_ROLES_ENABLED"));
		}
		return deleteAdminRolesEnabled;
	}
	
	public Boolean getDeleteAdminRolesEnabled()
	{
		return getDeleteAdminRolesEnabled(null);
	}
	
	public void setDeleteAdminRolesEnabled(Boolean deleteAdminRolesEnabled)
	{
		this.deleteAdminRolesEnabled=deleteAdminRolesEnabled;
	}
	
	public boolean isDeleteAdminRolesEnabled()
	{
		return getDeleteAdminRolesEnabled().booleanValue();
	}
	
	private Boolean getDeleteSuperadminRolesEnabled(Operation operation)
	{
		if (deleteSuperadminRolesEnabled==null)
		{
			deleteSuperadminRolesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_ADMINISTRATION_DELETE_SUPERADMIN_ROLES_ENABLED"));
		}
		return deleteSuperadminRolesEnabled;
	}
	
	public Boolean getDeleteSuperadminRolesEnabled()
	{
		return getDeleteSuperadminRolesEnabled(null);
	}
	
	public void setDeleteSuperadminRolesEnabled(Boolean deleteSuperadminRolesEnabled)
	{
		this.deleteSuperadminRolesEnabled=deleteSuperadminRolesEnabled;
	}
	
	public boolean isDeleteSuperadminRolesEnabled()
	{
		return getDeleteSuperadminRolesEnabled().booleanValue();
	}
	
	private void resetAdmins()
	{
		admins.clear();
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
	
	private void resetAdminRoles()
	{
		adminRoles.clear();
	}
	
	private boolean isAdminRole(Operation operation,UserType userType)
	{
		boolean adminRole=false;
		if (userType!=null)
		{
			Long userTypeId=Long.valueOf(userType.getId());
			if (adminRoles.containsKey(userTypeId))
			{
				adminRole=adminRoles.get(userTypeId).booleanValue();
			}
			else
			{
				adminRole=permissionsService.isGranted(
					getCurrentUserOperation(operation),userType,"PERMISSION_NAVIGATION_ADMINISTRATION");
				adminRoles.put(userTypeId,Boolean.valueOf(adminRole));
			}
		}
		return adminRole;
	}
	
	private void resetSuperadminRoles()
	{
		superadminRoles.clear();
	}
	
	private boolean isSuperadminRole(Operation operation,UserType userType)
	{
		boolean superadminRole=false;
		if (userType!=null)
		{
			Long userTypeId=Long.valueOf(userType.getId());
			if (superadminRoles.containsKey(userTypeId))
			{
				superadminRole=superadminRoles.get(userTypeId).booleanValue();
			}
			else
			{
				superadminRole=permissionsService.isGranted(getCurrentUserOperation(operation),userType,
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
				superadminRoles.put(userTypeId,Boolean.valueOf(superadminRole));
			}
		}
		return superadminRole;
	}
	
	private void resetEditUsersAllowed()
	{
		editUsersAllowed.clear();
	}
	
	private boolean isEditUserAllowed(Operation operation,long userId)
	{
		boolean allowed=false;
		if (userId>0L)
		{
			if (editUsersAllowed.containsKey(Long.valueOf(userId)))
			{
				allowed=editUsersAllowed.get(Long.valueOf(userId)).booleanValue();
			}
			else
			{
				try
				{
					if (userSessionService.getCurrentUserId()==userId)
					{
						allowed=true;
					}
					else
					{
						// Get current user Hibernate operation
						operation=getCurrentUserOperation(operation);
						
						allowed=getEditUsersEnabled(operation).booleanValue();
						if (allowed)
						{
							// Check if it is allowed to edit the user
							User user=usersService.getUser(operation,userId);
							if (isAdmin(operation,user) && !getEditAdminsEnabled(operation).booleanValue())
							{
								allowed=false;
							}
							if (allowed)
							{
								if (isSuperadmin(operation,user) && 
									!getEditSuperadminsEnabled(operation).booleanValue())
								{
									allowed=false;
								}
							}
						}
					}
				}
				catch (ServiceException se)
				{
					allowed=false;
				}
				editUsersAllowed.put(Long.valueOf(userId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isEditUserAllowed(User user)
	{
		return isEditUserAllowed(null,user==null?0L:user.getId());
	}
	
	private void resetDeleteUsersAllowed()
	{
		deleteUsersAllowed.clear();
	}
	
	private boolean isDeleteUserAllowed(Operation operation,long userId)
	{
		boolean allowed=false;
		if (userId>0L)
		{
			if (deleteUsersAllowed.containsKey(Long.valueOf(userId)))
			{
				allowed=deleteUsersAllowed.get(Long.valueOf(userId)).booleanValue();
			}
			else
			{
				try
				{
					if (userSessionService.getCurrentUserId()==userId)
					{
						allowed=true;
					}
					else
					{
						// Get current user Hibernate operation
						operation=getCurrentUserOperation(operation);
						
						allowed=getDeleteUsersEnabled(operation).booleanValue();
						if (allowed)
						{
							// Check if it is allowed to delete the user
							User user=usersService.getUser(operation,userId);
							if (isAdmin(operation,user) && !getDeleteAdminsEnabled(operation).booleanValue())
							{
								allowed=false;
							}
							if (allowed)
							{
								if (isSuperadmin(operation,user) && 
									!getDeleteSuperadminsEnabled(operation).booleanValue())
								{
									allowed=false;
								}
							}
						}
					}
				}
				catch (ServiceException se)
				{
					allowed=false;
				}
				deleteUsersAllowed.put(Long.valueOf(userId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isDeleteUserAllowed(User user)
	{
		return isDeleteUserAllowed(null,user==null?0L:user.getId());
	}
	
	private void resetEditUserTypesAllowed()
	{
		editUserTypesAllowed.clear();
	}
	
	private boolean isEditUserTypeAllowed(Operation operation,long userTypeId)
	{
		boolean allowed=false;
		if (userTypeId>0L)
		{
			if (editUserTypesAllowed.containsKey(Long.valueOf(userTypeId)))
			{
				allowed=editUserTypesAllowed.get(Long.valueOf(userTypeId)).booleanValue();
			}
			else
			{
				try
				{
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(operation);
					
					allowed=getEditUserTypesEnabled(operation).booleanValue();
					if (allowed)
					{
						// Check if it is allowed to edit the user
						UserType userType=getUserType();
						if (isAdminRole(operation,userType) && !getEditAdminsEnabled(operation).booleanValue())
						{
							allowed=false;
						}
						if (allowed)
						{
							if (isSuperadminRole(operation,userType) && 
								!getEditSuperadminsEnabled(operation).booleanValue())
							{
								allowed=false;
							}
						}
					}
				}
				catch (ServiceException se)
				{
					allowed=false;
				}
				editUserTypesAllowed.put(Long.valueOf(userTypeId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isEditUserTypeAllowed(UserType userType)
	{
		return isEditUserTypeAllowed(null,userType==null?0L:userType.getId());
	}
	
	private void resetDeleteUserTypesAllowed()
	{
		deleteUserTypesAllowed.clear();
	}
	
	private boolean isDeleteUserTypeAllowed(Operation operation,long userTypeId)
	{
		boolean allowed=false;
		if (userTypeId>0L)
		{
			if (deleteUserTypesAllowed.containsKey(Long.valueOf(userTypeId)))
			{
				allowed=deleteUserTypesAllowed.get(Long.valueOf(userTypeId)).booleanValue();
			}
			else
			{
				try
				{
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(operation);
					
					allowed=getDeleteUserTypesEnabled(operation).booleanValue();
					if (allowed)
					{
						// Check if it is allowed to delete the user type
						UserType userType=getUserType();
						if (isAdminRole(operation,userType) && !getDeleteAdminRolesEnabled(operation).booleanValue())
						{
							allowed=false;
						}
						if (allowed)
						{
							if (isSuperadminRole(operation,userType) && 
								!getDeleteSuperadminRolesEnabled(operation).booleanValue())
							{
								allowed=false;
							}
						}
					}
				}
				catch (ServiceException se)
				{
					allowed=false;
				}
				deleteUserTypesAllowed.put(Long.valueOf(userTypeId),Boolean.valueOf(allowed));
			}
		}
		return allowed;
	}
	
	public boolean isDeleteUserTypeAllowed(UserType userType)
	{
		return isDeleteUserTypeAllowed(null,userType==null?0L:userType.getId());
	}
	
	public String getUserTypeType(UserType userType)
	{
		return userType==null?"":userType.getType();
	}
	
	public String getUserTypeDescription(UserType userType)
	{
		return userType==null?"":userType.getDescription();
	}
	
	/**
	 * Abbreviates a string using ellipses.<br/><br/>
	 * Leading and trailing whitespace are ignored.<br/><br/>
	 * When abbreviating a string, this method will try to leave a whitespace between the last 
	 * non trimmed word and the ellipses if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of ellipses 
	 * is taking account when we need to abbreviate it)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
    public String abbreviate(String str,int maxLength)
    {
    	return StringUtils.abbreviate(str,maxLength);
    }
    
	/**
	 * Abbreviates words within a string using short ellipses.<br/><br/>
	 * @param str String to abbreviate words (if needed)
	 * @param maxWordLength Maximum length of the abbreviated words
	 * @return Same string but with large words abbreviated
	 */
	public String abbreviateWords(String str,int maxWordLength)
	{
		return StringUtils.abbreviateWords(str,maxWordLength);
	}
    
	/**
	 * @param text Text
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities 
	 */
	public String breakText(String text)
	{
		return HtmlUtils.breakText(text);
	}
    
	/**
	 * @param text Text
	 * @param maxBreaks Maximum number of breaks allowed or 0 unlimited
	 * @return Same text but replacing '\n' characters with &lt;br/&gt; tags after escaping it 
	 * using HTML entities
	 */
	public String breakText(String text,int maxBreaks)
	{
		return HtmlUtils.breakText(text,maxBreaks);
	}
	
	public String getConfirmMessage()
	{
		String confirmMessage="";
		if (CONFIRM_DELETE_USER.equals(getConfirmType()))
		{
			confirmMessage=localizationService.getLocalizedMessage(CONFIRM_DELETE_USER);
		}
		else if (CONFIRM_DELETE_USERTYPE.equals(getConfirmType()))
		{
			confirmMessage=localizationService.getLocalizedMessage(CONFIRM_DELETE_USERTYPE);
		}
		return confirmMessage;
	}
	
	/**
	 * Shows a dialog to confirm user deletion.
	 * @param user User to delete
	 */
	public void confirmDeleteUser(User user)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setConfirmType(null);
		setAdminUsersEnabled(null);
		if (getAdminUsersEnabled(operation).booleanValue())
		{
			long userId=user.getId();
			if (usersService.checkUserId(operation,userId))
			{
				resetAdmins();
				resetSuperadmins();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				if (isDeleteUserAllowed(operation,userId))
				{
					setConfirmType(CONFIRM_DELETE_USER);
					setUserId(userId);
				}
				else
				{
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setAdminUserTypesEnabled(null);
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
			}
			else
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				setAdminUserTypesEnabled(null);
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_NOT_FOUND_ERROR");
			}
		}
		else
		{
			setAdminUserTypesEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		
    	long userTypeId=getFilterUserTypeId();
    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
    	{
    		setFilterUserTypeId(0L);
    	}
    	
		// Reload users and roles from DB
		setUsers(null);
		setUserTypes(null);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		if (getConfirmType()!=null)
		{
			rq.execute("confirmDialog.show()");
		}
	}
	
	/**
	 * Shows a dialog to confirm user type deletion.
	 * @param userType User type to delete
	 */
	public void confirmDeleteUserType(UserType userType)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setConfirmType(null);
		setAdminUserTypesEnabled(null);
		if (getAdminUserTypesEnabled(operation).booleanValue())
		{
			long userTypeId=userType.getId();
			if (userTypesService.checkUserTypeId(operation,userTypeId))
			{
				resetAdminRoles();
				resetSuperadminRoles();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				if (isDeleteUserTypeAllowed(operation,userTypeId))
				{
					setConfirmType(CONFIRM_DELETE_USERTYPE);
					setUserTypeId(userTypeId);
				}
				else
				{
					setAdminUsersEnabled(null);
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
			}
			else
			{
				setAdminUsersEnabled(null);
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_NOT_FOUND_ERROR");
			}
		}
		else
		{
			setAdminUsersEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
			
	    	long userTypeId=getFilterUserTypeId();
	    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
	    	{
	    		setFilterUserTypeId(0L);
	    	}
	    	
			// Reload users from DB
			setUsers(null);
		}
		
		// Reload  roles from DB
		setUserTypes(null);
		
		RequestContext rq=RequestContext.getCurrentInstance();
		if (getConfirmType()!=null)
		{
			rq.execute("confirmDialog.show()");
		}
	}
	
    /**
	 * Change users to display on datatable based on filter.
     * @param event Action event
     */
    public void applyUsersFilter(ActionEvent event)
    {
    	long userTypeId=getFilterUserTypeId();
    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
    	{
    		setFilterUserTypeId(0L);
    		addErrorMessage(true,"INCORRECT_OPERATION","USERS_FILTER_ROLE_NOT_FOUND_ERROR");
    	}
    	
		// Reload users and roles from DB
		setUsers(null);
		setUserTypes(null);
    }
	
    public String getUserGroupsShort(User user)
    {
    	if (!user.equals(lastUser))
    	{
    		UserGroupBean ug=new UserGroupBean(user);
    		lastUserGroupsShort=ug.getGroupShort();
    		lastUserGroupsLong=ug.getGroupLong();
    		lastUser=user;
    	}
    	return lastUserGroupsShort;
    }
    
    public String getUserGroupsLong(User user)
    {
    	if (!user.equals(lastUser))
    	{
    		UserGroupBean ug=new UserGroupBean(user);
    		lastUserGroupsShort=ug.getGroupShort();
    		lastUserGroupsLong=ug.getGroupLong();
    		lastUser=user;
    	}
    	return lastUserGroupsLong;
    }
    
	/**
	 * Adds a new user.
	 * @return Next view
	 */
	public String addUser()
	{
		String newView=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setAdminUsersEnabled(null);
		if (getAdminUsersEnabled(operation).booleanValue())
		{
			setAddUsersEnabled(null);
			if (getAddUsersEnabled(operation).booleanValue())
			{
				newView="user";
			}
			else
			{
				resetAdmins();
				resetSuperadmins();
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				setAdminUserTypesEnabled(null);
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		else
		{
			setAdminUserTypesEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		
		long userTypeId=getFilterUserTypeId();
    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
    	{
    		setFilterUserTypeId(0L);
    	}
    	
    	// Reload users and roles from DB
    	setUsers(null);
		setUserTypes(null);
		
		return newView;
	}
	
	/**
	 * Edits an user.
	 * @return Update view 
	 */
	public String editUser(User user)
	{
		String updateView=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setAdminUsersEnabled(null);
		if (getAdminUsersEnabled(operation).booleanValue())
		{
			long userId=user.getId();
			if (usersService.checkUserId(operation,userId))
			{
				resetAdmins();
				resetSuperadmins();
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				if (isEditUserAllowed(operation,userId))
				{
					updateView="userupdate";
				}
				else
				{
					setAddUsersEnabled(null);
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					setAdminUserTypesEnabled(null);
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
			}
			else
			{
				setAdminUserTypesEnabled(null);
				if (isAdminEnabled(operation))
				{
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(true,"INCORRECT_OPERATION","USER_EDIT_NOT_FOUND_ERROR");
				}
			}
		}
		else
		{
			setAdminUserTypesEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		
    	long userTypeId=getFilterUserTypeId();
    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
    	{
    		setFilterUserTypeId(0L);
    	}
    	
    	// Reload users and roles from DB
    	setUsers(null);
		setUserTypes(null);
    	
		return updateView;
	}
	
	/**
	 * Checks if an user can be deleted or not and will show error messages otherwise.
	 * @param operation Operation
	 * @return true if user can be deleted, false otherwise
	 */
	private boolean checkDeleteUser(Operation operation)
	{
		boolean ok=true;
		try
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			setAdminUsersEnabled(null);
			if (getAdminUsersEnabled(operation).booleanValue())
			{
				if (usersService.checkUserId(getUserId()))
				{
					resetAdmins();
					resetSuperadmins();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetEditUsersAllowed();
					if (isDeleteUserAllowed(operation,getUserId()))
					{
						// Get user
						User user=usersService.getUser(operation,getUserId());
						if (user==null)
						{
							addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_NOT_FOUND_ERROR");
							ok=false;
						}
						else
						{
							// Check user resources
							if (resourcesService.getResourcesCount(operation,user)>0)
							{
								addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_RESOURCES_FOUND");
								ok=false;
							}
							
							// Check user questions
							if (questionsService.getQuestionsCount(operation,user)>0)
							{
								addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_QUESTIONS_FOUND");
								ok=false;
							}
							
							// Check user tests
							if (testsService.getTestsCount(operation,user)>0)
							{
								addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_TESTS_FOUND");
								ok=false;
							}
							
							// Check that all global categories of this user don't contain any resources, questions 
							// or tests from other users
							if (resourcesService.getAllUserGlobalCategoriesResourcesCount(operation,user,"",0L)>0 || 
								questionsService.getAllUserGlobalCategoriesQuestionsCount(operation,user,"","")>0 ||
								testsService.getAllUserGlobalCategoriesTestsCount(operation,user)>0)
							{
								addErrorMessage(
									true,"INCORRECT_OPERATION","USER_DELETE_NON_EMPTY_GLOBAL_CATEGORIES_FOUND");
								ok=false;
							}
						}
					}
					else
					{
						setAddUsersEnabled(null);
						setEditUsersEnabled(null);
						setEditAdminsEnabled(null);
						setEditSuperadminsEnabled(null);
						resetEditUsersAllowed();
						resetAdminRoles();
						resetSuperadminRoles();
						setAddUserTypesEnabled(null);
						setEditUserTypesEnabled(null);
						setEditAdminRolesEnabled(null);
						setEditSuperadminRolesEnabled(null);
						resetEditUserTypesAllowed();
						setDeleteUserTypesEnabled(null);
						setDeleteAdminRolesEnabled(null);
						setDeleteSuperadminRolesEnabled(null);
						resetDeleteUserTypesAllowed();
						
						addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
					}
				}
				else
				{
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetEditUsersAllowed();
					setAdminUserTypesEnabled(null);
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_NOT_FOUND_ERROR");
				}
			}
			else
			{
				setAdminUserTypesEnabled(null);
				if (isAdminEnabled(operation))
				{
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
				ok=false;
			}
		}
		catch (ServiceException se)
		{
			addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_CRITICAL_ERROR");
			ok=false;
		}
		return ok;
	}
	
	//TODO esta opción de borrado es muy restrictiva pero creo que es conveniente así por seguridad para evitar que se borren recursos, preguntas, pruebas, etc. que no debieran haberse borrado... tal vez más adelante sea interesante añadir una opción que fuerce el borrado pero: 1) es peligroso si al hacerlo se borra todo lo que el usuario hizo y 2) es algo de cierta complejidad si se quiere dar la opción de conservar todo o parte de lo que el usuario hizo
	/**
	 * Deletes an user. It will also delete all their categories.<br/><br/>
	 * Note that the operation will fail if the user has any resource, question or test.<br/><br/>
	 * The operation will also fail if the user has any non empty global category, even if the contents of these
	 * categories are from other users.
	 * @param event Action event
	 */
	public void deleteUser(ActionEvent event)
	{
		Operation operation=null;
		try
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(null);
			
			if (checkDeleteUser(operation))
			{
				Operation writeOp=null;
				try
				{
					// Start a new Hibernate operation
					writeOp=HibernateUtil.startOperation();
					
					// Delete user
					usersService.deleteUser(writeOp,usersService.getUser(writeOp,userId));
					
					// Reload users from DB
					setUsers(null);
					
					// Do commit
					writeOp.commit();
				}
				catch (ServiceException se)
				{
					// Do rollback
					writeOp.rollback();
					
					throw se;
				}
				finally
				{
					// End Hibernate operation
					HibernateUtil.endOperation(writeOp);
					
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
			}
		}
		catch (ServiceException se)
		{
			addErrorMessage(true,"INCORRECT_OPERATION","USER_DELETE_CRITICAL_ERROR");
		}
		finally
		{
			if (operation!=null)
			{
		    	long userTypeId=getFilterUserTypeId();
		    	if (userTypeId>0L && !userTypesService.checkUserTypeId(operation,userTypeId))
		    	{
		    		setFilterUserTypeId(0L);
		    	}
		    	
				// Reload users and roles from DB
				setUsers(null);
				setUserTypes(null);
			}
		}
	}
	
	/**
	 * Action listener to cancel confirmation dialog on users tab.
	 * @param event Action event
	 */
	public void cancelConfirmDeleteUser(ActionEvent event)
	{
		resetAdmins();
		resetSuperadmins();
		setAdminUsersEnabled(null);
		setAddUsersEnabled(null);
		setEditUsersEnabled(null);
		setEditAdminsEnabled(null);
		setEditSuperadminsEnabled(null);
		resetEditUsersAllowed();
		setDeleteUsersEnabled(null);
		setDeleteAdminsEnabled(null);
		setDeleteSuperadminsEnabled(null);
		resetDeleteUsersAllowed();
		setAddUserTypesEnabled(null);
		resetAdminRoles();
		resetSuperadminRoles();
		setAddUserTypesEnabled(null);
		setEditUserTypesEnabled(null);
		setEditAdminRolesEnabled(null);
		setEditSuperadminRolesEnabled(null);
		resetEditUserTypesAllowed();
		setDeleteUserTypesEnabled(null);
		setDeleteAdminRolesEnabled(null);
		setDeleteSuperadminRolesEnabled(null);
		resetDeleteUserTypesAllowed();
		
    	long userTypeId=getFilterUserTypeId();
    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
    	{
    		setFilterUserTypeId(0L);
    	}
    	
		// Reload users and roles from DB
		setUsers(null);
		setUserTypes(null);
	}
	
	/**
	 * Adds a new user type (a.k.a. role).
	 * @return Next view
	 */
	public String addUserType()
	{
		String newView=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setAdminUserTypesEnabled(null);
		if (getAdminUserTypesEnabled(operation).booleanValue())
		{
			setAddUserTypesEnabled(null);
			if (getAddUserTypesEnabled(operation).booleanValue())
			{
				newView="usertype";
			}
			else
			{
				resetAdmins();
				resetSuperadmins();
				setAdminUsersEnabled(null);
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
		}
		else
		{
			setAdminUsersEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
			
			long userTypeId=getFilterUserTypeId();
	    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
	    	{
	    		setFilterUserTypeId(0L);
	    	}
	    	
	    	// Reload users from DB
	    	setUsers(null);
		}
		
    	// Always reload roles from DB
		setUserTypes(null);
		
		return newView;
	}
	
	/**
	 * Edits an user type (a.k.a. role).
	 * @param userType User type
	 * @return Update view 
	 */
	public String editUserType(UserType userType)
	{
		String updateView=null;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		setAdminUserTypesEnabled(null);
		if (getAdminUserTypesEnabled(operation).booleanValue())
		{
			long userTypeId=userType.getId();
			if (userTypesService.checkUserTypeId(operation, userTypeId))
			{
				resetAdminRoles();
				resetSuperadminRoles();
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				
				// Reload user types from DB
				setUserTypes(null);
				
				if (isEditUserTypeAllowed(operation,userTypeId))
				{
					updateView="usertypeupdate";
				}
				else
				{
					resetAdmins();
					resetSuperadmins();
					setAdminUsersEnabled(null);
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					setAddUserTypesEnabled(null);
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
			}
			else
			{
				setAdminUsersEnabled(null);
				if (isAdminEnabled(operation))
				{
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(true,"INCORRECT_OPERATION","ROLE_EDIT_NOT_FOUND_ERROR");
				}
			}
		}
		else
		{
			setAdminUsersEnabled(null);
			if (isAdminEnabled(operation))
			{
				resetAdmins();
				resetSuperadmins();
				setAddUsersEnabled(null);
				setEditUsersEnabled(null);
				setEditAdminsEnabled(null);
				setEditSuperadminsEnabled(null);
				resetEditUsersAllowed();
				setDeleteUsersEnabled(null);
				setDeleteAdminsEnabled(null);
				setDeleteSuperadminsEnabled(null);
				resetDeleteUsersAllowed();
				resetAdminRoles();
				resetSuperadminRoles();
				setAddUserTypesEnabled(null);
				setEditUserTypesEnabled(null);
				setEditAdminRolesEnabled(null);
				setEditSuperadminRolesEnabled(null);
				resetEditUserTypesAllowed();
				setDeleteUserTypesEnabled(null);
				setDeleteAdminRolesEnabled(null);
				setDeleteSuperadminRolesEnabled(null);
				resetDeleteUserTypesAllowed();
				
				addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
			}
			
			long userTypeId=getFilterUserTypeId();
	    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
	    	{
	    		setFilterUserTypeId(0L);
	    	}
	    	
	    	// Reload users from DB
	    	setUsers(null);
		}
		
    	// Always reload roles from DB
		setUserTypes(null);
		
		return updateView;
	}
	
	/**
	 * Checks if an user type can be deleted or not and will show error messages otherwise.
	 * @param operation Operation
	 * @return true if user type can be deleted, false otherwise
	 */
	private boolean checkDeleteUserType(Operation operation)
	{
		boolean ok=true;
		try
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			setAdminUserTypesEnabled(null);
			if (getAdminUserTypesEnabled(operation).booleanValue())
			{
				if (userTypesService.checkUserTypeId(operation,getUserTypeId()))
				{
					resetAdminRoles();
					resetSuperadminRoles();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					if (isDeleteUserTypeAllowed(operation,getUserTypeId()))
					{
						// Get user type
						UserType userType=getUserType();
						if (userType==null)
						{
							addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_NOT_FOUND_ERROR");
							ok=false;
						}
						else
						{
							// Check that there are no users with this user type
							for (User user:usersService.getUsers(operation))
							{
								if (userType.equals(user.getUserType()))
								{
									addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_USERS_FOUND");
									ok=false;
									break;
								}
							}
						}
					}
					else
					{
						resetAdmins();
						resetSuperadmins();
						setAddUsersEnabled(null);
						setEditUsersEnabled(null);
						setEditAdminsEnabled(null);
						setEditSuperadminsEnabled(null);
						resetEditUsersAllowed();
						setDeleteUsersEnabled(null);
						setDeleteAdminsEnabled(null);
						setDeleteSuperadminsEnabled(null);
						resetDeleteUsersAllowed();
						setAddUserTypesEnabled(null);
						setEditUserTypesEnabled(null);
						setEditAdminRolesEnabled(null);
						setEditSuperadminRolesEnabled(null);
						resetDeleteUserTypesAllowed();
						
						addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
					}
				}
				else
				{
					setAdminUsersEnabled(null);
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetEditUsersAllowed();
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_NOT_FOUND_ERROR");
				}
			}
			else
			{
				setAdminUsersEnabled(null);
				if (isAdminEnabled(operation))
				{
					resetAdmins();
					resetSuperadmins();
					setAddUsersEnabled(null);
					setEditUsersEnabled(null);
					setEditAdminsEnabled(null);
					setEditSuperadminsEnabled(null);
					resetEditUsersAllowed();
					setDeleteUsersEnabled(null);
					setDeleteAdminsEnabled(null);
					setDeleteSuperadminsEnabled(null);
					resetDeleteUsersAllowed();
					resetAdminRoles();
					resetSuperadminRoles();
					setAddUserTypesEnabled(null);
					setEditUserTypesEnabled(null);
					setEditAdminRolesEnabled(null);
					setEditSuperadminRolesEnabled(null);
					resetEditUserTypesAllowed();
					setDeleteUserTypesEnabled(null);
					setDeleteAdminRolesEnabled(null);
					setDeleteSuperadminRolesEnabled(null);
					resetDeleteUserTypesAllowed();
					
					addErrorMessage(false,"INCORRECT_OPERATION","NON_AUTHORIZED_ACTION_ERROR");
				}
				ok=false;
			}
		}
		catch (ServiceException se)
		{
			addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_CRITICAL_ERROR");
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Deletes an user type.<br/><br/>
	 * Note that the operation will fail if there is any user with that user type.
	 * @param event Action event
	 */
	public void deleteUserType(ActionEvent event)
	{
		Operation operation=null;
		try
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(null);
			
			if (checkDeleteUserType(operation))
			{
				Operation writeOp=null;
				try
				{
					// Start a new Hibernate operation
					writeOp=HibernateUtil.startOperation();
					
					// Delete user type
					userTypesService.deleteUserType(writeOp,getUserType());
						
					// Reload user types from DB
					setUserTypes(null);
					
					// Do commit
					writeOp.commit();
				}
				catch (ServiceException se)
				{
					// Do rollback
					writeOp.rollback();
					
					throw se;
				}
				finally
				{
					// End Hibernate operation
					HibernateUtil.endOperation(writeOp);
					
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					// Get current user session Hibernate operation
					operation=getCurrentUserOperation(null);
				}
			}
		}
		catch (ServiceException se)
		{
			addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_CRITICAL_ERROR");
		}
		finally
		{
			if (operation!=null)
			{
				setAdminUserTypesEnabled(null);
				if (!getAdminUserTypesEnabled(operation).booleanValue())
				{
			    	long userTypeId=getFilterUserTypeId();
			    	if (userTypeId>0L && !userTypesService.checkUserTypeId(operation,userTypeId))
			    	{
			    		setFilterUserTypeId(0L);
			    	}
			    	
					// Reload users from DB
					setUsers(null);
				}
				
				// Reload user types from DB
				setUserTypes(null);
			}
		}
	}
	
	/**
	 * Deletes an user type.<br/><br/>
	 * Note that the operation will fail if there is any user with that user type.
	 * @param event Action event
	 */
	/*
	public void deleteUserType(ActionEvent event)
	{
		boolean ok=true;
		
		Operation writeOp=null;
		try
		{
			// Start a new Hibernate operation
			writeOp=HibernateUtil.startOperation();
			
			// Reload user types from DB
			setUserTypes(null);
			
			ok=checkDeleteUserType(writeOp);
			if (ok)
			{
				// Delete user type
				userTypesService.deleteUserType(writeOp,getUserType());
					
				// Reload user types from DB
				setUserTypes(null);
				
				// Do commit
				writeOp.commit();
			}
		}
		catch (ServiceException se)
		{
			if (ok)
			{
				// Do rollback
				writeOp.rollback();
			}
			
			ok=false;
			addErrorMessage(true,"INCORRECT_OPERATION","ROLE_DELETE_CRITICAL_ERROR");
		}
		finally
		{
			// End Hibernate operation
			HibernateUtil.endOperation(writeOp);
			
			if (ok)
			{
				// End current user session Hibernate operation
				userSessionService.endCurrentUserOperation();
			}
		}
	}
	*/
	
	/**
	 * Action listener to cancel confirmation dialog on roles tab.
	 * @param event Action event
	 */
	public void cancelConfirmDeleteUserType(ActionEvent event)
	{
		resetAdmins();
		resetSuperadmins();
		setAdminUsersEnabled(null);
		setAddUsersEnabled(null);
		setEditUsersEnabled(null);
		setEditAdminsEnabled(null);
		setEditSuperadminsEnabled(null);
		resetEditUsersAllowed();
		setDeleteUsersEnabled(null);
		setDeleteAdminsEnabled(null);
		setDeleteSuperadminsEnabled(null);
		resetDeleteUsersAllowed();
		setAddUserTypesEnabled(null);
		resetAdminRoles();
		resetSuperadminRoles();
		setAddUserTypesEnabled(null);
		setEditUserTypesEnabled(null);
		setEditAdminRolesEnabled(null);
		setEditSuperadminRolesEnabled(null);
		resetEditUserTypesAllowed();
		setDeleteUserTypesEnabled(null);
		setDeleteAdminRolesEnabled(null);
		setDeleteSuperadminRolesEnabled(null);
		resetDeleteUserTypesAllowed();
		
		if (!getAdminUserTypesEnabled(getCurrentUserOperation(null)))
		{
	    	long userTypeId=getFilterUserTypeId();
	    	if (userTypeId>0L && !userTypesService.checkUserTypeId(getCurrentUserOperation(null),userTypeId))
	    	{
	    		setFilterUserTypeId(0L);
	    	}
	    	
			// Reload users from DB
			setUsers(null);
		}
		
		// Always reload roles from DB
		setUserTypes(null);
	}
	
	/**
	 * Displays an error message.
	 * @param criticalError Flag to indicate if error is critical (true) or not (false)
	 * @param title Error title (before localization)
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(boolean criticalError,String title,String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		setCriticalErrorMessage(criticalError);
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,
			localizationService.getLocalizedMessage(title),localizationService.getLocalizedMessage(message)));
	}
}
