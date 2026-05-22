package io.doublegsoft.modelbase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class ModelbaseTestV7 extends ModelbaseTestBase {

  @Test
  public void test_enum() throws Exception {
    String expr = new String(Files.readAllBytes(
        new File("/Users/christian/export/local/works/doublegsoft.io/modelbase/03.Development/modelbase-test/spec/extension+details.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
    System.out.println("hello");
  }

}
