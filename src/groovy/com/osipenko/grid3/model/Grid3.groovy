package com.osipenko.grid3.model
class Grid3 {
    Grid3Table grid3Table
    Map<String, Grid3Table> aliasTableMap
    Grid3Column[] grid3Columns
    private List<String> columnPathes = []

    public Grid3Column getColumnByPath(String path){
        Grid3Column res = grid3Columns.find{it.path == path}
        if(!res){
            throw new IllegalArgumentException("Can't find column by path ${path}. Shall be one of: ${grid3Columns*.path}")
        }
        return res
    }

    public List<String> getColumnPathes(){
        return new ArrayList<String>(columnPathes)
    }

    public void addColumnPath(String columnPath){
        if(columnPath in columnPathes)
            return
        println "Adding columnPath ${columnPath}"
        columnPathes << columnPath
    }
}