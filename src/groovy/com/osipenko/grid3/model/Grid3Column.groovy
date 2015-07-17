package com.osipenko.grid3.model

import com.osipenko.grid3.view.Grid3Cell

/**
 * Created by osa on 17.08.2014.
 */
abstract class Grid3Column {
    final String path
    protected final String alias
    protected final String property
    protected final Grid3 grid3
    private int index

    public Grid3Column(String path, Grid3 grid3){
        this.path = path
        this.grid3 = grid3
        (alias, property) = path.split('\\.')
        index = assignPathIndex(path)
    }

    protected int assignPathIndex(String path){
        int res = grid3.columnPathes.indexOf(path)
        if(res > -1){
            return res
        }
        grid3.columnPathes << path
        return grid3.columnPathes.size() - 1
    }

    abstract Grid3Cell buildGrid3CellFromDataRow(Object[] row)
}
