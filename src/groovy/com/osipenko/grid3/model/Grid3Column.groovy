package com.osipenko.grid3.model

import com.osipenko.grid3.view.Grid3Cell
import com.osipenko.grid3.view.Grid3Row

/**
 * Created by osa on 17.08.2014.
 */
abstract class Grid3Column {
    final String path
    public final String alias
    public final String property
    protected final Grid3 grid3
    public final int index

    public Grid3Column(String path, Grid3 grid3){
        this.path = path
        this.grid3 = grid3
        (alias, property) = path.split('\\.')
        index = assignPathIndex(path)
        grid3.addColumnPath(path)
    }

    protected int assignPathIndex(String path){
        int res = grid3.columnPathes.indexOf(path)
        if(res > -1){
            return res
        }
        grid3.columnPathes << path
        return grid3.columnPathes.size()
    }

    public Object getValue(Grid3Row grid3Row){
        return grid3Row.getValue(index)
    }
}
