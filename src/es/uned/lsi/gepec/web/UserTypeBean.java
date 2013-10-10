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
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.backbeans.PermissionBean;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserPermissionsService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UserTypePermissionsService;
import es.uned.lsi.gepec.web.services.UserTypesService;

/**
 * Managed bean for creating/editing an user type (a.k.a. role).
 */
@SuppressWarnings("serial")
@ManagedBean(name="userTypeBean")
@ViewScoped
public class UserTypeBean implements Serializable
{
	private final static String GENERAL_WIZARD_TAB="general";
	private final static String PERMISSIONS_WIZARD_TAB="permissions";
	//private final static String CONFIRMATION_WIZARD_TAB="confirmation";
	
	private final static int GENERAL_TABVIEW_TAB=0;
	private final static int PERMISSIONS_TABVIEW_TAB=1;
	
	@ManagedProperty(value="#{userTypesService}")
	private UserTypesService userTypesService;
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
	
	private UserType userType;						// Current user type
	
	private List<PermissionBean> permissions;
	
	private List<PermissionBean> currentPermissions;
	
	/* UI Helper Properties */
	
	private String cancelUserTypeTarget;
	
	private String activeUserTypeTabName;
	private int activeUserTypeTabIndex;
	
	private Map<Long,PermissionBean> currentUserPermissions;
	
	public UserTypeBean()
	{
		activeUserTypeTabName=GENERAL_WIZARD_TAB;		
		activeUserTypeTabIndex=GENERAL_TABVIEW_TAB;
	}
	
	public void setUserTypesService(UserTypesService userTypesService)
	{
		this.userTypesService=userTypesService;
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
    	if (operation!=null && userType==null)
    	{
    		getUserType();
    		operation=null;
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
	public UserType getUserType()
	{
		if (userType==null)
		{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
    		
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
			
    		// We seek parameters
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
			
    		// Check if we are creating a new user or editing an existing one
    		if (params.containsKey("userTypeId"))
    		{
    			userType=userTypesService.getUserType(operation,Long.parseLong(params.get("userTypeId")));
    		}
    		else
    		{
    			userType=new UserType(0L,"","");
    			userType.setUserTypePermissions(new ArrayList<UserTypePermission>());
    		}
		}
		return userType;
	}
	
	public void setUserType(UserType userType)
	{
		this.userType=userType;
	}
	
	/**
	 * @param operation Operation
	 * @return Current permissions of the user type (a.k.a. role) we are creating without taking account 
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
			UserType userType=getUserType();
			if (userType.getId()==0L)
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
					userTypePermissionsService.getUserTypePermissions(operation,userType);
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
						currentPermission=new PermissionBean(
							rawPermission,PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
					}
					currentPermissions.add(currentPermission);
				}
			}
		}
		return currentPermissions;
	}
	
	/**
	 * Set current permissions of the user type (a.k.a. role) we are creating or editing without taking account unsaved
	 * changes.
	 */
	public void setCurrentPermissions(List<PermissionBean> currentPermissions)
	{
		this.currentPermissions=currentPermissions;
	}
	
	/**
	 * @param operation Operation
	 * @param permission Permission
	 * @return Current permission of the user type (a.k.a. role) we are creating or editing without taking account 
	 * unsaved changes
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
		
    	Permission raisePermissionsAllowedRaw=permissionsService.getPermission(
    		operation,"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED");
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
	 * @param operation Operation
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
	
	public List<PermissionBean> getPermissions(Operation operation)
	{
		if (permissions==null)
		{
			permissions=new ArrayList<PermissionBean>();
			
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			List<Permission> rawPermissions=permissionsService.getPermissions(operation);
			List<UserTypePermission> userTypePermissions=getUserType().getUserTypePermissions();
			for (Permission rawPermission:rawPermissions)
			{
				PermissionBean permission=null;
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
					permission=new PermissionBean(
						rawPermission,PermissionBean.USER_TYPE_PERMISSION_VALUE,userTypePermission.getValue());
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
		return permissions;
	}
	
	public String getCancelUserTypeTarget()
	{
		return cancelUserTypeTarget;
	}
	
	public void setCancelUserTypeTarget(String cancelUserTypeTarget)
	{
		this.cancelUserTypeTarget=cancelUserTypeTarget;
	}
	
	private Map<Long,PermissionBean> getCurrentUserPermissions()
	{
		if (currentUserPermissions==null)
		{
			currentUserPermissions=new HashMap<Long,PermissionBean>();
		}
		return currentUserPermissions;
	}
	
	private void resetCurrentUserPermissions()
	{
		currentUserPermissions=null;
	}
	
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
				// Get current user session Hibernate operation
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
					getCurrentUserPermissions().put(Long.valueOf(permission.getId()),currentUserPermission);
				}
			}
		}
		return currentUserPermission;
	}
	
	private void resetCurrentUserPermission(Permission permission)
	{
		Map<Long,PermissionBean> currentUserPermissions=getCurrentUserPermissions();
		if (permission!=null && currentUserPermissions.containsKey(Long.valueOf(permission.getId())))
		{
			currentUserPermissions.remove(Long.valueOf(permission.getId()));
		}
	}
	
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
	 * Action listener to show the dialog to confirm cancel of role creation/edition.
	 * @param event Action event
	 */
	public void showConfirmCancelUserTypeDialog(ActionEvent event)
	{
		setCancelUserTypeTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelUserTypeDialog.show()");
	}
	
	/**
	 * Cancel role creation/editon and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelUserType()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelUserTypeTarget()))
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
		else if (getCancelUserTypeTarget()!=null)
		{
			nextView=new StringBuffer(getCancelUserTypeTarget());
			nextView.append("?faces-redirect=true");
		}
		return nextView==null?null:nextView.toString();
	}
	
	/**
	 * Process general tab input fields.
	 */
	private void processGeneralInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		String userTypeTypeId=null;
		String userTypeDescriptionId=null;
		
		UserType userType=getUserType();
		if (userType.getId()==0L)
		{
			userTypeTypeId="userTypeForm:userTypeType";
			userTypeDescriptionId=":userTypeForm:userTypeDescription";
		}
		else
		{
			userTypeTypeId="userTypeForm:userTypeFormTabs:userTypeType";
			userTypeDescriptionId=":userTypeForm:userTypeFormTabs:userTypeDescription";
		}
		// We only need to process role if we are creating a new role
		if (userType.getId()==0L)
		{
			UIInput userTypeType=(UIInput)component.findComponent(userTypeTypeId);
			userTypeType.processDecodes(context);
			if (userTypeType.getSubmittedValue()!=null)
			{
				userType.setType((String)userTypeType.getSubmittedValue());
			}
		}
		// We always need to process description
		UIInput userTypeDescription=(UIInput)component.findComponent(userTypeDescriptionId);
		userTypeDescription.processDecodes(context);
		if (userTypeDescription.getSubmittedValue()!=null)
		{
			userType.setDescription((String)userTypeDescription.getSubmittedValue());
		}
	}
	
    /**
     * Checks that user has entered a role name or displays an error message.
     * @param roleName Role name
     * @return true if user has entered a role name, false otherwise
     */
    private boolean checkNonEmptyUserTypeRoleName(String roleName)
    {
    	boolean ok=true;
    	if (roleName==null || roleName.equals(""))
    	{
    		addErrorMessage("ROLE_NAME_REQUIRED");
    		ok=false;
    	}
    	return ok;
    }
    
	/**
	 * Checks that role name entered by user only includes valid characters or displays an error message.
     * @param roleName Role name
	 * @return true if role name only includes valid characters (letters, digits, whitespaces or some of the 
	 * following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 */
    private boolean checkValidCharactersForUserTypeRoleName(String roleName)
    {
    	boolean ok=true;
    	if (StringUtils.hasUnexpectedCharacters(
        	roleName,true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'}))
    	{
    		addErrorMessage("ROLE_NAME_INVALID_CHARACTERS");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that the first character of the role name entered by user is a letter.
     * @param roleName Role name
     * @return true if the first character of the role name entered by user is a letter, false otherwise
     */
    private boolean checkFirstCharacterIsALetterForUserTypeRoleName(String roleName)
    {
    	boolean ok=true;
    	if (!Character.isLetter(roleName.charAt(0)))
    	{
    		addErrorMessage("ROLE_NAME_FIRST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that last character of the role name entered by user is not a whitespace or displays an error message.
     * @param roleName Role name
	 * @return true if last character of the role name entered by user is not a whitespace, false otherwise
     */
    private boolean checkLastCharacterNotWhitespaceForUserTypeRoleName(String roleName)
    {
    	boolean ok=true;
    	if (StringUtils.isLastCharacterWhitespace(roleName))
    	{
    		addErrorMessage("ROLE_NAME_LAST_CHARACTER_INVALID");
    		ok=false;
    	}
    	return ok;
    }
    
    /**
     * Check that role name entered by user does not include consecutive whitespaces or displays an error message.
     * @param roleName Role name
	 * @return true if the role name entered by user does not include consecutive whitespaces, false otherwise
     */
    private boolean checkNonConsecutiveWhitespacesForUserTypeRoleName(String roleName)
    {
    	boolean ok=true;
    	if (StringUtils.hasConsecutiveWhitespaces(roleName))
    	{
    		addErrorMessage("ROLE_NAME_WITH_CONSECUTIVE_WHITESPACES");
    		ok=false;
    	}
    	return ok;
    }
	
    /**
     * Check that role name entered by user has not been used yet for other role.
     * @param operation Operation
     * @param roleName Role name
     * @return true if role name entered by user has not been used yet for other role, false otherwise
     */
	private boolean checkUnusedForUserTypeRoleName(Operation operation,String roleName)
	{
    	boolean ok=true;
    	if (userTypesService.checkUserTypeType(getCurrentUserOperation(operation),roleName))
		{
			addErrorMessage("ROLE_NAME_ALREADY_DECLARED");
			ok=false;
		}
    	return ok;
	}
    
    /**
     * Check that role name entered by user is valid or displays error messages indicating the causes.
     * @param operation Operation
     * @param roleName Role name
     * @return true if role name entered by user is valid, false otherwise
     */
    private boolean checkUserTypeRoleName(Operation operation,String roleName)
    {
    	boolean ok=checkNonEmptyUserTypeRoleName(roleName);
    	if (ok)
    	{
    		if (!checkValidCharactersForUserTypeRoleName(roleName))
    		{
    			ok=false;
    		}
    		if (!checkFirstCharacterIsALetterForUserTypeRoleName(roleName))
    		{
    			ok=false;
    		}
    		if (!checkLastCharacterNotWhitespaceForUserTypeRoleName(roleName))
    		{
    			ok=false;
    		}
    		if (!checkNonConsecutiveWhitespacesForUserTypeRoleName(roleName))
    		{
    			ok=false;
    		}
    		if (ok)
    		{
    			ok=checkUnusedForUserTypeRoleName(getCurrentUserOperation(operation),roleName);
    		}
    	}
    	return ok;
    }
	
	/**
	 * Check general input fields and display error messages if needed.
	 * @param operation Operation
	 * @return true if all checks are passed correctly, false if any check fails
	 */
	private boolean checkGeneralInputFields(Operation operation)
	{
		boolean ok=true;
		
		// We only need to check role name and only if we are creating a new role
		UserType userType=getUserType();
		if (userType.getId()==0L)
		{
			ok=checkUserTypeRoleName(getCurrentUserOperation(operation),userType.getType());
		}
		return ok;
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
    	String newStep=event.getNewStep();
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	if (checkAdministrationPermissions(operation))
    	{
        	if (GENERAL_WIZARD_TAB.equals(oldStep))
        	{
        		if (!checkGeneralInputFields(operation))
        		{
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
        	if (PERMISSIONS_WIZARD_TAB.equals(newStep))
        	{
        		setPermissions(null);
        		setCurrentPermissions(null);
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
    	activeUserTypeTabName=newStep;
    	return newStep;
    }
	
    public String getActiveUserTypeTabName()
    {
    	String activeUserTypeTabName=null;
    	if (getUserType().getId()>0L)
    	{
    		switch (activeUserTypeTabIndex)
    		{
    			case GENERAL_TABVIEW_TAB:
    				activeUserTypeTabName=GENERAL_WIZARD_TAB;
    				break;
    			case PERMISSIONS_TABVIEW_TAB:
    				activeUserTypeTabName=PERMISSIONS_WIZARD_TAB;
    		}
    	}
    	else
    	{
    		activeUserTypeTabName=this.activeUserTypeTabName;
    	}
    	return activeUserTypeTabName;
    }
    
	/**
	 * Tab change listener for displaying other tab of an user type.
	 * @param event Tab change event
	 */
    public void changeActiveUserTypeTab(TabChangeEvent event)
    {
    	boolean ok=true;
    	TabView userTypeFormTab=(TabView)event.getComponent();
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
    	if (checkAdministrationPermissions(operation))
    	{
        	if (activeUserTypeTabIndex==GENERAL_TABVIEW_TAB)
        	{
        		// We need to process some input fields
        		processGeneralInputFields(userTypeFormTab);
        		
        		ok=checkGeneralInputFields(operation);
        	}
        	else
        	if (activeUserTypeTabIndex==PERMISSIONS_TABVIEW_TAB)
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
			activeUserTypeTabIndex=userTypeFormTab.getActiveIndex();
		}
		else
		{
	    	userTypeFormTab.setActiveIndex(activeUserTypeTabIndex);
	    	
			// Scroll page to top position
			scrollToTop();
		}
    	if (activeUserTypeTabIndex==PERMISSIONS_TABVIEW_TAB)
    	{
    		setPermissions(null);
    		setCurrentPermissions(null);
    		resetCurrentUserPermissions();
    	}
    }
    
    /**
     * Checks if if all permission values are unchanged or its changes has been confirmed displaying an error message
     * otherwise.
     * @param operation Operation
     * @return true if all permission values are unchanged or its changes has been confirmed, false otherwise
     */
    private boolean checkPermissionsConfirmedValues(Operation operation)
    {
    	boolean ok=true;
    	for (PermissionBean permission:getPermissions(getCurrentUserOperation(operation)))
    	{
    		if (!permission.getValue().equals(permission.getConfirmedValue()))
    		{
    			addErrorMessage("PERMISSIONS_EDIT_CONFIRM_ERROR");
				ok=false;
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
    	PermissionBean permission=(PermissionBean)event.getObject();
    	
    	if (checkPermissionValueAllowed(getCurrentUserOperation(null),permission))
    	{
    		UserType userType=getUserType();
        	UserTypePermission userTypePermission=null;
        	for (UserTypePermission userTypePerm:userType.getUserTypePermissions())
        	{
        		if (userTypePerm.getPermission().equals(permission.getPermission()))
        		{
        			userTypePermission=userTypePerm;
        			break;
        		}
        	}
        	if (userTypePermission==null)
        	{
        		userTypePermission=
        			new UserTypePermission(0L,userType,permission.getPermission(),permission.getValue());
        		userType.getUserTypePermissions().add(userTypePermission);
        	}
        	else
        	{
        		userTypePermission.setValue(permission.getValue());
        	}
        	permission.setValueType(PermissionBean.USER_TYPE_PERMISSION_VALUE);
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
     * Reset a permission to its default value.
     * @param permission Permission
     */
    private void resetPermission(PermissionBean permission)
    {
    	UserType userType=getUserType();
    	UserTypePermission userTypePermission=null;
    	for (UserTypePermission userTypePerm:userType.getUserTypePermissions())
    	{
    		if (userTypePerm.getPermission().equals(permission.getPermission()))
    		{
    			userTypePermission=userTypePerm;
    			break;
    		}
    	}
    	if (userTypePermission!=null)
    	{
    		userType.getUserTypePermissions().remove(userTypePermission);
    	}
    	String value=permission.getPermission().getDefaultValue();
    	permission.setValue(value);
    	permission.setConfirmedValue(value);
    	permission.setValueType(PermissionBean.DEFAULT_VALUE);
    }
    
    /**
     * Listener for reset a permission to its default value.
     * @param event Action event
     */
    public void resetPermission(ActionEvent event)
    {
    	PermissionBean permission=(PermissionBean)event.getComponent().getAttributes().get("permission");
    	resetPermission(permission);
    }
    
    /**
     * Checks if if all permission values are allowed.
     * @param operation Operation
     * @param resetInvalidPermissions Flag to indicate if want to reset invalid permissions we found (true) 
     * or not (false)
     * @return true if all permission values are allowed, false otherwise
     */
    public boolean checkPermissionsValuesAllowed(Operation operation,boolean resetInvalidPermissions)
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
    				resetPermission(permission);
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
     * Checks that current user has permission to create or edit a role (depending on the current operation).
     * @param operation Operation
     * @return true if current user has permission to create or edit a role (depending on the current operation),
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
    		Permission rawRolesAdministrationPermission=
    			permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_ADMIN_ROLES");
    		resetCurrentUserPermission(rawRolesAdministrationPermission);
    		if ("true".equals(getCurrentPermission(operation,rawRolesAdministrationPermission).getValue()))
    		{
        		Permission rawAddOrEditRoleAllowedPermission=null;
        		if (getUserType().getId()>0L)
        		{
        			rawAddOrEditRoleAllowedPermission=
        				permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_EDIT_ROLE_ENABLED");
        		}
        		else
        		{
        			rawAddOrEditRoleAllowedPermission=
        				permissionsService.getPermission(operation,"PERMISSION_ADMINISTRATION_ADD_ROLE_ENABLED");
        		}
        		resetCurrentUserPermission(rawAddOrEditRoleAllowedPermission);
        		ok="true".equals(getCurrentUserPermission(operation,rawAddOrEditRoleAllowedPermission).getValue());
    		}
    	}
    	return ok;
    }
    
    /**
     * @param operation Operation
     * @param userTypePermissions List of user type permissions
     * @return List of user type permissions to save user type taking account user interface and values of permissions 
     * limited by permissions
     */
    private List<UserTypePermission> getUserTypePermissionsToSave(Operation operation,
    	List<UserTypePermission> userTypePermissions)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	List<UserTypePermission> userTypePermissionsToSave=new ArrayList<UserTypePermission>();
    	for (UserTypePermission userTypePermission:userTypePermissions)
    	{
    		userTypePermissionsToSave.add(userTypePermission);
    	}
		for (PermissionBean permission:getPermissions(operation))
		{
			if (permission.getValueType()==PermissionBean.LIMITED_PERMISSION_VALUE)
			{
				UserTypePermission userTypePermissionToSave=null;
				for (UserTypePermission userTypePermission:userTypePermissionsToSave)
				{
					if (userTypePermission.getPermission().equals(permission.getPermission()))
					{
						userTypePermissionToSave=userTypePermission;
						break;
					}
				}
				if (userTypePermissionToSave==null)
				{
					userTypePermissionToSave=
						new UserTypePermission(0L,getUserType(),permission.getPermission(),permission.getValue());
					userTypePermissionsToSave.add(userTypePermissionToSave);
				}
				else
				{
					userTypePermissionToSave.setValue(permission.getValue());
				}
			}
		}
    	return userTypePermissionsToSave;
    }
    
    /**
	 * Save user type to DB.
	 * @return Next wiew (administration page if save is sucessful, otherwise we keep actual view)
     */
    public String saveUserType()
    {
    	String nextView=null;
    	
    	// Get current user session Hibernate operation
    	Operation operation=getCurrentUserOperation(null);
    	
		UserType userType=getUserType();
    	
    	// We perform several checks before saving user
    	if (!checkAdministrationPermissions(operation))
    	{
    		displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
    	}
    	else if (userType.getId()>0L && !userTypesService.checkUserTypeId(operation,userType.getId()))
    	{
    		displayErrorPage("ROLE_EDIT_NOT_FOUND_ERROR","The role you are trying to edit no longer exists.");
    	}
    	else
    	{
    		if (checkGeneralInputFields(operation))
    		{
        		if (userType.getId()>0L) // Update user type
        		{
        			boolean updateOk=true;
        			// We need to check permissions to be sure that any changes has been confirmed
        			if (activeUserTypeTabIndex==PERMISSIONS_TABVIEW_TAB && !checkPermissionsConfirmedValues(operation))
        			{
        				updateOk=false;
        				rollbackPermissionsConfirmedValues(operation);
        			}
        			if (updateOk && !checkPermissionsValuesAllowed(operation,true))
        			{
       					updateOk=false;
       					addErrorMessage("RAISE_PERMISSIONS_RESET_ERROR");
        			}
        			if (updateOk)
        			{
        				// Backup user type permissions (user interface) to be able to restore them in case of error
        				List<UserTypePermission> userTypePermissions=userType.getUserTypePermissions();
        				
            			// Set user type permissions to save
        				userType.setUserTypePermissions(getUserTypePermissionsToSave(operation,userTypePermissions));
        				
        				try
        				{
        					userTypesService.updateUserType(userType);
        					nextView="administration?faces-redirect=true";
        				}
        				catch (ServiceException se)
        				{
            				// Restore user type permissions backup (user interface)
        					userType.setUserTypePermissions(userTypePermissions);
        					
            				// Display a critical error message
        					addErrorMessage("ROLE_SAVE_CRITICAL_ERROR");
        				}
        				finally
        				{
        					// End current user session Hibernate operation
        					userSessionService.endCurrentUserOperation();
        				}
        			}
        		}
        		else	// New user type
        		{
        			boolean addOk=true;
        			if (!checkPermissionsValuesAllowed(operation,true))
        			{
       					addOk=false;
       					addErrorMessage("RAISE_PERMISSIONS_RESET_ERROR");
        			}
        			if (addOk)
        			{
        				// Backup user type permissions (user interface) to be able to restore them in case of error
        				List<UserTypePermission> userTypePermissions=userType.getUserTypePermissions();
        				
            			// Set user type permissions to save
        				userType.setUserTypePermissions(getUserTypePermissionsToSave(operation,userTypePermissions));
        				
            			try
            			{
            				userTypesService.addUserType(userType);
            				nextView="administration?faces-redirect=true";
            			}
            			catch (ServiceException se)
            			{
            				// Restore user type permissions backup (user interface)
            				userType.setUserTypePermissions(userTypePermissions);
            				
            				// Display a critical error message
            				addErrorMessage("ROLE_SAVE_CRITICAL_ERROR");
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
}
