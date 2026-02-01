/*
 * DOUBLEGSOFT.COM CONFIDENTIAL
 *
 * Copyright (c) doublegsoft.com
 *
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of doublegsoft.com and its suppliers, if any.
 * The intellectual and technical concepts contained herein
 * are proprietary to doublegsoft.com and its suppliers  and
 * may be covered by China and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from doublegsoft.com.
 */
package io.doublegsoft.modelbase;

import com.doublegsoft.jcommons.metabean.BehaviorDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.ast.*;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import org.junit.Assert;
import org.junit.Test;

/**
 * It is an unit test for modelbase.
 *
 * @author <a href="mailto:guo.guo.gan@gmail.com">Christian Gann</a>
 *
 * @since 1.0
 */
public class ModelbaseTestV6 extends ModelbaseTestBase {

  @Test
  public void test_assignment() throws Exception {
    String expr = getText("/v6/assigment");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition obj = model.getObjects()[0];
    BehaviorDefinition bx = obj.getBehaviors()[0];
    Assert.assertEquals("calculate", bx.getName());

    /*!
    ** amount += 100
    */
    Assignment assign = (Assignment) bx.getStatements()[0];
    Assert.assertEquals(new Identifier("amount"), assign.assignee());
    Assert.assertEquals(Operator.ADDITION, assign.operator());
    Assert.assertEquals("100", assign.operand().constant());
    Assert.assertEquals(new PrimitiveType(PrimitiveType.NUMBER), assign.operand().type());

    /*!
    ** status = 'A'
    */
    assign = (Assignment) bx.getStatements()[1];
    Assert.assertEquals(new Identifier("status"), assign.assignee());
    Assert.assertEquals(Operator.ASSIGNMENT, assign.operator());
    Assert.assertEquals("A", assign.operand().constant());
    Assert.assertEquals(new PrimitiveType(PrimitiveType.STRING), assign.operand().type());

    /*!
    ** params.value = 200.2
    */
    assign = (Assignment) bx.getStatements()[3];
    Assert.assertEquals(new Identifier("params.value"), assign.assignee());
    Assert.assertEquals(Operator.ASSIGNMENT, assign.operator());
    Assert.assertEquals("200.2", assign.operand().constant());
    Assert.assertEquals(new PrimitiveType(PrimitiveType.NUMBER), assign.operand().type());

    /*!
    ** params.value += amount
    */
    assign = (Assignment) bx.getStatements()[4];
    Assert.assertEquals(new Identifier("params.value"), assign.assignee());
    Assert.assertEquals(Operator.ADDITION, assign.operator());
    Assert.assertEquals(new Identifier("amount"), assign.operand());

    /*!
    ** save@url (params)
    */
    Invocation invoc = (Invocation) bx.getStatements()[5];
    Assert.assertEquals("save", invoc.action());

    bx = obj.getBehaviors()[2];
    assign = (Assignment) bx.getStatements()[2];
    Assert.assertEquals(new Identifier("items"), assign.assignee());
    Assert.assertEquals(Operator.ASSIGNMENT, assign.operator());
    Assert.assertEquals(Invocation.class, assign.operand().getClass());
    invoc = (Invocation)assign.operand();
    Assert.assertEquals("fetch", invoc.action());
    Assert.assertEquals(new Identifier("url"), invoc.location());
  }

  @Test
  public void test_validation() throws Exception {
    String expr = getText("/v6/validation");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition obj = model.getObjects()[0];
    BehaviorDefinition bx = obj.getBehaviors()[0];

  }

  @Test
  public void test_if() throws Exception {
    String expr = getText("/v6/if");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition obj = model.getObjects()[0];
    BehaviorDefinition bx = obj.getBehaviors()[0];

    If $if = (If)bx.getStatements()[0];
    Comparison cmp = $if.comparison();
    Assert.assertEquals(new Identifier("a"), cmp.left());
    Assert.assertEquals(Comparator.NOT_EQUAL, cmp.comparator());
    Assert.assertEquals("A", cmp.right().constant());
    Assert.assertEquals(1, $if.statements().length);
    Assert.assertNotNull($if.$else());
  }

  @Test
  public void test_loop() throws Exception {
    String expr = getText("/v6/loop");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition obj = model.getObjects()[0];
    BehaviorDefinition bx = obj.getBehaviors()[0];

    Loop loop = (Loop)bx.getStatements()[0];
    Assert.assertEquals("0", loop.lower().constant());
    Assert.assertEquals(new Identifier("items"), loop.upper());
    Assert.assertEquals(2, loop.statements().length);
  }

  @Test
  public void test_application_service() throws Exception {
    String expr = getText("/v6/application_service");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition obj = model.getObjects()[0];
    BehaviorDefinition bx = obj.getBehaviors()[0];

    Validation vali = (Validation)bx.getStatements()[0];
    Assert.assertEquals("订单标识", vali.message());
    Assert.assertTrue(vali.required());
  }

}
