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
 * Clase contenedora del ítem tipo "Inline Choice". Contiene un campo para cada parámetro del 
 * ítem necesario para construir el archivo del ítem XML. Con los métodos que incluye permite 
 * crear un ítem tipo "Inline Choice" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 */
public class InlinechoiceXML extends AssessmentItem {
	/**
	 * Mezclar las respuestas aleatoriamente sí o no.
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
	 * Posición de la respuesta correcta.
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
	 * Constructor que permite inicializar el objeto con un primer grupo de parámetros. Son 
	 * los que se recogen en la primera página de la toma de datos. Para los parámetros 
	 * comunes a todos los ítems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del ítem
	 * @param 	titulo			título del ítem
	 * @param 	instrucciones	instrucciones del ítem
	 * @param 	shuffle			mezclar las respuestas o no
	 */
	public InlinechoiceXML(String identificador, String titulo, String instrucciones, 
			boolean shuffle) {
		super(identificador, titulo, instrucciones);
		this.shuffle = shuffle;
	}

	/**
	 * Método que inicializa el entero que indica la posición de la respuesta que es correcta.
	 * 
	 * @param 	correcto		posición de la opción que es correcta
	 */
	public void setCorrecto(int correcto) {
		this.correcto = correcto;
	}

	/**
	 * Método que inicializa el <code>array</code> de <code>boolean</code> que indica si la 
	 * respuesta en esa misma posición debe quedar fija en el caso de que se tengan que 
	 * mezclar aleatoriamente.
	 * 
	 * @param 	fija			array con las respuestas en posición fija
	 */
	public void setFija(boolean[] fija) {
		this.fija = fija;
	}

	/**
	 * Método que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posición el identificador de la respuesta en esa posición.
	 * 
	 * @param 	identificadores	array con los identificadores de las respuestas
	 */
	public void setIdentificadores(Identificador[] identificadores) {
		this.identificadores = identificadores;
	}

	/**
	 * Método que inicializa el <code>array</code> con las respuestas posibles a elegir.
	 * 
	 * @param 	respuestas		array con las respuestas
	 */
	public void setRespuestas(String[] respuestas) {
		this.respuestas = respuestas;
	}

	/**
	 * Método que inicializa el <code>array</code> con los textos antes y después de las 
	 * opciones.
	 * 
	 * @param 	texto1			string con el texto antes de las respuestas
	 * @param 	texto2			string con el texto después de las respuestas
	 */
	public void setTextos(String texto1, String texto2) {
		this.texto1 = texto1;
		this.texto2 = texto2;
	}

	/**
	 * Método que construye el ítem XML tipo "Inline Choice"  en la dirección que recibe como 
	 * parámetro con los campos que tiene almacenados. Devuelve información de estado.
	 * 
	 * @param 	path		dirección absoluta en la que crear el archivo XML
	 * @param	sc			servletContext para acceder a los parámetros iniciales de "web.xml"
	 * @param	req			string con la url base de la Aplicación Web requerida
	 * @return				información del estado final del proceso
	 * @throws 	IOException	si hay algún problema al intentar escribir el archivo en disco
	 * @see		XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
	 * @see		XMLStreamWriter
	 * @see		StringWriter
	 */
	public MensajeEstado creaItemXML(String path, String req) 
			throws IOException {
		
		MensajeEstado msg = new MensajeEstado();		// Mensaje de salida del método
		
		try {
			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xsw = null;				// Writer para escribir datos XML
			StringWriter sw = new StringWriter();	// Buffer en el que escribir
			xsw = xof.createXMLStreamWriter(sw);	// El writer XML escribe en el buffer
			// Escribe cabecera común a todos los tipos de pregunta
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
			xsw.writeStartElement("outcomeDeclaration");	// Variable de puntuación
			xsw.writeAttribute("identifier", "SCORE");
			xsw.writeAttribute("cardinality", "single");
			xsw.writeAttribute("baseType", "integer");
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la salida
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("0");
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("itemBody");				// Cuerpo del ítem
			// Las instrucciones se escriben directamente puesto que es xhtml, sin que el 
			// writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("blockquote");
			xsw.writeStartElement("p");						// Mete el texto en un párrafo
			// Los Textos se escriben mediante el método de escribir textos XML para sustituir 
			// los caracteres de nueva línea por los equivalentes <br /> de XHTML
			if (texto1 != null && !texto1.equals(""))
				ParserNeutro.escribeTextoXML(texto1, xsw);		// Texto antes de las opciones
			xsw.writeStartElement("inlineChoiceInteraction");	// Interacción tipo inlinechoice
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
			// Los Textos se escriben mediante el método de escribir textos XML para sustituir 
			// los caracteres de nueva línea por los equivalentes <br /> de XHTML
			if (texto2 != null && !texto2.equals(""))
				ParserNeutro.escribeTextoXML(texto2, xsw);		// Texto tras las opciones
			xsw.writeEndElement();								// Cierra "p"
			xsw.writeEndElement();								// Cierra "blockquote"
			// Escribe el final del ítem XML y cierra el writer
			writePie(xsw, req);
			
			// Vacía y cierra el Writer
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
			msg.setMensaje("Ítem creado con éxito");	
		}
		
		return msg;
	}
}