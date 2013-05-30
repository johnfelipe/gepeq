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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.UserSessionService;

/**
 * Backbean para la vista exportar
 * 
 * @author Víctor Manuel Alonso Rodríguez
 * @since  12/2011
 */
@ManagedBean(name="exportSummaryBean")
@RequestScoped
public class ExportSummaryBean {
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	
	private List<StreamedContent> files;	// Enlaces a los ficheros xml de las preguntas

	public ExportSummaryBean() {
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	public void setUserSessionService(UserSessionService userSessionService) {
		this.userSessionService = userSessionService;
	}

	public List<StreamedContent> getFiles() {	
		if(files == null)
		{
			createFileList();
		}
		
		return files;
	}
	
	/**
	 * Creamos la lista de ficheros desde la carpeta de exportación
	 */
	private void createFileList()
	{
		files = new ArrayList<StreamedContent>();
		String path = configurationService.getApplicationPath() + 
	        	File.separatorChar + configurationService.getQtiExportQuestionsFolder() +
	        	File.separatorChar + "u" + userSessionService.getCurrentUser().getId();
		
		File folder = new File(path);
    	
		for(File file: folder.listFiles())		// Obtenemos preguntas completas
		{
			
			try {
				InputStream stream;
				stream = new FileInputStream(file);
				
				StreamedContent sc = new DefaultStreamedContent(stream, "text/xml", file.getName());
				files.add(sc);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

	