/*
 * DOUBLEGSOFT.IO CONFIDENTIAL
 *
 * Copyright (C) doublegsoft.io
 *
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of doublegsoft.io and its suppliers, if any.
 * The intellectual and technical concepts contained herein
 * are proprietary to doublegsoft.io and its suppliers  and
 * may be covered by China and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from doublegsoft.io.
 */
package io.doublegsoft.modelbase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.BehaviorDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.RelationshipStyle;
import com.doublegsoft.jcommons.metabean.ast.*;
import com.doublegsoft.jcommons.metabean.type.*;
import io.doublegsoft.modelbase.ModelbaseParser.Anybase_stringContext;
import io.doublegsoft.modelbase.ModelbaseParser.Modelbase_typesContext;
import io.doublegsoft.modelbase.ModelbaseParser.Typebase_anytypeContext;
import io.doublegsoft.modelbase.ModelbaseParser;
import io.doublegsoft.modelbase.ModelbaseParser.*;
import java.util.HashMap;
import java.util.Map;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.Interval;

/**
 * It is the modelbase language parser and converts to other model contexts.
 *
 * @author <a href="mailto:guo.guo.gan@gmail.com">Christian Gann</a>
 *
 * @since 1.0
 */
public class Modelbase extends BaseErrorListener {

  private final Map<AttributeDefinition, Object[]> precollectedCustomTypes = new HashMap<>();
  
  private transient CharStream input;

  public ModelDefinition parse(String expr) {
    ModelDefinition retVal = new ModelDefinition();
    Modelbase_typesContext ctx = parse0(expr);
    assembleFromContext(ctx, retVal);
    setCustomTypesAndRelationships(retVal);
    return retVal;
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    System.err.println("line: '" + line + "': " + charPositionInLine + " " + msg);
  }

  private Modelbase_typesContext parse0(String expr) {
    this.input = CharStreams.fromString(expr);
    io.doublegsoft.modelbase.ModelbaseLexer lexer = new io.doublegsoft.modelbase.ModelbaseLexer(this.input);
    lexer.addErrorListener(this);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.modelbase.ModelbaseParser parser = new io.doublegsoft.modelbase.ModelbaseParser(tokens);
    parser.addErrorListener(this);
    // parser.setErrorHandler(new BailErrorStrategy());
    io.doublegsoft.modelbase.ModelbaseParser.Modelbase_typesContext retVal = parser.modelbase_types();
    return retVal;
  }
  
  /**
   * Assembles model definition from antlr4 context.
   * 
   * @param ctx
   *        the antlr4 context
   * 
   * @param model 
   *        the model definition
   * 
   * @version 3.1 added id syntax support for label options on Apr 9, 2019
   */
  private void assembleFromContext(Modelbase_typesContext ctx, ModelDefinition model) {
    ctx.modelbase_type().stream().forEach((ctxType) -> {
      String typename = null;
      String typealias = null;
      String typedescr = null;
      String persistenceName = null;
            
      if (ctxType.name != null) {
        typename = ctxType.name.getText();
      }
      if (ctxType.alias != null) {
        typealias = ctxType.alias.getText();
      }
      if (ctxType.descr != null) {
        typedescr = ctxType.descr.getText();
      }
      
      final ObjectDefinition obj = new ObjectDefinition(typename, model);
      int start = ctxType.start.getStartIndex();
      int stop = ctxType.stop.getStopIndex();
      Interval interval = new Interval(start, stop);
      obj.setScript(this.input.getText(interval));
      obj.setText(typedescr);
      obj.setAlias(typealias);
      if (ctxType.modelbase_labels() != null) {
        ctxType.modelbase_labels().modelbase_label().forEach(ctxLabel -> {
          String label = ctxLabel.name.getText();
          Map<String, String> options = new HashMap<>();
          if (ctxLabel.id != null) {
            options.put("id", ctxLabel.id.getText());
          }
          ctxLabel.modelbase_option().forEach(opt -> {
            options.put(opt.modelbase_name().getText(), string(opt.anybase_string()));
          });
          obj.setLabelledOptions(label, options);
        });
      }
      if (obj.isLabelled("persistence")) {
        persistenceName = obj.getLabelledOptions("persistence").get("name");
        obj.setPersistenceName(persistenceName);
      }
      if (obj.isLabelled("name")) {
        obj.setSingular(obj.getLabelledOptions("name").get("singular"));
        obj.setPlural(obj.getLabelledOptions("name").get("plural"));
      }
      if (obj.isLabelled("comment")) {
        obj.setText(obj.getLabelledOptions("comment").get("text"));
      }
      if (persistenceName == null) {
        persistenceName = typealias == null ? typename : typealias;
      }
      
      if (ctxType.modelbase_attrs() != null) {
        ctxType.modelbase_attrs().modelbase_attr().stream().forEach((ctxAttr) -> {
          String attrname = null;
          String attralias = null;
          String attrdescr = null;
          boolean array = false;
          String decorator = "";
          if (ctxAttr.name != null) {
            attrname = ctxAttr.name.getText();
          }
          if (ctxAttr.alias != null) {
            attralias = ctxAttr.alias.getText();
          }
          if (ctxAttr.descr != null) {
            attrdescr = ctxAttr.descr.getText();
          }
          if (ctxAttr.decorator != null) {
            decorator = ctxAttr.decorator.getText();
          }
          AttributeDefinition attr = new AttributeDefinition(attrname, obj);
          // the persistence name could be null, if not persist it
          // attr.setPersistenceName(attrname);
          attr.setText(attrdescr);
          attr.setAlias(attralias);
          if (ctxAttr.dflt != null) {
            // the default value is a quoted string
            attr.getConstraint().setDefaultValue(ctxAttr.dflt.getText());
          }
          if (ctxAttr.required != null) {
            attr.getConstraint().setNullable(false);
          }
          if (ctxAttr.identifiable != null) {
            attr.getConstraint().setIdentifiable(true);
          }
          if (ctxAttr.unique != null) {
            attr.getConstraint().setUnique(true);
          }
          if (ctxAttr.modelbase_labels() != null) {
            ctxAttr.modelbase_labels().modelbase_label().forEach(ctxLabel -> {
              String label = ctxLabel.name.getText();
              Map<String, String> options = new HashMap<>();
              if (ctxLabel.id != null) {
                options.put("id", ctxLabel.id.getText());
              }
              ctxLabel.modelbase_option().forEach(opt -> {
                options.put(opt.modelbase_name().getText(), string(opt.anybase_string()));
              });
              attr.setLabelledOptions(label, options);
            });
          }
          if (ctxAttr.typebase_anytype() != null) {
            // not relationship
            setObjectTypeAndConstraint(model, attr, ctxAttr.typebase_anytype(), decorator, false);
            if (ctxAttr.innerArray != null) {
              CollectionType collType = new CollectionType("");
              ObjectType objectType = attr.getType();
              collType.setComponentType(objectType);
              attr.setType(collType);
              if (ctxAttr.counted_name != null) {
                collType.setCountedName(ctxAttr.counted_name.getText());
              }
            }
          }
          if (attr.isLabelled("system")) {
            attr.getConstraint().setSystem(true);
          }
          if (attr.isLabelled("persistence")) {
            attr.setPersistenceName(attr.getLabelledOptions("persistence").get("name"));
          }
          if (attr.isLabelled("comment")) {
            attr.setText(attr.getLabelledOptions("comment").get("text"));
          }
        });
      }
      if (ctxType.modelbase_behaviors() != null) {
        ctxType.modelbase_behaviors().modelbase_behavior().forEach(ctxBx -> {
          BehaviorDefinition bx = new BehaviorDefinition(ctxBx.name.getText(), obj);
          if (ctxBx.modelbase_labels() != null) {
            ctxBx.modelbase_labels().modelbase_label().forEach(ctxLabel -> {
              String label = ctxLabel.name.getText();
              Map<String, String> options = new HashMap<>();
              ctxLabel.modelbase_option().forEach(opt -> {
                options.put(opt.modelbase_name().getText(), string(opt.anybase_string()));
              });
              bx.setLabelledOptions(label, options);
            });
          }
          if (bx.isLabelled("comment")) {
            bx.setText(bx.getLabelledOptions("comment").get("text"));
          }
          // parameters
          if (ctxBx.params != null) {
            ctxBx.params.modelbase_attr().forEach(ctxParam -> {
              AttributeDefinition param = new AttributeDefinition(ctxParam.name.getText(), bx);
              if (ctxParam.typebase_anytype() != null) {
                setObjectTypeAndConstraint(model, param, ctxParam.typebase_anytype(), "", true);
              }
              bx.addParameter(param);
            });
          }
          // method body
          if (ctxBx.body != null) {
            Modelbase_body_statementsContext ctxStatements = ctxBx.body.modelbase_body_statements();
            for (Modelbase_body_statementContext ctxStmt : ctxStatements.modelbase_body_statement()) {
              bx.addStatement(statement(ctxStmt));
            }
          }
          if (ctxBx.modelbase_labels() != null) {
            ctxBx.modelbase_labels().modelbase_label().forEach(ctxLabel -> {
              String label = ctxLabel.name.getText();
              Map<String, String> options = new HashMap<>();
              ctxLabel.modelbase_option().forEach(opt -> {
                options.put(opt.modelbase_name().getText(), string(opt.anybase_string()));
              });
              bx.setLabelledOptions(label, options);
            });
          }
          if (ctxBx.ret != null) {
            AttributeDefinition ret = new AttributeDefinition("ret", bx);
            setObjectTypeAndConstraint(model, ret, ctxBx.ret, "", true);
            bx.setReturnValue(ret);
          }
          if (ctxBx.body != null) {
            bx.setBody(ctxBx.body.getText().substring(1, ctxBx.body.getText().length() - 1));
          }
        });
      }
    });
  }
  
  /**
   * Sets the attribute type and constraint with typebase context and collects custom types defined in model.
   * 
   * @param model
   *      the model definition
   * 
   * @param attr
   *      the attribute definition
   * 
   * @param ctx
   *      the typebase context
   * 
   * @param decorator
   *      the decorator like required etc.
   * 
   * @param isProcessingBx 
   *      whether to process behaviors
   */
  private void setObjectTypeAndConstraint(ModelDefinition model, AttributeDefinition attr, Typebase_anytypeContext ctx, String decorator, boolean isProcessingBx) {
    if (decorator.contains("!!")) {
      attr.getConstraint().setIdentifiable(true);
    }
    if (decorator.contains("!")) {
      attr.getConstraint().setNullable(false);
    }
    boolean array = ctx.array != null;
    if (ctx.reftype != null) {
      if (!isProcessingBx) {
        precollectedCustomTypes.put(attr, new Object[]{ctx, array});
      } else {
        DomainType domainType = new DomainType(ctx.getText());
        domainType.setArray(array);
        domainType.setName(ctx.reftype.getText());
        attr.setType(domainType);
      }
      // see #setCustomTypes
    } else if (ctx.typebase_string() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      attr.setType(type);
      if (ctx.typebase_string().anybase_int() != null) {
        type.setLength(Integer.valueOf(ctx.typebase_string().anybase_int().getText()));
        attr.getConstraint().setMaxSize(Integer.valueOf(ctx.typebase_string().anybase_int().getText()));
      }
      if (ctx.typebase_string().length_name != null) {
        type.setLengthName(ctx.typebase_string().length_name.getText());
      }
    } else if (ctx.typebase_code() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      attr.setType(type);
      if (ctx.typebase_code().anybase_int() != null) {
        type.setLength(Integer.valueOf(ctx.typebase_code().anybase_int().getText()));
        attr.getConstraint().setMaxSize(Integer.valueOf(ctx.typebase_code().anybase_int().getText()));
      }
    } else if (ctx.typebase_id() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(64);
      attr.setType(type);
    } else if (ctx.typebase_name() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(100);
      attr.setType(type);
    } else if (ctx.typebase_email() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(60);
      attr.setType(type);
    } else if (ctx.typebase_phone() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(60);
      attr.setType(type);
    } else if (ctx.typebase_mobile() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(60);
      attr.setType(type);
    } else if (ctx.typebase_address() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(400);
      attr.setType(type);
    } else if (ctx.typebase_bool() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.BOOL);
      type.setLength(1);
      attr.setType(type);
    } else if (ctx.typebase_text() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      attr.setType(type);
    } else if (ctx.typebase_password() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      type.setLength(32);
      attr.setType(type);
    } else if (ctx.typebase_number() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.NUMBER);
      attr.setType(type);
      if (ctx.typebase_number().anybase_int().size() > 0) {
        Integer size = Integer.valueOf(ctx.typebase_number().anybase_int(0).getText());
        type.setPrecision(size);
        attr.getConstraint().setMaxSize(size);
        try {
          Integer scale = Integer.valueOf(ctx.typebase_number().anybase_int(1).getText());
          type.setScale(scale);
          attr.getConstraint().setScale(scale);
        } catch (Exception ex) {
        }
      }
    } else if (ctx.typebase_now() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.DATETIME);
      attr.setType(type);
      attr.getConstraint().setDomainType(new DomainType("now"));
      attr.getConstraint().setSystem(true);
    } else if (ctx.typebase_enum() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      attr.setType(type);
      ctx.typebase_enum().typebase_keytext().forEach(kt -> {
//        String key = kt.key1 == null ? kt.key2.getText() : kt.key1.getText();
        String key = kt.anybase_key().getText();
        if (kt.text == null) {
          attr.getConstraint().addCheckValue(new String[]{key, kt.name.getText()});
        } else {
          int len = kt.text.getText().length();
          attr.getConstraint().addCheckValue(new String[]{key, kt.name.getText(), kt.text.getText().substring(1, len - 1)});
        }
        type.setLength(Math.max(type.getLength(), key.length()));
      });
    } else if (ctx.typebase_state() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.STRING);
      attr.setType(type);
      type.setLength(1);
      attr.getConstraint().setDomainType(new DomainType("state"));
//      ctx.typebase_state().typebase_keytext().forEach(kt -> {
//        attr.getConstraint().addCheckValue(new String[]{kt.key.getText(), kt.text.getText()});
//      });
      attr.getConstraint().setSystem(true);
    } else if (ctx.typebase_datetime() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.DATETIME);
      attr.setType(type);
    } else if (ctx.typebase_date() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.DATE);
      attr.setType(type);
    } else if (ctx.typebase_time() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.TIME);
      attr.setType(type);
    } else if (ctx.typebase_int() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.INTEGER);
      attr.setType(type);
      if (ctx.typebase_int().length != null) {
        type.setLength(Integer.valueOf(ctx.typebase_int().length.getText()));
      }
    } else if (ctx.typebase_byte() != null) {
      PrimitiveType type = new PrimitiveType("byte");
      attr.setType(type);
      if (ctx.typebase_byte().length != null) {
        type.setLength(Integer.valueOf(ctx.typebase_byte().length.getText()));
      }
    } else if (ctx.typebase_long() != null) {
      PrimitiveType type = new PrimitiveType(PrimitiveType.LONG);
      attr.setType(type);
    } else if (ctx.typebase_anonymous_object() != null) {
      AnonymousType objAsType = new AnonymousType("anonymous");
      ctx.typebase_anonymous_object().typebase_attrdecl().stream().forEach((ctxAttr) -> {
        AttributeDefinition attrInAnonymous = new AttributeDefinition(ctxAttr.typebase_anybase_id(0).getText(), objAsType);
        setObjectTypeAndConstraint(model, attrInAnonymous, ctxAttr.typebase_anytype(), "", isProcessingBx);
      });
    } else if (ctx.typebase_native() != null) {
      attr.setType(new ConstantType(ctx.typebase_native().getText()));
      attr.getConstraint().setDomainType(new DomainType(ctx.getText()));
    } else if (ctx.typebase_version() != null) {
      attr.setType(new ConstantType(ctx.typebase_version().getText()));
      attr.getConstraint().setDomainType(new DomainType(ctx.getText()));
    } else {
      attr.setType(new DomainType(ctx.getText()));
      attr.getConstraint().setDomainType(new DomainType(ctx.getText()));
    }
    
    // 
    // set collection type if array flag is found
    // 
    if (array) {
      ObjectType compType = attr.getType();
      CollectionType collType = new CollectionType("");
      collType.setComponentType(compType);
      attr.setType(collType);
    }
    
    //
    // domain type is never null
    //
    DomainType dt = new DomainType(ctx.getText());
    dt.setArray(array);
    attr.getConstraint().setDomainType(dt);
  }
  
  /**
   * Sets custom type for the attributes of object defined in model.
   * <p>
   * Calls it after calling {@link #precollectedCustomTypes}.
   * 
   * @param model 
   *      the model definition
   * 
   * @version 3.0.1 - removed one-to-one relationship, it is non-sense
   */
  private void setCustomTypesAndRelationships(ModelDefinition model) {
    precollectedCustomTypes.entrySet().forEach(entry -> {
      AttributeDefinition attr = entry.getKey();
      Typebase_anytypeContext ctx = (Typebase_anytypeContext) entry.getValue()[0];
      boolean array = (boolean) entry.getValue()[1];

      ObjectDefinition refObj = model.findObjectByName(ctx.reftype.getText());
      if (refObj == null) {
        throw new NullPointerException(attr.getParent().getName() + "[" + attr.getName() + "]: " + ctx.reftype.getText() + " object not found in model");
      }
      CustomType customType = new CustomType(ctx.reftype.getText(), refObj);

//      attr.addRelationship(refObj, RelationshipStyle.ONE_TO_ONE);

      if (ctx.typebase_anybase_id() != null && ctx.typebase_anybase_id().size() > 0) {
        attr.addRelationship(model.findAttributeByNames(refObj.getName(), ctx.typebase_anybase_id(0).getText()), RelationshipStyle.MANY_TO_ONE);
      } else {
        attr.addRelationship(refObj, RelationshipStyle.MANY_TO_ONE);
      }
      if (array) {
        CollectionType collType = new CollectionType("list");
        collType.setComponentType(customType);
        attr.setType(collType);
        return;
      }
      attr.setType(customType);
    });
  }

  private String string(Anybase_stringContext ctx) {
    String text = ctx.getText();
    return text.substring(1, text.length() - 1);
  }

  private Statement statement(Modelbase_body_statementContext ctx) {
    if (ctx.modelbase_body_assignment() != null) {
      Modelbase_body_assignmentContext ctxAssign = ctx.modelbase_body_assignment();
      Assignment assignment = new Assignment();
      assignment.assignee(new Identifier(ctxAssign.assignee.getText()));
      assignment.operator(ctxAssign.modelbase_body_operators().getText());
      if (ctxAssign.operand != null) {
        Identifier value = new Identifier(ctxAssign.operand.getText());
        assignment.operand(value);
      } else if (ctxAssign.anybase_value() != null) {
        Value value = new Value(ctxAssign.anybase_value().getText());
        assignment.operand(value);
      } else if (ctxAssign.modelbase_body_invocation() != null) {
        assignment.operand(invocation(ctxAssign.modelbase_body_invocation()));
      }
      return assignment;
    } else if (ctx.modelbase_body_invocation() != null) {
      return invocation(ctx.modelbase_body_invocation());
    } else if (ctx.modelbase_body_validation() != null) {
      return validation(ctx.modelbase_body_validation());
    } else if (ctx.modelbase_body_if() != null) {
      return $if(ctx.modelbase_body_if());
    } else if (ctx.modelbase_body_loop() != null) {
      return loop(ctx.modelbase_body_loop());
    }
    return null;
  }

  private Invocation invocation(Modelbase_body_invocationContext ctx) {
    Invocation retVal = new Invocation();
    retVal.action(ctx.action.getText());
    if (ctx.constant != null) {
      retVal.location(new Value(ctx.constant.getText()));
    } else if (ctx.variable != null) {
      retVal.location(new Identifier(ctx.variable.getText()));
    }
    if (ctx.modelbase_body_arguments() != null) {
      for (Modelbase_body_argumentContext ctxArg : ctx.modelbase_body_arguments().modelbase_body_argument()) {
        Argument arg = new Argument();
        arg.name(new Identifier(ctxArg.name.getText()));
        if (ctxArg.anybase_value() != null) {
          arg.value(new Value(ctxArg.anybase_value().getText()));
        }
        retVal.argument(arg);
      }
    }

    if (ctx.format != null) {
      retVal.format(ctx.format.getText());
    }
    return retVal;
  }

  private Validation validation(Modelbase_body_validationContext ctx) {
    int a = ctx.start.getStartIndex();
    int b = ctx.stop.getStopIndex();
    Interval interval = new Interval(a,b);
    Validation retVal = new Validation(input.getText(interval));

    retVal.variable(new Identifier(ctx.anybase_identifier().getText()));
    if (ctx.modelbase_required() != null) {
      retVal.required(true);
    }
    if (ctx.modelbase_unique() != null) {
      retVal.unique(true);
    }
    if (ctx.format != null) {
      retVal.format(ctx.format.getText().substring(1, ctx.format.getText().length() - 1));
    }
    retVal.message(ctx.comment.getText().substring(1, ctx.comment.getText().length() - 1));
    return retVal;
  }

  private If $if(Modelbase_body_ifContext ctxIf) {
    If retVal = new If();
    // if
    Comparison comparison = new Comparison();
    comparison.left(new Identifier(ctxIf.if_cmp.anybase_identifier().getText()));
    if (ctxIf.if_cmp.anybase_value().anybase_identifier() != null) {
      comparison.right(new Identifier(ctxIf.if_cmp.anybase_value().anybase_identifier().getText()));
    } else {
      comparison.right(new Value(ctxIf.if_cmp.anybase_value().getText()));
    }
    comparison.comparator(ctxIf.if_cmp.modelbase_body_comparators().getText());
    retVal.comparison(comparison);
    for (Modelbase_body_statementContext ctxStmt : ctxIf.if_stmt.modelbase_body_statements().modelbase_body_statement()) {
      retVal.statement(statement(ctxStmt));
    }
    if (ctxIf.else_stmt != null) {
      If $else = new If();;
      for (Modelbase_body_statementContext ctxStmt : ctxIf.else_stmt.modelbase_body_statements().modelbase_body_statement()) {
        $else.statement(statement(ctxStmt));
      }
      retVal.$else($else);
    }
    return retVal;
  }

  private Loop loop(Modelbase_body_loopContext ctxLoop) {
    Loop retVal = new Loop();
    if (ctxLoop.lower != null) {
      if (ctxLoop.lower.anybase_identifier() != null) {
        retVal.lower(new Identifier(ctxLoop.lower.anybase_identifier().getText()));
      } else {
        retVal.lower(new Value(ctxLoop.lower.getText()));
      }
    }
    if (ctxLoop.upper.anybase_identifier() != null) {
      retVal.upper(new Identifier(ctxLoop.upper.anybase_identifier().getText()));
    } else {
      retVal.upper(new Value(ctxLoop.upper.getText()));
    }
    for (Modelbase_body_statementContext ctxStmt : ctxLoop.modelbase_body().modelbase_body_statements().modelbase_body_statement()) {
      retVal.statement(statement(ctxStmt));
    }
    return retVal;
  }

}
