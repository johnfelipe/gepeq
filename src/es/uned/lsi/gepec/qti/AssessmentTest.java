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

import es.uned.lsi.gepec.util.MensajeEstado;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;

/**
 * Clase que representa a un Test seg�n la norma QTI del IMS. Contiene varios atributos que se 
 * corresponden con los atributos definidos en la norma, como son el t�tulo, el identificador
 * del test, y las Partes del Test, <code>TestPart</code>, que incluye (en esta implementaci�n 
 * s�lo puede incluir una Parte de Test en el Test).

 * @author Blas Medina Alcudia
 */
public class AssessmentTest {

	/**
	 * Identificador del Test, que ser� tambi�n el nombre del archivo XML que lo representa.
	 */
	private String identificador;
	/**
	 * T�tulo del Test.
	 */
	private String titulo;
	/**
	 * Objeto TestPart que contiene el Test, donde est� la secci�n con las referencias a los 
	 * �tems que incluye el Test.
	 */
	private TestPart testPart;
	
	/**
	 * Constructor de la clase que le da los valores iniciales al identificador, al t�tulo y a 
	 * la Parte de Test que incluye el Test.
	 * 
	 * @param 	identificador	string con el identificador del Test, y el nombre del archivo
	 * @param 	titulo			string con el t�tulo del Test
	 * @param 	testPart		testPart que incluye el Test
	 */
	public AssessmentTest (String identificador, String titulo, TestPart testPart) {
		super ();
		this.identificador = identificador;
		this.titulo = titulo;
		this.testPart = testPart;
	}

	/**
	 * M�todo "get" para obtener el identificador del Test.
	 * 
	 * @return		string con el identificador del Test
	 * @see #setIdentificador
	 */
	public String getIdentificador() {
		return identificador;
	}

	/**
	 * M�todo "get" para obtener la Parte del Test que incluye el Test.
	 * 
	 * @return		testPart con la Parte del Test que incluye el Test
	 */
	public TestPart getTestPart() {
		return testPart;
	}

	
	/**
	 * M�todo "get" para obtener el t�tulo del Test.
	 * 
	 * @return		string con el t�tulo del Test
	 */
	public String getTitulo() {
		return titulo;
	}

	/**
	 * M�todo "set" para establecer un nuevo identificador para el test. Utilizado para 
	 * establecer un nuevo identificador en el caso de que ya exista un test con el mismo 
	 * identificador en ese mismo directorio.
	 * 
	 * @param 	identificador	string con el nuevo identificador para el Test
	 * @see		#getIdentificador
	 */
	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}
	
	/**
	 * M�todo que escribe este objeto <code>AssessmentTest</code> en el directorio indicado por 
	 * el <code>String path</code>. Mediante el objeto <code>ServletContext</code> se accede al 
	 * valor de algunos par�metros inciales definidos en el archivo descriptor de despliegue de 
	 * la Aplicaci�n, "web.xml". El objeto se escribe utilizando un <code>XMLStreamWriter
	 * </code>.
	 * 
	 * @param 	path			string indicando la direcci�n del directorio en el que 
	 * 							almacenar el Test 
	 * @param 	sc				servletContext de la Aplicaci�n para acceder a par�metros 
	 * 							definidos en "web.xml"	
	 * @return					mensajeEstado indicando c�mo ha ido el proceso
	 * @throws IOException		si hay alg�n problema al escribir en disco
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public MensajeEstado creaAssessmentTest(String path, String sc) throws IOException{

		MensajeEstado msg = new MensajeEstado();		// Mensaje de salida del proceso
		
		try {
			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xsw = null;				// Writer para escribir datos XML
			StringWriter sw = new StringWriter();	// Buffer en el que escribir
			xsw = xof.createXMLStreamWriter(sw);	// El writer XML escribe en el buffer
			xsw.writeStartDocument("ISO-8859-1","1.0");		// Inicio del documento XML
			xsw.writeStartElement("assessmentTest");		// Comienzo del Test
			xsw.writeDefaultNamespace("http://www.imsglobal.org/xsd/imsqti_v2p1");
			xsw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xsw.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			xsw.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", 
					"http://www.imsglobal.org/xsd/imsqti_v2p1 imsqti_v2p1.xsd");
			xsw.writeAttribute("identifier", identificador);	// Identificador del Test
			xsw.writeAttribute("title", titulo);				// T�tulo del Test
			// Nombre y versi�n de la herramienta
			xsw.writeAttribute("toolName", "nombre herramienta");
			xsw.writeAttribute("toolVersion", "versi�n herramienta");
			
			xsw.writeStartElement("outcomeDeclaration");	// Variable de puntuaci�n
			xsw.writeAttribute("identifier", "SCORE");		// Identificador de la puntuaci�n
			xsw.writeAttribute("cardinality", "single");	// Cardinalidad
			xsw.writeAttribute("baseType", "float");		// Puntuaci�n de tipo float
			xsw.writeStartElement("defaultValue");			// Valor por defecto 0.0
			xsw.writeStartElement("value");
			xsw.writeCharacters("0.0");
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("outcomeDeclaration");	// Constante valor del �tem correcto
			xsw.writeAttribute("identifier", "CORRECTO");	// Identificador de la constante
			xsw.writeAttribute("cardinality", "single");	// Cardinalidad de la constante
			xsw.writeAttribute("baseType", "float");		// Tipo base float
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la constante
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("1.0");						// Constante de valor 1.0
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("outcomeDeclaration");  // Constante valor del �tem incorrecto
			xsw.writeAttribute("identifier", "INCORRECTO");	// Identificador de la constante
			xsw.writeAttribute("cardinality", "single");	// Cardinalidad de la constante
			xsw.writeAttribute("baseType", "float");		// Tipo base float
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la constante
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("-1.0");					// Constante de valor -1.0
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			
			// Se llama al m�todo para escribir la Parte del Test, que contiene las secciones, 
			// las referencias a los �tems, y dem�s par�metros
			testPart.creaTestPart(xsw);
			
			// Escribe el proceso de respuesta para la puntuaci�n del Test
			xsw.writeStartElement("outcomeProcessing");
			// Array de referencias a los �tems con todos los que contiene el Test
			AssessmentItemRef[] items = testPart.getSeccion().getItemRefArray();
			// Para todos los �tems del Test
			for (int i = 0; i < items.length; i++){
				xsw.writeStartElement("outcomeCondition");		// Condici�n
				xsw.writeStartElement("outcomeIf");				// Si ()
				xsw.writeStartElement("equal");					// Son iguales
				xsw.writeAttribute("toleranceMode", "exact");	// Comparaci�n exacta
				xsw.writeEmptyElement("variable");				// La variable
				// Identificador de la variable resultado del �tem
				xsw.writeAttribute("identifier", 				
						items[i].getIdentificador().getIdentificador() + ".SCORE"); 
				xsw.writeEmptyElement("variable");				// Y La variable definida arriba
				xsw.writeAttribute("identifier", "CORRECTO");	// Con el identificador CORRECTO
				xsw.writeStartElement("setOutcomeValue");		// Entonces establece el valor
				xsw.writeAttribute("identifier", "SCORE");		// De la variable SCORE igual a
				xsw.writeStartElement("sum");					// La suma de
				xsw.writeEmptyElement("variable");				// El valor de la variable
				xsw.writeAttribute("identifier", "SCORE");		// SCORE
				xsw.writeEmptyElement("variable");				// Y el de la variable
				// Identificador de la variable del �tem
				xsw.writeAttribute("identifier", 				
						items[i].getIdentificador().getIdentificador() + ".SCORE"); 
				xsw.writeAttribute("weightIdentifier", "WeightOK");		// Por su peso correcto
				xsw.writeEndElement();							// Cierra "sum"
				xsw.writeEndElement();							// Cierra "setOutcomeValue"
				xsw.writeEndElement();							// Cierra "equal"
				xsw.writeEndElement();							// Cierra "outcomeIf"
				
				// Si no se cumple la anterior condici�n: Si ()
				xsw.writeStartElement("outcomeElseIf");
				xsw.writeStartElement("equal");					// Son iguales
				xsw.writeAttribute("toleranceMode", "exact");	// Comparaci�n exacta
				xsw.writeEmptyElement("variable");				// La variable
				// Identificador de la variable resultado del �tem
				xsw.writeAttribute("identifier", 				
						items[i].getIdentificador().getIdentificador() + ".SCORE");
				xsw.writeEmptyElement("variable");				// Y la variable definida arriba
				xsw.writeAttribute("identifier", "INCORRECTO");//Con el identificador INCORRECTO
				xsw.writeStartElement("setOutcomeValue");		// Entonces establece el valor
				xsw.writeAttribute("identifier", "SCORE");		// De la variable SCORE igual a
				xsw.writeStartElement("sum");					// La suma de
				xsw.writeEmptyElement("variable");				// El valor de la variable
				xsw.writeAttribute("identifier", "SCORE");		// SCORE
				xsw.writeEmptyElement("variable");				// Y el de la variable
				// Identificador de la variable del �tem
				xsw.writeAttribute("identifier", 				
						items[i].getIdentificador().getIdentificador() + ".SCORE");
				xsw.writeAttribute("weightIdentifier", "WeightNOK");   // Por su peso incorrecto
				xsw.writeEndElement();							// Cierra "sum"
				xsw.writeEndElement();							// Cierra "setOutcomeValue" 
				xsw.writeEndElement();							// Cierra "equal"
				xsw.writeEndElement();							// Cierra "outcomeElseIf"
				xsw.writeEndElement();							// Cierra "outcomeCondition"
			}
			

			xsw.writeEndElement();		// Cierra "outcomeProcessing"
			xsw.writeEndElement();		// Cierra "assessmentTest"
			xsw.writeEndDocument();		// Fin del documento
			xsw.flush();				// Vac�a el Writer XML
			xsw.close();				// Cierra el Writer XML
			
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
			// Error en el procesamiento de los datos XML
			msg.setMensaje("Error procesando el archivo XML");
			msg.setEstado(false);
		}
		if (msg.isEstado()){
			// Si todo ha ido bien se informa del resultado
			msg.setMensaje("Test creado con �xito");	
		}
		
		return msg;
	}
}