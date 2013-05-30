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
package es.uned.lsi.gepec.util;


/**
 * Clase con diversos m�todos est�ticos para leer los datos introducidos en las p�ginas de 
 * tomas de datos de los �tems y el test y comprobar, en aquellos que haga falta, que sean 
 * correctos. 
 * 
 * @author David Dom�nguez
 *
 */
public class Comprobaciones{

	/**
	 * M�todo para ayudar a leer un conjunto de checkboxes con un nombre base com�n de una 
	 * p�gina Web informando del n�mero que est�n marcadas. Lee los checkboxes recogidos en una 
	 * p�gina web con nombre base com�n, y rellena un <code> array</code> de boolean con las 
	 * respuestas correctas, devolviendo el n�mero de respuestas correctas totales.
	 * 
	 * @param 	respCorrecta	array de boolean a rellenar marcando las respuestas correctas
	 * @param 	origen			nombre base de los checkboxes a leer
	 * @param 	request			petici�n recibida del cliente donde est�n almacenados las 
	 * 							respuestas correctas marcadas por el usuario
	 * @return					entero con el n�mero total de respuestas marcadas correctas
	 * 
	 * @see		javax.servlet.http.HttpServletRequest#getParameter
	 * @see		utilidades.EstadoWeb#a�adeError(String, String)
	 */
	public static int sumaCorrectas (boolean[] respCorrecta, String origen 
			){
		
		int numCorrectas = 0;		// Inicializa el n�mero de respuestas correctas a cero
		String check;				// Variable intermedia para leer el valor de los checkboxes
		
		for (int i = 0; i < respCorrecta.length; i++){
			check = origen + i;		// Lee el checkbox i
			if (check != null && check.equalsIgnoreCase("ON")){
				// Si el checkbox es igual a "ON" la respuesta es correcta y se incrementa el 
				// n�mero de respuestas correctas
				respCorrecta[i] = true;
				numCorrectas++;
			}
			else
				// Si no es igual a "ON", respuesta incorrecta
				respCorrecta[i] = false;
		}
		

		return numCorrectas;
	}
	
	/**
	 * M�todo para ayudar a leer un conjunto de checkboxes con un nombre base com�n de una 
	 * p�gina Web. Lee un conjunto de checkboxes inicializando la tabla de <code>boolean</code> 
	 * que recibe como par�metro en funci�n de si est� cada checkbox marcado o no.
	 * 
	 * @param	respFija	array de boolean con las respuestas fijas
	 * @param 	origen		nombre base de los checkbox de respuesta fija
	 * @param 	request		petici�n del cliente
	 * @see		javax.servlet.http.HttpServletRequest#getParameter
	 */
	public static void leeCheckBoxes(boolean[] respFija, String origen){
		String checkValor;
		for (int i = 0; i < respFija.length; i++){
			checkValor = origen + i;
			if (checkValor != null && checkValor.equalsIgnoreCase("ON"))
				respFija[i] = true;
			else
				respFija[i] = false;
		}

	}
	
	/**
	 * Comprueba que el n�mero m�ximo de elecciones introducido sea correcto en preguntas con 
	 * un solo grupo de respuestas y entre las que se puede seleccionar m�s de una como 
	 * correcta. Comprueba que el valor introducido sea un entero positivo correcto mediante 
	 * el m�todo <code>esPositivo</code> de <code>EnteroPositivo</code>, mayor que el n�mero 
	 * de opciones marcadas como correctas o cero, y menor que el n�mero total de opciones de 
	 * respuesta disponibles.
	 * 
	 * @param 	maxChoices		enteroPositivo inicializado con el string introducido por el 
	 * 							usuario con el n�mero m�ximo de elecciones a comprobar
	 * @param 	estado			estadoWeb, representando el estado de la web. Se modifica con 
	 * 							el estado tras la comprobaci�n del n�mero m�ximo de elecciones.
	 * @param 	campo			campo de la web de donde se ha le�do el n�mero m�ximo de 
	 * 							elecciones indicando d�nde poner el foco en caso de error
	 * @param 	numCorrectas	n�mero de respuestas marcadas como correctas totales
	 * @param 	numMaxOpciones	n�mero m�ximo de respuestas posibles que tiene el candidato 
	 * 							para elegir
	 * @see		utilidades.MensajeEstado
	 * @see		utilidades.EstadoWeb#a�adeError(String, String)
	 * @see		utilidades.EnteroPositivo
	 */
	public static void testMaximo(EnteroPositivo maxChoices, EstadoWeb estado, String campo, 
			int numCorrectas, int numMaxOpciones){

		MensajeEstado msg = maxChoices.esPositivo();
		if (!msg.isEstado())		// Si el n�mero no es un entero positivo v�lido
			estado.a�adeError("Error: el n�mero m�ximo de elecciones " + msg.getMensaje() + 
					"<br />", campo);
		else if (maxChoices.getEnteroPositivo() != 0 &&	
				maxChoices.getEnteroPositivo() < numCorrectas)
			estado.a�adeError("El n�mero m�ximo de elecciones debe ser mayor o igual que el " +
					"n�mero de respuestas correctas o cero", campo);
		else if (maxChoices.getEnteroPositivo() > numMaxOpciones)
			estado.a�adeError("El n�mero m�ximo de elecciones debe ser menor o igual que el " +
					"n�mero de respuestas posibles<br />", campo);
	}
	
	/**
	 * Comprueba los n�meros m�ximos de elecciones introducidos para una pregunta en la que 
	 * hay varios grupos de opciones, y donde hay al menos un conjunto de elementos en el que 
	 * cada elemento tiene su propio n�mero m�ximo de elecciones con elementos de otro grupo. 
	 * Comprueba todos los elementos de un grupo uno por uno, examinando que el valor 
	 * introducido sea un entero positivo correcto mediante el m�todo <code>esPositivo</code> 
	 * de <code>EnteroPositivo</code>, mayor que el n�mero de opciones marcadas como correctas 
	 * o cero, y menor que el n�mero total de opciones de respuesta disponibles.
	 * 
	 * @param 	maxChoices		array de enteroPositivo con el n�mero m�ximo de elecciones de 
	 * 							cada elemento del grupo a rellenar con los string le�dos de la
	 * 							p�gina web contenidos en el par�metro <code>max</code>
	 * @param 	campo			nombre base del campo de la web de donde se ha le�do el n�mero 
	 * 							m�ximo de elecciones y d�nde poner el foco en caso de error
	 * @param 	estado			estadoWeb, representando el estado de la web. Se modifica con 
	 * 							el estado tras las comprobaciones
	 * @param 	max				array de string con los n�mero m�ximos de elecciones le�dos de 
	 * 							la p�gina web
	 * @param 	grupo			string con el grupo al que pertenecen los n�meros m�ximos de 
	 * 							elecciones.	Para rellenar correctamente los avisos al usuario.
	 * @param 	elemento		Si tratamos a las filas o las columnas. Para rellenar 
	 * 							correctamente los avisos al usuario en caso de error.
	 * @param 	numCorrectas	array con el n�mero de respuestas marcadas como correctas de 
	 * 							cada elemento
	 * @param 	numOpciones		n�mero m�ximo de opciones posibles a elegir como correctas
	 * @see		utilidades.MensajeEstado
	 * @see		utilidades.EstadoWeb#a�adeError(String, String)
	 * @see		utilidades.EnteroPositivo
	 */
	public static void testTablaMaximos(EnteroPositivo[] maxChoices, String campo, 
				EstadoWeb estado, String[] max, String grupo, String elemento, 
				int[] numCorrectas, int numOpciones){
		
		// Cadena con el nombre de los elementos opuestos
		String elemOpuesto;
		if (elemento.equals("fila"))
				elemOpuesto = "columna";
		else
				elemOpuesto = "fila";

		for (int i = 0; i < maxChoices.length; i++){
			maxChoices[i] = new EnteroPositivo(max[i]);
			// Comprueba el n�mero de elecciones, que sea un entero correcto, que no sea menor 
			// que el n�mero de elementos marcados si es distinto de cero y que no sea mayor 
			// que el n�mero de elementos disponibles
			MensajeEstado msg = maxChoices[i].esPositivo();
			if (!msg.isEstado())		// Si el n�mero no es un entero positivo v�lido
				estado.a�adeError("Error: el n�mero m�ximo de elecciones de la " + elemento +
						" " + (i+1) + " " + msg.getMensaje() + "<br />", campo + i);
			else if (maxChoices[i].getEnteroPositivo() != 0 &&	
					maxChoices[i].getEnteroPositivo() < numCorrectas[i])
				estado.a�adeError("El n�mero m�ximo de elecciones de la " + elemento + " " + 
						(i+1) +	" " + grupo + " debe ser mayor o igual que el n�mero de " +
						"respuestas " +	"correctas en esa " + elemento + " o cero<br />", 
						campo+ i);
			else if (maxChoices[i].getEnteroPositivo() > numOpciones)
				estado.a�adeError("El n�mero m�ximo de elecciones de la " + elemento + " " + 
						(i+1) +	" " + grupo + " debe ser menor que el n�mero de " + 
						elemOpuesto + "s<br />", 
						campo + i);
		}

	}
}