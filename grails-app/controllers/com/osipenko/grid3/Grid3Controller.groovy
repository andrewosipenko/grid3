package com.osipenko.grid3

import com.osipenko.grid3.model.Grid3
import com.osipenko.grid3.model.OptionGrid3Column
import com.osipenko.grid3.model.ReferenceGrid3Column
import com.osipenko.grid3.view.Grid3View
import grails.converters.JSON

class Grid3Controller {
    def hqlGrid3Service
    def simpleGrid3ViewService
    /**
     * Queries database with <code>query</code>. Returns query execution result as JSON.
     *
     *
     * @param query required
     * @return JSON. Example:
     {
         "columns":[
             [
                "type" : "com.osipenko.grid3.model.ReadonlyGrid3Column",
                "index" : 1,
                "path" : "brand.brandId"
             ],
             [
                 "type" : "com.osipenko.grid3.model.ValueGrid3Column",
                 "index" : 2,
                 "path" : "car.carId"
             ],
             [
                 "type" : "com.osipenko.grid3.model.ReferenceGrid3Column",
                 "index" : 3,
                 "path" : "engine.engineId",
                 "target" : "engine.id"
             ]
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
        println "hello ${grid3View.grid3.grid3Columns}"
        renderGrid3View(grid3View)
    }
    private void renderGrid3View(Grid3View grid3View){
        render (contentType:"text/json") {
            columns = grid3View.grid3.grid3Columns.collect{
                def res = [
                    type: it.class,
                    index: it.index,
                    path: it.path
                ]
                if (it instanceof OptionGrid3Column) {
                    res.target = it.target
                }
                else if (it instanceof ReferenceGrid3Column) {
                    res.target = it.keyPath
                }
                return res
            }
            rows = grid3View.rows*.data
        }
    }
    /**
     *
     *
     * @param query original query used to construct the grid
     * @param originalRow row before editing
     * @param path
     */
    def referenceOptions(String query, String originalRow, String path){
        Grid3 grid3 = hqlGrid3Service.buildGrid3(query)
        ReferenceGrid3Column referenceGrid3Column = (ReferenceGrid3Column)grid3.getColumnByPath(path)

        Grid3View optionGrid3View = simpleGrid3ViewService.buildView(referenceGrid3Column.optionGrid3)
        renderGrid3View(optionGrid3View)
    }

    def update(String query, String originalRow, String path, String value) {
        log.info "Updating ${path} to ${value}"
        Grid3 grid3 = hqlGrid3Service.buildGrid3(query)
        hqlGrid3Service.update(grid3, JSON.parse(originalRow), path, value)
        render ""
    }
}