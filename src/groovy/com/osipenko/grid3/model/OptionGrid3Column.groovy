package com.osipenko.grid3.model

/**
 * Created by osa on 9/17/2015.
 */
class OptionGrid3Column extends ReadonlyGrid3Column {
    String target
    OptionGrid3Column(String path, Grid3 grid3, String target) {
        super(path, grid3)
        this.target = target
    }
}
