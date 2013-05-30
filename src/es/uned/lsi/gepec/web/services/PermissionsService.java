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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import es.uned.lsi.gepec.model.dao.DaoException;
import es.uned.lsi.gepec.model.dao.PermissionsDao;
import es.uned.lsi.gepec.model.entities.Permission;
import es.uned.lsi.gepec.model.entities.PermissionType;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.UserPermission;
import es.uned.lsi.gepec.model.entities.UserType;
import es.uned.lsi.gepec.model.entities.UserTypePermission;
import es.uned.lsi.gepec.util.HibernateUtil;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;

/**
 * Manages permissions.
 */
@SuppressWarnings("serial")
@ManagedBean(eager=true)
@ApplicationScoped
public class PermissionsService implements Serializable
{
	private final static PermissionsDao PERMISSIONS_DAO=new PermissionsDao();
	
	private final static Map<Long,Permission> PERMISSIONS_CACHED_BY_ID=new HashMap<Long,Permission>();
	private final static Map<String,Permission> PERMISSIONS_CACHED_BY_NAME=new HashMap<String,Permission>();
	private final static List<Permission> PERMISSIONS_CACHED=new ArrayList<Permission>();
	
	@ManagedProperty(value="#{userPermissionsService}")
	private UserPermissionsService userPermissionsService;
	@ManagedProperty(value="#{userTypePermissionsService}")
	private UserTypePermissionsService userTypePermissionsService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	public PermissionsService()
	{
	}
	
	public void setUserPermissionsService(UserPermissionsService userPermissionsService)
	{
		this.userPermissionsService=userPermissionsService;
	}
	
	public void setUserTypePermissionsService(UserTypePermissionsService userTypePermissionsService)
	{
		this.userTypePermissionsService=userTypePermissionsService;
	}
	
	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService=localizationService;
	}
	
	/**
	 * @param id Permission identifier
	 * @return Permission
	 * @throws ServiceException
	 */
	public Permission getPermission(long id) throws ServiceException
	{
		return getPermission(null,id);
	}
	
	/**
	 * @param operation Operation
	 * @param id Permission identifier
	 * @return Permission
	 * @throws ServiceException
	 */
	public Permission getPermission(Operation operation,long id) throws ServiceException
	{
		Permission permission=null;
		Permission permissionFromCache=PERMISSIONS_CACHED_BY_ID.get(Long.valueOf(id));
		if (permissionFromCache==null)
		{
			try
			{
				PERMISSIONS_DAO.setOperation(operation);
				permissionFromCache=PERMISSIONS_DAO.getPermission(id,true);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (permissionFromCache!=null)
			{
				PERMISSIONS_CACHED_BY_ID.put(Long.valueOf(id),permissionFromCache);
				PERMISSIONS_CACHED_BY_NAME.put(permissionFromCache.getName(),permissionFromCache);
			}
		}
		// We don't want caller accessing directly to a cached permission so we return a copy
		if (permissionFromCache!=null)
		{
			permission=new Permission();
			permission.setFromOtherPermission(permissionFromCache);
			permission.setPermissionType(getPermissionTypeCopy(permission.getPermissionType()));
		}
		return permission;
	}
	
	/**
	 * @param name Permission name
	 * @return Permission
	 * @throws ServiceException
	 */
	public Permission getPermission(String name) throws ServiceException
	{
		return getPermission(null,name);
	}
	
	/**
	 * @param operation Operation
	 * @param name Permission name
	 * @return Permission
	 * @throws ServiceException
	 */
	public Permission getPermission(Operation operation,String name) throws ServiceException
	{
		Permission permission=null;
		Permission permissionFromCache=PERMISSIONS_CACHED_BY_NAME.get(name);
		if (permissionFromCache==null)
		{
			try
			{
				PERMISSIONS_DAO.setOperation(operation);
				permissionFromCache=PERMISSIONS_DAO.getPermission(name,true);
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
			if (permissionFromCache!=null)
			{
				PERMISSIONS_CACHED_BY_ID.put(Long.valueOf(permissionFromCache.getId()),permissionFromCache);
				PERMISSIONS_CACHED_BY_NAME.put(name,permissionFromCache);
			}
		}
		// We don't want caller accessing directly to a cached permission so we return a copy
		if (permissionFromCache!=null)
		{
			permission=new Permission();
			permission.setFromOtherPermission(permissionFromCache);
			permission.setPermissionType(getPermissionTypeCopy(permission.getPermissionType()));
		}
		return permission;
	}
	
	/**
	 * @return List of all permissions
	 * @throws ServiceException
	 */
	public List<Permission> getPermissions() throws ServiceException
	{
		return getPermissions(null);
	}
	
	/**
	 * @param operation Operation
	 * @return List of all permissions
	 * @throws ServiceException
	 */
	public List<Permission> getPermissions(Operation operation) throws ServiceException
	{
		List<Permission> permissions=new ArrayList<Permission>();
		if (PERMISSIONS_CACHED.isEmpty())
		{
			try
			{
				PERMISSIONS_DAO.setOperation(operation);
				for (Permission permission:PERMISSIONS_DAO.getPermissions(true))
				{
					Long permissionId=Long.valueOf(permission.getId());
					if (PERMISSIONS_CACHED_BY_ID.containsKey(permissionId))
					{
						permission=PERMISSIONS_CACHED_BY_ID.get(permissionId);
					}
					else
					{
						PERMISSIONS_CACHED_BY_ID.put(permissionId,permission);
						PERMISSIONS_CACHED_BY_NAME.put(permission.getName(),permission);
					}
					PERMISSIONS_CACHED.add(permission);
				}
			}
			catch (DaoException de)
			{
				throw new ServiceException(de.getMessage(),de);
			}
		}
		
		// We don't want caller accessing directly to cached permissions so we return copies of all them
		List<PermissionType> permissionTypeCopies=new ArrayList<PermissionType>();
		for (Permission permissionFromCache:PERMISSIONS_CACHED)
		{
			Permission permission=null;
			if (permissionFromCache!=null)
			{
				permission=new Permission();
				permission.setFromOtherPermission(permissionFromCache);
				permission.setPermissionType(
					getPermissionTypeCopy(permission.getPermissionType(),permissionTypeCopies));
				if (!permissionTypeCopies.contains(permission.getPermissionType()))
				{
					permissionTypeCopies.add(permission.getPermissionType());
				}
			}
			permissions.add(permission);
		}
		
		// We sort results by localized permission name 
		// (we can't have this sorting cached because user can change language at runtime)
		Collections.sort(permissions,new Comparator<Permission>()
		{
			@Override
			public int compare(Permission o1,Permission o2)
			{
				try
				{
				localizationService.getLocalizedMessage(o1.getName()).compareTo(
						localizationService.getLocalizedMessage(o2.getName()));
				}
				catch (NullPointerException npe)
				{
					System.out.println("NullPointerException al comparar "+ o1.getName() + " con " + o2.getName());
				}
				
				return localizationService.getLocalizedMessage(o1.getName()).compareTo(
					localizationService.getLocalizedMessage(o2.getName()));
			}
		});
		return permissions;
	}
	
	/**
	 * Reset cached permissions.
	 */
	public void resetCachedPermissions()
	{
		PERMISSIONS_CACHED_BY_ID.clear();
		PERMISSIONS_CACHED_BY_NAME.clear();
		PERMISSIONS_CACHED.clear();
	}
	
	/**
	 * @param permissionType Permission type
	 * @param permissionTypeCopies List with copies of permission types already done
	 * @return Copy of permission type
	 */
	private PermissionType getPermissionTypeCopy(PermissionType permissionType,
		List<PermissionType> permissionTypeCopies)
	{
		PermissionType permissionTypeCopy=null;
		if (permissionType!=null)
		{
			if (permissionTypeCopies.contains(permissionType))
			{
				permissionTypeCopy=permissionTypeCopies.get(permissionTypeCopies.indexOf(permissionType));
			}
			else
			{
				permissionTypeCopy=new PermissionType();
				permissionTypeCopy.setFromOtherPermissionType(permissionType);
			}
		}
		return permissionTypeCopy;
	}
	
	/**
	 * @param permissionType Permission type
	 * @return Copy of permission type
	 */
	private PermissionType getPermissionTypeCopy(PermissionType permissionType)
	{
		return getPermissionTypeCopy(permissionType,new ArrayList<PermissionType>());
	}
	
	/**
	 * Check that the permission indicated exists and its type is boolean. 
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @return Permission
	 * @throws ServiceException
	 */
	private Permission checkBooleanPermission(Operation operation,String permissionName) 
		throws ServiceException
	{
		Permission permission=null;
		try
		{
			// Get permission
			permission=getPermission(operation,permissionName);
		}
		catch (ServiceException se)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_CRITICAL_ERROR");
			}
			catch (ServiceException se2)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error when trying to check a permission. Contact an administrator if this problem persists.";
			}
			throw new ServiceException(errorMessage,se);
		}
		
		// Checks
		if (permission==null)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_NOT_FOUND_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error. Checked permission is not found.";
			}
			throw new ServiceException(errorMessage);
		}
		else if (!"PERMISSION_TYPE_BOOLEAN".equals(permission.getPermissionType().getType()))
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_NOT_BOOLEAN_TYPE_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error. Checked permission type is not boolean.";
			}
			throw new ServiceException(errorMessage);
		}
		return permission;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @return true if user is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(User user,String permissionName) throws ServiceException
	{
		return isGranted(null,user,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @return true if user is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(Operation operation,User user,String permissionName) throws ServiceException
	{
		boolean granted=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check if user is granted that permission
			granted=isGranted(operation,user,checkBooleanPermission(operation,permissionName));
		}
		catch (ServiceException se)
		{
			granted=false;
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return granted;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @return true if user is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(User user,String permissionName) throws ServiceException
	{
		return !isGranted(null,user,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @return true if user is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(Operation operation,User user,String permissionName) throws ServiceException
	{
		return !isGranted(operation,user,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permission Permission
	 * @return true if user is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	private boolean isGranted(Operation operation,User user,Permission permission) throws ServiceException
	{
		boolean granted=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (user!=null)
			{
				UserPermission userPermission=userPermissionsService.getUserPermission(operation,user,permission);
				if (userPermission==null)
				{
					if (user.getUserType()==null)
					{
						granted=Boolean.valueOf(permission.getDefaultValue()).booleanValue();
					}
					else
					{
						granted=isGranted(operation,user.getUserType(),permission);
					}
				}
				else
				{
					granted=Boolean.valueOf(userPermission.getValue()).booleanValue();
				}
			}
		}
		catch (ServiceException se)
		{
			granted=false;
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return granted;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return true if user type is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(UserType userType,String permissionName) throws ServiceException
	{
		return isGranted(null,userType,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return true if user type is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	public boolean isGranted(Operation operation,UserType userType,String permissionName) throws ServiceException
	{
		boolean granted=false;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Check if user type is granted that permission
			granted=isGranted(operation,userType,checkBooleanPermission(operation,permissionName));
		}
		catch (ServiceException se)
		{
			granted=false;
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return granted;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return true if user type is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(UserType userType,String permissionName) throws ServiceException
	{
		return !isGranted(null,userType,permissionName);
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return true if user type is denied that permission, false if granted.
	 * @throws ServiceException
	 */
	public boolean isDenied(Operation operation,UserType userType,String permissionName) throws ServiceException
	{
		return !isGranted(operation,userType,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permission Permission
	 * @return true if user type is granted that permission, false if denied.
	 * @throws ServiceException
	 */
	private boolean isGranted(Operation operation,UserType userType,Permission permission) throws ServiceException
	{
		boolean granted=false;
		try
		{
			if (userType!=null)
			{
				UserTypePermission userTypePermission=
					userTypePermissionsService.getUserTypePermission(operation,userType,permission);
				if (userTypePermission==null)
				{
					granted=Boolean.valueOf(permission.getDefaultValue()).booleanValue();
				}
				else
				{
					granted=Boolean.valueOf(userTypePermission.getValue()).booleanValue();
				}
			}
		}
		catch (ServiceException se)
		{
			granted=false;
		}
		return granted;
	}
	
	/**
	 * Check that the permission indicated exists and its type is integer. 
	 * @param operation Operation
	 * @param permissionName Permission name
	 * @return Permission or null if it is not found
	 * @throws ServiceException
	 */
	private Permission checkIntegerPermission(Operation operation,String permissionName) throws ServiceException
	{
		Permission permission=null;
		try
		{
			// Get permission
			permission=getPermission(operation,permissionName);
		}
		catch (ServiceException se)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_CRITICAL_ERROR");
			}
			catch (ServiceException se2)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error when trying to check a permission. Contact an administrator if this problem persists.";
			}
			throw new ServiceException(errorMessage,se);
		}
		
		// Checks
		if (permission==null)
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_NOT_FOUND_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error. Checked permission is not found.";
			}
			throw new ServiceException(errorMessage);
		}
		else if (!permission.getPermissionType().getType().equals("PERMISSION_TYPE_INT"))
		{
			String errorMessage=null;
			try
			{
				errorMessage=localizationService.getLocalizedMessage("PERMISSION_NOT_INTEGER_TYPE_ERROR");
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
			if (errorMessage==null)
			{
				errorMessage="Fatal error. Checked permission type is not boolean.";
			}
			throw new ServiceException(errorMessage);
		}
		return permission;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(User user,String permissionName) throws ServiceException
	{
		return getIntegerPermission(null,user,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(Operation operation,User user,String permissionName) throws ServiceException
	{
		int intValue=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get permission value
			String value=getIntegerPermission(operation,user,checkIntegerPermission(operation,permissionName));
			
			// Check that permission value is integer
			if (value!=null)
			{
				try
				{
					intValue=Integer.parseInt(value);
				}
				catch (NumberFormatException nfe)
				{
					String errorMessage=null;
					try
					{
						errorMessage=localizationService.getLocalizedMessage("PERMISSION_INCONSISTENT_VALUE_ERROR");
					}
					catch (ServiceException se)
					{
						errorMessage=null;
					}
					if (errorMessage==null)
					{
						errorMessage="Fatal error. A permission has been assigned a value inconsistent with its type.";
					}
					throw new ServiceException(errorMessage);
				}
			}
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return intValue;
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permission Permission
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	private String getIntegerPermission(Operation operation,User user,Permission permission) throws ServiceException
	{
		String value=null;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			if (user!=null)
			{
				UserPermission userPermission=userPermissionsService.getUserPermission(operation,user,permission);
				if (userPermission==null)
				{
					if (user.getUserType()==null)
					{
						value=permission.getDefaultValue();
					}
					else
					{
						value=getIntegerPermission(operation,user.getUserType(),permission);
					}
				}
				else
				{
					value=userPermission.getValue();
				}
			}
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return value;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerEqual(null,user,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(Operation operation,User user,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)==cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerDistinct(null,user,permissionName,cmpValue); 
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(Operation operation,User user,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)!=cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreater(null,user,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(Operation operation,User user,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)>cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreaterEqual(null,user,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(Operation operation,User user,String permissionName,int cmpValue)
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)>=cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLess(null,user,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(Operation operation,User user,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)<cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(User user,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLessEqual(null,user,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(Operation operation,User user,String permissionName,int cmpValue)
		throws ServiceException
	{
		return getIntegerPermission(operation,user,permissionName)<=cmpValue;
	}
	
	/**
	 * @param user User
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(User user,String permissionName,int minValue,int maxValue) throws ServiceException
	{
		return isIntegerBetween(null,user,permissionName,minValue,maxValue);
	}
	
	/**
	 * @param operation Operation
	 * @param user User
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(Operation operation,User user,String permissionName,int minValue,int maxValue)
		throws ServiceException
	{
		int intValue=getIntegerPermission(operation,user,permissionName);
		return intValue>=minValue && intValue<=maxValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(UserType userType,String permissionName) throws ServiceException
	{
		return getIntegerPermission(null,userType,permissionName);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	public int getIntegerPermission(Operation operation,UserType userType,String permissionName) throws ServiceException
	{
		int intValue=0;
		boolean singleOp=operation==null;
		try
		{
			if (singleOp)
			{
				// Start Hibernate operation
				operation=HibernateUtil.startOperation();
			}
			
			// Get permission value
			String value=getIntegerPermission(operation,userType,checkIntegerPermission(operation,permissionName));
			
			// Check that permission value is integer
			if (value!=null)
			{
				try
				{
					intValue=Integer.parseInt(value);
				}
				catch (NumberFormatException nfe)
				{
					String errorMessage=null;
					try
					{
						errorMessage=localizationService.getLocalizedMessage("PERMISSION_INCONSISTENT_VALUE_ERROR");
					}
					catch (ServiceException se)
					{
						errorMessage=null;
					}
					if (errorMessage==null)
					{
						errorMessage="Fatal error. A permission has been assigned a value inconsistent with its type.";
					}
					throw new ServiceException(errorMessage);
				}
			}
		}
		finally
		{
			if (singleOp)
			{
				// End operation
				HibernateUtil.endOperation(operation);
			}
		}
		return intValue;
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permission Permission
	 * @return Integer value of the permission
	 * @throws ServiceException
	 */
	private String getIntegerPermission(Operation operation,UserType userType,Permission permission) 
		throws ServiceException
	{
		String value=null;
		if (userType!=null)
		{
			UserTypePermission userTypePermission=
				userTypePermissionsService.getUserTypePermission(operation,userType,permission);
			if (userTypePermission==null)
			{
				value=permission.getDefaultValue();
			}
			else
			{
				value=userTypePermission.getValue();
			}
		}
		return value;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerEqual(null,userType,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check equality 
	 * @return true if integer value of the permission is equal to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerEqual(Operation operation,UserType userType,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)==cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerDistinct(null,userType,permissionName,cmpValue); 
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to check inequality 
	 * @return true if integer value of the permission is dictinct to the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerDistinct(Operation operation,UserType userType,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)!=cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreater(null,userType,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreater(Operation operation,UserType userType,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)>cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerGreaterEqual(null,userType,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (greater or equal comparison)
	 * @return true if integer value of the permission is greater or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerGreaterEqual(Operation operation,UserType userType,String permissionName,int cmpValue)
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)>=cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is greater than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLess(null,userType,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLess(Operation operation,UserType userType,String permissionName,int cmpValue) 
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)<cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(UserType userType,String permissionName,int cmpValue) throws ServiceException
	{
		return isIntegerLessEqual(null,userType,permissionName,cmpValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param cmpValue Integer value to perform numeric check (less than comparison)
	 * @return true if integer value of the permission is less or equal than the value indicated, false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerLessEqual(Operation operation,UserType userType,String permissionName,int cmpValue)
		throws ServiceException
	{
		return getIntegerPermission(operation,userType,permissionName)<=cmpValue;
	}
	
	/**
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(UserType userType,String permissionName,int minValue,int maxValue)
		throws ServiceException
	{
		return isIntegerBetween(null,userType,permissionName,minValue,maxValue);
	}
	
	/**
	 * @param operation Operation
	 * @param userType User type
	 * @param permissionName Permission name
	 * @param minValue Minimum value to perform numeric check (greater or equal comparison)
	 * @param maxValue Maximum value to perform numeric check (less or equal comparison)
	 * @return true if integer value of the permission is between the minimum and the maximum values indicated,
	 * false otherwise
	 * @throws ServiceException
	 */
	public boolean isIntegerBetween(Operation operation,UserType userType,String permissionName,int minValue,
		int maxValue) throws ServiceException
	{
		int intValue=getIntegerPermission(operation,userType,permissionName);
		return intValue>=minValue && intValue<=maxValue;
	}
}
