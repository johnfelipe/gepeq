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
 * Clase contenedora del �tem tipo "Text Entry". Contiene un campo para cada par�metro del 
 * �tem necesario para construir el archivo del �tem XML. Con los m�todos que incluye permite 
 * crear un �tem tipo "Text Entry" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 */
public class TextentryXML extends AssessmentItem {

	/**
	 * Respuesta correcta del �tem.
	 */
	protected String respuesta;
	/**
	 * Longitud esperada de la respuesta; inicializada a cero si no se debe incluir el atributo.
	 */
	protected int longitud = 0; 
	/**
	 * Texto antes de la respuesta.
	 */
	protected String texto1;
	/**
	 * Texto despu�s de la respuesta.
	 */
	protected String texto2;
	/**
	 * Cardinalidad de la variable de respuesta. En este caso siempre es "single".
	 */
	protected String cardinalidad = "single";
	
	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de par�metros. Son 
	 * los que se recogen en la primera p�gina de la toma de datos. En este caso s�lo se 
	 * recogen los que son comunes, as� que simplemente llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del �tem
	 * @param 	titulo			t�tulo del �tem
	 * @param 	instrucciones	instrucciones del �tem
	 */
	public TextentryXML(String identificador, String titulo, String instrucciones) {
		super(identificador, titulo, instrucciones);
	}

	/**
	 * M�todo que inicializa el valor de <code>longitud</code>, la longitud esperada de la 
	 * respuesta.
	 *  
	 * @param 	longitud		entero con la longitud esperada de la respuesta
	 */
	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}

	/**
	 * M�todo que inicializa el <code>String</code> con la respuesta correcta.
	 * 
	 * @param 	respuesta		string con la respuesta correcta
	 */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	/**
	 * M�todo que inicializa el <code>array</code> con los textos antes y despu�s del hueco a 
	 * rellenar por el usuario.
	 * 
	 * @param 	texto1			string con el texto antes del hueco a rellenar
	 * @param 	texto2			string con el texto despu�s del hueco a rellenar
	 */
	public void setTextos(String texto1, String texto2) {
		this.texto1 = texto1;
		this.texto2 = texto2;
	}

	/**
	 * M�todo que construye el �tem XML tipo "Text Entry"  en la direcci�n que recibe como 
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
			xsw.writeAttribute("baseType", "string");		// La respuesta es de tipo "string"
			xsw.writeStartElement("correctResponse");		// Valor de la respuesta correcta
			xsw.writeStartElement("value");					// Valor correcto
			xsw.writeCharacters(respuesta);
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
				ParserNeutro.escribeTextoXML(texto1, xsw);		// Texto antes del textentry
			xsw.writeEmptyElement("textEntryInteraction");		// Interacci�n tipo textentry
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			if (longitud != 0)			// El atributo longitud se pone si es distinto de cero
				xsw.writeAttribute("expectedLength", Integer.toString(longitud));
			if (texto2 != null && !texto2.equals(""))
			ParserNeutro.escribeTextoXML(texto2, xsw);		// Texto tras el textentry
			xsw.writeEndElement();							// Cierra "p"
			xsw.writeEndElement();							// Cierra "blockquote"
			// Escribe el final del �tem XML y cierra el writer
			writePie(xsw, req);
			
			// Vac�a y cierra el Writer
			sw.flush();
			sw.close();
			// Pasa a String el contenido del Writer
			String fich = sw.toString();
			// Fichero de salida
			File file = new File(path + identificador + ".xml");
			// Escribe en el fichero de salida
			FileUtils.writeStringToFile(file, fich, null);			
		} catch (XMLStreamException e) {
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