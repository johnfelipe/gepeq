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

import java.io.IOException;
import java.util.List;

import es.uned.lsi.gepec.model.*;
import es.uned.lsi.gepec.model.dao.QuestionsDao;
import es.uned.lsi.gepec.model.entities.*;
import es.uned.lsi.gepec.qti.TrueFalseXML;
import es.uned.lsi.gepec.qti.ChoiceXML;
import es.uned.lsi.gepec.qti.FinitemTrueFalse;

public class QuestionsXML {

	public boolean doLogic(String ruta, List<Question> listquestions) throws IOException {

	QuestionsDao qDao = new QuestionsDao();
	for (Question a : listquestions){	// Escribe las respuestas posibles
	{
	String type = a.getType();	
	// Obtenemos pregunta sin la categoría pero con las respuestas incluídas
	if (type.equals("TRUE_FALSE"))
	{	
	TrueFalseQuestion question = (TrueFalseQuestion)qDao.getQuestion(a.getId(),true,true,true,false,true);
	String identificador = "I" + String.valueOf(question.getId());	// Identificador del ítem 
	String titulo = question.getName();
	String instrucciones = "";	// Instrucciones del ítem
	String pregunta = question.getQuestionText();// Pregunta del ítem
	boolean shuffle = true;//question.getShuffle(); //question.getShuffle();		// Valor por defecto del parámetro
	List<Answer> listarespuestas = question.getAnswers();
	TrueFalseXML chXML = new TrueFalseXML(identificador, titulo, instrucciones,
			pregunta, shuffle, question.getCorrectAnswer());
	FinitemTrueFalse finitem = new FinitemTrueFalse();
	try {
		boolean fichero = finitem.doLogic(ruta, identificador, titulo, pregunta, question.getCorrectAnswer() );
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	else
	{
		MultichoiceQuestion question = (MultichoiceQuestion)qDao.getQuestion(a.getId(),true,true,true,false,true);
		String identificador = "I" + String.valueOf(question.getId());	// Identificador del ítem 
		String titulo = question.getName();
		String instrucciones = "";	// Instrucciones del ítem
		String pregunta = question.getQuestionText();// Pregunta del ítem
		boolean shuffle = question.getShuffle(); //question.getShuffle();		// Valor por defecto del parámetro
		List<Answer> listarespuestas = question.getAnswers();
		ChoiceXML chXML = new ChoiceXML(identificador, titulo, instrucciones,
				pregunta, shuffle,listarespuestas );
		Finitem finitem = new Finitem();
		try {
			boolean fichero = finitem.doLogic(ruta, identificador, titulo, pregunta, listarespuestas );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	}
	}
	return true;
	}
}
