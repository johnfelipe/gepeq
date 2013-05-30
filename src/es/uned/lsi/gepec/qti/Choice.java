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

import es.uned.lsi.gepec.util.*;
import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.qti.ChoiceXML;


import java.io.IOException;
import java.util.List;

/**
 * Componente de control implícito asociado a la página JSP "choice.jsp". Es la lógica 
 * asociada a esa página JSP. Se ejecutará su método <code>doLogic</code> siempre que sea 
 * llamada la página. Su función es comprobar que todos los parámetros iniciales de la toma de 
 * datos de una pregunta tipo "Choice" sean correctos, en cuyo caso redirecciona la respuesta 
 * a la siguiente página de toma de datos de tipo "Choice". La clase implementa la interfaz 
 * <code>Control</code>, implementando su método <code>doLogic</code>, que es el único que 
 * contiene, y que es el encargado de realizar la lógica asociada a la página "choice.jsp".
 * 
 * @author 	Blas Medina Alcudia
 * @see		Control
 * 
 */
public class Choice {
	
	/**
	 * Realiza la comprobación de los parámetros que se recogen en la página "choice.jsp". 
	 * Será llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * página requerida es "choice.jsp". Ya que es llamado desde un filtro, será ejecutado 
	 * siempre que llegue una petición de un cliente y antes de que la petición llegue al 
	 * Servlet (página JSP compilada a Servlet) que sirve la respuesta al cliente. Comprueba 
	 * uno por uno todos los parámetros, y, en caso de que sean correctos, crea un objeto <code>
	 * ChoiceXML</code> para guardar los datos asociados al tipo de pregunta y lo guarda en el 
	 * ámbito de sesión para utilizarlos más adelante para terminar de crear el ítem.
	 * 
	 * @param	request				la petición http enviada por el cliente
	 * @param	response			la respuesta http a enviar desde el servidor
	 * @return						true para que se sigan ejecutando los demás filtros del 
	 * 								servidor y se envíe la respuesta correctamente
	 * @throws	IOException			no debe de producirse
	 * @throws	ServletException	si hay algún problema accediendo a request o response
	 * @see 	Control#doLogic(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
	 * @see 	control.ControlFilter
	 * @see		javax.servlet.http.HttpServletRequest
	 * @see		javax.servlet.http.HttpServletResponse
	 * @see		utilidades.EstadoWeb
	 * @see		utilidades.MensajeEstado
	 * @see		utilidades.EnteroPositivo
	 * @see		xml.items.ChoiceXML
	 */
	public boolean doLogic() 
			throws IOException {
		
		// Comprueba si es la primera vez que se visita la página
			// Caracteres reservados no permitidos
			final String reservados = ";/?:@&=+$,\\*\"<>|";

			String identificador = "Identif";	// Identificador del ítem 
			String titulo = "Tit";			// Título del ítem
			String instrucciones = "Instruc";	// Instrucciones del ítem
			String pregunta = "Preg";			// Pregunta del ítem
			String Shuff = "Shuffle";		// Mezclar las respuestas o no
			String numResp = "Num_resp";		// Número de respuestas
			List<Answer> listarespuestas = null;
			// Estado de la petición web. Almacena si son correctos los parámetros, los 
			// mensajes de aviso al usuario y dónde colocar el foco en la página
			EstadoWeb estado = new EstadoWeb();		
			
			boolean shuffle = false;		// Valor por defecto del parámetro
			if ( Shuff != null){			// Interpreta el valor marcado en la página 
				if (Shuff.equalsIgnoreCase("ON"))
					shuffle = true;
			}

			// Comprueba que los caracteres del identificador estén permitidos
		        for(int i = 0; i < identificador.length() && estado.isEstado() == true; i++) {
		        	char c = identificador.charAt(i);	// Lee el siguiente carácter
		        	int marca = reservados.indexOf(c);
		        	if (marca >= 0 ){
						estado.añadeError("Error: El carácter '" + reservados.charAt(marca) + 
								"' en la posición " + (i+1) + " del Identificador no está " +
								"permitido<br />", "Identif");
		        	}
		        }

				// Comprueba que el número de respuestas sea un valor válido
				EnteroPositivo numRespuestas = new EnteroPositivo(numResp);
				MensajeEstado msg = numRespuestas.esPositivo();
				if(msg.isEstado()){		// Si es un número válido y mayor o igual que cero
					if (numRespuestas.getEnteroPositivo() <=  1)// Si es menor o igual que uno
						estado.añadeError("El número de respuestas debe ser mayor que 1<br />",
								"Num_resp");
				} else 					// Si no es un entero positivo válido
					estado.añadeError("Error: el número de respuestas " + msg.getMensaje() + 
							"<br />", "Num_resp");
				
				// Comprueba que las instrucciones sean un código XHTML válido
				if (instrucciones != null && !instrucciones.equals("")){
					msg = ParserNeutro.compruebaInstruccionesXHTML(instrucciones);
					if (msg.isEstado() == false)
						estado.añadeError(msg.getMensaje(), "Instruc");
				}
				
				// Si el estado es todo correcto pasa a generar el objeto contenedor del ítem 
				if ( estado.isEstado()){
					// Crea un objeto que contiene las características del ítem Choice a crear
					ChoiceXML chXML = new ChoiceXML(identificador, titulo, instrucciones,
							pregunta, shuffle, listarespuestas);
					// Guarda el objeto choiceXML en sesión para seguir utilizándolo luego
					// Redirecciona a la página de introducir las respuestas indicando el
					// número de respuestas y si hay que mezclarlas aleatoriamente
				}
			// Si ha habido algún fallo pone los datos que ya ha introducido el usuario junto 
			// a los mensajes de aviso y el foco en el primer fallo encontrado
		return true;
	}
}