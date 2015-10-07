package com.osipenko.grid3.build.hql

import com.osipenko.grid3.build.AbstractGrid3Service
import com.osipenko.grid3.model.AuxiliaryGrid3Column
import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.model.Grid3Column
import com.osipenko.grid3.model.Grid3Table
import com.osipenko.grid3.model.OptionGrid3Column
import com.osipenko.grid3.model.ReadonlyGrid3Column
import com.osipenko.grid3.model.ReferenceGrid3Column
import com.osipenko.grid3.model.ValueGrid3Column
import com.osipenko.grid3.view.Grid3Row
import groovy.xml.XmlUtil
import org.hibernate.Session

class HqlGrid3Service extends AbstractGrid3Service {
    def sessionFactory

    /**
     *
     * @param hqlXml
     * <hqlXml>
     *   <columns>
     *     <column path="car.productId"/>
     *     <column path="shortDesc.value" editable="value" />
     *     <column path="engine.productId" editable="reference">
     *       <options>
     *         <hqlXml>
     *           <columns>
     *             <column path="engine2.key" target="row.engine.key"/>
     *             <column path="engine2.productId" target="row.engine.productId"/>
     *           </columns>
     *           <table domain="Product" alias="engine2" key="key"/>
     *           <where>engine2.catalog.productCatalogId='V2015' and engine2.type.key='engine'</where>
     *         </hqlXml>
     *       </options>
     *       <create>
     *         <ProductRelation>
     *           <product>row.car</product>
     *           <type>
     *             <hqlXml>
     *               <columns>
     *                 <column path="type2" />
     *               </columns>
     *               <table domain="Type" alias="type2" key="key"/>
     *               <where>type2.key='has_engine'</where>
     *             </hqlXml>
     *           </type>
     *           <relatedProduct>row.engine</relatedProduct>
     *         </ProductRelation>
     *       </create>
     *     </column>
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
    public HqlGrid3 buildGrid3(String hqlXml, Map<String, Object> parameters = null){
        assert hqlXml
        def xml = new XmlSlurper().parseText(hqlXml)

        HqlGrid3 res = new HqlGrid3()
        res.aliasTableMap = [:]
        res.grid3Table = parseTable(xml, res.aliasTableMap)

        List<String> hqlClauses = []

        hqlClauses << "SELECT CLAUSE PLACEHOLDER"
        hqlClauses << buildFromHqlClause(res.grid3Table)
        hqlClauses << buildInnerJoinHqlClause(xml, res.aliasTableMap)
        hqlClauses << buildLeftJoinHqlClause(xml, res.aliasTableMap)
        hqlClauses << buildWhereHqlClause(xml)

        res.grid3Columns = parseColumns(xml, res)
        hqlClauses[0] = buildSelectHqlClause(res)

        res.hql = hqlClauses.findAll{it}.join('\n')
        log.debug "HQL: ${res.hql}"
        println "HQL: ${res.hql}"

        return res
    }

    public void update(Grid3 grid3, List originalRow, String path, String value){
        Grid3Column grid3Column = grid3.grid3Columns.find{it.path == path}
        if(!grid3Column){
            throw new IllegalArgumentException("Can't find column by path ${path}." +
                "The following pathes are available: ${grid3.columnPathes.keySet()}")
        }
        Grid3Row grid3Row = new Grid3Row(originalRow)
        if(grid3Column instanceof AuxiliaryGrid3Column){
            // update reference
            updateReference(grid3, grid3Column, grid3Row, value)
        }
        else if (grid3Column instanceof ValueGrid3Column){
            // update value
            updateValue(grid3Column, grid3Row, value)
        }
        else{
            throw new IllegalStateException("Unsupported column type ${grid3Column.class}")
        }
    }

    private void updateReference(Grid3 grid3, AuxiliaryGrid3Column grid3Column, Grid3Row grid3Row, Object value){
        Grid3Table grid3Table = grid3.aliasTableMap[grid3Column.alias]
        Grid3Table referrerGrid3Table = grid3Table.referrerGrid3Table
        Grid3Column referrerKeyGrid3Column = grid3.getColumnByPath("${referrerGrid3Table.alias}.${referrerGrid3Table.key}")


//        def session = sessionFactory.openStatelessSession()
        def session = sessionFactory.openSession()
        try{
            def referenceObject = loadObject(grid3Table, value, session)

            def referrer = loadObject(referrerGrid3Table, grid3Row.getValue(referrerKeyGrid3Column.index), session)
            if(referrer){
                println "Changing ${grid3Table.referrerProperty} from ${referrer[grid3Table.referrerProperty][grid3Table.key]} to ${referenceObject[grid3Table.key]}"
                referrer[grid3Table.referrerProperty] = referenceObject

                session.update(referrer)
            }
            else{
                throw new IllegalArgumentException("Can't find referring object")
            }
        } finally {
//            session.managedFlush()
//            session.close()
            session.flush()
        }
    }

    private Object loadObject(Grid3Table grid3Table, Object key, def session){
        String hql = "from ${grid3Table.domain} where ${grid3Table.key} = ${key}"
        log.debug "Loading object with HQL: ${hql}"
        return session.createQuery(hql).uniqueResult()
    }

    private Object loadObjectForUpdate(Grid3Column grid3Column, Object key){
        Grid3Table grid3Table = grid3Column.grid3.aliasTableMap[grid3Column.alias]
        Session session = sessionFactory.openSession()
        try{
            String hql = "from ${grid3Table.domain} where ${grid3Table.key} = ${key}"
            Object res = session.createQuery(hql).uniqueResult()
            if(!res){
                throw new IllegalArgumentException("Cant find object in database, query: ${hql}")
            }
            return res
        }
        finally{
            session.close()
        }
    }

    private void updateValue(ValueGrid3Column grid3Column, Grid3Row grid3Row, String value){
        Object object = loadObjectForUpdate(grid3Column, grid3Row.getValue(grid3Column.keyIndex))
        object[grid3Column.property] = value
        object.save(failOnError : true)
    }

    private List<Grid3Column> parseColumns(def xml, Grid3 grid3){
        List<Grid3Column> res = xml.columns.column.collect{
            String editable = it.@editable.text()
            String path = it.@path.text()
            switch (editable){
                case 'value':
                    return new ValueGrid3Column(path, grid3)
                case 'reference':
                    return parseReferenceGrid3Column(it, grid3)
                case null:
                case '':
                    String target = it.@target.text()
                    if(target){
                        return new OptionGrid3Column(path, grid3, target)
                    }
                    else {
                        return new ReadonlyGrid3Column(path, grid3)
                    }
                default:
                    throw new RuntimeException("Unsupported 'editable' attribute value '${editable}'")
            }
        }
        List<String> auxiliaryPathes = (grid3.columnPathes - res*.path)
        log.debug "Auxiliary column pathes: ${auxiliaryPathes}"
        res.addAll auxiliaryPathes.collect{
            new AuxiliaryGrid3Column(it, grid3)
        }
        return res.sort{it.index}
    }

    private ReferenceGrid3Column parseReferenceGrid3Column(def xml, Grid3 grid3){
        String path = xml.@path.text()
        def hqlXml = xml.options.hqlXml
        Grid3 optionGrid3 = null
        if(hqlXml.isEmpty()){
            throw new IllegalArgumentException("hqlXml is not defined for column ${path}")
        }
        else{
            optionGrid3 = buildGrid3(XmlUtil.serialize(hqlXml))
        }
        return new ReferenceGrid3Column(path, grid3, optionGrid3)
    }

    private Grid3Table parseTable(def xml, Map aliasTableMap){
        String alias = xml.table.@alias.text()
        Grid3Table res = new Grid3Table(xml.table.@domain.text(), alias, xml.table.@key.text())
        aliasTableMap[alias] = res
        return res
    }

    private String buildSelectHqlClause(Grid3 grid3){
        return "select " + grid3.columnPathes.join(', ')
    }
    private String buildFromHqlClause(Grid3Table grid3Table){
        return "from ${grid3Table.domain} ${grid3Table.alias}"
    }
    private String buildInnerJoinHqlClause(def xml, Map aliasTableMap){
        return xml.innerJoin.collect{
            Grid3Table grid3Table = parseTable(it, aliasTableMap)
            String path = it.@path.text()
            def (referrerAlias, referrerProperty) = path.split('\\.')
            grid3Table.setReferrer(referrerProperty, aliasTableMap[referrerAlias])
            List res = ["inner join ${path} ${grid3Table.alias}"]
            return res.join(' ')
        }.join('\n')
    }
    private String buildLeftJoinHqlClause(def xml, Map aliasTableMap){
        return xml.leftJoin.collect{
            Grid3Table grid3Table = parseTable(it, aliasTableMap)
            String path = it.@path.text()
            println "path: $path"
            def (referrerAlias, referrerProperty) = path.split('\\.')
            grid3Table.setReferrer(referrerProperty, aliasTableMap[referrerAlias])
            List res = ["left outer join ${path} ${grid3Table.alias}"]
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
