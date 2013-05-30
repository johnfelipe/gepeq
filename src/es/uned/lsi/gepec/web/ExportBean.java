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
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.DualListModel;

import es.uned.lsi.gepec.model.QuestionLevel;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionType;
import es.uned.lsi.gepec.qti.QuestionsXML;
import es.uned.lsi.gepec.util.HibernateUtil.Operation;
import es.uned.lsi.gepec.web.services.CategoriesService;
import es.uned.lsi.gepec.web.services.ConfigurationService;
import es.uned.lsi.gepec.web.services.QuestionTypesService;
import es.uned.lsi.gepec.web.services.QuestionsService;
import es.uned.lsi.gepec.web.services.UserSessionService;

/**
 * Backbean para la vista exportar
 * 
 * @author Víctor Manuel Alonso Rodríguez
 * @since  12/2011
 */
@ManagedBean(name="exportBean")
@RequestScoped
public class ExportBean {
	@ManagedProperty(value="#{configurationService}")
	private ConfigurationService configurationService;
	@ManagedProperty(value="#{categoriesService}")
	private CategoriesService categoriesService;
	@ManagedProperty(value="#{questionsService}")
	private QuestionsService questionsService;
	@ManagedProperty(value="#{questionTypesService}")
	private QuestionTypesService questionTypesService;
	@ManagedProperty(value="#{userSessionService}")
	private UserSessionService userSessionService;
	
	private DualListModel<Question> questions;	// Listas de preguntas
	private long categoryId;					// Id de la categoría seleccionada
	private String level;						// Nivel de dificultad seleccionado
	private String type;						// Tipo de pregunta seleccionado

	public ExportBean() {
		type = "";
		level = "";
	}
	
	public void setQuestionsService(QuestionsService questionsService) {
		this.questionsService = questionsService;
	}
	
	public void setQuestionTypesService(QuestionTypesService questionTypesService) {
		this.questionTypesService = questionTypesService;
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
     * @return true if current user is allowed to navigate "Export" page, false otherwise
     */
    public boolean isNavigationAllowed()
    {
    	return isNavigationAllowed(null);
    }
    
    /**
     * @param operation Operation
     * @return true if current user is allowed to navigate "Export" page, false otherwise
     */
    private boolean isNavigationAllowed(Operation operation)
    {
    	return userSessionService.isGranted(getCurrentUserOperation(operation),"PERMISSION_NAVIGATION_EXPORT");
    }
	
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setQuestions(DualListModel<Question> questions) {
		this.questions = questions;
	}
	
	public DualListModel<Question> getQuestions() {
		if(questions == null)
		{
			List<Question> source =
				questionsService.getQuestions(userSessionService.getCurrentUser(), categoryId, false, type, level);
			List<Question> target = new ArrayList<Question>();
			
			questions = new DualListModel<Question>(source, target);
		}
		
		return questions;
	}
	
	/**
     * Obtiene las preguntas disponibles para los criterios de filtrado
     * 
     * @param categoryId
     * @param questionType
     * @param questionLevel
     * @return
     */
    public List<Question> getQuestions(long categoryId, String questionType, String questionLevel) {
    	return questionsService.getQuestions(userSessionService.getCurrentUser(), categoryId, false, questionType, 
    		questionLevel);
    }
    
    /**
	 * Obtiene las categorías del usuario
	 * 
	 * @return
	 */
    public List<Category> getCategories() {
    	List<Category> categories = 
    			categoriesService.getCategoriesSortedByHierarchy(userSessionService.getCurrentUser());
    	
    	if(categoryId == 0)
    	{
    		categoryId = categories.get(0).getId();
    	}
		
    	return categories;
	}
    
    /**
	 * Obtiene los tipos de pregunta
	 * 
	 * @return
	 */
    public List<QuestionType> getQuestionTypes() {
		return questionTypesService.getQuestionTypes();
	}
    
    /**
	 * Obtiene los niveles de pregunta
	 * 
	 * @return
	 */
    public List<QuestionLevel> getQuestionLevels() {
		return questionsService.getQuestionLevels();
	}
	
    /**
     * Exporta las preguntas seleccionadas y cambia a la vista de resumen
     * 
     * @return Vista resumen si la exportación es correcta 
     */
	public String exportQuestions() {
    	if(questions.getTarget().size() == 0)	// No hay preguntas, seguimos en la vista actual
    	{
    		return null;
    	}
    	else
    	{
    		List<Question> exportQuestions = new ArrayList<Question>();
    		
    		// Preparamos carpeta de exportación
        	String path = configurationService.getApplicationPath() + 
        		File.separatorChar + configurationService.getQtiExportQuestionsFolder() +
        		File.separatorChar + "u" + userSessionService.getCurrentUser().getId();
        	
        	File folder = new File(path);
        	if(!folder.exists())
        	{
        		folder.mkdir();
        	}
        	else
        	{
        		for(File file: folder.listFiles())
        		{
        			file.delete();
        		}
        	}
        	
        	// Añadimos a la lista todas las preguntas
    		for(Question q: questions.getTarget())		// Obtenemos preguntas completas
    		{
    			Question question = questionsService.getQuestion(q.getId());
    			exportQuestions.add(question);
    		}
    		
    		// Generamos los ficheros QTI
    		QuestionsXML questionsXML = new QuestionsXML();
        	try {
        		questionsXML.doLogic(folder.getAbsolutePath(), exportQuestions);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
    	}
    	
    	return "exportsummary";
	}
}

	