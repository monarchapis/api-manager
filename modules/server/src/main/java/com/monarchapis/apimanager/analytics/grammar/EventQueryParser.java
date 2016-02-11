// Generated from EventQuery.g4 by ANTLR 4.3
package com.monarchapis.apimanager.analytics.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EventQueryParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EQ=1, LT=2, LE=3, GT=4, GE=5, NE=6, RE=7, IN=8, NIN=9, ALL=10, OR=11, 
		NOR=12, DOT=13, OPEN_PAR=14, CLOSE_PAR=15, OPEN_BRACKET=16, CLOSE_BRACKET=17, 
		PLUS=18, COMMA=19, STRING=20, INTEGER=21, DECIMAL=22, BOOLEAN=23, NULL=24, 
		NAME=25, REGEX=26, WS=27;
	public static final String[] tokenNames = {
		"<INVALID>", "'eq'", "'lt'", "LE", "'gt'", "GE", "'ne'", "RE", "'in'", 
		"'nin'", "'all'", "'or'", "'nor'", "'.'", "'('", "')'", "'['", "']'", 
		"PLUS", "COMMA", "STRING", "INTEGER", "DECIMAL", "BOOLEAN", "NULL", "NAME", 
		"REGEX", "WS"
	};
	public static final int
		RULE_expression = 0, RULE_filter = 1, RULE_fieldFilter = 2, RULE_equalsComparison = 3, 
		RULE_otherComparison = 4, RULE_simpleComparison = 5, RULE_containsComparison = 6, 
		RULE_arrayComparison = 7, RULE_regularExpressionEvaluation = 8, RULE_operation = 9, 
		RULE_containsOperation = 10, RULE_arrayOperation = 11, RULE_logicalGroup = 12, 
		RULE_list = 13, RULE_nameValuePair = 14, RULE_value = 15, RULE_numeric = 16;
	public static final String[] ruleNames = {
		"expression", "filter", "fieldFilter", "equalsComparison", "otherComparison", 
		"simpleComparison", "containsComparison", "arrayComparison", "regularExpressionEvaluation", 
		"operation", "containsOperation", "arrayOperation", "logicalGroup", "list", 
		"nameValuePair", "value", "numeric"
	};

	@Override
	public String getGrammarFileName() { return "EventQuery.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public EventQueryParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ExpressionContext extends ParserRuleContext {
		public List<FilterContext> filter() {
			return getRuleContexts(FilterContext.class);
		}
		public List<TerminalNode> PLUS() { return getTokens(EventQueryParser.PLUS); }
		public FilterContext filter(int i) {
			return getRuleContext(FilterContext.class,i);
		}
		public TerminalNode PLUS(int i) {
			return getToken(EventQueryParser.PLUS, i);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34); filter();
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(35); match(PLUS);
				setState(36); filter();
				}
				}
				setState(41);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterContext extends ParserRuleContext {
		public FieldFilterContext fieldFilter() {
			return getRuleContext(FieldFilterContext.class,0);
		}
		public LogicalGroupContext logicalGroup() {
			return getRuleContext(LogicalGroupContext.class,0);
		}
		public FilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitFilter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterContext filter() throws RecognitionException {
		FilterContext _localctx = new FilterContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_filter);
		try {
			setState(44);
			switch (_input.LA(1)) {
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(42); fieldFilter();
				}
				break;
			case OR:
			case NOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(43); logicalGroup();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldFilterContext extends ParserRuleContext {
		public EqualsComparisonContext equalsComparison() {
			return getRuleContext(EqualsComparisonContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(EventQueryParser.DOT); }
		public List<OtherComparisonContext> otherComparison() {
			return getRuleContexts(OtherComparisonContext.class);
		}
		public TerminalNode NAME() { return getToken(EventQueryParser.NAME, 0); }
		public OtherComparisonContext otherComparison(int i) {
			return getRuleContext(OtherComparisonContext.class,i);
		}
		public TerminalNode DOT(int i) {
			return getToken(EventQueryParser.DOT, i);
		}
		public FieldFilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldFilter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterFieldFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitFieldFilter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitFieldFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldFilterContext fieldFilter() throws RecognitionException {
		FieldFilterContext _localctx = new FieldFilterContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fieldFilter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46); match(NAME);
			setState(55);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				{
				setState(47); match(DOT);
				setState(48); equalsComparison();
				}
				}
				break;

			case 2:
				{
				setState(51); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(49); match(DOT);
					setState(50); otherComparison();
					}
					}
					setState(53); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EqualsComparisonContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public TerminalNode EQ() { return getToken(EventQueryParser.EQ, 0); }
		public EqualsComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equalsComparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterEqualsComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitEqualsComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitEqualsComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualsComparisonContext equalsComparison() throws RecognitionException {
		EqualsComparisonContext _localctx = new EqualsComparisonContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_equalsComparison);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57); match(EQ);
			setState(58); match(OPEN_PAR);
			setState(59); value();
			setState(60); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OtherComparisonContext extends ParserRuleContext {
		public ContainsComparisonContext containsComparison() {
			return getRuleContext(ContainsComparisonContext.class,0);
		}
		public SimpleComparisonContext simpleComparison() {
			return getRuleContext(SimpleComparisonContext.class,0);
		}
		public RegularExpressionEvaluationContext regularExpressionEvaluation() {
			return getRuleContext(RegularExpressionEvaluationContext.class,0);
		}
		public ArrayComparisonContext arrayComparison() {
			return getRuleContext(ArrayComparisonContext.class,0);
		}
		public OtherComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherComparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterOtherComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitOtherComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitOtherComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OtherComparisonContext otherComparison() throws RecognitionException {
		OtherComparisonContext _localctx = new OtherComparisonContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_otherComparison);
		try {
			setState(66);
			switch (_input.LA(1)) {
			case LT:
			case LE:
			case GT:
			case GE:
			case NE:
				enterOuterAlt(_localctx, 1);
				{
				setState(62); simpleComparison();
				}
				break;
			case IN:
			case NIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(63); containsComparison();
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 3);
				{
				setState(64); arrayComparison();
				}
				break;
			case RE:
				enterOuterAlt(_localctx, 4);
				{
				setState(65); regularExpressionEvaluation();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleComparisonContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public OperationContext operation() {
			return getRuleContext(OperationContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public SimpleComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleComparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterSimpleComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitSimpleComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitSimpleComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleComparisonContext simpleComparison() throws RecognitionException {
		SimpleComparisonContext _localctx = new SimpleComparisonContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_simpleComparison);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68); operation();
			setState(69); match(OPEN_PAR);
			setState(70); value();
			setState(71); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ContainsComparisonContext extends ParserRuleContext {
		public ContainsOperationContext containsOperation() {
			return getRuleContext(ContainsOperationContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public ContainsComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_containsComparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterContainsComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitContainsComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitContainsComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContainsComparisonContext containsComparison() throws RecognitionException {
		ContainsComparisonContext _localctx = new ContainsComparisonContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_containsComparison);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73); containsOperation();
			setState(74); match(OPEN_PAR);
			setState(75); list();
			setState(76); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayComparisonContext extends ParserRuleContext {
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public ArrayOperationContext arrayOperation() {
			return getRuleContext(ArrayOperationContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public ArrayComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayComparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterArrayComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitArrayComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitArrayComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayComparisonContext arrayComparison() throws RecognitionException {
		ArrayComparisonContext _localctx = new ArrayComparisonContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_arrayComparison);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78); arrayOperation();
			setState(79); match(OPEN_PAR);
			setState(80); list();
			setState(81); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegularExpressionEvaluationContext extends ParserRuleContext {
		public TerminalNode RE() { return getToken(EventQueryParser.RE, 0); }
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public TerminalNode REGEX() { return getToken(EventQueryParser.REGEX, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public RegularExpressionEvaluationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularExpressionEvaluation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterRegularExpressionEvaluation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitRegularExpressionEvaluation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitRegularExpressionEvaluation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularExpressionEvaluationContext regularExpressionEvaluation() throws RecognitionException {
		RegularExpressionEvaluationContext _localctx = new RegularExpressionEvaluationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_regularExpressionEvaluation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83); match(RE);
			setState(84); match(OPEN_PAR);
			setState(85); match(REGEX);
			setState(86); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OperationContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(EventQueryParser.LT, 0); }
		public TerminalNode NE() { return getToken(EventQueryParser.NE, 0); }
		public TerminalNode LE() { return getToken(EventQueryParser.LE, 0); }
		public TerminalNode GT() { return getToken(EventQueryParser.GT, 0); }
		public TerminalNode GE() { return getToken(EventQueryParser.GE, 0); }
		public OperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OperationContext operation() throws RecognitionException {
		OperationContext _localctx = new OperationContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_operation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LT) | (1L << LE) | (1L << GT) | (1L << GE) | (1L << NE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ContainsOperationContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(EventQueryParser.IN, 0); }
		public TerminalNode NIN() { return getToken(EventQueryParser.NIN, 0); }
		public ContainsOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_containsOperation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterContainsOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitContainsOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitContainsOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContainsOperationContext containsOperation() throws RecognitionException {
		ContainsOperationContext _localctx = new ContainsOperationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_containsOperation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			_la = _input.LA(1);
			if ( !(_la==IN || _la==NIN) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayOperationContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(EventQueryParser.ALL, 0); }
		public ArrayOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayOperation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterArrayOperation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitArrayOperation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitArrayOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayOperationContext arrayOperation() throws RecognitionException {
		ArrayOperationContext _localctx = new ArrayOperationContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_arrayOperation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92); match(ALL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LogicalGroupContext extends ParserRuleContext {
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode CLOSE_BRACKET() { return getToken(EventQueryParser.CLOSE_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(EventQueryParser.COMMA); }
		public TerminalNode OR() { return getToken(EventQueryParser.OR, 0); }
		public TerminalNode OPEN_BRACKET() { return getToken(EventQueryParser.OPEN_BRACKET, 0); }
		public TerminalNode OPEN_PAR() { return getToken(EventQueryParser.OPEN_PAR, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public TerminalNode NOR() { return getToken(EventQueryParser.NOR, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(EventQueryParser.COMMA, i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(EventQueryParser.CLOSE_PAR, 0); }
		public LogicalGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalGroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterLogicalGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitLogicalGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitLogicalGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalGroupContext logicalGroup() throws RecognitionException {
		LogicalGroupContext _localctx = new LogicalGroupContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_logicalGroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			_la = _input.LA(1);
			if ( !(_la==OR || _la==NOR) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(95); match(OPEN_PAR);
			setState(96); match(OPEN_BRACKET);
			setState(97); expression();
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(98); match(COMMA);
				setState(99); expression();
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105); match(CLOSE_BRACKET);
			setState(106); match(CLOSE_PAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode CLOSE_BRACKET() { return getToken(EventQueryParser.CLOSE_BRACKET, 0); }
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(EventQueryParser.COMMA); }
		public TerminalNode OPEN_BRACKET() { return getToken(EventQueryParser.OPEN_BRACKET, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(EventQueryParser.COMMA, i);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108); match(OPEN_BRACKET);
			setState(109); value();
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(110); match(COMMA);
				setState(111); value();
				}
				}
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(117); match(CLOSE_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameValuePairContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(EventQueryParser.COMMA, 0); }
		public TerminalNode NAME() { return getToken(EventQueryParser.NAME, 0); }
		public NameValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nameValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterNameValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitNameValuePair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitNameValuePair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameValuePairContext nameValuePair() throws RecognitionException {
		NameValuePairContext _localctx = new NameValuePairContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_nameValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119); match(NAME);
			setState(120); match(COMMA);
			setState(121); value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public NumericContext numeric() {
			return getRuleContext(NumericContext.class,0);
		}
		public TerminalNode BOOLEAN() { return getToken(EventQueryParser.BOOLEAN, 0); }
		public TerminalNode NULL() { return getToken(EventQueryParser.NULL, 0); }
		public TerminalNode STRING() { return getToken(EventQueryParser.STRING, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_value);
		int _la;
		try {
			setState(125);
			switch (_input.LA(1)) {
			case INTEGER:
			case DECIMAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(123); numeric();
				}
				break;
			case STRING:
			case BOOLEAN:
			case NULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << BOOLEAN) | (1L << NULL))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(EventQueryParser.INTEGER, 0); }
		public TerminalNode DECIMAL() { return getToken(EventQueryParser.DECIMAL, 0); }
		public NumericContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeric; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).enterNumeric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EventQueryListener ) ((EventQueryListener)listener).exitNumeric(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EventQueryVisitor ) return ((EventQueryVisitor<? extends T>)visitor).visitNumeric(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericContext numeric() throws RecognitionException {
		NumericContext _localctx = new NumericContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_numeric);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			_la = _input.LA(1);
			if ( !(_la==INTEGER || _la==DECIMAL) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\35\u0084\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\7\2(\n\2\f\2\16\2+\13\2\3\3\3\3\5\3/\n\3\3\4\3\4\3\4\3\4"+
		"\3\4\6\4\66\n\4\r\4\16\4\67\5\4:\n\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3"+
		"\6\5\6E\n\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3"+
		"\16\3\16\7\16g\n\16\f\16\16\16j\13\16\3\16\3\16\3\16\3\17\3\17\3\17\3"+
		"\17\7\17s\n\17\f\17\16\17v\13\17\3\17\3\17\3\20\3\20\3\20\3\20\3\21\3"+
		"\21\5\21\u0080\n\21\3\22\3\22\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"\2\7\3\2\4\b\3\2\n\13\3\2\r\16\4\2\26\26\31\32\3\2\27\30|"+
		"\2$\3\2\2\2\4.\3\2\2\2\6\60\3\2\2\2\b;\3\2\2\2\nD\3\2\2\2\fF\3\2\2\2\16"+
		"K\3\2\2\2\20P\3\2\2\2\22U\3\2\2\2\24Z\3\2\2\2\26\\\3\2\2\2\30^\3\2\2\2"+
		"\32`\3\2\2\2\34n\3\2\2\2\36y\3\2\2\2 \177\3\2\2\2\"\u0081\3\2\2\2$)\5"+
		"\4\3\2%&\7\24\2\2&(\5\4\3\2\'%\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2"+
		"*\3\3\2\2\2+)\3\2\2\2,/\5\6\4\2-/\5\32\16\2.,\3\2\2\2.-\3\2\2\2/\5\3\2"+
		"\2\2\609\7\33\2\2\61\62\7\17\2\2\62:\5\b\5\2\63\64\7\17\2\2\64\66\5\n"+
		"\6\2\65\63\3\2\2\2\66\67\3\2\2\2\67\65\3\2\2\2\678\3\2\2\28:\3\2\2\29"+
		"\61\3\2\2\29\65\3\2\2\2:\7\3\2\2\2;<\7\3\2\2<=\7\20\2\2=>\5 \21\2>?\7"+
		"\21\2\2?\t\3\2\2\2@E\5\f\7\2AE\5\16\b\2BE\5\20\t\2CE\5\22\n\2D@\3\2\2"+
		"\2DA\3\2\2\2DB\3\2\2\2DC\3\2\2\2E\13\3\2\2\2FG\5\24\13\2GH\7\20\2\2HI"+
		"\5 \21\2IJ\7\21\2\2J\r\3\2\2\2KL\5\26\f\2LM\7\20\2\2MN\5\34\17\2NO\7\21"+
		"\2\2O\17\3\2\2\2PQ\5\30\r\2QR\7\20\2\2RS\5\34\17\2ST\7\21\2\2T\21\3\2"+
		"\2\2UV\7\t\2\2VW\7\20\2\2WX\7\34\2\2XY\7\21\2\2Y\23\3\2\2\2Z[\t\2\2\2"+
		"[\25\3\2\2\2\\]\t\3\2\2]\27\3\2\2\2^_\7\f\2\2_\31\3\2\2\2`a\t\4\2\2ab"+
		"\7\20\2\2bc\7\22\2\2ch\5\2\2\2de\7\25\2\2eg\5\2\2\2fd\3\2\2\2gj\3\2\2"+
		"\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kl\7\23\2\2lm\7\21\2\2m\33\3"+
		"\2\2\2no\7\22\2\2ot\5 \21\2pq\7\25\2\2qs\5 \21\2rp\3\2\2\2sv\3\2\2\2t"+
		"r\3\2\2\2tu\3\2\2\2uw\3\2\2\2vt\3\2\2\2wx\7\23\2\2x\35\3\2\2\2yz\7\33"+
		"\2\2z{\7\25\2\2{|\5 \21\2|\37\3\2\2\2}\u0080\5\"\22\2~\u0080\t\5\2\2\177"+
		"}\3\2\2\2\177~\3\2\2\2\u0080!\3\2\2\2\u0081\u0082\t\6\2\2\u0082#\3\2\2"+
		"\2\n).\679Dht\177";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}