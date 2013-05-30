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
 * Clase que representa las referencias a los �tems individuales dentro de un Test seg�n la 
 * norma QTI del IMS. Se utiliza para incorporar cada una de las cuestiones individuales dentro 
 * de un Test. Contiene varios atributos que se corresponden con los atributos definidos en la 
 * norma, m�s una variable tipo <code>File</code> del archivo del �tem a incluir en el test 
 * para ayudar a calcular su URI relativa. Tal y como define la norma, esta clase hereda de la 
 * clase "SectionPart", por lo tanto tambi�n contendr� sus variables de clase.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class AssessmentItemRef extends SectionPart {


	/**
	 * Objeto <code>File</code> apuntando al �tem XML al que hace referencia esta clase.
	 */
	private File itemFile;

	/**
	 * URI relativa desde el test con la referencia al �tem XML que se debe incluir en el Test.
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
	 * Constructor de la clase referencia a un �tem. Le da los valores iniciales a los pesos 
	 * correcto e incorrecto del �tem, al objeto <code>File</code> que apunta al �tem, y llama 
	 * al constructor de la superclase para inicializar sus variables de clase con el resto de 
	 * los par�metros.
	 * 
	 * @param 	identificador	identificador de la referencia del �tem
	 * @param 	requerido		boolean indicando si el �tem es requerido
	 * @param 	fijo			boolean indicando si el �tem est� en posici�n fija
	 * @param 	itemFile		file del �tem al que hace referencia la clase
	 * @param 	ok				weight con el peso del �tem en caso de respuesta correcta
	 * @param 	noOk			weight con el peso del �tem en caso de respuesta incorrecta
	 */
	public AssessmentItemRef(Identificador identificador, boolean requerido, boolean fijo, 
			File itemFile, Weight ok, Weight noOk) {
		super(identificador, requerido, fijo);
		this.itemFile = itemFile;
		this.ok = ok;
		this.noOk = noOk;
	}

	/**
	 * Calcula la URI de referencia del �tem a partir de la localizaci�n del propio �tem y de 
	 * la del test que se est� creando, hallando la direcci�n relativa del �tem respecto al test.
	 * 
	 * @param 	test	file del test que se est� creando
	 * @see		java.net.URI
	 */
	public void creaItemRef (File test){
		// URI absoluta del �tem
		System.out.println("hola:" + test);
		System.out.println(itemFile.toURI().toString());
		//String itemAbsoluta = itemFile.toURI().toString();
		String itemAbsoluta = "C:/test/items";
		// URI absoluta del test
		//String  testPath = test.toURI().toString();
		String testPath = "C:/items";
		// String para calcular la parte de la direcci�n que comparten el test y el �tem
		String comun;
		// �ndice para ir avanzando por cada uno de los separadores de la direcci�n del �tem
		int indexAbs = 0;
		// Boolean indicando que ya se ha encontrado la direcci�n com�n entre el test y el �tem
		boolean fin;
		// Va avanzando por los separadores hasta que sean diferentes las direcciones del test 
		// y del �tem
		do {
			// localizaci�n del car�cter separador desde la posici�n anterior
			indexAbs = itemAbsoluta.indexOf('/', indexAbs + 1);
			System.out.println(indexAbs);
			System.out.println(testPath);
			// Direcci�n base com�n a probar si es com�n de ambas
			comun = itemAbsoluta.substring(0, indexAbs);
			// Se comprueba si esta direcci�n es com�n a ambas direcciones
			fin = testPath.startsWith(comun);
		} while (fin == true);
		// La parte com�n de la direcci�n es desde el comienzo hasta el �ltimo car�cter 
		// separador
		comun = comun.substring(0, comun.lastIndexOf('/'));
		// Direcci�n absoluta del �tem menos la parte com�n con la direcci�n absoluta del test 
		String rel = itemAbsoluta.substring(comun.length() + 1, itemAbsoluta.length());
		// Direcci�n absoluta del test menos la parte com�n con la direcci�n absoluta del �tem
		testPath = testPath.substring(comun.length() + 1, testPath.length());
		// C�lculo del n�mero de separadores que hay desde la localizaci�n del test hasta 
		// llegar a la direcci�n com�n con el �tem. Es el n�mero de saltos de directorios 
		// "hacia arriba" que hay que hacer para llegar al �tem desde el test
		int numDir = 0;
		for (int i = 0; i < testPath.length(); i++){
			if (testPath.charAt(i) == '/')
				numDir++;
		}
		// Direcci�n relativa del �tem respecto al test
		String relativa = "";
		// Primero se incluyen los caracteres de subir nivel en la estructura de directorios 
		// para llegar a la parte com�n la direcci�n absoluta del �tem
		for (int i = 0; i < numDir; i++)
			relativa += "../";
		// Luego se a�ade la direcci�n del �tem menos la parte que tiene en com�n con el test
		relativa += rel;
		// Se crea una URI relativa a partir de la direcci�n relativa contenida en el String
		href = URI.create(relativa);
	}
	
	/**
	 * M�todo que escribe este objeto <code>AssessmentItemRef</code> en el <code>XMLStreamWriter
	 * </code> recibido como par�metro, seg�n la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay alg�n problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaAssessmentItemRef (XMLStreamWriter xsw) throws XMLStreamException {
		
		xsw.writeStartElement("assessmentItemRef");		// Comienzo del elemento
		// Identificador de la referencia al �tem
		xsw.writeAttribute("identifier", identificador.getIdentificador());
		xsw.writeAttribute("href", href.toString());	// URI de referencia al �tem
		if (requerido == true)	// El elemento requerido se escribe si el �tem es requerido
			xsw.writeAttribute("required", Boolean.toString(requerido));
		if (fijo == true)		// El elemento fijo se escribe si el �tem se queda fijo
			xsw.writeAttribute("fixed", Boolean.toString(fijo));
		ok.creaWeight(xsw);	   // Se llama al m�todo que escriba el peso de respuesta correcta
		noOk.creaWeight(xsw);  // Se llama al m�todo que escriba el peso de respuesta incorrecta
		xsw.writeEndElement(); // Cierra el elemento "assessmentItemRef"
	}
}