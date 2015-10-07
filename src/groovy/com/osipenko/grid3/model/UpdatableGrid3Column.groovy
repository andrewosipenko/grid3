package com.osipenko.grid3.model

import com.osipenko.grid3.view.Grid3Cell

/**
 * Created by osa on 7/1/2015.
 */
abstract class UpdatableGrid3Column extends Grid3Column {
    protected final Grid3Table grid3Table
    protected final String keyPath
    public final int keyIndex

    public UpdatableGrid3Column(String path, Grid3 grid3){
        super(path, grid3)
        grid3Table = grid3.aliasTableMap[alias]
        println "hello ${alias}"
        println "hello ${grid3.aliasTableMap}"
        keyPath = grid3Table.alias + '.' + grid3Table.key
        keyIndex = assignPathIndex(keyPath)
        grid3.addColumnPath(keyPath)
    }
}
