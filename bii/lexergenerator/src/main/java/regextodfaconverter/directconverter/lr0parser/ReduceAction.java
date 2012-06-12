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
 * Authors: Johannes Dahlke
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package regextodfaconverter.directconverter.lr0parser;


import regextodfaconverter.directconverter.EventHandler;
import regextodfaconverter.directconverter.lr0parser.grammar.EmptyString;
import regextodfaconverter.directconverter.lr0parser.grammar.ProductionRule;
import regextodfaconverter.directconverter.lr0parser.grammar.RuleElement;
import regextodfaconverter.directconverter.lr0parser.grammar.RuleElementSequenz;
import regextodfaconverter.directconverter.lr0parser.grammar.Symbol;
import regextodfaconverter.directconverter.lr0parser.grammar.Terminal;
import regextodfaconverter.directconverter.lr0parser.itemset.Closure;

/**
 * 
 * @author Johannes Dahlke
 *
 * @param <Element>
 */
public class ReduceAction<Element extends Symbol> extends Action<Element> implements EventHandler {

	private ProductionRule reduceRule;
	
	public ReduceAction( ItemAutomataInterior<Element> itemAutomata, ProductionRule reduceRule) {
		super( itemAutomata);
		this.reduceRule = reduceRule;
	}

	public Object handle(Object sender) throws ReduceException {
		// apply rule elements and reduce reduce the stacks by the way
		RuleElementSequenz rightReduceRuleSide = reduceRule.getRightRuleSide();
		for ( int i = rightReduceRuleSide.size(); i > 0; i--) {
		   itemAutomata.getClosureStack().pop();
		   RuleElement reduceRuleElement = rightReduceRuleSide.get( i-1);
		   if ( !( reduceRuleElement instanceof EmptyString)) {
		     RuleElement elementFromStack = itemAutomata.getSymbolStack().pop();
		  
         if ( ! elementFromStack.equals( reduceRuleElement))
			     throw new ReduceException(String.format("Missing expected element %s while reduce with rule %s. Found instead %s.", reduceRuleElement, reduceRule, elementFromStack));
		   }
		}
		itemAutomata.getSymbolStack().push( reduceRule.getLeftRuleSide());
		
		return itemAutomata.getSymbolStack().peek();
	}
	
	public ProductionRule getReduceRule() {
		return reduceRule;
	}
	
	@Override
	public String toString() {
		return "Reduce with rule " + reduceRule.toString();
	}
	

}
