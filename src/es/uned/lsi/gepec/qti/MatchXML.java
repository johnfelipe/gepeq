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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;

import es.uned.lsi.gepec.util.*;

/**
 * Clase contenedora del �tem tipo "Match". Contiene un campo para cada par�metro del �tem 
 * necesario para construir el archivo del �tem XML. Con los m�todos que incluye permite crear 
 * un �tem tipo "Match" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 * @see		EnteroPositivo
 */
public class MatchXML extends AssessmentItem {

	/**
	 * Pregunta del �tem.
	 */
	protected String pregunta;
	/**
	 * Mezclar las respuestas aleatoriamente s� o no.
	 */
	protected boolean shuffle;
	/**
	 * Respuestas del grupo 1.
	 */
	protected String[] respuestas1;
	/**
	 * Respuestas del grupo 2.
	 */
	protected String[] respuestas2;
	/**
	 * Identificadores de las respuestas del grupo 1.
	 */
	protected Identificador[] identificadores1;
	/**
	 * Identificadores de las respuestas del grupo 2.
	 */
	protected Identificador[] identificadores2;
	/**
	 * Posiciones fijas de las respuestas del grupo 1.
	 */
	protected boolean[] fija1;
	/**
	 * Posiciones fijas de las respuestas del grupo 2.
	 */
	protected boolean[] fija2;
	/**
	 * N�mero total m�ximo de elecciones.
	 */
	protected int maxChoices;
	/**
	 * N�mero m�ximo de elecciones  de cada respuesta del grupo 1.
	 */
	protected EnteroPositivo[] max1;
	/**
	 * N�mero m�ximo de elecciones  de cada respuesta del grupo 2.
	 */
	protected EnteroPositivo[] max2;
	/**
	 * Combinaciones correctas entre los dos grupos.
	 */
	protected boolean[][] respCorrecta;
	/**
	 * Cardinalidad de la variable de respuesta.
	 */
	protected String cardinalidad;
	

	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de par�metros. Son 
	 * los que se recogen en la primera p�gina de la toma de datos. Para los par�metros 
	 * comunes a todos los �tems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador		identificador del �tem
	 * @param 	titulo				t�tulo del �tem
	 * @param 	instrucciones		instrucciones del �tem
	 * @param 	pregunta			pregunta del �tem
	 * @param 	shuffle				mezclar las respuestas o no
	 */
	public MatchXML(String identificador, String titulo, String instrucciones, 
			String pregunta, boolean shuffle) {
		super(identificador, titulo, instrucciones);
		this.pregunta = pregunta;
		this.shuffle = shuffle;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>boolean</code> que indica si la 
	 * respuesta del grupo 1 en esa misma posici�n debe quedar fija en el caso de que se 
	 * tengan que mezclar aleatoriamente.
	 * 
	 * @param 	fija1				array con las respuestas en posici�n fija del grupo 1
	 */
	public void setFija1(boolean[] fija1) {
		this.fija1 = fija1;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>boolean</code> que indica si la 
	 * respuesta del grupo 2 en esa misma posici�n debe quedar fija en el caso de que se 
	 * tengan que mezclar aleatoriamente.
	 * 
	 * @param 	fija2				array con las respuestas en posici�n fija del grupo 2
	 */
	public void setFija2(boolean[] fija2) {
		this.fija2 = fija2;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador de la opci�n del grupo 1 en esa posici�n.
	 * 
	 * @param 	identificadores1	array con los identificadores de las opciones del grupo 1
	 */
	public void setIdentificadores1(Identificador[] identificadores1) {
		this.identificadores1 = identificadores1;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador de la opci�n del grupo 2 en esa posici�n.
	 * 
	 * @param 	identificadores2	array con los identificadores de las opciones del grupo 2
	 */
	public void setIdentificadores2(Identificador[] identificadores2) {
		this.identificadores2 = identificadores2;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>EnteroPositivo</code> conteniendo 
	 * en cada elemento el n�mero m�ximo de opciones del grupo 2 con las que esa opci�n del 
	 * grupo 1 se puede asociar.
	 * 
	 * @param 	max1				array con los n�meros m�ximos de asociaciones de cada 
	 * 								opci�n
	 */
	public void setMax1(EnteroPositivo[] max1) {
		this.max1 = max1;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>EnteroPositivo</code> conteniendo 
	 * en cada elemento el n�mero m�ximo de opciones del grupo 1 con las que esa opci�n del 
	 * grupo 2 se puede asociar.
	 * 
	 * @param 	max2				array con los n�meros m�ximos de asociaciones de cada 
	 * 								opci�n
	 */
	public void setMax2(EnteroPositivo[] max2) {
		this.max2 = max2;
	}

	/**
	 * M�todo que inicializa el n�mero m�ximo de asociaciones en total que se pueden hacer 
	 * entre elementos del grupo 1 y del grupo 2.
	 *  
	 * @param 	maxChoices		n�mero m�ximo de asociaciones que se pueden hacer
	 */
	public void setMaxChoices(int maxChoices) {
		this.maxChoices = maxChoices;
		switch(maxChoices){
			case 1: 	cardinalidad = "single";
			default:	cardinalidad = "multiple";
		}
	}

	/**
	 * M�todo que inicializa el <code>array</code> de dos dimensiones de <code>boolean</code> 
	 * que indica si la combinaci�n de la opci�n "i" del grupo 1 con la "j" del grupo 2 es 
	 * correcta o no.
	 * 
	 * @param 	respCorrecta		array con las respuestas que son correctas
	 */
	public void setRespCorrecta(boolean[][] respCorrecta) {
		this.respCorrecta = respCorrecta;
	}

	/**
	 * M�todo que inicializa el <code>array</code> con las opciones posibles del grupo 1.
	 * 
	 * @param 	respuestas1		array con las opciones del grupo 1
	 */
	public void setRespuestas1(String[] respuestas1) {
		this.respuestas1 = respuestas1;
	}

	/**
	 * M�todo que inicializa el <code>array</code> con las opciones posibles del grupo 2.
	 * 
	 * @param 	respuestas2		array con las opciones del grupo 2
	 */
	public void setRespuestas2(String[] respuestas2) {
		this.respuestas2 = respuestas2;
	}

	/**
	 * M�todo que construye el �tem XML tipo "Match"  en la direcci�n que recibe como 
	 * par�metro con los campos que tiene almacenados. Devuelve informaci�n de estado.
	 * 
	 * @param 	path		direcci�n absoluta en la que crear el archivo XML
	 * @param	sc			servletContext para acceder a los par�metros iniciales de "web.xml"
	 * @param	req			string con la url base de la Aplicaci�n Web requerida
	 * @return				informaci�n del estado final del proceso
	 * @throws 	IOException	si hay alg�n problema al intentar escribir el archivo en disco
	 * @see		XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
	 * @see		XMLStreamWriter
	 * @see		StringWriter
	 */
	public MensajeEstado creaItemXML(String path, String req) 
			throws IOException {
		
		MensajeEstado msg = new MensajeEstado();		// Mensaje de salida del m�todo
		
		try {
			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xsw = null;				// Writer para escribir datos XML
			StringWriter sw = new StringWriter();	// Buffer en el que escribir
			xsw = xof.createXMLStreamWriter(sw);	// El writer XML escribe en el buffer
			// Escribe cabecera com�n a todos los tipos de pregunta
			writeCabecera(xsw);
			xsw.writeAttribute("identifier", identificador);
			xsw.writeAttribute("title", titulo);
			xsw.writeStartElement("responseDeclaration");	// Declara la variable de respuesta
			xsw.writeAttribute("identifier", "RESPONSE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "directedPair");
			xsw.writeStartElement("correctResponse");	// Valores de las respuestas correctas
			for (int i = 0; i < respuestas1.length; i++){
				for (int j = 0; j < respuestas2.length; j++)
				if (respCorrecta[i][j]){
					xsw.writeStartElement("value");			// Valor correcto
					xsw.writeCharacters(identificadores1[i].getIdentificador() + " ");
					xsw.writeCharacters(identificadores2[j].getIdentificador());
					xsw.writeEndElement();					// Cierra "value"
				}
			}
			xsw.writeEndElement();							// Cierra "correctResponse"
			xsw.writeEndElement();							// Cierra "responseDeclaration"
			xsw.writeStartElement("outcomeDeclaration");	// Variable de puntuaci�n
			xsw.writeAttribute("identifier", "SCORE");
			xsw.writeAttribute("cardinality", "single");
			xsw.writeAttribute("baseType", "integer");
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la salida
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("0");
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("itemBody");				// Cuerpo del �tem
			// Las instrucciones se escriben directamente puesto que es xhtml, sin que el 
			// writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("matchInteraction");		// Interacci�n tipo match
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			xsw.writeAttribute("shuffle",Boolean.toString(shuffle));	// Mezclar opciones
			xsw.writeAttribute("maxAssociations",Integer.toString(maxChoices));
			if (pregunta != null && !pregunta.equals("")){
				xsw.writeStartElement("prompt");			// Escribe la pregunta si la hay
				xsw.writeCharacters(pregunta);
				xsw.writeEndElement();						// Cierra "prompt"
			}
			xsw.writeStartElement("simpleMatchSet");		// Primer grupo de opciones
			for (int i = 0; i < respuestas1.length; i++){	// Escribe las respuestas posibles
				xsw.writeStartElement("simpleAssociableChoice");
				xsw.writeAttribute("identifier", identificadores1[i].getIdentificador());
				if (shuffle)								// Si mezcla marca las fijas
					if (fija1[i])							// Respuesta fija
						xsw.writeAttribute("fixed", Boolean.toString(fija1[i]));
				xsw.writeAttribute("matchMax", Integer.toString(max1[i].getEnteroPositivo()));
				xsw.writeCharacters(respuestas1[i]);		// Opciones
				xsw.writeEndElement();						// Cierra "simpleAssociableChoice"
			}
			xsw.writeEndElement();						// Cierra el primer "simpleMatchSet"
			xsw.writeStartElement("simpleMatchSet");		// Segundo grupo de opciones
			for (int i = 0; i < respuestas2.length; i++){	// Escribe las respuestas posibles
				xsw.writeStartElement("simpleAssociableChoice");
				xsw.writeAttribute("identifier", identificadores2[i].getIdentificador());
				if (shuffle)								// Si mezcla marca las fijas
					if (fija2[i])							// Respuesta fija
						xsw.writeAttribute("fixed", Boolean.toString(fija2[i]));
				xsw.writeAttribute("matchMax", Integer.toString(max2[i].getEnteroPositivo()));
				xsw.writeCharacters(respuestas2[i]);
				xsw.writeEndElement();						// Cierra "simpleAssociableChoice"
			}
			xsw.writeEndElement();						// Cierra el segundo "simpleMatchSet"
			xsw.writeEndElement();							// Cierra "choiceInteraction"
			// Escribe el final del �tem XML y cierra el writer
			writePie(xsw, req);
			
			// Vac�a y cierra el Writer
			sw.flush();
			sw.close();
			// Pasa a String en contenido del Writer
			String fich = sw.toString();
			// Fichero de salida
			File file = new File(path + identificador + ".xml");
			// Escribe en el fichero de salida
			FileUtils.writeStringToFile(file, fich, null);			
		} catch (XMLStreamException e) {
			// Error en el procesamiento de los datos XML
			msg.setMensaje("Error procesando el archivo XML");
			msg.setEstado(false);
		}
		if (msg.isEstado()){
			// Si todo ha ido bien se iforma del resultado
			msg.setMensaje("�tem creado con �xito");	
		}
		
		return msg;
	}
}