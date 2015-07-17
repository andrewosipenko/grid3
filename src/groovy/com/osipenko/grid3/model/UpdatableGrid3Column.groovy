package com.osipenko.grid3.model

import com.osipenko.grid3.view.Grid3Cell

/**
 * Created by osa on 7/1/2015.
 */
abstract class UpdatableGrid3Column extends Grid3Column {
    public UpdatableGrid3Column(String path){
        super(path)
    }
    public Grid3Cell buildGrid3CellFromDataRow(Object[] row){
        return new Grid3Cell(value: row[index])
    }
}
