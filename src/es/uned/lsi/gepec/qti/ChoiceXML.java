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

import java.io.*;
import java.util.List;


import javax.xml.stream.*;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.util.*;

import org.apache.commons.io.FileUtils;

/**
 * Clase contenedora del ítem tipo "Choice". Contiene un campo para cada parámetro del ítem 
 * necesario para construir el archivo del ítem XML. Con los métodos que incluye permite crear 
 * un ítem tipo "Choice" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 */
public class ChoiceXML extends AssessmentItem{
	
	/**
	 * Pregunta del ítem.
	 */
	protected String pregunta;
	/**
	 * Mezclar las respuestas del ítem sí o no.
	 */
	protected boolean shuffle;
	/**
	 * Texto de cada una de las respuestas.
	 */
	protected List<Answer> listarespuestas;
	/**
	 * Número máximo de respuestas seleccionables.
	 */
	protected int maxChoices;
	/**
	 * Array de boolean de respuesta correcta.
	 */
	protected boolean[] correcto;
	/**
	 * Array de boolean de respuesta fija.
	 */
	protected boolean[] fija;
	/**
	 * Cardinalidad de la variable respuesta.
	 */
	protected String cardinalidad;
	
	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de parámetros. 
	 * 
	 * @param 	identificador	identificador del ítem
	 * @param 	titulo			título del ítem
	 * @param 	instrucciones	instrucciones del ítem
	 * @param 	pregunta		pregunta del ítem
	 * @param 	shuffle			mezclar las respuestas o no
	 * @param 	listarespuestas	las respuestas del ítem
	 */
	public ChoiceXML (String identificador, String titulo, String instrucciones, 
			String pregunta, boolean shuffle, List<Answer> listarespuestas) {
		super(identificador, titulo, instrucciones);
		this.pregunta = pregunta;
		this.shuffle = shuffle;
		this.listarespuestas = listarespuestas;
	}
	/**
	 * Identificador de cada una de las respuestas.
	 */
	protected String identificadores[] = new String[8];


	/**
	 * Método que construye el ítem XML tipo "Choice"  en la dirección que recibe como 
	 * parámetro con los campos que tiene almacenados. Devuelve información de estado.
	 * 
	 * @return				información del estado final del proceso
	 * @throws 	IOException	si hay algún problema al intentar escribir el archivo en disco
	 * @see		XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
	 * @see		XMLStreamWriter
	 * @see		StringWriter
	 */
	public MensajeEstado creaItemXML(String path, String req) 
			throws IOException{
		
		MensajeEstado msg = new MensajeEstado();		// Mensaje de salida del proceso
		
		try {
			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xsw = null;				// Writer para escribir datos XML
			StringWriter sw = new StringWriter();	// Buffer en el que escribir
			xsw = xof.createXMLStreamWriter(sw);	// El writer XML escribe en el buffer
			// Escribe cabecera común a todos los tipos de pregunta
			writeCabecera(xsw);
			int maxChoices = 0;
			for (Answer a : listarespuestas){	// Para obtener número respuestas posibles
				if (a.getCorrect()){			// y después la cardinality
				maxChoices = maxChoices + 1;	
				}
			}	
			switch (maxChoices){
			case 1:		cardinalidad = "single"; break;
			default: 	cardinalidad = "multiple"; break;
			}			
			xsw.writeAttribute("identifier", identificador);
			xsw.writeAttribute("title", titulo);
			xsw.writeStartElement("responseDeclaration");	// Declara la variable de respuesta
			xsw.writeAttribute("identifier", "RESPONSE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "identifier");
			xsw.writeStartElement("correctResponse");	// Valores de las respuestas correctas
			int i = 1;
			for (int k=1;k<=listarespuestas.size();k++)
			{
				identificadores[k]="Respuesta" + String.valueOf(k);
			}	
			for (Answer a : listarespuestas){	// Escribe las respuestas posibles
				if (a.getCorrect()){
				xsw.writeStartElement("value");			// Valor correcto
				xsw.writeCharacters(identificadores[i]);
				xsw.writeEndElement();					// Cierra "value"
				}
			i = i +1;	
			}	
			xsw.writeEndElement();							// Cierra "correctResponse"
			xsw.writeEndElement();							// Cierra "responseDeclaration"
			xsw.writeStartElement("outcomeDeclaration");	// Variable de puntuación
			xsw.writeAttribute("identifier", "SCORE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "integer");
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la salida
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("0");
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("itemBody");				// Cuerpo del ítem
			// Las instrucciones se escriben mediante el parser neutro puesto que es XHTML, sin 
			// que el writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("choiceInteraction");		// Interacción tipo choice
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			xsw.writeAttribute("shuffle",Boolean.toString(shuffle));	// Mezclar respuestas
			xsw.writeAttribute("maxChoices",Integer.toString(maxChoices));// Máximas elecciones
			if (pregunta != null && !pregunta.equals("")){
				xsw.writeStartElement("prompt");			// Escribe la pregunta si la hay
				xsw.writeCharacters(pregunta);
				xsw.writeEndElement();						// Cierra "prompt"
			}
			i = 1;
			for (Answer a : listarespuestas){	// Escribe las respuestas posibles
				xsw.writeStartElement("simpleChoice");
				xsw.writeAttribute("identifier", identificadores[i]);
				if (shuffle)								// Si mezcla marca las fijas
					if (a.getFixed())							// Respuesta fija
						xsw.writeAttribute("fixed", Boolean.toString(a.getFixed()));
				xsw.writeCharacters(a.getText());			// Respuestas
				xsw.writeEndElement();						// Cierra "simpleChoice"
				i = i +1;
			}
			xsw.writeEndElement();							// Cierra "choiceInteraction"
			// Escribe el final del ítem XML y cierra el writer
			writePie(xsw, req);
			
			// Vacía y cierra el Writer
			sw.flush();
			sw.close();
			// Pasa a un string el contenido del Writer
			String fich = sw.toString();
			// Fichero de salida
			File file = new File(path + identificador + ".xml");
			// Escribe en el fichero de salida el contenido del ítem
			FileUtils.writeStringToFile(file, fich, null);			
		} catch (XMLStreamException e) {
			// Error en el procesamiento de los datos XML
			msg.setMensaje("Error procesando el archivo XML");
			msg.setEstado(false);
		}
		if (msg.isEstado()){
			// Si todo ha ido bien se iforma del resultado
			msg.setMensaje("Ítem creado con éxito");	
		}
		
		return msg;
	}
}