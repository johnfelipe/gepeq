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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import es.uned.lsi.gepec.model.entities.User;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.ServiceException;
import es.uned.lsi.gepec.web.services.UserSessionService;


//Backbean para la vista login
//@author Víctor Manuel Alonso Rodríguez
//since  12/2011
/**
 * Managed bean for login/logout.
 */
@SuppressWarnings("serial")
@ManagedBean(name="loginBean")
@RequestScoped
public class LoginBean implements Serializable
{
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	
	private String username;
	private String password;
	
	public LoginBean()
	{
		username="";
		password="";
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
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username=username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password=password;
	}
	
	/**
	 * @return Current user or null if it is not logged in
	 */
	public User getCurrentUser()
	{
		return getCurrentUser(null);
	}
	
	/**
	 * @param operation Operation
	 * @return Current user or null if it is not logged in
	 */
	private User getCurrentUser(Operation operation)
	{
		return userSessionService.getCurrentUser(getCurrentUserOperation(operation));
	}
	
	//Comprueba si los datos del usuario son correctos
	//return Vista a la que accederemos o null si no tenemos permiso
	/**
	 * Action to login into GEPEQ.<br/></br/>
	 * Note that login can be succesful or not. Next view and/or messages displayed to user will depend on that
	 * result.
	 * @return Next view.
	 */
	public String login()
	{
		String nextView=null;
		try
		{
			userSessionService.login(getUsername(),getPassword());
		}
		catch (ServiceException se)
		{
			addPlainErrorMessage(se.getMessage());
		}
		
		//TODO la pagina de inicio de momento elegimos de entre "Preguntas", "Pruebas", "Recursos", "Categorías" y "Administración" la primera de estas a la que el usuario tenga permiso de acceso...
		//TODO no estamos teniendo en cuenta las páginas de "Importar" y "Exportar" de momento mientras QTI no sea soportado correctamente
		//TODO además sería deseable que el usuario pudiera configurar unas preferencias y en ellas elegir cosas como la página inicial
		//TODO si el usuario no tiene permiso para acceder a ninguna de las páginas anteriores se muestra un error indicando que el usuario no tiene permisos suficientes para usar la aplicación....
		if (userSessionService.isLogged())
		{
			// Get current user session Hibernate operation
			Operation operation=userSessionService.getCurrentUserOperation();
			
			if (userSessionService.isGranted(operation,"PERMISSION_NAVIGATION_QUESTIONS"))
			{
				nextView="questions?faces-redirect=true";
			}
			else if (userSessionService.isGranted(operation,"PERMISSION_NAVIGATION_TESTS"))
			{
				nextView="tests?faces-redirect=true";
			}
			else if (userSessionService.isGranted(operation,"PERMISSION_NAVIGATION_RESOURCES"))
			{
				nextView="resources?faces-redirect=true";
			}
			else if (userSessionService.isGranted(operation,"PERMISSION_NAVIGATION_CATEGORIES"))
			{
				nextView="categories?faces-redirect=true";
			}
			else if (userSessionService.isGranted(operation,"PERMISSION_NAVIGATION_ADMINISTRATION"))
			{
				nextView="administration?faces-redirect=true";
			}
			else
			{
				userSessionService.logout();
				addErrorMessage("NON_SUFFICIENT_PERMISSIONS_ACCESS_ERROR");
			}
		}
		return nextView;
	}
	
	/**
	 * Action to logout from GEPEQ.<br/><br/>
	 * Note that next view will be the introduction page.
	 * @return Next view (introduction page)
	 */
	public String logout()
	{
		userSessionService.logout();
		return "intro";
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
}