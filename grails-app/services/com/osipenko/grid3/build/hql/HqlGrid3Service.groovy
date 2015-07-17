package com.osipenko.grid3.build.hql

import com.osipenko.grid3.build.AbstractGrid3Service
import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.model.Grid3Column
import com.osipenko.grid3.model.Grid3Table
import com.osipenko.grid3.model.ReadonlyGrid3Column

class HqlGrid3Service extends AbstractGrid3Service {
    /**
     *
     * @param hqlXml
     * <hqlXml>
     *   <columns>
     *     <column path="car.productId"/>
     *     <column path="shortDesc.value" update="value"/>
     *     <column path="engine.productId" update="reference"/>
     *   </columns>
     *   <table domain="Product" alias="car" key="key"/>
     *   <leftJoin path="car.attributeValues">
     *     <table domain="ProductAttributeValue" alias="shortDesc" key="key" />
     *     <with>shortDesc.attribute.key='nl'</with>
     *   </leftJoin>
     *   <leftJoin path="car.attributeValues">
     *     <table domain="ProductAttributeValue" alias="shortDesc" key="key" />
     *     <with>shortDesc.attribute.key='nl'</with>
     *   </leftJoin>
     *   <leftJoin path="car.productRelations">
     *     <table domain="ProductRelation" alias="engineRelation" key="key" />
     *     <with>engineRelation.type.key='has_engine'</with>
     *   </leftJoin>
     *   <leftJoin path="engineRelation.relatedProduct">
     *     <table domain="Product" alias="engine" key="key" />
     *   </leftJoin>
     *   <where>p.catalog.productCatalogId='V2015'</where>
     * </hqlXml>
     * @return
     */
    public HqlGrid3 buildGrid3(String hqlXml){
        def xml = new XmlSlurper().parseText(hqlXml)

        HqlGrid3 res = new HqlGrid3()
        res.aliasTableMap = [:]
        res.grid3Columns = parseColumns(xml, res)
        res.grid3Table = parseTable(xml, res.aliasTableMap)

        List<String> hqlClauses = []

        hqlClauses << buildSelectHqlClause(res)
        hqlClauses << buildFromHqlClause(res.grid3Table)
        hqlClauses << buildInnerJoinHqlClause(xml, res.aliasTableMap)
        hqlClauses << buildLeftJoinHqlClause(xml, res.aliasTableMap)
        hqlClauses << buildWhereHqlClause(xml)

        res.hql = hqlClauses.findAll{it}.join('\n')
        log.debug "HQL: ${res.hql}"

        return res
    }

    private List<Grid3Column> parseColumns(def xml, Grid3 grid3){
        grid3.columnPathes = []
        return xml.columns.column.collect{
            String updateMode = it.@update.text()
            String path = it.@path.text()
            switch (updateMode){
                case 'value':
                case 'reference':
                    throw new RuntimeException("Not implemented")
                case null:
                case '':
                    return new ReadonlyGrid3Column(path, grid3)
                default:
                    throw new RuntimeException("Unsupported update '${updateMode}'")
            }
        }
    }

    private Grid3Table parseTable(def xml, Map aliasTableMap){
        String alias = xml.table.@alias.text()
        Grid3Table res = new Grid3Table(xml.table.@domain.text(), alias, xml.table.@key.text())
        aliasTableMap[alias] = res
        return res
    }

    private List

    private String buildSelectHqlClause(Grid3 grid3){
        return "select " + grid3.columnPathes.join(', ')
    }
    private String buildFromHqlClause(Grid3Table grid3Table){
        return "from ${grid3Table.domain} ${grid3Table.alias}"
    }
    private String buildInnerJoinHqlClause(def xml, Map aliasTableMap){
        return xml.innerJoin.collect{
            Grid3Table grid3Table = parseTable(it, aliasTableMap)
            List res = ["inner join ${it.@path.text()} ${grid3Table.alias}"]
            return res.join(' ')
        }.join('\n')
    }
    private String buildLeftJoinHqlClause(def xml, Map aliasTableMap){
        return xml.leftJoin.collect{
            Grid3Table grid3Table = parseTable(it, aliasTableMap)
            List res = ["left outer join ${it.@path.text()} ${grid3Table.alias}"]
            if(!it.with.isEmpty()){
                res << "with ${it.with.text()}"
            }
            return res.join(' ')
        }.join('\n')
    }
    private String buildWhereHqlClause(def xml){
        if(!xml.where.isEmpty()) {
            return 'where ' + xml.where.text()
        }
    }
}
