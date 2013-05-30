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
 * Clase contenedora del ítem tipo "Gap Match". Contiene un campo para cada parámetro del ítem 
 * necesario para construir el archivo del ítem XML. Con los métodos que incluye permite crear 
 * un ítem tipo "Gap Match" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 * @see		EnteroPositivo
 */
public class GapmatchXML extends AssessmentItem {

	/**
	 * Pregunta del ítem.
	 */
	protected String pregunta;
	/**
	 * Mezclar las opciones aleatoriamente o no.
	 */
	protected boolean shuffle;
	/**
	 * Textos en los que están los huecos imbuidos.
	 */
	protected String[] textos;
	/**
	 * Valores posibles que pueden tomar los huecos.
	 */
	protected String[] opciones;
	/**
	 * Identificadores de las opciones.
	 */
	protected Identificador[] identOpciones;
	/**
	 * Identificadores de los huecos.
	 */
	protected Identificador[] identHuecos;
	/**
	 * Máximo de asociaciones de cada opción.
	 */
	protected EnteroPositivo[] maxOpciones;
	/**
	 * Tabla con el número de la opción correcta de cada hueco. Es el índice de las tablas de 
	 * opciones.
	 */
	protected int[] respCorrecta;
	/**
	 * Array con las opciones que deben ser fijas.
	 */
	protected boolean[] fija;
	/**
	 * Cardinalidad de la variable respuesta.
	 */
	protected String cardinalidad;
	
	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de parámetros. Son 
	 * los que se recogen en la primera página de la toma de datos. Para los parámetros 
	 * comunes a todos los ítems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del ítem
	 * @param 	titulo			título del ítem
	 * @param 	instrucciones	instrucciones del ítem
	 * @param 	pregunta		pregunta del ítem
	 * @param 	shuffle			mezclar las respuestas o no
	 */
	public GapmatchXML(String identificador, String titulo, String instrucciones, 
			String pregunta, boolean shuffle) {
		super(identificador, titulo, instrucciones);
		this.pregunta = pregunta;
		this.shuffle = shuffle;
	}

	/**
	 * Método que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posición el identificador del hueco en esa misma posición.
	 * 
	 * @param 	identHuecos		array con los identificadores de los huecos
	 */
	public void setIdentHuecos(Identificador[] identHuecos) {
		this.identHuecos = identHuecos;
		// Si el número de huecos es 1 la cardinalidad será simple
		switch(identHuecos.length){
			case 1: cardinalidad = "single";break;
			default: cardinalidad = "multiple";break;
		}
	}

	/**
	 * Método que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posición el identificador de la opción en esa misma posición.
	 * 
	 * @param 	identOpciones		array con los identificadores de las opciones
	 */
	public void setIdentOpciones(Identificador[] identOpciones) {
		this.identOpciones = identOpciones;
	}

	/**
	 * Método que inicializa el <code>array</code> de <code>EnteroPositivo</code> conteniendo 
	 * en cada elemento el número máximo de huecos con los que esa opción se puede asociar.
	 * 
	 * @param 	maxOpciones		array con los números máximos de asociaciones de cada opción
	 */
	public void setMaxOpciones(EnteroPositivo[] maxOpciones) {
		this.maxOpciones = maxOpciones;
	}

	/**
	 * Método que inicializa el <code>array</code> de <code>String</code> conteniendo en cada 
	 * elemento una de las opciones con las que se pueden asociar los huecos.
	 * 
	 * @param 	opciones		array con las opciones
	 */
	public void setOpciones(String[] opciones) {
		this.opciones = opciones;
	}

	/**
	 * Método que inicializa el <code>array</code> de <code>String</code> con los textos que 
	 * rodean a los huecos. Hay un texto más que el número de huecos.
	 * 
	 * @param 	textos			array con los textos que rodean a los huecos
	 */
	public void setTextos(String[] textos) {
		this.textos = textos;
	}
		
	/**
	 * Método que inicializa el <code>array</code> de enteros que indica la posición de la 
	 * opción que es correcta para cada hueco.
	 * 
	 * @param 	respCorrecta	array de enteros con la posición de la opción correcta de cada 
	 * 							hueco
	 */
	public void setRespCorrecta(String[] respCorrecta) {
		this.respCorrecta = new int[respCorrecta.length];
		for (int i = 0; i < respCorrecta.length; i++)
			this.respCorrecta[i] = Integer.parseInt(respCorrecta[i]);
	}

	/**
	 * Método que inicializa el <code>array</code> de <code>boolean</code> indicando si esa 
	 * opción debe quedarse en esa posición o mezclarse si se deben mezclar aleatoriamente.
	 * 
	 * @param 	fija			array de boolean indicando posiciones fijas
	 */
	public void setFija(boolean[] fija) {
		this.fija = fija;
	}

	/**
	 * Método que construye el ítem XML tipo "Gap Match" con los campos que tiene almacenados 
	 * en la dirección que recibe como parámetro. Devuelve información de estado.
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
			xsw.writeStartElement("responseDeclaration");	// Declara la variable de respuesta
			xsw.writeAttribute("identifier", "RESPONSE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "identifier");
			xsw.writeStartElement("correctResponse");	// Valores de las respuestas correctas
			for (int i = 0; i < identHuecos.length; i++){
				xsw.writeStartElement("value");			// Valor correcto
				xsw.writeCharacters(identHuecos[i].getIdentificador() + " ");
				xsw.writeCharacters(identOpciones[respCorrecta[i]].getIdentificador());
				xsw.writeEndElement();					// Cierra "value"
			}
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
			// Las instrucciones se escriben mediante el parser neutro puesto que es xhtml, sin 
			// que el writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("gapMatchInteraction");	// Interacción tipo GapMatch
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			xsw.writeAttribute("shuffle",Boolean.toString(shuffle));		// Mezclar opciones
			if (pregunta != null && !pregunta.equals("")){
				xsw.writeStartElement("prompt");			// Escribe la pregunta si la hay
				xsw.writeCharacters(pregunta);
				xsw.writeEndElement();						// Cierra "prompt"
			}
			for (int i = 0; i < identOpciones.length; i++){	// Escribe las opciones posibles
				xsw.writeStartElement("gapText");
				xsw.writeAttribute("identifier", identOpciones[i].getIdentificador());
				xsw.writeAttribute("matchMax",Integer.toString(
						maxOpciones[i].getEnteroPositivo()));
				if (shuffle)								// Si mezcla marca las fijas
					if (fija[i])							// Respuesta fija
						xsw.writeAttribute("fixed", Boolean.toString(fija[i]));
				xsw.writeCharacters(opciones[i]);
				xsw.writeEndElement();						// Cierra "gapText"
			}
			xsw.writeStartElement("blockquote");
			xsw.writeStartElement("p");						// Mete el texto en un párrafo
			for (int i = 0; i < identHuecos.length; i++){
				if (textos[i] != null && !textos[i].equals(""))			// Escribe texto 'i'
					// Los Textos se escriben mediante el método de escribir textos XML para 
					// sustituir los caracteres de nueva línea por los equivalentes <br /> de 
					// XHTML
					ParserNeutro.escribeTextoXML(textos[i], xsw);
				xsw.writeEmptyElement("gap");				// Hueco 'i'
				// Identificador del hueco 'i'
				xsw.writeAttribute("identifier", identHuecos[i].getIdentificador());
			}
			// Escribe el último texto
			if (textos[textos.length - 1] != null && !textos[textos.length - 1].equals(""))
				ParserNeutro.escribeTextoXML(textos[textos.length - 1], xsw);
			xsw.writeEndElement();							// Cierra "p"
			xsw.writeEndElement();							// Cierra "blockquote"
			xsw.writeEndElement();							// Cierra "gapMatchInteraction"
			// Escribe el final del ítem XML y cierra el writer
			writePie(xsw, req);
			
			// Vacía y cierra el Writer
			sw.flush();
			sw.close();
			// Escribe en un String el contenido del Writer
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