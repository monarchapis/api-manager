// Generated from EventQuery.g4 by ANTLR 4.3
package com.monarchapis.apimanager.analytics.grammar;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EventQueryParser}.
 */
public interface EventQueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link EventQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(@NotNull EventQueryParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(@NotNull EventQueryParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#regularExpressionEvaluation}.
	 * @param ctx the parse tree
	 */
	void enterRegularExpressionEvaluation(@NotNull EventQueryParser.RegularExpressionEvaluationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#regularExpressionEvaluation}.
	 * @param ctx the parse tree
	 */
	void exitRegularExpressionEvaluation(@NotNull EventQueryParser.RegularExpressionEvaluationContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(@NotNull EventQueryParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(@NotNull EventQueryParser.NumericContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#containsOperation}.
	 * @param ctx the parse tree
	 */
	void enterContainsOperation(@NotNull EventQueryParser.ContainsOperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#containsOperation}.
	 * @param ctx the parse tree
	 */
	void exitContainsOperation(@NotNull EventQueryParser.ContainsOperationContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#arrayOperation}.
	 * @param ctx the parse tree
	 */
	void enterArrayOperation(@NotNull EventQueryParser.ArrayOperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#arrayOperation}.
	 * @param ctx the parse tree
	 */
	void exitArrayOperation(@NotNull EventQueryParser.ArrayOperationContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(@NotNull EventQueryParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(@NotNull EventQueryParser.ListContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#fieldFilter}.
	 * @param ctx the parse tree
	 */
	void enterFieldFilter(@NotNull EventQueryParser.FieldFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#fieldFilter}.
	 * @param ctx the parse tree
	 */
	void exitFieldFilter(@NotNull EventQueryParser.FieldFilterContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#arrayComparison}.
	 * @param ctx the parse tree
	 */
	void enterArrayComparison(@NotNull EventQueryParser.ArrayComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#arrayComparison}.
	 * @param ctx the parse tree
	 */
	void exitArrayComparison(@NotNull EventQueryParser.ArrayComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#nameValuePair}.
	 * @param ctx the parse tree
	 */
	void enterNameValuePair(@NotNull EventQueryParser.NameValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#nameValuePair}.
	 * @param ctx the parse tree
	 */
	void exitNameValuePair(@NotNull EventQueryParser.NameValuePairContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#operation}.
	 * @param ctx the parse tree
	 */
	void enterOperation(@NotNull EventQueryParser.OperationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#operation}.
	 * @param ctx the parse tree
	 */
	void exitOperation(@NotNull EventQueryParser.OperationContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#simpleComparison}.
	 * @param ctx the parse tree
	 */
	void enterSimpleComparison(@NotNull EventQueryParser.SimpleComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#simpleComparison}.
	 * @param ctx the parse tree
	 */
	void exitSimpleComparison(@NotNull EventQueryParser.SimpleComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#logicalGroup}.
	 * @param ctx the parse tree
	 */
	void enterLogicalGroup(@NotNull EventQueryParser.LogicalGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#logicalGroup}.
	 * @param ctx the parse tree
	 */
	void exitLogicalGroup(@NotNull EventQueryParser.LogicalGroupContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(@NotNull EventQueryParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(@NotNull EventQueryParser.ValueContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#containsComparison}.
	 * @param ctx the parse tree
	 */
	void enterContainsComparison(@NotNull EventQueryParser.ContainsComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#containsComparison}.
	 * @param ctx the parse tree
	 */
	void exitContainsComparison(@NotNull EventQueryParser.ContainsComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#otherComparison}.
	 * @param ctx the parse tree
	 */
	void enterOtherComparison(@NotNull EventQueryParser.OtherComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#otherComparison}.
	 * @param ctx the parse tree
	 */
	void exitOtherComparison(@NotNull EventQueryParser.OtherComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#equalsComparison}.
	 * @param ctx the parse tree
	 */
	void enterEqualsComparison(@NotNull EventQueryParser.EqualsComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#equalsComparison}.
	 * @param ctx the parse tree
	 */
	void exitEqualsComparison(@NotNull EventQueryParser.EqualsComparisonContext ctx);

	/**
	 * Enter a parse tree produced by {@link EventQueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(@NotNull EventQueryParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link EventQueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(@NotNull EventQueryParser.FilterContext ctx);
}