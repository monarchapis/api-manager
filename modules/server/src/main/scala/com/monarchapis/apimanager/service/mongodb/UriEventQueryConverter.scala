/*
 * Copyright (C) 2015 CapTech Ventures, Inc.
 * (http://www.captechconsulting.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monarchapis.apimanager.service.mongodb

import scala.collection.JavaConversions._

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils

import com.monarchapis.apimanager.analytics.StringShortener
import com.monarchapis.apimanager.analytics.grammar.EventQueryBaseVisitor
import com.monarchapis.apimanager.analytics.grammar.EventQueryConverter
import com.monarchapis.apimanager.analytics.grammar.EventQueryLexer
import com.monarchapis.apimanager.analytics.grammar.EventQueryParser
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.MongoDBObjectBuilder

class UriEventQueryConverter(shortener: StringShortener) extends EventQueryConverter[DBObject] {
  def apply(query: String) = {
    apply(query, MongoDBObject())
  }

  def apply(query: String, dbObject: DBObject) = {
    val lexer = new EventQueryLexer(new ANTLRInputStream(query))
    val tokens = new CommonTokenStream(lexer)
    val parser = new EventQueryParser(tokens)

    val visitor = new ExpressionVisitor(dbObject)
    visitor.visitExpression(parser.expression)
  }

  trait ValueSupport {
    protected def parseValue(ctx: EventQueryParser.ValueContext) = {
      if (ctx.BOOLEAN != null) {
        ctx.BOOLEAN.getText.toBoolean
      } else if (ctx.numeric != null) {
        val num = ctx.numeric

        if (num.INTEGER != null) {
          num.INTEGER.getText.toInt
        } else if (num.DECIMAL != null) {
          num.DECIMAL.getText.toDouble
        } else {
          throw new IllegalStateException("could not extract value")
        }
      } else if (ctx.STRING != null) {
        val string = ctx.STRING.getText
        StringEscapeUtils.unescapeJava(string.substring(1, string.length - 1))
      } else if (ctx.NULL != null) {
        null
      } else throw new IllegalStateException("could not extract value")
    }
  }

  class ExpressionVisitor(dbObject: DBObject) extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitExpression(ctx: EventQueryParser.ExpressionContext) = {
      val fieldVisitor = new FilterVisitor

      for (filter <- ctx.filter) {
        val entry = fieldVisitor.visitFilter(filter)
        dbObject.put(entry._1, entry._2)
      }

      dbObject
    }
  }

  class FilterVisitor extends EventQueryBaseVisitor[(String, Any)] {
    override def visitFilter(ctx: EventQueryParser.FilterContext) = {
      if (ctx.fieldFilter != null) {
        val visitor = new FieldFilterVisitor
        visitor.visitFieldFilter(ctx.fieldFilter)
      } else if (ctx.logicalGroup != null) {
        val visitor = new LogicalGroupVisitor
        visitor.visitLogicalGroup(ctx.logicalGroup)
      } else throw new IllegalStateException("Encountered unexpected")
    }
  }

  class FieldFilterVisitor extends EventQueryBaseVisitor[(String, Any)] with ValueSupport {
    override def visitFieldFilter(ctx: EventQueryParser.FieldFilterContext) = {
      val name = shortener(ctx.NAME.getText)

      if (ctx.equalsComparison != null) {
        (name -> parseValue(ctx.equalsComparison.value))
      } else if (ctx.otherComparison != null) {
        val builder = new MongoDBObjectBuilder
        val visitor = new OtherComparisonVisitor

        for (otherComparison <- ctx.otherComparison) {
          val obj = visitor.visitOtherComparison(otherComparison)
          for (key <- obj.keySet) {
            builder += key -> obj.get(key)
          }
        }

        (name -> builder.result)
      } else throw new IllegalStateException("Encountered unexpected")
    }
  }

  class OtherComparisonVisitor extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitOtherComparison(ctx: EventQueryParser.OtherComparisonContext) = {
      if (ctx.simpleComparison != null) {
        val visitor = new SimpleComparisonVisitor
        visitor.visitSimpleComparison(ctx.simpleComparison)
      } else if (ctx.containsComparison != null) {
        val visitor = new ContainsComparisonVisitor
        visitor.visitContainsComparison(ctx.containsComparison)
      } else if (ctx.arrayComparison != null) {
        val visitor = new ArrayComparisonVisitor
        visitor.visitArrayComparison(ctx.arrayComparison)
      } else if (ctx.regularExpressionEvaluation != null) {
        val visitor = new RegularExpressionEvaluationVisitor
        visitor.visitRegularExpressionEvaluation(ctx.regularExpressionEvaluation)
      } else throw new IllegalStateException("Encountered unexpected")
    }
  }

  class SimpleComparisonVisitor extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitSimpleComparison(ctx: EventQueryParser.SimpleComparisonContext) = {
      val operation = new OperationVisitor().visitOperation(ctx.operation)

      MongoDBObject(operation -> parseValue(ctx.value))
    }
  }

  class OperationVisitor extends EventQueryBaseVisitor[String] {
    override def visitOperation(ctx: EventQueryParser.OperationContext) = {
      if (ctx.GE != null) {
        "$gte"
      } else if (ctx.GT != null) {
        "$gt"
      } else if (ctx.LE != null) {
        "$lte"
      } else if (ctx.LT != null) {
        "$lt"
      } else if (ctx.NE != null) {
        "$ne"
      } else throw new IllegalStateException("Encountered unexpected")
    }
  }

  class ContainsComparisonVisitor extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitContainsComparison(ctx: EventQueryParser.ContainsComparisonContext) = {
      val visitor = new ListVisitor
      val operation = ctx.containsOperation
      val operator = if (operation.IN != null) {
        "$in"
      } else if (operation.NIN != null) {
        "$nin"
      } else throw new IllegalStateException("Encountered unexpected")
      MongoDBObject(operator -> visitor.visitList(ctx.list))
    }
  }

  class ArrayComparisonVisitor extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitArrayComparison(ctx: EventQueryParser.ArrayComparisonContext) = {
      val visitor = new ListVisitor
      val operation = ctx.arrayOperation
      val operator = if (operation.ALL != null) {
        "$all"
      } else throw new IllegalStateException("Encountered unexpected")
      MongoDBObject(operator -> visitor.visitList(ctx.list))
    }
  }

  class ListVisitor extends EventQueryBaseVisitor[List[Any]] with ValueSupport {
    override def visitList(ctx: EventQueryParser.ListContext) = {
      val builder = List.newBuilder[Any]
      val visitor = new ExpressionVisitor(MongoDBObject())

      for (value <- ctx.value) {
        builder += parseValue(value)
      }

      builder.result
    }
  }

  class RegularExpressionEvaluationVisitor extends EventQueryBaseVisitor[DBObject] with ValueSupport {
    override def visitRegularExpressionEvaluation(ctx: EventQueryParser.RegularExpressionEvaluationContext) = {
      val fullRegex = ctx.REGEX.getText
      val options = StringUtils.substringAfterLast(fullRegex, "/")
      val regex = StringUtils.stripEnd(fullRegex, "/" + options).substring(1)

      if (options.length > 0) {
        MongoDBObject("$regex" -> regex, "$options" -> options)
      } else {
        MongoDBObject("$regex" -> regex)
      }
    }
  }

  class LogicalGroupVisitor extends EventQueryBaseVisitor[(String, List[DBObject])] with ValueSupport {
    override def visitLogicalGroup(ctx: EventQueryParser.LogicalGroupContext) = {
      val builder = List.newBuilder[DBObject]
      val visitor = new ExpressionVisitor(MongoDBObject())

      for (expression <- ctx.expression) {
        builder += visitor.visitExpression(expression)
      }

      val logic = if (ctx.OR != null) {
        "$or"
      } else if (ctx.NOR != null) {
        "$nor"
      } else throw new IllegalStateException("Encountered unexpected")

      (logic -> builder.result)
    }
  }
}