package com.osipenko.grid3.model

import com.osipenko.grid3.view.Grid3Cell

/**
 * Created by osa on 26.08.2014.
 */
class ReadonlyGrid3Column extends Grid3Column {
    ReadonlyGrid3Column(String path, Grid3 grid3) {
        super(path, grid3)
    }
    Grid3Cell buildGrid3CellFromDataRow(Object[] row){
        return new Grid3Cell(value: row[index])
    }
}
