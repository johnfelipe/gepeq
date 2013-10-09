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
package es.uned.lsi.gepec.web.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import es.uned.lsi.gepec.web.listeners.ForceLoginPhaseListenerException;
import es.uned.lsi.gepec.web.services.UserSessionService;

/**
 * Custom exception handler for GEPEQ.
 */
public class ExceptionHandler extends ExceptionHandlerWrapper
{
	private final javax.faces.context.ExceptionHandler wrapped;
	
	public ExceptionHandler(javax.faces.context.ExceptionHandler wrapped)
	{
		this.wrapped=wrapped;
	}
	
	@Override
	public javax.faces.context.ExceptionHandler getWrapped()
	{
		return wrapped;
	}
	
	@Override
	public void handle() throws FacesException
	{
		// Iterate all queued exception events
		for (final Iterator<ExceptionQueuedEvent> it=getUnhandledExceptionQueuedEvents().iterator();it.hasNext();)
		{
			// We get context
			FacesContext context=FacesContext.getCurrentInstance();
			
			// We get external context
			ExternalContext externalContext=context.getExternalContext();
			
			// We get navigation handler
			NavigationHandler nav=context.getApplication().getNavigationHandler();
			
			// We get request
			ServletRequest request=(ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			
			// We get response
			ServletResponse response=(ServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
			
			// We get request map
			Map<String,Object> requestMap=externalContext.getRequestMap();
			
			// We need to get the exception (we get rid off FacesException and ELException wrappers exceptions)
			Throwable t=it.next().getContext().getException();
			while ((t instanceof FacesException || t instanceof ELException) && t.getCause()!=null)
			{
				t=t.getCause();
			}
			
			// We try to handle exception
			boolean handled=false;
			try
			{
				UserSessionService userSessionService=
					(UserSessionService)context.getApplication().getELResolver().getValue(
					context.getELContext(),null,"userSessionService");
				
				String errorCode=null;
				String plainError=null;
				if (t instanceof ForceLoginPhaseListenerException)
				{
					errorCode=((ForceLoginPhaseListenerException)t).getErrorCode();
					plainError=((ForceLoginPhaseListenerException)t).getPlainMessage();
					handled=true;
				}
				else if (t instanceof FileNotFoundException)
				{
					errorCode="PAGE_NON_EXIST_ERROR";
					plainError="The page you are trying to access does not exist.";
					handled=true;
				}
				else if (t instanceof ViewExpiredException)
				{
					// As user session has expired we need to force logout
					userSessionService.logout();
					
					errorCode="SESSION_EXPIRED_ERROR";
					plainError="Your session has expired. You must login into the system again.";
					handled=true;
				}
				if (handled)
				{
					if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest())
					{
						if (externalContext.isResponseCommitted())
						{
							handled=false;
						}
						else
						{
							try
							{
								// Ajax: Dispatch is not working in Ajax, so we need to redirect instead
								
								// Redirect does not allow to set new request attributes so we need to pass 
								// information about the error with user session variables
								userSessionService.setAjaxErrorCode(errorCode);
								userSessionService.setAjaxPlainError(plainError);
								
								response.setCharacterEncoding(request.getCharacterEncoding());
								RenderKitFactory factory=
									(RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
								RenderKit renderKit=factory.getRenderKit(
									context,context.getApplication().getViewHandler().calculateRenderKitId(context));
								ResponseWriter responseWriter=renderKit.createResponseWriter(
									response.getWriter(),null,request.getCharacterEncoding());
								context.setResponseWriter(responseWriter);
								StringBuffer errorPagePath=new StringBuffer(externalContext.getRequestContextPath());
								if (errorPagePath.length()==0 || errorPagePath.charAt(errorPagePath.length()-1)!='/')
								{
									errorPagePath.append('/');
								}
								errorPagePath.append("pages/error.jsf");
								externalContext.redirect(errorPagePath.toString());
							}
							catch (IOException ioe)
							{
								userSessionService.setAjaxErrorCode(null);
								userSessionService.setAjaxPlainError(null);
								handled=false;
							}
						}
					}
					else
					{
						// We set request attributes with information about the error
						requestMap.put("errorCode",errorCode);
						requestMap.put("plainMessage",plainError);
						
						// Dispatch error page
		                nav.handleNavigation(context,null,"/pages/error.jsf");
		                context.renderResponse();
					}
				}
			}
			finally
			{
				if (handled)
				{
					it.remove();
				}
			}
		}
		getWrapped().handle();
	}
}
