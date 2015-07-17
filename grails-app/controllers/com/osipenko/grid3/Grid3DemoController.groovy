package com.osipenko.grid3

import com.osipenko.grid3.build.hql.HqlGrid3Service
import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.view.Grid3View

/**
 * Created by osa on 08.09.2014.
 */
class Grid3DemoController {
    def hqlGrid3Service
    def simpleGrid3ViewService
    def index(){
        Grid3 grid3 = hqlGrid3Service.buildGrid3("""
<hqlXml>
  <columns>
    <column path="car.productId"/>
    <column path="shortDesc.value" />
    <column path="carStatus.statusId" />
    <column path="engine.productId" />
    <column path="transmission.productId" />
  </columns>
  <table domain="Product" alias="car" key="key"/>
  <innerJoin path="car.classificationGroupAssociations">
    <table domain="Product2ClassificationGroup" alias="cga" key="key" />
  </innerJoin>
  <innerJoin path="cga.classificationGroup">
    <table domain="ClassificationGroup" alias="cg" key="key" />
  </innerJoin>
  <innerJoin path="car.status">
    <table domain="Status" alias="carStatus" key="key" />
  </innerJoin>
  <leftJoin path="car.attributeValues">
    <table domain="ProductAttributeValue" alias="shortDesc" key="key" />
    <with>shortDesc.attribute.key='ShortDescription' and shortDesc.language.key='nl'</with>
  </leftJoin>
  <leftJoin path="car.productRelations">
    <table domain="ProductRelation" alias="engineRelation" key="key" />
    <with>engineRelation.type.key='has_engine'</with>
  </leftJoin>
  <leftJoin path="engineRelation.relatedProduct">
    <table domain="Product" alias="engine" key="key" />
  </leftJoin>
  <leftJoin path="car.productRelations">
    <table domain="ProductRelation" alias="transmissionRelation" key="key" />
    <with>transmissionRelation.type.key='has_transmission'</with>
  </leftJoin>
  <leftJoin path="transmissionRelation.relatedProduct">
    <table domain="Product" alias="transmission" key="key" />
  </leftJoin>
  <where>car.catalog.productCatalogId='V2015' and cg.classificationGroupId='catalogCar'</where>
</hqlXml>
        """)
        Grid3View grid3View = simpleGrid3ViewService.buildView(grid3)
        render view: 'index', model: [grid3View: grid3View]
    }
}