package com.osipenko.grid3

import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.view.Grid3View

class Grid3Controller {
    def hqlGrid3Service
    def simpleGrid3ViewService
    /**
     * Queries database with <code>query</code>. Returns query execution result as JSON.
     *
     *
     * @param query required
     * @return JSON. Example:
     * {
         "type":"com.osipenko.grid3.model.ReadonlyGrid3Column",
         "index":4,
         "path":"transmission.transmissionId",
         "columns":[
             "brand.brandId",
             "model.modelId",
             "car.carId",
             "engine.engineId",
             "transmission.transmissionId"
         ],
         "rows":[
             [
                 "Ford",
                 "Focus",
                 "0001",
                 "1.0 Diesel",
                 "5 manual"
             ],
             [
                 "Ford",
                 "Focus",
                 "0002",
                 "1.2 Diesel",
                 "5 manual"
             ]
        ]
     }
     */
    def index(String query){
        Grid3 grid3 = hqlGrid3Service.buildGrid3(query)
        Grid3View grid3View = simpleGrid3ViewService.buildView(grid3)
        render (contentType:"text/json") {
            columns = grid3View.grid3.grid3Columns.collect{
                type = it.class
                index = it.index
                path = it.path
            }
            rows = grid3View.rows*.data
        }
    }

    def update(String query, String path, String value){
        Grid3 grid3 = hqlGrid3Service.buildGrid3(query)
        hqlGrid3Service.update(grid3, path, value)
    }
}