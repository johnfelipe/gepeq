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
package es.uned.lsi.gepec.web.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

@SuppressWarnings("serial")
public class ForceLoginPhaseListener implements PhaseListener
{
	// Error page
	private final static String ERROR_PAGE="/pages/error.xhtml";
	
	// List of pages that can be accesed without being logged into the system
	private final static List<String> PUBLIC_PAGES;
	static
	{
		PUBLIC_PAGES=new ArrayList<String>();
		PUBLIC_PAGES.add("/pages/intro.xhtml");
	}
	
	// List of pages than only can be accessed programatically (trying to access them directly will be denied)
	private final static List<String> PROGRAM_PAGES;
	static
	{
		PROGRAM_PAGES=new ArrayList<String>();
		PROGRAM_PAGES.add("/pages/dragdrop_update.xhtml");
		PROGRAM_PAGES.add("/pages/dragdrop.xhtml");
		PROGRAM_PAGES.add("/pages/multichoice_update.xhtml");
		PROGRAM_PAGES.add("/pages/multichoice.xhtml");
		PROGRAM_PAGES.add("/pages/omxml_update.xhtml");
		PROGRAM_PAGES.add("/pages/omxml.xhtml");
		PROGRAM_PAGES.add("/pages/resource.xhtml");
		PROGRAM_PAGES.add("/pages/test.xhtml");
		PROGRAM_PAGES.add("/pages/testupdate.xhtml");
		PROGRAM_PAGES.add("/pages/truefalse_update.xhtml");
		PROGRAM_PAGES.add("/pages/truefalse.xhtml");
		PROGRAM_PAGES.add("/pages/user.xhtml");
		PROGRAM_PAGES.add("/pages/usertype.xhtml");
		PROGRAM_PAGES.add("/pages/usertypeupdate.xhtml");
		PROGRAM_PAGES.add("/pages/userupdate.xhtml");
		PROGRAM_PAGES.add("/pages/questionrelease.xhtml");
		PROGRAM_PAGES.add("/pages/testrelease.xhtml");
	}
	
	// Map of pages that need a permission to be accessed (key:page,value:permission)
	private final static Map<String,String> RESTRICTED_PAGES;
	static
	{
		RESTRICTED_PAGES=new HashMap<String,String>();
		RESTRICTED_PAGES.put("/pages/administration.xhtml","PERMISSION_NAVIGATION_ADMINISTRATION");
		RESTRICTED_PAGES.put("/pages/categories.xhtml","PERMISSION_NAVIGATION_CATEGORIES");
		RESTRICTED_PAGES.put("/pages/export.xhtml","PERMISSION_NAVIGATION_EXPORT");
		RESTRICTED_PAGES.put("/pages/import.xhtml","PERMISSION_NAVIGATION_IMPORT");
		RESTRICTED_PAGES.put("/pages/publication.xhtml","PERMISSION_NAVIGATION_PUBLICATION");
		RESTRICTED_PAGES.put("/pages/questions.xhtml","PERMISSION_NAVIGATION_QUESTIONS");
		RESTRICTED_PAGES.put("/pages/resources.xhtml","PERMISSION_NAVIGATION_RESOURCES");
		RESTRICTED_PAGES.put("/pages/tests.xhtml","PERMISSION_NAVIGATION_TESTS");
	}
	
	@Override
	public void beforePhase(PhaseEvent event)
	{
	}
	
	@Override
	public void afterPhase(PhaseEvent event)
	{
		// Get context
		FacesContext context=FacesContext.getCurrentInstance();
		
		// Get actual view
		String viewId=null;
		try
		{
			viewId=context.getViewRoot().getViewId();
		}
		catch (NullPointerException npe)
		{
			viewId=null;
		}
		if (viewId!=null && !PUBLIC_PAGES.contains(viewId))
		{
			if (context.getExternalContext().getSession(false)==null)
			{
				displayErrorPage(
					"SESSION_EXPIRED_ERROR","Your session has expired. You must login into the system again.");
			}
			else
			{
				// We check if a special component with the identifier ":viewEnabled" exist within view 
				// to distinguish programatically access to this page from an illegal attempt to access it directly
				boolean viewEnabled=context.getViewRoot().findComponent(":viewEnabled")!=null;
				
				// Check if we are trying to access a page without being logged in
				UserSessionService userSessionService=(UserSessionService)
					context.getApplication().getELResolver().getValue(context.getELContext(),null,"userSessionService");
				if (userSessionService.isLogged())
				{
					// Permission checks
					if (RESTRICTED_PAGES.containsKey(viewId) && userSessionService.isDenied(
						userSessionService.getCurrentUserOperation(),RESTRICTED_PAGES.get(viewId)))
					{
						displayErrorPage("NON_AUTHORIZED_ACCESS_ERROR","You are not authorized to access that page.");
					}
					else if (!viewEnabled && PROGRAM_PAGES.contains(viewId))
					{
						displayErrorPage(
							"NOT_ALLOWED_DIRECT_ACCESS_ERROR","It is not allowed to access this page directly.");
					}
					else if (viewId.equals(ERROR_PAGE))
					{
						boolean ajaxError=false;
						String errorCode=(String)context.getExternalContext().getRequestMap().get("errorCode");
						if (errorCode==null)
						{
							errorCode=(String)context.getExternalContext().getRequestParameterMap().get("errorCode");
						}
						if (errorCode==null)
						{
							errorCode=userSessionService.getAjaxErrorCode();
							ajaxError=true;
						}
						if (errorCode==null)
						{
							if (!viewEnabled)
							{
								displayErrorPage(
									"NOT_ALLOWED_DIRECT_ACCESS_ERROR","It is not allowed to access this page directly.");
							}
						}
						else
						{
							String plainMessage=null;
							if (ajaxError)
							{
								plainMessage=userSessionService.getAjaxPlainError();
								if (plainMessage==null)
								{
									plainMessage="";
								}
								userSessionService.setAjaxErrorCode(null);
								userSessionService.setAjaxPlainError(null);
							}
							else
							{
								plainMessage=(String)context.getExternalContext().getRequestMap().get("plainMessage");
								if (plainMessage==null)
								{
									plainMessage=
										(String)context.getExternalContext().getRequestParameterMap().get("plainMessage");
									if (plainMessage==null)
									{
										plainMessage="";
									}
								}
							}
							displayErrorPage(errorCode,plainMessage);
						}
					}
				}
				else if (viewId.equals(ERROR_PAGE))
				{
					String errorCode=(String)context.getExternalContext().getRequestMap().get("errorCode");
					if (errorCode==null)
					{
						errorCode=(String)context.getExternalContext().getRequestParameterMap().get("errorCode");
					}
					if (errorCode==null)
					{
						errorCode=userSessionService.getAjaxErrorCode();
						if (errorCode!=null)
						{
							String plainMessage=userSessionService.getAjaxPlainError();
							if (plainMessage==null)
							{
								plainMessage="";
							}
							userSessionService.setAjaxErrorCode(null);
							userSessionService.setAjaxPlainError(null);
							displayErrorPage(errorCode,plainMessage);
						}
					}
					else
					{
						String plainMessage=(String)context.getExternalContext().getRequestMap().get("plainMessage");
						if (plainMessage==null)
						{
							plainMessage=(String)context.getExternalContext().getRequestParameterMap().get("plainMessage");
							if (plainMessage==null)
							{
								plainMessage="";
							}
						}
						displayErrorPage(errorCode,plainMessage);
					}
				}
				else
				{
					displayErrorPage("NOT_LOGGED_ERROR","You must login into the system to access that page.");
				}
			}
		}
	}
	
	@Override
	public PhaseId getPhaseId()
	{
		return PhaseId.RESTORE_VIEW;
	}
	
	/**
	 * Display error page with the indicated error message.<br/><br/>
	 * The message will be localized if it is possible or be displayed the plain version of the error message otherwise.
	 * @param errorCode Error message locale code
	 * @param plainMessage Plain version of the error message
	 */
	private void displayErrorPage(String errorCode,String plainMessage)
	{
		// Get context
		FacesContext context=FacesContext.getCurrentInstance();
		Application app=context.getApplication();
		LocalizationService localizationService=
			(LocalizationService)app.getELResolver().getValue(context.getELContext(),null,"localizationService");
		String errorMessage=null;
		if (localizationService!=null)
		{
			try
			{
				errorMessage=localizationService.getLocalizedMessage(errorCode);
			}
			catch (ServiceException se)
			{
				errorMessage=null;
			}
		}
		if (errorMessage==null)
		{
			errorMessage=plainMessage;
		}
		context.addMessage(":errorForm:messages",new FacesMessage(FacesMessage.SEVERITY_ERROR,errorMessage,null));
		NavigationHandler navigationHandler=app.getNavigationHandler();
		navigationHandler.handleNavigation(context,null,"/pages/error");
	}
}
