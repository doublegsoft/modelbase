package io.doublegsoft.modelbase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class ModelbaseTestV7 extends ModelbaseTestBase {

  @Test
  public void test_enum() throws Exception {
    String expr = new String(Files.readAllBytes(
        new File("src/test/resources/V7/enum.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }

}
