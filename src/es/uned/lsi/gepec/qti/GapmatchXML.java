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
 * Clase contenedora del �tem tipo "Gap Match". Contiene un campo para cada par�metro del �tem 
 * necesario para construir el archivo del �tem XML. Con los m�todos que incluye permite crear 
 * un �tem tipo "Gap Match" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 * @see		EnteroPositivo
 */
public class GapmatchXML extends AssessmentItem {

	/**
	 * Pregunta del �tem.
	 */
	protected String pregunta;
	/**
	 * Mezclar las opciones aleatoriamente o no.
	 */
	protected boolean shuffle;
	/**
	 * Textos en los que est�n los huecos imbuidos.
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
	 * M�ximo de asociaciones de cada opci�n.
	 */
	protected EnteroPositivo[] maxOpciones;
	/**
	 * Tabla con el n�mero de la opci�n correcta de cada hueco. Es el �ndice de las tablas de 
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
	 * Constructor que permite inicializar el objeto con un primer grupo de par�metros. Son 
	 * los que se recogen en la primera p�gina de la toma de datos. Para los par�metros 
	 * comunes a todos los �tems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del �tem
	 * @param 	titulo			t�tulo del �tem
	 * @param 	instrucciones	instrucciones del �tem
	 * @param 	pregunta		pregunta del �tem
	 * @param 	shuffle			mezclar las respuestas o no
	 */
	public GapmatchXML(String identificador, String titulo, String instrucciones, 
			String pregunta, boolean shuffle) {
		super(identificador, titulo, instrucciones);
		this.pregunta = pregunta;
		this.shuffle = shuffle;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador del hueco en esa misma posici�n.
	 * 
	 * @param 	identHuecos		array con los identificadores de los huecos
	 */
	public void setIdentHuecos(Identificador[] identHuecos) {
		this.identHuecos = identHuecos;
		// Si el n�mero de huecos es 1 la cardinalidad ser� simple
		switch(identHuecos.length){
			case 1: cardinalidad = "single";break;
			default: cardinalidad = "multiple";break;
		}
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador de la opci�n en esa misma posici�n.
	 * 
	 * @param 	identOpciones		array con los identificadores de las opciones
	 */
	public void setIdentOpciones(Identificador[] identOpciones) {
		this.identOpciones = identOpciones;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>EnteroPositivo</code> conteniendo 
	 * en cada elemento el n�mero m�ximo de huecos con los que esa opci�n se puede asociar.
	 * 
	 * @param 	maxOpciones		array con los n�meros m�ximos de asociaciones de cada opci�n
	 */
	public void setMaxOpciones(EnteroPositivo[] maxOpciones) {
		this.maxOpciones = maxOpciones;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>String</code> conteniendo en cada 
	 * elemento una de las opciones con las que se pueden asociar los huecos.
	 * 
	 * @param 	opciones		array con las opciones
	 */
	public void setOpciones(String[] opciones) {
		this.opciones = opciones;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>String</code> con los textos que 
	 * rodean a los huecos. Hay un texto m�s que el n�mero de huecos.
	 * 
	 * @param 	textos			array con los textos que rodean a los huecos
	 */
	public void setTextos(String[] textos) {
		this.textos = textos;
	}
		
	/**
	 * M�todo que inicializa el <code>array</code> de enteros que indica la posici�n de la 
	 * opci�n que es correcta para cada hueco.
	 * 
	 * @param 	respCorrecta	array de enteros con la posici�n de la opci�n correcta de cada 
	 * 							hueco
	 */
	public void setRespCorrecta(String[] respCorrecta) {
		this.respCorrecta = new int[respCorrecta.length];
		for (int i = 0; i < respCorrecta.length; i++)
			this.respCorrecta[i] = Integer.parseInt(respCorrecta[i]);
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>boolean</code> indicando si esa 
	 * opci�n debe quedarse en esa posici�n o mezclarse si se deben mezclar aleatoriamente.
	 * 
	 * @param 	fija			array de boolean indicando posiciones fijas
	 */
	public void setFija(boolean[] fija) {
		this.fija = fija;
	}

	/**
	 * M�todo que construye el �tem XML tipo "Gap Match" con los campos que tiene almacenados 
	 * en la direcci�n que recibe como par�metro. Devuelve informaci�n de estado.
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
			// Las instrucciones se escriben mediante el parser neutro puesto que es xhtml, sin 
			// que el writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("gapMatchInteraction");	// Interacci�n tipo GapMatch
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
			xsw.writeStartElement("p");						// Mete el texto en un p�rrafo
			for (int i = 0; i < identHuecos.length; i++){
				if (textos[i] != null && !textos[i].equals(""))			// Escribe texto 'i'
					// Los Textos se escriben mediante el m�todo de escribir textos XML para 
					// sustituir los caracteres de nueva l�nea por los equivalentes <br /> de 
					// XHTML
					ParserNeutro.escribeTextoXML(textos[i], xsw);
				xsw.writeEmptyElement("gap");				// Hueco 'i'
				// Identificador del hueco 'i'
				xsw.writeAttribute("identifier", identHuecos[i].getIdentificador());
			}
			// Escribe el �ltimo texto
			if (textos[textos.length - 1] != null && !textos[textos.length - 1].equals(""))
				ParserNeutro.escribeTextoXML(textos[textos.length - 1], xsw);
			xsw.writeEndElement();							// Cierra "p"
			xsw.writeEndElement();							// Cierra "blockquote"
			xsw.writeEndElement();							// Cierra "gapMatchInteraction"
			// Escribe el final del �tem XML y cierra el writer
			writePie(xsw, req);
			
			// Vac�a y cierra el Writer
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
			msg.setMensaje("�tem creado con �xito");	
		}
		
		return msg;
	}
}