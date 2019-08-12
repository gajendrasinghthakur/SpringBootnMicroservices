package com.csc.fsg.nba.utility;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AxaExpressionEvaluator {
    public boolean evaluateInfixExpression(String expression) {
    	Stack<String> operands = new Stack<String>();
    	Stack<String> operators = new Stack<String>();
    	while (!expression.equals("")) {
    		String firstChar = expression.substring(0, 1);
    		if (firstChar.equals(" ")) {
    			expression = expression.substring(1);
    			continue;
    		}
    		if ((firstChar.equals("("))) {
    			expression = expression.substring(1);
    		} else if ((firstChar.equals(")"))) {
    			expression = expression.substring(1);
    			evaluateOperator(operands, operators);
    		}
    		else if ((firstChar.equals("!"))) {
    			operators.push(firstChar);
    			expression = expression.substring(1);
    		} else if (firstChar.equals("&")) {
    			operators.push("&&");
    			expression = expression.substring(2);
    		} else if (firstChar.equals("|")) {
    			operators.push("||");
    			expression = expression.substring(2);
    		} else {
    			Pattern aPattern = Pattern.compile("(\\w)+");
    			Matcher aMatcher = aPattern.matcher(expression);
    			aMatcher.find();
    			String operand = aMatcher.group();
    			operands.push(operand);
    			expression = expression.substring(operand.length());
    		}
    	}
    	while (!operators.isEmpty()) {
    		evaluateOperator(operands, operators);
    	}
    	if (!operands.isEmpty()) {
    		operands.push(String.valueOf(evaluateOperand(operands.pop())));
    	}
    	return Boolean.valueOf(operands.pop());
    }
    
    protected void evaluateOperator(Stack<String> operands, Stack<String> operators) {
    	String operator = operators.pop();
    	if (operator.equals("!")) {
    		operands.push(String.valueOf(!evaluateOperand(operands.pop())));
    	} else if (operator.equals("&&")) {
    		String operand1 = operands.pop();
    		String operand2 = operands.pop();
    		if (operand1.equals("false") || operand1.equals("false")) {
    			operands.push("false");
    			return;
    		}
    		if(!evaluateOperand(operand1)) {
    			operands.push("false");
    			return;
    		}
    		if(!evaluateOperand(operand2)) {
    			operands.push("false");
    			return;
    		} 
    		operands.push("true");
    	} else if (operator.equals("||")) {
    		String operand1 = operands.pop();
    		String operand2 = operands.pop();
    		if (operand1.equals("true") || operand1.equals("true")) {
    			operands.push("true");
    			return;
    		}
    		if(evaluateOperand(operand1)) {
    			operands.push("true");
    			return;
    		}
    		if(evaluateOperand(operand2)) {
    			operands.push("true");
    			return;
    		}
    		operands.push("false");
    	}
    }
    
	abstract protected boolean evaluateOperand(String criteria);
}
