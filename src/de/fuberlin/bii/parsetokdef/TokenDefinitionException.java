/*
 * 
 * Copyright 2012 lexergen.
 * This file is part of lexergen.
 * 
 * lexergen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * lexergen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with lexergen.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * lexergen:
 * A tool to chunk source code into tokens for further processing in a compiler chain.
 * 
 * Projectgroup: bi, bii
 * 
 * Authors: Benjamin Weißenfels
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package de.fuberlin.bii.parsetokdef;

/**
 * 
 * @author 
 *
 */
public class TokenDefinitionException extends Exception {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public TokenDefinitionException(String message) {
		super(message);
	}
	
	/**
	 * Erstellt ein neues TokenDefinitionException Objekt.
	 */
	public TokenDefinitionException(int line, String message) {
		this("Something went wrong when reading the token definition\nline "
				+ line + ": " + message);
	}
	
	/**
	 * Erstellt ein neues TokenDefinitionException Objekt.
	 */
	public TokenDefinitionException(int line, int col, String message) {
		this("Something went wrong when reading the token definition\n("
				+ line + ","+col+") : " + message);
	}
}

