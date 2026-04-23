package io.doublegsoft.modelbase.ir;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.dataset.*;
import io.doublegsoft.modelbase.Modelbase;
import io.doublegsoft.modelbase.ModelbaseTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class CompositeRowBuilderTest extends ModelbaseTestBase {

  @Test
  public void test_match() throws Exception {
    String expr = new String(Files.readAllBytes(
        new File("src/test/resources/IR/match.modelbase").toPath()), "UTF-8");
    ModelDefinition dataModel = new Modelbase().parse(expr);
    ObjectDefinition matchObj = dataModel.findObjectByName("match");
    CompositeRowBuilder compositeRowBuilder = new CompositeRowBuilder(dataModel);
    CompositeRowDefinition compositeRow = compositeRowBuilder.buildCompositeRow(matchObj);
    List<QualifiedObjectDefinition> qualObjs = compositeRow.getQualifiedObjects();
    Assert.assertEquals(3, qualObjs.size());
    for (QualifiedAttributeDefinition qualAttr : compositeRow.getQualifiedAttributes()) {
      System.out.println(qualAttr.getAlias() + "." + qualAttr.getSnakeCaseQualifiedName(dataModel));
    }
  }

  @Test
  public void test_graph() throws Exception {
    String expr = new String(Files.readAllBytes(
        new File("src/test/resources/IR/graph.modelbase").toPath()), "UTF-8");
    ModelDefinition dataModel = new Modelbase().parse(expr);
    ObjectDefinition matchObj = dataModel.findObjectByName("connection");
    CompositeRowBuilder compositeRowBuilder = new CompositeRowBuilder(dataModel);
    CompositeRowDefinition compositeRow = compositeRowBuilder.buildCompositeRow(matchObj);
    List<QualifiedObjectDefinition> qualObjs = compositeRow.getQualifiedObjects();
    Assert.assertEquals(4, qualObjs.size());
    for (QualifiedAttributeDefinition qualAttr : compositeRow.getQualifiedAttributes()) {
      System.out.println(qualAttr.getAlias() + "." + qualAttr.getSnakeCaseQualifiedName(dataModel));
    }
  }

  @Test
  public void test_depth3() throws Exception {
    String expr = new String(Files.readAllBytes(
        new File("src/test/resources/IR/depth-3.modelbase").toPath()), "UTF-8");
    ModelDefinition dataModel = new Modelbase().parse(expr);
    ObjectDefinition matchObj = dataModel.findObjectByName("service");
    CompositeRowBuilder compositeRowBuilder = new CompositeRowBuilder(dataModel);
    CompositeRowDefinition compositeRow = compositeRowBuilder.buildCompositeRow(matchObj);
    List<QualifiedObjectDefinition> qualObjs = compositeRow.getQualifiedObjects();
    Assert.assertEquals(4, qualObjs.size());
    System.out.println("select: ");
    for (QualifiedAttributeDefinition qualAttr : compositeRow.getQualifiedAttributes()) {
      System.out.println("  " + qualAttr.getAlias() + "." + qualAttr.getSnakeCaseQualifiedName(dataModel));
    }
    System.out.println("left join: ");
    for (QualifiedAttributeDefinition[] pair : compositeRow.getPairedQualifiedAttributes()) {
      System.out.println("  " + pair[0].getAlias() + "." + pair[0].getSnakeCaseQualifiedName(dataModel) +
          " = " + pair[1].getAlias() + "." + pair[1].getSnakeCaseQualifiedName(dataModel));
    }
  }

}
