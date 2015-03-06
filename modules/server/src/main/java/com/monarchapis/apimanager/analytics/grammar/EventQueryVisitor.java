// Generated from EventQuery.g4 by ANTLR 4.3
package com.monarchapis.apimanager.analytics.grammar;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link EventQueryParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface EventQueryVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link EventQueryParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull EventQueryParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#regularExpressionEvaluation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularExpressionEvaluation(@NotNull EventQueryParser.RegularExpressionEvaluationContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(@NotNull EventQueryParser.NumericContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#containsOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainsOperation(@NotNull EventQueryParser.ContainsOperationContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#arrayOperation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayOperation(@NotNull EventQueryParser.ArrayOperationContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(@NotNull EventQueryParser.ListContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#fieldFilter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldFilter(@NotNull EventQueryParser.FieldFilterContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#arrayComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayComparison(@NotNull EventQueryParser.ArrayComparisonContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#nameValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameValuePair(@NotNull EventQueryParser.NameValuePairContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperation(@NotNull EventQueryParser.OperationContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#simpleComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleComparison(@NotNull EventQueryParser.SimpleComparisonContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#logicalGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalGroup(@NotNull EventQueryParser.LogicalGroupContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(@NotNull EventQueryParser.ValueContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#containsComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainsComparison(@NotNull EventQueryParser.ContainsComparisonContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#otherComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOtherComparison(@NotNull EventQueryParser.OtherComparisonContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#equalsComparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualsComparison(@NotNull EventQueryParser.EqualsComparisonContext ctx);

	/**
	 * Visit a parse tree produced by {@link EventQueryParser#filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter(@NotNull EventQueryParser.FilterContext ctx);
}