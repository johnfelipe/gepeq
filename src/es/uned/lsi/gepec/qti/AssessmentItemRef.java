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
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import es.uned.lsi.gepec.util.Identificador;

/**
 * Clase que representa las referencias a los ítems individuales dentro de un Test según la 
 * norma QTI del IMS. Se utiliza para incorporar cada una de las cuestiones individuales dentro 
 * de un Test. Contiene varios atributos que se corresponden con los atributos definidos en la 
 * norma, más una variable tipo <code>File</code> del archivo del ítem a incluir en el test 
 * para ayudar a calcular su URI relativa. Tal y como define la norma, esta clase hereda de la 
 * clase "SectionPart", por lo tanto también contendrá sus variables de clase.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class AssessmentItemRef extends SectionPart {


	/**
	 * Objeto <code>File</code> apuntando al ítem XML al que hace referencia esta clase.
	 */
	private File itemFile;

	/**
	 * URI relativa desde el test con la referencia al ítem XML que se debe incluir en el Test.
	 */
	private URI href;
	/**
	 * Objeto de tipo <code>Weight</code> con el peso que aplicar a la pregunta en caso de que 
	 * sea correcta
	 */
	private Weight ok;
	/**
	 * Objeto de tipo <code>Weight</code> con el peso que aplicar a la pregunta en caso de que 
	 * sea incorrecta
	 */
	private Weight noOk;
	
	/**
	 * Constructor de la clase referencia a un ítem. Le da los valores iniciales a los pesos 
	 * correcto e incorrecto del ítem, al objeto <code>File</code> que apunta al ítem, y llama 
	 * al constructor de la superclase para inicializar sus variables de clase con el resto de 
	 * los parámetros.
	 * 
	 * @param 	identificador	identificador de la referencia del ítem
	 * @param 	requerido		boolean indicando si el ítem es requerido
	 * @param 	fijo			boolean indicando si el ítem está en posición fija
	 * @param 	itemFile		file del ítem al que hace referencia la clase
	 * @param 	ok				weight con el peso del ítem en caso de respuesta correcta
	 * @param 	noOk			weight con el peso del ítem en caso de respuesta incorrecta
	 */
	public AssessmentItemRef(Identificador identificador, boolean requerido, boolean fijo, 
			File itemFile, Weight ok, Weight noOk) {
		super(identificador, requerido, fijo);
		this.itemFile = itemFile;
		this.ok = ok;
		this.noOk = noOk;
	}

	/**
	 * Calcula la URI de referencia del ítem a partir de la localización del propio ítem y de 
	 * la del test que se está creando, hallando la dirección relativa del ítem respecto al test.
	 * 
	 * @param 	test	file del test que se está creando
	 * @see		java.net.URI
	 */
	public void creaItemRef (File test){
		// URI absoluta del ítem
		System.out.println("hola:" + test);
		System.out.println(itemFile.toURI().toString());
		//String itemAbsoluta = itemFile.toURI().toString();
		String itemAbsoluta = "C:/test/items";
		// URI absoluta del test
		//String  testPath = test.toURI().toString();
		String testPath = "C:/items";
		// String para calcular la parte de la dirección que comparten el test y el ítem
		String comun;
		// Índice para ir avanzando por cada uno de los separadores de la dirección del ítem
		int indexAbs = 0;
		// Boolean indicando que ya se ha encontrado la dirección común entre el test y el ítem
		boolean fin;
		// Va avanzando por los separadores hasta que sean diferentes las direcciones del test 
		// y del ítem
		do {
			// localización del carácter separador desde la posición anterior
			indexAbs = itemAbsoluta.indexOf('/', indexAbs + 1);
			System.out.println(indexAbs);
			System.out.println(testPath);
			// Dirección base común a probar si es común de ambas
			comun = itemAbsoluta.substring(0, indexAbs);
			// Se comprueba si esta dirección es común a ambas direcciones
			fin = testPath.startsWith(comun);
		} while (fin == true);
		// La parte común de la dirección es desde el comienzo hasta el último carácter 
		// separador
		comun = comun.substring(0, comun.lastIndexOf('/'));
		// Dirección absoluta del ítem menos la parte común con la dirección absoluta del test 
		String rel = itemAbsoluta.substring(comun.length() + 1, itemAbsoluta.length());
		// Dirección absoluta del test menos la parte común con la dirección absoluta del ítem
		testPath = testPath.substring(comun.length() + 1, testPath.length());
		// Cálculo del número de separadores que hay desde la localización del test hasta 
		// llegar a la dirección común con el ítem. Es el número de saltos de directorios 
		// "hacia arriba" que hay que hacer para llegar al ítem desde el test
		int numDir = 0;
		for (int i = 0; i < testPath.length(); i++){
			if (testPath.charAt(i) == '/')
				numDir++;
		}
		// Dirección relativa del ítem respecto al test
		String relativa = "";
		// Primero se incluyen los caracteres de subir nivel en la estructura de directorios 
		// para llegar a la parte común la dirección absoluta del ítem
		for (int i = 0; i < numDir; i++)
			relativa += "../";
		// Luego se añade la dirección del ítem menos la parte que tiene en común con el test
		relativa += rel;
		// Se crea una URI relativa a partir de la dirección relativa contenida en el String
		href = URI.create(relativa);
	}
	
	/**
	 * Método que escribe este objeto <code>AssessmentItemRef</code> en el <code>XMLStreamWriter
	 * </code> recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaAssessmentItemRef (XMLStreamWriter xsw) throws XMLStreamException {
		
		xsw.writeStartElement("assessmentItemRef");		// Comienzo del elemento
		// Identificador de la referencia al ítem
		xsw.writeAttribute("identifier", identificador.getIdentificador());
		xsw.writeAttribute("href", href.toString());	// URI de referencia al ítem
		if (requerido == true)	// El elemento requerido se escribe si el ítem es requerido
			xsw.writeAttribute("required", Boolean.toString(requerido));
		if (fijo == true)		// El elemento fijo se escribe si el ítem se queda fijo
			xsw.writeAttribute("fixed", Boolean.toString(fijo));
		ok.creaWeight(xsw);	   // Se llama al método que escriba el peso de respuesta correcta
		noOk.creaWeight(xsw);  // Se llama al método que escriba el peso de respuesta incorrecta
		xsw.writeEndElement(); // Cierra el elemento "assessmentItemRef"
	}
}