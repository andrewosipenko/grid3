package com.osipenko.grid3.build

import com.osipenko.grid3.model.Grid3

abstract class AbstractGrid3Service {
    public abstract Grid3 buildGrid3(String xml)
    public abstract void update(Grid3 grid3, List originalRow, String path, String value)
}
