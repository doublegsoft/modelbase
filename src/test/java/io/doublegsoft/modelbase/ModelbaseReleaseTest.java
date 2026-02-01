/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.doublegsoft.modelbase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import java.io.File;
import java.nio.file.Files;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gg
 */
public class ModelbaseReleaseTest {
  
  @Ignore
  public void test_modelbase_release_base_person() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../../06.Release/基本信息模型/个人基本信息模型.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }
  
  @Ignore
  public void test_modelbase_release_base_user() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../../06.Release/基本信息模型/用户基本信息模型.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }
  
  @Ignore
  public void test_modelbase_release_base_equipment() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../../06.Release/基本信息模型/设备基本信息模型.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }
  
  @Ignore
  public void test_modelbase_release_base_organization() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../../06.Release/基本信息模型/组织机构信息模型.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }
  
  @Ignore
  public void test_modelbase_release_architecture_springboot() throws Exception {
    String expr = new String(Files.readAllBytes(new File("../../06.Release/架构应用模型/SpringBoot预定义模型.modelbase").toPath()), "UTF-8");
    ModelDefinition model = new Modelbase().parse(expr);
  }
  
}
