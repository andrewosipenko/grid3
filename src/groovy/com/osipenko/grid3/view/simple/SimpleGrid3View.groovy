package com.osipenko.grid3.view.simple

import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.view.Grid3Cell
import com.osipenko.grid3.view.Grid3Row
import com.osipenko.grid3.view.Grid3View

/**
 * Created by osa on 7/1/2015.
 */
class SimpleGrid3View extends Grid3View {
    private List<Grid3Row> rows

    public SimpleGrid3View(Grid3 grid3, List<Grid3Row> rows){
        super(grid3)
        rows = rows
    }

    public List<Grid3Row> getRows(){
        return rows
    }
}
