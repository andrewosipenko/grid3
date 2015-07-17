package com.osipenko.grid3.view

import com.osipenko.grid3.model.Grid3
import grails.transaction.Transactional

abstract class AbstractGrid3ViewService {
    abstract Grid3View buildView(Grid3 grid3)
}
