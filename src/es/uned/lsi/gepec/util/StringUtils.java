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

public class StringUtils
{
	/**
	 * Abbreviates a string using ellipses.<br/><br/>
	 * Leading and trailing whitespace are ignored.<br/><br/>
	 * When abbreviating a string, this method will try to leave a whitespace between the last 
	 * non trimmed word and the ellipses if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of ellipses 
	 * is taking account when we need to abbreviate it)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
	public static String abbreviate(String str,int maxLength)
	{
		return abbreviate(str,maxLength,true,"...",false,false);
	}
	
	/**
	 * Abbreviates a string using ellipses.<br/><br/>
	 * When abbreviating a string, this method will try to leave a whitespace between the last 
	 * non trimmed word and the ellipses if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of ellipses 
	 * is taking account when we need to abbreviate it)
	 * @param trim Flag to indicate if we want to omit leading and trailing whitespaces (true) 
	 * or not (false) 
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
	public static String abbreviate(String str,int maxLength,boolean trim)
	{
		return abbreviate(str,maxLength,trim,"...",false,false);
	}
	
	/**
	 * Abbreviates a string.<br/><br/>
	 * When abbreviating a string, this method will try to leave a whitespace 
	 * between the last non trimmed word and the string to append at the end if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of string to append at 
	 * the end is taking account when we need to abbreviate it)
	 * @param trim Flag to indicate if we want to omit leading and trailing whitespaces (true) 
	 * or not (false) 
	 * @param appendToEnd String to append at the end (if the string needs to be abbreviated)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
	public static String abbreviate(String str,int maxLength,boolean trim,String appendToEnd)
	{
		return abbreviate(str,maxLength,trim,appendToEnd,false,false);
	}
	
	/**
	 * Abbreviates a string.<br/><br/>
	 * When abbreviating a string, if we are not ignoring words this method will try to leave a whitespace 
	 * between the last non trimmed word and the string to append at the end if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of string to append at 
	 * the end is taking account when we need to abbreviate it)
	 * @param trim Flag to indicate if we want to omit leading and trailing whitespaces (true) 
	 * or not (false) 
	 * @param appendToEnd String to append at the end (if the string needs to be abbreviated)
	 * @param ignoreWords Flag to indicate if we want to ignore words when abbreviating (true) 
	 * or not (false)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
	public static String abbreviate(String str,int maxLength,boolean trim,String appendToEnd,
		boolean ignoreWords)
	{
		return abbreviate(str,maxLength,trim,appendToEnd,ignoreWords,false);
	}
	
	/**
	 * Abbreviates a string.<br/><br/>
	 * When abbreviating a string, if we are not ignoring words this method will try to leave a whitespace 
	 * before the string to append at the end if there is enough space.
	 * @param str String to abbreviate (if needed)
	 * @param maxLength Maximum length of the abbreviated string (but length of string to append at 
	 * the end is taking account when we need to abbreviate it if <i>ignoreAppendToEndLength</i> is
	 * false)
	 * @param trim Flag to indicate if we want to omit leading and trailing whitespaces (true) 
	 * or not (false) 
	 * @param appendToEnd String to append at the end (if the string needs to be abbreviated)
	 * @param ignoreWords Flag to indicate if we want to ignore words when abbreviating (true) 
	 * or not (false)
	 * @param ignoreAppendToEndLength Flag to indicate if we want to ignore length of string to append 
	 * at the end when abbreviating (true) or not (false)
	 * @return Same string if its length is less or equal than <i>maxLength</i> or abbreviated string
	 * otherwise
	 */
	public static String abbreviate(String str,int maxLength,boolean trim,String appendToEnd,
		boolean ignoreWords,boolean ignoreAppendToEndLength)
	{
		StringBuffer newStr=null;
		if (str==null)
		{
			str="";
		}
		else if (trim)
		{
			str=str.trim();
		}
		if (str.length()>maxLength)
		{
			int safeMaxLength=maxLength;
			if (!ignoreAppendToEndLength)
			{
				safeMaxLength-=appendToEnd.length();
			}
			if (!ignoreWords)
			{
	        	int i=safeMaxLength-1;
	        	char ch=str.charAt(i);
	        	while (!Character.isWhitespace(ch))
	        	{
	        		i--;
	        		if (i>=0)
	        		{
	        			ch=str.charAt(i);
	        		}
	        		else
	        		{
	        			ch=' ';
	        		}
	        	}
	        	if (i>=0)
	        	{
		        	while (Character.isWhitespace(ch))
		        	{
		        		i--;
		        		if (i>=0)
		        		{
		        			ch=str.charAt(i);
		        		}
		        		else
		        		{
		        			ch='0';
		        		}
		        	}
	        		if (i>=0)
	        		{
	        			newStr=new StringBuffer(str.substring(0,i+1));
	        			if (i<safeMaxLength-1)
	        			{
	        				newStr.append(' ');
	        			}
	        			newStr.append(appendToEnd);
	        		}
	        	}
			}
			if (newStr==null)
        	{
				newStr=new StringBuffer(str.substring(0,safeMaxLength));
				newStr.append(appendToEnd);
        	}
		}
		return newStr==null?str:newStr.toString();
	}
	
	/**
	 * Abbreviates words within a string using short ellipses.<br/><br/>
	 * @param str String to abbreviate words (if needed)
	 * @param maxWordLength Maximum length of the abbreviated words
	 * @return Same string but with large words abbreviated
	 */
	public static String abbreviateWords(String str,int maxWordLength)
	{
		return abbreviateWords(str,maxWordLength,"..",true);
	}
	
	/**
	 * Abbreviates words within a string.<br/><br/>
	 * @param str String to abbreviate words (if needed)
	 * @param maxWordLength Maximum length of the abbreviated words
	 * @param appendToWordEnd String to append at the end of word (for the words needing to be abbreviated)
	 * @return Same string but with large words abbreviated
	 */
	public static String abbreviateWords(String str,int maxWordLength,String appendToWordEnd)
	{
		return abbreviateWords(str,maxWordLength,appendToWordEnd,true);
	}
	
	/**
	 * Abbreviates words within a string.<br/><br/>
	 * @param str String to abbreviate words (if needed)
	 * @param maxWordLength Maximum length of the abbreviated words (but length of string to append at 
	 * the end of words is taking account when we need to abbreviate it if <i>ignoreAppendToWordEndLength</i> is
	 * false)
	 * @param appendToWordEnd String to append at the end of word (for the words needing to be abbreviated)
	 * @param ignoreAppendToWordEndLength Flag to indicate if we want to ignore length of string to append 
	 * at the end of words when abbreviating (true) or not (false)
	 * @return Same string but with large words abbreviated
	 */
	public static String abbreviateWords(String str,int maxWordLength,String appendToWordEnd,
		boolean ignoreAppendToWordEndLength)
	{
		StringBuffer newStr=new StringBuffer();
		if (str!=null)
		{
			if (!ignoreAppendToWordEndLength)
			{
				maxWordLength-=appendToWordEnd.length();
			}
			int i=0;
			char c=i<str.length()?str.charAt(0):'\0';
			while (i<str.length())
			{
				while (Character.isWhitespace(c))
				{
					newStr.append(c);
					i++;
					c=i<str.length()?str.charAt(i):'\0';
				}
				if (i<str.length())
				{
					int iWord=i;
					int wordLength=0;
					while (!Character.isWhitespace(c))
					{
						wordLength++;
						i++;
						c=i<str.length()?str.charAt(i):' ';
					}
					if (wordLength<=maxWordLength)
					{
						newStr.append(str.substring(iWord,iWord+wordLength));
					}
					else
					{
						newStr.append(str.substring(iWord,iWord+maxWordLength));
						newStr.append(appendToWordEnd);
					}
				}
			}
		}
		return newStr.toString();
	}
	
	/**
	 * @param str String
	 * @return true if string has at least one letter, false otherwise
	 */
	public static boolean hasLetter(String str)
	{
		boolean foundLetter=false;
		if (str!=null)
		{
			for (int i=0;i<str.length();i++)
			{
				if (Character.isLetter(str.charAt(i)))
				{
					foundLetter=true;
					break;
				}
			}
		}
		return foundLetter;
	}
	
	/**
	 * @param str String
	 * @return true if first character of string is a letter, false otherwise
	 */
	public static boolean isFirstCharacterLetter(String str)
	{
		return str!=null && !str.equals("") && Character.isLetter(str.charAt(0));
	}
	
	/**
	 * @param str String
	 * @return true if first character of string is a digit, false otherwise
	 */
	public static boolean isFirstCharacterDigit(String str)
	{
		return str!=null && !str.equals("") && Character.isDigit(str.charAt(0));
	}
	
	/**
	 * @param str String
	 * @return true if first character of string is a whitespace, false otherwise
	 */
	public static boolean isFirstCharacterWhitespace(String str)
	{
		return str!=null && !str.equals("") && Character.isWhitespace(str.charAt(0));
	}
	
	/**
	 * @param str String
	 * @return true if last character of string is a whitespace, false otherwise
	 */
	public static boolean isLastCharacterWhitespace(String str)
	{
		return str!=null && !str.equals("") && Character.isWhitespace(str.charAt(str.length()-1));
	}
	
	/**
	 * @param str String
	 * @param includeLetters true if string is expected to include letters, false if not 
	 * @param includeDigits true if string is expected to include digits, false if not
	 * @param includeWhitespaces true if string is expected to include whitespaces, false if not
	 * @param otherExpectedCharacters Array with other characters expected to be found within string
	 * @return true if string has at least an unexpected character, false otherwise
	 */
	public static boolean hasUnexpectedCharacters(String str,boolean includeLetters,boolean includeDigits,
		boolean includeWhitespaces,char[] otherExpectedCharacters)
	{
		boolean foundUnexpectedCharacter=false;
		if (str!=null)
		{
			for (int i=0;i<str.length();i++)
			{
				char c=str.charAt(i);
				if (Character.isLetter(c))
				{
					if (includeLetters)
					{
						continue;
					}
					else
					{
						foundUnexpectedCharacter=true;
						break;
					}
				}
				if (Character.isDigit(c))
				{
					if (includeDigits)
					{
						continue;
					}
					else
					{
						foundUnexpectedCharacter=true;
						break;
					}
				}
				if (Character.isWhitespace(c))
				{
					if (includeWhitespaces)
					{
						continue;
					}
					else
					{
						foundUnexpectedCharacter=true;
						break;
					}
				}
				if (otherExpectedCharacters==null)
				{
					foundUnexpectedCharacter=true;
					break;
				}
				else
				{
					for (char otherExpectedCharacter:otherExpectedCharacters)
					{
						if (c==otherExpectedCharacter)
						{
							foundUnexpectedCharacter=true;
							break;
						}
					}
					if (foundUnexpectedCharacter)
					{
						break;
					}
				}
			}
		}
		return foundUnexpectedCharacter;
	}
	
	/**
	 * @param str String
	 * @return true if string has consecutive whitespaces, false otherwise
	 */
	public static boolean hasConsecutiveWhitespaces(String str)
	{
		boolean foundConsecutiveWhitespaces=false;
		if (str!=null)
		{
			boolean foundWhitespace=false;
			for (int i=0;i<str.length();i++)
			{
				if (Character.isWhitespace(str.charAt(i)))
				{
					if (foundWhitespace)
					{
						foundConsecutiveWhitespaces=true;
						break;
					}
					else
					{
						foundWhitespace=true;
					}
				}
				else
				{
					foundWhitespace=false;
				}
			}
		}
		return foundConsecutiveWhitespaces;
	}
}
