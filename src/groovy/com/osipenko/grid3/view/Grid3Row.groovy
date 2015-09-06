package com.osipenko.grid3.view

/**
 * A value class to store a row received from DB.
 *
 * @author Andrew Osipenko
 */
public final class Grid3Row {
    private final Object[] data

    public Grid3Row(Object[] data){
        this.data = data
    }

    public Object getValue(int index){
        return data[index]
    }
}
