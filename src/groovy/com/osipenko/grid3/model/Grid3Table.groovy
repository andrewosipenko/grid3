package com.osipenko.grid3.model
class Grid3Table {
    final String domain
    final String alias
    final String key
    Grid3Table referrerGrid3Table
    String referrerProperty

    Grid3Table(String domain, String alias, String key) {
        this.domain = domain
        this.alias = alias
        this.key = key
    }

    void setReferrer(String property, Grid3Table grid3Table){
        this.referrerProperty = property
        this.referrerGrid3Table = grid3Table
    }
}