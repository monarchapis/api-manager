// Generated from EventQuery.g4 by ANTLR 4.3
package com.monarchapis.apimanager.analytics.grammar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EventQueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EQ=1, LT=2, LE=3, GT=4, GE=5, NE=6, RE=7, IN=8, NIN=9, ALL=10, OR=11, 
		NOR=12, DOT=13, OPEN_PAR=14, CLOSE_PAR=15, OPEN_BRACKET=16, CLOSE_BRACKET=17, 
		PLUS=18, COMMA=19, STRING=20, INTEGER=21, DECIMAL=22, BOOLEAN=23, NULL=24, 
		NAME=25, REGEX=26, WS=27;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'", "'\\u0018'", 
		"'\\u0019'", "'\\u001A'", "'\\u001B'"
	};
	public static final String[] ruleNames = {
		"EQ", "LT", "LE", "GT", "GE", "NE", "RE", "IN", "NIN", "ALL", "OR", "NOR", 
		"DOT", "OPEN_PAR", "CLOSE_PAR", "OPEN_BRACKET", "CLOSE_BRACKET", "PLUS", 
		"COMMA", "STRING", "INTEGER", "DECIMAL", "BOOLEAN", "NULL", "NAME", "REGEX", 
		"REGEX_ESC_SEQ", "DIGIT", "HEX_DIGIT", "ESC_SEQ", "OCTAL_ESC", "UNICODE_ESC", 
		"WS"
	};


	public EventQueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "EventQuery.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\35\u00fe\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\5\4Q\n\4\3"+
		"\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\5\6[\n\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\5\bg\n\b\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3"+
		"\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22"+
		"\3\22\3\23\5\23\u0086\n\23\3\23\3\23\5\23\u008a\n\23\3\24\5\24\u008d\n"+
		"\24\3\24\3\24\5\24\u0091\n\24\3\25\3\25\3\25\7\25\u0096\n\25\f\25\16\25"+
		"\u0099\13\25\3\25\3\25\3\26\6\26\u009e\n\26\r\26\16\26\u009f\3\27\7\27"+
		"\u00a3\n\27\f\27\16\27\u00a6\13\27\3\27\3\27\6\27\u00aa\n\27\r\27\16\27"+
		"\u00ab\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u00b7\n\30\3"+
		"\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00c1\n\31\3\32\3\32\7\32"+
		"\u00c5\n\32\f\32\16\32\u00c8\13\32\3\33\3\33\3\33\7\33\u00cd\n\33\f\33"+
		"\16\33\u00d0\13\33\3\33\3\33\7\33\u00d4\n\33\f\33\16\33\u00d7\13\33\3"+
		"\34\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3\37\5\37\u00e4\n\37"+
		"\3 \3 \3 \3 \3 \3 \3 \3 \3 \5 \u00ef\n \3!\3!\3!\3!\3!\3!\3!\3\"\6\"\u00f9"+
		"\n\"\r\"\16\"\u00fa\3\"\3\"\2\2#\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23"+
		"\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31"+
		"\61\32\63\33\65\34\67\29\2;\2=\2?\2A\2C\35\3\2\13\4\2$$^^\5\2C\\aac|\6"+
		"\2\62;C\\aac|\4\2\61\61^^\6\2kkoouuzz\b\2&&*-\60\61AA]`~~\5\2\62;CHch"+
		"\n\2$$))^^ddhhppttvv\5\2\13\f\17\17\"\"\u010e\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"+
		"\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2"+
		"\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2"+
		"\2\2\2\65\3\2\2\2\2C\3\2\2\2\3E\3\2\2\2\5H\3\2\2\2\7P\3\2\2\2\tR\3\2\2"+
		"\2\13Z\3\2\2\2\r\\\3\2\2\2\17f\3\2\2\2\21h\3\2\2\2\23k\3\2\2\2\25o\3\2"+
		"\2\2\27s\3\2\2\2\31v\3\2\2\2\33z\3\2\2\2\35|\3\2\2\2\37~\3\2\2\2!\u0080"+
		"\3\2\2\2#\u0082\3\2\2\2%\u0085\3\2\2\2\'\u008c\3\2\2\2)\u0092\3\2\2\2"+
		"+\u009d\3\2\2\2-\u00a4\3\2\2\2/\u00b6\3\2\2\2\61\u00c0\3\2\2\2\63\u00c2"+
		"\3\2\2\2\65\u00c9\3\2\2\2\67\u00d8\3\2\2\29\u00db\3\2\2\2;\u00dd\3\2\2"+
		"\2=\u00e3\3\2\2\2?\u00ee\3\2\2\2A\u00f0\3\2\2\2C\u00f8\3\2\2\2EF\7g\2"+
		"\2FG\7s\2\2G\4\3\2\2\2HI\7n\2\2IJ\7v\2\2J\6\3\2\2\2KL\7n\2\2LQ\7g\2\2"+
		"MN\7n\2\2NO\7v\2\2OQ\7g\2\2PK\3\2\2\2PM\3\2\2\2Q\b\3\2\2\2RS\7i\2\2ST"+
		"\7v\2\2T\n\3\2\2\2UV\7i\2\2V[\7g\2\2WX\7i\2\2XY\7v\2\2Y[\7g\2\2ZU\3\2"+
		"\2\2ZW\3\2\2\2[\f\3\2\2\2\\]\7p\2\2]^\7g\2\2^\16\3\2\2\2_`\7t\2\2`g\7"+
		"g\2\2ab\7t\2\2bc\7g\2\2cd\7i\2\2de\7g\2\2eg\7z\2\2f_\3\2\2\2fa\3\2\2\2"+
		"g\20\3\2\2\2hi\7k\2\2ij\7p\2\2j\22\3\2\2\2kl\7p\2\2lm\7k\2\2mn\7p\2\2"+
		"n\24\3\2\2\2op\7c\2\2pq\7n\2\2qr\7n\2\2r\26\3\2\2\2st\7q\2\2tu\7t\2\2"+
		"u\30\3\2\2\2vw\7p\2\2wx\7q\2\2xy\7t\2\2y\32\3\2\2\2z{\7\60\2\2{\34\3\2"+
		"\2\2|}\7*\2\2}\36\3\2\2\2~\177\7+\2\2\177 \3\2\2\2\u0080\u0081\7]\2\2"+
		"\u0081\"\3\2\2\2\u0082\u0083\7_\2\2\u0083$\3\2\2\2\u0084\u0086\5C\"\2"+
		"\u0085\u0084\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089"+
		"\7-\2\2\u0088\u008a\5C\"\2\u0089\u0088\3\2\2\2\u0089\u008a\3\2\2\2\u008a"+
		"&\3\2\2\2\u008b\u008d\5C\"\2\u008c\u008b\3\2\2\2\u008c\u008d\3\2\2\2\u008d"+
		"\u008e\3\2\2\2\u008e\u0090\7.\2\2\u008f\u0091\5C\"\2\u0090\u008f\3\2\2"+
		"\2\u0090\u0091\3\2\2\2\u0091(\3\2\2\2\u0092\u0097\7$\2\2\u0093\u0096\5"+
		"=\37\2\u0094\u0096\n\2\2\2\u0095\u0093\3\2\2\2\u0095\u0094\3\2\2\2\u0096"+
		"\u0099\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u009a\3\2"+
		"\2\2\u0099\u0097\3\2\2\2\u009a\u009b\7$\2\2\u009b*\3\2\2\2\u009c\u009e"+
		"\59\35\2\u009d\u009c\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u009d\3\2\2\2\u009f"+
		"\u00a0\3\2\2\2\u00a0,\3\2\2\2\u00a1\u00a3\59\35\2\u00a2\u00a1\3\2\2\2"+
		"\u00a3\u00a6\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a7"+
		"\3\2\2\2\u00a6\u00a4\3\2\2\2\u00a7\u00a9\7\60\2\2\u00a8\u00aa\59\35\2"+
		"\u00a9\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00ac"+
		"\3\2\2\2\u00ac.\3\2\2\2\u00ad\u00ae\7v\2\2\u00ae\u00af\7t\2\2\u00af\u00b0"+
		"\7w\2\2\u00b0\u00b7\7g\2\2\u00b1\u00b2\7h\2\2\u00b2\u00b3\7c\2\2\u00b3"+
		"\u00b4\7n\2\2\u00b4\u00b5\7u\2\2\u00b5\u00b7\7g\2\2\u00b6\u00ad\3\2\2"+
		"\2\u00b6\u00b1\3\2\2\2\u00b7\60\3\2\2\2\u00b8\u00b9\7p\2\2\u00b9\u00ba"+
		"\7w\2\2\u00ba\u00bb\7n\2\2\u00bb\u00c1\7n\2\2\u00bc\u00bd\7P\2\2\u00bd"+
		"\u00be\7W\2\2\u00be\u00bf\7N\2\2\u00bf\u00c1\7N\2\2\u00c0\u00b8\3\2\2"+
		"\2\u00c0\u00bc\3\2\2\2\u00c1\62\3\2\2\2\u00c2\u00c6\t\3\2\2\u00c3\u00c5"+
		"\t\4\2\2\u00c4\u00c3\3\2\2\2\u00c5\u00c8\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6"+
		"\u00c7\3\2\2\2\u00c7\64\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c9\u00ce\7\61\2"+
		"\2\u00ca\u00cd\5\67\34\2\u00cb\u00cd\n\5\2\2\u00cc\u00ca\3\2\2\2\u00cc"+
		"\u00cb\3\2\2\2\u00cd\u00d0\3\2\2\2\u00ce\u00cc\3\2\2\2\u00ce\u00cf\3\2"+
		"\2\2\u00cf\u00d1\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d1\u00d5\7\61\2\2\u00d2"+
		"\u00d4\t\6\2\2\u00d3\u00d2\3\2\2\2\u00d4\u00d7\3\2\2\2\u00d5\u00d3\3\2"+
		"\2\2\u00d5\u00d6\3\2\2\2\u00d6\66\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8\u00d9"+
		"\7^\2\2\u00d9\u00da\t\7\2\2\u00da8\3\2\2\2\u00db\u00dc\4\62;\2\u00dc:"+
		"\3\2\2\2\u00dd\u00de\t\b\2\2\u00de<\3\2\2\2\u00df\u00e0\7^\2\2\u00e0\u00e4"+
		"\t\t\2\2\u00e1\u00e4\5A!\2\u00e2\u00e4\5? \2\u00e3\u00df\3\2\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e3\u00e2\3\2\2\2\u00e4>\3\2\2\2\u00e5\u00e6\7^\2\2\u00e6"+
		"\u00e7\4\62\65\2\u00e7\u00e8\4\629\2\u00e8\u00ef\4\629\2\u00e9\u00ea\7"+
		"^\2\2\u00ea\u00eb\4\629\2\u00eb\u00ef\4\629\2\u00ec\u00ed\7^\2\2\u00ed"+
		"\u00ef\4\629\2\u00ee\u00e5\3\2\2\2\u00ee\u00e9\3\2\2\2\u00ee\u00ec\3\2"+
		"\2\2\u00ef@\3\2\2\2\u00f0\u00f1\7^\2\2\u00f1\u00f2\7w\2\2\u00f2\u00f3"+
		"\5;\36\2\u00f3\u00f4\5;\36\2\u00f4\u00f5\5;\36\2\u00f5\u00f6\5;\36\2\u00f6"+
		"B\3\2\2\2\u00f7\u00f9\t\n\2\2\u00f8\u00f7\3\2\2\2\u00f9\u00fa\3\2\2\2"+
		"\u00fa\u00f8\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc\u00fd"+
		"\b\"\2\2\u00fdD\3\2\2\2\30\2PZf\u0085\u0089\u008c\u0090\u0095\u0097\u009f"+
		"\u00a4\u00ab\u00b6\u00c0\u00c6\u00cc\u00ce\u00d5\u00e3\u00ee\u00fa\3\b"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}