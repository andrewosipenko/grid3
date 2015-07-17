package com.osipenko.grid3.build.hql

import com.osipenko.grid3.build.hql.HqlGrid3Service
import com.osipenko.grid3.model.Grid3
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Created by osa on 26.08.2014.
 */
@TestFor(HqlGrid3Service)
class HqlGrid3ServiceSpec extends Specification{
    void "test simplest query"(){
        when:
        Grid3 res = service.buildGrid3("""
          <hqlXml>
            <columns>
              <column path="car.productId"/>
            </columns>
            <table domain="Product" alias="car" key="key"/>
          </hqlXml>
        """)

        then:
        res.grid3Columns.size() == 1
        res.grid3Columns[0].alias == 'car'
        res.grid3Columns[0].property == 'productId'

        res.hql == """select car.productId
from Product car"""
    }
    void "test left outer join query"(){
        when:
        Grid3 res = service.buildGrid3("""
          <hqlXml>
            <columns>
              <column path="car.productId"/>
              <column path="shortDesc.value"/>
            </columns>
            <table domain="Product" alias="car" key="key"/>
            <leftJoin path='car.attributeValues'>
              <table domain="ProductAttributeValue" alias="shortDesc" key="key"/>
              <with>shortDesc.attribute.key = 'ShortDescription' and shortDesc.language.key = 'nl'</with>
            </leftJoin>
          </hqlXml>
        """)

        then:
        println res.hql
        res.hql == """select car.productId, shortDesc.value
from Product car
left outer join car.attributeValues shortDesc with shortDesc.attribute.key = 'ShortDescription' and shortDesc.language.key = 'nl'"""
    }

}
