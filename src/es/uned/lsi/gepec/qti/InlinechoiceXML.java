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
 * Clase contenedora del �tem tipo "Inline Choice". Contiene un campo para cada par�metro del 
 * �tem necesario para construir el archivo del �tem XML. Con los m�todos que incluye permite 
 * crear un �tem tipo "Inline Choice" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 */
public class InlinechoiceXML extends AssessmentItem {
	/**
	 * Mezclar las respuestas aleatoriamente s� o no.
	 */
	protected boolean shuffle;
	/**
	 * Textos de cada una de las respuestas.
	 */
	protected String[] respuestas;
	/**
	 * Texto antes de las respuestas.
	 */
	protected String texto1;
	/**
	 * Texto tras las respuestas.
	 */
	protected String texto2;
	/**
	 * Identificador de cada una de las respuestas.
	 */
	protected Identificador[] identificadores;
	/**
	 * Posici�n de la respuesta correcta.
	 */
	protected int correcto;
	/**
	 * Array con las respuestas que deben ser fijas.
	 */
	protected boolean[] fija;
	/**
	 * Cardinalidad de la variable respuesta que en este caso siempre es "single".
	 */
	String cardinalidad = "single";
	

	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de par�metros. Son 
	 * los que se recogen en la primera p�gina de la toma de datos. Para los par�metros 
	 * comunes a todos los �tems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del �tem
	 * @param 	titulo			t�tulo del �tem
	 * @param 	instrucciones	instrucciones del �tem
	 * @param 	shuffle			mezclar las respuestas o no
	 */
	public InlinechoiceXML(String identificador, String titulo, String instrucciones, 
			boolean shuffle) {
		super(identificador, titulo, instrucciones);
		this.shuffle = shuffle;
	}

	/**
	 * M�todo que inicializa el entero que indica la posici�n de la respuesta que es correcta.
	 * 
	 * @param 	correcto		posici�n de la opci�n que es correcta
	 */
	public void setCorrecto(int correcto) {
		this.correcto = correcto;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>boolean</code> que indica si la 
	 * respuesta en esa misma posici�n debe quedar fija en el caso de que se tengan que 
	 * mezclar aleatoriamente.
	 * 
	 * @param 	fija			array con las respuestas en posici�n fija
	 */
	public void setFija(boolean[] fija) {
		this.fija = fija;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador de la respuesta en esa posici�n.
	 * 
	 * @param 	identificadores	array con los identificadores de las respuestas
	 */
	public void setIdentificadores(Identificador[] identificadores) {
		this.identificadores = identificadores;
	}

	/**
	 * M�todo que inicializa el <code>array</code> con las respuestas posibles a elegir.
	 * 
	 * @param 	respuestas		array con las respuestas
	 */
	public void setRespuestas(String[] respuestas) {
		this.respuestas = respuestas;
	}

	/**
	 * M�todo que inicializa el <code>array</code> con los textos antes y despu�s de las 
	 * opciones.
	 * 
	 * @param 	texto1			string con el texto antes de las respuestas
	 * @param 	texto2			string con el texto despu�s de las respuestas
	 */
	public void setTextos(String texto1, String texto2) {
		this.texto1 = texto1;
		this.texto2 = texto2;
	}

	/**
	 * M�todo que construye el �tem XML tipo "Inline Choice"  en la direcci�n que recibe como 
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
			xsw.writeStartElement("responseDeclaration");	// Variable de respuesta
			xsw.writeAttribute("identifier", "RESPONSE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "identifier");
			xsw.writeStartElement("correctResponse");		// Valor de la respuesta correcta
			xsw.writeStartElement("value");					// Valor correcto
			xsw.writeCharacters(identificadores[correcto].getIdentificador());
			xsw.writeEndElement();							// Cierra "value"
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
			xsw.writeStartElement("blockquote");
			xsw.writeStartElement("p");						// Mete el texto en un p�rrafo
			// Los Textos se escriben mediante el m�todo de escribir textos XML para sustituir 
			// los caracteres de nueva l�nea por los equivalentes <br /> de XHTML
			if (texto1 != null && !texto1.equals(""))
				ParserNeutro.escribeTextoXML(texto1, xsw);		// Texto antes de las opciones
			xsw.writeStartElement("inlineChoiceInteraction");	// Interacci�n tipo inlinechoice
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			xsw.writeAttribute("shuffle",Boolean.toString(shuffle));	// Mezclar preguntas
			for (int i = 0; i < respuestas.length; i++){	// Escribe las respuestas posibles
				xsw.writeStartElement("inlineChoice");
				xsw.writeAttribute("identifier", identificadores[i].getIdentificador());
				if (shuffle)								// Si mezcla marca las fijas
					if (fija[i])							// Respuesta fija			
						xsw.writeAttribute("fixed", Boolean.toString(fija[i]));
				xsw.writeCharacters(respuestas[i]);
				xsw.writeEndElement();						// Cierra "inlineChoice"
			}
			xsw.writeEndElement();							// Cierra "inlineChoiceInteraction"
			// Los Textos se escriben mediante el m�todo de escribir textos XML para sustituir 
			// los caracteres de nueva l�nea por los equivalentes <br /> de XHTML
			if (texto2 != null && !texto2.equals(""))
				ParserNeutro.escribeTextoXML(texto2, xsw);		// Texto tras las opciones
			xsw.writeEndElement();								// Cierra "p"
			xsw.writeEndElement();								// Cierra "blockquote"
			// Escribe el final del �tem XML y cierra el writer
			writePie(xsw, req);
			
			// Vac�a y cierra el Writer
			sw.flush();
			sw.close();
			// Pasa a un String en contenido del Writer
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