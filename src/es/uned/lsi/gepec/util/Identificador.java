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
 * Clase que almacena la representación de un tipo <i>identifier</i> válido según la 
 * norma QTI del IMS. Contiene métodos para comprobar si es válido, para obtener el 
 * identificador, un método estático para comprobar que en un grupo de identificadores sean 
 * todos distintos entre sí, y otro método estático para leer un grupo de identificadores del 
 * request y comprobar que sean válidos.
 * 
 * @author David Domínguez
 *
 */
public class Identificador {
	
	/**
	 * Variable de clase con el identificador String genérico
	 */
	private String identificador;

	/**
	 * Constructor de la clase en el que simplemente se inicializa el valor de la variable de 
	 * clase con el valor recibido en el parámetro.
	 * 
	 * @param 	identificador	string con el valor inicial para la variable de la clase
	 */
	public Identificador(String identificador) {
		super();
		this.identificador = identificador;
	}

	/**
	 * Método "get" para obtener el valor de la variable de clase. Si no se usa el método <code>
	 * esIdentificador</code> antes no se tendrá certeza de si la variable es un identificador 
	 * válido o no.
	 * 
	 * @return					valor del string del identificador
	 */
	public String getIdentificador() {
		return identificador;
	}
	
	/**
	 * Comprueba si el identificador almacenado en la variable <code>identificador</code> de 
	 * tipo <code>String</code> de la clase es un identificador válido según la especificación 
	 * del QTI. Comprueba que tenga menos de 32 caracteres, que el primer carácter sea una 
	 * letra o '_' (guión bajo), y que el resto de los caracteres sean letras, dígitos,'-' 
	 * (guión), o '_' (guión bajo). En caso de error devuelve un mensaje con la posición del 
	 * primer carácter erróneo encontrado.
	 * 
	 * @return					mensaje con el resultado de la comprobación y los posibles 
	 * 							avisos al usuario en caso de que haya error
	 * @see 	utilidades.MensajeEstado
	 */
	public MensajeEstado esIdentificador(){
		
		MensajeEstado msg = new MensajeEstado();		// resultado de la comprobación

		// Comprueba que el string a comprobar tenga algún valor
		if (identificador != null && !identificador.equals("")){
			// Comprueba que la longitud sea menor de 32 caracteres por compatibilidad con la 
			// versión 1 del QTI
			if (identificador.length() <= 32){
				// Comprueba que el primer caracter del identificador sea una letra o '_'
				if (identificador.charAt(0) == '_' || 
						Character.isLetter(identificador.charAt(0))){
					for (int i = 1; i < identificador.length()&& msg.isEstado(); i++){
						// Si algún carácter no es del juego de caracteres permitidos
						if (identificador.charAt(i) != '_' && identificador.charAt(i) != '-' &&
								!Character.isLetterOrDigit(identificador.charAt(i))) {
							int loc = i + 1;
							msg.setEstado(false);
							msg.setMensaje("el carácter en la posición " + loc + " no está " +
									"permitido");
						}
					}
				} else {
					msg.setEstado(false);
					msg.setMensaje("el primer carácter debe ser una letra o '_'");
				}
			} else {
				msg.setEstado(false);
				msg.setMensaje("debe reducir el número de caracteres a 32 o menos");
			}
		} else {
			msg.setEstado(false);
			msg.setMensaje("no puede quedar vacío");
		}
		
		return msg;
	}

	/**
	 * Método estático para comprobar que los identificadores de una tabla de objetos <code>
	 * Identificador</code> sean todos diferentes entre sí.
	 * 
	 * @param 	tabla		tabla de objetos identificador a comprobar
	 * @return				un mensajeEstado con el estado de la comprobación y los mensajes 
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
	 * Método estático para leer un grupo de identificadores recibidos como parámetros en el 
	 * request. Lee todos los identificadores, inicializando  el array de <code>Identificador
	 * </code>, comprueba que sean correctos y modifica si es necesario el objeto <code>
	 * EstadoWeb</code> del estado de la página avisando en caso de error.
	 * 
	 * @param 	identificadores		array de objetos identificador a rellenar con los 
	 * 								identificadores	del request
	 * @param 	origen				string con el nombre base de los campos donde se leen en 
	 * 								la web los identificadores 
	 * @param 	estado				estadoWeb, representando el estado de la web. Se modifica 
	 * 								con el estado tras la comprobación de los identificadores.
	 * @param 	request				petición recibida del cliente donde están almacenados los 
	 * 								identificadores introducidos por el usuario
	 * @param	pre					Texto previo para los mensajes de error
	 * @see		javax.servlet.http.HttpServletRequest#getParameter
	 * @see		utilidades.EstadoWeb#añadeError(String, String)
	 * @see		utilidades.MensajeEstado
	 */
	public static void testIdentificadores(Identificador[] identificadores, String origen, 
			EstadoWeb estado, String pre){
		
		// Lee los identificadores de request con el nombre base común
		MensajeEstado msg = new MensajeEstado();
		int i;
		for (i = 0; i < identificadores.length; i++)
			identificadores[i]= new Identificador(origen + i);
		// Comprueba que sean identificadores válidos
		for (i = 0; i < identificadores.length && msg.isEstado(); i++)
			msg = identificadores[i].esIdentificador();
		if (msg.isEstado()){
			// Si todos son identificadores correctos comprueba que sean todos distintos
			msg = Identificador.compruebaIdentificadores(identificadores);
			if (!msg.isEstado())	// Si hay dos identificadores repetidos
				estado.añadeError("Error: " + pre + msg.getMensaje() + "<br />", origen + "0");
		} else						// Si encuentra un identificador no válido
			estado.añadeError("Error: " + pre + "el identificador " + i + " " + 
					msg.getMensaje() +	"<br />", origen + (i-1));
	}
}