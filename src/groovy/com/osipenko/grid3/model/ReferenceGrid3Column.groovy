package com.osipenko.grid3.model

/**
 * Created by osa on 7/1/2015.
 */
class ReferenceGrid3Column extends UpdatableGrid3Column {
    public Grid3 optionGrid3

    ReferenceGrid3Column(String path, Grid3 grid3, Grid3 optionGrid3) {
        super(path, grid3)
        this.optionGrid3 = optionGrid3
    }
}
