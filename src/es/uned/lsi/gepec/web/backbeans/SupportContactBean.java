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
package es.uned.lsi.gepec.web.backbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.SupportContact;
import es.uned.lsi.gepec.model.entities.Test;
import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.TestBean;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.UserSessionService;
import es.uned.lsi.gepec.web.services.UsersService;

/**
 * Backbean for a support contact.
 */
@SuppressWarnings("serial")
public class SupportContactBean implements Serializable
{
	private final static int MAXIMUM_NICKS_DISPLAYED_WITHIN_FILTERING_STRING=10;
	private final static int MAXIMUM_GROUPS_DISPLAYED_WITHIN_FILTERING_STRING=10;
	
	private TestBean testBean;
	private long id;
	private String supportContact;
	private String filterType;
	private String filterSubtype;
	private String filterValue;
	
	public SupportContactBean(String supportContact,String filterType,String filterSubtype,String filterValue)
	{
		this(null,supportContact,filterType,filterSubtype,filterValue);
	}
	
	public SupportContactBean(TestBean testBean,String supportContact,String filterType,String filterSubtype,
		String filterValue)
	{
		this.testBean=testBean;
		this.id=0L;
		this.supportContact=supportContact;
		this.filterType=filterType;
		this.filterSubtype=filterSubtype;
		this.filterValue=filterValue;
	}
	
	public SupportContactBean(SupportContact supportContact)
	{
		this(null,supportContact);
	}
	
	public SupportContactBean(TestBean testBean,SupportContact supportContact)
	{
		this.testBean=testBean;
		setFromSupportContact(supportContact);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id=id;
	}
	
	public String getSupportContact()
	{
		return supportContact;
	}
	
	public void setSupportContact(String supportContact)
	{
		this.supportContact=supportContact;
	}
	
	public String getFilterType()
	{
		return filterType;
	}
	
	public void setFilterType(String filterType)
	{
		this.filterType=filterType;
	}
	
	public String getFilterSubtype()
	{
		return filterSubtype;
	}
	
	public void setFilterSubtype(String filterSubtype)
	{
		this.filterSubtype=filterSubtype;
	}
	
	public String getFilterValue()
	{
		return filterValue;
	}
	
	public void setFilterValue(String filterValue)
	{
		this.filterValue=filterValue;
	}
	
	public String getFilteringString()
	{
		String filteringString=null;
		FacesContext context=FacesContext.getCurrentInstance();
        ELContext elContext=context.getELContext();
        ELResolver resolver=context.getApplication().getELResolver();
        LocalizationService localizationService=
        	(LocalizationService)resolver.getValue(elContext,null,"localizationService");
        if ("USER_FILTER".equals(getFilterType()))
        {
        	if ("USERS_SELECTION".equals(getFilterSubtype()))
        	{
        		StringBuffer usersNicks=new StringBuffer();
   				if (getFilterValue()!=null && !"".equals(getFilterValue()))
   				{
   	        		UserSessionService userSessionService=
   	        			(UserSessionService)resolver.getValue(elContext,null,"userSessionService");
   	        		UsersService usersService=(UsersService)resolver.getValue(elContext,null,"usersService");
   	        		
   	        		// Get current user session Hibernate operation
   	        		Operation operation=userSessionService.getCurrentUserOperation();
   	        		
   	        		String[] sOUCUs=getFilterValue().split(",");
   	        		List<String> checkedOUCUs=new ArrayList<String>();
   	        		int displayedNicks=0;
   	        		for (int i=0;i<sOUCUs.length && displayedNicks<=MAXIMUM_NICKS_DISPLAYED_WITHIN_FILTERING_STRING;i++)
   	        		{
   	        			String sOUCU=sOUCUs[i];
   	        			if (!checkedOUCUs.contains(sOUCU))
   	        			{
   	   	            		User user=usersService.getUserFromOucu(operation,sOUCUs[i]);
   	   	            		if (user!=null)
   	   	            		{
	   	            			if (usersNicks.length()>0)
   	   	            			{
   	   	            				usersNicks.append(", ");
   	   	            			}
   	   	            			if (displayedNicks<MAXIMUM_NICKS_DISPLAYED_WITHIN_FILTERING_STRING)
   	   	            			{
   	   	            				usersNicks.append(user.getNick());
   	   	            			}
   	   	            			else
   	   	            			{
   	   	            				usersNicks.append("...");
   	   	            			}
   	   	            			displayedNicks++;
   	   	            		}
   	   	            		checkedOUCUs.add(sOUCU);
   	        			}
   	        		}
   				}
        		filteringString=localizationService.getLocalizedMessage(
        			"USER_SELECTION_FILTERING").replace("?",usersNicks.toString());
        	}
        	else if ("RANGE_NAME".equals(getFilterSubtype()))
        	{
        		filteringString=localizationService.getLocalizedMessage(
        			"RANGE_NAME_FILTERING").replace("?",getFilterValue().replace("-",".."));
        	}
        	else if ("RANGE_SURNAME".equals(getFilterSubtype()))
        	{
        		filteringString=localizationService.getLocalizedMessage(
        			"RANGE_SURNAME_FILTERING").replace("?",getFilterValue().replace("-",".."));
        	}
        }
        else if ("GROUP_FILTER".equals(getFilterType()))
        {
    		StringBuffer groups=new StringBuffer();
			if (getFilterValue()!=null && !"".equals(getFilterValue()))
			{
	        	String[] sAuthIds=getFilterValue().split(Pattern.quote(","));
        		List<String> checkedAuthIds=new ArrayList<String>();
	        	int displayedGroups=0;
	       		for (int i=0;i<sAuthIds.length && displayedGroups<=MAXIMUM_GROUPS_DISPLAYED_WITHIN_FILTERING_STRING;
	  	        	i++)
	       		{
	       			String sAuthId=sAuthIds[i];
	       			if (!checkedAuthIds.contains(sAuthId))
	       			{
	       				if (groups.length()>0)
	       				{
	       					groups.append(", ");
	       				}
	       				if (displayedGroups<MAXIMUM_GROUPS_DISPLAYED_WITHIN_FILTERING_STRING)
	       				{
	       					groups.append(sAuthId);
	       				}
	       				else
	       				{
	       					groups.append("...");
	       				}
	       				displayedGroups++;
	       				checkedAuthIds.add(sAuthId);
	       			}
	       		}
			}
    		filteringString=
    			localizationService.getLocalizedMessage("GROUP_SELECTION_FILTERING").replace("?",groups.toString());
        }
        if (filteringString==null)
		{
			filteringString=localizationService.getLocalizedMessage("NO_FILTERING");
		}
        return filteringString;
	}
	
	/**
	 * Set support contact bean fields from a SupportContact object.
	 * @param supportContact SupportContact object
	 */
	public void setFromSupportContact(SupportContact supportContact)
	{
		setId(supportContact.getId());
		setSupportContact(supportContact.getSupportContact());
		setFilterType(supportContact.getAddressType().getType());
		setFilterSubtype(supportContact.getAddressType().getSubtype());
		setFilterValue(supportContact.getFilterValue());
	}
	
	/**
	 * @param test Test object
	 * @return SupportContact object with data from this support contact bean
	 */
	public SupportContact getAsSupportContact(Test test)
	{
		return testBean==null?null:new SupportContact(getId(),test,
			testBean.getAddressType(getFilterType(),getFilterSubtype()),getFilterValue(),getSupportContact());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean ok=false;
		if (obj instanceof SupportContactBean)
		{
			if (getId()==0L)
			{
				ok=((SupportContactBean)obj).getId()==0L && 
					getFilterType().equals(((SupportContactBean)obj).getFilterType()) &&
					getFilterSubtype()==null?
					((SupportContactBean)obj).getFilterSubtype()==null:
					getFilterSubtype().equals(((SupportContactBean)obj).getFilterSubtype());
				if (ok)
				{
					if (getSupportContact()==null || getSupportContact().equals(""))
					{
						ok=((SupportContactBean)obj).getSupportContact()==null || 
							((SupportContactBean)obj).getSupportContact().equals("");
					}
					else
					{
						ok=getSupportContact().equals(((SupportContactBean)obj).getSupportContact());
					}
				}
			}
			else
			{
				ok=getId()==((SupportContactBean)obj).getId();
			}
		}
		return ok;
	}
}
