package io.doublegsoft.modelbase.ir;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.dataset.JoinConditionDefinition;
import com.doublegsoft.jcommons.metamodel.dataset.JoinPredicateDefinition;
import com.doublegsoft.jcommons.metamodel.dataset.QualifiedAttributeDefinition;
import com.doublegsoft.jcommons.metamodel.root.AggregateRootDefinition;
import com.doublegsoft.jcommons.metamodel.root.JoinedObjectDefinition;

public class AggregateRootBuilder {

  public AggregateRootDefinition build(ObjectDefinition root, ModelDefinition dataModel) {
    AggregateRootDefinition retVal = new AggregateRootDefinition(root);
    for (AttributeDefinition attr : root.getAttributes()) {
      QualifiedAttributeDefinition qualifiedAttr = new QualifiedAttributeDefinition(null, attr);
      retVal.getQualifiedAttributes().add(qualifiedAttr);
      JoinedObjectDefinition joinedObj = null;
      if (attr.getType().isCustom()) {
        ObjectDefinition refObj = dataModel.findObjectByName(attr.getType().getName());
        joinedObj = new JoinedObjectDefinition(refObj);
        JoinPredicateDefinition joinPredicate = new JoinPredicateDefinition();
        joinPredicate.setLeftAttribute(attr);
        joinPredicate.setLeftObject(root);
        joinPredicate.setRightAttribute(refObj.getIdentifiableAttribute());
        joinPredicate.setRightObject(refObj);
        JoinConditionDefinition joinCondition = new JoinConditionDefinition(joinPredicate);
        joinedObj.setJoinCondition(joinCondition);
      } else if (attr.getType().isCollection()) {
        CollectionType colltype = (CollectionType) attr.getType();
        ObjectDefinition refObj = dataModel.findObjectByName(colltype.getComponentType().getName());

        if (attr.isLabelled("conjunction")) {
          String conjName = attr.getLabelledOption("conjunction", "object");
          if (conjName == null) {
            conjName = attr.getLabelledOption("conjunction", "name");
          }
          ObjectDefinition conjObj = dataModel.findObjectByName(conjName);

          // root <> conj
          joinedObj = new JoinedObjectDefinition(conjObj);
          joinedObj.setCollection(true);
          joinedObj.setImplicit(true);

          JoinPredicateDefinition joinPredicate = new JoinPredicateDefinition();
          joinPredicate.setLeftObject(root);
          joinPredicate.setLeftAttribute(root.getIdentifiableAttribute());

          joinPredicate.setRightAttribute(findReferencingAttribute(conjObj, root.getName()));
          joinPredicate.setRightObject(conjObj);
          JoinConditionDefinition joinCondition = new JoinConditionDefinition(joinPredicate);
          joinedObj.setJoinCondition(joinCondition);

          // conj <> ref
          JoinedObjectDefinition joinedRefObj = new JoinedObjectDefinition(refObj);
          joinPredicate = new JoinPredicateDefinition();
          joinPredicate.setLeftObject(conjObj);
          joinPredicate.setRightAttribute(findReferencingAttribute(conjObj, refObj.getName()));
          joinPredicate.setRightObject(refObj);
          joinPredicate.setRightAttribute(refObj.getIdentifiableAttribute());

          joinCondition = new JoinConditionDefinition(joinPredicate);
          joinedRefObj.setJoinCondition(joinCondition);

          joinedObj.getChildren().add(joinedRefObj);
        } else {
          joinedObj = new JoinedObjectDefinition(refObj);
          JoinPredicateDefinition joinPredicate = new JoinPredicateDefinition();
          joinPredicate.setLeftObject(root);
          joinPredicate.setLeftAttribute(root.getIdentifiableAttribute());
          joinPredicate.setRightAttribute(findReferencingAttribute(refObj, root.getName()));
          joinPredicate.setRightObject(refObj);
          JoinConditionDefinition joinCondition = new JoinConditionDefinition(joinPredicate);
          joinedObj.setJoinCondition(joinCondition);
        }
      }
      if (joinedObj != null) {
        retVal.getJoinedObjects().add(joinedObj);
      }
    }
    return retVal;
  }

  public AggregateRootDefinition build(UsecaseDefinition usecase, ModelDefinition dataModel) {
    AggregateRootDefinition retVal = new AggregateRootDefinition(null);
    return retVal;
  }

  /**
   * 在“被引用对象（refObj）”中查找一个属性，该属性的类型指向“引用方对象（targetObjectName）”。
   *
   * <p>典型场景：</p>
   * <ul>
   *   <li>两个对象之间存在关联关系（如 A 引用 B），需要在 B 中找到那个“反向指向 A”的属性。</li>
   *   <li>用于建立双向关联、推断关系路径或构建 Join/Association 关系。</li>
   * </ul>
   *
   * <p>查找逻辑：</p>
   * <ul>
   *   <li>遍历 refObj 的所有属性（AttributeDefinition）。</li>
   *   <li>筛选类型为“自定义类型”（isCustom() == true）的属性。</li>
   *   <li>判断该属性的类型名称是否等于 referencingObjName。</li>
   *   <li>若匹配，则返回该属性。</li>
   * </ul>
   *
   * @param refObj 被引用的对象定义（Reference Object），在其属性中查找反向引用
   * @param targetObjectName 引用方对象名称（Referencing Object Name），即期望匹配的类型名
   * @return 找到的属性定义（该属性的类型指向 referencingObjName）
   *
   * @throws IllegalArgumentException 如果未找到匹配的属性，则抛出异常
   *
   * <p>注意：</p>
   * <ul>
   *   <li>仅匹配“自定义类型”的属性，基础类型（如 int/string）会被忽略。</li>
   *   <li>匹配依据为类型名称字符串相等，需保证命名一致。</li>
   * </ul>
   */
  private AttributeDefinition findReferencingAttribute(ObjectDefinition refObj,
                                                       String targetObjectName) {
    for (AttributeDefinition refObjAttr : refObj.getAttributes()) {
      if (refObjAttr.getType().isCustom() && refObjAttr.getType().getName().equals(targetObjectName)) {
        return refObjAttr;
      }
    }
    throw new IllegalArgumentException("not found referencing object named \""
        + targetObjectName + " in \"" + refObj.getName() + "\" object");
  }

}
