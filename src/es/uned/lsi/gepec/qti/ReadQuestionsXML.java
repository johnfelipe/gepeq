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
package es.uned.lsi.gepec.qti;

import java.util.List;
import java.io.File;


import es.uned.lsi.gepec.model.dao.AnswersDao;
import es.uned.lsi.gepec.model.dao.CategoriesDao;
import es.uned.lsi.gepec.model.dao.QuestionsDao;
import es.uned.lsi.gepec.model.dao.UsersDao;
import es.uned.lsi.gepec.model.entities.*;
import es.uned.lsi.gepec.qti.ReadTrueFalseXML;
import es.uned.lsi.gepec.model.entities.Category;
import es.uned.lsi.gepec.model.entities.User;

public class ReadQuestionsXML {
		private static QuestionsDao QDao = new QuestionsDao();
		private static AnswersDao ADao = new AnswersDao();

	public boolean doLogic (String ruta, String Questiontype, User user,Category category){
		ReadTrueFalseXML read = new ReadTrueFalseXML();
		ReadChoiceXML readchoice = new ReadChoiceXML();
		File dir = new File(ruta);
		String[] ficheros = dir.list();
		if (ficheros == null)
		{}
		else
		{
		for (int x=0;x<ficheros.length;x++)
		{
		if(Questiontype.equals("truefalse"))
		{	
		List<TrueFalseQuestion> readConfig = read.readConfig(ruta + "\\" + ficheros[x]);
		for (TrueFalseQuestion question : readConfig) {
			question.setCategory(category);
			question.setCreatedBy(user);
			question.setModifiedBy(user);
			QDao.saveQuestion(question);
		List<Answer> listarespuestas = question.getAnswers();
        for(Answer c : listarespuestas) 
        { 
        	c.setQuestion(question);
        	ADao.saveAnswer(c);
        }        
		}
		}
		else
		{
			List<MultichoiceQuestion> readConfig = readchoice.readConfig(ruta + "\\" + ficheros[x]);
			for (MultichoiceQuestion question : readConfig) {
				question.setCategory(category);
				question.setCreatedBy(user);
				question.setModifiedBy(user);
				QDao.saveQuestion(question);
			List<Answer> listarespuestas = question.getAnswers();
	        for(Answer c : listarespuestas) 
	        { 
	        	c.setQuestion(question);
	        	ADao.saveAnswer(c);
	        }        
			}
		}		
		//end files
		}
		}
		return true;
	}
}
