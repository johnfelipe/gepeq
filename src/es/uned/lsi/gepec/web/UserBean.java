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
import java.util.ArrayList;
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

import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;

import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserPermission;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.UserTypePermission;
import es.uned.lsi.gepec.util.HtmlUtils;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.PermissionBean;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.TestUsersService;
import es.uned.lsi.gepec.web.services.UserPermissionsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypePermissionsService;
import es.uned.lsi.gepec.web.services.UserTypesService;
import es.uned.lsi.gepec.web.services.UsersService;

//Backbean para la vista usuario
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for creating/editing an user.
 */
@SuppressWarnings("serial")
@ManagedBean(name="userBean")
@ViewScoped
public class UserBean implements Serializable
{
	private final static String GENERAL_WIZARD_TAB="general";
	private final static String PERMISSIONS_WIZARD_TAB="permissions";
	private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	private final static int GENERAL_TABVIEW_TAB=0;
	private final static int PERMISSIONS_TABVIEW_TAB=1;
	
	@ManagedProperty(value="#{usersService}")
	private UsersService usersService;
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
	@ManagedProperty(value="#{testUsersService}")
	private TestUsersService testUsersService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	@ManagedProperty(value="#{userPermissionsService}")
	private UserPermissionsService userPermissionsService;
	@ManagedProperty(value="#{userTypePermissionsService}")
	private UserTypePermissionsService userTypePermissionsService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	private User user;							// Current user
	private String confirmPassword;				// Password confirmation (must match with password)
	
	private List<PermissionBean> permissions;
	
	private List<PermissionBean> currentPermissions;
	
	private List<UserType> userTypes;
	
	/* UI Helper Properties */
	private boolean changePassword;
	private boolean displayUserPermissions;
	private String cancelUserTarget;
	
	private boolean enabledCheckboxesSetters;
	
	private String activeUserTabName;
	private int activeUserTabIndex;
	
	private Map<Long,PermissionBean> currentUserPermissions;
	
	public UserBean()
	{
		confirmPassword="";
		changePassword=false;
		displayUserPermissions=false;
		activeUserTabName=GENERAL_WIZARD_TAB;
		activeUserTabIndex=GENERAL_TABVIEW_TAB;
		permissions=null;
		currentPermissions=null;
		currentUserPermissions=null;
		userTypes=null;
	}
	
	public void setUsersService(UsersService usersService)
	{
		this.usersService=usersService;
	}
	
	public void setUserTypesService(UserTypesService userTypesService)
	{
		this.userTypesService=userTypesService;
	}
	
	public void setTestUsersService(TestUsersService testUsersService)
	{
		this.testUsersService=testUsersService;
	}
	
	public void setPermissionsService(PermissionsService permissionsService)
	{
		this.permissionsService=permissionsService;
	}
	
	public void setUserPermissionsService(UserPermissionsService userPermissionsService)
	{
		this.userPermissionsService=userPermissionsService;
	}
	
	public void setUserTypePermissionsService(UserTypePermissionsService userTypePermissionsService)
	{
		this.userTypePermissionsService=userTypePermissionsService;
	}
	
	public void setUserSessionService(UserSessionService userSessionService)
	{
		this.userSessionService=userSessionService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
    private Operation getCurrentUserOperation(Operation operation)
    {
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
    /**
     * Returns a new user if we are creating one or an existing user from DB if we are updating one.<br/><br/>
     * Once question is instantiated (or readed from DB) next calls to this method will return that instance.
     * <br/><br/>
     * If you need to instantiate user again (or read it again from DB) you can set user to null, 
     * but be careful with possible side effects.
     * @return A new user if we are creating one or an existing user from DB if we are editing one
     */
	public User getUser()
	{
		return getUser(null);
	}
	
    /**
     * Returns a new user if we are creating one or an existing user from DB if we are updating one.<br/><br/>
     * Once question is instantiated (or readed from DB) next calls to this method will return that instance.
     * <br/><br/>
     * If you need to instantiate user again (or read it again from DB) you can set user to null, 
     * but be careful with possible side effects.
     * @param operation Operation
     * @return A new user if we are creating one or an existing user from DB if we are editing one
     */
	private User getUser(Operation operation)
	{
		if (user==null)
		{
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
			
    		// Check if we are creating a new user or editing an existing one
    		if (params.containsKey("userId"))
    		{
    			// Get current user session Hibernate operation
    			operation=getCurrentUserOperation(operation);
    				
    			long userId=Long.parseLong(params.get("userId"));
        		user=usersService.getUser(operation,userId);
        		UserType userType=user.getUserType();
        		if (userType!=null)
        		{
        			user.setUserType(userTypesService.getUserType(operation,userType.getId()));
        		}
        		
    			setEnabledCheckboxesSetters(true);
    		}
    		else
    		{
    			user=new User(0L,null,"","","",true);
    			user.setUserPermissions(new ArrayList<UserPermission>());
    			setEnabledCheckboxesSetters(false);
    		}
		}
		return user;
	}
	
	public void setUser(User user)
	{
		this.user=user;
	}
	
	public String getConfirmPassword()
	{
		return confirmPassword;
	}
	
	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword=confirmPassword;
	}
	
	/**
	 * @param operation Operation
	 * @return Current permissions of the user we are creating or editing taking account of selected role but not 
	 * unsaved changes
	 */
	private List<PermissionBean> getCurrentPermissions(Operation operation)
	{
		if (currentPermissions==null)
		{
			currentPermissions=new ArrayList<PermissionBean>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			List<Permission> rawPermissions=permissionsService.getPermissions(operation);
			User user=getUser(operation);
			if (user.getId()==0L)
			{
				if (user.getUserType()==null)
				{
					for (Permission rawPermission:rawPermissions)
					{
						PermissionBean currentPermission=new PermissionBean(
							rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
						currentPermissions.add(currentPermission);
					}
				}
				else
				{
					List<UserTypePermission> userTypePermissions=
						userTypePermissionsService.getUserTypePermissions(operation,user.getUserType());
					for (Permission rawPermission:rawPermissions)
					{
						PermissionBean currentPermission=null;
						UserTypePermission userTypePermission=null;
						for (UserTypePermission userTypePerm:userTypePermissions)
						{
							if (rawPermission.equals(userTypePerm.getPermission()))
							{
								userTypePermission=userTypePerm;
								break;
							}
						}
						if (userTypePermission==null)
						{
							currentPermission=new PermissionBean(
								rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
						}
						else
						{
							currentPermission=new PermissionBean(rawPermission,
								PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
						}
						currentPermissions.add(currentPermission);
					}
				}
			}
			else
			{
				List<UserPermission> userPermissions=userPermissionsService.getUserPermissions(operation,user);
				if (user.getUserType()==null)
				{
					for (Permission rawPermission:rawPermissions)
					{
						PermissionBean currentPermission=null;
						UserPermission userPermission=null;
						for (UserPermission userPerm:userPermissions)
						{
							if (rawPermission.equals(userPerm.getPermission()))
							{
								userPermission=userPerm;
								break;
							}
						}
						if (userPermission==null)
						{
							currentPermission=new PermissionBean(
								rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
						}
						else
						{
							currentPermission=new PermissionBean(
								rawPermission,PermissionBean.USER_PERMISSION_VALUE,userPermission.getValue());
						}
						currentPermissions.add(currentPermission);
					}
				}
				else
				{
					List<UserTypePermission> userTypePermissions=
						userTypePermissionsService.getUserTypePermissions(operation,user.getUserType());
					for (Permission rawPermission:rawPermissions)
					{
						PermissionBean currentPermission=null;
						UserPermission userPermission=null;
						for (UserPermission userPerm:userPermissions)
						{
							if (rawPermission.equals(userPerm.getPermission()))
							{
								userPermission=userPerm;
								break;
							}
						}
						if (userPermission==null)
						{
							UserTypePermission userTypePermission=null;
							for (UserTypePermission userTypePerm:userTypePermissions)
							{
								if (rawPermission.equals(userTypePerm.getPermission()))
								{
									userTypePermission=userTypePerm;
									break;
								}
							}
							if (userTypePermission==null)
							{
								currentPermission=new PermissionBean(
									rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
							}
							else
							{
								currentPermission=new PermissionBean(rawPermission,
									PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
							}
						}
						else
						{
							currentPermission=new PermissionBean(
								rawPermission,PermissionBean.USER_PERMISSION_VALUE,userPermission.getValue());
						}
						currentPermissions.add(currentPermission);
					}
				}
			}
		}
		return currentPermissions;
	}
	
	/**
	 * Set current permissions of the user we are creating or editing taking account selected role but not unsaved
	 * changes.
	 */
	private void setCurrentPermissions(List<PermissionBean> currentPermissions)
	{
		this.currentPermissions=currentPermissions;
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return Current permission of the user we are creating or editing taking account selected role but not unsaved 
	 * changes
	 */
	private PermissionBean getCurrentPermission(Operation operation,Permission permission)
	{
		PermissionBean currentPermission=null;
		for (PermissionBean currentPerm:getCurrentPermissions(getCurrentUserOperation(operation)))
		{
			if (permission.getId()==currentPerm.getPermission().getId())
			{
				currentPermission=currentPerm;
				break;
			}
		}
		return currentPermission;
	}
	
	/**
	 * @param operation Operation
	 * @return true if current user is granted to change permissions even if doing that will result in permissions
	 * greater than their owned ones, false otherwise.
	 */
	private boolean checkRaisePermissionAllowed(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
    	Permission raisePermissionsAllowedRaw=
    		permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
    	resetCurrentUserPermission(raisePermissionsAllowedRaw);
    	PermissionBean raisePermissionsAllowed=getCurrentUserPermission(operation,raisePermissionsAllowedRaw);
    	return "true".equals(raisePermissionsAllowed.getValue());
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return true if the value selected by current user for a permission of boolean type is allowed taking account 
	 * current user permissions, false otherwise
	 */
	private boolean checkPermissionBooleanValueAllowed(Operation operation,PermissionBean permission)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
    	if ("true".equals(permission.getValue()) && !checkRaisePermissionAllowed(operation))
    	{
    		PermissionBean currentPermission=getCurrentPermission(operation,permission.getPermission());
    		if (currentPermission.getValueType()==PermissionBean.DEFAULT_VALUE || 
    			!"true".equals(currentPermission.getValue()))
    		{
    			resetCurrentUserPermissionExceptRaisePermissionsAllowed(permission.getPermission());
    			PermissionBean currentUserPermission=getCurrentUserPermission(operation,permission.getPermission());
    			ok="true".equals(currentUserPermission.getValue());
    		}
    	}
		return ok;
	}
	
	/**
	 * @param permission Permission
	 * @return true if the value selected by current user for a permission of integer type is allowed taking account 
	 * current user permissions, false otherwise
	 */
	private boolean checkPermissionIntValueAllowed(Operation operation,PermissionBean permission)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
    	if (!checkRaisePermissionAllowed(operation))
    	{
    		int permissionIntValue=Integer.parseInt(permission.getConfirmedValue());
    		PermissionBean currentPermission=getCurrentPermission(operation,permission.getPermission());
    		int currentPermissionIntValue=Integer.parseInt(currentPermission.getValue());
    		if (currentPermission.getValueType()==PermissionBean.DEFAULT_VALUE || 
    			(currentPermissionIntValue>0 && 
    			(permissionIntValue<=0 || permissionIntValue>currentPermissionIntValue)))
    		{
    			resetCurrentUserPermission(permission.getPermission());
    			PermissionBean currentUserPermission=getCurrentUserPermission(operation,permission.getPermission());
    			int currentUserPermissionIntValue=Integer.parseInt(currentUserPermission.getValue());
    			ok=currentUserPermissionIntValue<=0 || 
    				(permissionIntValue>0 && permissionIntValue<=currentPermissionIntValue);
    		}
    	}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return true if the value selected by current user for a permission is allowed taking account current 
	 * user permissions, false otherwise
	 */
	private boolean checkPermissionValueAllowed(Operation operation,PermissionBean permission)
	{
		boolean ok=true;
		if ("PERMISSION_TYPE_BOOLEAN".equals(permission.getPermission().getPermissionType().getType()))
		{
			ok=checkPermissionBooleanValueAllowed(getCurrentUserOperation(operation),permission);
		}
		else if ("PERMISSION_TYPE_INT".equals(permission.getPermission().getPermissionType().getType()))
		{
			ok=checkPermissionIntValueAllowed(getCurrentUserOperation(operation),permission);
		}
		// Other permission types allows all values allways
		return ok;
	}
	
	/**
	 * @return Greater allowed value for a limited permission of boolean type as string
	 */
	private String getLimitedPermissionBooleanValue()
	{
		return "false";
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return Greater allowed value for a limited permission of integer type as string
	 */
	private String getLimitedPermissionIntValue(Operation operation,PermissionBean permission)
	{
		String limitedPermissionIntValue=null;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		PermissionBean currentPermission=getCurrentPermission(operation,permission.getPermission());
		if (currentPermission.getValueType()==PermissionBean.DEFAULT_VALUE)
		{
			PermissionBean currentUserPermission=getCurrentUserPermission(operation,permission.getPermission());
			int currentUserPermissionIntValue=Integer.parseInt(currentUserPermission.getValue());
			if (currentUserPermissionIntValue>0)
			{
				limitedPermissionIntValue=currentUserPermission.getValue();
			}
			else
			{
				limitedPermissionIntValue="0";
			}
		}
		else
		{
			int currentPermissionIntValue=Integer.parseInt(currentPermission.getValue());
			if (currentPermissionIntValue>0)
			{
				PermissionBean currentUserPermission=getCurrentUserPermission(operation,permission.getPermission());
				int currentUserPermissionIntValue=Integer.parseInt(currentUserPermission.getValue());
				if (currentUserPermissionIntValue>0)
				{
					if (currentPermissionIntValue>=currentUserPermissionIntValue)
					{
						limitedPermissionIntValue=currentPermission.getValue();
					}
					else
					{
						limitedPermissionIntValue=currentUserPermission.getValue();
					}
				}
				else
				{
					limitedPermissionIntValue="0";
				}
			}
			else
			{
				limitedPermissionIntValue="0";
			}
		}
		return limitedPermissionIntValue;
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return Greater allowed value for a limited permission as string
	 */
	private String getLimitedPermissionValue(Operation operation,PermissionBean permission)
	{
		String limitedPermissionValue=null;
		if ("PERMISSION_TYPE_BOOLEAN".equals(permission.getPermission().getPermissionType().getType()))
		{
			limitedPermissionValue=getLimitedPermissionBooleanValue();
		}
		else if ("PERMISSION_TYPE_INT".equals(permission.getPermission().getPermissionType().getType()))
		{
			limitedPermissionValue=getLimitedPermissionIntValue(getCurrentUserOperation(operation),permission);
		}
		return limitedPermissionValue;
	}
	
	public List<PermissionBean> getPermissions()
	{
		return getPermissions(null);
	}
	
	public void setPermissions(List<PermissionBean> permissions)
	{
		this.permissions=permissions;
	}
	
	private List<PermissionBean> getPermissions(Operation operation)
	{
		if (permissions==null)
		{
			permissions=new ArrayList<PermissionBean>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			List<Permission> rawPermissions=permissionsService.getPermissions(operation);
			User user=getUser(operation);
			List<UserPermission> userPermissions=user.getUserPermissions();
			if (user.getUserType()==null)
			{
				for (Permission rawPermission:rawPermissions)
				{
					PermissionBean permission=null;
					UserPermission userPermission=null;
					for (UserPermission userPerm:userPermissions)
					{
						if (rawPermission.equals(userPerm.getPermission()))
						{
							userPermission=userPerm;
							break;
						}
					}
					if (userPermission==null)
					{
						permission=new PermissionBean(
							rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
					}
					else
					{
						permission=new PermissionBean(
							rawPermission,PermissionBean.USER_PERMISSION_VALUE,userPermission.getValue());
					}
					if (!checkPermissionValueAllowed(operation,permission))
					{
						String limitedPermissionValue=getLimitedPermissionValue(operation,permission);
						permission.setValue(limitedPermissionValue);
						permission.setConfirmedValue(limitedPermissionValue);
						permission.setValueType(PermissionBean.LIMITED_PERMISSION_VALUE);
					}
					permissions.add(permission);
				}
			}
			else
			{
				List<UserTypePermission> userTypePermissions=
					userTypePermissionsService.getUserTypePermissions(operation,user.getUserType());
				for (Permission rawPermission:rawPermissions)
				{
					PermissionBean permission=null;
					UserPermission userPermission=null;
					for (UserPermission userPerm:userPermissions)
					{
						if (rawPermission.equals(userPerm.getPermission()))
						{
							userPermission=userPerm;
							break;
						}
					}
					if (userPermission==null)
					{
						UserTypePermission userTypePermission=null;
						for (UserTypePermission userTypePerm:userTypePermissions)
						{
							if (rawPermission.equals(userTypePerm.getPermission()))
							{
								userTypePermission=userTypePerm;
								break;
							}
						}
						if (userTypePermission==null)
						{
							permission=new PermissionBean(
								rawPermission,PermissionBean.DEFAULT_VALUE,rawPermission.getDefaultValue());
						}
						else
						{
							permission=new PermissionBean(rawPermission,
								PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
						}
					}
					else
					{
						permission=new PermissionBean(
							rawPermission,PermissionBean.USER_PERMISSION_VALUE,userPermission.getValue());
					}
					if (!checkPermissionValueAllowed(operation,permission))
					{
						String limitedPermissionValue=getLimitedPermissionValue(operation,permission);
						permission.setValue(limitedPermissionValue);
						permission.setConfirmedValue(limitedPermissionValue);
						permission.setValueType(PermissionBean.LIMITED_PERMISSION_VALUE);
					}
					permissions.add(permission);
				}
			}
		}
		return permissions;
	}
	
	public long getUserTypeId()
	{
		return getUserTypeId(null);
	}
	
	public void setUserTypeId(long userTypeId)
	{
		setUserTypeId(null,userTypeId);
	}
	
	private long getUserTypeId(Operation operation)
	{
		UserType userType=getUser(getCurrentUserOperation(operation)).getUserType();
		return userType==null?0L:userType.getId();
	}
	
	private void setUserTypeId(Operation operation,long userTypeId)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		getUser(operation).setUserType(userTypeId>0L?userTypesService.getUserType(operation,userTypeId):null);
	}
	
	public String getUserTypeDescription()
	{
		return getUserTypeDescription(null);
	}
	
	private String getUserTypeDescription(Operation operation)
	{
		UserType userType=getUser(getCurrentUserOperation(operation)).getUserType();
		return userType==null?"":userType.getDescription();
	}
	
	public boolean isChangePassword()
	{
		return changePassword;
	}
	
	public void setChangePassword(boolean changePassword)
	{
		if (isEnabledChecboxesSetters())
		{
			this.changePassword=changePassword;
		}
	}
	
	public boolean isDisplayUserPermissions()
	{
		return displayUserPermissions;
	}
	
	public void setDisplayUserPermissions(boolean displayUserPermissions)
	{
		if (isEnabledChecboxesSetters())
		{
			this.displayUserPermissions=displayUserPermissions;
		}
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
	
	public String getCancelUserTarget()
	{
		return cancelUserTarget;
	}
	
	public void setCancelUserTarget(String cancelUserTarget)
	{
		this.cancelUserTarget=cancelUserTarget;
	}
	
	/**
	 * @return Map with permissions of current user
	 */
	private Map<Long,PermissionBean> getCurrentUserPermissions()
	{
		if (currentUserPermissions==null)
		{
			currentUserPermissions=new HashMap<Long,PermissionBean>();
		}
		return currentUserPermissions;
	}
	
	/**
	 * Reset map with permissions of current user.
	 */
	private void resetCurrentUserPermissions()
	{
		currentUserPermissions=null;
	}
	
	/**
	 * @param permission Permission
	 * @return Permission of current user
	 */
	private PermissionBean getCurrentUserPermission(Operation operation,Permission permission)
	{
		PermissionBean currentUserPermission=null;
		if (permission!=null)
		{
			Map<Long,PermissionBean> currentUserPermissions=getCurrentUserPermissions();
			if (currentUserPermissions.containsKey(Long.valueOf(permission.getId())))
			{
				currentUserPermission=currentUserPermissions.get(Long.valueOf(permission.getId()));
			}
			else
			{
				// Get current user Hibernate operation
				operation=getCurrentUserOperation(operation);
				
				User currentUser=userSessionService.getCurrentUser(operation);
				UserPermission userPermission=
					userPermissionsService.getUserPermission(operation,currentUser,permission);
				if (userPermission==null)
				{
					UserTypePermission userTypePermission=null;
					UserType currentUserType=currentUser.getUserType();
					if (currentUserType!=null)
					{
						userTypePermission=
							userTypePermissionsService.getUserTypePermission(operation,currentUserType,permission);
					}
					if (userTypePermission==null)
					{
						currentUserPermission=new PermissionBean(
							permission,PermissionBean.DEFAULT_VALUE,permission.getDefaultValue());
					}
					else
					{
						currentUserPermission=new PermissionBean(
							permission,PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
					}
				}
				else
				{
					currentUserPermission=new PermissionBean(
						permission,PermissionBean.USER_PERMISSION_VALUE,userPermission.getValue());
				}
				if (currentUserPermission!=null)
				{
					currentUserPermissions.put(Long.valueOf(permission.getId()),currentUserPermission);
				}
			}
		}
		return currentUserPermission;
	}
	
	/**
	 * Reset a permission of current user-
	 * @param permission Permission
	 */
	private void resetCurrentUserPermission(Permission permission)
	{
		Map<Long,PermissionBean> currentUserPermissions=getCurrentUserPermissions();
		if (permission!=null && currentUserPermissions.containsKey(Long.valueOf(permission.getId())))
		{
			currentUserPermissions.remove(Long.valueOf(permission.getId()));
		}
	}
	
	/**
	 * Reset a permission of current user, except if it is the permission to allow change permissions of other users
	 * to values greater than their owned ones.
	 * @param permission Permission
	 */
	private void resetCurrentUserPermissionExceptRaisePermissionsAllowed(Permission permission)
	{
		if (!permission.getName().equals("PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED"))
		{
			resetCurrentUserPermission(permission);
		}
	}
	/**
	 * @param permission Permission of boolean type
	 * @return List of allowed values for a permission of boolean type
	 */
	public List<String> getBooleanPermissionValues(Permission permission)
	{
		return getBooleanPermissionValues(null,permission);
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission of boolean type
	 * @return List of allowed values for a permission of boolean type
	 */
	private List<String> getBooleanPermissionValues(Operation operation,Permission permission)
	{
		List<String> booleanPermissionValues=new ArrayList<String>();
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		PermissionBean raisePermissionsAllowed=getCurrentUserPermission(operation,permissionsService.getPermission(
			operation,"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED"));
		if ("true".equals(raisePermissionsAllowed.getValue()))
		{
			booleanPermissionValues.add("true");
		}
		else
		{
			PermissionBean currentPermission=getCurrentPermission(operation,permission);
			if ("true".equals(currentPermission.getValue()))
			{
				booleanPermissionValues.add("true");
			}
			else
			{
				PermissionBean currentUserPermission=getCurrentUserPermission(operation,permission);
				if ("true".equals(currentUserPermission.getValue()))
				{
					booleanPermissionValues.add("true");
				}
			}
		}
		booleanPermissionValues.add("false");
		return booleanPermissionValues;
	}
	
	/**
	 * @param booleanPermissionValue Value of a permission of boolean type
	 * @return Localized value of a permission of boolean type
	 */
	public String getLocalizedBooleanPermissionValue(String booleanPermissionValue)
	{
		String localizedBooleanPermissionValue=null;
		if ("true".equals(booleanPermissionValue))
		{
			localizedBooleanPermissionValue=localizationService.getLocalizedMessage("YES");
		}
		else
		{
			localizedBooleanPermissionValue=localizationService.getLocalizedMessage("NO");
		}
		return localizedBooleanPermissionValue;
	}
	
	/**
	 * Checks that current user has enough permissions to assign indicated user type (a.k.a. role) to an user.
	 * @param operation Operation
	 * @param userType User type
	 * @return true if current user has enough permissions to assign indicated user type to an user, 
	 * false otherwise 
	 */
	private boolean checkUserTypeAllowed(Operation operation,UserType userType)
	{
		boolean ok=true;
		if (userType!=null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			// We use an UserTypeBean instance to perform the check
			UserTypeBean userTypeBean=new UserTypeBean();
			
			// We need to initialize services of the UserTypeBean instance
			userTypeBean.setUserTypesService(userTypesService);
			userTypeBean.setPermissionsService(permissionsService);
			userTypeBean.setUserPermissionsService(userPermissionsService);
			userTypeBean.setUserTypePermissionsService(userTypePermissionsService);
			userTypeBean.setUserSessionService(userSessionService);
			userTypeBean.setLocalizationService(localizationService);
			
			// We need to get the user type from DB
			UserType userTypeFromDB=userTypesService.getUserType(operation,userType.getId());
			
			// We set user type of the UserTypeBean instance with the user type (a.k.a. role) we want to check
			userTypeBean.setUserType(userTypeFromDB);
			
			// We need to initialize permissions of the UserTypeBean instance, so we call its getter
			userTypeBean.getPermissions(operation);
			
			// We want to compare the permissions of the user type with the current permissions of the user we are 
			// creating/editing so we set them in the UserTypeBean instance
			userTypeBean.setCurrentPermissions(getCurrentPermissions(operation));
			
			// Finally we check permissions with the checkPermissionsValuesAllowed method of the UserTypeBean instance
			ok=userTypeBean.checkPermissionsValuesAllowed(operation,false);
		}
		return ok;
	}
	
	// IMPLEMENTACION ANTIGUA usando UserBean en vez de UserTypeBean... 
	// funciona pero es mas compleja y posiblemente mas lenta...
	// la dejo de momento comentada por si acaso y por consulta
	/*
	private boolean checkUserTypeAllowed(UserType userType)
	{
		// We create a fake user
		User fakeUser=new User(0L,null,"","","");
		
		// We need to initialize user permissions of the fake user (initially empty)
		List<UserPermission> fakeUserUserPermissions=new ArrayList<UserPermission>();
		fakeUser.setUserPermissions(fakeUserUserPermissions);
		
		// We indicate that the user type of the fake user will be the user type we want to check
		// (this is temporary)
		fakeUser.setUserType(userType);
		
		// We instantiate and initialize a new user bean with the fake user.
		// Note that we also need to initialize its services.
		UserBean fakeUserBean=new UserBean();
		fakeUserBean.setUsersService(usersService);
		fakeUserBean.setUserTypesService(userTypesService);
		fakeUserBean.setPermissionsService(permissionsService);
		fakeUserBean.setUserPermissionsService(userPermissionsService);
		fakeUserBean.setUserTypePermissionsService(userTypePermissionsService);
		fakeUserBean.setUserSessionService(userSessionService);
		fakeUserBean.setLocalizationService(localizationService);
		fakeUserBean.setUser(fakeUser);
		
		// Now we get permissions beans of the fake user
		List<PermissionBean> fakeUserPermissions=fakeUserBean.getPermissions();
		
		// We want to assign to the fake user the same permissions that the user type to check.
		// To do it we iterate all its permissions beans and when we match an user type permission we add it
		// as an user permission of the fake user and change value type of the permission bean
		for (PermissionBean fakeUserPermission:fakeUserPermissions)
		{
			if (fakeUserPermission.getValueType()==PermissionBean.USER_TYPE_PERMISSION_VALUE)
			{
				UserPermission fakeUserUserPermission=new UserPermission(
					0L,fakeUser,fakeUserPermission.getPermission(),fakeUserPermission.getValue());
				fakeUserUserPermissions.add(fakeUserUserPermission);
				fakeUserPermission.setValueType(PermissionBean.USER_PERMISSION_VALUE);
			}
		}
		
		// Now that the fake user has the same permissions than the user type to check we remove that user type
		// from the user bean
		fakeUserBean.getUser().setUserType(null);
		
		// We need to reset current permissions to default values before checking fake user permissions
		fakeUserBean.setCurrentPermissions(null);
		
		// Finally we check that permissions values of the fake user are allowed, as the fake user has the same
		// permissions of the user type to check, it will be as if we are checking the user type
		return fakeUserBean.checkPermissionsValuesAllowed(false);
	}
	*/
	
	/**
	 * @return List of identifiers of user types
	 */
	public List<Long> getUserTypesIds()
	{
		return getUserTypesIds(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of identifiers of user types
	 */
	private List<Long> getUserTypesIds(Operation operation)
	{
		List<Long> userTypesIds=new ArrayList<Long>();
		for (UserType userType:getUserTypes(getCurrentUserOperation(operation)))
		{
			userTypesIds.add(Long.valueOf(userType.getId()));
		}
		return userTypesIds;
	}
	
    /**
	 * @return List of user types
	 */
	public List<UserType> getUserTypes()
	{
		return getUserTypes(null);
	}
	
	public void setUserTypes(List<UserType> userTypes)
	{
		this.userTypes=userTypes;
	}
	
    /**
	 * @param operation Operation
	 * @return List of user types
	 */
	private List<UserType> getUserTypes(Operation operation)
	{
		if (userTypes==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			userTypes=userTypesService.getUserTypes(operation);
			
			List<UserType> notAllowedUserTypes=new ArrayList<UserType>();
			for (UserType userType:userTypes)
			{
				if (!checkUserTypeAllowed(operation,userType))
				{
					notAllowedUserTypes.add(userType);
				}
			}
			for (UserType notAllowedUserType:notAllowedUserTypes)
			{
				userTypes.remove(userTypes.indexOf(notAllowedUserType));
			}
		}
		return userTypes;
	}
	
	/**
	 * @return Localized user's application
	 */
	public String getUserApplication()
	{
		return getUserApplication(null);
	}
	
	/**
	 * @param operation
	 * @return Localized user's application
	 */
	private String getUserApplication(Operation operation)
	{
		return localizationService.getLocalizedMessage(
			getUser(getCurrentUserOperation(operation)).isGepeqUser()?"APPLICATION_GEPEQ":"APPLICATION_OM");
	}
	
	/**
	 * @param permission Permission
	 * @return Localized permission value type
	 */
	public String getLocalizedPermissionValueType(PermissionBean permission)
	{
		return localizationService.getLocalizedMessage(permission.getValueTypeStr());
	}
	
	/**
	 * @param permission Permission
	 * @return Localized permission value
	 */
	public String getLocalizedPermissionValue(PermissionBean permission)
	{
		String localizedPermissionValue=null;
		String permissionType=permission.getPermission().getPermissionType().getType();
		if ("PERMISSION_TYPE_BOOLEAN".equals(permissionType))
		{
			if (Boolean.valueOf(permission.getValue()))
			{
				localizedPermissionValue=localizationService.getLocalizedMessage("YES");
			}
			else
			{
				localizedPermissionValue=localizationService.getLocalizedMessage("NO");
			}
		}
		else
		{
			localizedPermissionValue=permission.getValue();
		}
		return localizedPermissionValue;
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
	
	/**
	 * Action listener to show the dialog to confirm cancel of user creation/edition.
	 * @param event Action event
	 */
	public void showConfirmCancelUserDialog(ActionEvent event)
	{
		setCancelUserTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelUserDialog.show()");
	}
	
	/**
	 * Cancel user creation/editon and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelUser()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelUserTarget()))
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
		else if (getCancelUserTarget()!=null)
		{
			nextView=new StringBuffer(getCancelUserTarget());
			nextView.append("?faces-redirect=true");
		}
		return nextView==null?null:nextView.toString();
	}
	
	/**
	 * @param operation Operation
	 * Process general tab input fields.
	 */
	private void processGeneralInputFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String userLoginId=null;
		String userPasswordId=null;
		String confirmPasswordId=null;
		String userNickId=null;
		String userNameId=null;
		String userSurnameId=null;
		
		User user=getUser(getCurrentUserOperation(operation));
		if (user.getId()==0L)
		{
			userLoginId="userForm:userLogin";
			userPasswordId=":userForm:userPassword";
			confirmPasswordId=":userForm:confirmPassword";
			userNickId=":userForm:userNick";
			userNameId=":userForm:userName";
			userSurnameId=":userForm:userSurname";
		}
		else
		{
			userLoginId="userForm:userFormTabs:userLogin";
			userPasswordId=":userForm:userFormTabs:userPassword";
			confirmPasswordId=":userForm:userFormTabs:confirmPassword";
			userNickId=":userForm:userFormTabs:userNick";
			userNameId=":userForm:userFormTabs:userName";
			userSurnameId=":userForm:userFormTabs:userSurname";
		}
		
		// We only need to process login if we are creating a new user
		if (user.getId()==0L)
		{
			UIInput userLogin=(UIInput)component.findComponent(userLoginId);
			userLogin.processDecodes(context);
			if (userLogin.getSubmittedValue()!=null)
			{
				user.setLogin((String)userLogin.getSubmittedValue());
			}
		}
		// We only need to process password and confirm password if we are creating a new user or change password 
		// checkbox is checked
		if (user.getId()==0L || isChangePassword())
		{
			UIInput userPassword=(UIInput)component.findComponent(userPasswordId);
			userPassword.processDecodes(context);
			if (userPassword.getSubmittedValue()!=null)
			{
				user.setPassword((String)userPassword.getSubmittedValue());
			}
			UIInput confirmPassword=(UIInput)component.findComponent(confirmPasswordId);
			confirmPassword.processDecodes(context);
			if (confirmPassword.getSubmittedValue()!=null)
			{
				setConfirmPassword((String)confirmPassword.getSubmittedValue());
			}
		}
		// We always need to process nick, name and surname
		UIInput userNick=(UIInput)component.findComponent(userNickId);
		userNick.processDecodes(context);
		if (userNick.getSubmittedValue()!=null)
		{
			user.setNick((String)userNick.getSubmittedValue());
		}
		UIInput userName=(UIInput)component.findComponent(userNameId);
		userName.processDecodes(context);
		if (userName.getSubmittedValue()!=null)
		{
			user.setName((String)userName.getSubmittedValue());
		}
		UIInput userSurname=(UIInput)component.findComponent(userSurnameId);
		userSurname.processDecodes(context);
		if (userSurname.getSubmittedValue()!=null)
		{
			user.setSurname((String)userSurname.getSubmittedValue());
		}
	}
	
	/**
	 * Ajax listener to handle the checkbox used to indicate if we are changing user password or not.
	 * @param event Ajax event
	 */
	public void changePassword(AjaxBehaviorEvent event)
	{
		// We reset password and confir password fields
		getUser(getCurrentUserOperation(null)).setPassword("");
		setConfirmPassword("");
	}
	
	/**
	 * Ajax listener to handle the combo that indicates user's application.
	 * @param event Ajax event
	 */
	public void changeApplication(AjaxBehaviorEvent event)
	{
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We need to process some input fields
		processGeneralInputFields(operation,event.getComponent());
		
		getUser(operation).setUserType(null);
		setDisplayUserPermissions(false);
		
		//TODO ¿Realmente hace falta? Al cambiar a la página de permisos siempre vamos a tener que volver a leerlos de BD por si otro administrador los hubiera modificado, así que no tengo muy claro que sentido tiene hacerlo aquí también
		setPermissions(null);
	}
	
	//TODO ¿Realmente hace falta? Al cambiar a la página de permisos siempre vamos a tener que volver a leerlos de BD por si otro administrador los hubiera modificado, así que no tengo muy claro que sentido tiene hacerlo aquí también
	/**
	 * Ajax listener to handle a change of user's role.
	 * @param event Ajax event
	 */
	public void changeRole(AjaxBehaviorEvent event)
	{
		// Reload permissions from DB
		setPermissions(null);
	}
	
	/**
	 * Ajax listener to handle a change event in the checkbox for displaying/hiding user permissions advanced 
	 * configuration.
	 * @param event Ajax event
	 */
	public void changeDisplayUserPermissions(AjaxBehaviorEvent event)
	{
		// Check if we need to process the checkbox for displaying/hiding user permissions advanced
		UIInput displayUserPermissions=(UIInput)event.getComponent().findComponent(":userForm:displayUserPermissions");
		if (displayUserPermissions.getSubmittedValue()==null)
		{
			// Process the checkbox for displaying/hiding user permissions advanced (listener will be invoked again)
			displayUserPermissions.processDecodes(FacesContext.getCurrentInstance());
		}
		else
		{
			// We need to process some input fields
			processGeneralInputFields(getCurrentUserOperation(null),event.getComponent());
			
			// Set submitted value for the checkbox for displaying/hiding user permissions advanced
			setEnabledCheckboxesSetters(true);
			setDisplayUserPermissions(Boolean.valueOf((String)displayUserPermissions.getSubmittedValue()));
			
			// We enable/disable checkboxes setters as needed
			setEnabledCheckboxesSetters(isDisplayUserPermissions());
		}
	}
	
    /**
     * Checks that user has entered a login or displays an error message.
     * @param userLogin Login
     * @return true if user has entered a login, false otherwise
     */
    private boolean checkNonEmptyUserLogin(String userLogin)
    {
    	boolean ok=true;
    	if (userLogin==null || userLogin.equals(""))
    	{
    		addErrorMessage("USER_LOGIN_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
	/**
	 * Checks that login entered by user only includes valid characters or displays an error message.
     * @param userLogin Login
	 * @return true if login only includes valid characters (letters, digits, whitespaces or some of the 
	 * following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForUserLogin(String userLogin)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
        	userLogin,true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'}))
    	{
    		addErrorMessage("USER_LOGIN_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that the first character of the login entered by user is a letter.
     * @param userLogin Login
     * @return true if the first character of the login entered by user is a letter, false otherwise
     */
    private boolean checkFirstCharacterIsALetterForUserLogin(String userLogin)
    {
    	boolean ok=true;
    	if (!Character.isLetter(userLogin.charAt(0)))
    	{
    		addErrorMessage("USER_LOGIN_FIRST_CHARACTACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the login entered by user is not a whitespace or displays an error message.
     * @param userLogin Login
	 * @return true if last character of the login entered by user is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForUserLogin(String userLogin)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(userLogin))
    	{
    		addErrorMessage("USER_LOGIN_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that login entered by user does not include consecutive whitespaces or displays an error message.
     * @param userLogin Login
	 * @return true if the login entered by user does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForUserLogin(String userLogin)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(userLogin))
    	{
    		addErrorMessage("USER_LOGIN_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
	
    /**
     * Check that login entered by user has not been used yet for other user.
     * @param operation Operation
     * @param userLogin Login
     * @return true if login entered by user has not been used yet for other user, false otherwise
     */
	private boolean checkUnusedForUserLogin(Operation operation,String userLogin)
	{
    	boolean ok=true;
    	if (usersService.checkUser(getCurrentUserOperation(operation),userLogin))
		{
			addErrorMessage("USER_LOGIN_ALREADY_DECLARED");
			ok=false;
		}
    	return ok;
	}
    
    /**
     * Check that login entered by user is valid or displays error messages indicating the causes.
     * @param operation Operation
     * @param userLogin User login
     * @return true if login entered by user is valid, false otherwise
     */
    private boolean checkUserLogin(Operation operation,String userLogin)
    {
    	boolean ok=checkNonEmptyUserLogin(userLogin);
    	if (ok)
    	{
    		if (!checkValidCharactersForUserLogin(userLogin))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterIsALetterForUserLogin(userLogin))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForUserLogin(userLogin))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForUserLogin(userLogin))
    		{
    			ok=false;
    		}
    		if (ok)
    		{
    			ok=checkUnusedForUserLogin(getCurrentUserOperation(operation),userLogin);
    		}
    	}
    	return ok;
    }
	
    
    /**
     * Checks that user has entered a password or displays an error message.
     * @param userPassword password
     * @return true if user has entered a password, false otherwise
     */
    private boolean checkNonEmptyUserPassword(String userPassword)
    {
    	boolean ok=true;
    	if (userPassword==null || userPassword.equals(""))
    	{
    		addErrorMessage("USER_PASSWORD_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that passwords entered by user (user password and its confirmation) are the same 
     * or displays an error message.
     * @param userPassword User password
     * @param confirmPassword Confirmation password (must be the same that the user password)
     * @return true if password entered by user is valid, false otherwise
     */
    private boolean checkConfirmationForUserPassword(String userPassword,String confirmPassword)
    {
    	boolean ok=true;
    	if (!userPassword.equals(confirmPassword))
    	{
    		addErrorMessage("USER_PASSWORD_CONFIRMATION_ERROR");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that password entered by user is valid or displays error messages indicating the causes.
     * @param userPassword User password
     * @param confirmPassword Confirmation password (must be the same that the user password)
     * @return true if password entered by user is valid, false otherwise
     */
    private boolean checkUserPassword(String userPassword,String confirmPassword)
    {
    	return checkNonEmptyUserPassword(userPassword) && 
    		checkConfirmationForUserPassword(userPassword,confirmPassword);
    }
    
    /**
     * Checks that user has entered a nickname or displays an error message.
     * @param userNick Nickname
     * @return true if user has entered a nickname, false otherwise
     */
    private boolean checkNonEmptyUserNick(String userNick)
    {
    	boolean ok=true;
    	if (userNick==null || userNick.equals(""))
    	{
    		addErrorMessage("USER_LOGIN_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
	/**
	 * Checks that nickname entered by user only includes valid characters or displays an error message.
     * @param userNick Nickname
	 * @return true if nickname only includes valid characters (letters, digits, whitespaces or some of the 
	 * following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForUserNick(String userNick)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
        	userNick,true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'}))
    	{
    		addErrorMessage("USER_NICK_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that nickname entered by user includes at least one letter or displays an error message.
     * @param userNick Nickname
     * @return true if nickname includes at least one letter, false otherwise
     */
    private boolean checkLetterIncludedForUserNick(String userNick)
    {
    	boolean ok=true;
    	if (!StringUtils.hasLetter(userNick))
    	{
    		addErrorMessage("USER_NICK_WITHOUT_LETTER");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the nickname entered by user is not a digit nor a whitespace 
     * or displays an error message.
     * @param userNick Nickname
	 * @return true if first character of nickname is not a digit nor a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotDigitNotWhitespaceForUserNick(String userNick)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterDigit(userNick) || StringUtils.isFirstCharacterWhitespace(userNick))
    	{
    		addErrorMessage("USER_NICK_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the nickname entered by user is not a whitespace or displays an error message.
     * @param userNick Nickname
	 * @return true if last character of the nickname entered by user is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForUserNick(String userNick)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(userNick))
    	{
    		addErrorMessage("USER_NICK_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that nickname entered by user does not include consecutive whitespaces or displays an error message.
     * @param userNick Nickname
	 * @return true if the nickname entered by user does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForUserNick(String userNick)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(userNick))
    	{
    		addErrorMessage("USER_NICK_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that nickname entered by user is valid or displays error messages indicating the causes.
     * @param userNick Nickname
     * @return true if nickname entered by user is valid, false otherwise
     */
    private boolean checkUserNick(String userNick)
    {
    	boolean ok=checkNonEmptyUserNick(userNick);
    	if (ok)
    	{
    		if (!checkValidCharactersForUserNick(userNick))
    		{
    			ok=false;
    		}
    		if (!checkLetterIncludedForUserNick(userNick))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterNotDigitNotWhitespaceForUserNick(userNick))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForUserNick(userNick))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForUserNick(userNick))
    		{
    			ok=false;
    		}
    	}
    	return ok;
    }
    
	/**
	 * Checks that name entered by user only includes valid characters or displays an error message.
     * @param userName Name
	 * @return true if name entered by user only includes valid characters (letters or whitespaces), 
	 * false otherwise
	 */
    private boolean checkValidCharactersForUserName(String userName)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(userName,true,false,true,null))
    	{
    		addErrorMessage("USER_NAME_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the name entered by user is not a whitespace or displays an error message.
     * @param userName Name
	 * @return true if first character of name is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForUserName(String userName)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(userName))
    	{
    		addErrorMessage("USER_NAME_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the name entered by user is not a whitespace or displays an error message.
     * @param userName Name
	 * @return true if last character of the name entered by user is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForUserName(String userName)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(userName))
    	{
    		addErrorMessage("USER_NAME_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that name entered by user does not include consecutive whitespaces or displays an error message.
     * @param userName Name
	 * @return true if the name entered by user does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForUserName(String userName)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(userName))
    	{
    		addErrorMessage("USER_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that name entered by user is valid or displays error messages indicating the causes.
     * @param userName Name
     * @return true if name entered by user is valid, false otherwise
     */
    private boolean checkUserName(String userName)
    {
    	boolean ok=true;
    	if (userName!=null && !"".equals(userName))
    	{
    		if (!checkValidCharactersForUserName(userName))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterNotWhitespaceForUserName(userName))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForUserName(userName))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForUserName(userName))
    		{
    			ok=false;
    		}
    	}
    	return ok;
    }
    
	/**
	 * Checks that surname entered by user only includes valid characters or displays an error message.
     * @param userSurname Surname
	 * @return true if surname entered by user only includes valid characters (letters or whitespaces), 
	 * false otherwise
	 */
    private boolean checkValidCharactersForUserSurname(String userSurname)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(userSurname,true,false,true,null))
    	{
    		addErrorMessage("USER_SURNAME_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that first character of the surname entered by user is not a whitespace or displays an error message.
     * @param userSurname Surname
	 * @return true if first character of surname is not a whitespace, false otherwise
     */
    private boolean checkFirstCharacterNotWhitespaceForUserSurname(String userSurname)
    {
    	boolean ok=true;
    	if (StringUtils.isFirstCharacterWhitespace(userSurname))
    	{
    		addErrorMessage("USER_SURNAME_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the surname entered by user is not a whitespace or displays an error message.
     * @param userSurname Surname
	 * @return true if last character of the surname entered by user is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForUserSurname(String userSurname)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(userSurname))
    	{
    		addErrorMessage("USER_SURNAME_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that surname entered by user does not include consecutive whitespaces or displays an error message.
     * @param userSurname Surname
	 * @return true if the surname entered by user does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForUserSurname(String userSurname)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(userSurname))
    	{
    		addErrorMessage("USER_SURNAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that surname entered by user is valid or displays error messages indicating the causes.
     * @param userSurname Surname
     * @return true if surname entered by user is valid, false otherwise
     */
    private boolean checkUserSurname(String userSurname)
    {
    	boolean ok=true;
    	if (userSurname!=null && !"".equals(userSurname))
    	{
    		if (!checkValidCharactersForUserSurname(userSurname))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterNotWhitespaceForUserSurname(userSurname))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForUserSurname(userSurname))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForUserSurname(userSurname))
    		{
    			ok=false;
    		}
    	}
    	return ok;
    }
    
	/**
	 * Check general input fields and display error messages if needed.
	 * @param operation Operation
	 * @return true if all checks are passed correctly, false if any check fails
	 */
	private boolean checkGeneralInputFields(Operation operation,UIComponent component)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		// Note that it is not possible to modify login so we only need to check it when creating a new user
		User user=getUser(operation);
		if (user.getId()==0L)
		{
			if (!checkUserLogin(operation,user.getLogin()))
			{
				ok=false;
			}
			
			// We need to check password if we are creating a new user or if the "Change password" checkbox is checked
			if (isChangePassword() && !checkUserPassword(user.getPassword(),getConfirmPassword()))
			{
				// If password checking fails we reset "Password" and "Confirm password" fields 
	    		user.setPassword("");
	    		setConfirmPassword("");
	    		updatePasswordsTextFields(operation,component);
				ok=false;
			}
		}
		
		// We always need to check nick, name and surname
		if (!checkUserNick(user.getNick()))
		{
			ok=false;
		}
		if (!checkUserName(user.getName()))
		{
			ok=false;
		}
		if (!checkUserSurname(user.getSurname()))
		{
			ok=false;
		}
		return ok;
	}
	
	/**
	 * Update password text fields.<br/><br/>
	 * This is needed after some operations because password text fields are not always being updated correctly 
	 * on page view.
	 * @param operation Operation
	 * @param component Component that triggered the listener that called this method
	 */
	private void updatePasswordsTextFields(Operation operation,UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String userPasswordId=null;
		String confirmPasswordId=null;
		User user=getUser(getCurrentUserOperation(operation));
		if (user.getId()>0L)
		{
			userPasswordId=":userForm:userFormTabs:userPassword";
			confirmPasswordId=":userForm:userFormTabs:confirmPassword";
		}
		else
		{
			userPasswordId=":userForm:userPassword";
			confirmPasswordId=":userForm:confirmPassword";
		}
		UIInput userPassword=(UIInput)component.findComponent(userPasswordId);
		userPassword.pushComponentToEL(context,null);
		userPassword.setSubmittedValue(user.getPassword());
		userPassword.popComponentFromEL(context);
		UIInput confirmPassword=(UIInput)component.findComponent(confirmPasswordId);
		confirmPassword.pushComponentToEL(context,null);
		confirmPassword.setSubmittedValue(getConfirmPassword());
		confirmPassword.popComponentFromEL(context);
	}
	
    /**
     * Flow listener to handle a step change within wizard component. 
     * @param event Flow event
     * @return Next step name
     */
    public String changeStep(FlowEvent event)
    {
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	List<PermissionBean> currentPermissions=null;
    	String oldStep=event.getOldStep();
    	String newStep=event.getNewStep();
    	if (checkAdministrationPermissions(operation))
    	{
        	if (GENERAL_WIZARD_TAB.equals(oldStep))
        	{
        		setCurrentPermissions(null);
        		currentPermissions=getCurrentPermissions(operation);
        		
        		if (!checkGeneralInputFields(operation,event.getComponent()))
        		{
        			newStep=oldStep;
        			ok=false;
        		}
        		
        		User user=getUser(operation);
        		if (!checkUserTypeAllowed(operation,user.getUserType()))
        		{
        			user.setUserType(null);
        			addErrorMessage("USER_ROLE_RAISE_PERMISSIONS_ERROR");
        			newStep=oldStep;
        			ok=false;
        		}
        	}
        	else if (PERMISSIONS_WIZARD_TAB.equals(oldStep))
        	{
        		if (GENERAL_WIZARD_TAB.equals(newStep))
        		{
        			rollbackPermissionsConfirmedValues(operation);
        		}
        		else if (!checkPermissionsConfirmedValues(operation))
        		{
       				rollbackPermissionsConfirmedValues(operation);
       				newStep=oldStep;
       				ok=false;
        		}
        	}
        	setEnabledCheckboxesSetters(!CONFIRMATION_WIZARD_TAB.equals(newStep));
        	if (GENERAL_WIZARD_TAB.equals(newStep))
        	{
    			setUserTypes(null);
        	}
        	else if (PERMISSIONS_WIZARD_TAB.equals(newStep))
        	{
        		setPermissions(null);
        		setCurrentPermissions(currentPermissions);
        		resetCurrentUserPermissions();
        	}
    	}
    	else
    	{
    		addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
    		newStep=oldStep;
    		ok=false;
    	}
    	if (!ok)
    	{
			// Scroll page to top position
			scrollToTop();
    	}
    	activeUserTabName=newStep;
    	return newStep;
    }
	
    public String getActiveUserTabName()
    {
    	return getActiveUserTabName(null);
    }
    
    private String getActiveUserTabName(Operation operation)
    {
    	String activeUserTabName=null;
    	if (getUser(getCurrentUserOperation(operation)).getId()>0L)
    	{
    		switch (activeUserTabIndex)
    		{
    			case GENERAL_TABVIEW_TAB:
    				activeUserTabName=GENERAL_WIZARD_TAB;
    				break;
    			case PERMISSIONS_TABVIEW_TAB:
    				activeUserTabName=PERMISSIONS_WIZARD_TAB;
    		}
    	}
    	else
    	{
    		activeUserTabName=this.activeUserTabName;
    	}
    	return activeUserTabName;
    }
    
	/**
	 * Tab change listener for displaying other tab of an user.
	 * @param event Tab change event
	 */
    public void changeActiveUserTab(TabChangeEvent event)
    {
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	TabView userFormTab=(TabView)event.getComponent();
    	List<PermissionBean> currentPermissions=null;
    	if (checkAdministrationPermissions(operation))
    	{
        	if (activeUserTabIndex==GENERAL_TABVIEW_TAB)
        	{
        		// We need to process some input fields
        		processGeneralInputFields(operation,userFormTab);
        		
        		setCurrentPermissions(null);
        		currentPermissions=getCurrentPermissions(operation);
        		
        		if (!checkGeneralInputFields(operation,event.getComponent()))
        		{
        			ok=false;
        		}
        		User user=getUser(operation);
        		if (!checkUserTypeAllowed(operation,user.getUserType()))
        		{
        			user.setUserType(null);
        			addErrorMessage("USER_ROLE_RAISE_PERMISSIONS_ERROR");
        			ok=false;
        		}
        	}
        	else if (activeUserTabIndex==PERMISSIONS_TABVIEW_TAB)
        	{
    			if (!checkPermissionsConfirmedValues(operation))
    			{
    				rollbackPermissionsConfirmedValues(operation);
    				ok=false;
    			}
        	}
    	}
    	else
    	{
			addErrorMessage("NON_AUTHORIZED_ACTION_ERROR");
			ok=false;
    	}
		if (ok)
		{
			activeUserTabIndex=userFormTab.getActiveIndex();
		}
		else
		{
	    	userFormTab.setActiveIndex(activeUserTabIndex);
	    	
			// Scroll page to top position
			scrollToTop();
		}
    	if (activeUserTabIndex==GENERAL_TABVIEW_TAB)
    	{
			setUserTypes(null);
    	}
    	else if (activeUserTabIndex==PERMISSIONS_TABVIEW_TAB)
    	{
    		setPermissions(currentPermissions);
    		resetCurrentUserPermissions();
    	}
    }
    
	/**
	 * @param userTypeId User type's identifier
	 * @return User type as a string
	 */
    public String getUserTypeString(long userTypeId)
    {
    	return getUserTypeString(null,userTypeId);
    }
    
	/**
	 * @param operation Operation
	 * @param userTypeId User type's identifier
	 * @return User type as a string
	 */
    private String getUserTypeString(Operation operation,long userTypeId)
    {
    	return userTypesService.getUserTypeString(getCurrentUserOperation(operation),userTypeId);
    }
    
    /**
     * Checks if all permission values are unchanged or if its changes has been confirmed, displaying an error message
     * otherwise.
     * @param operation Operation
     * @return true if all permission values are unchanged or if its changes has been confirmed, false otherwise
     */
    private boolean checkPermissionsConfirmedValues(Operation operation)
    {
    	boolean ok=true;
    	for (PermissionBean permission:getPermissions(getCurrentUserOperation(operation)))
    	{
    		if (!permission.getValue().equals(permission.getConfirmedValue()))
    		{
				ok=false;
				addErrorMessage("PERMISSIONS_EDIT_CONFIRM_ERROR");
				break;
    		}
    	}
    	return ok;
    }
    
    /**
     * Rollback permission values to the confirmed values.<br/><br/>
     * We need to do this because navigation through &lt;p:wizard&gt; component can update values even when they
     * are not still confirmed.
     * @param operation Operation
     */
    private void rollbackPermissionsConfirmedValues(Operation operation)
    {
    	for (PermissionBean permission:getPermissions(getCurrentUserOperation(operation)))
    	{
    		permission.setValue(permission.getConfirmedValue());
    	}
    }
    
    /**
     * Listener for update a permission.
     * @param event Row edit event
     */
    public void changePermission(RowEditEvent event)
    {
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	PermissionBean permission=(PermissionBean)event.getObject();
    	if (checkPermissionValueAllowed(operation,permission))
    	{
    		User user=getUser(operation);
        	UserPermission userPermission=null;
        	for (UserPermission userPerm:user.getUserPermissions())
        	{
        		if (userPerm.getPermission().equals(permission.getPermission()))
        		{
        			userPermission=userPerm;
        			break;
        		}
        	}
        	if (userPermission==null)
        	{
        		userPermission=new UserPermission(0L,user,permission.getPermission(),permission.getValue());
        		user.getUserPermissions().add(userPermission);
        	}
        	else
        	{
        		userPermission.setValue(permission.getValue());
        	}
        	permission.setValueType(PermissionBean.USER_PERMISSION_VALUE);
          	permission.setConfirmedValue(permission.getValue());
    	}
    	else
    	{
    		permission.setValue(permission.getConfirmedValue());
    		addErrorMessage("RAISE_PERMISSION_ERROR");
    		
			// Scroll page to top position
			scrollToTop();
    	}
    }
    
    /**
     * Reset a permission to the value of its user type permission if defined or to default value otherwise.
     * @param operation Operation
     * @param permission Permission
     */
    private void resetPermission(Operation operation,PermissionBean permission)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	User user=getUser(operation);
    	UserPermission userPermission=null;
    	for (UserPermission userPerm:user.getUserPermissions())
    	{
    		if (userPerm.getPermission().equals(permission.getPermission()))
    		{
    			userPermission=userPerm;
    			break;
    		}
    	}
    	if (userPermission!=null)
    	{
    		user.getUserPermissions().remove(userPermission);
    	}
    	UserType userType=user.getUserType();
    	UserTypePermission userTypePermission=null;
    	if (userType!=null)
    	{
    		userTypePermission=
    			userTypePermissionsService.getUserTypePermission(operation,userType,permission.getPermission());
    	}
    	int valueType=PermissionBean.DEFAULT_VALUE;
    	String value=null;
    	if (userTypePermission==null)
    	{
    		value=permission.getPermission().getDefaultValue();
    	}
    	else
    	{
    		valueType=PermissionBean.USER_TYPE_PERMISSION_VALUE;
    		value=userTypePermission.getValue();
    	}
    	if (!checkPermissionValueAllowed(operation,permission))
    	{
    		valueType=PermissionBean.LIMITED_PERMISSION_VALUE;
    		value=getLimitedPermissionValue(operation,permission);
    	}
    	permission.setValue(value);
    	permission.setConfirmedValue(value);
    	permission.setValueType(valueType);
    }
    
    /**
     * Listener for reset a permission to the value of its user type permission if defined or to default value 
     * otherwise.
     * @param event Action event
     */
    public void resetPermission(ActionEvent event)
    {
    	PermissionBean permission=(PermissionBean)event.getComponent().getAttributes().get("permission");
    	resetPermission(getCurrentUserOperation(null),permission);
    }
    
    /**
     * Checks if if all permission values are allowed.
     * @param operation Operation
     * @param resetInvalidPermissions Flag to indicate if want to reset invalid permissions we found (true) 
     * or not (false)
     * @return true if all permission values are allowed, false otherwise
     */
    private boolean checkPermissionsValuesAllowed(Operation operation,boolean resetInvalidPermissions)
    {
    	boolean ok=true;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	for (PermissionBean permission:getPermissions(operation))
    	{
    		if (!checkPermissionValueAllowed(operation,permission))
    		{
    			ok=false;
    			if (resetInvalidPermissions)
    			{
    				resetPermission(operation,permission);
    			}
    			else
    			{
        			break;
    			}
    		}
    	}
    	return ok;
    }
    
    /**
     * Checks that current user has permission to create or edit an user (depending on the current operation).
     * @param operation Operation
     * @return true if current user has permission to create or edit an user (depending on the current operation),
     * false otherwise
     */
    private boolean checkAdministrationPermissions(Operation operation)
    {
    	boolean ok=false;
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	Permission rawAdministrationNavigationPermission=
    		permissionsService.getPermission(operation,"PERMISSION_NAVIGATION_ADMINISTRATION");
    	resetCurrentUserPermission(rawAdministrationNavigationPermission);
    	if ("true".equals(getCurrentUserPermission(operation,rawAdministrationNavigationPermission).getValue()))
    	{
    		Permission rawUsersAdministrationPermission=
    			permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_ADMIN_USERS");
    		resetCurrentUserPermission(rawUsersAdministrationPermission);
    		if ("true".equals(getCurrentUserPermission(operation,rawUsersAdministrationPermission).getValue()))
    		{
        		Permission rawAddOrEditUserAllowedPermission=null;
        		if (getUser(operation).getId()>0L)
        		{
        			rawAddOrEditUserAllowedPermission=
        				permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_EDIT_USER_ENABLED");
        		}
        		else
        		{
        			rawAddOrEditUserAllowedPermission=
        				permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_ADD_USER_ENABLED");
        		}
        		resetCurrentUserPermission(rawAddOrEditUserAllowedPermission);
        		ok="true".equals(getCurrentUserPermission(operation,rawAddOrEditUserAllowedPermission).getValue());
    		}
    	}
    	return ok;
    }
    
    /**
     * @param operation Operation
     * @param userPermissions List of user permissions
     * @return List of user permissions to save user taking account user interface and values of permissions limited by
     * permissions
     */
    private List<UserPermission> getUserPermissionsToSave(Operation operation,List<UserPermission> userPermissions)
    {
    	List<UserPermission> userPermissionsToSave=new ArrayList<UserPermission>();
    	
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	User user=getUser(operation);
    	if (user.getId()>0L || isDisplayUserPermissions())
    	{
    		for (UserPermission userPermission:userPermissions)
    		{
    			userPermissionsToSave.add(userPermission);
    		}
    	}
		for (PermissionBean permission:getPermissions(operation))
		{
			if (permission.getValueType()==PermissionBean.LIMITED_PERMISSION_VALUE)
			{
				UserPermission userPermissionToSave=null;
				for (UserPermission userPermission:userPermissionsToSave)
				{
					if (userPermission.getPermission().equals(permission.getPermission()))
					{
						userPermissionToSave=userPermission;
						break;
					}
				}
				if (userPermissionToSave==null)
				{
					userPermissionToSave=new UserPermission(0L,user,permission.getPermission(),permission.getValue());
					userPermissionsToSave.add(userPermissionToSave);
				}
				else
				{
					userPermissionToSave.setValue(permission.getValue());
				}
			}
		}
    	return userPermissionsToSave;
    }
    
    /**
	 * Save user to DB.
	 * @return Next wiew (administration page if save is sucessful, otherwise we keep actual view)
     */
    public String saveUser()
    {
    	String nextView=null;
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	// We perform several checks before saving user
    	if (!checkAdministrationPermissions(operation))
    	{
    		displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
    	}
    	else
    	{
    		if (checkGeneralInputFields(operation,FacesContext.getCurrentInstance().getViewRoot()))
    		{
        		setCurrentPermissions(null);
        		User user=getUser(operation);
        		if (user.getId()>0L) // Update user
        		{
        			boolean updateOk=true;
        			// We need to check permissions to be sure that any changes has been confirmed
        			if (activeUserTabIndex==PERMISSIONS_TABVIEW_TAB && !checkPermissionsConfirmedValues(operation))
        			{
        				updateOk=false;
        				rollbackPermissionsConfirmedValues(operation);
        			}
        			if (updateOk && !checkUserTypeAllowed(operation,user.getUserType()))
        			{
       					updateOk=false;
       					addErrorMessage("USER_ROLE_RAISE_PERMISSIONS_ERROR");
        			}
        			if (updateOk && !checkPermissionsValuesAllowed(operation,true))
        			{
       					updateOk=false;
       					addErrorMessage("RAISE_PERMISSIONS_RESET_ERROR");
        			}
        			if (updateOk)
        			{
        				// Backup user permissions (user interface) to be able to restore them in case of error
            			List<UserPermission> userPermissions=user.getUserPermissions();
            			
            			// Set user permissions to save
            			user.setUserPermissions(getUserPermissionsToSave(operation,userPermissions));
            			
            			// Set tests of user (as they are on DB)
            			user.setUserTests(testUsersService.getUserTests(operation,user));
            			
            			// Edit user (including user permissions)
        				try
        				{
        					usersService.updateUser(user,isChangePassword());
        					nextView="administration?faces-redirect=true";
        				}
        				catch (ServiceException se)
        				{
            				// Restore user permissions backup (user interface)
            				user.setUserPermissions(userPermissions);
            				
            				// Display a critical error message
        					addErrorMessage("USER_SAVE_CRITICAL_ERROR");
        					nextView=null;
        				}
        				finally
        				{
        					// End current user session Hibernate operation
        					userSessionService.endCurrentUserOperation();
        				}
        			}
        		}
        		else // New user
        		{
        			boolean addOk=true;
        			if (!checkUserTypeAllowed(operation,user.getUserType()))
        			{
        				addOk=false;
        				addErrorMessage("USER_ROLE_RAISE_PERMISSIONS_ERROR");
       				}
        			if (addOk && isDisplayUserPermissions() && !checkPermissionsValuesAllowed(operation,true))
        			{
       					addOk=false;
       					addErrorMessage("RAISE_PERMISSIONS_RESET_ERROR");
        			}
        			if (addOk)
        			{
        				// Backup user permissions (user interface) to be able to restore them in case of error
            			List<UserPermission> userPermissions=user.getUserPermissions();
            			
            			// Set user permissions to save
            			user.setUserPermissions(getUserPermissionsToSave(operation,userPermissions));
            			
            			// Add user (including user permissions)
            			try
            			{
            				usersService.addUser(user);
            				nextView="administration?faces-redirect=true";
            			}
            			catch (ServiceException se)
            			{
            				// Restore user permissions backup (user interface)
            				user.setUserPermissions(userPermissions);
            				
            				// Display a critical error message
            				addErrorMessage("USER_SAVE_CRITICAL_ERROR");
            				nextView=null;
            			}
        				finally
        				{
        					// End current user session Hibernate operation
        					userSessionService.endCurrentUserOperation();
        				}
        			}
        		}
    		}
    	}
    	return nextView;
    }
	
	/**
	 * Displays an error message.
	 * @param message Error message (before localization)
	 */
	private void addErrorMessage(String message)
	{
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
	 * Handle enabling/disabling setters for checkboxes if user changes locale.<br/><br/>
	 * Note that this is needed because of a bug of &lt;p:wizard&gt; component that always set properties for 
	 * their checkboxes components as <i>false</i> when submitting the form.
	 * @param event Action event
	 */
	public void changeLocale(ActionEvent event)
	{
		if (getUser(getCurrentUserOperation(null)).getId()==0L)
		{
			setEnabledCheckboxesSetters(false);
		}
	}
}
