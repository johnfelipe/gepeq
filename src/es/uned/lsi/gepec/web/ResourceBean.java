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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Copyright;
import es.uned.lsi.gepec.model.entities.Resource;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.model.entities.Visibility;
import es.uned.lsi.gepec.util.StringUtils;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.CategoryTypesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.CopyrightsService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.PermissionsService;
import es.uned.lsi.gepec.web.services.ResourcesService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.VisibilitiesService;

//Backbean para la vista recurso
//author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for creating/updating a resource.
 */
@SuppressWarnings("serial")
@ManagedBean(name="resourceBean")
@ViewScoped
public class ResourceBean implements Serializable
{
	public final class Source
	{
		private String source;
		private boolean enabled;
		
		public Source(String source,boolean enabled)
		{
			this.source=source;
			this.enabled=enabled;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof Source && source.equals(((Source)obj).source);
		}
		
		public String getSource()
		{
			return source;
		}
		
		public void setSource(String source)
		{
			this.source=source;
		}
		
		public boolean isEnabled()
		{
			return enabled;
		}
		
		public void setEnabled(boolean enabled)
		{
			this.enabled=enabled;
		}
		
		public String getDisabled()
		{
			return enabled?"false":"true";
		}
	}
	
	private final static int MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW=150;
	private final static int MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW=100;
	
	private final static String RESOURCE_SOURCE_LOCAL="RESOURCE_SOURCE_LOCAL";
	private final static String RESOURCE_SOURCE_REMOTE="RESOURCE_SOURCE_REMOTE";
	
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{resourcesService}")
	private ResourcesService resourcesService;
	@ManagedProperty(value="#{copyrightsService}")
	private CopyrightsService copyrightsService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{categoryTypesService}")
	private CategoryTypesService categoryTypesService;
	@ManagedProperty(value="#{visibilitiesService}")
	private VisibilitiesService visibilitiesService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{permissionsService}")
	private PermissionsService permissionsService;
	
	private Resource resource;											// Resource to create/update
    private UploadedFile file;											// Uploaded file
    private File tmpFile;												// Temporary file used for preview
    private String tmpExt;												// Source extension of temporary file
    
    private String source;
    private String url;
    private String uploadedUrl;
    private byte[] urlContent;
    
    private int width;
    private int height;
    
    private String confirmCancelResourceDialogMessage;
	private String cancelResourceTarget;
	
	private long initialCategoryId;
	
	private List<Category> resourcesCategories;
	private List<Source> resourceSources;
	
	// Copyrights
	private List<Copyright> copyrights;
	
	private Boolean filterGlobalResourcesEnabled;
	private Boolean filterOtherUsersResourcesEnabled;
	private Boolean globalOtherUserCategoryAllowed;
	private Boolean addEnabled;
	private Boolean editEnabled;
	private Boolean editOtherUsersResourcesEnabled;
	private Boolean editAdminsResourcesEnabled;
	private Boolean editSuperadminsResourcesEnabled;
	private Boolean viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	private Boolean viewResourcesFromAdminsPrivateCategoriesEnabled;
	private Boolean viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	private Boolean localSourceAllowed;
	private Boolean networkSourceAllowed;
	private Boolean resourceUserAdmin;
	private Boolean resourceUserSuperadmin;
    private Boolean useBetterUploadSizeLimit;
    private Boolean useBetterMaximumSpaceLimit;
	
	private Integer resourceSizeLimit;
	private Integer resourcesMaximumAvailableSpace;
	private Integer resourcesUsedSpace;
	
	private String decodeUTF8;
	
    public ResourceBean()
    {
    	this(null);
	}
    
    public ResourceBean(Resource resource)
    {
    	this.resource=resource;
    	file=null;
    	tmpFile=null;
    	tmpExt=null;
    	source=null;
    	url="";
    	urlContent=null;
    	width=-1;
    	height=-1;
    	filterGlobalResourcesEnabled=null;
    	filterOtherUsersResourcesEnabled=null;
    	globalOtherUserCategoryAllowed=null;
    	addEnabled=null;
    	editEnabled=null;
    	editOtherUsersResourcesEnabled=null;
    	editAdminsResourcesEnabled=null;
    	editSuperadminsResourcesEnabled=null;
    	viewResourcesFromOtherUsersPrivateCategoriesEnabled=null;
    	viewResourcesFromAdminsPrivateCategoriesEnabled=null;
    	viewResourcesFromSuperadminsPrivateCategoriesEnabled=null;
    	localSourceAllowed=null;
    	networkSourceAllowed=null;
    	resourceUserAdmin=null;
    	resourceUserSuperadmin=null;
        useBetterUploadSizeLimit=null;
        useBetterMaximumSpaceLimit=null;
    	resourceSizeLimit=null;
    	resourcesMaximumAvailableSpace=null;
    	resourcesUsedSpace=null;
    	decodeUTF8="false";
    }
    
    public void setLocalizationService(LocalizationService localizationService)
    {
		this.localizationService=localizationService;
	}
    
	public void setResourcesService(ResourcesService resourcesService)
	{
		this.resourcesService=resourcesService;
	}
	
	public void setCopyrightsService(CopyrightsService copyrightsService)
	{
		this.copyrightsService=copyrightsService;
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
	
	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService=configurationService;
	}
	
    public void setUserSessionService(UserSessionService userSessionService)
    {
		this.userSessionService=userSessionService;
	}
    
    public void setPermissionsService(PermissionsService permissionsService)
    {
    	this.permissionsService=permissionsService;
    }
    
    public Operation getCurrentUserOperation(Operation operation)
    {
    	if (operation!=null && resource==null)
    	{
    		getResource();
    		operation=null;
    	}
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
    
    public UploadedFile getFile()
    {
		return file;
	}
    
	public void setFile(UploadedFile file)
	{
		this.file=file;
	}
    
    public long getResourceCategoryId()
    {
		return getResourceCategoryId(null);
	}
    
	public void setResourceCategoryId(long resourceCategoryId)
	{
		setResourceCategoryId(null,resourceCategoryId);
	}
	
    private long getResourceCategoryId(Operation operation)
    {
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
    	Resource resource=getResource();
    	if (resource.getCategory()==null)
    	{
    		List<Category> resourcesCategories=getResourcesCategories(operation);
    		if (!resourcesCategories.isEmpty())
    		{
    			setResourceCategoryId(resourcesCategories.get(0).getId());
    		}
    	}
		return resource.getCategory()==null?0L:resource.getCategory().getId();
	}
	
	private void setResourceCategoryId(Operation operation,long resourceCategoryId)
	{
		getResource().setCategory(resourceCategoryId>0L?
			categoriesService.getCategory(getCurrentUserOperation(operation),resourceCategoryId):null);
	}
    
    public String getSource()
    {
		return getSource(null);
	}
    
	public void setSource(String source)
	{
		this.source=source;
	}
	
    private String getSource(Operation operation)
    {
    	if (source==null)
    	{
    		for (Source s:getResourceSources(getCurrentUserOperation(operation)))
    		{
    			if (s.isEnabled())
    			{
    				source=s.getSource();
    				break;
    			}
    		}
    	}
		return source;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url=url;
	}
	
	public String getResourceMimeType()
	{
		return getResource().getMimeType();
	}
	
	public void setResourceMimeType(String resourceMimeType)
	{
		getResource().setMimeType(resourceMimeType);
	}
	
    public long getResourceCopyrightId()
    {
		return getResourceCopyrightId(null);
	}
    
	public void setResourceCopyrightId(long resourceCopyrightId)
	{
		setResourceCopyrightId(null,resourceCopyrightId);
	}
	
    private long getResourceCopyrightId(Operation operation)
    {
    	Resource resource=getResource();
    	if (resource.getCopyright()==null)
    	{
    		List<Copyright> copyrights=getCopyrights(getCurrentUserOperation(operation));
    		if (!copyrights.isEmpty())
    		{
    			setResourceCopyrightId(copyrights.get(0).getId());
    		}
    	}
		return resource.getCopyright().getId();
	}
	
	private void setResourceCopyrightId(Operation operation,long resourceCopyrightId)
	{
		getResource().setCopyright(
			copyrightsService.getCopyright(getCurrentUserOperation(operation),resourceCopyrightId));
	}
	
	public String getDecodeUTF8()
	{
		return decodeUTF8;
	}
	
	public void setDecodeUTF8(String decodeUTF8)
	{
		this.decodeUTF8=decodeUTF8;
	}
	
	public String getUploadedUrl()
	{
		return uploadedUrl;
	}
	
	public void setUploadedUrl(String uploadUrl)
	{
		this.uploadedUrl=uploadUrl;
	}
	
	public byte[] getUrlContent()
	{
		return urlContent;
	}
	
	public void setUrlContent(byte[] urlContent)
	{
		this.urlContent=urlContent;
	}
	
	/**
	 * @return List of categories for resources from current user or globals
	 */
	public List<Category> getResourcesCategories()
    {
		return getResourcesCategories(null); 
	}
	
	public void setResourcesCategories(List<Category> resourcesCategories)
	{
		this.resourcesCategories=resourcesCategories;
	}
	
	/**
	 * @param operation Operation
	 * @return List of categories for resources from current user or globals
	 */
	private List<Category> getResourcesCategories(Operation operation)
    {
		if (resourcesCategories==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Resource resource=getResource();
			User resourceUser=resource.getId()>0L?resource.getUser():userSessionService.getCurrentUser(operation);
			
			//TODO el tipo de categoría CATEGORY_TYPE_IMAGES en realidad esta pensado solo para imagenes, cambiarlo por otro más genérico como CATEGORY_TYPE_RESOURCES cuando este implementado
			resourcesCategories=categoriesService.getCategoriesSortedByHierarchy(operation,resourceUser,
				categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),true,true,
				CategoriesService.NOT_VIEW_OTHER_USERS_CATEGORIES);
			
			// Remove from list categories not allowed
			List<Category> resourcesCategoriesToRemove=new ArrayList<Category>();
			for (Category resourceCategory:resourcesCategories)
			{
				if (!checkCategory(operation,resourceCategory))
				{
					resourcesCategoriesToRemove.add(resourceCategory);
				}
			}
			for (Category resourceCategoryToRemove:resourcesCategoriesToRemove)
			{
				resourcesCategories.remove(resourceCategoryToRemove);
			}
		}
		return resourcesCategories; 
	}
	
	/**
	 * @return All copyrights
	 */
	public List<Copyright> getCopyrights()
	{
		return getCopyrights(null);
	}
	
	/**
	 * @param operation Operation
	 * @return All copyrights
	 */
	private List<Copyright> getCopyrights(Operation operation)
	{
		if (copyrights==null)
		{
			copyrights=copyrightsService.getCopyrights(getCurrentUserOperation(operation));
		}
		return copyrights;
	}
	
	
	public void setCopyrights(List<Copyright> copyrights)
	{
		this.copyrights=copyrights;
	}
	
	/**
	 * @return List of all allowed sources to upload resources to GEPEQ
	 */
    public List<Source> getResourceSources()
    {
    	return getResourceSources(null);
    }
    
    private List<Source> getResourceSources(Operation operation)
    {
    	if (resourceSources==null)
    	{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			resourceSources=new ArrayList<Source>();
        	setLocalSourceAllowed(null);
        	resourceSources.add(new Source(RESOURCE_SOURCE_LOCAL,getLocalSourceAllowed(operation).booleanValue()));
        	setNetworkSourceAllowed(null);
        	resourceSources.add(new Source(RESOURCE_SOURCE_REMOTE,getNetworkSourceAllowed(operation).booleanValue()));
    	}
    	return resourceSources;
    }
    
    public void setResourceSource(List<Source> resourceSources)
    {
    	this.resourceSources=resourceSources;
    }
    
    public boolean isAllSourcesDenied()
    {
    	return isAllSourcesDenied(null);
    }
    
    private boolean isAllSourcesDenied(Operation operation)
    {
    	boolean allSourcesDenied=true;
    	for (Source source:getResourceSources(getCurrentUserOperation(operation)))
    	{
    		if (source.isEnabled())
    		{
    			allSourcesDenied=false;
    			break;
    		}
    	}
    	return allSourcesDenied;
    }
    
    public String getAllSourcesDeniedMessageStyle()
    {
    	return isUpdate()?"ui-messages-warn ui-corner-all":"ui-messages-error ui-corner-all";
    }
    
    public String getAllSourcesDeniedMessageIconStyle()
    {
    	return isUpdate()?"ui-messages-warn-icon":"ui-messages-error-icon";
    }
    
    public String getAllSourcesDeniedMessageSummaryStyle()
    {
    	return isUpdate()?"ui-messages-warn-summary":"ui-messages-error-summary";
    }
    
    public Boolean getUseBetterUploadSizeLimit()
    {
    	return getUseBetterUploadSizeLimit(null);
    }
    
    public void setUseBetterUploadSizeLimit(Boolean useBetterUploadSizeLimit)
    {
    	this.useBetterUploadSizeLimit=useBetterUploadSizeLimit;
    }
    
    public boolean isUseBetterUploadSizeLimit()
    {
    	return getUseBetterUploadSizeLimit().booleanValue();
    }
    
    private Boolean getUseBetterUploadSizeLimit(Operation operation)
    {
    	if (useBetterUploadSizeLimit==null)
    	{
    		useBetterUploadSizeLimit=Boolean.valueOf(userSessionService.isGranted(
    			getCurrentUserOperation(operation),"PERMISSION_RESOURCE_OTHER_USER_USE_BETTER_UPLOAD_SIZE_LIMIT"));
    	}
    	return useBetterUploadSizeLimit;
    }
    
    public Boolean getUseBetterMaximumSpaceLimit()
    {
    	return getUseBetterMaximumSpaceLimit(null);
    }
    
    public void setUseBetterMaximumSpaceLimit(Boolean useBetterMaximumSpaceLimit)
    {
    	this.useBetterMaximumSpaceLimit=useBetterMaximumSpaceLimit;
    }
    
    public boolean isUseBetterMaximumSpaceLimit()
    {
    	return getUseBetterMaximumSpaceLimit().booleanValue();
    }
    
    private Boolean getUseBetterMaximumSpaceLimit(Operation operation)
    {
    	if (useBetterMaximumSpaceLimit==null)
    	{
    		useBetterMaximumSpaceLimit=Boolean.valueOf(userSessionService.isGranted(
    			getCurrentUserOperation(operation),
    			"PERMISSION_RESOURCE_OTHER_USER_USE_BETTER_MAXIMUM_SPACE_LIMIT"));
    	}
    	return useBetterMaximumSpaceLimit;
    }
    
    public Integer getResourceSizeLimit()
    {
    	return getResourceSizeLimit(null);
    }
    
    public void setResourceSizeLimit(Integer resourceSizeLimit)
    {
    	this.resourceSizeLimit=resourceSizeLimit;
    }
    
    private Integer getResourceSizeLimit(Operation operation)
    {
    	if (resourceSizeLimit==null)
    	{
        	// Get current user session Hibernate operation
        	operation=getCurrentUserOperation(operation);
    		
        	Resource resource=getResource();
        	if (resource.getId()>0L)
        	{
            	resourceSizeLimit=Integer.valueOf(permissionsService.getIntegerPermission(
            		operation,resource.getUser(),"PERMISSION_RESOURCE_UPLOAD_SIZE_LIMIT"));
           		setUseBetterUploadSizeLimit(null);
           		if (getUseBetterMaximumSpaceLimit(operation).booleanValue())
           		{
           			int currentUserResourceSizeLimit=
           				userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_UPLOAD_SIZE_LIMIT");
           			if (currentUserResourceSizeLimit<=0)
           			{
           				resourceSizeLimit=Integer.valueOf(0);
           			}
           			else if (currentUserResourceSizeLimit>resourceSizeLimit.intValue())
           			{
           				resourceSizeLimit=Integer.valueOf(currentUserResourceSizeLimit);
           			}
           		}
       		}
       		else
       		{
           		resourceSizeLimit=Integer.valueOf(
           			userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_UPLOAD_SIZE_LIMIT"));
       		}
    	}
    	return resourceSizeLimit;
    }
    
    public Integer getResourcesMaximumAvailableSpace()
    {
    	return getResourcesMaximumAvailableSpace(null);
    }
    
    public void setResourcesMaximumAvailableSpace(Integer resourcesMaximumAvailableSpace)
    {
    	this.resourcesMaximumAvailableSpace=resourcesMaximumAvailableSpace;
    }
    
    private Integer getResourcesMaximumAvailableSpace(Operation operation)
    {
    	if (resourcesMaximumAvailableSpace==null)
    	{
        	// Get current user session Hibernate operation
        	operation=getCurrentUserOperation(operation);
    		
        	Resource resource=getResource();
        	if (resource.getId()>0L)
        	{
        		resourcesMaximumAvailableSpace=Integer.valueOf(permissionsService.getIntegerPermission(
        			operation,resource.getUser(),"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT"));
        		setUseBetterMaximumSpaceLimit(null);
        		if (getUseBetterMaximumSpaceLimit(operation).booleanValue())
        		{
        			int currentUserMaximumAvailableSpace=Integer.valueOf(
        				userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT"));
       				if (currentUserMaximumAvailableSpace<=0)
       				{
       					resourcesMaximumAvailableSpace=Integer.valueOf(0);
       				}
       				else if (currentUserMaximumAvailableSpace>resourceSizeLimit.intValue())
       				{
       					resourcesMaximumAvailableSpace=Integer.valueOf(currentUserMaximumAvailableSpace);
       				}
       			}
       		}
       		else
       		{
           		resourcesMaximumAvailableSpace=Integer.valueOf(
           			userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT"));
       		}
    	}
    	return resourcesMaximumAvailableSpace;
    }
    
    public Integer getResourcesUsedSpace()
    {
    	return getResourcesUsedSpace(null);
    }
    
    public void setResourcesUsedSpace(Integer resourcesUsedSpace)
    {
    	this.resourcesUsedSpace=resourcesUsedSpace;
    }
    
    private Integer getResourcesUsedSpace(Operation operation)
    {
    	if (resourcesUsedSpace==null)
    	{
    		// Get current user session Hibernate operation
    		operation=getCurrentUserOperation(operation);
    		
        	int usedSpace=0;
        	List<Resource> resources=null;
        	Resource resource=getResource();
        	if (resource.getId()>0L)
        	{
        		resources=resourcesService.getResources(operation,resource.getUser());
        	}
        	else
        	{
        		resources=resourcesService.getResources(operation,userSessionService.getCurrentUser(operation));
        	}
        	for (Resource res:resources)
        	{
        		if (!res.equals(resource))
        		{
        			File resourceFile=new File(resourcesService.getResourceFilePath(res));
        			if (resourceFile.exists())
        			{
        				usedSpace+=(int)resourceFile.length();
        			}
        		}
        	}
        	resourcesUsedSpace=Integer.valueOf(usedSpace);
    	}
    	return resourcesUsedSpace;
    }
    
    public String getErrorMessageSizeLimit()
    {
    	return getErrorMessageSizeLimit(null);
    }
    
    private String getErrorMessageSizeLimit(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	Resource resource=getResource();
        String errorKey=null;
        if (resource.getId()>0L)
        {
        	User currentUser=userSessionService.getCurrentUser(operation);
        	User resourceUser=resource.getUser();
        	if (resourceUser.equals(currentUser))
        	{
        		errorKey="FILE_SIZE_LIMIT";
        	}
        	else if (getUseBetterUploadSizeLimit(operation).booleanValue())
        	{
    			int currentUserSizeLimit=
    				userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT");
       			if (currentUserSizeLimit<=0)
       			{
           			errorKey="FILE_SIZE_LIMIT";
       			}
       			else
       			{
           			int resourceUserSizeLimit=permissionsService.getIntegerPermission(
           				operation,resourceUser,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT");
           			if (resourceUserSizeLimit<=0 || resourceUserSizeLimit>currentUserSizeLimit)
           			{
           				errorKey="OTHER_USER_FILE_SIZE_LIMIT";
           			}
           			else
           			{
           				errorKey="FILE_SIZE_LIMIT";
           			}
       			}
       		}
       		else
       		{
       			errorKey="OTHER_USER_FILE_SIZE_LIMIT";
       		}
       	}
       	else
       	{
       		errorKey="FILE_SIZE_LIMIT";
       	}
       	int resourceSizeLimit=getResourceSizeLimit(operation).intValue();
       	String units="bytes";
       	if (resourceSizeLimit%1024==0)
       	{
       		resourceSizeLimit=resourceSizeLimit/1024;
       		units="Kb";
       		if (resourceSizeLimit%1024==0)
       		{
       			resourceSizeLimit=resourceSizeLimit/1024;
       			units="Mb";
       			if (resourceSizeLimit%1024==0)
       			{
       				resourceSizeLimit=resourceSizeLimit/1024;
       				units="Gb";
       			}
       		}
       	}
       	StringBuffer resourceSizeLimitWithUnits=new StringBuffer();
       	resourceSizeLimitWithUnits.append(resourceSizeLimit);
       	resourceSizeLimitWithUnits.append(' ');
       	resourceSizeLimitWithUnits.append(units);
       	
    	return localizationService.getLocalizedMessage(errorKey).replace("?",resourceSizeLimitWithUnits.toString()); 
    }
    
    public boolean isResourceSizeUnlimited()
    {
    	return isResourceSizeUnlimited(null);
    }
    
    private boolean isResourceSizeUnlimited(Operation operation)
    {
    	return getResourceSizeLimit(getCurrentUserOperation(operation))<=0;
    }
    
    public String getErrorMessageResourcesSpaceLimit()
    {
    	return getErrorMessageResourcesSpaceLimit(null);
    }
    
    private String getErrorMessageResourcesSpaceLimit(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
       	Resource resource=getResource();
    	String errorKey=null;
        if (resource.getId()>0L)
        {
        	User currentUser=userSessionService.getCurrentUser(operation);
        	User resourceUser=resource.getUser();
        	if (resourceUser.equals(currentUser))
        	{
        		errorKey="RESOURCES_SPACE_LIMIT";
        	}
        	else if (getUseBetterMaximumSpaceLimit(operation).booleanValue())
        	{
        		int currentUserMaximumSpaceLimit=
        			userSessionService.getIntegerPermission(operation,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT");
        		if (currentUserMaximumSpaceLimit<=0)
        		{
        			errorKey="RESOURCES_SPACE_LIMIT";
        		}
        		else
        		{
        			int resourceUserMaximumSpaceLimit=permissionsService.getIntegerPermission(
        				operation,resourceUser,"PERMISSION_RESOURCE_MAXIMUM_SPACE_LIMIT");
        			if (resourceUserMaximumSpaceLimit<=0 || resourceUserMaximumSpaceLimit>currentUserMaximumSpaceLimit)
        			{
               			errorKey="OTHER_USER_RESOURCES_SPACE_LIMIT";
        			}
        			else
        			{
           				errorKey="RESOURCES_SPACE_LIMIT";
        			}
        		}
        	}
        	else
        	{
        		errorKey="OTHER_USER_RESOURCES_SPACE_LIMIT";
        	}
        }
        else
        {
        	errorKey="RESOURCES_SPACE_LIMIT";
        }
        int freeSpace=
        	getResourcesMaximumAvailableSpace(operation).intValue()-getResourcesUsedSpace(operation).intValue();
        if (freeSpace<0)
        {
       		freeSpace=0;
       	}
       	String units="bytes";
       	if (freeSpace%1024==0)
       	{
       		freeSpace=freeSpace/1024;
       		units="Kb";
       		if (freeSpace%1024==0)
       		{
       			freeSpace=freeSpace/1024;
       			units="Mb";
       			if (freeSpace%1024==0)
       			{
       				freeSpace=freeSpace/1024;
       				units="Gb";
       			}
       		}
       	}
       	StringBuffer freeSpaceWithUnits=new StringBuffer();
       	freeSpaceWithUnits.append(freeSpace);
       	freeSpaceWithUnits.append(' ');
       	freeSpaceWithUnits.append(units);
       	
    	return localizationService.getLocalizedMessage(errorKey).replace("?",freeSpaceWithUnits.toString()); 
    }
    
	//Obtiene un recurso dependiendo de si se trata o no de una actualización
    //return Nuevo recurso si no es una actualización y recurso buscado si lo es
    /**
     * @return Searched resource if we are updating, a new resource otherwise 
     */
    public Resource getResource()
    {
    	if (resource==null)
    	{
			// End current user session Hibernate operation
			userSessionService.endCurrentUserOperation();
    		
    		// Get current user session Hibernate operation
    		Operation operation=getCurrentUserOperation(null);
    		
    		FacesContext context=FacesContext.getCurrentInstance();
    		Map<String,String> params=context.getExternalContext().getRequestParameterMap();
    		if (params.containsKey("resourceId"))						// Update resource
    		{
    			resource=resourcesService.getResource(operation,Long.parseLong(params.get("resourceId")));
    	    	initialCategoryId=resource.getCategory().getId();
    			setConfirmCancelResourceDialogMessage("CONFIRM_CANCEL_RESOURCE_UPDATE");
    		}
    		else														// New resource
    		{
    			resource=new Resource();
    			initialCategoryId=0L;
    			setConfirmCancelResourceDialogMessage("CONFIRM_CANCEL_RESOURCE");
    		}
    	}
		return resource;
	}
	
    //Indica si ya se ha subido un fichero de recurso
    //return Cierto si hay fichero y falso si no
    /**
     * @return true if there is an uploaded file, false otherwise
     */
	public boolean isUploaded()
	{
		return getFile()!=null || getUrlContent()!=null;
	}
	
    //Indica si se trata de una actualización
    //return Cierto si tenemos id y falso si no
	/**
	 * @return true if we are updating an existing resource, false if we are creating a new one
     */
	public boolean isUpdate()
	{
		return getResource().getId()>0L;
	}
	
	public boolean isUploadURLEnabled()
	{
		return getUrl()!=null && !"".equals(getUrl());
	}
	
	public boolean isSaveEnabled()
	{
		Resource resource=getResource();
		return resource!=null && resource.getName()!=null && !resource.getName().equals("") && 
			(isUpdate() || isUploaded());
	}
	
	public int getResourcePreviewWidth()
	{
		return getResourcePreviewWidth(null);
	}
	
	private int getResourcePreviewWidth(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		int width=getWidth(operation);
		int height=getHeight(operation);
		if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
		{
			if ((double)width/(double)height>=
				(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
			{
				width=MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW;
			}
			else
			{
				width=(width*MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)/height;
			}
		}
		return width>=1?width:1;
	}
	
	public int getResourcePreviewHeight()
	{
		return getResourcePreviewHeight(null);
	}
	
	private int getResourcePreviewHeight(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		int width=getWidth(operation);
		int height=getHeight(operation);
		if (width>MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW || height>MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
		{
			if ((double)width/(double)height>=
				(double)MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW/(double)MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW)
			{
				height=(height*MAX_IMAGE_WIDTH_FOR_RESOURCE_PREVIEW)/width;
			}
			else
			{
				height=MAX_IMAGE_HEIGHT_FOR_RESOURCE_PREVIEW;
			}
		}
		return height>=1?height:1;
	}
	
	/**
	 * Process some input fields of the resource page.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processNotWatchedInputFields(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput resourceName=(UIInput)component.findComponent(":resourceForm:resourceName");
		resourceName.processDecodes(context);
		if (resourceName.getSubmittedValue()!=null)
		{
			getResource().setName((String)resourceName.getSubmittedValue());
		}
		UIInput resourceDescription=(UIInput)component.findComponent(":resourceForm:resourceDescription");
		resourceDescription.processDecodes(context);
		if (resourceDescription.getSubmittedValue()!=null)
		{
			getResource().setDescription((String)resourceDescription.getSubmittedValue());
		}
		UIInput resourceCopyright=(UIInput)component.findComponent(":resourceForm:resourceCopyright");
		resourceCopyright.processDecodes(context);
		if (resourceCopyright.getSubmittedValue()!=null)
		{
			if (resourceCopyright.getSubmittedValue() instanceof Long)
			{
				setResourceCopyrightId(((Long)resourceCopyright.getSubmittedValue()).longValue());
			}
			else if (resourceCopyright.getSubmittedValue() instanceof String)
			{
				setResourceCopyrightId(Long.parseLong((String)resourceCopyright.getSubmittedValue()));
			}
		}
		UIInput resourceMimeType=(UIInput)component.findComponent(":resourceForm:resourceMimeType");
		resourceMimeType.processDecodes(context);
		if (resourceMimeType.getSubmittedValue()!=null)
		{
			setResourceMimeType((String)resourceMimeType.getSubmittedValue());
		}
	}
	
	/**
	 * Process MIME type input field of the resource page.
	 * @param component Component that triggered the listener that called this method
	 */
	private void processResourceMimeType(UIComponent component)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		UIInput resourceMimeType=(UIInput)component.findComponent(":resourceForm:resourceMimeType");
		resourceMimeType.processDecodes(context);
		if (resourceMimeType.getSubmittedValue()!=null)
		{
			setResourceMimeType((String)resourceMimeType.getSubmittedValue());
		}
	}
	
	/**
	 * Convert some bean properties to UTF-8.<br/><br/>
	 * Note that this is needed because form is multipart/form-data and encodes strings with the default system 
	 * charset.
	 */
	private void processUTF8()
	{
		Resource resource=getResource();
		if (resource!=null)
		{
		    try
		    {
		    	resource.setName(new String(resource.getName().getBytes(),"UTF-8"));
		    }
		    catch (UnsupportedEncodingException e)
			{
			}
		    try
		    {
		    	resource.setDescription(new String(resource.getDescription().getBytes(),"UTF-8"));
		    }
		    catch (UnsupportedEncodingException e)
			{
			}
		    setDecodeUTF8("false");
		}
	}
	
	//ActionListener para el evento de subida completada
	/**
	 * Listener for file upload event
	 * @param event File upload event
	 * @throws IOException
	 */
	public void handleFileUpload(FileUploadEvent event) throws IOException
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		Operation operation=getCurrentUserOperation(null);
		
		// We check that we have permission to upload a resource from local file system
		setLocalSourceAllowed(null);
		if (!getLocalSourceAllowed(operation).booleanValue())
		{
			resourcesService.deleteResourceFile(tmpFile);
			setFile(null);
			setUrl(null);
			setUploadedUrl(null);
			setUrlContent(null);
			tmpFile=null;
			addErrorMessage("LOCAL_SOURCE_DENIED");
			ok=false;
		}
		else
		{
			// Check file length (because our resource size limit could have been changed by an administrator)
			setResourceSizeLimit(null);
			int resourceSizeLimit=getResourceSizeLimit(operation).intValue();
			if (resourceSizeLimit>0 && (int)event.getFile().getSize()>resourceSizeLimit)
			{
				resourcesService.deleteResourceFile(tmpFile);
				setFile(null);
				setUrl(null);
				setUploadedUrl(null);
				setUrlContent(null);
				tmpFile=null;
				addPlainErrorMessage(getErrorMessageSizeLimit(operation));
				ok=false;
			}
			// Check that resources used space is below resources maximum available space
			if (ok)
			{
				setResourcesMaximumAvailableSpace(null);
				int resourcesMaximumAvailableSpace=getResourcesMaximumAvailableSpace(operation).intValue();
				if (resourcesMaximumAvailableSpace>0)
				{
					setResourcesUsedSpace(null);
					if ((int)event.getFile().getSize()+getResourcesUsedSpace(operation).intValue()>
						resourcesMaximumAvailableSpace)
					{
						resourcesService.deleteResourceFile(tmpFile);
						setFile(null);
						setUrl(null);
						setUploadedUrl(null);
						setUrlContent(null);
						tmpFile=null;
						addPlainErrorMessage(getErrorMessageResourcesSpaceLimit(operation));
						ok=false;
					}
				}
			}
			if (ok)
			{
				// We need to process MIME type input field
				processResourceMimeType(event.getComponent());
				
				setFile(event.getFile());
				setUrlContent(null);
				int iExt=getFile().getFileName().lastIndexOf('.');
				if (iExt<getFile().getFileName().length()-1)
				{
					tmpExt=getFile().getFileName().substring(iExt+1);
				}
				else
				{
					tmpExt="";
				}
				
				width=-1;
				height=-1;
				saveTemporaryFile();
				
				// We check that the MIME type of the file is the expected one or the default used by that extension
				String checkedMimeType=resourcesService.checkMimeType(tmpFile,tmpExt,getResourceMimeType());
				if (checkedMimeType==null)
				{
					resourcesService.deleteResourceFile(tmpFile);
					setFile(null);
					tmpFile=null;
					addErrorMessage("FILE_UNEXPECTED_TYPE");
					ok=false;
				}
				else
				{
					// We set actual MIME type of the file
					setResourceMimeType(checkedMimeType);
				}
			}
		}
		if (!ok)
		{
			// Scroll page to top position
			scrollToTop();
		}
	}
	
	/**
	 * Listener for URL upload event
	 * @param event Action event
	 */
	public void handleUrlUpload(ActionEvent event)
	{
		if (isUploadURLEnabled())
		{
			boolean ok=true;
			
			// Get current user session Hibernate operation
			Operation operation=getCurrentUserOperation(null);
			
			// We check that we have permission to upload a resource from an URL
			setNetworkSourceAllowed(null);
			if (!getNetworkSourceAllowed(operation).booleanValue())
			{
				resourcesService.deleteResourceFile(tmpFile);
				setFile(null);
				setUrl(null);
				setUploadedUrl(null);
				setUrlContent(null);
				tmpFile=null;
				addErrorMessage("NETWORK_SOURCE_DENIED");
				ok=false;
			}
			else
			{
				// We need to process MIME type input field
				processResourceMimeType(event.getComponent());
				
				try
				{
					byte[] uploadedContent=
						resourcesService.uploadFromURL(getUrl(),getResourceSizeLimit(operation).intValue());
					if (uploadedContent!=null)
					{
						// Check that resources used space is below resources maximum available space
						setResourcesMaximumAvailableSpace(null);
						int resourcesMaximumAvailableSpace=getResourcesMaximumAvailableSpace(operation).intValue();
						if (resourcesMaximumAvailableSpace>0)
						{
							setResourcesUsedSpace(null);
							if (uploadedContent.length+getResourcesUsedSpace(operation).intValue()>
								resourcesMaximumAvailableSpace)
							{
								resourcesService.deleteResourceFile(tmpFile);
								setFile(null);
								setUrl(null);
								setUploadedUrl(null);
								setUrlContent(null);
								tmpFile=null;
								addPlainErrorMessage(getErrorMessageResourcesSpaceLimit(operation));
								ok=false;
							}
						}
						if (ok)
						{
							setUploadedUrl(getUrl());
							setUrlContent(uploadedContent);
							int iExt=getUploadedUrl().lastIndexOf('.');
							if (iExt<getUploadedUrl().length()-1)
							{
								tmpExt=getUploadedUrl().substring(iExt+1);
							}
							else
							{
								tmpExt="";
							}
							setFile(null);
							width=-1;
							height=-1;
							saveTemporaryFile();
							
							// We check that the MIME type of the file is the expected one 
							// or the default used by that extension
							String checkedMimeType=
								resourcesService.checkMimeType(tmpFile,tmpExt,getResourceMimeType());
							if (checkedMimeType==null)
							{
								resourcesService.deleteResourceFile(tmpFile);
								setUrl(null);
								setUploadedUrl(null);
								setUrlContent(null);
								tmpFile=null;
								addErrorMessage("FILE_UNEXPECTED_TYPE");
								ok=false;
							}
							else
							{
								// We set actual MIME type of the file
								setResourceMimeType(checkedMimeType);
							}
						}
					}
				}
				catch (MalformedURLException e)
				{
					addErrorMessage("URL_MARLFORMED");
					ok=false;
				}
				catch (IOException e)
				{
					addErrorMessage("URL_ACCESS_ERROR");
					ok=false;
				}
				catch (Exception e)
				{
					if (e.getMessage().equals("FILE_SIZE_LIMIT"))
					{
						addPlainErrorMessage(getErrorMessageSizeLimit(operation));
						ok=false;
					}
				}
			}
			if (!ok)
			{
				// Scroll page to top position
				scrollToTop();
			}
		}
	}
	
	/**
	 * @return true if resource name only includes valid characters (letters, digits, whitespaces 
	 * or any of the following characters  _ ( ) [ ] { } + - * /<br/>
	 * ), false otherwise
	 * 
	 */
	private boolean checkValidCharactersForResourceName()
	{
		return !StringUtils.hasUnexpectedCharacters(
			getResource().getName(),true,true,true,new char[]{'_','(',')','[',']','{','}','+','-','*','/'});
	}
	
	/**
	 * @return true if resource name includes at least one letter, false otherwise
	 */
	private boolean checkLetterIncludedForResourceName()
	{
		return StringUtils.hasLetter(getResource().getName());
	}
	
	/**
	 * @return true if first character of resource name is not a digit nor a whitespace, false otherwise
	 */
	private boolean checkFirstCharacterNotDigitNotWhitespaceForResourceName()
	{
		String resourceName=getResource().getName();
		return !StringUtils.isFirstCharacterDigit(resourceName) && 
			!StringUtils.isFirstCharacterWhitespace(resourceName);
	}
	
	/**
	 * @return true if last character of resource name is not a whitespace, false otherwise
	 */
	private boolean checkLastCharacterNotWhitespaceForResourceName()
	{
		return !StringUtils.isLastCharacterWhitespace(getResource().getName());
	}
	
	/**
	 * @return true if resource name does not include consecutive whitespaces, false otherwise
	 */
	private boolean checkNonConsecutiveWhitespacesForResourceName()
	{
		return !StringUtils.hasConsecutiveWhitespaces(getResource().getName());
	}
	
	/**
	 * @param operation Operation
	 * @return true if resource name is valid, false otherwise
	 */
	private boolean checkResourceName()
	{
		boolean ok=true;
		
		if (!checkValidCharactersForResourceName())
		{
			addErrorMessage("RESOURCE_NAME_INVALID_CHARACTERS");
			ok=false;
		}
		if (!checkLetterIncludedForResourceName())
		{
			addErrorMessage("RESOURCE_NAME_WITHOUT_LETTER");
			ok=false;
		}
		if (!checkFirstCharacterNotDigitNotWhitespaceForResourceName())
		{
			addErrorMessage("RESOURCE_NAME_FIRST_CHARACTER_INVALID");
			ok=false;
		}
		if (!checkLastCharacterNotWhitespaceForResourceName())
		{
			addErrorMessage("RESOURCE_NAME_LAST_CHARACTER_INVALID");
			ok=false;
		}
		if (!checkNonConsecutiveWhitespacesForResourceName())
		{
			addErrorMessage("RESOURCE_NAME_WITH_CONSECUTIVE_WHITESPACES");
			ok=false;
		}
		return ok;
	}
	
	/**
	 * @param operation Operation
	 * @return true if category selected is usable by current user, false otherwise
	 */
    private boolean checkCategory(Operation operation)
    {
    	return checkCategory(getCurrentUserOperation(operation),getResource().getCategory());
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
    		categoryTypesService.getCategoryType(operation,"CATEGORY_TYPE_IMAGES"),
    		categoryTypesService.getCategoryTypeFromCategoryId(operation,category.getId()));
    	
    	// Check visibility
    	if (ok)
    	{
    		ok=false;
    		Resource resource=getResource();
    		User resourceUser=resource.getId()>0L?resource.getUser():userSessionService.getCurrentUser(operation);
    		Visibility categoryVisibility=
    			visibilitiesService.getVisibilityFromCategoryId(operation,category.getId());
    		if (categoryVisibility.isGlobal())	
    		{
    			ok=getFilterGlobalResourcesEnabled(operation).booleanValue() && 
    				((initialCategoryId>0L && category.getId()==initialCategoryId) || 
    				resourceUser.equals(category.getUser()) || 
    				getGlobalOtherUserCategoryAllowed(operation).booleanValue());
    		}
    		else if (resourceUser.equals(category.getUser()))
    		{
    			if (resourceUser.getId()==userSessionService.getCurrentUserId())
    			{
    				ok=true;
    			}
    			else if (getFilterOtherUsersResourcesEnabled(operation).booleanValue()) 
    			{
    				if (categoryVisibility.getLevel()>=visibilitiesService.getVisibility(
    					operation,"CATEGORY_VISIBILITY_PRIVATE").getLevel())
    				{
    					ok=getViewResourcesFromOtherUsersPrivateCategoriesEnabled(operation).booleanValue() && 
    						(!getResourceUserAdmin(operation).booleanValue() || 
    						getViewResourcesFromAdminsPrivateCategoriesEnabled(operation).booleanValue()) && 
    						(!getResourceUserSuperadmin(operation).booleanValue() || 
    						getViewResourcesFromSuperadminsPrivateCategoriesEnabled(operation).booleanValue());
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
	 * @return true if current category of the resource we are editing is usable by current user, false otherwise
	 */
    private boolean checkCurrentCategory(Operation operation)
    {
    	// Get current user session Hibernate operation
    	operation=getCurrentUserOperation(operation);
    	
    	return checkCategory(operation,categoriesService.getCategoryFromResourceId(operation,getResource().getId()));
    }
    
    
    /**
	 * @param operation Operation
	 * @return true if resource name entered by user is available, false otherwise 
	 */
	private boolean checkAvailableResourceName(Operation operation)
	{
		boolean ok=true;
		
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		Resource resource=getResource();
		String resourceName=resource.getName();
		long categoryId=resource.getCategory()==null?0L:resource.getCategory().getId();
		if (resourceName!=null)
		{
			ok=resourcesService.isResourceNameAvailable(operation,resource.getName(),categoryId,resource.getId());
		}
		return ok;
	}
    
	/**
	 * Refresh available categories of the combo box.
	 * @param event Action event
	 */
	public void refreshResourceCategories(ActionEvent event)
	{
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalResourcesEnabled(null);
		setFilterOtherUsersResourcesEnabled(null);
	    setGlobalOtherUserCategoryAllowed(null);
	    setResourceUserAdmin(null);
	    setResourceUserSuperadmin(null);
	    setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	    setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	    setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
	    long resourceCategoryId=getResource().getCategory().getId();
	    if (!categoriesService.checkCategoryId(operation,resourceCategoryId) || !checkCategory(operation))
	    {
	    	// Refresh categories from DB
	    	resetResourcesCategories(operation);
	    }
	    else
	    {
			// Reload categories from DB
			setResourcesCategories(null);
	    }
	}
	
	private void resetResourcesCategories(Operation operation)
	{
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
		// Reload categories from DB
		setResourcesCategories(null);
		
		// Check that initial category already exists and it is valid
		long resetCategoryId=0L;
		if (initialCategoryId>0L)
		{
			if (!categoriesService.checkCategoryId(operation,initialCategoryId))
			{
				initialCategoryId=0L;
			}
			else if (checkCategory(operation,categoriesService.getCategory(operation,initialCategoryId)))
			{
				resetCategoryId=initialCategoryId;
			}
		}
		
		// Reset selected category
		if (resetCategoryId==0L)
		{
			List<Category> resourcesCategories=getResourcesCategories(operation);
			if (!resourcesCategories.isEmpty())
			{
				resetCategoryId=resourcesCategories.get(0).getId();
			}
		}
		setResourceCategoryId(operation,resetCategoryId);
	}
	
	private boolean checkSaveResource(Operation operation)
	{
		boolean ok=false;
		
		// Get current user session operation
		operation=getCurrentUserOperation(operation);
		
   		Resource resource=getResource();
    	if (resource.getId()>0L)
    	{
    		ok=false;
    		if (getEditEnabled(operation).booleanValue())
    		{
    			if (resource.getUser().getId()==userSessionService.getCurrentUserId())
    			{
    				ok=true;
    			}
    			else
    			{
    				ok=getEditOtherUsersResourcesEnabled(operation).booleanValue() && 
    					(!getResourceUserAdmin(operation).booleanValue() || 
    					getEditAdminsResourcesEnabled(operation).booleanValue()) && 
    					(!getResourceUserSuperadmin(operation).booleanValue() || 
    					getEditSuperadminsResourcesEnabled(operation).booleanValue());
    			}
    		}
    	}
    	else
    	{
    		ok=getAddEnabled(operation).booleanValue();
   		}
		return ok;
	}
	
	//Guarda un nuevo recurso o actualiza el existente
	//return Vista resources si todo es correcto o la vista actual si hay errores
	/**
	 * Save resource if needed.
	 * @return Next view
	 */
	public String saveResource()
	{
		String nextView=null;
		
		// Get current user session operation
		Operation operation=getCurrentUserOperation(null);
		
		setFilterGlobalResourcesEnabled(null);
		setFilterOtherUsersResourcesEnabled(null);
	    setGlobalOtherUserCategoryAllowed(null);
	    setResourceUserAdmin(null);
	    setResourceUserSuperadmin(null);
	    setAddEnabled(null);
	    setEditEnabled(null);
	    setEditOtherUsersResourcesEnabled(null);
	    setEditAdminsResourcesEnabled(null);
	    setEditSuperadminsResourcesEnabled(null);
	    setViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	    setViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	    setViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
		
		Resource resource=getResource();
		boolean update=isUpdate();
		if (update && !resourcesService.checkResourceId(operation,resource.getId()))
		{
			displayErrorPage(
				"RESOURCE_UPDATE_NOT_FOUND_ERROR","The resource you are trying to update no longer exists.");
		}
		else if (!checkSaveResource(operation) || (update && !checkCurrentCategory(operation)))
	    {
	    	displayErrorPage("NON_AUTHORIZED_ACTION_ERROR","You are not authorized to execute that operation");
	    }
	    else
	    {
			FacesContext context=FacesContext.getCurrentInstance();
			nextView="resources?faces-redirect=true";
			User currentUser=null;
			boolean reloadCategories=true;
			
			// Process now not watched input fields (needed because immediate='true')
			processNotWatchedInputFields(context.getViewRoot());
			
			// We need to convert name and description to UTF-8 charset
			processUTF8();
		    
			File resourceFile=null;
			if (tmpFile==null)
			{
				resourceFile=new File(resourcesService.getCurrentResourceFilePath(this));
			}
			else
			{
				resourceFile=tmpFile;
			}
			if (resource.getName()==null || resource.getName().equals(""))
			{
				addErrorMessage("RESOURCE_NAME_REQUIRED");
				nextView=null;
			}
			else if (!checkResourceName())
			{
				nextView=null;
			}
			
			if (!categoriesService.checkCategoryId(operation,resource.getCategory().getId()))
		    {
				addErrorMessage(update?"RESOURCE_CATEGORY_UPDATE_NOT_FOUND":"RESOURCE_CATEGORY_ADD_NOT_FOUND");
				nextView=null;
				
		    	// Refresh categories from DB
		    	resetResourcesCategories(operation);
		    	reloadCategories=false;
		    }
		    else if (checkCategory(operation))
			{
				if (!checkAvailableResourceName(operation))
				{
					addErrorMessage("RESOURCE_NAME_ALREADY_DECLARED");
					nextView=null;
				}
				boolean invalidUpload=false;
				if (getFile()!=null)
				{
					setLocalSourceAllowed(null);
					if (!getLocalSourceAllowed(operation).booleanValue())
					{
						resourcesService.deleteResourceFile(tmpFile);
						setFile(null);
						setUrl(null);
						setUploadedUrl(null);
						setUrlContent(null);
						tmpFile=null;
						width=-1;
						height=-1;
						setLocalSourceAllowed(Boolean.TRUE);
						addErrorMessage("LOCAL_SOURCE_DENIED");
						nextView=null;
						invalidUpload=true;
					}
					if (!invalidUpload)
					{
						setResourceSizeLimit(null);
						int resourceSizeLimit=getResourceSizeLimit(operation).intValue();
						if (resourceSizeLimit>0 && (int)getFile().getSize()>resourceSizeLimit)
						{
							resourcesService.deleteResourceFile(tmpFile);
							setFile(null);
							setUrl(null);
							setUploadedUrl(null);
							setUrlContent(null);
							tmpFile=null;
							width=-1;
							height=-1;
							setLocalSourceAllowed(Boolean.TRUE);
							addPlainErrorMessage(getErrorMessageSizeLimit(operation));
							nextView=null;
							invalidUpload=true;
						}
					}
					if (!invalidUpload)
					{
						setResourcesMaximumAvailableSpace(null);
						int resourcesMaximumAvailableSpace=getResourcesMaximumAvailableSpace(operation).intValue();
						if (resourcesMaximumAvailableSpace>0)
						{
							setResourcesUsedSpace(null);
							if (((int)getFile().getSize()+getResourcesUsedSpace(operation).intValue())>
								resourcesMaximumAvailableSpace)
							{
								resourcesService.deleteResourceFile(tmpFile);
								setFile(null);
								setUrl(null);
								setUploadedUrl(null);
								setUrlContent(null);
								tmpFile=null;
								width=-1;
								height=-1;
								setLocalSourceAllowed(Boolean.TRUE);
								addPlainErrorMessage(getErrorMessageResourcesSpaceLimit(operation));
								nextView=null;
								invalidUpload=true;
							}
						}
					}
				}
				else if (getUrlContent()!=null)
				{
				setNetworkSourceAllowed(null);
					if (!getNetworkSourceAllowed(operation).booleanValue())
					{
						resourcesService.deleteResourceFile(tmpFile);
						setFile(null);
						setUrl(null);
						setUploadedUrl(null);
						setUrlContent(null);
						tmpFile=null;
						width=-1;
						height=-1;
						setNetworkSourceAllowed(Boolean.TRUE);
						addErrorMessage("NETWORK_SOURCE_DENIED");
						nextView=null;
						invalidUpload=true;
					}
					if (!invalidUpload)
					{
						setResourceSizeLimit(null);
						int resourceSizeLimit=getResourceSizeLimit(operation).intValue();
						if (resourceSizeLimit>0 && getUrlContent().length>resourceSizeLimit)
						{
							resourcesService.deleteResourceFile(tmpFile);
							setFile(null);
							setUrl(null);
							setUploadedUrl(null);
							setUrlContent(null);
							tmpFile=null;
							width=-1;
							height=-1;
							setLocalSourceAllowed(Boolean.TRUE);
							addPlainErrorMessage(getErrorMessageSizeLimit(operation));
							nextView=null;
							invalidUpload=true;
						}
					}
					if (!invalidUpload)
					{
						setResourcesMaximumAvailableSpace(null);
						int resourcesMaximumAvailableSpace=getResourcesMaximumAvailableSpace(operation).intValue();
						if (resourcesMaximumAvailableSpace>0)
						{
							setResourcesUsedSpace(null);
							if (getUrlContent().length+getResourcesUsedSpace(operation).intValue()>
								resourcesMaximumAvailableSpace)
							{
								resourcesService.deleteResourceFile(tmpFile);
								setFile(null);
								setUrl(null);
								setUploadedUrl(null);
								setUrlContent(null);
								tmpFile=null;
								width=-1;
								height=-1;
								setLocalSourceAllowed(Boolean.TRUE);
								addPlainErrorMessage(getErrorMessageResourcesSpaceLimit(operation));
								nextView=null;
								invalidUpload=true;
							}
						}
					}
				}
				if (!invalidUpload && 
					resourcesService.checkMimeType(resourceFile,null,getResourceMimeType())==null)
				{
					addErrorMessage("FILE_UNEXPECTED_TYPE");
					nextView=null;
				}
			}
			else
			{
				addErrorMessage("RESOURCE_CATEGORY_NOT_GRANTED_ERROR");
				nextView=null;
				
		    	// Refresh categories from DB
		    	resetResourcesCategories(operation);
		    	reloadCategories=false;
			}
			if (nextView!=null && !update && isUploaded())
			{
				currentUser=userSessionService.getCurrentUser(operation);
			}
			
			if (nextView!=null)
			{
				if (update)														// Update resource
				{
					// NOTE: We need to end current user session Hibernate operation before updating resource 
					//       to avoid an Hibernate deadlock
					
					// End current user session Hibernate operation
					userSessionService.endCurrentUserOperation();
					
					resourcesService.updateResource(this,isUploaded());
				}
				else if (isUploaded())
				{
					try
					{
						// All is correct, so we save resource
						resource.setUser(currentUser);
						resourcesService.addResource(this);
					}
					finally
					{
						// End current user session Hibernate operation
						userSessionService.endCurrentUserOperation();
					}
				}
				else															// No file uploaded
				{
					addErrorMessage("FILE_UPLOAD_REQUIRED");
					nextView=null;
				}
				if (nextView!=null)
				{
					// We delete temporary file if created
					resourcesService.deleteResourceFile(tmpFile);
				}
			}
			if (nextView==null)
			{
				// Reload categories if needed
				if (reloadCategories)
				{
					setResourcesCategories(null);
				}
				
				// Reset user permissions
				setLocalSourceAllowed(null);
				setNetworkSourceAllowed(null);
				setResourceSizeLimit(null);
				setResourcesMaximumAvailableSpace(null);
				setResourcesUsedSpace(null);
			}
	    }
		return nextView;
	}
	
	/**
	 * @return Context relative path to resource file or to temporary file with uploaded content for preview.
	 */
	public String getResourcePreview()
	{
		StringBuffer resourcePreview=null;
		if (isUploaded())
		{
			if (tmpFile==null || !tmpFile.exists())
			{
				try
				{
					saveTemporaryFile();
				}
				catch (IOException e)
				{
					tmpFile=null;
				}
			}
			if (tmpFile!=null)
			{
				resourcePreview=new StringBuffer();
				resourcePreview.append('/');
				resourcePreview.append(configurationService.getTmpFolder());
				resourcePreview.append('/');
				resourcePreview.append(tmpFile.getName());
			}
		}
		else if (isUpdate())
		{
			resourcePreview=new StringBuffer();
			resourcePreview.append(resource.getFileName());
		}
		return resourcePreview==null?null:resourcePreview.toString();
	}
	
	/**
	 * @return Extension of source file if we have uploaded one or extension of resource file otherwise
	 */
	public String getExtension()
	{
		String ext=null;
		
		if (isUploaded())
		{
			ext=tmpExt;
		}
		else if (isUpdate())
		{
			Resource resource=getResource();
			if (resource.getFileName()!=null)
			{
				int iExt=resource.getFileName().lastIndexOf('.');
				if (iExt<resource.getFileName().length()-1)
				{
					ext=resource.getFileName().substring(iExt+1);
				}
			}
		}
		return ext;
	}
	
	/**
	 * @return true if resource is an image, false otherwise
	 */
	public boolean isImage()
	{
		String mimeType=getResourceMimeType();
		return mimeType!=null && mimeType.startsWith("image/");
	}
	
	/**
	 * @return Image width in pixels if resource is a recognized image or -1 otherwise 
	 */
	public int getWidth()
	{
		return getWidth(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Image width in pixels if resource is a recognized image or -1 otherwise 
	 */
	public int getWidth(Operation operation)
	{
		if (width==-1)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (isImage())
			{
				getImageDimensions(operation);
			}
		}
		return width;
	}
	
	/**
	 * @return Image height in pixels if resource is a recognized image or -1 otherwise 
	 */
	public int getHeight()
	{
		return getHeight(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Image height in pixels if resource is a recognized image or -1 otherwise 
	 */
	public int getHeight(Operation operation)
	{
		if (height==-1)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			if (isImage())
			{
				getImageDimensions(operation);
			}
		}
		return height;
	}
	
	/**
	 * Read image dimensions of a resource if it is a recognized image.<br/><br/>
	 * If it is impossible to read them returns -1 for width and height.
	 * @param operation Operation
	 */
	private void getImageDimensions(Operation operation)
	{
		// Get current user session Hibernate operation
		operation=getCurrentUserOperation(operation);
		
		File file=null;
		if (isUploaded())
		{
			if (tmpFile==null || !tmpFile.exists())
			{
				try
				{
					saveTemporaryFile();
				}
				catch (IOException e)
				{
					tmpFile=null;
				}
			}
			if (tmpFile!=null)
			{
				file=tmpFile;
			}
		}
		else if (isUpdate())
		{
			String filePath=resourcesService.getCurrentResourceFilePath(this);
			if (filePath!=null)
			{
				file=new File(filePath);
			}
		}
		if (file!=null)
		{
			int[] imageDimensions=resourcesService.getImageDimensionsByMIME(file,getResourceMimeType());
			width=imageDimensions[0];
			height=imageDimensions[1];
		}
	}
	
	public String getConfirmCancelResourceDialogMessage()
	{
		return confirmCancelResourceDialogMessage; 
	}
	
	public void setConfirmCancelResourceDialogMessage(String confirmCancelResourceDialogMessage)
	{
		this.confirmCancelResourceDialogMessage=confirmCancelResourceDialogMessage;
	}
	
	public String getCancelResourceTarget()
	{
		return cancelResourceTarget;
	}
	
	public void setCancelResourceTarget(String cancelResourceTarget)
	{
		this.cancelResourceTarget=cancelResourceTarget;
	}
	
	public Boolean getFilterGlobalResourcesEnabled()
	{
		return getFilterGlobalResourcesEnabled(null);
	}
	
	public void setFilterGlobalResourcesEnabled(Boolean filterGlobalResourcesEnabled)
	{
		this.filterGlobalResourcesEnabled=filterGlobalResourcesEnabled;
	}
	
	public boolean isFilterGlobalResourcesEnabled()
	{
		return getFilterGlobalResourcesEnabled().booleanValue();
	}
	
	private Boolean getFilterGlobalResourcesEnabled(Operation operation)
	{
		if (filterGlobalResourcesEnabled==null)
		{
			filterGlobalResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_GLOBAL_FILTER_ENABLED"));
		}
		return filterGlobalResourcesEnabled;
	}
	
	public Boolean getFilterOtherUsersResourcesEnabled()
	{
		return getFilterOtherUsersResourcesEnabled(null);
	}
	
	public void setFilterOtherUsersResourcesEnabled(Boolean filterOtherUsersResourcesEnabled)
	{
		this.filterOtherUsersResourcesEnabled=filterOtherUsersResourcesEnabled;
	}
	
	private Boolean getFilterOtherUsersResourcesEnabled(Operation operation)
	{
		if (filterOtherUsersResourcesEnabled==null)
		{
			filterOtherUsersResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_OTHER_USERS_FILTER_ENABLED"));
		}
		return filterOtherUsersResourcesEnabled;
	}
	
	public Boolean getGlobalOtherUserCategoryAllowed()
	{
		return getGlobalOtherUserCategoryAllowed(null);
	}
	
	public void setGlobalOtherUserCategoryAllowed(Boolean globalOtherUserCategoryAllowed)
	{
		this.globalOtherUserCategoryAllowed=globalOtherUserCategoryAllowed;
	}
	
	public boolean isGlobalOtherUserCategoryAllowed()
	{
		return getGlobalOtherUserCategoryAllowed().booleanValue();
	}
	
	private Boolean getGlobalOtherUserCategoryAllowed(Operation operation)
	{
		if (globalOtherUserCategoryAllowed==null)
		{
			// Get current user session Hibernate operation
			operation=getCurrentUserOperation(operation);
			
			Resource resource=getResource();
			if (resource.getId()>0L)
			{
				globalOtherUserCategoryAllowed=Boolean.valueOf(permissionsService.isGranted(
					operation,resource.getUser(),"PERMISSION_RESOURCE_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
			}
			else
			{
				globalOtherUserCategoryAllowed=Boolean.valueOf(userSessionService.isGranted(
					operation,"PERMISSION_RESOURCE_GLOBAL_OTHER_USER_CATEGORY_ALLOWED"));
			}
		}
		return globalOtherUserCategoryAllowed;
	}
	
	public Boolean getAddEnabled()
	{
		return getAddEnabled(null);
	}
	
	public void setAddEnabled(Boolean addEnabled)
	{
		this.addEnabled=addEnabled;
	}
	
	public boolean isAddEnabled()
	{
		return getAddEnabled().booleanValue();
	}
	
	private Boolean getAddEnabled(Operation operation)
	{
		if (addEnabled==null)
		{
			addEnabled=Boolean.valueOf(
				userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_RESOURCES_ADD_ENABLED"));
		}
		return addEnabled;
	}
	
	public Boolean getEditEnabled()
	{
		return getEditEnabled(null);
	}
	
	public void setEditEnabled(Boolean editEnabled)
	{
		this.editEnabled=editEnabled;
	}
	
	public boolean isEditEnabled()
	{
		return getEditEnabled().booleanValue();
	}
	
	private Boolean getEditEnabled(Operation operation)
	{
		if (editEnabled==null)
		{
			editEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_ENABLED"));
		}
		return editEnabled;
	}
	
	public Boolean getEditOtherUsersResourcesEnabled()
	{
		return getEditOtherUsersResourcesEnabled(null);
	}
	
	public void setEditOtherUsersResourcesEnabled(Boolean editOtherUsersResourcesEnabled)
	{
		this.editOtherUsersResourcesEnabled=editOtherUsersResourcesEnabled;
	}
	
	public boolean isEditOtherUsersResourcesEnabled()
	{
		return getEditOtherUsersResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditOtherUsersResourcesEnabled(Operation operation)
	{
		if (editOtherUsersResourcesEnabled==null)
		{
			editOtherUsersResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_OTHER_USERS_RESOURCES_ENABLED"));
		}
		return editOtherUsersResourcesEnabled;
	}
	
	public Boolean getEditAdminsResourcesEnabled()
	{
		return getEditAdminsResourcesEnabled(null);
	}
	
	public void setEditAdminsResourcesEnabled(Boolean editAdminsResourcesEnabled)
	{
		this.editAdminsResourcesEnabled=editAdminsResourcesEnabled;
	}
	
	public boolean isEditAdminsResourcesEnabled()
	{
		return getEditAdminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditAdminsResourcesEnabled(Operation operation)
	{
		if (editAdminsResourcesEnabled==null)
		{
			editAdminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_ADMINS_RESOURCES_ENABLED"));
		}
		return editAdminsResourcesEnabled;
	}
	
	public Boolean getEditSuperadminsResourcesEnabled()
	{
		return getEditSuperadminsResourcesEnabled(null);
	}
	
	public void setEditSuperadminsResourcesEnabled(Boolean editSuperadminsResourcesEnabled)
	{
		this.editSuperadminsResourcesEnabled=editSuperadminsResourcesEnabled;
	}
	
	public boolean isEditSuperadminsResourcesEnabled()
	{
		return getEditSuperadminsResourcesEnabled().booleanValue();
	}
	
	private Boolean getEditSuperadminsResourcesEnabled(Operation operation)
	{
		if (editSuperadminsResourcesEnabled==null)
		{
			editSuperadminsResourcesEnabled=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCES_EDIT_SUPERADMINS_RESOURCES_ENABLED"));
		}
		return editSuperadminsResourcesEnabled;
	}
	
	public Boolean getViewResourcesFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewResourcesFromOtherUsersPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromOtherUsersPrivateCategoriesEnabled(
		Boolean viewResourcesFromOtherUsersPrivateCategoriesEnabled)
	{
		this.viewResourcesFromOtherUsersPrivateCategoriesEnabled=
			viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromOtherUsersPrivateCategoriesEnabled()
	{
		return getViewResourcesFromOtherUsersPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromOtherUsersPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromOtherUsersPrivateCategoriesEnabled==null)
		{
			viewResourcesFromOtherUsersPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_OTHER_USERS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromOtherUsersPrivateCategoriesEnabled;
	}
	
	public Boolean getViewResourcesFromAdminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromAdminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromAdminsPrivateCategoriesEnabled(
		Boolean viewResourcesFromAdminsPrivateCategoriesEnabled)
	{
		this.viewResourcesFromAdminsPrivateCategoriesEnabled=viewResourcesFromAdminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromAdminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromAdminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromAdminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromAdminsPrivateCategoriesEnabled==null)
		{
			viewResourcesFromAdminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_ADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromAdminsPrivateCategoriesEnabled;
	}
	
	public Boolean getViewResourcesFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromSuperadminsPrivateCategoriesEnabled(null);
	}
	
	public void setViewResourcesFromSuperadminsPrivateCategoriesEnabled(
		Boolean viewResourcesFromSuperadminsPrivateCategoriesEnabled)
	{
		this.viewResourcesFromSuperadminsPrivateCategoriesEnabled=
			viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public boolean isViewResourcesFromSuperadminsPrivateCategoriesEnabled()
	{
		return getViewResourcesFromSuperadminsPrivateCategoriesEnabled().booleanValue();
	}
	
	private Boolean getViewResourcesFromSuperadminsPrivateCategoriesEnabled(Operation operation)
	{
		if (viewResourcesFromSuperadminsPrivateCategoriesEnabled==null)
		{
			viewResourcesFromSuperadminsPrivateCategoriesEnabled=
				Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
				"PERMISSION_RESOURCES_VIEW_RESOURCES_OF_SUPERADMINS_PRIVATE_CATEGORIES_ENABLED"));
		}
		return viewResourcesFromSuperadminsPrivateCategoriesEnabled;
	}
	
	public Boolean getLocalSourceAllowed()
	{
		return getLocalSourceAllowed(null);
	}
	
	public void setLocalSourceAllowed(Boolean localSourceAllowed)
	{
		this.localSourceAllowed=localSourceAllowed;
	}
	
	public boolean isLocalSourceAllowed()
	{
		return getLocalSourceAllowed().booleanValue();
	}
	
	private Boolean getLocalSourceAllowed(Operation operation)
	{
		if (localSourceAllowed==null)
		{
			localSourceAllowed=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCE_LOCAL_SOURCE_ALLOWED"));
		}
		return localSourceAllowed;
	}
	
	public Boolean getNetworkSourceAllowed()
	{
		return getNetworkSourceAllowed(null);
	}
	
	public void setNetworkSourceAllowed(Boolean networkSourceAllowed)
	{
		this.networkSourceAllowed=networkSourceAllowed;
	}
	
	public boolean isNetworkSourceAllowed()
	{
		return getNetworkSourceAllowed().booleanValue();
	}
	
	private Boolean getNetworkSourceAllowed(Operation operation)
	{
		if (networkSourceAllowed==null)
		{
			networkSourceAllowed=Boolean.valueOf(userSessionService.isGranted(
				getCurrentUserOperation(operation),"PERMISSION_RESOURCE_NETWORK_SOURCE_ALLOWED"));
		}
		return networkSourceAllowed;
	}
	
	public Boolean getResourceUserAdmin()
	{
		return getResourceUserAdmin(null);
	}
	
	public void setResourceUserAdmin(Boolean resourceUserAdmin)
	{
		this.resourceUserAdmin=resourceUserAdmin;
	}
	
	public boolean isResourceUserAdmin()
	{
		return getResourceUserAdmin().booleanValue();
	}
	
	private Boolean getResourceUserAdmin(Operation operation)
	{
		if (resourceUserAdmin==null)
		{
			Resource resource=getResource();
			if (resource.getId()>0L)
			{
				resourceUserAdmin=Boolean.valueOf(permissionsService.isGranted(
					getCurrentUserOperation(operation),resource.getUser(),"PERMISSION_NAVIGATION_ADMINISTRATION"));
			}
			else
			{
				resourceUserAdmin=Boolean.valueOf(userSessionService.isGranted(
					getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_ADMINISTRATION"));
			}
		}
		return resourceUserAdmin;
	}
	
	public Boolean getResourceUserSuperadmin()
	{
		return getResourceUserSuperadmin(null);
	}
	
	public void setResourceUserSuperadmin(Boolean resourceUserSuperadmin)
	{
		this.resourceUserSuperadmin=resourceUserSuperadmin;
	}
	
	public boolean isResourceUserSuperadmin()
	{
		return getResourceUserSuperadmin().booleanValue();
	}

	private Boolean getResourceUserSuperadmin(Operation operation)
	{
		if (resourceUserSuperadmin==null)
		{
			Resource resource=getResource();
			if (resource.getId()>0L)
			{
				resourceUserSuperadmin=Boolean.valueOf(permissionsService.isGranted(getCurrentUserOperation(operation),
					resource.getUser(),"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED"));
			}
			else
			{
				resourceUserSuperadmin=Boolean.valueOf(userSessionService.isGranted(getCurrentUserOperation(operation),
					"PERMISSION_ADMINISTRATION_RAISE_PERMISSIONS_OVER_OWNED_ALLOWED"));
			}
		}
		return resourceUserSuperadmin;
	}
	
    /**
     * @param category Category
     * @return Localized category long name
     */
    public String getLocalizedCategoryLongName(Category category)
    {
    	return getLocalizedCategoryLongName(null,category.getId());
    }
    
    /**
     * @param categoryId Category identifier
     * @return Localized category long name
     */
    public String getLocalizedCategoryLongName(Long categoryId)
    {
    	return getLocalizedCategoryLongName(null,categoryId);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category identifier
     * @return Localized category long name
     */
    private String getLocalizedCategoryLongName(Operation operation,Long categoryId)
    {
    	return categoriesService.getLocalizedCategoryLongName(getCurrentUserOperation(operation),categoryId);
    }
    
    /**
     * @param category Category
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    public String getLocalizedCategoryLongName(Category category,int maxLength)
    {
    	return getLocalizedCategoryLongName(null,category.getId(),maxLength);
    }
    
    /**
     * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    public String getLocalizedCategoryLongName(Long categoryId,int maxLength)
    {
    	return getLocalizedCategoryLongName(null,categoryId,maxLength);
    }
    
    /**
     * @param operation Operation
     * @param categoryId Category identifier
	 * @param maxLength Maximum length
	 * @return Localized category long name, if length is greater than maximum length it will be abbreviated
     */
    private String getLocalizedCategoryLongName(Operation operation,Long categoryId,int maxLength)
    {
    	return categoriesService.getLocalizedCategoryLongName(
    		getCurrentUserOperation(operation),categoryId,maxLength);
    }
    
	/**
	 * Save uploaded content to a temporary file.
	 * @throws IOException
	 */
	private void saveTemporaryFile() throws IOException
	{
		if (isUploaded())
		{
			resourcesService.deleteResourceFile(tmpFile);
			tmpFile=resourcesService.saveTemporaryFile(this);
		}
	}
	
	/**
	 * Action listener to show the dialog to confirm cancel of resource creation/edition.
	 * @param event Action event
	 */
	public void showConfirmCancelResourceDialog(ActionEvent event)
	{
		setCancelResourceTarget((String)event.getComponent().getAttributes().get("target"));
		RequestContext rq=RequestContext.getCurrentInstance();
		rq.execute("confirmCancelResourceDialog.show()");
	}
	
	/**
	 * Cancel resource creation/editon and return to resources view.
	 * @return Resources view
	 */
	public String cancel()
	{
		setCancelResourceTarget("resources");
		return cancelResource();
	}
	
	/**
	 * Cancel resource creation/editon and navigate to next view.
	 * @return Next wiew
	 */
	public String cancelResource()
	{
		StringBuffer nextView=null;
		if ("logout".equals(getCancelResourceTarget()))
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
		else if (getCancelResourceTarget()!=null)
		{
			nextView=new StringBuffer(getCancelResourceTarget());
			nextView.append("?faces-redirect=true");
		}
		if (nextView!=null)
		{
			// We delete temporary file if created
			resourcesService.deleteResourceFile(tmpFile);
		}
		return nextView==null?null:nextView.toString();
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
	 * Displays an error message.
	 * @param message Error message (plain message not needed to localize)
	 */
	private void addPlainErrorMessage(String message)
	{
		FacesContext context=FacesContext.getCurrentInstance();
		context.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,message,null));
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
