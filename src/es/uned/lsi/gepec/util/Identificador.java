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
 * Clase que almacena la representaci�n de un tipo <i>identifier</i> v�lido seg�n la 
 * norma QTI del IMS. Contiene m�todos para comprobar si es v�lido, para obtener el 
 * identificador, un m�todo est�tico para comprobar que en un grupo de identificadores sean 
 * todos distintos entre s�, y otro m�todo est�tico para leer un grupo de identificadores del 
 * request y comprobar que sean v�lidos.
 * 
 * @author David Dom�nguez
 *
 */
public class Identificador {
	
	/**
	 * Variable de clase con el identificador String gen�rico
	 */
	private String identificador;

	/**
	 * Constructor de la clase en el que simplemente se inicializa el valor de la variable de 
	 * clase con el valor recibido en el par�metro.
	 * 
	 * @param 	identificador	string con el valor inicial para la variable de la clase
	 */
	public Identificador(String identificador) {
		super();
		this.identificador = identificador;
	}

	/**
	 * M�todo "get" para obtener el valor de la variable de clase. Si no se usa el m�todo <code>
	 * esIdentificador</code> antes no se tendr� certeza de si la variable es un identificador 
	 * v�lido o no.
	 * 
	 * @return					valor del string del identificador
	 */
	public String getIdentificador() {
		return identificador;
	}
	
	/**
	 * Comprueba si el identificador almacenado en la variable <code>identificador</code> de 
	 * tipo <code>String</code> de la clase es un identificador v�lido seg�n la especificaci�n 
	 * del QTI. Comprueba que tenga menos de 32 caracteres, que el primer car�cter sea una 
	 * letra o '_' (gui�n bajo), y que el resto de los caracteres sean letras, d�gitos,'-' 
	 * (gui�n), o '_' (gui�n bajo). En caso de error devuelve un mensaje con la posici�n del 
	 * primer car�cter err�neo encontrado.
	 * 
	 * @return					mensaje con el resultado de la comprobaci�n y los posibles 
	 * 							avisos al usuario en caso de que haya error
	 * @see 	utilidades.MensajeEstado
	 */
	public MensajeEstado esIdentificador(){
		
		MensajeEstado msg = new MensajeEstado();		// resultado de la comprobaci�n

		// Comprueba que el string a comprobar tenga alg�n valor
		if (identificador != null && !identificador.equals("")){
			// Comprueba que la longitud sea menor de 32 caracteres por compatibilidad con la 
			// versi�n 1 del QTI
			if (identificador.length() <= 32){
				// Comprueba que el primer caracter del identificador sea una letra o '_'
				if (identificador.charAt(0) == '_' || 
						Character.isLetter(identificador.charAt(0))){
					for (int i = 1; i < identificador.length()&& msg.isEstado(); i++){
						// Si alg�n car�cter no es del juego de caracteres permitidos
						if (identificador.charAt(i) != '_' && identificador.charAt(i) != '-' &&
								!Character.isLetterOrDigit(identificador.charAt(i))) {
							int loc = i + 1;
							msg.setEstado(false);
							msg.setMensaje("el car�cter en la posici�n " + loc + " no est� " +
									"permitido");
						}
					}
				} else {
					msg.setEstado(false);
					msg.setMensaje("el primer car�cter debe ser una letra o '_'");
				}
			} else {
				msg.setEstado(false);
				msg.setMensaje("debe reducir el n�mero de caracteres a 32 o menos");
			}
		} else {
			msg.setEstado(false);
			msg.setMensaje("no puede quedar vac�o");
		}
		
		return msg;
	}

	/**
	 * M�todo est�tico para comprobar que los identificadores de una tabla de objetos <code>
	 * Identificador</code> sean todos diferentes entre s�.
	 * 
	 * @param 	tabla		tabla de objetos identificador a comprobar
	 * @return				un mensajeEstado con el estado de la comprobaci�n y los mensajes 
	 * 						pertinentes
	 */
	public static MensajeEstado compruebaIdentificadores(Identificador[] tabla){
		MensajeEstado msg = new MensajeEstado();
		for (int i = 0; i < tabla.length; i++)
			for (int j = i + 1; j < tabla.length; j++)
				if (tabla[i].getIdentificador().equals(tabla[j].getIdentificador())){
					// Si dos identificadores son iguales
					msg.setMensaje("el identificador " + (i+1) + " es igual al identificador "
							+ (j+1));
					msg.setEstado(false);
				}
		
		return msg;
	}
	
	/**
	 * M�todo est�tico para leer un grupo de identificadores recibidos como par�metros en el 
	 * request. Lee todos los identificadores, inicializando  el array de <code>Identificador
	 * </code>, comprueba que sean correctos y modifica si es necesario el objeto <code>
	 * EstadoWeb</code> del estado de la p�gina avisando en caso de error.
	 * 
	 * @param 	identificadores		array de objetos identificador a rellenar con los 
	 * 								identificadores	del request
	 * @param 	origen				string con el nombre base de los campos donde se leen en 
	 * 								la web los identificadores 
	 * @param 	estado				estadoWeb, representando el estado de la web. Se modifica 
	 * 								con el estado tras la comprobaci�n de los identificadores.
	 * @param 	request				petici�n recibida del cliente donde est�n almacenados los 
	 * 								identificadores introducidos por el usuario
	 * @param	pre					Texto previo para los mensajes de error
	 * @see		javax.servlet.http.HttpServletRequest#getParameter
	 * @see		utilidades.EstadoWeb#a�adeError(String, String)
	 * @see		utilidades.MensajeEstado
	 */
	public static void testIdentificadores(Identificador[] identificadores, String origen, 
			EstadoWeb estado, String pre){
		
		// Lee los identificadores de request con el nombre base com�n
		MensajeEstado msg = new MensajeEstado();
		int i;
		for (i = 0; i < identificadores.length; i++)
			identificadores[i]= new Identificador(origen + i);
		// Comprueba que sean identificadores v�lidos
		for (i = 0; i < identificadores.length && msg.isEstado(); i++)
			msg = identificadores[i].esIdentificador();
		if (msg.isEstado()){
			// Si todos son identificadores correctos comprueba que sean todos distintos
			msg = Identificador.compruebaIdentificadores(identificadores);
			if (!msg.isEstado())	// Si hay dos identificadores repetidos
				estado.a�adeError("Error: " + pre + msg.getMensaje() + "<br />", origen + "0");
		} else						// Si encuentra un identificador no v�lido
			estado.a�adeError("Error: " + pre + "el identificador " + i + " " + 
					msg.getMensaje() +	"<br />", origen + (i-1));
	}
}