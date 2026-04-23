package io.doublegsoft.modelbase.ir;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.dataset.*;

import java.util.ArrayList;
import java.util.List;

public class CompositeRowBuilder {

  private ModelDefinition dataModel;

  public CompositeRowBuilder(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public CompositeRowDefinition buildCompositeRow(ObjectDefinition root) {
    CompositeRowDefinition retVal = new CompositeRowDefinition();
    List<QualifiedAttributeDefinition> qualAttrs = buildQualifiedAttributes(retVal, "", root);
    qualAttrs.stream().forEach(retVal::addQualifiedAttribute);
    return retVal;
  }

  private List<QualifiedAttributeDefinition> buildQualifiedAttributes(CompositeRowDefinition compositeRow,
      String alias, ObjectDefinition dataObj) {
    List<QualifiedAttributeDefinition> retVal = new ArrayList<>();
    // 引用了对象的属性的属性名称作为此引用对象的别名
    for (AttributeDefinition attr : dataObj.getAttributes()) {
      if (attr.getType().isCustom() && attr.isLabelled("eager")) {
        continue;
      }
      QualifiedAttributeDefinition qualAttr = new QualifiedAttributeDefinition(alias, attr);
      retVal.add(qualAttr);
    }
    for (AttributeDefinition attr : dataObj.getAttributes()) {
      if (attr.getType().isCustom() && attr.isLabelled("eager")) {
        ObjectDefinition referencedObjByAttr = dataModel.findObjectByName(attr.getType().getName());
        // 创建关联关系
        QualifiedAttributeDefinition dummyAttr = new QualifiedAttributeDefinition(alias, attr);
        compositeRow.addPairedQualifiedAttributes(
            dummyAttr,
            new QualifiedAttributeDefinition(attr.getName(), referencedObjByAttr.getIdentifiableAttribute())
        );
      }
    }
    for (AttributeDefinition attr : dataObj.getAttributes()) {
      if (attr.getType().isCustom() && attr.isLabelled("eager")) {
        ObjectDefinition referencedObjByAttr = dataModel.findObjectByName(attr.getType().getName());
        List<QualifiedAttributeDefinition> children = buildQualifiedAttributes(compositeRow, attr.getName(), referencedObjByAttr);
        retVal.addAll(children);
      }
    }
    return retVal;
  }

}
