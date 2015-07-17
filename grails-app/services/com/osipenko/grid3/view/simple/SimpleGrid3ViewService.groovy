package com.osipenko.grid3.view.simple

import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.view.AbstractGrid3ViewService
import com.osipenko.grid3.view.Grid3Cell
import com.osipenko.grid3.view.Grid3Row
import com.osipenko.grid3.view.Grid3View
import grails.transaction.Transactional

class SimpleGrid3ViewService extends AbstractGrid3ViewService {
    def sessionFactory
    Grid3View buildView(Grid3 grid3){
        return new SimpleGrid3View(grid3: grid3, rows: loadRows(grid3))
    }
    private List<Grid3Row> loadRows(Grid3 grid3){
        def session = sessionFactory.openStatelessSession()
        try{
            return session.createQuery(grid3.hql)
            .list()
            .collect{ dataRow ->
                new Grid3Row(
                    cells: grid3.grid3Columns.collect{it.buildGrid3CellFromDataRow(dataRow)}
                )
            }
        } finally {
            session.managedFlush()
            session.close()
        }
    }
}
