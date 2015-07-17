package com.osipenko.grid3.view

import com.osipenko.grid3.model.Grid3

/**
 * Created by osa on 08.09.2014.
 */
abstract class Grid3View {
    public final Grid3 grid3

    public Grid3View(Grid3 grid3){
        this.grid3 = grid3
    }

    public abstract List<Grid3Row> getRows()
}
