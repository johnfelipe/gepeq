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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.qti.ReadQuestionsXML;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.LocalizationService;
import es.uned.lsi.gepec.web.services.UserSessionService;

/**
 * Backbean para la vista importar
 * 
 * @author Víctor Manuel Alonso Rodríguez
 * @since  12/2011
 */
@ManagedBean(name="importBean")
@ViewScoped
public class ImportBean implements Serializable {
	@ManagedProperty(value="#{localizationService}")
	private LocalizationService localizationService;
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;		
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;					
    private List<UploadedFile> files;				// Ficheros subidos por el usuario
    
    public ImportBean() { 
    	files = new ArrayList<UploadedFile>();
	}

    public void setLocalizationService(LocalizationService localizationService) {
		this.localizationService = localizationService;
	}
    
    public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
    
    public void setCategoriesService(CategoriesService categoriesService) {
		this.categoriesService = categoriesService;
	}
    
    public void setUserSessionService(UserSessionService userSessionService) {
		this.userSessionService = userSessionService;
	}
	
    private Operation getCurrentUserOperation(Operation operation)
    {
    	return operation==null?userSessionService.getCurrentUserOperation():operation;
    }
	
    /**
     * @return true if current user is allowed to navigate "Import" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Import" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_IMPORT");
    }
    
	/**
	 * ActionListener para la subida de un fichero
	 * 
	 * @param event
	 * @throws IOException
	 */
	public void handleFileUpload(FileUploadEvent event) throws IOException {
		files.add(event.getFile());
	}

	/**
	 * Importa las preguntas y nos devuelve a la vista preguntas
	 * 
	 * @return
	 */
	public String importQuestions() {
		if(files.size() == 0)		// No hay ficheros subidos
		{
			FacesContext context = FacesContext.getCurrentInstance();
			String message = 
					localizationService.getLocalizedMessage("XML_FILE_REQUIRED");
			
			context.addMessage(":accordion:editForm", 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
			
			return null;
		}
		
		List<Question> exportQuestions = new ArrayList<Question>();
    	String path = configurationService.getApplicationPath() + 
    		File.separatorChar + configurationService.getQtiImportQuestionsFolder() +
    		File.separatorChar + "u" + userSessionService.getCurrentUser().getId();
    	
    	File folder = new File(path);
    	if(!folder.exists())
    	{
    		folder.mkdir();
    	}
    	else
    	{
    		for(File f: folder.listFiles())
    		{
    			f.delete();
    		}
    	}
    	
    	
    	try
    	{
    		for(UploadedFile uf: files)
    		{
    			String fileName = uf.getFileName();

    			// Guardamos el fichero

    			File target = new File(path + File.separatorChar + fileName);
    			FileOutputStream output = new FileOutputStream(target);

    			output.write(uf.getContents());
    			output.close();

    		}

    		ReadQuestionsXML readQuestions = new ReadQuestionsXML();

    		readQuestions.doLogic(folder.getAbsolutePath(), "truefalse", 
    				userSessionService.getCurrentUser(), categoriesService.getCategory(1));

    	}
    	catch (Exception e) {
    		FacesContext context = FacesContext.getCurrentInstance();
			String message = localizationService.getLocalizedMessage("IMPORT_FAIL");
			
			context.addMessage(":accordion:editForm", 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
			
			files = new ArrayList<UploadedFile>();
			
			return null;
    	}
    	
    	FacesContext context = FacesContext.getCurrentInstance();
		String message = localizationService.getLocalizedMessage("IMPORT_COMPLETE");
		
		context.addMessage(":accordion:editForm", 
			new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
		
		files = new ArrayList<UploadedFile>();
		
		return null;
	}
}

	