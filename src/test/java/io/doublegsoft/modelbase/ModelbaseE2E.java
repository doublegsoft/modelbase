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

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.BehaviorDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.CustomType;
import java.io.File;
import java.nio.file.Files;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * It is an end-to-end test for modelbase.
 *
 * @author <a href="mailto:guo.guo.gan@gmail.com">Christian Gann</a>
 *
 * @since 1.0
 */
public class ModelbaseE2E {

  @Ignore
  public void test_taobao_ddd() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../modelbase-testdata/e2e/ddd-taobao").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);

    ObjectDefinition order = model.findObjectByPersistenceName("order");
    Assert.assertNotNull(order);
    Assert.assertTrue(order.isLabelled("entity"));

    AttributeDefinition orderItems = model.findAttributeByPersistenceNames("order", "order_items");
    CollectionType colltype = (CollectionType) orderItems.getType();
    CustomType customtype = (CustomType) colltype.getComponentType();
    Assert.assertEquals("order_item", customtype.getName());
    System.out.println(customtype.getObjectDefinition());

    ObjectDefinition cart = model.findObjectByPersistenceName("cart");
    Assert.assertEquals(1, cart.getBehaviors().length);

    BehaviorDefinition orderInCart = cart.getBehaviors()[0];
    System.out.println(orderInCart.getParameters()[0].getType());

    ObjectDefinition inventory = model.findObjectByPersistenceName("inventory");
    Assert.assertEquals(2, inventory.getBehaviors().length);

    BehaviorDefinition increaseAmount = inventory.getBehaviors()[0];
    Assert.assertEquals("increaseAmount", increaseAmount.getName());
    Assert.assertEquals(1, increaseAmount.getParameters().length);

    AttributeDefinition countParameterIncreaseAmount = increaseAmount.getParameters()[0];
    Assert.assertEquals("count", countParameterIncreaseAmount.getName());

    System.out.println(increaseAmount.getBody());
  }

}
