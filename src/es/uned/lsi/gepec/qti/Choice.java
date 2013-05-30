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
 * Componente de control impl�cito asociado a la p�gina JSP "choice.jsp". Es la l�gica 
 * asociada a esa p�gina JSP. Se ejecutar� su m�todo <code>doLogic</code> siempre que sea 
 * llamada la p�gina. Su funci�n es comprobar que todos los par�metros iniciales de la toma de 
 * datos de una pregunta tipo "Choice" sean correctos, en cuyo caso redirecciona la respuesta 
 * a la siguiente p�gina de toma de datos de tipo "Choice". La clase implementa la interfaz 
 * <code>Control</code>, implementando su m�todo <code>doLogic</code>, que es el �nico que 
 * contiene, y que es el encargado de realizar la l�gica asociada a la p�gina "choice.jsp".
 * 
 * @author 	Blas Medina Alcudia
 * @see		Control
 * 
 */
public class Choice {
	
	/**
	 * Realiza la comprobaci�n de los par�metros que se recogen en la p�gina "choice.jsp". 
	 * Ser� llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * p�gina requerida es "choice.jsp". Ya que es llamado desde un filtro, ser� ejecutado 
	 * siempre que llegue una petici�n de un cliente y antes de que la petici�n llegue al 
	 * Servlet (p�gina JSP compilada a Servlet) que sirve la respuesta al cliente. Comprueba 
	 * uno por uno todos los par�metros, y, en caso de que sean correctos, crea un objeto <code>
	 * ChoiceXML</code> para guardar los datos asociados al tipo de pregunta y lo guarda en el 
	 * �mbito de sesi�n para utilizarlos m�s adelante para terminar de crear el �tem.
	 * 
	 * @param	request				la petici�n http enviada por el cliente
	 * @param	response			la respuesta http a enviar desde el servidor
	 * @return						true para que se sigan ejecutando los dem�s filtros del 
	 * 								servidor y se env�e la respuesta correctamente
	 * @throws	IOException			no debe de producirse
	 * @throws	ServletException	si hay alg�n problema accediendo a request o response
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
		
		// Comprueba si es la primera vez que se visita la p�gina
			// Caracteres reservados no permitidos
			final String reservados = ";/?:@&=+$,\\*\"<>|";

			String identificador = "Identif";	// Identificador del �tem 
			String titulo = "Tit";			// T�tulo del �tem
			String instrucciones = "Instruc";	// Instrucciones del �tem
			String pregunta = "Preg";			// Pregunta del �tem
			String Shuff = "Shuffle";		// Mezclar las respuestas o no
			String numResp = "Num_resp";		// N�mero de respuestas
			List<Answer> listarespuestas = null;
			// Estado de la petici�n web. Almacena si son correctos los par�metros, los 
			// mensajes de aviso al usuario y d�nde colocar el foco en la p�gina
			EstadoWeb estado = new EstadoWeb();		
			
			boolean shuffle = false;		// Valor por defecto del par�metro
			if ( Shuff != null){			// Interpreta el valor marcado en la p�gina 
				if (Shuff.equalsIgnoreCase("ON"))
					shuffle = true;
			}

			// Comprueba que los caracteres del identificador est�n permitidos
		        for(int i = 0; i < identificador.length() && estado.isEstado() == true; i++) {
		        	char c = identificador.charAt(i);	// Lee el siguiente car�cter
		        	int marca = reservados.indexOf(c);
		        	if (marca >= 0 ){
						estado.a�adeError("Error: El car�cter '" + reservados.charAt(marca) + 
								"' en la posici�n " + (i+1) + " del Identificador no est� " +
								"permitido<br />", "Identif");
		        	}
		        }

				// Comprueba que el n�mero de respuestas sea un valor v�lido
				EnteroPositivo numRespuestas = new EnteroPositivo(numResp);
				MensajeEstado msg = numRespuestas.esPositivo();
				if(msg.isEstado()){		// Si es un n�mero v�lido y mayor o igual que cero
					if (numRespuestas.getEnteroPositivo() <=  1)// Si es menor o igual que uno
						estado.a�adeError("El n�mero de respuestas debe ser mayor que 1<br />",
								"Num_resp");
				} else 					// Si no es un entero positivo v�lido
					estado.a�adeError("Error: el n�mero de respuestas " + msg.getMensaje() + 
							"<br />", "Num_resp");
				
				// Comprueba que las instrucciones sean un c�digo XHTML v�lido
				if (instrucciones != null && !instrucciones.equals("")){
					msg = ParserNeutro.compruebaInstruccionesXHTML(instrucciones);
					if (msg.isEstado() == false)
						estado.a�adeError(msg.getMensaje(), "Instruc");
				}
				
				// Si el estado es todo correcto pasa a generar el objeto contenedor del �tem 
				if ( estado.isEstado()){
					// Crea un objeto que contiene las caracter�sticas del �tem Choice a crear
					ChoiceXML chXML = new ChoiceXML(identificador, titulo, instrucciones,
							pregunta, shuffle, listarespuestas);
					// Guarda el objeto choiceXML en sesi�n para seguir utiliz�ndolo luego
					// Redirecciona a la p�gina de introducir las respuestas indicando el
					// n�mero de respuestas y si hay que mezclarlas aleatoriamente
				}
			// Si ha habido alg�n fallo pone los datos que ya ha introducido el usuario junto 
			// a los mensajes de aviso y el foco en el primer fallo encontrado
		return true;
	}
}